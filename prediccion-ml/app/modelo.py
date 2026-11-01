"""
Modelo híbrido de TensorFlow para predecir la deserción de clientes.

Arquitectura:
    asistencia semanal (12 semanas) ──► LSTM(32) ──┐
                                                   ├─► Dense(32) ─► Dense(1, sigmoide) = P(abandono)
    variables del cliente (14)      ──► Dense(32) ─┘

- La LSTM lee la asistencia semana a semana y aprende patrones temporales
  (por ejemplo, alguien que venía 3 veces por semana y va bajando).
- La parte densa usa variables como pago al día, días para vencer, precio...
- La salida es una PROBABILIDAD (0 a 1), no sólo "Se va / Permanece".
"""
from __future__ import annotations

import json
import os
from pathlib import Path

import numpy as np

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
import tensorflow as tf  # noqa: E402
from tensorflow import keras  # noqa: E402

from .features import COLUMNAS_TABULARES, NOMBRES_LEGIBLES, SEMANAS  # noqa: E402

RAIZ = Path(__file__).resolve().parent.parent
DIR_MODELOS = RAIZ / "modelos"
RUTA_MODELO = DIR_MODELOS / "modelo_hibrido.keras"
RUTA_META = DIR_MODELOS / "metadatos.json"


# ------------------------------------------------------------------ construcción
def construir_modelo(n_tabular: int = len(COLUMNAS_TABULARES)) -> keras.Model:
    entrada_seq = keras.Input(shape=(SEMANAS, 1), name="asistencia_semanal")
    x1 = keras.layers.LSTM(32, name="lstm")(entrada_seq)
    x1 = keras.layers.Dropout(0.2)(x1)

    entrada_tab = keras.Input(shape=(n_tabular,), name="variables_cliente")
    x2 = keras.layers.Dense(32, activation="relu", name="densa_cliente")(entrada_tab)
    x2 = keras.layers.Dropout(0.2)(x2)

    x = keras.layers.Concatenate(name="union")([x1, x2])
    x = keras.layers.Dense(32, activation="relu", name="densa_final")(x)
    x = keras.layers.Dropout(0.2)(x)
    salida = keras.layers.Dense(1, activation="sigmoid", name="prob_abandono")(x)

    modelo = keras.Model([entrada_seq, entrada_tab], salida, name="GymAssist_Desercion")
    modelo.compile(
        optimizer=keras.optimizers.Adam(1e-3),
        loss="binary_crossentropy",
        metrics=[keras.metrics.AUC(name="auc"), keras.metrics.Recall(name="recall"),
                 keras.metrics.Precision(name="precision")],
    )
    return modelo


# --------------------------------------------------------------- normalización
class Normalizador:
    """Estandariza las variables tabulares (media 0, desviación 1). Se guarda en JSON."""

    def __init__(self, media=None, desv=None):
        self.media = None if media is None else np.asarray(media, dtype=np.float32)
        self.desv = None if desv is None else np.asarray(desv, dtype=np.float32)

    def ajustar(self, X_tab: np.ndarray) -> "Normalizador":
        self.media = X_tab.mean(axis=0)
        self.desv = X_tab.std(axis=0) + 1e-6
        return self

    def tab(self, X_tab: np.ndarray) -> np.ndarray:
        return ((X_tab - self.media) / self.desv).astype(np.float32)

    @staticmethod
    def seq(X_seq: np.ndarray) -> np.ndarray:
        return (X_seq / 7.0).astype(np.float32).reshape(-1, SEMANAS, 1)


# ------------------------------------------------------------------ entrenamiento
def entrenar(X_seq, X_tab, y, X_seq_val, X_tab_val, y_val, epocas=100, verbose=1, semilla=42):
    keras.utils.set_random_seed(semilla)
    norm = Normalizador().ajustar(X_tab)
    modelo = construir_modelo(X_tab.shape[1])

    # Pesos por clase: el dataset está desbalanceado (hay menos clientes que se van)
    pos = float(y.mean())
    pesos = {0: 0.5 / (1 - pos), 1: 0.5 / pos}

    historia = modelo.fit(
        [norm.seq(X_seq), norm.tab(X_tab)], y,
        validation_data=([norm.seq(X_seq_val), norm.tab(X_tab_val)], y_val),
        epochs=epocas, batch_size=64, class_weight=pesos, verbose=verbose,
        callbacks=[keras.callbacks.EarlyStopping(monitor="val_auc", mode="max", patience=12,
                                                 restore_best_weights=True)],
    )
    return modelo, norm, historia.history


