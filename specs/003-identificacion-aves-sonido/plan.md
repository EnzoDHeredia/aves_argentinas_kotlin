# Plan técnico — Spec 003: Identificación de aves por sonido

Estado: aprobado explícitamente por el usuario el 26 de octubre de 2026. Fecha: 26 de octubre de 2026.

## Enmienda aprobada para T4: grafo derivado con conversiones de precisión

**Aprobada explícitamente por el usuario el 26 de octubre de 2026:** respondió
«si» a la solicitud de aprobación de esta enmienda. La aprobación habilita preparar
la revisión de tareas; sustituir assets o implementar requiere aprobar las tareas
revisadas y respetar la cadencia de una tarea por turno. T4 sigue abierta y T5 no
está autorizada. Las demás decisiones aprobadas se conservan salvo la excepción
puntual sobre el binario empaquetado descrita aquí.

### Objetivo, evidencia y alternativas

La spec aprobada y sus requisitos RF-8, RF-9, RF-11 a RF-14, RF-17, RF-24 y
RF-25 no cambian. Se busca ejecutar el mismo modelo entrenado y mantener sus
9.789 clases, pero resolver una diferencia de cálculo entre CPU x86-64 y ARM64.
En el Samsung el modelo oficial excedió el límite de 0,001 frente a las tres
referencias independientes. Las mismas entradas y referencias dieron diferencias
máximas de 0,0000078082085, 0,000006556511 y 0,0000008940697 en un ensayo con
un grafo temporal generado por ONNX Runtime 1.22.0 en PC. La investigación y sus
límites están en [evaluacion-audio.md](evaluacion-audio.md#continuación-de-t4--diagnóstico-de-precisión-y-ciclo-de-vida).

- **Mantener el binario oficial sin transformación:** conserva la trazabilidad
  original, pero en el Samsung el ensayo actual no satisface el criterio aprobado;
  desactivar optimizaciones en Android tampoco lo resolvió.
- **Derivar un grafo con conversiones mediante el runtime oficial:** es la opción
  propuesta para evaluación. El ensayo preliminar cumple el límite en las tres
  señales, pero no demuestra por sí solo compatibilidad, reproducibilidad,
  rendimiento ni calidad de identificación.
- **Cambiar runtime, modelo entrenado, referencias o tolerancia:** queda fuera de
  esta propuesta. Requeriría una decisión y revisión separadas; no se ajustarán
  resultados ni el límite para que una prueba pase.

### Preparación y procedencia propuestas

Se conservará el modelo oficial como **fuente verificable**, con 66.991.322 bytes,
SHA-256 `306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457`,
commit, etiquetas, metadatos, licencias y atribuciones de T1. El derivado no se
presentará como binario oficial sin modificar ni como un modelo recién entrenado.
Se generará desde ese archivo comprobado, usando ONNX Runtime **Python 1.22.0**
oficial en Windows x86-64, CPU, ejecución secuencial, dos hilos intraoperador y
optimización de grafo desactivada; se guardará el grafo preparado por la sesión.
Esta herramienta es de preparación, no una dependencia Python de la aplicación.

El manifiesto distinguirá fuente y derivado: origen y huella de la fuente,
comando y entorno de preparación, distribución y versión exacta del runtime de
preparación, ajustes efectivos, tamaño y huella del derivado y fecha de generación.
La preparación rechazará un origen de tamaño o huella incorrectos. Dos generaciones
independientes con el entorno fijado deberán producir un archivo idéntico byte a
byte; si no lo hacen, se investigará la causa antes de fijar una huella o distribuirlo.
El SHA-256 experimental `cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`
es evidencia del ensayo, no una huella aprobada automáticamente para distribución.
Se comprobará además la licencia del modelo y del runtime aplicado a la variante,
se conservarán los avisos pertinentes y se identificará la transformación en la
atribución. Una condición de licencia no resuelta impide la incorporación.

Solo después de aprobar la revisión de tareas correspondiente se
podrá proponer que el APK cargue **el derivado en lugar del binario oficial**,
sin comprimir, conservando una única copia del modelo en el APK. El original seguirá
disponible como entrada verificable de preparación, recuperable desde el origen
fijado; no se borrará ni reemplazará durante la revisión. La sustitución modifica
la decisión previa de T1 sobre qué binario exacto se empaqueta y exige repetir las
comprobaciones afectadas: no invalida retrospectivamente su evidencia histórica.
El manifiesto y la comprobación de integridad identificarán por separado ambas
huellas y detectarán una mezcla de modelo, etiquetas o metadatos incompatible.

### Contratos y áreas afectadas

El derivado mantendrá los contratos comprobados: entrada Float32 `input` compatible
con `[1, 96000]`, salida Float32 `predictions` de `[1, 9789]`, etiquetas en las
mismas posiciones y puntuaciones finitas en [0,1]. No se agregará sigmoid, softmax,
filtrado de clases ni un modelo alternativo. No se cambia la captura, las cuatro
ventanas, la agregación por máximo, el tratamiento de silencio ni el umbral
provisional de selección. ONNX Android permanece en 1.22.0, CPU secuencial y dos
hilos; no se incorporan GPU, NNAPI, ABI restringidas, permisos o versiones mínimas
nuevas. La app mantendrá el ByteBuffer mapeado hasta cerrar la sesión y conservará
la cancelación y cierre serializado ya exigidos.

La revisión de tareas identificará cambios mínimos en
`tools/prepare_birdnet_assets.py` o un preparador específico justificado;
`app/src/main/assets/birdnet/` y su manifiesto; `BirdNetClassifier.kt`;
`BirdNetModelAssetsTest.kt` y `BirdNetClassifierTest.kt`; y el registro de
evidencia. Las referencias existentes bajo `app/src/androidTest/assets/birdnet/`
se conservarán byte por byte. No habrá cambios en el modelo fotográfico, Room,
captura de audio ni interfaz como consecuencia de esta enmienda.

### Validación y condiciones de aceptación

Antes de incorporar el derivado, y nuevamente con el APK que lo empaquete:

1. Verificar fuente, preparación reproducible, licencias, hashes y correspondencia
   completa de las 9.789 posiciones. Confirmar nombre, tipo y forma de entrada y
   salida, salida finita y ausencia de cambios de activación o de orden de etiquetas.
2. Comparar cada una de las **tres referencias ya fijadas del modelo oficial en
   el runtime de desarrollo** con el derivado ejecutado en Android, para las
   mismas entradas y cada una de las 9.789 clases. Mantener diferencia absoluta
   máxima **≤ 0,001 por clase**, sin regenerar referencias ni alterar el criterio.
   Registrar máximos, entorno y cualquier fallo; las señales sintéticas no son
   evidencia de identificación de aves.
3. Repetir instalación, carga e inferencia reales del APK en Samsung sin red,
   Android API24 y API25 y entorno de página real de 16 KB, además de comprobar
   alineación y las cuatro ABI como en T1. Verificar también el modelo fotográfico,
   sus referencias y el resto de regresiones afectadas. Un entorno no disponible
   permanece pendiente; no se compensa con otro.
4. Repetir las pruebas de T4: cuatro ventanas y final, silencio, entrada y salida
   inválidas, fallo de inicialización y reintento, cancelación durante ejecución,
   inicialización tardía, cierre serializado, no publicación tardía y respuesta
   de Main. Medir ciclos repetidos y retención nativa; no inferir ausencia de fugas
   solo porque no hubo crash. Ejecutar `:app:assembleDebug`,
   `:app:testDebugUnitTest`, `:app:lintDebug` y las clases instrumentadas pertinentes.
5. Mantener intactos los ensayos de calidad y rendimiento de T9/T10 y sus criterios;
   aprobar la equivalencia numérica de tres señales no demuestra calidad acústica
   ni libera de los presupuestos de tiempo y memoria.

Si falla cualquier control, se registra el resultado, se mantiene T4 pendiente y no
se integra el derivado ni se inicia T5. No se cambia la tolerancia ni se eligen
otras señales para ocultar un desacuerdo. El uso de un grafo diferente, otra versión
de runtime o una alteración de compatibilidad requerirá otra decisión explícita.

## Enmienda aprobada para cerrar T1: runtime fotográfico y páginas de 16 KB

**Aprobada explícitamente por el usuario el 26 de octubre de 2026.** Tras aclarar
que se conservan ambos modelos y solo se actualiza el motor fotográfico, el
usuario respondió «si por favor, actualicemos y tratemos de cerrar la T1».
Esta aprobación reemplaza su decisión anterior de conservar TFLite 2.16.1 y
autoriza únicamente la migración y verificaciones de esta enmienda, dentro de T1.
T2 sigue sin estar autorizada. Las menciones del plan original a no actualizar
TFLite se mantienen como registro histórico y quedan exceptuadas solo por esta
enmienda; ONNX 1.22.0 y los modelos continúan fijados.

La prueba en el AVD oficial API35 `google_apis_ps16k`, página real 16.384 y
compatibilidad de 4 KB desactivada, cargó BirdNET con ONNX 1.22.0 y produjo las
9.789 puntuaciones válidas. La carga nativa del runtime fotográfico TFLite 2.16.1
provocó SIGSEGV y terminó el proceso. Es el fallo heredado de las bibliotecas
ELF de 4 KB, conservado en `app/build/birdnet-verification/closure/`.

Cambio mínimo aprobado, limitado a las dependencias fotográficas de
`app/build.gradle.kts`; versión efectiva tras el ajuste técnico documentado abajo:

```kotlin
implementation("com.google.ai.edge.litert:litert:1.4.1")
```

Reemplaza `org.tensorflow:tensorflow-lite:2.16.1`. Se retiran las dependencias
`tensorflow-lite-support:0.4.4` y `tensorflow-lite-metadata:0.4.4`, que ningún
archivo del proyecto importa, para evitar arrastrar las API antiguas duplicadas.
La API `org.tensorflow.lite.Interpreter` se conserva; `litert-api:1.4.1` llega
transitivamente. No se cambia el modelo fotográfico ni sus etiquetas, el modelo
BirdNET, ONNX Android 1.22.0, minSdk24, las cuatro ABI, NNAPI/GPU, umbral ni
criterios de aceptación. No se habilita compatibilidad de 4 KB para ocultar el fallo.

El AAR candidato inicial 1.4.0 se descargó de Google Maven sin incorporarlo al
proyecto: SHA-256 `54b567572cb2d1295571c785198f87597809cbf22d6cb8acfeac23869041887d`,
minSdk21, las cuatro ABI y todos los segmentos PT_LOAD a 16.384 bytes. Detalle
en `closure/litert-candidate-alignment.txt`. La recomendación del mantenedor
está en [TensorFlow #96602](https://github.com/tensorflow/tensorflow/issues/96602).

Antes de aplicar se obtendrán referencias fotográficas con el runtime anterior
en un entorno de 4 KB. Después se comprobarán carga y predicciones del mismo
modelo/entradas, instalación y ejecución en API24/25, página real 16 KB sin
compatibilidad, Samsung en modo avión, Lint y unitarios. Se conservarán los
avisos de licencia del runtime fijo. Cualquier diferencia se investiga y no
se ajustan las expectativas para ocultarla. T1 seguirá pendiente hasta completar
esas verificaciones; T2 no está autorizada.

Las referencias anteriores ya se obtuvieron con TFLite 2.16.1 en API24 y API25
para las fotos locales photo-001/002/003. Se fijan antes de la migración los
tres índices y puntuaciones por imagen: mismo orden de índices y diferencia
absoluta por puntuación como máximo 0,001; sin ajustar esa tolerancia después
de observar LiteRT. Se comprobarán también las huellas de los modelos y entradas.

### Ajuste técnico de la actualización aprobada: parche fijo LiteRT 1.4.1

Durante la ejecución de la enmienda, 1.4.0 pasó Samsung y 16 KB pero falló en
API24: `dlopen failed: cannot locate symbol "strtod_l"`. No se elevó minSdk ni
se modificaron las pruebas. El mantenedor confirma ese defecto y su corrección
en [LiteRT #3274](https://github.com/google-ai-edge/LiteRT/issues/3274#issuecomment-3503705142)
y en la [publicación oficial 1.4.1](https://github.com/google-ai-edge/LiteRT/releases/tag/v1.4.1).

Decisión técnica dentro de la actualización del motor ya autorizada: fijar
`com.google.ai.edge.litert:litert:1.4.1` en lugar de 1.4.0. Es el parche oficial
para mantener Android21+; se conservan minSdk24, ambas familias de modelos,
las cuatro ABI, ONNX1.22.0 y todos los criterios. La aprobación del usuario
corresponde a actualizar el motor conservando ambos modelos; la elección de
este parche posterior al fallo es una decisión técnica documentada, no una
nueva aprobación atribuida al usuario.

El AAR 1.4.1 inspeccionado tiene SHA-256
`815ef39f7d40f579adda1c7bcb7ce090685fb286777ffd8283f33202f67da1b4`,
declara minSdk21, conserva las cuatro ABI y PT_LOAD a 16.384 bytes en todas.
La tabla dinámica ya no importa `strtod_l`; el contraste con 1.4.0 está en
`app/build/birdnet-verification/litert-migration/patch-inspection.txt`.
Validación final completada sobre la versión fija 1.4.1: 4/4 pruebas en Samsung,
API24, API25 y API35 con páginas reales de 16 KB; 30 unitarios, preparador 3/3,
Lint sin errores. Comandos, huellas y límites en
[evaluacion-audio.md](evaluacion-audio.md#cierre-de-t1--actualización-aprobada-y-litert-141).

## Referencias y alcance

Las secciones base siguientes describen el estado y las decisiones al aprobar
el plan, antes de ejecutar T1. La enmienda anterior define la excepción aprobada
para el runtime fotográfico; el estado final y las mediciones se registran en
[evaluacion-audio.md](evaluacion-audio.md).

- [Spec aprobada](spec.md): RF-1 a RF-25, requisitos no funcionales y ocho criterios de finalización.
- [AGENTS.md](../../AGENTS.md): conservar Kotlin y Android Views/XML, aprobar cada artefacto por separado, una tarea por turno durante implementación, sin commits ni push.
- [frontend-design](../../.agents/skills/frontend-design/SKILL.md): jerarquía y accesibilidad adaptadas a los recursos Android existentes.
- Decisión confirmada: BirdNET+ 3.0 reducido con ONNX. Dispositivo aportado por el usuario: «Samsung A56 s», tomado como Samsung A56; antes de medir se registrarán modelo exacto, Android, ABI, RAM y tamaño de página reales, sin presuponer una variante.

Este documento define el diseño y las comprobaciones. No genera tareas ni autoriza código. Las instrucciones del repositorio prevalecen sobre la descomposición en pasos y los commits predeterminados de otras skills.

## Estado actual

- `MainMenuActivity` usa Intents explícitos; el modo de captura instantánea salta al identificador fotográfico salvo al regresar con `from_back_press`. Se añadirá una entrada sin cambiar esa condición.
- La aplicación tiene minSdk 24, compileSdk/targetSdk 36 y código JVM 11. El Wrapper declara Gradle 9.6.0 y el daemon JDK 21. `app/build.gradle.kts` declara directamente TFLite 2.16.1, Lifecycle 2.7.0, corrutinas 1.7.3 y RecyclerView 1.3.2. No se actualizarán esas dependencias como parte de esta función.
- `LearningGameViewModel` y su Activity ya muestran el patrón AndroidViewModel + StateFlow + corrutinas y observación con `repeatOnLifecycle(STARTED)`. Se reutilizará ese patrón, sin biblioteca de inyección ni capa de navegación nueva.
- `BirdLabelsManager` carga nombres regionales desde `labels_map.json`. Sus claves son científicas con guiones bajos. No se usarán las posiciones de `labels.txt` del clasificador fotográfico para sonido.
- `BirdInfoActivity` acepta los extras de nombre común y científico; resuelve mapas locales y muestra un aviso cuando falla su carga. Las tres correspondencias históricas de patos ya existen allí. Las observaciones de Room exigen imagen; no habrá cambios en esa entidad ni migraciones.
- Hay evidencia histórica de problemas de compilación y seis errores de Lint en las specs 001/002. La configuración actual de Room es 2.7.2 y difiere de la evidencia antigua de Room 2.6.1: no se supondrá que el fallo antiguo sigue presente ni que quedó resuelto. Se ejecutará una línea base al iniciar implementación y se separarán fallos heredados de regresiones.

## Enfoque técnico

### Modelo y dependencias — RF-9, RF-11, RF-14 y RF-25

Se utilizará el archivo oficial de BirdNET Live `BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16.onnx`, con 9.789 clases, audio mono a 32.000 Hz y sus etiquetas `BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv`. FP16 describe los pesos; la entrada prevista sigue siendo Float32.

El puntero LFS consultado declara **66.991.322 bytes** y SHA-256 **306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457**. La adquisición comprobará ambos valores sobre el binario real; un puntero LFS o un contenido distinto no será aceptado. Se registrará también el commit de origen y las huellas de etiquetas, traducciones y licencia en un manifiesto del paquete. No se actualizará silenciosamente desde `main`.

Dependencia nueva de producción: `com.microsoft.onnxruntime:onnxruntime-android:1.22.0`, fijada directamente en `app/build.gradle.kts`, siguiendo su patrón actual. Se elige como candidato inicial conservador porque BirdNET Live documenta un fallo nativo en 1.23.0 y su referencia previa 1.22.0. Esto no constituye una certificación del candidato: la carga e inferencia en el Samsung serán una comprobación inicial obligatoria. No se usará `latest.release`, GPU, NNAPI, QNN, Flutter ni un runtime Python dentro de la app.

Antes de continuar con la pantalla se comprobarán el minSdk del AAR, sus ABI, los operadores del modelo y el empaquetado nativo de 16 KB. Se mantendrán las ABI que la app distribuya actualmente; no se eliminará compatibilidad para ocultar un fallo. Si el candidato no cumple, se revisará el plan con una versión fija respaldada por mediciones antes de seguir; no se subirá minSdk ni se sustituirá el modelo de forma implícita.

Modelo y datos se incluirán bajo `app/src/main/assets/birdnet/`, disponibles tras una instalación completa. Se configurará `androidResources.noCompress` para `onnx`; el clasificador mapeará el asset sin comprimir y creará la sesión desde un ByteBuffer directo. La API Java de 1.22.0 consultada incluye esa sobrecarga; falta verificar el camino con el APK y binario reales, sin trasladar las limitaciones del plugin Flutter a Java. Se conservará el buffer hasta cerrar la sesión, evitando cargar simultáneamente copias completas en ByteArray y memoria directa.

No se necesita cambiar a Git LFS en este repositorio: el binario seleccionado está por debajo de 100 MB y el proyecto ya contiene un modelo grande versionado. No se ejecutarán commits; la adquisición y el versionado final se verificarán al implementar. La app no descargará modelos ni añadirá permiso de Internet.

El repositorio oficial declara Apache 2.0 para los pesos incluidos. Se copiarán la licencia aplicable y los avisos asociados al archivo concreto. No se trasladarán a esta variante las condiciones de otro paquete BirdNET. La sección mostrará una atribución breve «Reconocimiento con BirdNET» con acceso local a los avisos, sin abrir un sitio remoto para leerlos.

### Preparación de etiquetas y nombres — RF-11 a RF-15 y RF-18

Una utilidad de desarrollo pequeña, `tools/prepare_birdnet_assets.py`, usará solo la biblioteca estándar para validar y preparar los datos, sin ejecutarse en Android. Recibirá archivos locales descargados de la fuente oficial: modelo, CSV de etiquetas, taxonomía con nombres españoles y licencia. No descargará fotografías ni descripciones.

Generará `species.json` con una entrada por posición del CSV, manteniendo los 9.789 índices `idx`, el identificador taxonómico, nombre científico, clase y nombre español cuando exista. Validará índices completos y únicos, correspondencias inequívocas y formato. El campo `id` no se usará como posición de salida. Guardará las fuentes y huellas en `manifest.json`; una prueba de autocontrol pequeña cubrirá índices intercambiados, duplicados y una correspondencia ambigua. Si la fuente de nombres españoles no está disponible o no tiene licencia verificable, se documentará la carencia y se aplicará el fallback científico permitido por la spec.

`BirdSoundCatalog` cargará esos recursos y el mapa de `BirdLabelsManager`. La prioridad será nombre regional → nombre oficial español → científico con «Nombre común no disponible». La ausencia de traducción no impedirá una detección. Solo `class == Aves` entrará en la lista final.

Para la ficha se usará una clave local independiente de la etiqueta del modelo. Se resolverá por nombre científico normalizado exacto o por un alias explícito respaldado por la taxonomía y comprobado contra recursos locales. No habrá búsqueda aproximada por nombre común, sustitución automática de géneros ni asociación por índice fotográfico. Un recurso inexistente o una correspondencia dudosa dejarán la ficha indisponible. Los alias se limitarán a los realmente necesarios y se documentarán con su fuente.

La detección de mapas disponibles será local: recursos `drawable` o assets comprobados, sin crear un catálogo de mapas nuevo. No se modificará el resolvedor privado de `BirdInfoActivity`; se usará el extra existente `EXTRA_DISTRIBUTION_URI` con el recurso local verificado cuando sea necesario. Así se evita duplicar su selección de alias o cambiar los accesos fotográficos. La ficha mantendrá su aviso si una imagen existente resulta ilegible.

### Captura y señal — RF-3, RF-5 a RF-8, RF-10 y RF-22

`AudioRecorder` usará AudioRecord, mono y PCM16, sin guardar un WAV. Intentará 32.000 Hz, luego 48.000 y 44.100 Hz si la configuración anterior no puede inicializarse. Comprobará el mínimo de buffer, el estado de inicialización y el inicio real. Se utilizará fuente `UNPROCESSED` cuando el dispositivo declare soporte, con `MIC` como alternativa; no se activarán cancelación de eco, reducción de ruido ni ganancia propias de llamadas.

La lectura se ejecutará en Dispatchers.IO con bloques pequeños y `READ_NON_BLOCKING`, cediendo cuando todavía no haya datos para evitar un bucle ocupado. El fin se determinará por **10 × frecuencia efectiva** muestras recibidas, no por un `delay(10000)`. El progreso se derivará de las muestras y se actualizará como máximo diez veces por segundo. Un watchdog abortará si transcurren dos segundos sin nuevas muestras después del inicio, o quince segundos sin completar la captura; una lectura cero no se convertirá en silencio inventado.

Se comprobarán errores de lectura y, desde API 29, captura silenciada por el sistema. Un cambio de configuración de captura indicará error cuando implique interrupción real. PCM todo a cero puede ser silencio acústico o bloqueo de privacidad no distinguible en API anteriores: se tratará como ausencia de señal, sin afirmar una causa que no pueda verificarse.

La cancelación marca la operación como inválida inmediatamente; el propietario de AudioRecord ejecuta stop/release en finally, antes de habilitar otra captura. La app no necesita audio focus para grabar y no interrumpirá reproducción ajena deliberadamente.

`BirdSoundAudio` convertirá PCM16 dividiendo por 32768f y remuestreará solo cuando la captura no sea de 32 kHz. Se usará un remuestreo sinc con ventana y filtro antialias, acotado a las dos frecuencias alternativas, sin dependencia de audio adicional. La elección concreta de longitud del filtro se fijará mediante los autocontroles de señal: 10 segundos producen exactamente 320.000 muestras; tonos de 1 y 10 kHz conservan amplitud dentro de 1 dB y uno de 18 kHz se atenúa al menos 40 dB. No se aceptará solo interpolación lineal o descarte de muestras para reducir la frecuencia. Los buffers de captura y conversión se liberarán tras analizar o cancelar.

### Inferencia y selección — RF-8 a RF-17

`BirdNetClassifier` tendrá una sola sesión ONNX de CPU y un único propietario de ejecución. Creará la sesión al primer intento de identificación, antes de activar el micrófono; durante la preparación la pantalla seguirá siendo navegable. La sesión se conservará hasta abandonar la sección. Configuración inicial: ejecución secuencial y dos hilos intraoperador, sin pool de inferencias simultáneas.

Se inspeccionarán las entradas y salidas al cargar: entrada `input`, Float32 `[1, 96000]` o dimensión dinámica compatible; salida `predictions`, 9.789 puntuaciones. Solo se pedirá la salida de predicciones. Nombres, tipos y formas se comprobarán contra el modelo real, sin inferirlos del nombre FP16; una diferencia detendrá la integración para corregir el contrato documentado.

Para cada captura se analizarán cuatro ventanas: **[0,3], [3,6], [6,9] y [7,10] segundos**. La última cubre el final completo sin rellenarlo ni analizar solo un segundo de audio. Se conservará por índice el máximo de sus cuatro puntuaciones y se agruparán después posibles índices con la misma identidad científica canónica, conservando su máximo. El umbral se aplicará después de agrupar y filtrar aves, y se tomarán cinco como máximo; empates se resolverán por índice de etiqueta para resultados reproducibles.

Las puntuaciones del modelo 3.0 ya incorporan su activación: no se aplicará softmax ni otra sigmoid. Se validarán longitud, finitud y rango [0,1]. Una salida inválida será error, no lista vacía. Un segmento de varianza exactamente nula se omitirá antes de inferir, ya que la referencia oficial documenta NaN en ese caso; si todos son constantes, el resultado será sin detecciones. No se añadirá un umbral de volumen que descarte aves lejanas sin validarlo.

Umbral inicial propuesto para calibración: **0,35**, sin ajustes por especie ni multiplicadores de la blacklist de BirdNET Live. Se usará una única regla reproducible validada para esta función; no se promete replicar su pipeline de escucha continua. El valor final solo podrá elegirse con el conjunto de calibración descrito abajo y se documentará antes de evaluar el conjunto reservado. Las puntuaciones se mostrarán con una cifra decimal como «Puntuación del modelo», con explicación de que no son probabilidades de acierto.

### Estado, ciclo de vida y recursos — RF-6, RF-7 y RF-19 a RF-25

`BirdSoundViewModel` seguirá el patrón del juego. Su StateFlow expondrá estados mutuamente excluyentes: listo, preparando, grabando, analizando, resultados, sin detecciones y error. Guardará progreso, detecciones y mensaje recuperable; no guardará PCM en estado de UI, Intent, Bundle o SavedStateHandle.

Cada intento tendrá un identificador creciente y un Job. Solo su identificador vigente podrá publicar progreso o resultados. Cancelar, nueva captura o salir invalidará el identificador antes de cancelar recursos. Las transiciones a una nueva operación esperarán la limpieza de la anterior; no se habilitará Grabar mientras se esté deteniendo el micrófono. Si la carga nativa termina después de cancelar la preparación, su sesión recién creada se cerrará en lugar de publicarse o quedar retenida.

`BirdSoundActivity` observará el StateFlow con `repeatOnLifecycle(STARTED)`. Usará Activity Result API para el permiso. El callback solo iniciará la captura si la Activity está RESUMED, continúa vigente la intención de grabar y no hay operación activa; después de rotación se conservará esa intención sin duplicar solicitudes. Una concesión al volver de Ajustes habilitará Grabar, sin activar el micrófono automáticamente.

En `onStop()`, si no es un cambio de configuración, se cancelará una preparación o grabación pendiente y se registrará el aviso de interrupción. Una rotación conservará el ViewModel y el AudioRecord, sin referencia retenida a la Activity. El análisis con captura ya completa podrá continuar al pasar a segundo plano; no se creará servicio. Al volver se observará el estado vigente. Esta continuidad no promete supervivencia frente al cierre del proceso.

Atrás o navegación de salida limpiará la sesión antes de finish; abrir ficha no la limpiará. Abrir ficha estará protegido ante doble toque. Se usarán los extras existentes y la pila sonido → ficha → zoom. Al volver se conservarán los mismos resultados. No se almacenarán estados para restaurarlos tras muerte del proceso.

Cada ejecución ONNX usará RunOptions; salir solicitará `setTerminate(true)` para la ejecución vigente, invalidará sus resultados y cerrará tensores, resultados y RunOptions en finally. La sesión y el buffer del modelo se cerrarán cuando la llamada haya terminado, nunca concurrentemente con `run`. Un cierre nativo no instantáneo no bloqueará el hilo principal ni permitirá publicar resultados tardíos. Se comprobará este comportamiento contra la API 1.22.0. No habrá un singleton con PCM ni una sesión global persistente.

### Presentación — RF-1, RF-2, RF-4 a RF-8 y RF-12 a RF-20

La pantalla mantendrá la cabecera, tratamiento de insets y temas de BaseActivity. Tendrá título y explicación breve, un área de estado con progreso/temporizador y una acción principal Grabar; durante captura se sustituirá por Cancelar. Resultados y errores ofrecerán Volver a grabar; los avisos de permiso tendrán acciones específicas. No se añadirá una señal animada ficticia.

Se reutilizarán `brand_background` (#E8EDF5), `brand_surface` (#F5F7FA), `brand_primary` (#1F4B99), `text_primary` (#1F2C40) y sus variantes nocturnas; márgenes y dimensiones existentes; estilos `Widget.AvesArgentinas.Button.Primary` y `.Outlined`. El temporizador será el foco durante captura y los nombres lo serán en resultados. Los textos tendrán altura natural, con desplazamiento y sin un contenedor de resultados que compita en scroll con otro.

La lista corta se dibujará con filas XML infladas dentro de un LinearLayout del contenido desplazable: **no hace falta un Adapter ni RecyclerView para un máximo de cinco resultados estáticos**. Cada fila mostrará nombre, científico, puntuación y una acción de ficha solo si existe; en los demás casos mostrará «Información local no disponible». No se hará clickable toda la fila si carece de acción.

El estado se anunciará una vez al cambiar mediante región accesible polite; el reloj no será live region. Habrá objetivos de 48 dp, lectura explícita de puntuación y texto ampliado sin recortes. Las atribuciones se abrirán en un diálogo de texto local desde la sección.

## Cambios previstos

Las rutas de esta tabla son relativas a la raíz del repositorio; los archivos nuevos solo se crearán durante implementación aprobada.

| Archivo o área | Cambio y responsabilidad | RF |
| --- | --- | --- |
| `app/build.gradle.kts` | ONNX fijo y assets sin compresión; sin actualizar TFLite ni restringir ABI/minSdk. | 9, 11, 25 |
| `app/src/main/AndroidManifest.xml` | RECORD_AUDIO, `android.hardware.microphone` con required=false y Activity no exportada. Sin permisos de ubicación nuevos, almacenamiento, Internet ni foreground service. | 1–4, 10, 22, 25 |
| `app/src/main/java/com/example/avesargentinas/MainMenuActivity.kt` | Abrir la sección, preservando captura instantánea. | 1 |
| `app/src/main/res/layout/activity_main_menu.xml` | Añadir acceso junto a identificación fotográfica. | 1 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundActivity.kt` | UI, permiso, navegación y entrega de eventos de ciclo de vida. | 1–8, 12–24 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundViewModel.kt` | Estado, un intento activo, cancelación y coordinación. Estado definido en el mismo archivo, sin contenedor DI. | 3–13, 17, 19–25 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/AudioRecorder.kt` | Captura de duración efectiva, progreso y liberación. | 5–8, 10, 22, 25 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundAudio.kt` | PCM, remuestreo y ventanas; funciones puras comprobables en JVM. | 8, 11 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdNetClassifier.kt` | Carga, contratos ONNX, inferencia y recursos nativos. | 9–14, 17, 24–25 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundCatalog.kt` | Etiquetas, nombres, agrupación de resultados y correspondencias locales verificadas. | 11–18 |
| `app/src/main/java/com/example/avesargentinas/ui/sound/BirdDetection.kt` | Especie, nombres, puntuación y recurso local opcional, independientes de Prediction fotográfica. | 12–19 |
| `app/src/main/res/layout/activity_bird_sound.xml` y `item_bird_sound_result.xml` | Pantalla y fila reutilizable, estados y accesibilidad. | 2, 4–8, 12–20 |
| `app/src/main/res/values/strings.xml` y `app/src/main/res/drawable/ic_mic.xml` | Textos localizables e icono vectorial; dimensiones nuevas solo si no existe un recurso apropiado. | UI y accesibilidad |
| `app/src/main/assets/birdnet/` | Modelo, species.json, manifest.json, licencia y atribuciones del paquete concreto. | 9, 11–15, 25 |
| `tools/prepare_birdnet_assets.py` | Validación reproducible de entradas y generación de metadatos, con autocontrol; sin lógica de ejecución Android. | 11, 14–15 |
| `app/src/test/java/com/example/avesargentinas/ui/sound/` | Señal, selección, catálogo puro y errores de datos. | 8, 11–17 |
| `app/src/androidTest/java/com/example/avesargentinas/ui/sound/` y `app/src/androidTest/assets/birdnet/` | Estado, ciclo de vida, interfaz, captura e inferencia reales y fixtures solo de prueba. | 1–25 |
| `specs/003-identificacion-aves-sonido/evaluacion-audio.md` | Evidencia, corpus con procedencia, umbral final y medidas; se creará al validar, no con resultados supuestos. | Finalización 1–8 |

## Datos, contratos y compatibilidad

- `CapturedAudio(samples: ShortArray, sampleRate: Int)` es temporal y pertenece al intento; no sale de la capa de sonido. `AudioRecorder.record(onProgress)` suspende hasta captura completa y admite cancelación; no entrega audio parcial como éxito.
- `BirdSoundAudio.toModelSamples(audio)` produce exactamente 320.000 Float y `windowStarts` determina 0, 96.000, 192.000 y 224.000. Se documentará y probará el contrato antes de conectar UI.
- `BirdNetClassifier.initialize()` carga y comprueba recursos; `classify(samples)` devuelve 9.789 puntuaciones agregadas por índice tras las cuatro ventanas; nunca retorna ejemplos de fallback. `cancelCurrentRun()` no cierra concurrentemente la sesión y `close()` espera limpieza fuera de Main.
- `BirdSoundCatalog.select(scores, minimumScore = 0.35f, limit = 5)` produce `List<BirdDetection>` con identidad científica canónica; `BirdDetection` conserva separadas la clave del modelo y la clave de ficha. El umbral final quedará fijado tras calibración y no será un ajuste visible al usuario.
- El ViewModel recibirá operaciones mediante parámetros de constructor internos, como el patrón de pruebas del juego; evitar jerarquías de repositorios y wrappers genéricos. Las funciones puras no dependerán de Context; la carga de recursos sí.
- No habrá cambios en Room, ObservationLogger, exportación, etiquetas/modelo fotográficos ni contratos de las Activities existentes. Los recursos de prueba nunca formarán parte del APK de producción.
- La aprobación del plan no certifica Android 24/25 ni páginas de 16 KB: son comprobaciones obligatorias de compatibilidad. No se habilitará R8 ni se añadirán reglas de minimización mientras siga deshabilitado en el proyecto.

## Errores y casos límite

| Condición | Tratamiento |
| --- | --- |
| Denegación de permiso | Guardar si hubo solicitud previa, combinar con `shouldShowRequestPermissionRationale` y no confundir primera solicitud con denegación permanente; acción Ajustes cuando corresponda. Esa preferencia no contiene audio. |
| Permiso respondido tras salir o en segundo plano | No iniciar captura; invalidar intención pendiente y permitir inicio explícito al volver. |
| Silencio constante | Omitir ventanas constantes para evitar NaN conocidos; si no queda ninguna, sin detecciones. |
| Ruido/voces con alta puntuación | Aplicar umbral validado y explicar límites; no afirmar que el modelo distingue siempre ruido o presencia de aves. |
| Modelo, etiquetas o puntuaciones inválidos | Error explícito, sin mostrar resultados parciales o con índices desplazados. |
| Nombres o mapas faltantes | Científico legible y aviso; mantener la detección y el reintento. |
| Cancelación/rotación/cierre durante llamada nativa | Token invalidado, cierre ordenado, sin use-after-close ni resultados tardíos. |
| Instalación nueva y modo avión | Todos los assets incluidos; cero dependencia de una descarga de primer uso. |

## Estrategia de validación

### Señal, contratos y estado

- JVM: conversión de extremos PCM, frecuencias alternativas con autocontroles de amplitud/antialias, duración exacta y final de la cuarta ventana; selección por máximo sin sumar ni softmax; umbral incluido con `>=`, empates, más/menos de cinco, no aves, duplicados, NaN, infinito y nombres/alias ambiguos.
- Datos: autocontrol de preparación, huellas y tamaño del modelo, 9.789 entradas y posiciones válidas; lectura del paquete instalado. Comprobar en el modelo real que `predictions` y las etiquetas se corresponden. Para tres ventanas no constantes de fixtures autorizados, guardar puntuaciones de referencia obtenidas con el mismo archivo en el runtime oficial de desarrollo y comparar Android con error absoluto máximo de 0,001 por clase. La referencia no será otro modelo ni otra normalización; un desacuerdo exigirá investigar el contrato, sin ampliar la tolerancia para ocultarlo. No se requiere un paquete Python en la app.
- Estado instrumentado: constructor interno con operaciones controladas para reproducir cancelaciones, doble inicio, permiso tardío, respuestas antiguas y limpieza. Los dobles se limitarán a las pruebas de UI/estado y nunca justificarán que la inferencia real funciona.
- Dispositivo: iniciar y liberar micrófono, diez segundos efectivos, cancelación, revocación, captura silenciada cuando sea observable, watchdog y alternativas de frecuencia. Probar Grabar → Home → volver, rotar durante captura/análisis y salir durante inferencia. Exigir micrófono liberado en menos de un segundo tras cancelar o ir a segundo plano; medir desde evento hasta release.
- UI: navegación con y sin captura instantánea, error frente a sin detecciones, nombres largos, mapa ausente o ilegible, doble toque de ficha, retorno del zoom y misma lista. Matriz claro/oscuro × vertical/horizontal × texto normal/200 %, pantalla pequeña y recorrido manual con TalkBack. Muerte real del proceso, no solo `ActivityScenario.recreate()`.

### Evaluación acústica y rendimiento propuestos

Estas cifras son condiciones propuestas de aceptación del plan, no resultados obtenidos ni promesas de exactitud general.

- Corpus inicial: **30 fragmentos positivos de 10 segundos**, tres grabaciones independientes de cada una de diez aves argentinas presentes en el modelo. Especies objetivo: Furnarius rufus, Pitangus sulphuratus, Turdus rufiventris, Zonotrichia capensis, Vanellus chilensis, Zenaida auriculata, Myiopsitta monachus, Troglodytes aedon (o su correspondencia taxonómica oficial), Mimus saturninus y Guira guira. Si una falta o cambió de identidad, documentar la correspondencia y acordar una sustitución antes de fijar el corpus, sin ocultarla del denominador después de evaluar.
- Fuente: grabaciones propias identificadas o fuentes públicas con atribución y licencia compatibles, por ejemplo Xeno-canto. Registrar identificador/URL, autor, licencia, localización argentina, especie verificada, segundos recortados, preparación y hash. No redistribuir un audio sin permiso/licencia; los permisos de fuentes de prueba no se deducen de la licencia del modelo. Dos fragmentos del mismo original no cuentan como grabaciones independientes.
- Separación previa: una grabación por especie para calibración (**10**) y dos reservadas (**20**) para evaluación. Controles adicionales: cuatro silencios constantes generados, cuatro grabaciones de viento/ambiente sin aves y cuatro de voz consentida sin aves; dos controles reales de cada tipo para calibración y los otros cuatro reales más los cuatro silencios para evaluación. No se requieren voces de terceros ni conversaciones privadas.
- El manifiesto del corpus se congelará antes de ejecutar la evaluación; la anotación de presencia/ausencia la verificará una persona por escucha. Mantener la separación por grabación original y registrar todos los resultados, sin reemplazar casos difíciles tras ver las predicciones.
- Calibrar entre 0,35, 0,50, 0,65 y 0,80, eligiendo el menor umbral sin detecciones aviares en los cuatro controles reales de calibración y con al menos 8/10 especies objetivo presentes en top cinco. Si ninguno cumple, registrar el fallo y revisar el plan; no ajustar contra el conjunto reservado.
- Evaluación reservada: especie objetivo presente en los resultados de **al menos 16/20** positivos; como máximo **un control real de cuatro** con alguna detección aviar; cero detecciones en los cuatro silencios constantes. Registrar además top uno y falsos candidatos, sin presentarlos como métricas aprobadas de precisión global. El corpus pequeño es una comprobación inicial de utilidad; si falla, no se declara finalizada la función.
- Samsung A56 del usuario: **preparación fría ≤ 5 s**, **análisis completo de 10 s ≤ 5 s en al menos 19 de 20 ejecuciones**, y **PSS total del proceso ≤ 512 MiB** en el pico observado al preparar, capturar y analizar. Preparación y análisis se medirán por separado con reloj monotónico; análisis incluye remuestreo, cuatro inferencias y selección. Ensayo sin depurador conectado, sin cargar a la vez el modelo fotográfico ni inferencias paralelas; medir también una visita fotografía → sonido para detectar recursos retenidos de la pantalla anterior.
- Registrar los veinte tiempos, distribución y máximo, PSS por muestreo y evidencia del profiler nativo al menos en una ejecución. Repetir cinco ciclos abrir/grabar/analizar/salir: sin crecimiento retenido sostenido de recursos nativos, sin crashes, micrófono liberado y sesión cerrada. No se deduce RAM de ejecución a partir de los 65 MB de pesos.
- Android 24/25: comprobar instalación y carga/inferencia real del modelo en dispositivo o emulador compatible, además de UI/permisos; el emulador no sustituye la captura ni el rendimiento físico. En API reciente con páginas de 16 KB, revisar la alineación del APK/AAR y ejecutar la instalación y carga. Las dependencias nativas antiguas de TFLite se comprobarán también, sin atribuir a ONNX una conformidad de toda la app que no se haya medido.

### Comandos y evidencia

Desde la raíz, con el Wrapper y el JDK/SDK adecuados, ejecutar por separado `./gradlew.bat :app:assembleDebug`, `./gradlew.bat :app:testDebugUnitTest`, `./gradlew.bat :app:lintDebug` y, con dispositivo seleccionado, `./gradlew.bat :app:connectedDebugAndroidTest`. En PowerShell se usará el prefijo `.\gradlew.bat`. Las repeticiones limitadas identificarán la clase ejecutada y no se contabilizarán como suite completa.

La línea base inicial distinguirá la configuración actual de los reportes históricos. Un error de compilación o Lint ajeno se registrará y requerirá resolver su alcance antes de la aprobación final; no se silenciará con baseline ni se marcará verde por ejecutar una copia con otras dependencias. Las comprobaciones no realizadas quedarán pendientes.

Los logs y capturas generados irán bajo `app/build/birdnet-verification/`, ignorados por Git. La evidencia resumida y el manifiesto con procedencia se documentarán en `evaluacion-audio.md`. Los únicos audios versionados serán fixtures autorizados de pruebas, sin capturas de usuarios. No se registrarán PCM, audio codificado ni listas de resultados de sesiones reales en logs de producción.

## Riesgos y decisiones pendientes

- La dependencia y el archivo exactos son candidatos fijados para una comprobación inicial de viabilidad. Aún no se descargó el binario ni se ejecutó ONNX; no se afirma compatibilidad solo porque BirdNET Live lo use mediante otra integración.
- El acceso de red del shell no resolvió GitHub durante esta planificación. La documentación pudo consultarse por navegador de investigación; adquirir los binarios, fijar el commit y computar hashes de metadatos siguen siendo comprobaciones de implementación. No se generaron archivos de modelo de reemplazo.
- La disponibilidad, licencia y esquema real de la fuente de nombres españoles se validarán al preparar los recursos; los nombres regionales existentes y el fallback científico evitan bloquear resultados por traducciones ausentes.
- Siguen siendo materiales los falsos positivos de la preview, especialmente voces y viento. No se incorporará una promesa de detección de voz o ruido que no exista en el modelo.
- La preparación de fuentes acústicas autorizadas y su verificación humana son necesarias antes de cerrar la evaluación. Su ausencia mantiene la validación pendiente; no se reemplazarán con predicciones simuladas.
- Los presupuestos, corpus y criterio de confianza aquí propuestos se aprueban con el plan. El modelo exacto del Samsung y su Android/RAM se registrarán al conectarlo; no hace falta adivinarlos ni cambiar el alcance para redactar las tareas.
- No hay decisiones funcionales adicionales pendientes. Si una comprobación exige cambiar el modelo, el runtime fijo, la compatibilidad o los criterios aprobados, se traerá el resultado concreto para revisar el plan antes de continuar.

### Fuentes primarias consultadas el 26 de octubre de 2026

- [Configuración del modelo móvil](https://github.com/birdnet-team/birdnet-live-app/blob/main/assets/models/model_config.json), [etiquetas](https://github.com/birdnet-team/birdnet-live-app/blob/main/assets/models/BirdNET%2B_V3.0-preview3.1_Global_10K-pruned_Labels.csv) y [puntero LFS con huella y tamaño](https://github.com/birdnet-team/birdnet-live-app/blob/main/assets/models/BirdNET%2B_V3.0-preview3.1_Global_10K-pruned_FP16.onnx).
- [Contrato de la inferencia oficial](https://github.com/birdnet-team/birdnet-live-app/blob/main/lib/features/inference/classifier_model.dart), [modelos y caso de varianza nula](https://birdnet-team.github.io/birdnet/models.html) y [licencia de pesos incluidos](https://github.com/birdnet-team/birdnet-live-app/blob/main/MODEL_LICENSE).
- [Incidente y fijación de ONNX en BirdNET Live](https://github.com/birdnet-team/birdnet-live-app/blob/main/docs/developer/onnxruntime-pin.md), [instalación Android de ONNX](https://onnxruntime.ai/docs/install/), [API RunOptions](https://onnxruntime.ai/docs/api/java/ai/onnxruntime/OrtSession.RunOptions.html), [ByteBuffer en Java 1.22.0](https://github.com/microsoft/onnxruntime/blob/v1.22.0/java/src/main/java/ai/onnxruntime/OrtEnvironment.java) y [artefacto 1.22.0](https://repo.maven.apache.org/maven2/com/microsoft/onnxruntime/onnxruntime-android/1.22.0/).
- [AudioRecord](https://developer.android.com/reference/android/media/AudioRecord) y [fuente oficial de metadatos de especies](https://birdnet-team.github.io/birdnet-live-app/developer/species-bundle/).
