"""
Pipeline de entrenamiento: genera/carga los datos, entrena el modelo híbrido
de TensorFlow, lo compara con otros modelos (incluido el árbol J48 anterior)
y guarda métricas y gráficas en reportes/.
"""
from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.ensemble import HistGradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (accuracy_score, average_precision_score, confusion_matrix, f1_score,
                             precision_score, recall_score, roc_auc_score, roc_curve)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.tree import DecisionTreeClassifier

from . import modelo as M
from .features import COLUMNAS_TABULARES, NOMBRES_LEGIBLES, SEMANAS, matrices
from .simulador import generar_dataset

RAIZ = Path(__file__).resolve().parent.parent
DIR_DATOS = RAIZ / "datos"
DIR_REPORTES = RAIZ / "reportes"
RUTA_SINTETICO = DIR_DATOS / "dataset_sintetico.csv"
RUTA_REALES = DIR_DATOS / "clientes_reales.csv"

COLS_SEQ = [f"semana_{i + 1}" for i in range(SEMANAS)]
COLS_J48 = ["asistencias_4_sem", "pago_al_dia", "antiguedad_meses", "plan_trimestral"]


# ------------------------------------------------------------------------ datos
def clientes_a_dataframe(clientes: list[dict], etiquetas, origen: str, fecha_ref=None) -> pd.DataFrame:
    X_seq, X_tab, _ = matrices(clientes, fecha_ref)
    df = pd.DataFrame(X_seq, columns=COLS_SEQ)
    df[COLUMNAS_TABULARES] = X_tab
    df.insert(0, "id", [c.get("id") for c in clientes])
    df["abandona"] = np.asarray(etiquetas, dtype=int)
    df["origen"] = origen
    return df


def cargar_o_generar_sintetico(n: int = 6000, regenerar: bool = False) -> pd.DataFrame:
    if RUTA_SINTETICO.exists() and not regenerar:
        return pd.read_csv(RUTA_SINTETICO)
    print(f"Generando dataset sintético de {n} clientes...")
    clientes, y, _ = generar_dataset(n)
    df = clientes_a_dataframe(clientes, y, "sintetico")
    DIR_DATOS.mkdir(parents=True, exist_ok=True)
    df.to_csv(RUTA_SINTETICO, index=False)
    exportar_arff_weka(df)
    # Guardamos unos ejemplos en formato JSON (como los manda Spring Boot) para pruebas
    (DIR_DATOS / "ejemplos_clientes.json").write_text(
        json.dumps({"clientes": clientes[:5]}, indent=2, ensure_ascii=False), encoding="utf-8")
    return df


def exportar_arff_weka(df: pd.DataFrame):
    """Mismo formato que el dataset.arff original, para poder comparar en Weka."""
    lineas = ["@relation gym_abandono_sintetico", "",
              "@attribute asistencias_4_sem numeric", "@attribute pago_al_dia {No,Si}",
              "@attribute antiguedad_meses numeric", "@attribute tipo_plan {Mensual,Trimestral}",
              "@attribute abandona_30dias(clase) {'Se va',Permanece}", "", "@data"]
    for _, r in df.iterrows():
        lineas.append(f"{int(r.asistencias_4_sem)},{'Si' if r.pago_al_dia else 'No'},"
                      f"{int(r.antiguedad_meses)},{'Trimestral' if r.plan_trimestral else 'Mensual'},"
                      f"{chr(39) + 'Se va' + chr(39) if r.abandona else 'Permanece'}")
    (DIR_DATOS / "dataset_weka_4_variables.arff").write_text("\n".join(lineas), encoding="utf-8")


def cargar_datos(regenerar: bool = False) -> pd.DataFrame:
    df = cargar_o_generar_sintetico(regenerar=regenerar)
    if RUTA_REALES.exists():
        reales = pd.read_csv(RUTA_REALES)
        print(f"Añadiendo {len(reales)} clientes reales al entrenamiento")
        # Los datos reales pesan más: se repiten 3 veces
        df = pd.concat([df] + [reales] * 3, ignore_index=True)
    return df


