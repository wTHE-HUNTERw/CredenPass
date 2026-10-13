# Guía de Contribución 🤝

¡Gracias por tu interés en colaborar con **Mis Credenciales**!

## 🌿 Flujo de Trabajo
1. Realiza un Fork de este repositorio.
2. Crea una rama para tu feature o corrección (`git checkout -b feature/nueva-funcionalidad`).
3. Realiza tus cambios asegurando respetar la arquitectura basada en Compose y Material 3.
4. Verifica que el proyecto compile sin errores ejecutando:
   ```bash
   ./gradlew assembleDebug
   ```
5. Haz commit con mensajes claros y descriptivos (`git commit -m "feat: agrega soporte para lector de código QR"`).
6. Sube los cambios a tu repositorio remoto (`git push origin feature/nueva-funcionalidad`).
7. Abre un Pull Request describiendo los cambios propuestos.

## 📐 Estándares de Código
- Código limpio en **Kotlin** siguiendo las convenciones de Android.
- Arquitectura unidireccional con Compose y `StateFlow`.
- Mantener cadenas de texto traducibles en `strings.xml`.
