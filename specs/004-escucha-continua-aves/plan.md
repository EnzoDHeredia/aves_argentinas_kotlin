# Plan técnico — Spec 004: Escucha continua con lista acumulativa

Estado: aprobado explícitamente por el usuario el 27 de octubre de 2026. Fecha: 27 de octubre de 2026.

## Referencias y alcance

- [Spec 004 aprobada](spec.md): RF-1 a RF-23 y nueve criterios de finalización.
- [AGENTS.md](../../AGENTS.md): Android Views/XML, recursos existentes, aprobación separada de artefactos y una tarea por turno, sin commits ni push.
- [Implementación y evidencia de la spec 003](../003-identificacion-aves-sonido/evaluacion-audio.md): base que debe preservarse, no una validación de escucha continua.
- [frontend-design](../../.agents/skills/frontend-design/SKILL.md): jerarquía de estado, controles accesibles y lista legible con la identidad actual.

Se sustituirá el recorrido fijo por escucha continua en la misma sección. El plan no incluye tareas de ejecución ni autoriza código. El Samsung Galaxy A56 SM-A566E aportado por el usuario es la referencia física; se registrará su configuración real al validar, sin deducirla de pruebas históricas.

## Estado actual

- `AudioRecorder.record()` inicializa AudioRecord, captura `rate * 10` muestras y devuelve un único CapturedAudio. Tiene alternativas 32/48/44,1 kHz, comprobación de permiso y silenciamiento, watchdog de dos segundos sin muestras y otro de quince segundos para completar la captura. El último no es aplicable a una sesión continua.
- `BirdSoundAudio.toModelSamples()` exige diez segundos y produce 320.000 Float; su remuestreo antialias ya tiene pruebas. `BirdNetClassifier.classify()` exige esa longitud y analiza cuatro ventanas de 96.000 muestras bajo un Mutex.
- La implementación utiliza el modelo CPU derivado ya aprobado, `BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx`, de 65.830.813 bytes y SHA-256 `cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`. El runtime actual es ONNX Android 1.22.0; la ruta fotográfica usa LiteRT 1.4.1. No se regresará al binario originalmente propuesto antes de las revisiones de la spec 003.
- `BirdSoundCatalog.select()` valida las puntuaciones, agrupa identidades canónicas, filtra aves y devuelve hasta cinco. BirdDetection ya contiene identidad canónica, nombres, puntuación y URI de ficha; no hace falta otra entidad de especie.
- `SoundSession` y `RealSoundSession`, en `BirdSoundViewModel.kt`, coordinan preparar → grabar → analizar. El ViewModel dispone de tokens y limpieza para evitar respuestas antiguas, pero todavía no conserva detecciones mientras graba o detiene.
- `BirdSoundActivity` observa StateFlow y conserva el permiso diferido, los extras de ficha y el control de segundo plano. Su renderizado reconstruye filas con `removeAllViews()` y vacía resultados en varios estados; eso no permite conservar posición o foco durante actualizaciones continuas.
- El layout tiene NestedScrollView y hasta cinco filas infladas. RecyclerView ya es una dependencia del proyecto; no habrá nuevas bibliotecas, permisos, servicios ni cambios de Gradle, modelo, catálogo empaquetado o Room previstos.

## Enfoque técnico

### Captura continua y trabajo acotado — RF-4, RF-6, RF-7, RF-20 y RF-23

Mantener un único AudioRecord activo mientras la sesión escucha. Reutilizar su inicialización, fuentes, frecuencias alternativas y manejo de errores. La captura se ejecutará en IO con lecturas pequeñas no bloqueantes y cesión ante lecturas cero. Conservar el watchdog de dos segundos sin muestras, las comprobaciones de permiso/silenciamiento y stop/release en finally; eliminar el tope global de quince segundos y el objetivo de diez segundos de la ruta de producto.

Se usará una ventana de **3 segundos**, con **salto de 1 segundo**. La primera se entregará al recibir `rate * 3` muestras; las siguientes al recibir cada `rate` muestras adicionales. Duración y saltos se calculan por muestras efectivas, no por un temporizador que invente audio. Una lectura que atraviese uno o más límites se dividirá correctamente para que las ventanas correspondan a su extremo exacto.

