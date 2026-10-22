# Verificación T8 — Aprender jugando

Fecha: 20 de octubre de 2026 (Argentina). Rama: `feature/juego-interactivo`.
Estado: **T8 pendiente; funcionalidad sin aprobación final de integración**.

La autorización de este turno comprende T8 y la verificación final. Se revisaron
`AGENTS.md`, `spec.md`, `plan.md`, `tasks.md`, los fuentes y las evidencias de T1–T7,
sin tomar sus casillas como demostración. No se hacen commits ni push.

## Comandos oficiales

Ejecutados individualmente desde la raíz mediante el Wrapper, con JDK 21:

| Comando | Resultado | Evidencia local bajo `app/build/t8-verification/` |
| --- | --- | --- |
| `.\gradlew.bat :app:testDebugUnitTest` | Falló antes de ejecutar tests | `official-unit.log` |
| `.\gradlew.bat :app:assembleDebug` | Falló antes de producir APK | `official-assemble.log` |
| `.\gradlew.bat :app:lintDebug` | Falló antes de completar Lint | `official-lint.log` |
| `.\gradlew.bat :app:connectedDebugAndroidTest` | Falló antes de ejecutar tests | `official-connected.log` |

Las cuatro invocaciones terminan con código 1 en `:app:kaptDebugKotlin`.
La repetición diagnóstica `:app:kaptDebugKotlin --stacktrace` confirma
`Provided Metadata instance has version 2.2.0, while maximum supported version is 2.0.0`.
El stack pasa por el procesador de Room y su lector de metadatos
(`official-kapt-stacktrace.log`). Es el bloqueo previo documentado en T1–T7:
Room 2.6.1 con Kotlin 2.2.10. No se cambian dependencias para eludirlo.
El primer intento restringido no pudo escribir el bloqueo del caché Gradle;
las ejecuciones con acceso al caché sí alcanzaron y reprodujeron el error anterior.

## Evidencias previas contrastadas

Se leyeron los fuentes actuales de modelos, sesión, cargador, ViewModel,
actividad, menú, layouts, recursos, manifiesto y Gradle, y sus consumidores/tests.
Se revisaron los logs y XML de T1/T2 (`green.log`: 16 y 30 tests), T3/T4
(cargador e integridad del paquete), T5 (`final-android.xml`: 30 tests), T6
(`final-android.xml`: 44 tests) y T7 (`final-repeat-android.xml`: 47 tests,
`final-unit/`: 30 tests). Son evidencias históricas, con configuraciones
aisladas descritas en `tasks.md`; no prueban la configuración oficial.

Se repitió `verify-bank.ps1`: coinciden literalmente los 40 IDs, temas,
niveles, preguntas, respuestas y referencias con el banco aprobado, ocho
temas con cinco preguntas cada uno (`content-bank.log`). Se repitió
`verify-package.py`: 45 especies, IDs, rutas, WebP, dimensiones, hashes,
pesos y enlaces correctos; los 3.500 originales conservan nombres, bytes
y SHA-256 (`content-package.log`). El paquete ocupa 4.235.004 bytes.

Se inspeccionaron nuevamente las 15 planchas `comparison-01.jpg` a
`comparison-15.jpg` de T4, que contienen las 45 parejas original/WebP, junto
al registro individual de selección. Conservan los encuadres y rasgos
registrados; no se cambia la selección ni se infiere la validación solo de
las etiquetas. Se mantiene el registro individual de revisión visual de T4.

Una revisión independiente de solo lectura contrastó implementación,
tests y los 30 RF sin encontrar defectos de producción confirmados.
Señaló correctamente que las pruebas previas de recreación/store y permisos
sin cambios no prueban muerte real ni permisos denegados. No ejecutó Android.
`Declined to judge: []`. No hay hallazgos menores diferidos de producción.

## Verificación adicional en la copia compatible

Se usa el AVD existente `Medium_Phone_API_36.1`, Android 16/API 36 (36.1).
No hay dispositivo físico conectado ni imagen/AVD API 24/25 instalado;
esa franja queda obligatoriamente pendiente. Un intento de iniciar el mismo
AVD detectó que ya estaba abierto y salió sin reemplazarlo ni borrar datos.

