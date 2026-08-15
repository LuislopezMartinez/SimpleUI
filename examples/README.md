# Ejemplos didacticos de SimpleUI

Esta coleccion esta pensada para aprender por comparacion. Cada ejemplo Desktop
tiene una version Android equivalente con la misma idea, los mismos nombres y la
misma API. La diferencia principal es el `import`, el tamano de la ventana y la
adaptacion a pantalla completa.

Los sketches no necesitan declarar `mousePressed()`, `mouseDragged()`,
`mouseReleased()`, `keyPressed()` ni `keyTyped()`: SimpleUI registra y distribuye
esos eventos automaticamente.

Todos los ejemplos definen una resolucion virtual con `SimpleUI.setMode()`.
Las variantes Android usan `UIScaleMode.RESPONSIVE` para aprovechar diferentes
relaciones de aspecto.

## Controles, de menor a mayor complejidad

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

## Miniaplicaciones

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
| 23 | `Desktop23CoreBouncingGame` | `Android23CoreBouncingGame` | Entidades de un minijuego |
| 24 | `Desktop24CoreButtonParticles` | `Android24CoreButtonParticles` | SimpleUI y SimpleCore juntos |
| 25 | `Desktop25CoreHoverGraphic` | `Android25CoreTouchGraphic` | Reaccion al raton o al dedo |

## Orden recomendado en clase

1. Abrir las parejas 01, 05 y 06 para explicar controles, estado y eventos.
2. Continuar con 10, 12 y 18 para trabajar con datos y entrada del usuario.
3. Construir las miniaplicaciones 19, 20 y 21.
4. Introducir SimpleCore con 22 y convertir el concepto en movimiento con 23.
5. Terminar combinando las dos partes de la libreria en 24 y 25.

## Demostraciones adicionales

Ademas de los 52 sketches numerados, la distribucion contiene ocho
demostraciones generales:

| Desktop | Android | Contenido |
|---|---|---|
| `DesktopBasic` | `AndroidBasic` | Inicio rapido con controles basicos |
| `DesktopCalendar` | `AndroidCalendar` | Uso general del calendario |
| `DesktopSimpleCore` | `AndroidSimpleCore` | Integracion basica de SimpleCore |
| `DesktopUnifiedFeatures` | `AndroidUnifiedFeatures` | Resumen de funciones compartidas |

Cada carpeta es un sketch independiente y puede abrirse directamente desde
Processing una vez instalada la libreria SimpleUI.
