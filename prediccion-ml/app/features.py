"""
Ingeniería de características (features) de GymAssist.

Convierte los datos "crudos" de un cliente (como vienen de MongoDB a través
de Spring Boot) en:
  1. Una SECUENCIA con las asistencias semana a semana (entrada de la LSTM).
  2. Un vector TABULAR con variables resumen (entrada de la parte densa).

Este mismo módulo se usa para generar el dataset de entrenamiento y para
predecir en producción, así el modelo siempre recibe los datos de la misma forma.
"""
from __future__ import annotations

from datetime import date, datetime, timedelta
from typing import Any

import numpy as np

SEMANAS = 12  # semanas de historial que lee la LSTM

# Orden fijo de las variables tabulares (¡no cambiar sin reentrenar!)
COLUMNAS_TABULARES = [
    "asistencias_4_sem",       # visitas en las últimas 4 semanas (la del modelo J48)
    "asistencias_4_previas",   # visitas en las 4 semanas anteriores (semanas 5-8)
    "tendencia_asistencia",    # cambio relativo entre ambos bloques (-1 = dejó de venir)
    "dias_ultima_visita",      # días desde la última vez que fue
    "semanas_activas_12",      # semanas (de 12) con al menos 1 visita
    "regularidad",             # coeficiente de variación semanal (alto = va a rachas)
    "antiguedad_meses",        # meses desde que se inscribió (la del modelo J48)
    "pago_al_dia",             # 1 = membresía vigente (la del modelo J48)
    "dias_para_vencer",        # días que faltan para que venza la membresía
    "plan_trimestral",         # 1 = plan trimestral, 0 = mensual (la del modelo J48)
    "precio_mensual_miles",    # precio mensual equivalente, en miles de COP
    "variedad_musculos",       # grupos musculares distintos entrenados (últimas 4 sem.)
    "cambio_imc",              # variación del IMC en las mediciones recientes
    "tiene_mediciones",        # 1 = se ha tomado medidas en los últimos 120 días
]

# Nombres legibles para explicar las predicciones
NOMBRES_LEGIBLES = {
    "secuencia": "Patrón de asistencia semanal",
    "asistencias_4_sem": "Asistencias últimas 4 semanas",
    "asistencias_4_previas": "Asistencias 4 semanas anteriores",
    "tendencia_asistencia": "Tendencia de asistencia",
    "dias_ultima_visita": "Días desde la última visita",
    "semanas_activas_12": "Semanas activas (de 12)",
    "regularidad": "Irregularidad en la asistencia",
    "antiguedad_meses": "Antigüedad",
    "pago_al_dia": "Pago al día",
    "dias_para_vencer": "Días para vencer la membresía",
    "plan_trimestral": "Tipo de plan",
    "precio_mensual_miles": "Precio del plan",
    "variedad_musculos": "Variedad de entrenamiento",
    "cambio_imc": "Progreso físico (IMC)",
    "tiene_mediciones": "Seguimiento de medidas",
}


# ----------------------------------------------------------------- utilidades
def _a_fecha(valor: Any) -> date | None:
    """Acepta date, datetime o texto ISO ('2026-10-01' o '2026-10-01T05:00:00Z')."""
    if valor is None or valor == "":
        return None
    if isinstance(valor, datetime):
        return valor.date()
    if isinstance(valor, date):
        return valor
    texto = str(valor).strip().replace("Z", "+00:00")
    try:
        return datetime.fromisoformat(texto).date()
    except ValueError:
        try:
            return date.fromisoformat(texto[:10])
        except ValueError:
            return None


def meses_del_plan(tipo_plan: str | None, duracion_meses: int | None = None) -> int:
    if duracion_meses and duracion_meses > 0:
        return int(duracion_meses)
    t = (tipo_plan or "").lower()
    if "anual" in t:
        return 12
    if "semes" in t:
        return 6
    if "trim" in t:
        return 3
    return 1


def es_trimestral(tipo_plan: str | None, duracion_meses: int | None = None) -> bool:
    """Igual que en Spring Boot: el J48 sólo conocía Mensual / Trimestral."""
    return meses_del_plan(tipo_plan, duracion_meses) >= 3


def _get(d: dict, *claves, defecto=None):
    """Lee un campo aceptando camelCase o snake_case."""
    for c in claves:
        if c in d and d[c] is not None:
            return d[c]
    return defecto


