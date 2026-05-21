from pyomo.environ import *
from pyomo.opt import SolverStatus, TerminationCondition
import json
import sys

# LEER JSON UNA SOLA VEZ
raw_json = sys.stdin.read()

print("JSON RECIBIDO:", raw_json, file=sys.stderr)

data = json.loads(raw_json)

# IMPORTANTE:
# Jackson está enviando nombres en minúscula
I = data["i"]
C = data["c"]
P = data["p"]
K = data["k"]
M = data["m"] / 100
E = data["e"]
Gmin = data["gmin"]
A = data["a"]
a1 = data["a1"]
a2 = data["a2"]
Umin = data["umin"]
T = data["t"]
R = data["r"] / 100

model = ConcreteModel()

model.x1 = Var(domain=NonNegativeIntegers)
model.x2 = Var(domain=NonNegativeIntegers)

model.obj = Objective(
    expr=((I - C) * model.x1) - (P * model.x2),
    sense=maximize
)

model.r1 = Constraint(expr=model.x1 + model.x2 <= K)

model.r2 = Constraint(expr=model.x1 >= model.x2)

model.r3 = Constraint(
    expr=model.x2 <= M * (model.x1 + model.x2)
)

# AQUÍ TENÍAS ERROR EN TU MODELO:
# decía x1 <= E
# debe ser x2 <= E
model.r4 = Constraint(expr=model.x2 <= E)

model.r5 = Constraint(
    expr=((I - C) * model.x1) - (P * model.x2) >= Gmin
)

model.r6 = Constraint(
    expr=(a1 * model.x1) + (a2 * model.x2) <= A
)

model.r7 = Constraint(expr=model.x1 >= Umin)

model.r8 = Constraint(expr=model.x1 + model.x2 <= T)

model.r9 = Constraint(
    expr=model.x1 >= R * (model.x1 + model.x2)
)

solver = SolverFactory('glpk')

results = solver.solve(model)

if (
    results.solver.status != SolverStatus.ok or
    results.solver.termination_condition != TerminationCondition.optimal
):

    print(json.dumps({
        "x1": 0,
        "x2": 0,
        "ganancia": 0,
        "ocupacion": 0
    }))

    sys.exit()

resultado = {
    "x1": int(value(model.x1)),
    "x2": int(value(model.x2)),
    "ganancia": round(value(model.obj), 2),
    "ocupacion": round(
        ((value(model.x1) + value(model.x2)) / K) * 100,
        2
    )
}

print(json.dumps(resultado))