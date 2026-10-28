# Tareas — Spec 004: Escucha continua con lista acumulativa

Estado: lista aprobada explícitamente por el usuario el 27 de octubre de 2026; autorizadas y completadas T1, T2, T3, T4, T5, T6 y T7; T8 y T9 pendientes. Fecha: 27 de octubre de 2026.

## Referencias

- [Spec aprobada](spec.md): RF-1 a RF-23 y nueve criterios de finalización.
- [Plan aprobado](plan.md): contratos, ventanas, selección, ciclo de vida y presupuestos de validación.
- [AGENTS.md](../../AGENTS.md): aprobar tareas antes de implementar; una tarea por turno, con pruebas y evidencia; confirmar antes de la siguiente; sin commits ni push.

## Reglas de ejecución

- Las tareas están pendientes. La aprobación del plan autoriza preparar esta lista, no ejecutar una lista que todavía no se presentó para revisión.
- Después de aprobar estas tareas, implementar únicamente T1 en el siguiente turno. No iniciar otra en ese mismo turno, aunque sus dependencias estén satisfechas.
- Antes de editar cada área, revisar sus callers y pruebas. Conservar el árbol de trabajo actual, incluidos todos los cambios de la spec 003 y los ajenos al sonido; no restaurar, limpiar ni rehacer esos cambios para obtener una línea base distinta.
- Añadir primero las pruebas de lógica no trivial, comprobar un fallo relevante y después implementar lo mínimo para hacerlas pasar. No crear tests artificiales para demostrar solo que falta un archivo. Cada tarea mantiene el proyecto compilable y sus consumidores existentes coherentes.
- Se permiten adaptaciones de transición exclusivamente para mantener los callers actuales hasta el cambio de flujo indicado en T6. No se añadirá un modo de diez segundos seleccionable ni se conservarán rutas de producto sin uso al finalizar la integración.
- Registrar comandos, dispositivo, recuentos, errores y verificaciones omitidas. Una tarea incompleta se mantiene pendiente; una limitación de entorno no cuenta como aprobación de esa comprobación. Si una dependencia no está verificada, no comenzar la siguiente sin resolverla o acordar explícitamente un cambio de orden/alcance.
- Marcar una tarea terminada solo con su condición de cierre demostrada; actualizar evidencia y estado, mostrar el resultado y esperar confirmación. No hay autorización para ejecutar varias tareas automáticamente ni para hacer commits o push.
- Los valores del plan son vinculantes. Un fallo no permite cambiar silenciosamente umbral, frecuencia, modelo, runtime, compatibilidad o presupuestos. Presentar la evidencia y revisar el plan antes de hacerlo.
- Rutas relativas a la raíz del repositorio. Reportes y capturas generados bajo `app/build/birdnet-verification/spec004/`, ignorados por Git. Resumen durable en `specs/004-escucha-continua-aves/evaluacion-continua.md`, sin PCM ni resultados de sesiones privadas en logs de producción.

## Tareas

- [x] **T1 — Preparar ventanas recientes con señal correcta y memoria acotada.**
  - Áreas afectadas: `app/src/main/java/com/example/avesargentinas/ui/sound/AudioWindowBuffer.kt` y `BirdSoundAudio.kt`; `app/src/test/java/com/example/avesargentinas/ui/sound/AudioWindowBufferTest.kt` y `BirdSoundAudioTest.kt`; documento `evaluacion-continua.md` de esta spec.
  - Cubre: base de RF-7, RF-20 y RF-23; calidad de señal y memoria acotada.
  - Depende de: ninguna.
  - Entrega: línea base de compilación, unitarios y Lint con la configuración actual, sin modificar dependencias. Anillo puro de `3 * rate` PCM16, primera ventana tras tres segundos y siguientes cada segundo, con posiciones Long y copias que no cambian al capturar más. Repartir correctamente bloques de lectura que atraviesan varios límites.
  - Entrega: `toModelSamples()` admite únicamente las duraciones de 3/10 s acordadas y devuelve 96.000/320.000 Float, conservando extremos PCM, frecuencias 32.000/48.000/44.100 Hz y antialias. Reutilizar coeficientes sin almacenar audio entre sesiones. El soporte interno de diez segundos mantiene los consumidores de evaluación existentes y no añade un modo de UI.
  - Verificación: unitarios para bloques desiguales, varios saltos en una lectura, longitudes exactas, límites y última muestra, independencia de copias y entradas inválidas. Simular al menos una hora con capacidad constante del anillo y sin conservar todo el flujo. En 48/44,1 kHz exigir tonos de 1/10 kHz dentro de 1 dB y atenuación de 18 kHz ≥ 40 dB. Ejecutar los unitarios afectados y compilación; no relajar referencias anteriores para aceptar un cambio de señal sin explicación.
  - Condición de cierre: ventanas y remuestreo correctos con pruebas reproducibles y línea base registrada. No se afirma captura física ni reconocimiento continuo todavía.