Se preparó `app/build/t8-verification/android-check/` copiando el verificador
completo de T7, con applicationId exclusivo `com.example.avesargentinas.t8check`.
Conserva Room y las dependencias de producción, pero usa Kotlin/stdlib 2.0.21
y language/API 2.0 para poder realizar comprobaciones adicionales.
`source-match.txt` contrastó 196 archivos de fuentes, recursos, assets y tests
contra la aplicación, sin diferencias. Esta copia **no acredita compilación,
Lint ni pruebas integradas de la configuración oficial**. El test de ejemplo
Android que exige literalmente el applicationId original queda fuera del
filtro `ui.learning`; no se presenta como ejecutado.

Se añade una prueba instrumentada que completa ambas modalidades y repasos
con radios desactivadas, exige permisos denegados antes y después, comprueba
10/7/3 → 3/2/1 → 1/1/0, las tarjetas exactas y conservación al recrear y volver
del segundo plano durante el repaso. El test preserva los estados de radio.
La revisión detectó un caso de restauración con claves ausentes: se exige
un estado inicial 0/1 antes de modificarlo. También se espera la ausencia
efectiva de red predeterminada mediante `dumpsys connectivity`, cuya salida
se conserva como evidencia. Estos ajustes afectan solo a la prueba.

Wrapper sobre esa copia: `testDebugUnitTest assembleDebug connectedDebugAndroidTest`
con filtro de paquete `com.example.avesargentinas.ui.learning`, `--continue`:
**BUILD SUCCESSFUL**, 78 tareas ejecutadas. Resultado: **48 tests Android,
0 fallos, 0 errores, 0 omitidos** (18 de contenido, 18 de actividad, 12 de
ViewModel) y **30 unitarios, 0 fallos, 0 errores** (29 de sesión y el existente).
Evidencia: `isolated-final.log`, `suite-android.xml` y `final-unit/`.

Después de fortalecer únicamente la nueva prueba (precondición 0/1 antes
de tocar radios y espera de ausencia efectiva de red), se ejecutó otra vez
filtrada por `LearningGameActivityTest#bothModesCompleteRoundAndSuccessiveReviewsOfflineWithoutPermissions`:
**1 test, 0 fallos, 0 errores, 0 omitidos; BUILD SUCCESSFUL**
(`offline-final.log`, `offline-final.xml`). `offline-connectivity-final.txt`
contiene `Active default network: none` y `Current Networks:` vacío.
`permissions-final.txt` confirma cámara, ubicación precisa/aproximada y fotos
denegadas. El segundo contraste de los 196 archivos, incluido el test final,
también coincide (`source-match-final.txt`).

Las capturas actuales se guardan en `captures-suite/` y `captures-menu/`:
selector, pregunta/dorso, fotografía completa, nombres revelados, resumen
10/7/3, texto 200 %, tema oscuro y menú pequeño 720×1280 a 420 dpi en ambas
orientaciones. Se inspeccionaron directamente las imágenes, el árbol frontal
neutral y `talkback-services.txt`, que registra TalkBack conectado con
`FEEDBACK_SPOKEN`. El test opera la revelación mediante la acción accesible y
comprueba que no existan nombres ni rutas en el árbol frontal. **No se hizo
escucha humana ni recorrido manual completo con gestos de TalkBack**; eso se
registra como pendiente. Se descarta como evidencia visual aprobada
`large-landscape-actions.png`: capturó la transición diagonal de rotación del
sistema, aunque el test de alcance de controles pasó. Las capturas estables
del ensayo host complementan esa vista. Los archivos `unreachable-*` y
`talkback-unavailable-tree.txt` que existían en las carpetas temporales
compartidas son diagnósticos antiguos, no resultados de aprobación de T8.

También se ejecutó `lintDebug` en la copia: **falló**, con seis errores
preexistentes y 230 avisos (`copied-lint.log`, informe XML bajo
`android-check/build/reports/`). Son `GestureBackNavigation` y dos `NewApi`
en `ObservationLogActivity`, `windowLightNavigationBar` en ambos temas y
sangría en `FocusManager`. Sus archivos no tienen diferencias respecto de
HEAD. No hay errores en los archivos del juego o menú modificados. Este
diagnóstico tampoco aprueba Lint oficial ni autoriza cambios ajenos a T8.

