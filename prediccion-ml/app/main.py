"""
API de predicción de deserción — GymAssist.

Spring Boot y la app Flutter llaman a esta API por HTTP. Arrancar con:
    uvicorn app.main:app --host 0.0.0.0 --port 8000
(--host 0.0.0.0 permite que un celular en la misma red Wi-Fi la alcance)

Documentación interactiva (para probar): http://localhost:8000/docs
"""
from __future__ import annotations

import json
import threading
from datetime import date, timedelta
from typing import Any, Optional

import numpy as np
import pandas as pd
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, ConfigDict, Field

from . import entrenamiento
from .features import _a_fecha, calcular_features, features_legacy, matrices
from .modelo import RUTA_META, RUTA_MODELO, Predictor
from .optimizacion import optimizar

app = FastAPI(title="GymAssist - Predicción de deserción",
              description="Red neuronal LSTM (TensorFlow) + optimización de ofertas (Pyomo)",
              version="2.0")

# CORS: permite que la app Flutter (sobre todo en Chrome / Flutter web) llame
# directamente a esta API. En móvil/escritorio no hace falta, pero no estorba.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

_predictor: Predictor | None = None
_lock = threading.Lock()


def predictor() -> Predictor:
    global _predictor
    if _predictor is None:
        if not RUTA_MODELO.exists():
            raise HTTPException(503, "No hay modelo entrenado. Ejecuta: python -m scripts.entrenar")
        _predictor = Predictor()
    return _predictor


# ------------------------------------------------------------------- esquemas
class Asistencia(BaseModel):
    fecha: Any
    musculos: Optional[list[str]] = None


class Medicion(BaseModel):
    fecha: Any = None
    peso: Optional[float] = None
    estatura: Optional[float] = None


class Cliente(BaseModel):
    model_config = ConfigDict(extra="allow")
    id: str
    nombre: Optional[str] = None
    estado: Optional[str] = None
    fechaIngreso: Any = None
    fechaInicioMembresia: Any = None
    fechaFinMembresia: Any = None
    tipoPlan: Optional[str] = None
    duracionPlanMeses: Optional[int] = None
    precioPlan: Optional[float] = None
    asistencias: list[Asistencia] = Field(default_factory=list)
    mediciones: list[Medicion] = Field(default_factory=list)


class Lote(BaseModel):
    clientes: list[Cliente]
    fechaReferencia: Any = None


class SolicitudOfertas(BaseModel):
    clientes: list[Cliente]
    presupuesto: float = 500000
    maxOfertas: Optional[int] = None
    mesesValor: float = 6
    umbralMinimo: float = 0.3


# ------------------------------------------------------------- interpretación
def nivel_riesgo(p: float) -> str:
    if p >= 0.8:
        return "CRITICO"
    if p >= 0.6:
        return "ALTO"
    if p >= 0.3:
        return "MEDIO"
    return "BAJO"