- [x] **T2 — Exponer inferencia de una ventana y todas las especies elegibles.**
  - Áreas afectadas: `app/src/main/java/com/example/avesargentinas/ui/sound/BirdNetClassifier.kt` y `BirdSoundCatalog.kt`; pruebas existentes de clasificador, catálogo e integridad en `test`/`androidTest` del paquete `ui/sound`.
  - Cubre: RF-7, RF-8, RF-9, RF-11, RF-12 y RF-21; soporte de cancelación de RF-14/RF-22.
  - Depende de: T1.
  - Entrega: `classifyWindow()` para 96.000 Float y ejecución nativa compartida con `classify()` de 320.000. Conservar modelo CPU actual, huella, 9.789 salidas, runtime, dos hilos, Mutex y cierre de recursos; sin lock recursivo, otro motor, sigmoid o softmax añadidos. Ventanas constantes no entran al modelo; salidas inválidas son error.
  - Entrega: `selectAll(scores, minimumScore)` conserva validaciones, agrupación canónica, filtro Aves, nombres y URI, sin recorte a cinco. `select()` delega y conserva su contrato limitado para los consumidores anteriores. Las puntuaciones no se suman y la lista de elegibles tiene orden determinista.
  - Verificación: JVM para seis o más especies, alias, clases no aviares, nombres/mapas faltantes, puntuación igual al umbral, NaN/infinito/longitudes incompatibles y conservación del top cinco anterior. Instrumentadas reales en Samsung A56, sin conexión: tres ventanas de referencia con error máximo por clase ≤ 0,001; paridad de ambas rutas equivalentes, constantes sin NaN, errores y cancelación durante run sin uso tras cierre. Ejecutar también las referencias existentes de diez segundos y controles de integridad.
  - Condición de cierre: inferencia de una ventana y selección sin límite demostradas, sin regresión del contrato anterior ni modificación de assets o dependencias. Las pruebas de UI o los dobles no sustituyen esa ejecución nativa.

- [x] **T3 — Mantener el micrófono abierto y emitir ventanas sin límite de diez segundos.**
  - Áreas afectadas: `app/src/main/java/com/example/avesargentinas/ui/sound/AudioRecorder.kt`; `app/src/androidTest/java/com/example/avesargentinas/ui/sound/AudioRecorderTest.kt`; pruebas puras del anillo si aparecen límites nuevos de lectura.
  - Cubre: mecanismo de RF-4, RF-6, RF-7, RF-14, RF-19, RF-20 y RF-23.
  - Depende de: T1 y T2.
  - Entrega: `AudioRecorder.listen(onStarted, onWindow)` con inicio efectivo, frecuencia, ventanas de tres segundos, posición final Long y tiempo monotónico. Conservar inicialización y fuentes/frecuencias alternativas, chequeo de permiso/silenciamiento, lectura no bloqueante y watchdog de dos segundos sin muestras. La ruta continua no tiene el tope de quince segundos ni cierra el micrófono entre ventanas; onWindow solo ofrece datos y nunca reconoce.
  - Entrega: stop/release en finally, cancelación segura y un único propietario. Mantener compilables los callers del recorrido actual mientras T6 los sustituye, compartiendo inicialización/lectura donde corresponda en lugar de duplicar una implementación completa. Eliminar esa transición en T6 cuando deje de tener consumidores de producto.
  - Verificación: entradas controladas para fuente/frecuencia fallida, cero lecturas, watchdog, códigos de error, revocación y captura silenciada donde sea observable. En Samsung A56, una escucha de 60 s entrega ventanas sin reiniciar AudioRecord ni finalizar a los 10/15 s; cantidad y posiciones según las muestras efectivas. Comprobar inicialización/inicio únicos y liberación ≤ 1 s al cancelar, sin archivo de grabación ni silencio inventado para rellenar faltantes.
  - Condición de cierre: captura continua real comprobada, con errores/cancelación y memoria acotada. Todavía no se presenta el flujo continuo como disponible al usuario desde la pantalla.