# ------------------------------------------------------------- cálculo principal
def calcular_features(cliente: dict, fecha_ref: date | None = None) -> tuple[np.ndarray, dict]:
    """
    Devuelve (secuencia, tabular_dict).

    secuencia: np.array de forma (SEMANAS,) con las visitas por semana,
               de la más antigua a la más reciente.
    """
    fecha_ref = _a_fecha(fecha_ref) or _a_fecha(_get(cliente, "fechaReferencia", "fecha_referencia")) or date.today()

    # --- asistencias
    fechas, musculos_por_fecha = [], []
    for a in _get(cliente, "asistencias", defecto=[]) or []:
        if isinstance(a, dict):
            f = _a_fecha(_get(a, "fecha"))
            m = _get(a, "musculos", defecto=[]) or []
        else:
            f, m = _a_fecha(a), []
        if f is not None and f <= fecha_ref:
            fechas.append(f)
            musculos_por_fecha.append(m)

    semanas = np.zeros(SEMANAS, dtype=np.float32)
    for f in fechas:
        hace = (fecha_ref - f).days
        idx = hace // 7  # 0 = semana más reciente
        if 0 <= idx < SEMANAS:
            semanas[SEMANAS - 1 - idx] += 1
    semanas = np.minimum(semanas, 7)

    a4 = float(semanas[-4:].sum())
    a4p = float(semanas[-8:-4].sum())
    tendencia = (a4 - a4p) / (a4p + 1.0)
    dias_ultima = float(min((fecha_ref - max(fechas)).days, 90)) if fechas else 90.0
    semanas_activas = float((semanas > 0).sum())
    regularidad = float(semanas.std() / (semanas.mean() + 0.5))

    musc = set()
    for f, m in zip(fechas, musculos_por_fecha):
        if (fecha_ref - f).days < 28:
            musc.update(str(x).strip().lower() for x in m)

    # --- membresía y plan
    ingreso = _a_fecha(_get(cliente, "fechaIngreso", "fechaIngresoCliente", "fecha_ingreso"))
    antiguedad = max(0.0, (fecha_ref - ingreso).days / 30.44) if ingreso else 0.0
    fin = _a_fecha(_get(cliente, "fechaFinMembresia", "fecha_fin_membresia"))
    pago = 1.0 if (fin is not None and fin > fecha_ref) else 0.0
    dias_vencer = float(np.clip((fin - fecha_ref).days, -60, 120)) if fin else -60.0

    tipo_plan = _get(cliente, "tipoPlan", "tipo_plan", "mensualidad", defecto="Mensual")
    duracion = _get(cliente, "duracionPlanMeses", "duracion_plan_meses")
    meses = meses_del_plan(tipo_plan, duracion)
    precio = float(_get(cliente, "precioPlan", "precio_plan", defecto=0) or 0)
    precio_mensual = (precio / meses / 1000.0) if precio > 0 else 100.0  # 100k COP si no se conoce

    # --- mediciones (IMC)
    imcs = []
    for m in _get(cliente, "mediciones", defecto=[]) or []:
        f = _a_fecha(_get(m, "fecha", "fechaRegistro"))
        peso = _get(m, "peso")
        est = _get(m, "estatura")
        if f is None or not peso or not est or f > fecha_ref or (fecha_ref - f).days > 120:
            continue
        est = float(est) / 100.0 if float(est) > 3 else float(est)  # cm -> m
        imcs.append((f, float(peso) / (est * est)))
    imcs.sort()
    cambio_imc = float(np.clip(imcs[-1][1] - imcs[0][1], -5, 5)) if len(imcs) >= 2 else 0.0

    tabular = {
        "asistencias_4_sem": a4,
        "asistencias_4_previas": a4p,
        "tendencia_asistencia": float(tendencia),
        "dias_ultima_visita": dias_ultima,
        "semanas_activas_12": semanas_activas,
        "regularidad": regularidad,
        "antiguedad_meses": float(round(antiguedad, 1)),
        "pago_al_dia": pago,
        "dias_para_vencer": dias_vencer,
        "plan_trimestral": 1.0 if es_trimestral(tipo_plan, duracion) else 0.0,
        "precio_mensual_miles": float(round(precio_mensual, 1)),
        "variedad_musculos": float(len(musc)),
        "cambio_imc": float(round(cambio_imc, 2)),
        "tiene_mediciones": 1.0 if imcs else 0.0,
    }
    return semanas, tabular


def matrices(clientes: list[dict], fecha_ref: date | None = None) -> tuple[np.ndarray, np.ndarray, list[dict]]:
    """Calcula features para varios clientes. Devuelve (X_seq, X_tab, lista_dicts)."""
    seqs, tabs, dicts = [], [], []
    for c in clientes:
        s, t = calcular_features(c, fecha_ref)
        seqs.append(s)
        tabs.append([t[k] for k in COLUMNAS_TABULARES])
        dicts.append(t)
    X_seq = np.array(seqs, dtype=np.float32).reshape(-1, SEMANAS)
    X_tab = np.array(tabs, dtype=np.float32).reshape(-1, len(COLUMNAS_TABULARES))
    return X_seq, X_tab, dicts


def features_legacy(t: dict) -> dict:
    """Las 4 variables del modelo J48 original, con el mismo formato que usaba Weka."""
    return {
        "asistencias4Semanas": int(t["asistencias_4_sem"]),
        "pagoAlDia": "Si" if t["pago_al_dia"] >= 0.5 else "No",
        "antiguedadMeses": int(t["antiguedad_meses"]),
        "tipoPlan": "Trimestral" if t["plan_trimestral"] >= 0.5 else "Mensual",
    }
