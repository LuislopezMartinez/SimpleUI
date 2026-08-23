# AndroidRendererIntegration

Prueba manual integral de SimpleUI 0.7.0 en Processing Android Mode con P2D.
La carpeta ya contiene `code/SimpleUI.jar` y los sonidos necesarios en `data`.

## Ejecución

1. Abre `AndroidRendererIntegration.pde` con Processing.
2. Selecciona Android Mode.
3. Ejecuta el sketch en un dispositivo físico.
4. Mantén el dispositivo en orientación vertical.

No requiere permisos de almacenamiento ni conexión a Internet: los sonidos se
leen desde los assets incluidos en el APK.

## Prueba de audio

- La cabecera debe mostrar `OGG OK · WAV OK`.
- `Play OGG`, pausa, continuar, stop y rebobinar controlan `2013_15.ogg`.
- El interruptor Loop activa y desactiva la repetición del OGG.
- El slider cambia simultáneamente el volumen de OGG y WAV.
- `Play WAV` reproduce `error.wav` desde el principio en cada pulsación.
- Posición, duración, estado y barra de progreso se actualizan durante la reproducción.

## Resto de integración

Desliza verticalmente fuera de los controles con scroll interno para recorrer
todo el contenido. Comprueba los cuatro modos de escala, señal, lista, tabla,
teclado, área multilínea, chat, calendario, modal y la `Task` animada.