- [x] **T4 — Coordinar captura e inferencia con una sola ventana pendiente.**
  - Áreas afectadas: `SoundSession` y `RealSoundSession` en `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundViewModel.kt`; `app/src/androidTest/java/com/example/avesargentinas/ui/sound/ContinuousSoundSessionTest.kt`; dobles existentes afectados por el contrato interno.
  - Cubre: RF-6, RF-7, RF-14, RF-20, RF-21, RF-22 y RF-23, en la coordinación.
  - Depende de: T2 y T3.
  - Entrega: operación suspendida listen que coordina dos hijas estructuradas y Channel(CONFLATED): una ventana en ejecución y como máximo una pendiente, siempre la más reciente. La captura no espera a la inferencia. Remuestreo e inferencia fuera de Main; callbacks de inicio/detecciones suspendibles y secuenciales, sin una corrutina fire-and-forget por ventana.
  - Entrega: descartar pendiente con más de tres segundos de antigüedad; watchdog de inferencia de cinco segundos que solicita terminación nativa y termina con error. Cancelar un hijo cancela al otro; Detener cancela la pendiente sin drenarla. Conservar recursos hasta terminar run y cerrar fuera de Main. No sustituir el timeout por una cancelación de corrutina que deje ejecutándose el código nativo indefinidamente.
  - Verificación: con operaciones controladas, bloquear inferencia y seguir produciendo ventanas; al desbloquear, recibir la última pendiente, sin cola creciente ni inferencias paralelas. Probar edad, timeout, fallo de ambos hijos, preparación tardía y cierre ordenado. Comprobar con captura/inferencia reales que el micrófono sigue activo mientras se reconocen ventanas y libera ≤ 1 s al cancelar. Los dobles solo demuestran carreras, no calidad acústica.
  - Condición de cierre: coordinación acotada y cancelación demostradas antes de cambiar el estado/UI. Durante la transición los consumidores actuales compilan; no se introduce un framework de eventos ni un segundo motor.

- [x] **T5 — Preparar una lista incremental que conserve orden, posición y foco.**
  - Áreas afectadas: `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundResultAdapter.kt` y renderizado de resultados en `BirdSoundActivity.kt`; `app/src/main/res/layout/activity_bird_sound.xml` e `item_bird_sound_result.xml`; pruebas de presentación en `app/src/androidTest/java/com/example/avesargentinas/ui/sound/BirdSoundActivityTest.kt`.
  - Cubre: RF-8, RF-9, RF-10, RF-11, RF-12, RF-16 y RF-18 en presentación; accesibilidad de actualizaciones.
  - Depende de: T2; se ejecuta después de T4 por la secuencia acordada, sin afirmar que dependa de captura para probar la lista.
  - Entrega: RecyclerView como único desplazamiento vertical; ListAdapter/DiffUtil con identidad científica canónica y actualización puntual. Reutilizar fila actual, cabecera/pie desplazables mediante ConcatAdapter ya instalado, sin biblioteca nueva. Mantener acción y estado fuera del scroll; no reconstruir todas las filas ni desplazar al final con cada alta.
  - Entrega: conservar foco y primer elemento visible; sin parpadeo al mejorar puntuación, sin reenviar lista al avanzar reloj. Restaurar posición tras rotación cuando haya datos. El recorrido de diez segundos seguirá siendo operativo solo hasta su sustitución en T6; no etiquetar todavía la pantalla como escucha continua ni simular actividad del micrófono.
  - Verificación: casos controlados de cero, una, más de cinco y al menos 200 especies; altas al final, mejoras y listas idénticas. Identidad/posición y foco se conservan; filas sin nombre común/mapa y nombres largos siguen legibles. Matriz preliminar horizontal/vertical y fuente al 200 %: la acción permanece alcanzable, sin scroll anidado. Adaptar aserciones antiguas que suponían LinearLayout, sin eliminar su intención de prueba.
  - Condición de cierre: presentación incremental comprobada y pantalla compilable/operativa con el recorrido aún vigente. Los casos controlados de 200 especies no cuentan como resultados reales del modelo.