`AudioWindowBuffer.kt` será una pieza pequeña y pura para el anillo de `3 * rate` PCM16 y el conteo Long de muestras, sin referencias a Android. Se conservarán las frecuencias admitidas de 32.000, 48.000 y 44.100 Hz. Cuando exista una ventana completa se emitirá una copia inmutable del tramo más reciente y su posición final; el productor no modificará los datos que esté usando el clasificador.

La coordinación real tendrá dos corrutinas hijas: captura y reconocimiento. Un `Channel(CONFLATED)` entregará ventanas: **una en ejecución y como máximo una pendiente**, reemplazando la pendiente por la más reciente cuando el consumidor esté ocupado. La captura no esperará a la inferencia ni se lanzará una corrutina nueva por ventana. El canal, el anillo y los buffers de trabajo estarán limitados; nunca se guardará el audio de toda la sesión. El cierre cancelará el canal y descartará la ventana pendiente, sin drenar trabajo después de Detener.

La captura informará su inicio efectivo con frecuencia y reloj monotónico. El reloj visible se actualizará una vez por segundo a partir de ese inicio, no del porcentaje de diez segundos. Este reloj no decidirá cuándo inferir. Las mediciones de audio incluirán índices de muestras y tiempos monotónicos de prueba; no se registrará PCM en producción.

### Señal e inferencia por ventana — RF-7, RF-14, RF-21 y RF-23

Ampliar `toModelSamples()` para aceptar CapturedAudio de **3 o 10 segundos**, con salidas de 96.000 o 320.000 Float, respectivamente. Los diez segundos permanecen solo para los contratos internos y corpus que ya los utilizan; no habrá un segundo modo en la interfaz. Conservar conversión PCM y filtro antialias, reutilizando coeficientes de cada frecuencia durante una sesión en vez de generarlos en cada actualización. Validar extremos, límites de ventana y las dos frecuencias alternativas antes de conectar captura.

Añadir `BirdNetClassifier.classifyWindow(samples: FloatArray): FloatArray` para exactamente 96.000 valores finitos. Factorizar la ejecución nativa actual para que este método y `classify()` compartan una sola implementación de verificación, tensor, RunOptions y salida. `classify()` conservará sus cuatro ventanas para regresiones y evaluación histórica; no se duplicará un motor de inferencia ni se adquirirá el Mutex recursivamente.

Se mantendrán sesión ONNX de CPU, dos hilos intraoperador, nombres de tensor, tipo Float32, 9.789 puntuaciones, comprobaciones de integridad y cancelación nativa actuales. Los pesos y licencias no cambian. Cada ventana constante se convertirá en ausencia de candidatos sin entrar en el modelo; NaN, infinito, rango o dimensiones incorrectos seguirán siendo error. No se aplicará otra sigmoid, softmax o normalización por suma.

El consumidor remuestreará la ventana en ejecución fuera de Main. Tras procesarla publicará candidatos solo si la sesión sigue activa y su token coincide. Para evitar mostrar audio envejecido, se descartará una ventana pendiente que ya haya terminado hace más de tres segundos; se esperará la próxima reciente. Una inferencia que dure cinco segundos se abortará usando RunOptions y terminará la sesión con error, conservando la lista publicada. No se intentará procesar en paralelo ni aumentar la cola para ocultar un dispositivo que no cumple el presupuesto.

La cancelación y el cierre conservarán la exclusión mutua: invalidar primero el intento, solicitar terminación, esperar a que `run` salga y cerrar recursos fuera de Main. Una sesión creada después de invalidar su preparación se cerrará sin publicarse. La detención no permitirá reutilizar una sesión nativa todavía limpiándose.

### Selección y lista acumulativa — RF-8 a RF-13 y RF-15

Extraer de `BirdSoundCatalog.select()` una operación `selectAll(scores, minimumScore)` que conserve validación, agrupación canónica, filtro Aves y nombres/recursos, sin recortar a cinco. `select()` delegará en ella y conservará su límite para los consumidores internos anteriores. La escucha nueva utilizará selectAll: todas las especies elegibles de una ventana pueden incorporarse, sin imponer a la sesión el antiguo límite.