El estado pendiente no se levantará sin comandos oficiales, las dos franjas
de API y la revisión accesible pendiente.

## Ciclo de vida: proceso real y cierre

`check-process.py` se ejecutó desde el host, fuera de la instrumentación y
del proceso de la app. Solo opera `com.example.avesargentinas.t8check`.
En cada modalidad se marcó la primera tarjeta y se dejó revelada la segunda:

| Modalidad | PID original | PID después de restaurar | Resultado |
| --- | --- | --- | --- |
| Preguntas | 8512 | 8953 | Conserva dorso/progreso al volver de Home y rotar; pierde sesión tras muerte real |
| Fotografías | 9247 | 9683 | Conserva nombre/dorso/progreso al volver de Home y rotar; pierde sesión tras muerte real |

En ambos casos se envió la app a Home, se ejecutó `am kill` exclusivamente
sobre el paquete temporal y se afirmó que `pidof` no devolvía ningún PID.
`dumpsys activity activities` conservaba la tarea y la actividad de juego con
`app=null`, antes de la reapertura normal desde el launcher. El nuevo proceso
mostró el selector, sin tarjeta ni resumen. No se usó `ActivityScenario.recreate`
como sustituto de esta comprobación. Las pruebas instrumentadas, separadamente,
comparan el estado completo, incluidas tarjetas, evaluaciones, repaso y resumen
al recrearse la actividad o volver del segundo plano.

También se creó otra sesión en cada modalidad, se cerró mediante
`am force-stop` del paquete temporal y se comprobó PID ausente y selector vacío
al reabrir. Este cierre es una detención forzada explícita; no se presenta
como un gesto de retirar la tarea de Recientes. La salida normal de actividades
y del juego se comprueba además con los tests de navegación.

Evidencias: `process-check.log`, `process-results.json`,
`learning_questions-dead-task.txt`, `learning_species-dead-task.txt`,
capturas/XML `*-before-background`, `*-after-background`, `*-after-rotation`,
`*-after-process-death` y `*-after-force-stop` bajo `app/build/t8-verification/`.
El primer intento (`process-check-first.log`) falló antes de matar el proceso
por leer el árbol anterior durante la transición a Home. Se confirmó por
`dumpsys` que el launcher estaba resumido y se sustituyó la comprobación
instantánea por una espera de la condición real; la repetición completa pasó.
Ese intento no cuenta como prueba aprobada ni como defecto de producción.

Se inspeccionaron las capturas estables `large-stable-dark-*`: texto 200 %
en vertical/horizontal, contenido desplazable y acciones accesibles sin
superposición con la barra de navegación. Las capturas de gran texto de la
suite usan una pregunta de fixture para probar insuficiencia y controles;
las del host usan el banco real. Las zonas fuera del visor se alcanzan por
desplazamiento, no se exige que toda una respuesta grande entre en una pantalla.

También se ejecutó `check-photo-text.py`: **aprobado**. Se inspeccionaron las
capturas `photo-large-dark-*` del frente y dorso fotográficos con texto 200 %,
en vertical y horizontal, incluidos ambos botones mediante desplazamiento.
La imagen mantiene proporciones y encuadre; cuando supera el visor horizontal
se recorre desplazando el contenido (`photo-text-check.log`).

## Alcance preservado, pendientes y resultado

Durante T8 no se modificaron fuentes de producción, dependencias, catálogo,
clasificador ni persistencia de observaciones. Se añadió únicamente la prueba
offline a `LearningGameActivityTest.kt`, este informe y el registro de tareas.
Los verificadores, APK, XML, capturas y logs permanecen ignorados en
`app/build/t8-verification/`; no deben versionarse. La copia usa otro package
y no opera datos ni permisos de la aplicación original. Se preservó la rama
y HEAD; no se hicieron commits, push ni otra rama/worktree.

Al terminar, `preferences-final.txt` confirma fuente 1.0, rotación automática 1,
rotación de usuario 0, escala de animadores predeterminada (`null`), servicios
de accesibilidad `null`/accesibilidad 0, modo avión 0, wifi 1, datos 1, tema
nocturno del sistema `no`, tamaño físico 1080×2400 sin override y densidad 420.
Las preferencias de captura instantánea se restauran con su presencia/valor
originales dentro de los tests; no se guardan modificaciones desde Opciones.
Desinstalado exclusivamente `com.example.avesargentinas.t8check` al terminar;
`cleanup.log` confirma que la aplicación original sigue instalada. Se deja
abierto el emulador que ya estaba disponible antes del ensayo.