- [x] **T6 — Conectar escucha continua, acumulación y estados de sesión a la pantalla.**
  - Áreas afectadas: `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundViewModel.kt` y `BirdSoundActivity.kt`; `app/src/main/res/layout/activity_bird_sound.xml`; `app/src/main/res/values/strings.xml`; `BirdSoundViewModelTest.kt` y `BirdSoundActivityTest.kt` en `androidTest`.
  - Cubre: RF-1 a RF-15, RF-18, RF-20, RF-21, RF-22 y RF-23; integración base de RF-19.
  - Depende de: T4 y T5.
  - Entrega: fases listo/preparando/escuchando/deteniendo/detenido/error separadas de lista, reloj y motivo. LinkedHashMap por identidad canónica: insertar al final, mejorar solo el máximo y no borrar/reordenar ante ventanas vacías. Un inicio aceptado con permiso limpia antes de preparar; una mera denegación no limpia una lista detenida. Un fallo del nuevo inicio no recupera resultados anteriores.
  - Entrega: un token por sesión; Detener congela inmediatamente las especies y puntuaciones publicadas antes de cancelar. Deteniendo mantiene la lista y bloquea un inicio hasta completar limpieza; Detenido permite Escuchar con lista nueva. Distinguir Esperando coincidencias durante escucha de una sesión detenida vacía o un error con lista conservada. Eliminar el uso de record/analyze fijos de producto y sus adaptaciones temporales; conservar los contratos internos de diez segundos que aún sirven al corpus.
  - Entrega: Escuchar/Detener, tiempo monotónico mm:ss con soporte de horas y actualización una vez por segundo, textos de sesión y pulso del micrófono solo tras inicio real. Cancelar/restablecer el animador al detener, error o pérdida de visibilidad; respetar animaciones desactivadas con las guardas API existentes. No representar una amplitud de señal ficticia.
  - Verificación: pruebas de acumulación con seis o más aves, duplicados/alias y puntuaciones mayores/menores/iguales; reloj no reconstruye lista. Doble inicio/stop, detener antes de tres segundos, durante run y durante preparación, limpiar al reintentar, respuestas de sesiones anteriores y preparación que termina tarde. Verificar Main no bloqueado y congelación exacta al aceptar Detener, con buffers/recursos fuera del estado persistible.
  - Verificación física: Escuchar → detecciones durante captura → Detener → misma lista → Escuchar → lista vacía, más de diez segundos, offline. Solo un micrófono/animador/reloj; pulso y texto nunca dicen Escuchando durante preparación o detención. Ejecutar las pruebas de consumidores modificados y compilación/Lint apropiados.
  - Condición de cierre: único recorrido continuo de producto conectado a inferencias reales, sin predicciones simuladas, modos extra ni rutas temporales sin consumidores. El cierre completo de navegación/lifecycle y accesibilidad se verifica en T7.

- [x] **T7 — Completar detención al navegar, ciclo de vida y accesibilidad.**
  - Áreas afectadas: `BirdSoundActivity.kt`, `BirdSoundViewModel.kt`, adaptador/layout/textos de sonido donde sea necesario; pruebas instrumentadas de Activity/ViewModel y sus regresiones existentes.
  - Cubre: RF-1, RF-2, RF-5, RF-6, RF-14 a RF-23; compatibilidad de navegación y accesibilidad.
  - Depende de: T6.
  - Entrega: abrir ficha activa congela la lista y espera liberación antes del Intent; destino único, token vigente, Activity activa y doble toque protegido. Si falla limpiar, conservar error/lista y no navegar dejando micrófono activo. Regresar de ficha/zoom deja Detenido, sin autograbación; onStop de esa navegación es idempotente. No modificar BirdInfoActivity, Room o sus contratos.
  - Entrega: rotación conserva sesión/lista/orden/reloj sin duplicar recursos. Home/bloqueo detienen conservando resultados y marcan interrupción; salida real descarta también intención/preparación pendientes. Atrás o cabecera abandonan; muerte del proceso no restaura sesión. Conservar permiso diferido, denegaciones y vuelta de Ajustes sin iniciar automáticamente.
  - Verificación: rotación en cada fase y permiso abierto, callback tardío, Home/bloqueo, consulta activa/detenida con zoom, doble toque, error de mapa, salida durante run y muerte real del proceso desde el host. Lista congelada sin alteraciones tardías, nueva escucha limpia y micrófono liberado ≤ 1 s. No usar recreación de Activity como sustituto de muerte real.
  - Verificación visual/manual: temas claro/oscuro, vertical/horizontal, pantalla pequeña, texto al 200 %, animaciones habilitadas/deshabilitadas y objetivos de 48 dp. Mientras llegan especies, TalkBack anuncia un lote nuevo sin repetir lista/reloj/mejoras; recorrido y escucha manuales reales, sin robo de foco ni auto-scroll. Confirmar explicación de puntuación/atribución offline y que Detener siempre es alcanzable.
  - Condición de cierre: sesión y navegación seguras con comprobaciones físicas y manuales documentadas. Las verificaciones sensoriales pendientes se mantienen pendientes; no se dan por realizadas mediante capturas o nodos automatizados.