# --------------------------------------------------------------------- métricas
def metricas(y, p, umbral) -> dict:
    pred = (p >= umbral).astype(int)
    return {
        "accuracy": round(float(accuracy_score(y, pred)), 4),
        "precision": round(float(precision_score(y, pred, zero_division=0)), 4),
        "recall": round(float(recall_score(y, pred, zero_division=0)), 4),
        "f1": round(float(f1_score(y, pred, zero_division=0)), 4),
        "roc_auc": round(float(roc_auc_score(y, p)), 4),
        "pr_auc": round(float(average_precision_score(y, p)), 4),
        "umbral": round(float(umbral), 3),
        "matriz_confusion": confusion_matrix(y, pred).tolist(),
    }


def mejor_umbral(y, p) -> float:
    """Umbral que maximiza F1 en validación (mejor que 0.5 con clases desbalanceadas)."""
    candidatos = np.linspace(0.2, 0.8, 61)
    f1s = [f1_score(y, (p >= u).astype(int), zero_division=0) for u in candidatos]
    return float(candidatos[int(np.argmax(f1s))])


# ------------------------------------------------------------------- pipeline
def ejecutar(regenerar: bool = False, comparar: bool = True, verbose: int = 1, epocas: int = 100) -> dict:
    df = cargar_datos(regenerar)
    y = df["abandona"].values
    idx = np.arange(len(df))
    i_train, i_tmp = train_test_split(idx, test_size=0.3, stratify=y, random_state=42)
    i_val, i_test = train_test_split(i_tmp, test_size=0.5, stratify=y[i_tmp], random_state=42)

    S = df[COLS_SEQ].values.astype(np.float32)
    T = df[COLUMNAS_TABULARES].values.astype(np.float32)
    plano = np.hstack([T, S])  # para los modelos clásicos: variables + semanas

    print(f"Datos: {len(df)} clientes | abandono = {y.mean():.1%} | "
          f"train {len(i_train)} / val {len(i_val)} / test {len(i_test)}")

    resultados, curvas_roc = {}, {}

    # 1) Modelo híbrido LSTM (TensorFlow)
    print("\n=== Entrenando modelo híbrido LSTM + Densa (TensorFlow) ===")
    red, norm, historia = M.entrenar(S[i_train], T[i_train], y[i_train],
                                     S[i_val], T[i_val], y[i_val], epocas=epocas, verbose=verbose)
    pred_val = red.predict([norm.seq(S[i_val]), norm.tab(T[i_val])], verbose=0).reshape(-1)
    umbral = mejor_umbral(y[i_val], pred_val)
    p_red = red.predict([norm.seq(S[i_test]), norm.tab(T[i_test])], verbose=0).reshape(-1)
    resultados["Red híbrida LSTM (TensorFlow)"] = metricas(y[i_test], p_red, umbral)
    curvas_roc["Red híbrida LSTM (TensorFlow)"] = p_red

    if comparar:
        modelos = {
            "Árbol J48 (4 variables, modelo anterior)": (
                DecisionTreeClassifier(criterion="entropy", min_samples_leaf=2, ccp_alpha=0.002,
                                       random_state=42), COLS_J48),
            "Regresión logística": (make_pipeline(StandardScaler(), LogisticRegression(max_iter=2000)), None),
            "Random Forest": (RandomForestClassifier(n_estimators=300, min_samples_leaf=3,
                                                     random_state=42, n_jobs=-1), None),
            "Gradient Boosting": (HistGradientBoostingClassifier(random_state=42), None),
        }
        for nombre, (clf, cols) in modelos.items():
            print(f"Entrenando {nombre}...")
            X = df[cols].values if cols else plano
            clf.fit(X[i_train], y[i_train])
            u = mejor_umbral(y[i_val], clf.predict_proba(X[i_val])[:, 1])
            p = clf.predict_proba(X[i_test])[:, 1]
            resultados[nombre] = metricas(y[i_test], p, u)
            curvas_roc[nombre] = p

    # Importancia global de variables (oclusión promedio en test)
    meta_extra = {
        "version": datetime.now().strftime("%Y%m%d-%H%M%S"),
        "entrenado": datetime.now().isoformat(timespec="seconds"),
        "n_clientes": int(len(df)),
        "n_reales": int((df["origen"] == "real").sum()),
        "secuencia_promedio": S[i_train].mean(axis=0).tolist(),
    }
    M.guardar(red, norm, umbral, resultados["Red híbrida LSTM (TensorFlow)"], meta_extra)

    predictor = M.Predictor()
    _, expl = predictor.explicar(S[i_test], T[i_test], top=len(COLUMNAS_TABULARES) + 1)
    imp = {}
    for lista in expl:
        for e in lista:
            imp[e["factor"]] = imp.get(e["factor"], 0) + abs(e["impacto"])
    importancia = {k: round(v / len(expl), 4) for k, v in sorted(imp.items(), key=lambda x: -x[1])}

    DIR_REPORTES.mkdir(parents=True, exist_ok=True)
    salida = {"fecha": meta_extra["entrenado"], "datos": {
        "clientes": int(len(df)), "reales": meta_extra["n_reales"], "tasa_abandono": round(float(y.mean()), 4),
        "test": int(len(i_test))}, "modelos": resultados, "importancia_variables": importancia}
    (DIR_REPORTES / "metricas.json").write_text(json.dumps(salida, indent=2, ensure_ascii=False), encoding="utf-8")

    if comparar:
        graficas(resultados, curvas_roc, y[i_test], historia, importancia)
        escribir_reporte(salida)

    print("\n=== Resultados en el conjunto de prueba ===")
    tabla = pd.DataFrame({k: {m: v[m] for m in ["accuracy", "precision", "recall", "f1", "roc_auc"]}
                          for k, v in resultados.items()}).T
    print(tabla.to_string())
    return salida


