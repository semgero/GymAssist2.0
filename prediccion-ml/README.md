# Predicción de deserción — GymAssist (Python + TensorFlow)

Microservicio que reemplaza el árbol J48 de Weka por una **red neuronal híbrida LSTM** en
TensorFlow. Para cada cliente del gimnasio predice la **probabilidad de que abandone en los
próximos 30 días**, explica por qué y, con **Pyomo**, decide qué oferta de retención darle a
cada cliente sin pasarse de un presupuesto.

```
Flutter / Web  ──►  Spring Boot (8080)  ──HTTP──►  API Python FastAPI (8000)  ──►  Red LSTM (TensorFlow)
                    /api/gym-predicciones                /predecir/lote                 + Pyomo (ofertas)
```

El J48 de Weka se eliminó del proyecto. Si la API de Python está apagada, Spring Boot responde con un error 503 y un mensaje que explica cómo encenderla.

---

## 1. Instalación en Windows (una sola vez)

1. Instala **Python 3.11** desde https://www.python.org/downloads/ y marca la casilla
   **"Add python.exe to PATH"** durante la instalación.
2. Abre una terminal en VS Code (**Terminal → New Terminal**) y entra a la carpeta:
   ```powershell
   cd prediccion-ml
   py -3.11 -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt
   ```
   Si PowerShell no deja activar el entorno, ejecuta una vez:
   `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`, y vuelve a intentar.

## 2. Entrenar el modelo

```powershell
python -m scripts.entrenar
```

Esto entrena la red y la compara con el J48 y otros modelos. El modelo queda en `modelos/` y
las métricas y gráficas en `reportes/`. Tarda entre 1 y 3 minutos.

- `python -m scripts.entrenar --regenerar` vuelve a generar el dataset sintético.
- Ya se incluye un modelo entrenado. Si al arrancar la API sale un error al cargarlo (por ser
  otra versión de TensorFlow), basta con volver a entrenar.

## 3. Encender la API

```powershell
uvicorn app.main:app --port 8000
```

- Para probarla en el navegador: http://localhost:8000/docs
- Deja esta terminal abierta y arranca Spring Boot como siempre. Spring Boot usa
  `ml.api.url=http://localhost:8000`, configurado en `application.properties`.

---

## Endpoints de la API (Python)

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/salud` | Estado del modelo y métricas |
| POST | `/predecir` | Predicción de un cliente |
| POST | `/predecir/lote` | Predicción de muchos clientes (lo usa Spring Boot) |
| POST | `/optimizar-ofertas` | Mejor oferta por cliente según presupuesto (Pyomo) |
| POST | `/reentrenar` | Reentrena con clientes reales |
| GET | `/modelo/metricas` | Comparación de modelos |

## Endpoints en Spring Boot

| Método | Ruta | Cambio |
|---|---|---|
| GET | `/api/gym-predicciones/{gymId}` | Igual que antes, más `probabilidadAbandono`, `factores`, `asistenciaSemanal` y `modelo` |
| GET | `/api/gym-predicciones/cliente/{clienteId}` | Nuevo: detalle de un cliente |
| GET | `/api/gym-predicciones/{gymId}/ofertas?presupuesto=500000&maxOfertas=10` | Nuevo: ofertas optimizadas |
| POST | `/api/gym-predicciones/{gymId}/reentrenar` | Nuevo: reentrena con los clientes del gym |
| GET | `/api/gym-predicciones/modelo/estado` | Nuevo: estado y métricas del modelo |

La respuesta de `/api/gym-predicciones/{gymId}` conserva todos los campos anteriores
(`prediccion`, `prediccionOriginal`, `estadoRiesgo`, `features`, `recomendacion`, `estadisticas`),
así que **Flutter sigue funcionando sin cambios**. Los campos nuevos se pueden ir mostrando:

```json
{
  "nombre": "Ana",
  "prediccion": "No",
  "prediccionOriginal": "Se va",
  "estadoRiesgo": "CRITICO",
  "probabilidadAbandono": 0.93,
  "factores": [
    {"factor": "Días desde la última visita", "impacto": 0.116, "descripcion": "Lleva 54 días sin venir"},
    {"factor": "Asistencias últimas 4 semanas", "impacto": 0.09, "descripcion": "0 visitas en las últimas 4 semanas"}
  ],
  "asistenciaSemanal": [3,3,2,2,1,1,1,0,0,0,0,0],
  "recomendacion": "URGENTE: Contactarlo y ofrecer una sesión gratis con entrenador y una rutina nueva.",
  "modelo": "Red híbrida LSTM (TensorFlow) v20261101-111004"
}
```

---

## Cómo funciona

### Variables (app/features.py)
El J48 usaba 4 variables. Ahora se calculan **14 variables, más la secuencia semanal**, con datos
que la app ya guarda en MongoDB:

| Variable | De dónde sale |
|---|---|
| Asistencia semana a semana (12 semanas) → **LSTM** | `cliente.asistencias[].fecha` |
| Asistencias últimas 4 semanas y 4 anteriores, tendencia | `asistencias` |
| Días desde la última visita, semanas activas, regularidad | `asistencias` |
| Antigüedad | `fechaIngresoCliente` |
| Pago al día, días para vencer | `fechaFinMembresia` |
| Tipo de plan, precio mensual | `plan.duracion`, `plan.precio` |
| Variedad de grupos musculares | `asistencias[].musculos` |
| Cambio de IMC, tiene mediciones | colección `mediciones` (peso, estatura) |

### Modelo (app/modelo.py)
```
asistencia semanal (12×1) → LSTM(32) → Dropout ─┐
                                                 ├→ Dense(32, relu) → Dropout → Dense(1, sigmoide)