def describir(variable: str, t: dict) -> str:
    """Frase entendible para el administrador del gimnasio."""
    a4, ap = int(t["asistencias_4_sem"]), int(t["asistencias_4_previas"])
    d = {
        "secuencia": f"Pasó de {ap} a {a4} visitas (4 semanas anteriores vs. últimas 4)" if a4 != ap
        else f"Asistencia estable ({a4} visitas cada 4 semanas)",
        "asistencias_4_sem": f"{a4} visitas en las últimas 4 semanas",
        "asistencias_4_previas": f"{ap} visitas en las 4 semanas anteriores",
        "tendencia_asistencia": f"La asistencia {'bajó' if a4 < ap else 'subió'} de {ap} a {a4} visitas"
        if a4 != ap else "Asistencia sin cambios",
        "dias_ultima_visita": f"Lleva {int(t['dias_ultima_visita'])} días sin venir"
        if t["dias_ultima_visita"] < 90 else "No registra visitas recientes",
        "semanas_activas_12": f"Fue al gimnasio {int(t['semanas_activas_12'])} de las últimas 12 semanas",
        "regularidad": "Asiste de forma irregular (a rachas)" if t["regularidad"] > 0.6 else "Asiste de forma regular",
        "antiguedad_meses": f"{int(t['antiguedad_meses'])} {'mes' if int(t['antiguedad_meses']) == 1 else 'meses'} de antigüedad",
        "pago_al_dia": "Tiene el pago al día" if t["pago_al_dia"] >= 0.5 else "La membresía está vencida / sin pagar",
        "dias_para_vencer": f"La membresía vence en {int(t['dias_para_vencer'])} días" if t["dias_para_vencer"] > 0
        else f"La membresía venció hace {abs(int(t['dias_para_vencer']))} días",
        "plan_trimestral": "Plan trimestral" if t["plan_trimestral"] >= 0.5 else "Plan mensual",
        "precio_mensual_miles": f"Paga ${int(t['precio_mensual_miles'] * 1000):,} al mes".replace(",", "."),
        "variedad_musculos": f"Entrenó {int(t['variedad_musculos'])} grupos musculares distintos este mes",
        "cambio_imc": "Está progresando (su IMC bajó)" if t["cambio_imc"] < -0.2
        else ("Su IMC subió" if t["cambio_imc"] > 0.2 else "Sin cambios en su IMC"),
        "tiene_mediciones": "Se ha tomado medidas recientemente" if t["tiene_mediciones"] >= 0.5
        else "No se ha tomado medidas recientemente",
    }
    return d.get(variable, variable)


def recomendar(p: float, t: dict, factores: list[dict], estado: str | None) -> str:
    if p < 0.3:
        if t["asistencias_4_sem"] >= 12:
            return "Cliente comprometido. Ofrecerle plan premium o descuento por referidos."
        return "Riesgo bajo. Mantener comunicación y seguimiento mensual."
    if estado and estado.upper() != "ACTIVO" and t["pago_al_dia"] < 0.5:
        return "Cliente inactivo. Incluirlo en campaña de reactivación con descuento."
    principales = [f["variable"] for f in factores if f["impacto"] > 0]
    prefijo = "URGENTE: " if p >= 0.8 else ""
    for v in principales:
        if v in ("pago_al_dia", "dias_para_vencer"):
            return prefijo + "Ofrecer plan de pago flexible o descuento por renovar antes del vencimiento."
        if v in ("secuencia", "asistencias_4_sem", "tendencia_asistencia", "dias_ultima_visita",
                 "semanas_activas_12", "regularidad"):
            return prefijo + "Contactarlo y ofrecer una sesión gratis con entrenador y una rutina nueva."
        if v == "precio_mensual_miles":
            return prefijo + "Ofrecer descuento o cambio a plan trimestral más económico."
        if v in ("variedad_musculos", "cambio_imc", "tiene_mediciones"):
            return prefijo + "Agendar valoración física y mostrarle su progreso para motivarlo."
    return prefijo + "Programar llamada para conocer sus motivos y ofrecer beneficios especiales."


def _predecir_lote(clientes: list[dict], fecha_ref=None) -> list[dict]:
    pr = predictor()
    X_seq, X_tab, dicts = matrices(clientes, _a_fecha(fecha_ref))
    probs, expl = pr.explicar(X_seq, X_tab, top=3)
    salida = []
    for c, p, t, factores, seq in zip(clientes, probs, dicts, expl, X_seq):
        p = float(p)
        for f in factores:
            f["descripcion"] = describir(f["variable"], t)
        salida.append({
            "id": c["id"],
            "nombre": c.get("nombre"),
            "probabilidadAbandono": round(p, 4),
            "prediccion": "Se va" if p >= pr.umbral else "Permanece",
            "riesgo": nivel_riesgo(p),
            "factores": factores,
            "recomendacion": recomendar(p, t, factores, c.get("estado")),
            "features": features_legacy(t),
            "featuresAvanzadas": {k: round(v, 3) for k, v in t.items()},
            "asistenciaSemanal": [int(x) for x in seq],
        })
    return salida


