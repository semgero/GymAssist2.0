# Reporte de entrenamiento — Predicción de deserción GymAssist

Fecha: 2026-11-01T11:10:04
Clientes: 6000 (reales: 0) — tasa de abandono 34.6%
Conjunto de prueba: 900 clientes (no vistos en el entrenamiento)

> Datos sintéticos generados con app/simulador.py, porque la app todavía no tiene suficiente
> historial real. El endpoint /reentrenar incorpora clientes reales de MongoDB.

## Comparación de modelos

| Modelo | Exactitud | Precisión | Recall | F1 | ROC-AUC |
|---|---|---|---|---|---|
| Red híbrida LSTM (TensorFlow) | 0.811 | 0.698 | 0.801 | 0.746 | 0.881 |
| Árbol J48 (4 variables, modelo anterior) | 0.791 | 0.677 | 0.756 | 0.714 | 0.870 |
| Regresión logística | 0.824 | 0.735 | 0.768 | 0.752 | 0.885 |
| Random Forest | 0.816 | 0.699 | 0.820 | 0.754 | 0.886 |
| Gradient Boosting | 0.804 | 0.693 | 0.778 | 0.733 | 0.875 |

- **Recall**: de los clientes que realmente se van, cuántos detecta el modelo (lo más importante para retener).
- **Precisión**: de los que el modelo marca como "se va", cuántos se van de verdad.
- **ROC-AUC**: qué tan bien ordena a los clientes por riesgo (1 = perfecto, 0.5 = azar).

## Variables más importantes (red híbrida)

1. Asistencias últimas 4 semanas: 0.086
2. Pago al día: 0.046
3. Patrón de asistencia semanal: 0.045
4. Días para vencer la membresía: 0.043
5. Días desde la última visita: 0.033
6. Precio del plan: 0.033
7. Asistencias 4 semanas anteriores: 0.031
8. Irregularidad en la asistencia: 0.025

## Gráficas

- comparacion_modelos.png
- curvas_roc.png
- matrices_confusion.png
- curvas_entrenamiento.png
- importancia_variables.png