14 variables (estandarizadas) → Dense(32) → Dropout ─┘
```
- Pérdida *binary cross-entropy*, optimizador Adam, **pesos por clase** (hay menos clientes que se van)
  y *early stopping* según el AUC de validación.
- El umbral de decisión se elige maximizando F1 en validación, en lugar de usar 0.5 fijo.
- Niveles de riesgo según la probabilidad: BAJO < 30 % ≤ MEDIO < 60 % ≤ ALTO < 80 % ≤ CRÍTICO.

### Explicación de cada predicción
Se usa explicación por oclusión, una idea similar a SHAP: cada variable se reemplaza por la del
"cliente promedio" y se mide cuánto cambia la probabilidad. Las 3 variables que más la mueven son
los `factores` que ve el administrador.

### Optimización de ofertas (app/optimizacion.py)
Programación lineal entera con Pyomo y el solver HiGHS:

- max Σ x<sub>ij</sub> · (p<sub>i</sub> · e<sub>ij</sub> · v<sub>i</sub> − c<sub>ij</sub>)
- sujeto a: una opción por cliente, presupuesto total y capacidad del personal.

Donde p<sub>i</sub> es la probabilidad que da la red, v<sub>i</sub> el valor del cliente,
e<sub>ij</sub> la efectividad de la oferta según su perfil (por ejemplo, el pago flexible funciona
mejor con quien está atrasado) y c<sub>ij</sub> el costo.

### Datos
La app todavía no tiene suficiente historial real para entrenar una red neuronal, así que
`app/simulador.py` genera **6000 clientes sintéticos** con comportamiento realista: rachas,
vacaciones, clientes que se van desenganchando y renovaciones tardías. Todos pasan por el mismo
cálculo de variables que los clientes reales. **Esto debe aclararse en el documento y en la
sustentación.**

Con `/reentrenar`, los clientes reales se etiquetan solos y se suman al entrenamiento, con más peso:

- **Abandonó:** su membresía venció hace más de 30 días y no renovó.
- **Permaneció:** tiene la membresía vigente y lleva más de 60 días inscrito.

### Resultados (conjunto de prueba, 900 clientes)
Ver `reportes/reporte.md` y las gráficas en `reportes/`.

En este dataset todos los modelos quedan relativamente cerca (ROC-AUC ≈ 0.87–0.89), porque
asistencia reciente y pago al día explican gran parte del abandono. La red supera al J48 en
recall (detecta más clientes que se van) y en AUC. Además entrega lo que el J48 no puede:
probabilidad, explicación por cliente, lectura del patrón temporal y conexión con la optimización.

## Estructura

```
prediccion-ml/
├── app/
│   ├── features.py       cálculo de variables (igual para entrenar y predecir)
│   ├── simulador.py      generador del dataset sintético
│   ├── modelo.py         red LSTM híbrida, guardar/cargar, explicación
│   ├── entrenamiento.py  pipeline + comparación de modelos + gráficas
│   ├── optimizacion.py   ofertas de retención con Pyomo
│   └── main.py           API FastAPI
├── scripts/entrenar.py
├── datos/        dataset sintético (CSV), versión ARFF para Weka, ejemplos JSON
├── modelos/      modelo_hibrido.keras + metadatos.json
└── reportes/     metricas.json, reporte.md, gráficas PNG
```