El ViewModel mantendrá un LinkedHashMap cuya clave sea el nombre científico canónico normalizado con Locale.ROOT, equivalente a la identidad que ya usa el catálogo. En la primera detección se insertará la fila al final. Para varias altas en una misma actualización, el desempate será puntuación descendente y luego índice del modelo, usando el orden determinista del catálogo; esto no reordena las especies de actualizaciones anteriores.

Una detección repetida solo sustituirá los datos de su fila si su puntuación aumenta, conservando su posición. No habrá caducidad, eliminación automática ni ordenamiento global por confianza. Una ventana sin candidatos no cambia la lista. Las nuevas instantáneas de resultados se publicarán solo si cambian filas; actualizar el reloj no reconstruirá ni reenviará la lista a su adaptador.

Al aceptar Escuchar con permiso y sin otra operación, se vaciarán el mapa y la lista visible antes de preparar. Si falla la preparación, la sesión nueva quedará vacía con error: no se restituye la anterior. Una denegación de permiso, por sí sola, no es un inicio aceptado y no borrará una lista detenida. El reintento desde error sigue la misma regla de sesión nueva.

Umbral de operación del flujo continuo: **0,50**. Inicialmente propuesto en 0,35; en la evaluación experimental continua de 5 minutos (298 ventanas) sobre habla humana ininterrumpida a 0,35 se acumularon dos falsos positivos marginales (< 0,50), incumpliendo el criterio de aceptación. En la calibración inicial, 0,50 satisfizo formalmente los requisitos (8/10 positivos y 0/4 controles reales). Conforme al plan, tras acuerdo explícito con el usuario se fijó el umbral en **0,50** y se preparó un segundo conjunto independiente de tres controles reservados de 5 minutos (Set B / V2) para su validación formal. No se añadirá suavizado temporal, confirmación por varias ventanas ni blacklist de puntuaciones.

### Estado y detención — RF-1 a RF-6 y RF-14 a RF-23

Conservar el ViewModel de Activity y su interfaz interna de sesión para las pruebas. Sustituir el contrato grabar-y-analizar por `prepare()`, una operación suspendida `listen(...)`, `cancel()` y `close()`. La operación listen coordinará el productor y consumidor bajo el mismo alcance estructurado y entregará inicio efectivo y detecciones mediante callbacks; el callback de ventana del productor solo ofrecerá al canal y nunca hará inferencias. Los callbacks de estado/detecciones se consumirán secuencialmente en Main, sin trabajos fire-and-forget por resultado.

El estado observable separará fase y contenido: listo, preparando, escuchando, deteniendo, detenido y error; junto con lista inmutable, tiempo transcurrido y motivo de interrupción cuando exista. No se guardará PCM ni un registro de todas las ventanas en StateFlow, Intent o SavedStateHandle. La instancia del ViewModel conserva la sesión durante rotación; el cierre del proceso no se recuperará.

Detener se aceptará en Main y congelará inmediatamente la instantánea ya publicada: invalidará el token antes de cancelar captura/inferencia. El estado Deteniendo mantendrá esa lista y evitará otro inicio hasta completar la liberación. No se publicará un análisis final de audio parcial ni una ventana que termine después del evento de detención. El micrófono y la animación dejarán de anunciar escucha; el texto indicará Deteniendo hasta completar la limpieza.

Al finalizar la limpieza se mostrará Detenido, con Escuchar y la misma lista. Si está vacía, el mensaje será el de ausencia de coincidencias de la sesión. En una escucha todavía vacía se mostrará Escuchando / Esperando coincidencias, sin error ni finalización automática.

El productor o consumidor que falle cancelará a su hermano y cerrará el intento, conservando solo la lista válida ya publicada y el motivo del error. Los recursos nativos se cerrarán también al detener, manteniendo la política actual de liberar memoria entre sesiones. Un nuevo inicio recreará los recursos; la latencia de preparación tendrá presupuesto propio.

La Activity conservará el flujo de permiso diferido. El callback requerirá RESUMED e intención vigente; volver de Ajustes no activará el micrófono. Una salida real al segundo plano invalidará también una preparación o intención pendiente. Rotar mantendrá esa intención, sin emitir una segunda solicitud.

