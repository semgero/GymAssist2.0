"""
Optimización de ofertas de retención con Pyomo (programación lineal entera).

Une las dos partes de Python del proyecto: la PROBABILIDAD de abandono que da
la red neuronal alimenta un modelo de optimización que decide qué oferta darle
a cada cliente para retener el máximo de ingresos con un presupuesto limitado.

Modelo matemático
-----------------
    i ∈ clientes, j ∈ ofertas
    x[i,j] ∈ {0,1}    1 si al cliente i se le da la oferta j

    p_i    probabilidad de abandono (red neuronal)
    v_i    valor del cliente = precio mensual × meses esperados de permanencia
    e_ij   efectividad de la oferta j en el cliente i (reducción de p_i, según su perfil)
    c_ij   costo de la oferta j para el cliente i

    max   Σ_i Σ_j  x[i,j] · (p_i · e_ij · v_i − c_ij)         (ingreso retenido neto)
    s.a.  Σ_j x[i,j] = 1                         ∀ i           (una opción por cliente; "ninguna" incluida)
          Σ_i Σ_j c_ij · x[i,j] ≤ Presupuesto
          Σ_i Σ_{j≠ninguna} x[i,j] ≤ MaxOfertas                (capacidad del personal)
          x[i,j] = 0  si p_i < umbral mínimo y j ≠ ninguna
"""
from __future__ import annotations

from dataclasses import dataclass

from pyomo.environ import (Binary, ConcreteModel, Constraint, Objective, SolverFactory, Var, maximize,
                           value)


@dataclass
class Oferta:
    codigo: str
    nombre: str
    descripcion: str


OFERTAS = [
    Oferta("ninguna", "Sin oferta", "No se ofrece nada"),
    Oferta("descuento_10", "Descuento 10 %", "10 % de descuento en la próxima renovación"),
    Oferta("descuento_20", "Descuento 20 %", "20 % de descuento en la próxima renovación"),
    Oferta("sesion_entrenador", "Sesión con entrenador", "Sesión personalizada gratis + rutina nueva"),
    Oferta("pago_flexible", "Plan de pago flexible", "Pagar la membresía en 2 cuotas sin recargo"),
    Oferta("upgrade_trimestral", "Pasar a trimestral con descuento",
           "Cambiar a plan trimestral con 15 % de descuento"),
]
COSTO_SESION_ENTRENADOR = 25000
COSTO_PAGO_FLEXIBLE = 8000


def efectividad_y_costo(c: dict) -> dict[str, tuple[float, float]]:
    """
    Efectividad (cuánto reduce la probabilidad de abandono, 0-1) y costo en COP
    de cada oferta para un cliente, según sus características.
    """
    t = c["features"]
    precio_mes = t["precio_mensual_miles"] * 1000
    baja_asistencia = t["asistencias_4_sem"] < 6 or t["tendencia_asistencia"] < -0.3
    sin_pago = t["pago_al_dia"] < 0.5
    mensual = t["plan_trimestral"] < 0.5
    antiguo = t["antiguedad_meses"] >= 6

    res = {"ninguna": (0.0, 0.0)}
    res["descuento_10"] = (0.12 + 0.06 * sin_pago, 0.10 * precio_mes)
    res["descuento_20"] = (0.22 + 0.08 * sin_pago, 0.20 * precio_mes)
    res["sesion_entrenador"] = (0.30 if baja_asistencia else 0.10, COSTO_SESION_ENTRENADOR)
    res["pago_flexible"] = (0.28 if sin_pago else 0.04, COSTO_PAGO_FLEXIBLE)
    if mensual:
        res["upgrade_trimestral"] = (0.30 if antiguo else 0.15, 0.15 * precio_mes * 3)
    else:
        res["upgrade_trimestral"] = (0.0, 0.0)
    return res


def _solver():
    for nombre in ("appsi_highs", "highs", "glpk", "cbc"):
        try:
            s = SolverFactory(nombre)
            if s is not None and s.available(exception_flag=False):
                return nombre, s
        except Exception:  # noqa: BLE001
            continue
    return None, None


