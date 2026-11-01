"""
Simulador de clientes de gimnasio (dataset SINTÉTICO).

La app todavía no tiene suficiente historial real para entrenar una red
neuronal, así que generamos clientes realistas con:
  - historial de asistencia diario (con rachas, vacaciones y caídas),
  - membresía, tipo y precio de plan,
  - grupos musculares entrenados y mediciones de peso/estatura.

La etiqueta "abandona en los próximos 30 días" se genera con un modelo de
comportamiento oculto (que el modelo de ML NO ve directamente) + ruido.
Los clientes se guardan en el MISMO formato JSON que envía Spring Boot, y
luego pasan por app/features.py igual que los clientes reales.

IMPORTANTE para la sustentación: este dataset es sintético y debe decirse así.
Con el endpoint /reentrenar el modelo se va ajustando a los datos reales.
"""
from __future__ import annotations

from datetime import date, timedelta

import numpy as np

MUSCULOS = ["pecho", "espalda", "pierna", "hombro", "biceps", "triceps", "abdomen", "gluteo"]


def _sigmoide(z):
    return 1.0 / (1.0 + np.exp(-z))


def generar_cliente(rng: np.random.Generator, idx: int, fecha_ref: date) -> tuple[dict, int, float]:
    """Devuelve (cliente_json, etiqueta, probabilidad_real)."""
    semanas_hist = 26

    # --- perfil básico
    antig_dias = int(np.clip(rng.exponential(420), 7, 1800))
    ingreso = fecha_ref - timedelta(days=antig_dias)
    trimestral = rng.random() < 0.35
    if trimestral:
        precio = round(rng.uniform(180, 330)) * 1000
        duracion = 3
    else:
        precio = round(rng.uniform(70, 130)) * 1000
        duracion = 1
    precio_mensual_k = precio / duracion / 1000

    # --- comportamiento oculto
    base = float(np.clip(rng.gamma(3.0, 0.9), 0.2, 6.0))          # visitas/semana "normales"
    base *= 1 + 0.1 * min(antig_dias / 365, 2)                     # los antiguos son más constantes
    r = rng.random()
    if r < 0.55:
        tipo, inicio, tasa = "estable", None, 0.0
    elif r < 0.85:
        tipo = "decayendo"
        inicio = int(rng.integers(12, 25))                          # semana en que empieza a fallar
        tasa = float(rng.uniform(0.08, 0.45))
    else:
        tipo, inicio, tasa = "mejorando", None, 0.0

    lam = np.full(semanas_hist, base)
    t = np.arange(semanas_hist)
    if tipo == "decayendo":
        lam = np.where(t >= inicio, base * np.exp(-tasa * (t - inicio)), base)
    elif tipo == "mejorando":
        lam = base * (0.45 + 0.55 * t / (semanas_hist - 1))
    lam = lam * rng.lognormal(0, 0.25, semanas_hist)               # ruido semanal
    if rng.random() < 0.15:                                         # vacaciones / enfermedad
        v = int(rng.integers(0, semanas_hist - 2))
        lam[v:v + int(rng.integers(1, 3))] *= 0.1

    # semanas antes de inscribirse = 0
    for w in range(semanas_hist):
        inicio_semana = fecha_ref - timedelta(days=7 * (semanas_hist - w))
        if inicio_semana < ingreso - timedelta(days=6):
            lam[w] = 0

    conteos = np.minimum(rng.poisson(lam), 6)
    variado = rng.random() < min(0.9, 0.25 + base / 8)
    asistencias = []
    for w, n in enumerate(conteos):
        if n == 0:
            continue
        inicio_semana = fecha_ref - timedelta(days=7 * (semanas_hist - w) - 1)
        dias = sorted(rng.choice(7, size=int(n), replace=False))
        for d in dias:
            f = inicio_semana + timedelta(days=int(d))
            if ingreso <= f <= fecha_ref:
                k = 2 if variado else 1
                musculos = list(rng.choice(MUSCULOS if variado else MUSCULOS[:4], size=k, replace=False))
                asistencias.append({"fecha": f.isoformat(), "musculos": musculos})

    reciente = float(conteos[-4:].mean())
    previo = float(conteos[-8:-4].mean())

    # --- membresía (los que se están desenganchando tienden a no renovar a tiempo)
    max_dias = 90 if trimestral else 30
    p_vencida = 0.12 + (0.35 if tipo == "decayendo" else 0) + (0.15 if reciente < 1 else 0)
    if rng.random() < p_vencida:
        dias_vencer = int(rng.integers(-45, 1))
    else:
        dias_vencer = int(rng.integers(1, max_dias + 1))
    fin = fecha_ref + timedelta(days=dias_vencer)
    pago_al_dia = dias_vencer > 0

    # --- mediciones de peso/estatura
    mediciones = []
    cambio_imc = 0.0
    if rng.random() < 0.55 + 0.1 * (base > 3):
        estatura = rng.uniform(1.55, 1.90)
        peso0 = rng.uniform(55, 105)
        n_med = int(rng.integers(2, 5))
        tendencia_peso = rng.normal(-1.5 if (tipo != "decayendo" and base > 2.5) else 0.8, 1.0)
        for i in range(n_med):
            f = fecha_ref - timedelta(days=int(110 - i * (100 / max(1, n_med - 1))))
            if f < ingreso:
                continue
            peso = peso0 + tendencia_peso * i / max(1, n_med - 1) + rng.normal(0, 0.3)
            mediciones.append({"fecha": f.isoformat(), "peso": round(float(peso), 1),
                               "estatura": round(float(estatura * 100))})  # en cm, como suele guardarse
        if len(mediciones) >= 2:
            p0, p1 = mediciones[0]["peso"], mediciones[-1]["peso"]
            cambio_imc = (p1 - p0) / (estatura ** 2)

    dias_ultima = 90
    if asistencias:
        dias_ultima = (fecha_ref - date.fromisoformat(asistencias[-1]["fecha"])).days

    # --- etiqueta: ¿abandona en los próximos 30 días?
    z = (
        0.55
        - 0.75 * reciente
        + 0.35 * max(0.0, previo - reciente)
        + (1.4 * min(1.0, tasa * 4) if tipo == "decayendo" else 0.0)
        + 1.5 * (not pago_al_dia)
        + 0.7 * (0 < dias_vencer <= 10)
        - 0.32 * np.log1p(antig_dias / 30)
        + 0.55 * (precio_mensual_k - 95) / 35
        - 0.45 * trimestral
        - 0.5 * (cambio_imc < -0.3)
        - 0.12 * (4 if variado else 1)
        + 0.02 * min(dias_ultima, 60)
        + rng.normal(0, 0.55)
    )
    p = float(_sigmoide(z))
    y = int(rng.random() < p)

    cliente = {
        "id": f"sim-{idx:05d}",
        "nombre": f"Cliente simulado {idx}",
        "estado": "ACTIVO" if pago_al_dia else "SUSPENDIDO",
        "fechaIngreso": ingreso.isoformat(),
        "fechaFinMembresia": fin.isoformat(),
        "tipoPlan": "Trimestral" if trimestral else "Mensual",
        "duracionPlanMeses": duracion,
        "precioPlan": precio,
        "asistencias": asistencias,
        "mediciones": mediciones,
        "fechaReferencia": fecha_ref.isoformat(),
    }
    return cliente, y, p


def generar_dataset(n: int = 6000, semilla: int = 42, fecha_ref: date = date(2026, 10, 15)):
    rng = np.random.default_rng(semilla)
    clientes, etiquetas, probs = [], [], []
    for i in range(n):
        c, y, p = generar_cliente(rng, i, fecha_ref)
        clientes.append(c)
        etiquetas.append(y)
        probs.append(p)
    return clientes, np.array(etiquetas), np.array(probs)