`onStop()` con `!isChangingConfigurations` detendrá conservando lista y marcará la interrupción. Una rotación conservará captura, lista, orden y reloj; el inicio del reloj pertenece al ViewModel, no a onCreate. Salida con Atrás o cabecera abandonará y borrará la sesión. No habrá servicio, permiso nuevo, wakelock ni audio persistente. La app seguirá deteniéndose si se bloquea la pantalla; para el ensayo prolongado se configurará y restaurará el timeout del dispositivo de prueba sin incorporarlo al producto.

Para abrir ficha durante escucha, congelar primero la sesión y esperar la liberación antes de lanzar el Intent. Una única navegación pendiente guardará la especie elegida; la espera ocurrirá en una corrutina de ciclo de vida sin bloquear Main. Al completar, comprobar que la Activity sigue activa, que no cambió el intento y que la especie pertenece a la lista. Mantener el bloqueo de doble toque. Si la limpieza falla no se abrirá otra pantalla con el micrófono activo; se mostrará el error conservando resultados. Desde Detenido se podrá consultar directamente.

La consulta usará los extras y URI locales existentes. Volver de ficha o zoom conservará Detenido y los resultados, sin micrófono. El onStop provocado por esa navegación será idempotente y no sustituirá la lista por un error nuevo. No se modifican BirdInfoActivity ni los datos de observaciones.

### Presentación y accesibilidad — RF-4, RF-5 y RF-8 a RF-19

Reorganizar únicamente `activity_bird_sound.xml`: cabecera actual, estado compacto y acción Escuchar/Detener fuera del área desplazable, y lista con el espacio restante. Un RecyclerView será el único desplazamiento vertical; retirar el NestedScrollView que envuelve a las filas, evitando scrolls anidados o inflar todos los resultados de una sesión larga. La explicación extensa y los avisos/atribuciones irán como cabecera y pie desplazables del mismo RecyclerView, usando ConcatAdapter disponible en la dependencia actual y adaptadores locales pequeños en el archivo de presentación. Así no desplazarán Detener fuera de pantalla en horizontal y fuente al 200 %. El bloque fijo conservará texto y objetivos táctiles; no se reducirá la fuente para forzar el encaje.

`BirdSoundResultAdapter`, basado en ListAdapter/DiffUtil ya disponibles, reutilizará `item_bird_sound_result.xml`. Identidad por científico canónico y contenido por los campos presentados; cambiar una puntuación actualizará esa fila, sin reconstruir todas. Desactivar animación de cambio que cause parpadeo; conservar posición, foco y selección al agregar al final. No desplazar automáticamente hacia la última detección. Al iniciar una sesión nueva se llevará la lista vacía a su inicio; durante rotación se restaurará su posición, postergando restauración hasta que el adaptador reciba la lista.

Los estados Preparing/Listening/Stopping/Stopped/Error conservarán resultados cuando corresponda. Retirar los textos de diez segundos, porcentajes de captura y `removeAllViews()` de las transiciones. La lista se titulará «Aves detectadas en esta sesión»; el reloj mostrará mm:ss y permitirá horas sin volver a cero. Mantener nombres, aviso de información local ausente, puntuación con una cifra decimal, explicación y atribución existentes.

La animación será un pulso suave del icono de micrófono mediante las APIs de animación de Views, solo durante escucha confirmada y Activity visible. No mide volumen. Se creará una sola animación por Activity, se cancelará al dejar de escuchar/onStop/onDestroy y se restablecerá la escala. Se seguirá la guarda ya usada por LearningGameActivity: `ValueAnimator.areAnimatorsEnabled()` desde API 26 y lectura de duración de animador en API anteriores; desactivada, conservar icono y texto estáticos. Sin nueva biblioteca ni animación en bucle durante preparación o detención.

TalkBack recibirá un aviso breve por lote de especies nuevas, sin volver a leer toda la lista ni cada mejora de puntuación. El reloj y el pulso no serán regiones live. Las actualizaciones no moverán el foco al encabezado ni a una fila nueva. Se mantendrán los 48 dp y estilos de botones/paleta existentes en ambos temas; la animación no será la única señal del micrófono.