- [ ] **T8 — Validar confianza y falsos positivos de la acumulación continua.**
  - Áreas afectadas: prueba instrumentada de evaluación continua del paquete `ui/sound`; fixtures y manifiestos autorizados bajo `app/src/androidTest/assets/birdnet/`; umbral de la ruta continua; `specs/004-escucha-continua-aves/evaluacion-continua.md`.
  - Cubre: calidad real de RF-7 a RF-13 y RF-21/RF-23; criterio de finalización 7.
  - Depende de: T6 y T7.
  - Entrega: reutilizar los 42 clips y particiones de la spec 003 sin modificarlos, con sesiones independientes por clip y ventanas de 3 s cada 1 s. No recortar a cinco para medir la nueva lista. Registrar el hash de los datos, procedencia/licencia y configuración. Ese corpus ya fue utilizado: es regresión, no un nuevo ensayo ciego.
  - Verificación de calibración: solo diez positivos y cuatro controles reales de su partición, entre 0,35/0,50/0,65/0,80. Elegir el menor con ≥ 8/10 especies objetivo detectadas y cero aves en los cuatro controles; congelarlo antes de evaluar. Si ninguno cumple, registrar y revisar el plan, sin introducir otra regla de confirmación temporal silenciosamente.
  - Verificación restante: ≥ 16/20 positivos con especie objetivo en la lista, como máximo un control real de cuatro con aves y cero aves en cuatro silencios. Registrar todas las especies falsas, tiempo hasta la primera detección objetivo y tamaño final; no ocultar candidatos adicionales de la selección sin límite.
  - Verificación prolongada: tres controles nuevos independientes, de cinco minutos cada uno, voz consentida/viento/ambiente no aviar, con ausencia de aves verificada humanamente y procedencia/licencia/hash. Reservarlos hasta fijar el umbral: máximo una especie aviar falsa acumulada por sesión. No reemplazar un caso difícil después de ver resultados ni recalibrar sobre esos mismos controles. Una nueva estrategia requiere revisar el plan y nuevos controles reservados.
  - Verificación de integración: audio autorizado con especies que aparecen en momentos distintos y se repiten, demostrando orden, deduplicación y máximo con el modelo real. Declarar montajes de clips como pruebas controladas, no como grabaciones independientes de campo. Repetir selección/UI afectadas si cambia el umbral final dentro de los candidatos aprobados.
  - Condición de cierre: criterios cumplidos con datos trazables y límites de la evaluación explícitos; fixtures de prueba fuera del APK de producción. Datos faltantes, anotación no realizada o un criterio fallido mantienen T8 pendiente.

