# Script para instalar la aplicación Mis Credenciales en tu celular conectado por USB
$adb = "C:\Users\erick\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "app\build\outputs\apk\debug\app-debug.apk"

Write-Host "Verificando dispositivo conectado..." -ForegroundColor Cyan
& $adb devices

Write-Host "`nInstalando APK en tu dispositivo..." -ForegroundColor Yellow
Write-Host "Si aparece una ventana en la pantalla de tu Xiaomi, presiona 'Instalar' o 'Permitir'..." -ForegroundColor Magenta

& $adb install -r $apk

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n¡Instalación exitosa! Abriendo aplicación..." -ForegroundColor Green
    & $adb shell am start -n com.example.credencialholder/.MainActivity
} else {
    Write-Host "`nHubo un detalle en la instalación." -ForegroundColor Red
    Write-Host "En tu celular Xiaomi:" -ForegroundColor Yellow
    Write-Host "1. Abre Ajustes -> Ajustes adicionales -> Opciones de desarrollador."
    Write-Host "2. Asegúrate de activar la opción 'Instalar vía USB'."
    Write-Host "3. Desbloquea la pantalla de tu celular y vuelve a ejecutar este script."
}
