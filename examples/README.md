# Ejemplos didacticos de SimpleUI

Esta coleccion esta pensada para aprender por comparacion. Cada ejemplo Desktop
tiene una version Android equivalente con la misma idea, los mismos nombres y la
misma API. La diferencia principal es el `import`, el tamano de la ventana y la
adaptacion a pantalla completa.

Los sketches estan agrupados por dificultad. Dentro de cada nivel se conservan
juntas las versiones Desktop y Android:

| Carpeta | Contenido |
|---|---|
| `BASIC` | Controles aislados, estado elemental e inicio rapido |
| `MEDIUM` | Datos, texto, formularios, vistas, calendario y fundamentos de SimpleCore |
| `ADVANCED` | Juegos, particulas, interaccion grafica y demostraciones integrales |

La ruta de un sketch se forma como `examples/NIVEL/NombreDelSketch`. Por
ejemplo, el primer boton Desktop esta en
`examples/BASIC/Desktop01Button/Desktop01Button.pde`.

Los sketches no necesitan declarar `mousePressed()`, `mouseDragged()`,
`mouseReleased()`, `keyPressed()` ni `keyTyped()`: SimpleUI registra y distribuye
esos eventos automaticamente.

Todos los ejemplos trabajan con una resolucion virtual compartida por SimpleUI
y SimpleCore. `FIT` muestra y centra toda la composicion, `FILL` llena conservando
la proporcion y puede recortar, `RESPONSIVE` aprovecha espacio adicional desde
la esquina superior izquierda y `STRETCH` llena sin recortar mediante escalas X/Y
independientes.

El renderer se elige en `settings()`: Desktop admite `JAVA2D` o `P2D`, mientras
Android Mode admite Android2D o `P2D`. La inicializacion usa la unica firma
`SimpleUI.initUI(this, fuente, tamano, UIScaleMode)`.

## Indice de controles

| Numero | Desktop | Android | Concepto principal |
|---:|---|---|---|
| 01 | `Desktop01Button` | `Android01Button` | Boton y evento `clicked` |
| 02 | `Desktop02ImageButton` | `Android02ImageButton` | Imagen generada por codigo |
| 03 | `Desktop03Label` | `Android03Label` | Texto actualizado en tiempo real |
| 04 | `Desktop04Indicator` | `Android04Indicator` | Estado visual binario |
| 05 | `Desktop05Checkbox` | `Android05Checkbox` | Valor booleano |
| 06 | `Desktop06Slider` | `Android06Slider` | Valor continuo |
| 07 | `Desktop07ProgressBar` | `Android07ProgressBar` | Progreso de una operacion |
| 08 | `Desktop08Dropdown` | `Android08Dropdown` | Seleccion desplegable |
| 09 | `Desktop09Tabs` | `Android09Tabs` | Secciones por pestanas |
| 10 | `Desktop10List` | `Android10List` | Lista desplazable y seleccion |
| 11 | `Desktop11Table` | `Android11Table` | Datos en filas y columnas |
| 12 | `Desktop12TextField` | `Android12TextField` | Entrada de una linea |
| 13 | `Desktop13TextArea` | `Android13TextArea` | Entrada de varias lineas |
| 14 | `Desktop14NumberField` | `Android14NumberField` | Numero, restricciones y rango |
| 15 | `Desktop15Panel` | `Android15Panel` | Agrupacion visual de contenido |
| 16 | `Desktop16Chat` | `Android16Chat` | Conversacion con metadatos |
| 17 | `Desktop17SignalMeter` | `Android17SignalMeter` | Intensidad RSSI |
| 18 | `Desktop18Calendar` | `Android18Calendar` | Fechas, eventos y seleccion |
| 26 | `Desktop26Switch` | `Android26Switch` | Interruptor booleano compacto |

## Miniaplicaciones (`MEDIUM`)