- [ ] **T9 — Verificar escucha prolongada, compatibilidad y regresiones finales.**
  - Áreas afectadas: pruebas instrumentadas de benchmark continuo y suites afectadas del paquete `ui/sound`; `evaluacion-continua.md` y este registro. Correcciones de código solo dentro del alcance, respaldadas por una prueba y repetición de los controles afectados.
  - Cubre: RF-1 a RF-23, requisitos no funcionales y los nueve criterios de finalización.
  - Depende de: T1 a T8 completadas con su evidencia obligatoria.
  - Verificación física: 15 minutos continuos en Samsung A56 sin reinicio del micrófono, modo avión, sin carga ni depurador, brillo 50 % y ambiente de referencia 20–27 °C. Registrar modelo/Android/RAM/página y restaurar los ajustes temporales del teléfono al terminar. Los valores siguientes son criterios, no resultados a copiar como mediciones.
  - Verificación de latencia: preparación fría ≤ 5 s; primera actualización analítica ≤ 5 s desde el primer audio. Al menos 95 % de ventanas procesadas dentro de dos segundos desde su fin de captura; registrar ventanas omitidas y edad de pendiente incluso sin nuevas aves. Comparar los primeros y últimos cinco minutos: sin deterioro creciente. Exigir cola acotada y reportar el ritmo real frente al salto objetivo de un segundo.
  - Verificación de recursos: PSS total ≤ 512 MiB, también fotografía → sonido; con cantidad fija de especies, diferencia de medianas de minutos 4–5 a 14–15 ≤ 20 MiB y sin crecimiento de buffers/tareas pendientes. Muestreo e inspección nativa; CPU media ≤ 100 % de un núcleo lógico equivalente según tiempo CPU/tiempo de pared. Consumo ≤ cinco puntos de batería, incremento de temperatura de batería ≤ 5 °C y sin estado térmico SEVERE o superior, con medidas al inicio y cada minuto. No sustituir batería/temperatura físicas por estimaciones del tamaño del modelo.
  - Verificación de detención: congelación inmediata de lista, release del micrófono ≤ 1 s y cierre nativo ≤ 5 s, sin bloquear Main. Cinco ciclos de inicio/escucha/detención sin crecimiento retenido sostenido ni autograbación. Si una medida falla, registrar y revisar el diseño/presupuesto, sin bajar silenciosamente la frecuencia o retirar compatibilidad.
  - Verificación final: instalación completa → modo avión → primera escucha → ficha → nueva escucha; sin audio persistente, transferencias, historial nuevo o alteración de observaciones. Android mínimo, ABI y assets preservados, comprobar compatibilidad de las rutas modificadas en API 24/25 además del Samsung. Revisar fotografía, menú/captura instantánea, fichas, historial/exportación, aprendizaje y preferencias; repetir matriz visual y TalkBack si hubo correcciones posteriores a T7.
  - Verificación automatizada: ejecutar por separado compilación, unitarios, Lint e instrumentadas completas de los comandos siguientes, registrando recuentos y dispositivo. No presentar una ejecución filtrada o UP-TO-DATE como nueva suite completa. Separar fallos heredados de regresiones; no silenciarlos ni arreglar otras features sin acordar el alcance.
  - Condición de cierre: nueve criterios y pruebas obligatorias demostrados en el estado final. Falta de teléfono, controles manuales, API de compatibilidad o fallo obligatorio deja la integración pendiente; no se declara completa por tener casillas marcadas o reportes de la spec anterior.

## Trazabilidad

| Requisito | Implementación y comprobación principal |
| --- | --- |
| RF-1 | T6 y T7; regresión de acceso T9. |
| RF-2 | T6 y T7. |
| RF-3 | T6; reintento y permiso T7. |
| RF-4 | T3 y T6; duración final T9. |
| RF-5 | T6 y T7. |
| RF-6 | T3/T4 para exclusión y coordinación; T6/T7 para acciones y estado. |
| RF-7 | T1/T2/T3/T4 y conexión T6; calidad/latencia T8/T9. |
| RF-8 | T2 y T5/T6; evaluación real T8. |
| RF-9 | T2 y T5/T6; audio repetido T8. |
| RF-10 | T5/T6; silencio e intervalos sin detecciones T8. |
| RF-11 | T2 y T5/T6. |
| RF-12 | T2 y T5/T6; accesibilidad T7. |
| RF-13 | T6 y T8. |
| RF-14 | T3/T4 y T6/T7; medición final T9. |
| RF-15 | T6 y T7. |
| RF-16 | T5/T7. |
| RF-17 | T7. |
| RF-18 | T5/T6/T7. |
| RF-19 | T3/T6/T7. |
| RF-20 | T1/T3/T4 y T6/T7. |
| RF-21 | T2/T4 y T6/T7; evaluación T8. |
| RF-22 | T2/T4 y T6/T7. |
| RF-23 | T1/T3/T4 y T6/T7; privacidad final T9. |
| Criterio de finalización 1 | T6/T7 y ensayo final T9. |
| Criterio de finalización 2 | T2/T5/T6 y audio real T8. |
| Criterio de finalización 3 | T6/T7. |
| Criterio de finalización 4 | T3/T4/T6/T7. |
| Criterio de finalización 5 | T7. |
| Criterio de finalización 6 | T7 y verificación final T9. |
| Criterio de finalización 7 | T8 y T9. |
| Criterio de finalización 8 | T5/T7 y revisión final T9. |
| Criterio de finalización 9 | Pruebas de cada tarea y suite/regresiones T9. |

## Comandos y evidencia

Desde la raíz, con JDK/SDK configurados y Gradle Wrapper. Son comandos de ejecución futura: no se corrieron al redactar esta lista.