## Cambios previstos

Rutas relativas a la raíz. Se crearán los archivos solo durante implementación aprobada.

| Archivo o área | Cambio | RF |
| --- | --- | --- |
| `app/src/main/java/com/example/avesargentinas/ui/sound/AudioRecorder.kt` | Captura continua y callbacks de inicio/ventana; conservar inicialización, cancelación y errores. Retirar la captura fija del flujo de producto y actualizar sus consumidores/pruebas. | 4, 6, 7, 14, 19–23 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/AudioWindowBuffer.kt` | Anillo y partición pura de ventanas de 3 s cada 1 s, con memoria acotada. | 7, 20, 23 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundAudio.kt` | Conversión/remuestreo de ventanas de 3 s, conservando soporte de 10 s para consumidores internos existentes. | 7, 21 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdNetClassifier.kt` | Exponer inferencia de una ventana y compartir lógica con el método existente; cancelación y timeout sin cerrar durante run. | 7, 14, 21, 22 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundCatalog.kt` | Selección de todas las especies elegibles con nombres y recursos actuales; conservar select limitado para regresiones. | 8, 9, 11, 12 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundViewModel.kt` | Sesión estructurada con canal acotado, fase/lista/reloj, acumulación, token e interrupciones. | 2–4, 6–10, 13–23 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundActivity.kt` | Permisos y navegación conservados; renderizado continuo, animación, detención antes de ficha y lista estable. | 1–6, 10–19, 22 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundResultAdapter.kt` | Filas recicladas e identificación estable; actualización puntual y acciones de ficha. | 8–12, 16–18 |
| `app/src/main/res/layout/activity_bird_sound.xml`, `item_bird_sound_result.xml`, `app/src/main/res/values/strings.xml` | Un único flujo continuo, lista, estados y textos. Recursos dimensionales solo si falta uno equivalente. | 1, 4, 5, 11–17 |
| `app/src/test/java/com/example/avesargentinas/ui/sound/` | Ventanas, señal, elegibilidad y orden/acumulación donde sea lógica pura. | 3, 7–11, 14 |
| `app/src/androidTest/java/com/example/avesargentinas/ui/sound/` | Adaptar pruebas de recorder/ViewModel/Activity y añadir inferencia, evaluación y benchmark continuos; conservar regresiones de modelo y señal. | 1–23 |
| `app/src/androidTest/assets/birdnet/` | Solo fixtures adicionales autorizados y manifiesto de evaluación continua, sin modificar las particiones históricas de la spec 003. | Calidad, privacidad |
| `specs/004-escucha-continua-aves/evaluacion-continua.md` | Evidencia, umbral final, latencias, recursos y pruebas prolongadas; se creará durante validación. | Finalización 1–9 |

## Datos, contratos y compatibilidad

- Reutilizar BirdDetection y los recursos del catálogo. La colección acumulativa solo existe en memoria y crece por especies distintas, no por cada detección o segundo transcurrido; su máximo natural son las identidades aviares del catálogo.
- `AudioRecorder.listen(onStarted, onWindow)` reemplazará al uso de `record()`; la lectura seguirá activa hasta cancelación/error. onStarted comunicará frecuencia y origen del reloj; onWindow entregará CapturedAudio de `3 * rate` muestras, posición final Long y tiempo de captura. La firma final del evento se mantendrá específica de sonido, sin framework de eventos general.
- `BirdSoundAudio.toModelSamples()` conservará el nombre, validando duraciones de 3/10 s y frecuencia admitida. `classifyWindow()` consume 96.000 Float y devuelve 9.789 puntuaciones; `classify()` mantiene el contrato de 320.000 para sus consumidores actuales.
- `BirdSoundCatalog.selectAll(scores, minimumScore)` no limita el total; `select(scores, minimumScore, limit)` conserva su comportamiento anterior. Ningún otro consumidor cambiará accidentalmente de top cinco a catálogo completo.
- La interfaz interna SoundSession y sus dobles se actualizarán juntos. Los callbacks de detecciones serán suspendibles para entregarse en Main sin acumular trabajos; el productor de ventanas solo realizará una oferta no bloqueante. Un fallo en cualquiera de las corrutinas cancelará ambas, propagando la causa al ViewModel.
- Los mensajes tardíos de una sesión anterior no pueden limpiar la lista nueva ni cambiar su fase. Las acciones públicas se serializan en Main y la propiedad de AudioRecord/sesión nativa se conserva hasta finalizar su limpieza.
- No cambiar modelo, huellas, catálogos, ONNX/LiteRT, minSdk 24, targetSdk 36, ABI, permisos o contratos de fichas. No hay migración de base de datos ni persistencia de sesiones. No se alteran evidencias históricas para hacerlas coincidir con requisitos nuevos.

## Errores y casos límite

| Situación | Respuesta técnica |
| --- | --- |
| Detener antes de la primera ventana o durante inferencia | Invalidar token y congelar lista vacía/actual; cancelar productor, pendiente y run; no calcular un resultado final diferido. |
| Inferencia más lenta que nuevas ventanas | Mantener solo la pendiente reciente; contador de ventanas omitidas en evidencia de prueba, sin cola creciente. Exigir igualmente el presupuesto de latencia. |
| Captura sin muestras, micrófono ocupado o revocado | Watchdog/error existente, cancelar consumidor, conservar lista publicada y liberar; no completar ventanas con ceros artificiales. |
| Silencio real constante | Ventana sin candidatos, continuar escucha y conservar filas; un error nativo no se convierte en silencio. |
| Repetida con mayor/menor puntuación | Máximo por identidad, misma posición y notificación solo de contenido cambiado. |
| Permiso tarde, Home durante preparación o Ajustes | Invalidar intención al salir realmente; no iniciar después ni autograbar al regresar. |
| Rotación con AudioRecord activo | Misma sesión en ViewModel, otro observador de UI y una nueva animación visual, sin duplicar micrófono/temporizador. |
| Ficha durante escucha | Detener y esperar release antes de navegar; token y destino vigente, doble toque bloqueado. Volver en estado detenido. |
| Error con lista no vacía | Estado de error junto a lista congelada; un nuevo inicio limpia antes de preparar. |
| Lista extensa y lectura de fila antigua | Reciclado, sin auto-scroll ni reconstrucción global; Detener permanece alcanzable. |

## Estrategia de validación

### Lógica, señal y concurrencia

- Anillo con bloques pequeños/grandes/desiguales, lecturas que cruzan varios saltos, 3 s iniciales y 1 s posteriores, frecuencias admitidas y conteos Long. Comprobar muestras exactas de ventanas y que no cambian al capturar más. Simular al menos una hora sin aumentar la capacidad de los buffers ni guardar todo el audio.
- Remuestreo de 3/10 s, extremos PCM, 1/10 kHz dentro de 1 dB y 18 kHz atenuado al menos 40 dB para 48/44,1 kHz. Comparar las ventanas convertidas con referencias consistentes, sin relajar los tests anteriores para aceptar un cambio de señal no explicado.
- `classifyWindow()` real sobre las tres ventanas de referencia disponibles y comparación por clase con diferencia absoluta ≤ 0,001, incluida paridad con la ruta interna equivalente. Constantes sin NaN, error de dimensión, fallo de carga y cancelación nativa. La ruta de diez segundos sigue pasando sus referencias.
- Selección all frente a top cinco, seis o más aves en una actualización, clases no aviares, alias, nombres/mapas ausentes, umbral incluido con >= y datos inválidos. Acumulación con inserción estable, máximo de puntuación, ventanas vacías y lista nueva al reintentar.
- Sesión controlada: captura produce ventanas mientras inferencia está suspendida; como máximo una pendiente y procesamiento de la última al desbloquear. Detener e iniciar de nuevo, resultados viejos, errores de ambos hijos, preparación tardía y timeout. Comprobar orden de liberación y estados, no solo llamadas o textos.
- Recorder físico más allá de 10 s y hasta 60 s como comprobación corta; verificar que solo se inicializa/arranca una vez. Watchdog sin muestras, revocación, permisos, ruido/silencio y alternativas de frecuencia. Liberación efectiva ≤ 1 s tras Detener, bloqueo o Home.

### UI, ciclo de vida y regresiones

- Recorrido real Escuchar → detecciones durante captura → Detener → lista idéntica → Escuchar → lista vacía. Detener antes de 3 s y durante inferencia. Capturas/animación/control coherentes en todos los estados.
- Con datos controlados solo para presentación, introducir más de cinco y una lista de al menos 200 especies: foco y primer elemento visible conservados al añadir/mejorar filas; ninguna reconstrucción completa por tick del reloj. Esa prueba no cuenta como calidad acústica.
- Rotar durante preparación, escucha, detención y ficha; una sesión/permiso, tiempo continuo y orden intacto. Home/bloqueo retienen lista detenida, consulta activa espera al cierre y regresa detenida; muerte real del proceso regresa a listo sin sesión.
- Matriz claro/oscuro, vertical/horizontal, texto normal/200 %, pantalla pequeña y animaciones desactivadas. Acciones de 48 dp, escucha y detención claras, lista alcanzable, sin auto-scroll. Recorrido manual de TalkBack con detecciones entrantes y actualización de puntuaciones, sin anuncios repetidos del reloj o de toda la lista.
- Comprobar identificación fotográfica, captura instantánea, menú, fichas/zoom, historial/exportación, aprendizaje y preferencias. Ejecutar por separado desde la raíz los cuatro comandos oficiales: `.\gradlew.bat :app:assembleDebug`, `.\gradlew.bat :app:testDebugUnitTest`, `.\gradlew.bat :app:lintDebug` y `.\gradlew.bat :app:connectedDebugAndroidTest`, con dispositivo seleccionado explícitamente.
- La línea base se medirá antes de implementar porque el árbol contiene cambios de la spec 003 sin commit. Preservarlos; distinguir fallos heredados de regresiones nuevas y no declarar una suite verde basándose solo en casillas de documentos o resultados históricos. Los fallos y comprobaciones omitidas se registrarán como pendientes.

### Calidad de las detecciones continuas

Estas son condiciones propuestas para aprobar este plan; todavía no se ejecutaron.

- Reutilizar sin modificar los 42 clips y particiones de la spec 003 como corpus de regresión de 10 s. Para el flujo nuevo, alimentar cada clip como captura continua y analizar ventanas de 3 s con salto de 1 s, reiniciando la sesión entre clips para no mezclar originales. Mantener todas las detecciones elegibles y no recortar la lista a cinco para calcular el resultado.
- Calibrar solo en los diez positivos y cuatro controles reales de calibración existentes, con candidatos 0,35/0,50/0,65/0,80. Tras la evaluación inicial y revisión formal aprobada por el usuario, el umbral operativo del flujo continuo se fijó en **0,50** (cumpliendo 8/10 especies objetivo detectadas y cero detecciones aviares en los cuatro controles reales).
- En los veinte positivos restantes exigir la especie objetivo en la lista en ≥ 16/20; como máximo un control real de cuatro con alguna ave y cero aves en los cuatro silencios constantes. Registrar también cantidad y nombres de falsos candidatos, demora hasta la primera detección objetivo y tamaño final de lista. Este corpus ya fue utilizado en la spec 003: es regresión y no un nuevo ensayo ciego ni una estimación de precisión global.
- Controles prolongados independientes de **5 minutos** sin aves: para validar el umbral revisado de 0,50, se preparó un segundo conjunto independiente reservado de tres controles de 5 minutos (Set B / V2: voz consentida en Oberá, Misiones; viento gélido de Beeld en Geluid; y lluvia de tormenta en Uetersen), con procedencia/licencia, hash y anotación humana. En cada sesión se exige como máximo una especie aviar falsa acumulada. No se elimina un control difícil tras conocer su resultado.
- Comprobar además en audio autorizado con varias especies que aparecen en momentos distintos y repiten después, verificando acumulación y puntuación máxima con el modelo real. Si ese montaje se prepara a partir de clips conocidos, declararlo como prueba controlada de integración y no como grabación de campo independiente.

### Latencia, memoria y consumo en Samsung A56

- Ensayo de **15 minutos de escucha continua**, sin interrupciones del micrófono, en modo avión, sin cargar el teléfono ni depurador, brillo fijo al 50 %, ambiente de referencia entre 20 y 27 °C. Registrar modelo/Android/RAM/página, estado inicial de batería y térmico y configuración. Ajustes temporales del dispositivo se restaurarán al terminar. Hacer otro ciclo corto fotografía → sonido para verificar recursos compartidos.
- Preparación fría ≤ 5 s; primera actualización de reconocimiento ≤ 5 s desde el primer audio capturado. Actualizaciones analíticas en al menos el 95 % de ventanas procesadas dentro de **2 s desde su final de captura**; medir aunque no contengan aves, sin confundir ausencia de nuevas filas con falta de reconocimiento. El objetivo normal es una ventana cada segundo; ventanas saltadas y edad de la pendiente se registrarán. No se aceptará deterioro creciente entre los primeros y últimos cinco minutos.
- PSS total del proceso ≤ **512 MiB**, incluida transición desde fotografía. Con cantidad fija de especies, comparar medianas de PSS de minutos 4–5 y 14–15: crecimiento retenido ≤ **20 MiB** y sin crecimiento del número de buffers o tareas pendientes. Medir por muestreo y una inspección de memoria nativa, distinguiendo la colección legítima de especies del audio/trabajo retenidos.
- CPU total del proceso media ≤ **100 % de un núcleo lógico equivalente**, calculada como tiempo CPU dividido por tiempo de pared, sumando sus hilos. Se registra la fórmula y las medidas, sin confundir ese 100 % con ocupar todos los núcleos.
- En las condiciones anteriores, consumo ≤ **5 puntos porcentuales de batería en 15 minutos**, incremento de temperatura de batería ≤ **5 °C**, y ausencia de estado térmico SEVERE o superior reportado por Android. Registrar nivel/temperatura y estado al menos al inicio y cada minuto. Son presupuestos propuestos del ensayo controlado, no una garantía de autonomía para toda temperatura ambiente.
- Detener deja inmutable la lista inmediatamente y libera el micrófono ≤ **1 s**. La sesión nativa termina y cierra ≤ **5 s**; durante limpieza no se permite otro inicio. Repetir cinco ciclos de inicio/escucha/detención y verificar cierre sin crecimiento retenido sostenido. Inicio, stop, error y salida no bloquean Main.
- Si alguna medición falla, documentar datos reales y revisar el diseño o presupuesto antes de seguir. No bajar frecuencia, cambiar umbral o retirar compatibilidad para encubrir el incumplimiento del plan aprobado.

## Riesgos y decisiones pendientes

- El máximo acumulado aumenta la oportunidad de falsos positivos con la duración: un umbral probado en capturas cortas no basta. Los controles prolongados son un criterio independiente, no se reemplazan con un silencio digital o un video de UI.
- Las ventanas más recientes tienen prioridad bajo carga, por lo que una vocalización muy breve puede omitirse si la inferencia no sostiene el ritmo. El límite de cola evita retraso creciente, pero no demuestra calidad; se medirán omisiones y latencia.
- La selección sin límite y las actualizaciones continuas pueden elevar el costo de filas y catálogo; se reutilizarán colecciones y filas de forma proporcional al cambio. Optimizaciones adicionales se justificarán solo si falla una medición concreta.
- Si el teléfono o los controles prolongados autorizados no están disponibles, las verificaciones correspondientes quedan pendientes. No se sustituye micrófono, batería, TalkBack o temperatura reales con dobles.
- No hay decisiones funcionales nuevas pendientes. Los presupuestos y reglas de calibración aquí propuestos se aprueban con el plan; cualquier cambio posterior se contrastará con la spec antes de implementar.

### Referencias técnicas primarias

- [Canal conflado con una pendiente](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/-factory/-c-o-n-f-l-a-t-e-d.html), [ListAdapter](https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter) y [estado térmico de Android](https://developer.android.com/reference/android/os/PowerManager#THERMAL_STATUS_SEVERE), consultados el 27 de octubre de 2026. Se usarán las APIs compatibles con las versiones ya instaladas; consultar documentación actual no autoriza actualizarlas.