# ------------------------------------------------------------------ endpoints
@app.get("/salud")
def salud():
    ok = RUTA_MODELO.exists()
    meta = json.loads(RUTA_META.read_text(encoding="utf-8")) if RUTA_META.exists() else {}
    return {"estado": "ok" if ok else "sin_modelo", "modeloVersion": meta.get("version"),
            "entrenado": meta.get("entrenado"), "umbral": meta.get("umbral"),
            "metricas": {k: v for k, v in meta.get("metricas", {}).items() if k != "matriz_confusion"}}


@app.post("/predecir")
def predecir(cliente: Cliente):
    return _predecir_lote([cliente.model_dump()])[0]


@app.post("/predecir/lote")
def predecir_lote(lote: Lote):
    resultados = _predecir_lote([c.model_dump() for c in lote.clientes], lote.fechaReferencia)
    return {"total": len(resultados), "modeloVersion": predictor().meta.get("version"),
            "resultados": resultados}


@app.post("/optimizar-ofertas")
def optimizar_ofertas(sol: SolicitudOfertas):
    clientes = [c.model_dump() for c in sol.clientes]
    preds = _predecir_lote(clientes)
    for p in preds:
        p["features"] = p["featuresAvanzadas"]
    return optimizar(preds, sol.presupuesto, sol.maxOfertas, sol.mesesValor, sol.umbralMinimo)


@app.get("/modelo/metricas")
def metricas_modelo():
    ruta = entrenamiento.DIR_REPORTES / "metricas.json"
    if not ruta.exists():
        raise HTTPException(404, "Aún no hay métricas. Ejecuta: python -m scripts.entrenar")
    return json.loads(ruta.read_text(encoding="utf-8"))


@app.post("/reentrenar")
def reentrenar(lote: Lote):
    """
    Recibe los clientes reales del gimnasio y los etiqueta automáticamente:
      - Abandonó (1): su membresía venció hace más de 30 días y no renovó.
        Sus variables se calculan 15 días ANTES del vencimiento (cuando aún se podía retener).
      - Permaneció (0): tiene la membresía vigente y lleva más de 60 días inscrito.
        Sus variables se calculan como estaban hace 30 días.
    Los nuevos datos se agregan a datos/clientes_reales.csv y se reentrena la red.
    """
    global _predictor
    hoy = _a_fecha(lote.fechaReferencia) or date.today()
    filas, etiquetas, refs = [], [], []
    for c in (x.model_dump() for x in lote.clientes):
        fin = _a_fecha(c.get("fechaFinMembresia"))
        ingreso = _a_fecha(c.get("fechaIngreso"))
        if fin is None or ingreso is None:
            continue
        if fin < hoy - timedelta(days=30):
            ref, y = fin - timedelta(days=15), 1
        elif fin > hoy and (hoy - ingreso).days > 60:
            ref, y = hoy - timedelta(days=30), 0
        else:
            continue
        if ref <= ingreso:
            continue
        filas.append(c)
        etiquetas.append(y)
        refs.append(ref)

    if len(filas) < 20:
        return {"reentrenado": False, "mensaje": f"Solo {len(filas)} clientes con historial suficiente "
                                                 "(mínimo 20). Se sigue usando el modelo actual."}
    dfs = [entrenamiento.clientes_a_dataframe([c], [y], "real", r) for c, y, r in zip(filas, etiquetas, refs)]
    nuevos = pd.concat(dfs, ignore_index=True)
    if entrenamiento.RUTA_REALES.exists():
        viejos = pd.read_csv(entrenamiento.RUTA_REALES)
        nuevos = pd.concat([viejos[~viejos["id"].isin(nuevos["id"])], nuevos], ignore_index=True)
    nuevos.to_csv(entrenamiento.RUTA_REALES, index=False)

    with _lock:
        res = entrenamiento.ejecutar(comparar=False, verbose=0)
        _predictor = Predictor()
    red = res["modelos"]["Red híbrida LSTM (TensorFlow)"]
    return {"reentrenado": True, "clientesReales": int(len(nuevos)),
            "abandonos": int(nuevos["abandona"].sum()), "metricas": red,
            "modeloVersion": _predictor.meta.get("version")}