def guardar(modelo, norm: Normalizador, umbral: float, metricas: dict, extra: dict | None = None):
    DIR_MODELOS.mkdir(parents=True, exist_ok=True)
    modelo.save(RUTA_MODELO)
    meta = {
        "version": extra.get("version") if extra else None,
        "columnas_tabulares": COLUMNAS_TABULARES,
        "semanas": SEMANAS,
        "media": norm.media.tolist(),
        "desv": norm.desv.tolist(),
        "umbral": float(umbral),
        "metricas": metricas,
        "tensorflow": tf.__version__,
    }
    if extra:
        meta.update(extra)
    RUTA_META.write_text(json.dumps(meta, indent=2, ensure_ascii=False), encoding="utf-8")


# --------------------------------------------------------------------- predicción
class Predictor:
    """Carga el modelo guardado y hace predicciones con explicación."""

    def __init__(self):
        self.modelo = keras.models.load_model(RUTA_MODELO)
        self.meta = json.loads(RUTA_META.read_text(encoding="utf-8"))
        self.norm = Normalizador(self.meta["media"], self.meta["desv"])
        self.umbral = float(self.meta["umbral"])
        # "Cliente de referencia" para la explicación: el promedio del entrenamiento
        self.ref_tab = np.asarray(self.meta["media"], dtype=np.float32)
        self.ref_seq = np.asarray(self.meta.get("secuencia_promedio", [1.5] * SEMANAS), dtype=np.float32)

    def _predecir(self, X_seq, X_tab) -> np.ndarray:
        if len(X_tab) == 0:
            return np.zeros(0)
        p = self.modelo.predict([self.norm.seq(X_seq), self.norm.tab(X_tab)],
                                batch_size=1024, verbose=0)
        return p.reshape(-1)

    def probabilidades(self, X_seq, X_tab) -> np.ndarray:
        return self._predecir(X_seq, X_tab)

    def explicar(self, X_seq, X_tab, top: int = 3) -> tuple[np.ndarray, list[list[dict]]]:
        """
        Explicación por oclusión (idea similar a SHAP): para cada variable,
        se reemplaza su valor por el del cliente promedio y se mide cuánto cambia
        la probabilidad. Si al "normalizar" la variable el riesgo BAJA, esa
        variable está empujando al cliente hacia el abandono.
        """
        n, k = X_tab.shape
        base = self._predecir(X_seq, X_tab)
        # Grupos de variables que se ocultan juntas (comparten información)
        grupos = {"secuencia": None}
        for j, col in enumerate(COLUMNAS_TABULARES):
            grupos[col] = [j]
        nombres = list(grupos.keys())

        seqs, tabs = [], []
        for nombre in nombres:
            s = X_seq.copy()
            t = X_tab.copy()
            if nombre == "secuencia":
                s[:] = self.ref_seq
            else:
                t[:, grupos[nombre]] = self.ref_tab[grupos[nombre]]
            seqs.append(s)
            tabs.append(t)
        p_ocl = self._predecir(np.concatenate(seqs), np.concatenate(tabs)).reshape(len(nombres), n)
        impacto = base[None, :] - p_ocl  # >0: la variable AUMENTA el riesgo

        explicaciones = []
        for i in range(n):
            orden = np.argsort(-np.abs(impacto[:, i]))
            lista = []
            for idx in orden[:top]:
                nombre = nombres[idx]
                lista.append({
                    "variable": nombre,
                    "factor": NOMBRES_LEGIBLES[nombre],
                    "impacto": round(float(impacto[idx, i]), 4),
                    "efecto": "aumenta el riesgo" if impacto[idx, i] > 0 else "reduce el riesgo",
                })
            explicaciones.append(lista)
        return base, explicaciones