# --------------------------------------------------------------------- gráficas
def graficas(resultados, curvas_roc, y_test, historia, importancia):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    azul, gris = "#2563eb", "#9ca3af"
    nombres = list(resultados.keys())

    # Comparación de modelos
    fig, ax = plt.subplots(figsize=(10, 5))
    mets = ["accuracy", "precision", "recall", "f1", "roc_auc"]
    ancho = 0.8 / len(nombres)
    colores = ["#2563eb", "#9ca3af", "#60a5fa", "#34d399", "#f59e0b"]
    for i, n in enumerate(nombres):
        ax.bar(np.arange(len(mets)) + i * ancho, [resultados[n][m] for m in mets], ancho,
               label=n, color=colores[i % len(colores)])
    ax.set_xticks(np.arange(len(mets)) + ancho * (len(nombres) - 1) / 2)
    ax.set_xticklabels(["Exactitud", "Precisión", "Recall", "F1", "ROC-AUC"])
    ax.set_ylim(0, 1)
    ax.set_title("Comparación de modelos (conjunto de prueba)")
    ax.legend(fontsize=8, loc="lower right")
    ax.grid(axis="y", alpha=0.3)
    fig.tight_layout()
    fig.savefig(DIR_REPORTES / "comparacion_modelos.png", dpi=130)
    plt.close(fig)

    # Curvas ROC
    fig, ax = plt.subplots(figsize=(6, 6))
    for i, (n, p) in enumerate(curvas_roc.items()):
        fpr, tpr, _ = roc_curve(y_test, p)
        ax.plot(fpr, tpr, label=f"{n} (AUC {resultados[n]['roc_auc']:.3f})",
                lw=2.5 if i == 0 else 1.5, color=colores[i % len(colores)])
    ax.plot([0, 1], [0, 1], "--", color=gris)
    ax.set_xlabel("Tasa de falsos positivos")
    ax.set_ylabel("Tasa de verdaderos positivos")
    ax.set_title("Curvas ROC")
    ax.legend(fontsize=7, loc="lower right")
    fig.tight_layout()
    fig.savefig(DIR_REPORTES / "curvas_roc.png", dpi=130)
    plt.close(fig)

    # Matrices de confusión: red vs J48
    fig, axes = plt.subplots(1, 2, figsize=(10, 4.5))
    for ax, n in zip(axes, [nombres[0], next((x for x in nombres if "J48" in x), nombres[0])]):
        cm = np.array(resultados[n]["matriz_confusion"])
        ax.imshow(cm, cmap="Blues")
        for (a, b), v in np.ndenumerate(cm):
            ax.text(b, a, str(v), ha="center", va="center", fontsize=14,
                    color="white" if v > cm.max() / 2 else "black")
        ax.set_xticks([0, 1], ["Permanece", "Se va"])
        ax.set_yticks([0, 1], ["Permanece", "Se va"])
        ax.set_xlabel("Predicción")
        ax.set_ylabel("Real")
        ax.set_title(n, fontsize=9)
    fig.tight_layout()
    fig.savefig(DIR_REPORTES / "matrices_confusion.png", dpi=130)
    plt.close(fig)

    # Curvas de entrenamiento
    fig, axes = plt.subplots(1, 2, figsize=(10, 4))
    axes[0].plot(historia["loss"], label="Entrenamiento", color=azul)
    axes[0].plot(historia["val_loss"], label="Validación", color="#f59e0b")
    axes[0].set_title("Pérdida (binary cross-entropy)")
    axes[1].plot(historia["auc"], label="Entrenamiento", color=azul)
    axes[1].plot(historia["val_auc"], label="Validación", color="#f59e0b")
    axes[1].set_title("AUC")
    for ax in axes:
        ax.set_xlabel("Época")
        ax.legend()
        ax.grid(alpha=0.3)
    fig.tight_layout()
    fig.savefig(DIR_REPORTES / "curvas_entrenamiento.png", dpi=130)
    plt.close(fig)

    # Importancia de variables
    fig, ax = plt.subplots(figsize=(8, 6))
    items = list(importancia.items())[::-1]
    ax.barh([k for k, _ in items], [v for _, v in items], color=azul)
    ax.set_title("Importancia de variables (red híbrida, oclusión)")
    ax.set_xlabel("Cambio promedio en la probabilidad de abandono")
    fig.tight_layout()
    fig.savefig(DIR_REPORTES / "importancia_variables.png", dpi=130)
    plt.close(fig)