| Objetivo | PowerShell |
| --- | --- |
| Compilación | `.\gradlew.bat :app:assembleDebug` |
| Unitarios completos | `.\gradlew.bat :app:testDebugUnitTest` |
| Unitarios de una clase | `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.avesargentinas.ui.sound.AudioWindowBufferTest"` |
| Lint | `.\gradlew.bat :app:lintDebug` |
| Instrumentadas completas | `.\gradlew.bat :app:connectedDebugAndroidTest` |
| Instrumentadas de una clase | `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.ContinuousSoundSessionTest` |

Seleccionar explícitamente dispositivo cuando haya varios. Sustituir los nombres de ejemplo por la clase realmente ejecutada y registrar qué cubrió; no sumar resultados filtrados como si fueran una suite completa. Conservar los registros de la spec 003 como históricos, sin reescribirlos para reflejar este flujo nuevo.

En `evaluacion-continua.md` registrar por tarea fecha, cambios, comandos, recuentos, dispositivo y referencias a reportes ignorados. El manifiesto de nuevos controles autorizados contiene procedencia/atribución, no audio de usuarios capturado por la app. No versionar SDK local, logs, capturas o salidas de profiler.

## Verificación final

- [ ] T1 a T9 completadas, cada una con evidencia y confirmación entre turnos.
- [ ] Ejecutar la suite del plan en el estado final y comprobar RF-1 a RF-23 y los nueve criterios de finalización.
- [ ] Confirmar lista acumulada sin límite de cinco, puntuación máxima, orden estable y limpieza al iniciar otra sesión.
- [ ] Confirmar micrófono/animación, detención sin resultados tardíos, consulta de ficha detenida y ausencia de autograbación.
- [ ] Confirmar calidad y controles prolongados reservados, latencia, memoria, CPU, batería y temperatura con mediciones reales.
- [ ] Registrar accesibilidad, compatibilidad y regresiones realizadas; mantener pendientes las que no se ejecutaron.
- [ ] Confirmar ausencia de funciones excluidas, cambios de modelo/runtime/datos y rutas temporales sin uso; sin commits ni push.

## Estado de ejecución

