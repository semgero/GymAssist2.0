# API para la app Flutter (`/api/m/**`)

Backend: `https://gymassist20-production.up.railway.app` (Railway). Todas las respuestas son JSON.
Los errores llegan como `{"error": "mensaje"}` con su código HTTP (400, 401, 402, 403, 404, 409).

## Autenticación (JWT)
Login → el backend devuelve un `token`. En las demás peticiones va en la cabecera:
`Authorization: Bearer <token>`. Dura 72 h (`JWT_EXPIRATION_HOURS`). La clave sale de `JWT_SECRET`
(ponla en Railway; si falta, se genera una temporal y los tokens se pierden en cada reinicio).

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/m/auth/login` | `{username, password}` | `{token, username, email, role, tieneGimnasio, gimnasio, cliente}` |
| POST | `/api/m/auth/register` | `{username, email, password}` | igual (crea un ADMIN) |
| GET | `/api/m/auth/me` | — | datos de la sesión |

## Administrador (`role = ADMIN`)
| Método | Ruta | Notas |
|---|---|---|
| GET/POST | `/api/m/admin/gimnasio` | POST `{nombre, nit, direccion, descripcion}` (una sola vez) |
| GET | `/api/m/admin/resumen` | totales por estado, por vencer, ingreso mensual estimado |
| GET | `/api/m/admin/dashboard` | los mismos datos del dashboard web |
| PUT | `/api/m/admin/cuenta` | `{correo, password?}` |
| GET/POST | `/api/m/admin/clientes` | POST `{nombre, correo, documento, telefono, planId, username, password}` |
| PUT | `/api/m/admin/clientes/{id}/estado` | `{estado: ACTIVO|PENDIENTE|SUSPENDIDO}` |
| POST | `/api/m/admin/clientes/{id}/pago-manual` | pago en efectivo: activa la membresía por la duración del plan |
| GET | `/api/m/admin/clientes/{id}/asistencias` | |
| GET | `/api/m/admin/ml/clientes` | clientes en el formato de la API de Python (FastAPI `/predecir/lote`) |
| GET/POST | `/api/m/admin/planes` | `{nombre, descripcion, precio, duracionMeses}` |
| PUT/DELETE | `/api/m/admin/planes/{id}` | |
| GET | `/api/m/admin/rutinas` | |
| POST | `/api/m/admin/rutinas` | multipart: `nombre, grupoMuscular, repeticiones, series, fotos[], descripciones[]` (las fotos van a S3) |
| PUT/DELETE | `/api/m/admin/rutinas/{id}` | PUT `{nombre, grupoMuscular, repeticiones, series}` |

## Cliente (`role = CLIENTE`)
| Método | Ruta | Notas |
|---|---|---|
| GET | `/api/m/cliente/perfil` | cliente + gimnasio + última medición |
| PUT | `/api/m/cliente/cuenta` | `{correo, username, password?}` → devuelve un `token` nuevo |
| GET | `/api/m/cliente/planes` | planes de su gimnasio |
| PUT | `/api/m/cliente/plan` | `{planId}` |
| POST | `/api/m/cliente/pago` | `{returnUrl?}` crea el cobro en Mercado Pago → `{initPoint}` (link que la app abre) |
| POST | `/api/m/cliente/pago/verificar` | `{paymentId}` consulta el pago en Mercado Pago y activa si está aprobado |
| POST | `/api/m/cliente/chatbot` | `{mensaje}` → `{respuesta}` |
| GET | `/api/m/cliente/rutinas?grupo=Pecho` | 402 si la membresía no está activa |
| GET/POST | `/api/m/cliente/asistencias` | POST `{musculos: ["Pecho", "Triceps"]}` (una por día) |
| GET/POST | `/api/m/cliente/mediciones` | POST `{peso, estatura}` (estatura en metros) |

## Imágenes (público)
`GET /api/m/imagenes?key=<nombreArchivo>` sirve la foto desde S3 a través del backend (sin CORS ni bucket público).

## Pagos con Mercado Pago
1. La app pide `POST /api/m/cliente/pago` y abre `initPoint`.
2. Mercado Pago avisa al webhook `/Api/Payment/webhook` y redirige a `/Api/Payment/success` (ambos públicos).
3. En los dos casos el backend **consulta el pago en Mercado Pago** y activa la membresía solo si está `approved`
   (`PagoMembresiaService`, no procesa dos veces el mismo pago). Fechas en hora de Colombia.
4. Si vino de Flutter web (`?app=http://localhost...`), redirige a la app con `?pago=aprobado|pendiente|rechazado`.

Grupos musculares válidos: `Pecho, Espalda, Cuadriceps, Femoral, Gluteos, Biceps, Triceps, Hombros`.

Código: `src/main/java/com/ProyectoAula/GymAssist/mobile/`.