**Aprobadas en la copia compatible API 36:** reglas, contenido real, navegación,
autoevaluación, resúmenes, repasos, errores, ambas modalidades offline y sin
permisos, conservación de actividad/fondo/rotación, descarte con proceso real
y cierre explícito, capturas claro/oscuro y ambas orientaciones, controles
con texto 200 %, árbol/activación TalkBack y animación desactivada API 26+.

**Fallidas:** los cuatro comandos oficiales (bloqueo Room/Kotlin, sin ejecución
integrada) y Lint de la copia (seis errores previos). El primer ensayo host
fallido queda conservado y fue reemplazado por una repetición completa aprobada.
No se confirmó un defecto de producción del alcance aprobado que requiriera
una corrección de código durante T8.

**Pendientes que impiden finalizar:**

1. Resolver el bloqueo previo Room/Kotlin y repetir las cuatro comprobaciones
   oficiales; los éxitos aislados no lo sustituyen.
2. Resolver o acordar el tratamiento de los errores de Lint preexistentes
   dentro de su propio alcance y obtener Lint oficial aprobado.
3. Ejecutar la matriz obligatoria en API 24 o 25; no hay dispositivo ni imagen
   instalados de esa franja. La ruta de animación API 24/25 solo se leyó.
4. Escucha y recorrido manual completo con gestos de TalkBack; la automatización
   verificó servicio real, árbol y revelación, no esa experiencia completa.

**T8 permanece pendiente y no se declara completada la funcionalidad.**

## Matriz de los 30 requisitos

La columna de evidencia indica qué verifica cada requisito; la aprobación
integrada de **todos** sigue bloqueada por los cuatro comandos oficiales.
Las pruebas aisladas permiten evaluar el comportamiento de los fuentes,
sin levantar ese bloqueo. Los nombres de métodos corresponden a las clases
`LearningSessionTest` (S), `LearningContentLoaderTest` (C),
`LearningGameViewModelTest` (V) y `LearningGameActivityTest` (A).