def optimizar(clientes: list[dict], presupuesto: float, max_ofertas: int | None = None,
              meses_valor: float = 6.0, umbral_minimo: float = 0.3) -> dict:
    """
    clientes: lista con {"id", "nombre", "probabilidadAbandono", "features": {...}}
    """
    I = list(range(len(clientes)))
    J = [o.codigo for o in OFERTAS]
    datos = [efectividad_y_costo(c) for c in clientes]
    p = [float(c["probabilidadAbandono"]) for c in clientes]
    v = [c["features"]["precio_mensual_miles"] * 1000 * meses_valor for c in clientes]

    def ganancia(i, j):
        e, costo = datos[i][j]
        return p[i] * e * v[i] - costo

    nombre_solver, solver = _solver()
    if solver is not None and I:
        m = ConcreteModel()
        m.x = Var(I, J, domain=Binary)
        m.obj = Objective(expr=sum(ganancia(i, j) * m.x[i, j] for i in I for j in J), sense=maximize)
        m.una = Constraint(I, rule=lambda m, i: sum(m.x[i, j] for j in J) == 1)
        m.presupuesto = Constraint(expr=sum(datos[i][j][1] * m.x[i, j] for i in I for j in J) <= presupuesto)
        if max_ofertas is not None:
            m.capacidad = Constraint(expr=sum(m.x[i, j] for i in I for j in J if j != "ninguna") <= max_ofertas)
        for i in I:
            for j in J:
                inutil = j != "ninguna" and (p[i] < umbral_minimo or datos[i][j][0] <= 0)
                if inutil:
                    m.x[i, j].fix(0)
        solver.solve(m)
        eleccion = {i: next(j for j in J if (m.x[i, j].value or 0) > 0.5) for i in I}
        metodo = f"Pyomo + {nombre_solver}"
    else:
        # Respaldo si no hay solver instalado: algoritmo voraz (mejor ganancia por peso gastado)
        eleccion = {i: "ninguna" for i in I}
        candidatos = []
        for i in I:
            if p[i] < umbral_minimo:
                continue
            for j in J[1:]:
                g, costo = ganancia(i, j), datos[i][j][1]
                if g > 0 and datos[i][j][0] > 0:
                    candidatos.append((g / max(costo, 1), g, i, j))
        gastado, n = 0.0, 0
        for _, g, i, j in sorted(candidatos, reverse=True):
            if eleccion[i] != "ninguna" or gastado + datos[i][j][1] > presupuesto:
                continue
            if max_ofertas is not None and n >= max_ofertas:
                break
            eleccion[i] = j
            gastado += datos[i][j][1]
            n += 1
        metodo = "voraz (no se encontró solver; instala highspy)"

    info = {o.codigo: o for o in OFERTAS}
    asignaciones, costo_total, retenido_total = [], 0.0, 0.0
    for i in I:
        j = eleccion[i]
        e, costo = datos[i][j]
        retenido = p[i] * e * v[i]
        costo_total += costo
        retenido_total += retenido
        asignaciones.append({
            "id": clientes[i]["id"],
            "nombre": clientes[i].get("nombre"),
            "probabilidadAbandono": round(p[i], 4),
            "oferta": j,
            "ofertaNombre": info[j].nombre,
            "ofertaDescripcion": info[j].descripcion,
            "costo": round(costo),
            "probabilidadConOferta": round(p[i] * (1 - e), 4),
            "ingresoRetenidoEsperado": round(retenido),
        })
    asignaciones.sort(key=lambda a: (a["oferta"] == "ninguna", -a["probabilidadAbandono"]))
    return {
        "metodo": metodo,
        "presupuesto": presupuesto,
        "costoTotal": round(costo_total),
        "ingresoRetenidoEsperado": round(retenido_total),
        "retornoNeto": round(retenido_total - costo_total),
        "clientesConOferta": sum(1 for a in asignaciones if a["oferta"] != "ninguna"),
        "asignaciones": asignaciones,
    }