| Numero | Desktop | Android | Aprendizaje |
|---:|---|---|---|
| 19 | `Desktop19Login` | `Android19Login` | Formulario y validacion sencilla |
| 20 | `Desktop20Modal` | `Android20Modal` | Confirmar o cancelar una accion |
| 21 | `Desktop21Views` | `Android21Views` | Navegar entre dos `UIView` |

El login es una demostracion de interfaz. El campo de clave no esta enmascarado y
no debe usarse como sistema de autenticacion real.

## SimpleCore

| Numero | Desktop | Android | Aprendizaje |
|---:|---|---|---|
| 22 | `Desktop22CoreTaskLifecycle` | `Android22CoreTaskLifecycle` | Ciclo de vida de una `Task` |
| 23 | `Desktop23CoreBouncingGame` | `Android23CoreBouncingGame` | Resolucion virtual, teclado, toque y `text()` |
| 24 | `Desktop24CoreButtonParticles` | `Android24CoreButtonParticles` | Viewport compartido entre SimpleUI y SimpleCore |
| 25 | `Desktop25CoreHoverGraphic` | `Android25CoreTouchGraphic` | Detección unificada de contacto con `isTouched()` y `TouchPoint` |
| 27 | `Desktop27CoreKeyboard` | `Android27CoreKeyboard` | Teclado, `key()` y pulsaciones simultaneas |
| 28 | `Desktop28CoreText` | `Android28CoreText` | Texto, alineacion, color y alpha |
| 29 | `Desktop29CoreScreenFade` | `Android29CoreScreenFade` | Cambio de escena con `fadeOff()` y `fadeOn()` |
| 30 | `Desktop30CoreTouchPoints` | `Android30CoreTouchPoints` | Visualizacion de todos los contactos activos |
| 31 | `Desktop31CoreFingerDraw` | `Android31CoreFingerDraw` | Dibujo persistente con raton o varios dedos |
| 32 | `Desktop32CoreMultiTouchDrag` | `Android32CoreMultiTouchDrag` | Drag independiente de dos objetos con dos dedos |
| 33 | `Desktop33CoreTimers` | `Android33CoreTimers` | Temporizadores periodicos con `timer()` |
| 34 | `Desktop34CoreSceneCamera` | `Android34CoreSceneCamera` | Escena, objetivo, zona muerta y limites de camara |
| 35 | `Desktop35CoreAudio` | `Android35CoreAudio` | Reproduccion real de OGG y WAV con `Sound` |

## Orden recomendado en clase

1. Abrir las parejas 01, 05 y 06 para explicar controles, estado y eventos.
2. Continuar con 10, 12 y 18 para trabajar con datos y entrada del usuario.
3. Construir las miniaplicaciones 19, 20 y 21.
4. Introducir SimpleCore con 22 y convertir el concepto en movimiento con 23.
5. Practicar `key()` y `text()` por separado con 27 y 28.
6. Continuar con fading y contactos mediante 29 y 30.
7. Continuar con dibujo y drag multitactil en 31 y 32.
8. Practicar timers independientes con 33.
9. Construir un mundo con escena y camara en 34.
10. Terminar probando audio real en 35.

## Demostraciones adicionales

Ademas de los 70 sketches numerados, la distribucion contiene ocho
demostraciones generales:

| Desktop | Android | Contenido |
|---|---|---|
| `DesktopBasic` | `AndroidBasic` | Inicio rapido con controles basicos |
| `DesktopCalendar` | `AndroidCalendar` | Uso general del calendario |
| `DesktopSimpleCore` | `AndroidSimpleCore` | Integracion basica de SimpleCore |
| `DesktopUnifiedFeatures` | `AndroidUnifiedFeatures` | Resumen de funciones compartidas |

Las demostraciones `Basic` estan en `BASIC`; `Calendar` y `SimpleCore`, en
`MEDIUM`; y `UnifiedFeatures`, en `ADVANCED`.

Cada carpeta es un sketch independiente y puede abrirse directamente desde
Processing una vez instalada la libreria SimpleUI.