Lista aprobada y ejecución de T1 autorizada por el usuario el 27 de octubre de 2026.
T1 completada y verificada (línea base registrada, ventanas y remuestreo implementados con 18 pruebas unitarias afectadas aprobadas, suite de 55 unitarios limpia, compilación exitosa y Lint sin nuevas advertencias).
Ejecución de T2 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T2 completada y verificada (implementados classifyWindow y selectAll compartiendo lógica nativa/catálogo; 59 unitarios limpios; 16 pruebas instrumentadas de BirdNetClassifierTest y 2 de BirdSoundCatalogTest aprobadas al 100% en Samsung Galaxy A56 físico SM-A566E sin conexión; compilación exitosa y Lint sin errores ni advertencias nuevas).
Ejecución de T3 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T3 completada y verificada (implementada captura continua `listen(onStarted, onWindow)` en `AudioRecorder.kt` con inicialización/lectura/errores compartidos, sin límite de 10/15 s, sin almacenamiento de audio ni relleno ficticio de silencio, y manteniendo compilables y operativos los consumidores de transición; 30 pruebas instrumentadas de `AudioRecorderTest` aprobadas al 100% en Samsung Galaxy A56 físico SM-A566E sin conexión, incluyendo escucha física continua de 60 s sin reinicio de micrófono ni detención prematura, watchdog de 2 s sin muestras, revocación de permisos, errores hardware, silenciamiento, cancelación explícita y liberación en <= 1000 ms; 59 unitarios limpios, compilación exitosa y Lint sin errores ni advertencias nuevas).
Ejecución de T4 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T4 completada y verificada (coordinación estructurada en `RealSoundSession.listen` mediante `Channel.CONFLATED` con una ventana en ejecución y como máximo una pendiente; descarte de ventanas con antigüedad > 3 s; watchdog de inferencia de 5 s con interrupción nativa en ONNX Runtime y terminación ordenada; cancelación bidireccional inmediata y liberación de hardware <= 1 s; 8 pruebas instrumentadas de `ContinuousSoundSessionTest` aprobadas al 100% en Samsung Galaxy A56 físico SM-A566E sin conexión; 59 unitarios limpios, compilación exitosa y Lint sin errores ni advertencias nuevas).
Ejecución de T5 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T5 completada y verificada (implementada lista incremental mediante ConcatAdapter(headerAdapter, resultAdapter, footerAdapter) con ListAdapter y DiffUtil por identidad científica canónica, sin límite artificial de 5 elementos ni scroll anidado; actualización granular de puntuaciones mediante payloads sin reconstruir filas ni robar foco; casos de 0, 1, >5 y 200 especies controladas probados; nombres largos con elipsis y textos sin recursos locales con touch target de 48 dp; posición de scroll y primer elemento visible conservados; flujo vigente de 10 s plenamente operativo sin simular escucha continua; 62 pruebas unitarias JVM aprobadas al 100% en 29 s; 25 pruebas instrumentadas de BirdSoundActivityTest aprobadas al 100% en Samsung Galaxy A56 SM-A566E / Android 16 en 36.9 s; compilación exitosa y Lint sin errores ni advertencias nuevas).
Ejecución de T6 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T6 completada y verificada (conectado flujo continuo único en `BirdSoundViewModel.kt` y `BirdSoundActivity.kt` con fases explícitas READY/PREPARING/LISTENING/STOPPING/STOPPED/ERROR; acumulación sin límite en LinkedHashMap por clave canónica con actualización exclusiva ante mayor puntuación y preservación ante ventanas vacías; reloj monotónico 1 Hz y animación de pulso condicional a escucha y animaciones del sistema; 62 unitarios JVM limpios; 14 pruebas de BirdSoundViewModelTest y 25 de BirdSoundActivityTest aprobadas al 100% en Samsung Galaxy A56 físico SM-A566E; compilación exitosa y Lint sin errores).
Ejecución de T7 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T7 completada y verificada (implementada navegación segura con congelación de resultados y espera de liberación del hardware mediante `stopAndAwait()` antes de iniciar Intent de ficha; retorno desde ficha/zoom mantiene la lista en estado detenido sin autograbación; rotación conserva sesión activa o resultados congelados sin duplicar micrófono ni solicitudes; Home y bloqueo detienen conservando detecciones y marcan interrupción `ERROR(INTERRUPTED)`, descartando `pendingRecordIntent`; abandono y muerte real del proceso desde el host mediante `am force-stop` descartan la sesión iniciando limpios en `READY`; debounce de 500 ms contra doble toque; anuncio accesible de lotes nuevos en TalkBack vía `accessibilityAnnouncer` sin repetir lista ni anunciar mejoras de puntuación, reloj o rotación; 62 pruebas unitarias JVM limpias; 18 pruebas instrumentadas de BirdSoundViewModelTest y 30 pruebas instrumentadas de BirdSoundActivityTest aprobadas al 100% en Samsung Galaxy A56 físico SM-A566E sin conexión; compilación exitosa con APK generado y Lint con 0 errores). Comprobaciones sensoriales humanas (percepción auditiva directa con TalkBack de la voz del lector en audio en vivo y revisión visual humana en pantalla pequeña con fuente 200% y ambos temas) registradas explícitamente como pendientes de evaluación humana.
Ejecución de T8 autorizada por el usuario el 27 de octubre de 2026 tras validar dependencias con evidencia.
T8 evaluada en hardware físico Samsung Galaxy A56 (SM-A566E) con umbral fijado en T = 0.35:
- Sweep de calibración en partición de calibración (14 clips) cumplido formalmente fijando y congelando el menor umbral válido: T = 0.35 (positivos 9/10, controles reales 0/4).
- Partición de evaluación reservada (28 clips históricos) cumplida: positivos detectados 16/20 (req >= 16), controles reales 0/4 (req <= 1) y silencios 0/4 (req == 0).
- Integración multiespecie (montaje de 15 s) cumplida: orden de aparición, deduplicación y preservación/actualización de score máximo probados y aprobados en hardware real (9.199 s).
- Controles prolongados independientes de 5 minutos (300 s / 298 ventanas nativas de inferencia): viento cumplido con 1 falso positivo (req <= 1), ambiente de lluvia cumplido con 0 falsos positivos (req <= 1); control de voz humana consentida fallido con 2 falsos positivos (Mellisuga helenae con score 0.481 y Buteo platypterus con score 0.397) frente al tope de <= 1.
- Consecuencia formal: según la condición de cierre de la tarea ("un criterio fallido mantiene T8 pendiente; no reemplazar un caso difícil después de ver resultados ni recalibrar sobre esos mismos controles"), T8 se mantiene PENDIENTE de decisión técnica del usuario sobre la estrategia a seguir, sin recalibrar a posteriori sobre los controles reservados ni relajar reglas sin revisión del plan.
62 unitarios JVM limpios, compilación exitosa con APK generado y Lint con 0 errores. T9 pendiente de autorización. Sin commits ni push.