| RF | Comprobación y evidencia trazable | Límite de cierre |
| --- | --- | --- |
| 1 | A: `menuOpensBothModesAndReentryDiscardsRoundsAndSummaries`; botón real entre observaciones y opciones | Integración oficial pendiente |
| 2 | A: `explicitLaunchOffersBothModes`; selector con descripciones XML | Integración oficial pendiente |
| 3 | S: ronda de 10 preguntas; fotos agrupadas por especie; 21 semillas controladas | Integración oficial pendiente |
| 4 | A: `instantCaptureReturnsToMenuAndKeepsExistingDestinations`; preferencia restaurada | Integración oficial pendiente |
| 5 | C: `packagedQuestionsMatchAllFieldsOfTheApprovedBank`; comparación literal `content-bank.log`; A frente/dorso | Integración oficial pendiente |
| 6 | A: `speciesFrontAccessibilityDoesNotExposeAnswerAndBackDoes`, árbol TalkBack neutral; rutas PHOTO neutrales | Integración oficial pendiente |
| 7 | C: nombres resueltos desde catálogo/clave; A: dorso con ambos nombres, científico en cursiva | Integración oficial pendiente |
| 8 | S: agrupación por especie antes de sortear, incluidas varias fotos; C: 45 especies distintas | Integración oficial pendiente |
| 9 | S/A: progreso, frente al avanzar y reiniciar repaso | Integración oficial pendiente |
| 10 | A: activación de tarjeta y dos fases de giro; ruta inmediata sin animaciones | API 24/25 pendiente |
| 11 | S/A: giro ida/vuelta sin evaluación ni avance | Integración oficial pendiente |
| 12 | S/V: tokens obsoletos y doble evaluación; A: toques repetidos y giro interrumpido | Integración oficial pendiente |
| 13 | A: botones visibles solo desde dorso listo; foto cargada antes de evaluar | Integración oficial pendiente |
| 14 | S/A: cada token registra una evaluación, avanza al frente y llega al resumen | Integración oficial pendiente |
| 15 | Modelos/recursos y UI: autoevaluación explícita; sin corrección automática ni campos de respuesta | Integración oficial pendiente |
| 16 | S/A: resúmenes 10/7/3, 3/2/1 y 1/1/0; suma exacta por pasada | Integración oficial pendiente |
| 17 | S/A: `canReview`, botón visible con marcas | Integración oficial pendiente |
| 18 | S/A: listas iguales a las marcadas, misma foto, total 3 y luego 1; frente/contadores nuevos | Integración oficial pendiente |
| 19 | S/A: segundo repaso solo de la pasada recién terminada, sin acumulados | Integración oficial pendiente |
| 20 | S/A: cero marcas no inicia repaso y oculta el botón; jugar/salir disponibles | Integración oficial pendiente |
| 21 | S/V/A: nueva ronda limpia evaluaciones y fallos; mantiene modalidad | Integración oficial pendiente |
| 22 | A: `summaryActionsDiscardSessionAndExitFinishes`; elegir modalidad y menú descartan | Integración oficial pendiente |
| 23 | A: `backDiscardsRoundAndSummaryAndSelectorFinishes`; reentrada vacía desde menú | Integración oficial pendiente |
| 24 | Lectura completa de `ui/learning`: sin escrituras de partidas, historial ni observaciones; S/V: limpieza | Integración oficial pendiente |
| 25 | V/A: igualdad de estado completo al recrear ronda parcial, repaso, resumen y giro interrumpido; host: rotación real y Home/regreso en ambas modalidades | Aprobada en copia API 36; integración oficial pendiente |
| 26 | ViewModel sin estado serializado; host: PID ausente, tarea retenida, PID nuevo y selector vacío; cierre explícito/reapertura en ambas modalidades | Aprobada en copia API 36; integración oficial pendiente |
| 27 | A: prueba offline definitiva de ambas modalidades y repasos, sin red predeterminada; permisos denegados antes/después; assets locales/Glide sin caché de disco | Aprobada en copia API 36; integración oficial pendiente |
| 28 | C: inválidas/duplicados/rutas/archivo ilegible; S: reemplazo sin repetir, omisión en repaso, agotamiento; A: error real de Glide | Integración oficial pendiente |
| 29 | S: 0/1/9 y total ajustado; A: aviso previo y ronda de una; V: especies distintas | Integración oficial pendiente |
| 30 | C/V/A: error parcial/global, botones deshabilitados con cero, navegación y ausencia de resumen ficticio | Integración oficial pendiente |

## Nueve criterios de finalización

| Criterio | Evidencia disponible o comprobación | Estado de aceptación final |
| --- | --- | --- |
| 1. Acceso, modalidades, regreso y captura instantánea | A: menú y destinos existentes, preferencia preservada | Pendiente de integración oficial |
| 2. 40 preguntas y fotos revisadas de 45 especies | Comparación completa del banco, paquete/hashes y planchas de las 45 parejas; C: decodificación Android | Contenido contrastado; ejecución oficial pendiente |
| 3. Rondas de diez válidas sin repeticiones y excepción informada | S/C/A: selección e insuficiencia 0/1/9 | Pendiente de integración oficial |
| 4. Giro reversible y autoevaluación única | S/V/A: frente/dorso, tokens, controles y giro interrumpido | Pendiente de integración y API 24/25 |
| 5. Resumen 10/7/3 y repaso exacto de tres | S/A y prueba offline nueva | Pendiente de integración oficial |
| 6. Repaso 3/2/1 y luego una, sin repaso vacío | S/A y prueba offline nueva, mismas tarjetas/fotos | Pendiente de integración oficial |
| 7. Conservación en rotación/fondo y descarte al salir/cerrar | A/V para estado completo; host independiente con desaparición/cambio de PID y tarea retenida | Aprobado en copia API 36; integración oficial pendiente |
| 8. Modalidades/repasos offline y sin permisos ni historial | Prueba definitiva, red efectiva ausente, permisos denegados y revisión de almacenamiento | Aprobado en copia API 36; integración oficial pendiente |
| 9. Errores, temas, legibilidad y operación accesible | C/S/A, capturas y TalkBack; falta API 24/25 | Pendiente de matriz completa e integración oficial |
