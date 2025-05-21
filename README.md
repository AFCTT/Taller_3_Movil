# 🗺️ Taller 3 - App de Geolocalización en Tiempo Real

Este proyecto es una aplicación Android desarrollada en **Kotlin** con **Jetpack Compose**, **Google Maps** y **Firebase**, que permite:

- 📍 Ver tu ubicación en tiempo real en el mapa.
- 🧑‍🤝‍🧑 Ver a otros usuarios conectados, junto con su nombre y foto de perfil.
- 🌀 Las rutas de los usuarios cambian de color según su velocidad.
- 🟦 Las fotos de los usuarios se muestran como íconos circulares en el mapa (al estilo WhatsApp).
- 🔁 Puedes activar/desactivar el seguimiento de ubicación y si el mapa debe seguir tu movimiento.

## 🧰 Tecnologías utilizadas

- Kotlin + Jetpack Compose
- Firebase Authentication
- Firebase Realtime Database
- Firebase Storage (para fotos)
- Google Maps API
- Accompanist Permissions
- Material3

## 🧪 Funcionalidades principales

| Funcionalidad                       | Descripción                                                                 |
|------------------------------------|-----------------------------------------------------------------------------|
| 🔐 Login/Register                  | Autenticación con correo o Google                                           |
| 🖼️ Subir foto                      | Al registrarse puedes elegir tu foto y subirla                             |
| 🌍 Mapa interactivo                | Muestra tu ubicación y la de otros usuarios                                |
| 🎨 Ruta por velocidad             | Rutas de colores (azul/verde/rojo) según velocidad                         |
| 🔘 Iconos personalizados           | Cada usuario aparece en el mapa con su foto circular como ícono            |
| 🧭 Switch seguir ubicación         | Puedes activar o no el seguimiento automático de tu ubicación en el mapa   |
| 🔄 Actualización en tiempo real    | Todos los datos se sincronizan con Firebase                                |

## 🚦 Colores de velocidad
- 🔵 Velocidad baja (< 5 m/s)
- 🟢 Velocidad media (5 - 15 m/s)
- 🔴 Velocidad alta (> 15 m/s)

## 📝 Estructura del proyecto
```
├── screens
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   ├── MapScreen.kt
│   ├── ProfileScreen.kt
│   └── MenuScreen.kt
├── AppNavigation.kt
└── MainActivity.kt
```

## 📷 Vista previa
![Icono personalizado en mapa](https://your-screenshot-url.com)

## ✅ Requisitos para ejecución
- Tener configurado Firebase con Auth, Realtime Database y Storage.
- Agregar tu archivo `google-services.json` en la carpeta `/app`.
- API Key habilitada para Google Maps SDK en Google Cloud Console.

## ✨ Autores
**Juan David Sanchez - juandavid0420-rgb**
**Andres Centanaro - AFCTT**

¡Gracias por revisar este proyecto! 💙 Siéntete libre de usarlo y mejorarlo.