def escribir_reporte(salida: dict):
    m = salida["modelos"]
    filas = "\n".join(
        f"| {n} | {v['accuracy']:.3f} | {v['precision']:.3f} | {v['recall']:.3f} | {v['f1']:.3f} | {v['roc_auc']:.3f} |"
        for n, v in m.items())
    imp = "\n".join(f"{i + 1}. {k}: {v:.3f}" for i, (k, v) in enumerate(list(salida["importancia_variables"].items())[:8]))
    texto = f"""# Reporte de entrenamiento — Predicción de deserción GymAssist

Fecha: {salida['fecha']}
Clientes: {salida['datos']['clientes']} (reales: {salida['datos']['reales']}) — tasa de abandono {salida['datos']['tasa_abandono']:.1%}
Conjunto de prueba: {salida['datos']['test']} clientes (no vistos en el entrenamiento)

> Datos sintéticos generados con app/simulador.py, porque la app todavía no tiene suficiente
> historial real. El endpoint /reentrenar incorpora clientes reales de MongoDB.

## Comparación de modelos

| Modelo | Exactitud | Precisión | Recall | F1 | ROC-AUC |
|---|---|---|---|---|---|
{filas}

- **Recall**: de los clientes que realmente se van, cuántos detecta el modelo (lo más importante para retener).
- **Precisión**: de los que el modelo marca como "se va", cuántos se van de verdad.
- **ROC-AUC**: qué tan bien ordena a los clientes por riesgo (1 = perfecto, 0.5 = azar).

## Variables más importantes (red híbrida)

{imp}

## Gráficas

- comparacion_modelos.png
- curvas_roc.png
- matrices_confusion.png
- curvas_entrenamiento.png
- importancia_variables.png
"""
    (DIR_REPORTES / "reporte.md").write_text(texto, encoding="utf-8")
