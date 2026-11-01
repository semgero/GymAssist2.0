"""
Entrena el modelo de deserción y genera el reporte comparativo.

Uso (desde la carpeta prediccion-ml):
    python -m scripts.entrenar              # usa el dataset existente (o lo genera)
    python -m scripts.entrenar --regenerar  # vuelve a generar el dataset sintético
"""
import argparse

from app.entrenamiento import ejecutar

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--regenerar", action="store_true", help="regenerar el dataset sintético")
    ap.add_argument("--epocas", type=int, default=100)
    args = ap.parse_args()
    ejecutar(regenerar=args.regenerar, epocas=args.epocas, verbose=2)
    print("\nListo. Modelo en modelos/ y reporte en reportes/")
