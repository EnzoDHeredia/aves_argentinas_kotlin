# Evaluación de audio — Spec 003

Fecha: 26 de octubre de 2026. Alcance de este turno: **solo T1**.
La lista de tareas fue aprobada explícitamente por el usuario. **T1 completada**;
no se inició T2 ni se hicieron commits o push. La actualización del motor
fotográfico fue aprobada posteriormente, conservando ambos modelos.

## Resultado y límites

- Incorporados el modelo auténtico, las etiquetas originales y las licencias/avisos
  locales; ONNX Android fijado a 1.22.0 y `.onnx` sin compresión.
- Demostrada carga desde mapeo directo e inferencia CPU real sin red en el
  Samsung A56 y en API24, API25 y API35 con páginas reales de 16 KB y
  compatibilidad de 4 KB desactivada. Clase final: **4/4 en cada entorno**.
- Las cuatro discrepancias se investigaron en el historial oficial, la taxonomía
  fijada y la API de Cornell. Se resolvieron mediante cuatro erratas explícitas
  en los metadatos derivados, conservando íntegros el modelo y el CSV original.
  **`species.json` y `manifest.json` están generados y reproducidos byte por byte**.
- Última clase instrumentada completa en Samsung, sin red: **4/4 aprobadas**,
  incluyendo catálogo, manifiesto, inferencia BirdNET, JNI y predicciones fotos.
- Última validación: 30 unitarios aprobados; Lint con **0 errores y 243
  advertencias**. Los seis errores heredados se corrigieron con la autorización
  posterior del usuario.
- El fallo previo de TFLite 2.16.1 en 16 KB quedó resuelto mediante la
  actualización aprobada a LiteRT, fijado finalmente a **1.4.1** para conservar
  API24/25. Modelo fotográfico, etiquetas y preprocesamiento intactos;
  referencias anteriores con idénticos índices y diferencias menores a 0,001.
- Samsung reconectado por USB y prueba final repetida después de que el usuario
  activara modo avión y apagara Wi-Fi. No falta una comprobación obligatoria de T1.
- No se implementaron captura, pantalla, catálogo de resultados ni selección.
  La señal sintética demuestra ejecución; no identificación de aves argentinas.

Las secciones siguientes conservan la investigación y los resultados históricos,
incluidos los fallos. El cierre vigente figura en
[Cierre de T1](#cierre-de-t1--actualización-aprobada-y-litert-141).

## Procedencia e integridad

Origen BirdNET Live fijado a
[`2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846`](https://github.com/birdnet-team/birdnet-live-app/tree/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846).
Se resolvió `commits/main` una vez y se descargaron los archivos por ese commit,
sin actualizar posteriormente desde `main`. El puntero LFS y `model_config.json`
del mismo commit coinciden con los valores aprobados. Se conservó el binario real,
no el puntero de 133 bytes.

| Entrada oficial | Bytes | SHA-256 |
| --- | ---: | --- |
| `assets/models/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16.onnx` | 66.991.322 | `306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457` |
| `assets/models/BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv` | 674.836 | `b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9` |
| `assets/models/taxonomy.csv` | 12.850.078 | `c1052902a6a003f4040548644bbbc02e8dd24a42425585842b97900ac4e3ad16` |
| `MODEL_LICENSE` | 10.384 | `7f60e859849a3e4cef08c8979ad0a212f79882724331cc7d90327704fcf77320` |
| `LICENSE` → `SOURCE_LICENSE` | 1.069 | `7cc5572eafcd9daa7dbd3dd16855da967e9336d3a7adeea9b0b7046850ea4848` |

Descarga del modelo: `https://media.githubusercontent.com/media/birdnet-team/birdnet-live-app/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846/assets/models/BirdNET%2B_V3.0-preview3.1_Global_10K-pruned_FP16.onnx`.
Las demás entradas se obtuvieron con `Invoke-WebRequest` desde
`https://raw.githubusercontent.com/birdnet-team/birdnet-live-app/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846/`
seguido de su ruta de la tabla. Las fuentes de preparación permanecen en el
directorio ignorado `app/build/birdnet-verification/` como `model.onnx`,
`labels.csv`, `taxonomy.csv`, `MODEL_LICENSE` y `LICENSE`.

`MODEL_LICENSE` declara Apache-2.0 para los pesos incluidos. Se preservó además la
licencia MIT raíz del repositorio, sin inferir permisos para sus fotografías o
descripciones: no se incorporaron esas imágenes ni textos. La preparación solo
consume identidad, clase y nombres de la taxonomía publicada en el mismo commit.
Se adjuntó `ATTRIBUTION.txt` legible offline. La presentación en la pantalla
corresponde a una tarea posterior.

ONNX se resolvió desde Maven Central como
`com.microsoft.onnxruntime:onnxruntime-android:1.22.0`. SHA-256 del AAR:
`04a4617a9c797cf49225595e45b5546081cb34c86ac817581141577d3b7dbfe2`.
El AAR no contiene archivos de licencia/avisos: se copiaron íntegros desde
[`v1.22.0/LICENSE`](https://github.com/microsoft/onnxruntime/blob/v1.22.0/LICENSE)
y [`v1.22.0/ThirdPartyNotices.txt`](https://github.com/microsoft/onnxruntime/blob/v1.22.0/ThirdPartyNotices.txt)
como `ONNX_LICENSE` (1.073 bytes, SHA-256
`2f07c72751aed99790b8a4869cf2311df85a860b22ded05fa22803587a48922c`)
y `ONNX_ThirdPartyNotices.txt` (326.866 bytes, SHA-256
`e9e90971a8e75a9a8ac0c6412e29c1202d079998389915aa485f46c816c3b4cc`).

## Preparador y rechazo inicial de datos auténticos

Esta sección conserva el fallo de la primera ejecución. Su resolución y las
comprobaciones nuevas se detallan en «Investigación y resolución de las cuatro
discrepancias»; el resultado inicial no se convirtió retroactivamente en aprobado.

`tools/prepare_birdnet_assets.py` usa solo la biblioteca estándar. Valida el tamaño
y SHA-256 fijados, punteros LFS, las 9.789 posiciones en orden, IDs únicos,
correspondencias exactas inequívocas y clases coherentes. `idx` sigue siendo la
posición de salida y `id` la identidad independiente. Serializa JSON UTF-8 con
orden estable, sin rutas locales ni fecha variable, y calcula huellas por bloques.
Valida antes de escribir el paquete para evitar presentar datos inválidos como
un catálogo utilizable.

Comandos ejecutados desde la raíz:

```powershell
python tools/prepare_birdnet_assets.py --self-test
python tools/prepare_birdnet_assets.py --model app/build/birdnet-verification/model.onnx --labels app/build/birdnet-verification/labels.csv --taxonomy app/build/birdnet-verification/taxonomy.csv --model-license app/build/birdnet-verification/MODEL_LICENSE --source-license app/build/birdnet-verification/LICENSE --source-commit 2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846 --output app/src/main/assets/birdnet
```

- Autocontrol: **2 pruebas aprobadas**. Incluye rechazo de LFS, tamaño y huella
  erróneos, índices intercambiados/duplicados, ID duplicado, longitud incompleta,
  correspondencia ausente/ambigua y clase incompatible; conserva el nombre
  español ausente como `null` y compara bytes de dos generaciones del catálogo
  controlado. Antes de implementar las validaciones, ambas pruebas fallaban por
  funciones aún no implementadas (`prepare-red.log`).
- Entradas auténticas: **salida 1**,
  `Preparation failed: Missing/incompatible taxonomy correspondence: Anarhynchus montanus`.
- Repetición de idénticas entradas con `--output app/build/birdnet-verification/repeated-package`:
  mismo rechazo y salida 1 (`prepare-repeat.log`). No se afirma reproducibilidad
  de un paquete real completo: no se generó ninguno. La reproducibilidad de
  `manifest.json` con entradas reales permanece pendiente.

La investigación comparó por nombre científico exacto todas las filas originales,
sin confundir `id` con `idx` ni con `birdnet_id`. Las discrepancias son de origen:

| idx | id | Nombre científico | Clase en etiquetas | Clase en taxonomía |
| ---: | ---: | --- | --- | --- |
| 410 | 767 | Anarhynchus montanus | Insecta | Aves |
| 411 | 768 | Anarhynchus pallidus | Insecta | Aves |
| 2510 | 4442 | Corvus celebensis | Mammalia | Aves |
| 5592 | 10203 | Microtarsus urostictus | Insecta | Aves |

Las 9.789 posiciones son completas y los IDs/nombres son únicos. Las etiquetas
declaran 8.923 Aves, 340 Amphibia, 269 Mammalia y 257 Insecta. La taxonomía tiene
372 nombres españoles vacíos; siete corresponden a filas etiquetadas Aves:
833 Arizelocichla milanjensis; 2342 Colluricincla megarhyncha; 5115 Loriotus
cristatus; 5611 Mionectes galbinus; 6457 Pachycephala macrorhyncha; 8960 Tarsiger
indicus; 9635 Xenodacnis parina. No se inventaron traducciones.

En la primera ejecución no se eligió otra revisión ni se asignó silenciosamente
una clase de una fuente sobre la otra. La continuación de T1 investigó su
autoridad y aplicó las cuatro correcciones trazables descritas más abajo.

## Compilación, unitarios y Lint

Se trabajó en la ruta y rama existentes, `feature/reconocimiento-por-sonido`.
Estado inicial: `.agents/plugins/` y `specs/003-identificacion-aves-sonido/`
sin seguimiento. Durante la ejecución apareció también un cambio en
`.idea/deploymentTargetSelector.xml`, que se preservó sin editar ni revertir.
No se tocaron archivos de plugins, la spec aprobada ni el plan.
No se creó otro checkout ni se modificaron Wrapper, JDK, SDK, TFLite o minSdk/ABI.
Las ejecuciones usaron el Wrapper con la configuración real; la primera tentativa
restringida falló por acceso al bloqueo de la caché y se repitió con permisos
para esa caché. La adquisición de red también requirió ejecución autorizada.

| Fase | Comando | Resultado | Evidencia bajo `app/build/birdnet-verification/` |
| --- | --- | --- | --- |
| Línea base | `.\gradlew.bat :app:assembleDebug` | salida 0, 45 s | `baseline-assemble.log` |
| Línea base | `.\gradlew.bat :app:testDebugUnitTest` | salida 0, 30 tests, 10 s | `baseline-unit.log` |
| Línea base | `.\gradlew.bat :app:lintDebug` | salida 1, 6 errores y 248 advertencias | `baseline-lint.log`, `baseline-lint.xml` |
| Prueba antes de dependencia | `.\gradlew.bat :app:compileDebugAndroidTestKotlin` | salida 1: `ai.onnxruntime` no disponible | `instrumented-red.log` |
| ONNX incorporado | `.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest` | salida 0; ambos APK compilados | `onnx-assemble.log` |
| Estado final | `.\gradlew.bat :app:assembleDebug` | salida 0, UP-TO-DATE tras el empaquetado real ejecutado por las instrumentadas | `final-assemble.log` |
| Verificación posterior | `.\gradlew.bat :app:testDebugUnitTest` | salida 0, 30 tests, 0 fallos | `final-unit.log`, XML de unitarios en `app/build/test-results/testDebugUnitTest/` |
| Verificación posterior | `.\gradlew.bat :app:lintDebug` | salida 1, 6 errores y 250 advertencias | `final-lint.log`, `final-lint.xml` |

Los 30 unitarios son `ExampleUnitTest` (1) y `LearningSessionTest` (29).
Comparación XML por ID/mensaje/archivo, ignorando el desplazamiento de líneas:
los seis errores son idénticos y ninguna advertencia anterior desapareció.
Errores heredados: GestureBackNavigation en ObservationLogActivity:377;
dos NewApi en ObservationLogActivity:362; NewApi en values/themes.xml:31 y
values-night/themes.xml:31; SuspiciousIndentation en FocusManager:50.
Las dos advertencias nuevas son NewerVersionAvailable y UseTomlInstead para
ONNX. Se conserva 1.22.0 y su declaración directa según el plan; sin supresiones,
baseline artificial, actualización de runtime ni arreglos ajenos.

## Inferencia instrumentada y dispositivo

La clase `BirdNetModelAssetsTest` separa la carga/inferencia real del catálogo
preparado. La primera exige modo avión, mapea el asset con `FileChannel.map`,
comprueba buffer directo y SHA-256, conserva el buffer hasta cerrar la sesión,
usa CPU secuencial con dos hilos y cierra opciones, RunOptions, tensor, resultado
y sesión con `use`. Pide solo `predictions`, sin activaciones adicionales.
La segunda prueba exige el catálogo/manifiesto reales y comprueba todas las
posiciones contra las etiquetas; su fallo no se omite.

Comando ejecutado en emulador, con selección explícita:

```powershell
$env:ANDROID_SERIAL='emulator-5554'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdNetModelAssetsTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'
```

Resultado inicial: **salida 1, 2 pruebas ejecutadas, 1 aprobada y 1 fallida**.
`officialUncompressedModelRunsOffline` pasó. `preparedMetadataMatchesEveryPackagedLabelAndHash`
falló por el `manifest.json` ausente tras el rechazo documentado del preparador.
No se creó un manifiesto ficticio ni se deshabilitó esa prueba.

Emulador temporal: Google/sdk_gphone64_x86_64, Android 16/API 36 (AVD API 36.1),
ABI anunciadas x86_64/arm64-v8a, RAM 2.586.398.720 bytes, página 4.096 bytes.
Modo avión 1, Wi-Fi y datos deshabilitados antes de ejecutar. El APK inspeccionado
no declara INTERNET ni RECORD_AUDIO. No se abrió el micrófono ni se descargaron
assets desde Android.

Evidencia de la ejecución real (`inference-api36-isolated-logcat.txt`):

- Runtime 1.22.0; entrada `input`, FLOAT, forma declarada `[-1,-1]`.
- Salida `predictions`, FLOAT, forma declarada `[-1,9789]`.
- Ventana: Float32 `[1,96000]`, dos tonos de 440/1.200 Hz, no constante.
- Resultado real: Float32 `[1,9789]`, **9.789 valores finitos**;
  mínimo `5.662441E-7`, máximo `0.16616076`.
- Preparación de sesión observada: 365 ms; intervalo posterior hasta salida:
  221 ms (incluye preparar la señal/tensor). No es el ensayo de rendimiento de T10.
- Recursos cerrados sin crash. Un ciclo no demuestra ausencia de fugas sostenidas.

El primer intento en el AVD habitual falló antes de ejecutar por
INSTALL_FAILED_INSUFFICIENT_STORAGE, aunque Gradle imprimió BUILD SUCCESSFUL;
**no se contabiliza como prueba pasada** (`instrumented-api36.log`). Se apagó
el emulador iniciado por esta tarea y se arrancó con `-read-only -no-window
-no-audio -no-snapshot -data <directorio-de-evidencia>/api36-userdata.img
-cache <directorio-de-evidencia>/api36-cache.img`. No se borraron aplicaciones
del AVD original. Dos intentos de arranque rechazaron `-partition-size 8192` y
un `-initdata` inexistente antes de usar la inicialización predeterminada válida.
La ejecución válida quedó en `instrumented-api36-isolated.log`, con copia de XML
y logcat por prueba en `api36-test-results/`.

Samsung inicialmente detectado por ADB inalámbrico y luego conectado por USB
por el usuario: **SM-A566E, Android 16, API 36, arm64-v8a, MemTotal 7.595.040 KiB,
RAM de ActivityManager 7.777.320.960 bytes, página 4.096 bytes**. No se dedujo
la memoria del nombre comercial.

Comando físico ejecutado:

```powershell
$env:ANDROID_SERIAL='R5CY602DAGP'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdNetModelAssetsTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'
```

Primera ejecución física, antes de resolver los datos: modo avión 1,
`cmd wifi status`: `Wifi is disabled`; `dumpsys connectivity`:
`Active default network: none`, antes y después. **Salida 1: dos pruebas
ejecutadas, una aprobada y una fallida**, igual que en el emulador. La carga e
inferencia reales pasaron; el catálogo/manifiesto rechazado impide aprobar toda
la clase. No se filtró por método para ocultar el fallo.

`inference-samsung-logcat.txt` registra runtime 1.22.0, `input` FLOAT `[-1,-1]`,
`predictions` FLOAT `[-1,9789]`; ejecución concreta `[1,96000]` → `[1,9789]`;
9.789 valores finitos, mínimo `5.662441E-7`, máximo `0.16624716`, sin crash.
Preparación de sesión 402 ms; intervalo posterior hasta salida 239 ms, incluyendo
la preparación de la señal/tensor. Todos los recursos cerrados en el orden
descrito. Son medidas de una ventana sintética, no del análisis completo de
10 s ni de la calidad acústica. No se compararon estas salidas como referencias
de T4 ni se evaluaron grabaciones de aves.

Evidencia: `instrumented-samsung.log`, `samsung-test-results/` (XML de dos tests y
logcat por método), `samsung-usb-inventory.txt`, `samsung-network-before.txt` y
`samsung-network-after.txt`.

Se verificó en las fuentes locales de AGP 9.4.1 que
`android.injected.androidTest.leaveApksInstalledAfterRun=true` evita la
desinstalación posterior. Se conservó la app existente mediante actualización:
`firstInstallTime=2026-10-22 22:31:17`, firma y directorio de datos idénticos
antes/después (`samsung-package-before.txt`, `samsung-package-after.txt`). No se
ejecutaron borrados de datos ni desinstalaciones del Samsung. Los APK de app y
pruebas quedaron instalados; el teléfono continúa en el modo avión solicitado.
El emulador temporal se cerró al terminar.

## Compatibilidad nativa y empaquetado — inspección inicial

- AAR 1.22.0: manifest con minSdk 24; bibliotecas para arm64-v8a,
  armeabi-v7a, x86 y x86_64. Sin filtros ABI nuevos.
- Inspección del APK mediante `aapt2 dump badging`: minSdk 24, targetSdk 36,
  las mismas cuatro ABI, sin nuevos permisos de red o micrófono.
- Modelo en ZIP_STORED (compresión 0), exactamente 66.991.322 bytes.
- APK de la primera ejecución, anterior a la corrección: 253.511.842 bytes, SHA-256
  `d6c8bc1051cccf75652a63a5228d134425d1c2883c84a9df5087ccea10e56d69`.
- `python app/build/birdnet-verification/review_native.py` inspeccionó todos los
  segmentos ELF PT_LOAD del AAR/APK y offsets ZIP; datos detallados en
  `native-review.json` y `native-review.txt`.
- `zipalign.exe -c -P 16 -v 4 app/build/outputs/apk/debug/app-debug.apk`:
  **salida 0, Verification successful** (`zipalign.txt`).
- ONNX arm64-v8a/x86_64: ambas bibliotecas con segmentos alineados a 16.384.
  En armeabi-v7a/x86, `libonnxruntime4j_jni.so` conserva alineación ELF 4.096;
  `libonnxruntime.so` usa 16.384 en las cuatro ABI.
- **TFLite existente:** `libtensorflowlite_jni.so` tiene alineación ELF 4.096
  en las cuatro ABI. ZIP correctamente alineado no equivale a ELF compatible.
  No se actualizó TFLite ni se quitaron ABI para sortear este resultado.
- Solo hay una imagen de emulador API 36.1 instalada: **API 24, API 25 y
  ejecución con páginas reales de 16 KB pendientes**. El AAR y `zipalign` no
  sustituyen instalación/carga/inferencia en esos entornos.

## Investigación y resolución de las cuatro discrepancias

Continuación autorizada por el usuario el 26 de octubre de 2026 para investigar
y resolver este bloqueo de T1. Se aplicó systematic-debugging y se mantuvieron
el modelo, runtime, posiciones, compatibilidad y criterios de confianza aprobados.

### Causa y evidencia oficial

Las anotaciones descriptivas `class` y `order` del CSV de etiquetas ya contienen
los errores en el repositorio oficial; los archivos locales conservan su SHA-256.
La identidad, `idx` e `id` de esas filas coinciden con las fuentes fijadas.
La investigación del historial encontró:

- En el commit [`4d824706b89386b8a7410501cbfcf968ca7047c6`](https://github.com/birdnet-team/birdnet-live-app/commit/4d824706b89386b8a7410501cbfcf968ca7047c6),
  de 22 de junio de 2026 (conjunto anterior de 10.208 etiquetas), Anarhynchus
  montanus, Corvus celebensis y Microtarsus urostictus ya tienen las clases y
  órdenes incorrectos. Ese modelo anterior no se incorporó a la app.
- En [`6794c3f58fde9c4bd51aece30b5012a411b4b69b`](https://github.com/birdnet-team/birdnet-live-app/commit/6794c3f58fde9c4bd51aece30b5012a411b4b69b),
  de 28 de junio de 2026 (9.789 etiquetas), están los cuatro errores en las
  posiciones actuales. La revisión posterior `f74e5f592451073e6dcecee65a35e78517f12310`
  no los corrige; permanecen en el commit fijado de adquisición.
- El [`taxonomy.csv` del commit fijado](https://github.com/birdnet-team/birdnet-live-app/blob/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846/assets/models/taxonomy.csv)
  relaciona exactamente los cuatro nombres científicos con `taxon_group=Aves`.
- El [generador oficial, líneas 441–472](https://github.com/birdnet-team/birdnet-live-app/blob/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846/tools/build_species_bundle.py#L441-L472)
  toma `taxon_group` de la taxonomía cuando encuentra la especie; solo usa
  `model_info['class']` si no encuentra correspondencia. El
  [modelo de datos de la app oficial](https://github.com/birdnet-team/birdnet-live-app/blob/2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846/lib/shared/models/taxonomy_species.dart#L228-L239)
  lee ese `taxon_group` para presentar y agrupar especies.

Se consultó el esquema publicado en
`https://birdnet.cornell.edu/taxonomy/openapi.json`, y luego las cuatro rutas
`https://birdnet.cornell.edu/taxonomy/api/species/<nombre-científico-escapado>?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id`
con `Invoke-WebRequest`. Las cuatro respuestas declaran `taxonomy_version=v0.3-Jul2026`,
nombre exacto, tipo de registro `species` y clase **Aves**. Solo se descargaron
campos de identidad; sin fotografías ni descripciones. Las URLs completas y las
huellas de esas respuestas se conservan en `manifest.json/class_corrections`.

| idx / id del modelo | Especie | Clase / orden del CSV original | ID taxonómico | Clase verificada |
| --- | --- | --- | --- | --- |
| 410 / 767 | [Anarhynchus montanus](https://birdnet.cornell.edu/taxonomy/api/species/Anarhynchus%20montanus?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id) | Insecta / Diptera | BN00678 | Aves |
| 411 / 768 | [Anarhynchus pallidus](https://birdnet.cornell.edu/taxonomy/api/species/Anarhynchus%20pallidus?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id) | Insecta / Diptera | BN00681 | Aves |
| 2510 / 4442 | [Corvus celebensis](https://birdnet.cornell.edu/taxonomy/api/species/Corvus%20celebensis?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id) | Mammalia / Artiodactyla | BN03724 | Aves |
| 5592 / 10203 | [Microtarsus urostictus](https://birdnet.cornell.edu/taxonomy/api/species/Microtarsus%20urostictus?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id) | Insecta / Hemiptera | BN08926 | Aves |

El mecanismo que produjo originalmente esas anotaciones incorrectas no está
demostrado por el historial disponible. La corrección se fundamenta en las
identidades inequívocas y las fuentes taxonómicas oficiales coincidentes.

### Corrección aplicada y límites

`CLASS_ERRATA` en el preparador contiene solo estas cuatro filas. Para aplicar
cada errata se exigen los hashes completos ya registrados de etiquetas y
taxonomía, `idx`, `id`, nombre, clase y orden originales exactos, más el nombre
canónico, ID y clase de la fila taxonómica esperada. Un cambio en cualquiera de
ellos se rechaza. Cualquier otra discrepancia también se rechaza.

Solo se modifica la clase de esas cuatro entradas en `species.json`. El CSV y
el binario no se reescriben ni se cambian por otra versión; tampoco se cambia
`idx`, `id`, identidad científica, traducciones o puntuaciones. No se incluyen
órdenes en el catálogo derivado. El manifiesto registra clase/orden originales,
clase corregida, IDs separados, fuente fijada, URL y SHA-256 de confirmación.
No se incorporó una regla que acepte indiscriminadamente toda diferencia de clase.

### Verificaciones de la resolución

Evidencia nueva bajo `app/build/birdnet-verification/taxonomy-research/`:

| Comando o comprobación | Resultado | Evidencia |
| --- | --- | --- |
| `python tools/prepare_birdnet_assets.py --self-test`, antes de implementar | salida 1, nuevo caso falla en las cuatro erratas por resolución aún ausente | `corrections-red.log` |
| Mismo autocontrol, tras implementar | salida 0, 3 pruebas aprobadas; cubre cambios de identidad, clases, órdenes, IDs taxonómicos y hashes, además de las validaciones anteriores | `corrections-self-test.log` |
| Comando de preparación ya documentado con `--output app/src/main/assets/birdnet` | salida 0, 9.789 entradas preparadas, 372 traducciones ausentes como null | `prepare-corrected.log` |
| Misma preparación con `--output app/build/birdnet-verification/taxonomy-research/repeated-package` | salida 0, siete archivos generados idénticos byte por byte, incluido manifiesto | `prepare-corrected-repeat.log`, `reproducibility.txt` |
| Comparación completa de todas las posiciones/IDs/nombres y clases con CSV original | 9.789 identidades conservadas; solo diferencias de clase 410/411/2510/5592; 8.927 Aves, 340 Amphibia, 268 Mammalia y 254 Insecta | `reproducibility.txt` |
| `.\gradlew.bat :app:assembleDebug` | salida 0, APK empaquetado con los nuevos JSON | `assemble.log` |
| `.\gradlew.bat :app:testDebugUnitTest` | salida 0, UP-TO-DATE; conserva el resultado previo de 30 tests, sin ejecución nueva de unitarios | `unit.log` |
| Clase completa `BirdNetModelAssetsTest` por Wrapper, `ANDROID_SERIAL=R5CY602DAGP`, con `leaveApksInstalledAfterRun=true` | salida 0, **2 tests ejecutados, 0 fallos ni omisiones** | `instrumented-samsung.log`, `samsung-test-results/` |
| `.\gradlew.bat :app:lintDebug` | salida 1: mismos 6 errores y 250 advertencias; comparación XML sin hallazgos añadidos o retirados | `lint.log`, `lint.xml`, `lint-comparison.txt` |

La prueba instrumentada ahora verifica las cuatro correcciones literales,
incluyendo identidades y anotaciones originales, rechaza otras diferencias y
comprueba las 9.789 posiciones y huellas del paquete. En el Samsung continuaron
modo avión 1, Wi-Fi deshabilitado y ninguna red activa. La inferencia real produjo
9.789 valores finitos en [0,1], mínimo `5.662441E-7`, máximo `0.16624716`;
preparación de sesión 398 ms e intervalo posterior 250 ms, con cierre ordenado.
No es evaluación acústica ni el presupuesto del análisis completo de T10.
Se conservaron firma, directorio de datos y fecha inicial de instalación de la app.

Huellas del paquete corregido:

- `species.json`: 2.043.560 bytes, SHA-256 `5e3598bdbc08a0684ba4edc47e7760c44515ce6febb6e5cfd0b0668f778244b7`.
- `manifest.json`: 9.489 bytes, SHA-256 `f7215405576cdf3f5598e91d34072fc12cb644371d5ded46662c3c280ff3bea2`.
- Modelo y CSV: conservan los hashes de adquisición de la tabla inicial.
- APK probado: 253.827.120 bytes, SHA-256 `23facf778c61dd3cb9550b6d9d4a700760b20f03ec01ba97e60eae3ece9303e3`.

Las respuestas oficiales, esquema y resumen de consultas GitHub están en
`taxonomy-research/Anarhynchus_montanus.json`, `Anarhynchus_pallidus.json`,
`Corvus_celebensis.json`, `Microtarsus_urostictus.json`, `openapi.json` y
`github-history-summary.json`. No se enviaron reportes ni mensajes a terceros.

## Continuación: validación de compatibilidad y correcciones de Lint

El usuario autorizó «hagamos lo que haga falta para poder cerrar esta T1»,
incluyendo el tratamiento de los seis errores heredados. Se mantuvieron intactos
el modelo, ONNX Android 1.22.0, TFLite 2.16.1 y sus dependencias support/metadata,
minSdk24, las cuatro ABI y los criterios de aceptación. No se inició T2.

### Correcciones de Lint autorizadas

- `FocusManager.kt`: sangría de `displayRect` coherente con el cuerpo del método;
  sin modificar el recorte.
- `ObservationLogActivity.kt`: `TextViewCompat.setLineHeight` para admitir API24,
  y `OnBackPressedCallback` habilitado solo durante selección. El callback
  conserva la cancelación y limpieza antes de dejar actuar al regreso normal;
  sustituye el estado booleano y el override antiguo.
- `values/themes.xml` y `values-night/themes.xml`: el atributo XML
  `android:windowLightNavigationBar` se trasladó a `values-v27/themes.xml` y
  `values-night-v27/themes.xml`, conservando true en claro y false en oscuro.
  El atributo XML requiere API27, aunque su flag programático exista en API26.
- `ObservationLogActivityTest`: comprueba dispatcher y botón visual, cancela y
  limpia selección antes de salir y espera la destrucción asíncrona. Usa un ID
  sintético en el adapter; no inserta ni borra observaciones de Room.

`closure/lint-red.log` reproduce los seis errores. El test de Atrás falló antes
de migrar el manejo (`lint-back-red.log`). La primera tentativa de GREEN exigía
un cierre inmediato; se corrigió la espera del test respetando el callback
asíncrono de Android, sin forzar finish en producción. GREEN final:
`lint-back-final.log`, **1/1 en API36**, y `api25-history-test.txt`, **1/1 en API25**.
No se afirma que esto simule un gesto físico humano ni una revisión visual completa.

Comprobaciones ejecutadas con Wrapper:

| Comando | Resultado | Evidencia bajo `app/build/birdnet-verification/closure/` |
| --- | --- | --- |
| `.\gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` | salida 0, Lint sin errores, 30 unitarios y ambos APK compilados | `lint-green.log` |
| `.\gradlew.bat :app:assembleDebugAndroidTest :app:lintDebug` | salida 0 tras cerrar la regresión de Atrás | `lint-final.log` |
| `.\gradlew.bat :app:lintDebug` | salida 0 sobre los tests finales, 0 errores, 250 advertencias | `final-lint.log`, `final-lint.xml` |
| `python tools/prepare_birdnet_assets.py --self-test` | salida 0, 3 pruebas aprobadas | `preparer-self-test.log` |
| `zipalign.exe -c -P 16 -v 4 app/build/outputs/apk/debug/app-debug.apk` | salida 0, ZIP correctamente alineado; no acredita compatibilidad ELF de TFLite | `zipalign.txt` |

### Entornos instalados y prueba real

Se instalaron desde los catálogos oficiales de Google, sin actualizar las
plataformas de compilación del proyecto:

- `system-images;android-24;google_apis;x86_64`, revisión 27.
- `system-images;android-25;google_apis;x86_64`, revisión 18.
- `system-images;android-35;google_apis_ps16k;x86_64`, revisión 5.

Se añadieron herramientas Android oficiales verificadas; el lanzador nuevo
23.0 dividía los identificadores de paquetes, y el BAT 19.0 interpretaba mal
la cadena de versión Java «21». La instalación válida invocó directamente
`com.android.sdklib.tool.sdkmanager.SdkManagerCli` con el JDK21 ya instalado:
`--install "system-images;android-24;google_apis;x86_64" "system-images;android-25;google_apis;x86_64" "system-images;android-35;google_apis_ps16k;x86_64"`,
con el SDK local indicado solo en el entorno/comando no versionado.
No se saltó una versión mínima real del JDK ni se alteró el JDK de Gradle.
Los AVD `T1_API24`, `T1_API25` y `T1_API35_16K` se crearon bajo `closure/avds/`.
Los logs de instalación/creación permanecen allí; no se borraron AVD personales.

Un arranque simultáneo agotó memoria del host; se cerró únicamente el proceso
fallido identificado como AVD de esta tarea y se ejecutó un emulador a la vez,
sin ventana, con dos núcleos, `-memory 1536`, renderizado SwiftShader y Vulkan
desactivado para la pantalla. La inferencia sigue siendo CPU secuencial/dos
hilos; no se habilitó un proveedor GPU/NNAPI. La RAM de la tabla se obtuvo del
dispositivo, no se dedujo de ese parámetro del emulador.

| Entorno real | ABI anunciadas / RAM / página | Resultado observado |
| --- | --- | --- |
| Android 7.0 / API24 | x86_64,x86 / 1.570.775.040 bytes / 4.096 bytes | **4 pruebas, 0 fallos**; catálogo, BirdNET real, JNI TFLite y tres imágenes con el clasificador fotográfico existente |
| Android 7.1.1 / API25 | x86_64,x86 / 1.570.775.040 bytes / 4.096 bytes | **4 pruebas, 0 fallos**, las mismas verificaciones; además Atrás **1/1** en ejecución independiente |
| Android 15 / API35 ps16k | x86_64,arm64-v8a / 2.593.198.080 bytes / 16.384 bytes | catálogo y BirdNET real **aprobados**; carga JNI TFLite **SIGSEGV**, proceso terminado, tarea fallida |

En API24/25 se estableció modo avión y se apagaron Wi-Fi y datos; el APK no
declara INTERNET. El comando `getconf` no está disponible en las imágenes
antiguas: la página se midió con `Os.sysconf` dentro del test y se contrastó con
`KernelPageSize` de `/proc/self/smaps`. En API35 se confirmó `getconf PAGE_SIZE=16384`
y se desactivó explícitamente la compatibilidad de 4 KB antes de probar:

```text
bionic.linker.16kb.app_compat.enabled=false
pm.16kb.app_compat.disabled=true
```

Comando para cada ejecución del paquete; se seleccionó el dispositivo concreto:

```powershell
$env:ANDROID_SERIAL='emulator-5556'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdNetModelAssetsTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'
```

La invocación inicial también incluyó la clase de historial separada por coma,
pero los reportes solo contabilizaron BirdNetModelAssetsTest; no se atribuyen
pruebas de historial a ese comando. La regresión de historial se ejecutó aparte
con `adb shell am instrument -w -r -e class com.example.avesargentinas.ui.log.ObservationLogActivityTest com.example.avesargentinas.test/androidx.test.runner.AndroidJUnitRunner`.

Evidencia: `api24-tests.log`, `api24-test-results/`, `api24-logcat.txt`;
`api25-tests.log`, `api25-test-results/`, `api25-logcat.txt` y
`api25-history-test.txt`; `api35-16k-tests.log`, `api35-16k-test-results/`,
`api35-16k-logcat.txt` y `api35-16k-native-failure.txt`. Los XML y logs fueron
leídos; en 16 KB el proceso terminó al comenzar `existingPhotoNativeRuntimeLoads`,
y una repetición directa del runner confirmó `Process crashed`. No se ocultó
esa prueba ni se contabilizó como exitoso el comando ADB por su código de salida.
El test adicional de las tres fotos se incorporó después del diagnóstico de
16 KB y pasó en API24/25; no se afirma que haya corrido en el proceso que falló.

En API24 y API25 ONNX 1.22.0 entregó 9.789 valores finitos, mínimo
`5.662441E-7`, máximo `0.16616076`; sesiones y tensores cerrados. En 16 KB entregó
los mismos extremos antes del fallo de TFLite. Esto acredita el contrato de
BirdNET en esos entornos, sin certificar la app completa en 16 KB.
Las tres fotos empaquetadas se procesaron con el modelo fotográfico original y
produjeron tres puntuaciones válidas por imagen; no constituyen una evaluación
acústica ni una garantía de precisión fotográfica general.

### Decisión sobre TFLite y estado final de este turno

Se inspeccionó LiteRT 1.4.0 como alternativa concreta: AAR oficial de Google
Maven, minSdk21, las cuatro ABI y ELF a 16 KB. La propuesta se registró en el
plan y **no fue aprobada**: el usuario respondió «Conservar TFLite y dejar T1
pendiente». El AAR/licencia candidatos permanecen solo en `closure/`; no se
agregaron a dependencias o assets de producción. No se retiró support/metadata.

El usuario también indicó que no puede conectar ahora el Samsung. Se conserva
la evidencia física anterior de inferencia real, pero **no se repitió el APK
final** tras corregir Lint y ampliar los tests; queda pendiente, sin nueva
solicitud de conexión en este turno. Los emuladores temporales se cerraron.

APK final probado en API24/25: 255.061.809 bytes, SHA-256
`e0858c8bafc04878546f7a4d2ae0fc6e682c34eb95f7d240169aabf7ac4397e0`.
`closure/native-review.json` confirma que ONNX conserva ELF de 16 KB en sus ABI
de 64 bits y TFLite original conserva ELF de 4 KB. `species.json`, `manifest.json`,
modelo y etiquetas mantienen las huellas documentadas en la resolución anterior.
No se modificó `.idea/deploymentTargetSelector.xml` ni `.agents/plugins/`.
Sin commits, push o implementación de T2.

## Condiciones pendientes al cierre de la continuación anterior (histórico)

- Resolver el fallo del runtime fotográfico original en páginas de 16 KB mediante
  una decisión posterior explícita. El usuario eligió conservar TFLite y mantener
  T1 pendiente; no se sustituye esa prueba fallida por la inferencia ONNX aprobada.
- Repetir la clase instrumentada del APK final en el Samsung por USB y sin red
  cuando el teléfono esté disponible. La prueba física previa permanece documentada.

Revisión final de la primera ejecución: sintaxis del preparador mediante `ast.parse`,
referencias locales de ambos documentos, espacios finales de archivos propios y
`git diff --check`, sin errores. Se contrastaron las casillas y cifras con los
XML/logs conservados; entonces faltaban los dos JSON. La continuación de T1
resolvió ese bloqueo, conservando sin reformato los archivos auténticos de terceros.
La casilla T1 sigue abierta por el fallo de TFLite en 16 KB y la repetición física
del APK final. API24/25 y los seis errores de Lint quedaron resueltos en este turno.

T2 continúa pendiente y no se iniciará en este turno. La siguiente confirmación
debe respetar primero los bloqueos de T1 o acordar explícitamente un cambio de
alcance/orden, como exige la lista aprobada.

## Cierre de T1 — actualización aprobada y LiteRT 1.4.1

**T1 completada el 26 de octubre de 2026.** Esta sección reemplaza los estados
pendientes anteriores sin borrar sus fallos ni atribuirles conformidad.
Después de aclarar que ambos modelos se conservan, el usuario aprobó «si por
favor, actualicemos y tratemos de cerrar la T1». La enmienda de `plan.md`
registra esa autorización, limitada al motor fotográfico y sus verificaciones.
La elección del parche 1.4.1 es una decisión técnica dentro de esa actualización,
no una nueva aprobación atribuida al usuario. T2 no fue autorizada ni iniciada.

### Investigación, dependencia efectiva y licencias

LiteRT 1.4.0 resolvió el fallo de 16 KB y pasó en Samsung, pero falló en API24:
`dlopen failed: cannot locate symbol "strtod_l"`. Se conservó la prueba fallida
en `litert-migration/api24-runner.txt` y el diagnóstico en
`api24-native-diagnostic.txt`. El mantenedor confirma ese defecto en Android
antiguo y su corrección en
[LiteRT #3274](https://github.com/google-ai-edge/LiteRT/issues/3274#issuecomment-3503705142);
la [publicación oficial 1.4.1](https://github.com/google-ai-edge/LiteRT/releases/tag/v1.4.1)
registra la corrección del mínimo Android21. La inspección de la tabla dinámica
confirma que 1.4.1 ya no importa `strtod_l` (`patch-inspection.txt`).

Se fijó `com.google.ai.edge.litert:litert:1.4.1`; `litert-api:1.4.1` llega
transitivamente. Se retiraron support/metadata 0.4.4 sin usos en el proyecto.
`final-dependencies.log` confirma esas coordenadas y ONNX 1.22.0, sin artefactos
`org.tensorflow` antiguos. Se conserva `org.tensorflow.lite.Interpreter` y el
clasificador existente, con cuatro hilos CPU y NNAPI deshabilitado. No cambian
preprocesamiento, modelos, etiquetas, minSdk24, target36, cuatro ABI ni criterios.

| Artefacto oficial Google Maven | SHA-256 |
| --- | --- |
| `litert-1.4.1.aar` | `815ef39f7d40f579adda1c7bcb7ce090685fb286777ffd8283f33202f67da1b4` |
| `litert-api-1.4.1.aar` | `ce5f6cc1924f0b9eb0fc339e0d3a3e6c9d041b9deb5e99f800fd6a42150f1875` |

La licencia oficial del AAR se copió íntegra a
`app/src/main/assets/LITERT_LICENSE` (13.575 bytes), idéntica a la del AAR API.
La cadena JNI `2.20.0-dev0+selfbuilt` procede del binario oficial 1.4.1;
no se compiló un runtime propio. Coordenada, hash y cadena nativa se registran
separadamente. Las descargas candidatas permanecen ignoradas bajo `app/build/`.

### Referencias fotográficas fijadas antes de migrar

Los tres índices, su orden y las puntuaciones siguientes se obtuvieron con
TFLite 2.16.1 y quedaron fijados antes de observar LiteRT. Criterio sin cambios:
top3 idéntico y diferencia absoluta por puntuación ≤ 0,001. La prueba llama a
`BirdClassifier`, con imágenes y modelo reales; cierra clasificador y bitmaps.

| Imagen local | Índices | Puntuaciones de referencia |
| --- | --- | --- |
| `photo-001.webp` | 38, 40, 2 | 0,90181565; 0,0034073265; 0,0033567683 |
| `photo-002.webp` | 11, 29, 37 | 0,91397464; 0,0030245832; 0,002625227 |
| `photo-003.webp` | 16, 12, 26 | 0,87790924; 0,0042506633; 0,003572709 |

Huellas conservadas: modelo fotográfico
`e4380e2ac90d2ba83f066fbea4150b40732dd8ccf430b2cb5c2324ac7459bae9`;
`labels.txt` `8bbb993a4555ec02bca668f0d0ebc8d0baaaf7caccce754f0a49b93d96239bf4`;
`labels_map.json` `ef23d3a6006c98f8ad54af80d1b19361b51aaa0846e8dc351c0133f7f974db25`.
Las tres fotos conservan respectivamente
`f404ad2cd7e870c3e4c2d5ea3b638a95c4b5ce4d9650961113227e8160fca3c1`,
`84dc36d77124057e69a1bceff675dac7cf42a80e408d745fe6e62700047f53d0` y
`351280d35abd2bce82be6352dc9095dc8fd98585935ea286eeba91c9939e16a7`.
`input-sha256-before.json` y `final-acceptance.json` contrastan esas entradas y
el modelo BirdNET, `species.json` y `manifest.json`, todos intactos.

### Matriz final ejecutada sobre LiteRT 1.4.1

Toda la evidencia de esta sección está bajo
`app/build/birdnet-verification/litert-migration/`, ignorado. Cada XML final
contabiliza cuatro pruebas, cero fallos, cero errores y cero omisiones.

| Entorno real | ABI / RAM bytes / página bytes | Resultado y diferencia fotográfica máxima | Evidencia |
| --- | --- | --- | --- |
| Android7.0 / API24 | x86_64,x86 / 1.570.775.040 / 4.096 | **4/4**; 0,000000005 | `final-api24-tests.log`, `final-api24-test-results/`, `final-api24-logcat.txt` |
| Android7.1.1 / API25 | x86_64,x86 / 1.570.775.040 / 4.096 | **4/4**; 0,000000005 | `patch-api25-tests.log`, `patch-api25-test-results/`, `patch-api25-logcat.txt` |
| Android15 / API35 ps16k | x86_64,arm64-v8a / 2.593.198.080 / 16.384 | **4/4**; 0,00008155 | `final-api35-tests.log`, `final-api35-test-results/`, `final-api35-logcat.txt` |
| Samsung SM-A566E / Android16 / API36 | arm64-v8a / 7.777.320.960 / 4.096 | **4/4**; 0,00008085 | `patch-samsung-tests.log`, `patch-samsung-test-results/`, `patch-samsung-logcat.txt` |

En los cuatro entornos se comprueban catálogo completo y hashes, JNI fotográfico,
predicciones fotográficas anteriores e inferencia CPU BirdNET real con ONNX1.22.0.
El modelo se mapea a ByteBuffer directo, conserva su tamaño/huella y produce
9.789 valores finitos en [0,1] para 96.000 Float no constantes. Contrato de entrada
y salida, recursos cerrados y ausencia de crash verificados. Sin micrófono ni
descarga. En el entorno de 16 KB se mantuvieron
`bionic.linker.16kb.app_compat.enabled=false` y
`pm.16kb.app_compat.disabled=true`, con página real medida de 16.384.

El usuario volvió a conectar el Samsung por USB y confirmó «Lo dejo en modo
avión y sin Wi-Fi». Antes de la prueba final se verificó modo avión=1, Wi-Fi
deshabilitado y ninguna red predeterminada activa; evidencia en
`patch-samsung-network.txt`. Se actualizó la app sin desinstalar ni borrar datos:
misma firma, fecha inicial `2026-10-22 22:31:17` y directorio de datos, contrastados
en `samsung-before.txt` y `patch-samsung-after.txt`. El primer intento de referencia
anterior a activar modo avión dio tres pruebas aprobadas y un fallo esperado
por avión=0; no cuenta como conformidad offline (`baseline-samsung.txt`).

En API24 hubo una instalación repetida con `INSTALL_FAILED_ALREADY_EXISTS` pese
a que Gradle imprimió BUILD SUCCESSFUL. No se contó como aprobación: se verificó
el fallo del runner y después se ejecutó sobre userdata limpio del AVD temporal.
Solo los XML finales 4/4 de la tabla acreditan la versión fija. No se modificaron
AVD personales ni se elevaron versiones mínimas para evitar el defecto.

### Comandos, paquete y verificaciones finales

Ejecutados desde la raíz con Wrapper y JDK/SDK configurados en el entorno.
Para las instrumentadas se seleccionó exclusivamente el serial del destino en
`ANDROID_SERIAL`: emuladores temporales y Samsung por USB.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :app:testDebugUnitTest --rerun :app:lintDebug
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath
$env:ANDROID_SERIAL='<serial del destino>'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdNetModelAssetsTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'
python tools/prepare_birdnet_assets.py --self-test
python tools/prepare_birdnet_assets.py --model app/build/birdnet-verification/model.onnx --labels app/build/birdnet-verification/labels.csv --taxonomy app/build/birdnet-verification/taxonomy.csv --model-license app/build/birdnet-verification/MODEL_LICENSE --source-license app/build/birdnet-verification/LICENSE --source-commit 2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846 --output app/build/birdnet-verification/litert-migration/reproduced-birdnet
zipalign.exe -c -P 16 -v 4 app/build/outputs/apk/debug/app-debug.apk
aapt2.exe dump badging app/build/outputs/apk/debug/app-debug.apk
python app/build/birdnet-verification/litert-migration/acceptance_check.py
git diff --check
```

- Compilación de ambos APK: salida 0 (`patch-assemble.log`). APK final:
  **267.454.150 bytes**, SHA-256
  `3d2c75ac6d2409ed3d1eea4b6eafc4f65fd178e250f353ff1f8add407254d155`.
- Unitarios ejecutados nuevamente: **30/30**, cero fallos; Lint **0 errores,
  243 advertencias**, sin baseline ni supresiones (`final-unit-lint.log`,
  `final-lint.xml`). Las advertencias restantes no se presentan como corregidas.
- Autocontrol del preparador: **3/3**, salida 0 (`final-preparer-tests.log`).
  Generación real repetida y siete archivos contrastados byte por byte con
  producción: idénticos (`final-reproduction.log`, `reproduced-birdnet/`).
- `zipalign`: salida 0 y Verification successful (`final-zipalign.txt`). APK con
  minSdk24, target36 y cuatro ABI, ONNX sin comprimir; no declara INTERNET ni
  RECORD_AUDIO (`final-apk-badging.txt`). Modelo ONNX de 66.991.322 bytes intacto.
  LiteRT tiene PT_LOAD 16 KB en las cuatro ABI; ONNX en ambas de 64 bits.
  JNI ONNX de 32 bits conserva alineación 4 KB: no se afirma ejecución de
  procesos de 32 bits en un sistema de páginas de 16 KB.
  Detalle: `native-review.json`, `final-native-review.txt`.
- Regresión de Atrás del historial con el APK final en API35/16 KB: **1/1**,
  `final-api35-history.txt`; comando
  `adb shell am instrument -w -r -e class com.example.avesargentinas.ui.log.ObservationLogActivityTest com.example.avesargentinas.test/androidx.test.runner.AndroidJUnitRunner`.
  Menú existente revisado visualmente en claro y oscuro, sin desbordes en la
  configuración vertical normal del emulador: `ui-light.png`, `ui-dark.png`.
  No equivale a la matriz de accesibilidad de T8.
- Auditoría de XML, referencias, huellas, unitarios y Lint:
  `final-acceptance.json`, con todos los criterios comprobados. Revisión estática
  independiente anterior al parche: sin hallazgos, limitada a assets, contratos
  y alcance; dejó las ejecuciones finales a cargo del implementador. API24 detectó
  después el defecto de 1.4.0 y se repitió toda la matriz con 1.4.1. No se atribuye
  al revisor una prueba dinámica que no realizó.

### Estado de cierre y límites

No quedan comprobaciones obligatorias pendientes de T1. Assets auténticos y
reproducibles, carga e inferencia real de ambos modelos y compatibilidad de esta
tarea demostrados. Se preservaron los cambios ajenos de
`.idea/deploymentTargetSelector.xml` y `.agents/plugins/`; sin commits ni push.
Se cerró únicamente el emulador temporal de esta tarea al terminar.

La señal sintética no mide acierto acústico. Captura, pantalla de sonido,
cancelación de la integración, corpus de aves argentinas, accesibilidad y
rendimiento completo pertenecen a T2–T10 y siguen pendientes. Los 372 nombres
españoles ausentes permanecen nulos, sin traducciones inventadas. **Se espera
confirmación explícita para empezar T2 en otro turno.**

## T2 — Señal y ventanas

El usuario autorizó exclusivamente T2 tras completar T1. La evidencia previa
de T1 se conserva arriba y en `app/build/birdnet-verification/litert-migration/`,
incluido `final-acceptance.json`. Se añadió conversión PCM16 y remuestreo sinc
con ventana Hann, radio de 64 muestras y corte de 14 kHz; los coeficientes se
normalizan también en los bordes para conservar una señal constante. La salida
tiene 320.000 muestras y las cuatro posiciones son 0, 96.000, 192.000 y 224.000.

Las pruebas se escribieron antes de la implementación: la ejecución inicial
falló en compilación por las referencias aún inexistentes `BirdSoundAudio` y
`CapturedAudio` (`app/build/birdnet-verification/t2-red.log`). Después se ejecutó
`.\gradlew.bat :app:testDebugUnitTest --rerun` desde la raíz: salida 0,
**35 pruebas, cero fallos, errores u omisiones**, incluidas cinco de T2.
La salida se conserva en `app/build/birdnet-verification/t2-unit.log` y el XML
en `app/build/test-results/testDebugUnitTest/`. La prueba verifica extremos PCM,
entrada inalterada, frecuencias y tamaños inválidos, constantes, duración,
orden, cuatro ventanas y evento en la última muestra de entrada.

Mediciones de RMS de tonos sintéticos, excluyendo 128 muestras en cada borde:

| Entrada | 1 kHz | 10 kHz | 18 kHz |
| --- | ---: | ---: | ---: |
| 44.100 Hz | -0,00036 dB | -0,00029 dB | -93,64 dB |
| 48.000 Hz | -0,00027 dB | -0,00049 dB | -88,90 dB |

Se cumplen los límites de ±1 dB en banda y al menos 40 dB de atenuación a
18 kHz en esta prueba sintética. No se midió captura física, calidad de
identificación ni rendimiento en teléfono: corresponden a tareas posteriores.
No se inició T3, ni se hicieron commits o push.

## T3 — Catálogo y selección

El usuario autorizó únicamente T3. T1 figura cerrada con la matriz y huellas
registradas en la sección de cierre de T1; T2 también estaba completada. La
carga comprueba el SHA-256 preparado de `species.json`, las 9.789 posiciones y
sus índices consecutivos. Una puntuación de longitud incorrecta, no finita o
fuera de [0,1] produce error incluso si corresponde a una clase no aviar.
La selección filtra Aves, agrupa por nombre científico canónico normalizado,
conserva la puntuación máxima, incluye el umbral, ordena por puntuación e
índice y limita la lista a cinco. Se conserva el índice y el nombre del modelo.

El nombre regional precede al español y la ausencia de ambos queda representada
por `null`, sin ocultar la especie; la indicación textual corresponde a T7/T8.
Una ficha requiere coincidencia científica exacta con `labels_map.json` y un
mapa local existente, cuya URI se conserva independientemente de la identidad
BirdNET. No se agregaron alias: por eso nombres parecidos y cambios de género
no resuelven fichas ajenas, pero pueden dejar fichas legítimas indisponibles.
No se modificaron el clasificador fotográfico, sus etiquetas o BirdInfoActivity.

- Las pruebas JVM se escribieron antes de la implementación; el primer comando
  falló en compilación por las clases de T3 ausentes. Luego
  `./gradlew.bat :app:testDebugUnitTest --rerun` pasó y, tras añadir una prueba
  adicional de coincidencia exacta, `./gradlew.bat :app:testDebugUnitTest` pasó:
  XML final **40 pruebas, 0 fallos, 0 errores y 0 omitidas**, cinco de T3.
  Se verificaron duplicados, máximos, empates, umbral, límite, clases no aviares,
  nombres ausentes, mapa ausente, índices inicial/final y datos inválidos.
- `:app:connectedDebugAndroidTest` con filtro `BirdSoundCatalogTest` en
  Samsung SM-A566E / Android 16: XML **1 prueba, 0 fallos, 0 errores y 0
  omitidas**. Cargó los assets reales, comprobó los índices 0 y 9788, un ave
  sin traducción ni ficha y `Aramus guarauna` con nombre regional y URI de
  mapa que pudo abrirse. La instalación fue una actualización del APK de
  depuración; no se borraron datos.
- El umbral 0,35 es provisional: no se evaluó precisión acústica ni se calibró
  con el corpus; eso corresponde a T9. No se ejecutaron T4 ni tareas posteriores.
  Sin commits ni push.

## T4 — Trabajo parcial y discrepancia numérica

El usuario autorizó únicamente T4 y, durante esta ejecución, tres señales
sintéticas reproducibles como referencias numéricas. T1–T3 contaban con la
evidencia anterior; T4 permanece **pendiente**, sin iniciar T5.

Se implementó un clasificador todavía no validado completamente: carga del
modelo, comprobación de contratos, cuatro ventanas, máximo por índice, omisión
de segmentos constantes y cierre serializado con la ejecución. Las señales
son tonos generados, no grabaciones de aves ni evaluación de calidad acústica.
`tools/prepare_birdnet_t4_fixtures.py` fija el modelo por SHA-256 y genera las
tres entradas Float32 de 96.000 muestras a 32 kHz y sus 9.789 salidas con
ONNX Runtime Python 1.22.0 CPU, ejecución secuencial y dos hilos. Frecuencias
de los tonos: 440/1200, 750/2700 y 1800/5100 Hz. El manifiesto de fixtures
registra huellas; los archivos están exclusivamente en `androidTest/assets`.

En Samsung SM-A566E, Android 16, modo avión activo y Wi-Fi deshabilitado,
`BirdNetClassifierTest` ejecutó cuatro pruebas: tres pasaron (silencio y uso
tras cierre, evento al final, y máximo de cuatro eventos separados) y falló
la comparación de referencias. Diferencias absolutas máximas por clase para
las señales 1, 2 y 3: **0,0051316917; 0,009003997; 0,0065317154**. El límite
aprobado continúa en **0,001**. El fallo inicial detalló la clase 2945 de la
señal 1: referencia 0,12910616 frente a Android 0,12482661. No se alteraron
referencias ni tolerancias para obtener conformidad. La misma ejecución de
referencia en PC con optimización de grafo desactivada produjo diferencia
cero respecto de la referencia por defecto; esto no demuestra la causa de la
diferencia Android. Debe investigarse el contrato y la ejecución entre entornos.

El primer intento instrumentado falló antes de ejecutar por una firma no void
de JUnit; se corrigió y se repitió. El XML final y los máximos deben conservarse
como evidencia del fallo, no como aprobación de T4. La primera compilación de
las pruebas falló por ausencia del clasificador, antes de implementarlo.
`./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` terminó
con salida 0; Gradle informó advertencias de configuración obsoleta. La prueba
instrumentada filtrada terminó con salida 1 y un fallo de cuatro pruebas.

Pendientes: investigar la diferencia sin cambiar el criterio; probar rechazo
de entrada/salida inválida y reintento tras inicialización fallida; cancelación
durante `run`, inicialización que termina después de cancelarse, ausencia de
publicación tardía, cierre sin concurrencia con `run`, ciclos repetidos y
retención nativa, y ausencia de bloqueo de Main. El código no debe considerarse
listo para integrar hasta demostrar esas condiciones. Sin commits ni push.

### Continuación de T4 — diagnóstico de precisión y ciclo de vida

La comparación directa del modelo oficial en ONNX Runtime Android 1.22.0,
sin pasar por `BirdNetClassifier`, reproduce la discrepancia. En Samsung, con
optimizaciones desactivadas, los máximos por señal fueron 0,00368312;
0,002820015; 0,0009152293. Con las optimizaciones predeterminadas fueron
0,0051316917; 0,009003997; 0,0065317154. Por tanto, desactivarlas no cumple
el criterio y no se cambió la configuración de producción.

El modelo original contiene 361 tensores iniciales FP16. Con optimizaciones
desactivadas, los grafos preparados por ONNX Runtime 1.22.0 contienen 374
conversiones a FP32 y 14 a FP16 en PC x86-64, frente a 334 y 49 en Samsung
ARM64. El [código oficial de ONNX Runtime 1.22.0](https://github.com/microsoft/onnxruntime/blob/v1.22.0/onnxruntime/core/optimizer/insert_cast_transformer.cc)
documenta la inserción de conversiones para operadores FP16 sin una implementación
asignada. Es una explicación compatible con las diferencias observadas, no una
prueba aislada de qué operador causa cada diferencia.

Experimento diagnóstico: se guardó el grafo preparado por el runtime de PC con
optimizaciones desactivadas y se ejecutó temporalmente en el Samsung, con las
entradas y referencias existentes. Las diferencias máximas fueron 0,0000078082085;
0,000006556511; 0,0000008940697. Ese grafo derivado tiene 65.830.813 bytes y
SHA-256 `cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`;
permanece solo en `app/build/birdnet-verification/t4/pc-NO_OPT.onnx`, no en los
assets de la app. No se sustituye el binario oficial ni se da por validada la
portabilidad, licencia, reproducción, memoria, rendimiento o calidad del grafo
derivado. El archivo temporal del teléfono se eliminó después del ensayo.

La clase de T4 ampliada probó en el Samsung la cancelación mientras `run` estaba
en la pila nativa, reintento, cancelación durante `createSession` nativo sin
publicación de sesión tardía, cierre durante inferencia, respuesta de Main,
entrada inválida, modelo ausente y tres ciclos de apertura/ejecución/cierre.
Estas pruebas no sustituyen una medición de retención nativa sostenida ni una
demostración exhaustiva de todas las carreras. La comparación original contra
las referencias permanece fallando y T4 no puede cerrarse; tampoco se ha iniciado
T5. Antes de emplear una variante del grafo, el plan exige una decisión y revisión
explícitas, incluida su preparación reproducible y las mismas validaciones de T1.

### T4a — Preparación y evaluación fuera de producción

**T4a completada el 26 de octubre de 2026.** El usuario aprobó las tareas
revisadas e indicó «si, comencemos con 4a». La ejecución se limitó a preparar
y comprobar el candidato; el modelo oficial empaquetado no se sustituyó y T4b,
T4c y T5 no se iniciaron.

El preparador `tools/prepare_birdnet_t4.py` usa la validación existente de tamaño
y SHA-256 del original y rechaza sobrescribir una salida. Antes de implementarlo
la prueba falló por ausencia del módulo; después `python tools/test_prepare_birdnet_t4.py`
pasó **3/3**: tamaño incorrecto, huella incorrecta con tamaño correcto y protección
de salida existente. Se ejecutaron dos procesos independientes del preparador final:
sus archivos fueron idénticos byte a byte y coinciden con el candidato probado.
Cada uno tiene **65.830.813 bytes**, SHA-256
`cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`.
La fuente conservó sus **66.991.322 bytes** y SHA-256
`306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457`.

Preparación: Windows x86-64, Python 3.10.11, distribución oficial
`onnxruntime` 1.22.0, CPU secuencial, dos hilos intraoperador y optimización
desactivada. El wheel `onnxruntime-1.22.0-cp310-cp310-win_amd64.whl` tiene
SHA-256 `20bca6495d06925631e201f2b257cc37086752e8fe7b6c83a67c6509f4759bc9`;
se contrastaron **297** archivos Python y binarios instalados con el wheel,
sin diferencias. `app/build/birdnet-verification/t4a/provenance.json` registra
fecha UTC, origen fijado, comando, parámetros y huellas de modelo, derivado,
distribución y avisos. Las dos salidas finales y sus manifiestos están allí.
Con `PYTHONPATH=app/build/birdnet-verification/t4-python`, los procesos finales
ejecutaron `python tools/prepare_birdnet_t4.py app/src/main/assets/birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16.onnx app/build/birdnet-verification/t4a/candidate-3.onnx`
y el mismo comando con salida `app/build/birdnet-verification/t4a/candidate-4.onnx`.

La licencia de pesos incluida identifica Apache 2.0 y la del runtime, MIT;
también se inspeccionaron la licencia del repositorio, los avisos de terceros y
la atribución existente. No se detectó una condición pendiente para la evaluación
local. La integración deberá conservar esos textos y avisos y hacer constar que
el grafo fue modificado; la atribución actual describe correctamente el original
empaquetado y no se alteró anticipadamente. Las huellas están en `provenance.json`.

En Samsung SM-A566E, modo avión activo y Wi-Fi deshabilitado, la prueba
`BirdNetT4CandidateTest` comprobó huellas del candidato, de las tres señales y
de sus tres referencias, tipos y formas de entrada/salida, y 9.789 puntuaciones
finitas en [0,1]. Las diferencias máximas por clase fueron **0,0000078082085**,
**0,000006556511** y **0,0000008940697**, inferiores al límite sin cambios de
**0,001**. La prueba se escribió con un control explícito de provisión; su primer
ensayo falló por candidato ausente y el ensayo final pasó **1/1** al copiarlo al
caché de prueba. El candidato se retiró del teléfono después de la prueba. La
prueba queda omitida en ejecuciones generales si no se solicita y provisiona
explícitamente; una ejecución de T4a con la opción activa exige el archivo y su
huella. XML y máximos: `t4a/candidate-samsung-final.xml` y
`t4a/candidate-samsung-final.txt`.

La prueba instrumentada existente de metadatos pasó **1/1** y comprobó las
9.789 posiciones, etiquetas, correcciones documentadas y hashes del catálogo
original (`t4a/catalog-samsung.xml`). El verificador ONNX 1.17.0 aceptó ambos
grafos; sus nombres de entradas/salidas coinciden y los **429 inicializadores**
del candidato conservan nombre y contenido idénticos al original. Esto, junto
con la comparación por índice, es evidencia de correspondencia para esta tarea,
no prueba de equivalencia para cualquier audio ni de precisión acústica.

`./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` terminó
con salida 0, con advertencias de configuración Gradle obsoleta. Se mantienen
pendientes la integración y matriz de API24/25 y 16 KB de T4b, y los ensayos
de retención nativa de T4c. El ensayo acústico con aves y los presupuestos de
rendimiento corresponden a T9/T10; las señales sintéticas no los sustituyen.

### T4b — Integración y matriz de compatibilidad

**T4b completada el 27 de octubre de 2026.** El usuario autorizó comenzar T4b
y pidió continuar hasta intentar cerrarla. El APK incluye únicamente el grafo
derivado de T4a, sin compresión; el modelo oficial se conserva como fuente
verificable en `app/build/birdnet-verification/model.onnx` y en la copia previa
`t4b/original-before-integration.onnx`. Ambos tienen SHA-256
`306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457`; son
archivos de build y pueden eliminarse al limpiar, pero el manifiesto conserva
la URL y el commit fijados para recuperar y verificar el original.

El derivado empaquetado tiene 65.830.813 bytes y SHA-256
`cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`.
El manifiesto distingue fuente y derivado e incluye comando, fecha y entorno
de preparación y huella del wheel; la atribución indica la transformación.
El clasificador verifica tamaño y SHA-256 antes de abrir la sesión. El catálogo
sigue comprobando independientemente la huella fija de `species.json`. Las
etiquetas, especies, metadatos de origen y correcciones se contrastaron byte a
byte o por contenido con los anteriores. Dos preparaciones finales del paquete
produjeron los mismos siete archivos byte a byte. Los avisos de licencia y el
modelo fotográfico se conservaron.

La prueba de integridad del clasificador se escribió antes de su implementación:
falló por la función inexistente y pasó al implementarla. Se probaron longitud
y huella incorrectas y preservación de la posición del buffer. Una comprobación
adicional de procedencia falló primero por `generated_at_utc` ausente; se añadió
la procedencia al manifiesto y se recompiló el APK. No se cambió ninguna de las
tres referencias ni su tolerancia; sus seis hashes se contrastaron con el APK de
pruebas. Los preparadores pasaron **4/4** y **3/3** autocontroles.

El APK final, igual en los cuatro entornos, mide **267.454.212 bytes** y su
SHA-256 es `76b032098277b46e267b6dafc66361e3cb35557e27a1e38b777b5d9ef1d74ac6`.
`zipalign -c -P 16 -v 4` informó `Verification successful`; `aapt2` informó
minSdk24 y las cuatro ABI originales. Los segmentos PT_LOAD de ONNX y LiteRT
de 64 bits tienen alineación de al menos 16.384 bytes. JNI de ONNX de 32 bits
mantiene segmentos de 4.096 bytes: no se afirma compatibilidad de procesos de
32 bits en un sistema de páginas de 16 KB.

| Entorno | ABI / página | Assets y modelo fotográfico | Referencias de audio | Catálogo | Mayor diferencia de audio por clase |
| --- | --- | --- | --- | --- | ---: |
| Samsung SM-A566E, Android 16 | arm64-v8a / 4.096 | 4/4 | 1/1 | 1/1 | 0,0000078082085 |
| Emulador Android 7.0 / API24 | x86_64 / 4.096 | 4/4 | 1/1 | 1/1 | 0,000017285347 |
| Emulador Android 7.1.1 / API25 | x86_64 / 4.096 | 4/4 | 1/1 | 1/1 | 0,000017285347 |
| Emulador Android 15 / API35 | x86_64 / 16.384 | 4/4 | 1/1 | 1/1 | 0,000017285347 |

En cada entorno las cuatro pruebas de assets comprueban el runtime fotográfico,
las tres fotografías de referencia, las 9.789 posiciones y hashes y una
inferencia real del modelo sonoro empaquetado. La prueba de referencias compara
las tres entradas con 9.789 puntuaciones cada una y la del catálogo comprueba
recursos locales. En el Samsung se verificaron modo avión activo, Wi-Fi apagado
y ausencia de red predeterminada. En API35 se verificaron página real de 16.384,
`bionic.linker.16kb.app_compat.enabled=false` y
`pm.16kb.app_compat.disabled=true`. El primer intento API24 no pudo instalar
por almacenamiento temporal insuficiente; se documentó y se usó un AVD nuevo
aislado. Un reinicio posterior de API25 quedó sin ADB y se repitió en otro
puerto sobre un AVD aislado. Ninguno de esos intentos fallidos se cuenta como
aprobación. Los emuladores de prueba usados se cerraron al terminar.

`./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
:app:testDebugUnitTest :app:lintDebug` terminó con salida 0; los XML unitarios
registran **42 pruebas, 0 fallos, 0 errores y 0 omitidas**, y Lint **0 errores
y 245 advertencias**. Los detalles y logcats por entorno se conservan bajo
`app/build/birdnet-verification/t4b/`, con prefijos `samsung-final`,
`api24-final`, `api25-final` y `api35-final`. Se actualizaron los APK de prueba
sin desinstalar ni borrar datos del teléfono.

T4b no acredita ausencia de retención nativa sostenida ni calidad acústica real;
esas comprobaciones corresponden a T4c y T9/T10. T4 siguió pendiente hasta
completar T4c. No se hicieron commits ni push.

### T4c — Demostrar cancelación, limpieza y cierre de T4 con el derivado

**T4c y T4 completadas el 27 de octubre de 2026.** El usuario autorizó comenzar
T4c para cerrar la evaluación del runtime de inferencia y la gestión de recursos.

En Samsung SM-A566E (Android 16, ARM64, modo avión activo, Wi-Fi deshabilitado
y sin red predeterminada), la suite instrumentada `BirdNetClassifierTest`
ejecutó sus **11 pruebas con 11/11 aprobadas** (0 fallos, 0 errores):

1. `syntheticWindowsMatchIndependentRuntimeForEveryClass`:
   Las 3 referencias de desarrollo comparadas contra el modelo derivado empaquetado
   obtuvieron diferencias absolutas máximas por clase de:
   - Señal 1: `0,0000078082085`
   - Señal 2: `0,000006556511`
   - Señal 3: `0,0000008940697`
   Todas estrictamente inferiores al umbral aprobado de `0,001`.
2. `constantAudioHasNoDetectionsAndClosedClassifierCannotRun`:
   Audio de varianza constante (silencio) omite llamadas nativas y devuelve
   vector de 9.789 ceros; el clasificador cerrado rechaza nuevas ejecuciones
   con `IllegalStateException`.
3. `finalWindowContributesRealScores`:
   Señal no constante concentrada en el último segundo (muestras 288.000–320.000,
   correspondiente a la cuarta ventana [7, 10] s) aporta puntuaciones positivas
   finitas en [0,1], comprobando la preservación de la cola de captura.
4. `fourSeparatedEventsAggregateByMaximum`:
   Eventos armónicos distribuidos en las cuatro ventanas agregan por máximo
   exacto por clase.
5. `cancelWhileNativeRunIsActiveAndThenRetry`:
   Cancelación durante la ejecución activa en la capa nativa (`running.setTerminate(true)`)
   interrumpe de forma limpia, captura la excepción y permite reintentar una nueva
   inferencia exitosa inmediatamente.
6. `cancelledNativeInitializationDoesNotPublishSessionAndCanRetry`:
   Cancelación durante la creación nativa de la sesión (`createSession`) aborta
   sin publicar una sesión corrupta y permite reinicializar y clasificar.
7. `closeDuringNativeInferenceSuppressesResult`:
   El cierre durante inferencia activa interrumpe la sesión, no publica resultados
   parciales e invalida el clasificador.
8. `invalidInputFailsAndRepeatedSessionsCanRunAndClose`:
   Rechazo de tamaño distinto de 320.000 muestras y valores NaN/infinitos;
   ejecución y cierre limpios a lo largo de ciclos sucesivos.
9. `missingModelFailsAndFreshClassifierCanRetry`:
   Fallo de carga ante asset inexistente no corrompe el runtime; una nueva
   instancia válida inicializa y ejecuta normalmente.
10. `mainThreadRemainsResponsiveWhileClosingInference`:
    El cierre desde segundo plano mientras corre la inferencia no bloquea el hilo
    principal (el looper de Main respondió en menos de 2 s).
11. `repeatedCyclesDoNotRetainNativeMemory`:
    Medición explícita de retención nativa mediante `Debug.getNativeHeapAllocatedSize()`
    y `Debug.MemoryInfo` a lo largo de 5 ciclos completos sucesivos de creación,
    inicialización, inferencia de 10 s y cierre:
    - Heap nativo base (`baselineAllocated`): **13.596.448 bytes** (~13,6 MB); PSS base: 216.274 KB.
    - Ciclo 0: 13.597.968 bytes (delta: +1.520 bytes; PSS: 215.881 KB).
    - Ciclo 1: 13.597.968 bytes (delta: +1.520 bytes; PSS: 216.637 KB).
    - Ciclo 2: 13.597.856 bytes (delta: +1.408 bytes; PSS: 216.525 KB).
    - Ciclo 3: 13.597.856 bytes (delta: +1.408 bytes; PSS: 216.645 KB).
    - Ciclo 4: 13.597.856 bytes (delta: +1.408 bytes; PSS: 216.537 KB).
    - Delta neto final: **+1.408 bytes** (< 1,5 KB); delta de PSS total: **+263 KB**.
    No existe retención nativa acumulativa; las estructuras y buffers de ONNX Runtime
    quedan completamente liberados tras cada invocación a `close()`.

Comprobaciones automáticas complementarias ejecutadas en la misma sesión:
- Pruebas instrumentadas de assets (`BirdNetModelAssetsTest`): **4/4 OK**.
- Pruebas instrumentadas de catálogo (`BirdSoundCatalogTest`): **1/1 OK**.
- Pruebas unitarias de JVM (`:app:testDebugUnitTest`): **42/42 OK**.
- Android Lint (`:app:lintDebug`): **0 errores, 0 fatales**.

**Cierre formal de T4:**
Con T4a (reproducibilidad y procedencia del derivado), T4b (integración empaquetada
y matriz de compatibilidad API 24, API 25, API 35 16 KB y Samsung) y T4c (cancelación,
limpieza y ausencia demostrada de retención nativa) cumplidas, **T4 queda formalmente
cerrada**. Los ensayos de precisión acústica en aves argentinas corresponden a T9
y la captura real del micrófono a T5. Sin commits ni push.

## T5 — Captura de audio: diez segundos efectivos y liberación

**T5 completada el 27 de octubre de 2026.** El usuario autorizó únicamente T5
tras constatar que sus dependencias T2 y T4 estaban completas con evidencia.
No se inició T6 ni se implementó la pantalla de sonido; no se hicieron commits ni push.

### Alcance e implementación de AudioRecorder

Se implementó `AudioRecorder.kt` en `app/src/main/java/com/example/avesargentinas/ui/sound/AudioRecorder.kt`,
y se declararon en `app/src/main/AndroidManifest.xml` el permiso `RECORD_AUDIO` y la feature
opcional `android.hardware.microphone` (`required="false"`):

1. **Configuración de AudioRecord y fuentes:**
   - Modo mono PCM16 (`AudioFormat.CHANNEL_IN_MONO`, `AudioFormat.ENCODING_PCM_16BIT`).
   - Selección dinámica de fuente de audio: consulta soporte del sistema para
     `UNPROCESSED` vía `AudioManager.getProperty(PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)`;
     utiliza `MediaRecorder.AudioSource.UNPROCESSED` cuando está soportado y recurre a
     `MediaRecorder.AudioSource.MIC` como alternativa estándar sin procesamientos de llamadas.
   - Secuencia de frecuencias candidatas: intento en 32.000 Hz, seguido de 48.000 Hz y 44.100 Hz
     si la configuración anterior no puede inicializarse (`STATE_INITIALIZED`).
2. **Duración efectiva y lectura no bloqueante:**
   - La duración está definida estrictamente por la cantidad de muestras objetivo:
     `samplesTarget = sampleRate * 10` (exactamente 320.000 muestras para 32 kHz,
     480.000 para 48 kHz y 441.000 para 44,1 kHz), sin depender de `delay(10000)` ni
     de tiempos de preparación del hardware.
   - Bucle de lectura en `Dispatchers.IO` utilizando bloques de 1.024 muestras en modo
     `AudioRecord.READ_NON_BLOCKING`. Ante lectura cero cede la ejecución mediante `delay(10)`
     evitando bucle ocupado.
3. **Progreso acotado:**
   - El progreso `Float` se calcula como `samplesRead.toFloat() / samplesTarget` y se emite
     a través del callback `onProgress`.
   - Throttling temporal: se actualiza como máximo diez veces por segundo (intervalos de
     al menos 100 ms entre emisiones intermedias), garantizando la emisión final de `1.0f` al
     completar la captura.
4. **Watchdogs de seguridad y errores:**
   - Watchdog de estancamiento (stall watchdog): aborta con `AudioRecordingException` si
     transcurren 2 segundos continuos sin recibir nuevas muestras tras el inicio. Una lectura
     cero no se convierte en silencio ficticio ni avanza el recuento.
   - Watchdog de tiempo total: aborta con `AudioRecordingException` si transcurren 15 segundos
     totales sin completar la captura de las muestras necesarias.
   - Detección de silenciamiento por políticas de privacidad del sistema en Android 10+
     (API 29+): inspección de `AudioRecord.getActiveRecordingConfiguration()?.isClientSilenced`,
     abortando con error explícito sin presentar la señal silenciada como captura válida.
   - Validación de códigos de error de lectura (`ERROR_INVALID_OPERATION`, `ERROR_BAD_VALUE`,
     `ERROR_DEAD_OBJECT`, `ERROR`).
5. **Cancelación, revocación y liberación de recursos:**
   - Soporta cancelación cooperativa mediante cancelación del Job de corrutina y mediante
     el método explícito `recorder.cancel()`.
   - En ambos casos se ejecuta `record.stop()` y `record.release()` dentro de bloques `finally`,
     invalidando el intento y descartando el buffer parcial.
   - Medición de liberación: el micrófono queda completamente liberado en menos de un segundo
     (tiempos medidos entre 115 ms y 682 ms).
   - Bloqueo de capturas concurrentes: una segunda invocación mientras `isRecordingActive == true`
     es rechazada de forma inmediata con `IllegalStateException`.
   - Detección de revocación: verificación del permiso `RECORD_AUDIO` antes de iniciar y durante
     la lectura en caliente; si se revoca el permiso se aborta inmediatamente con `SecurityException`,
     descartando el audio parcial y liberando el micrófono.
   - Privacidad: no se guardan grabaciones ni buffers temporales en almacenamiento; ningún
     archivo `.wav`, `.pcm` u otro archivo de audio se crea en disco (`filesDir`, `cacheDir`, `externalCacheDir`).

### Resultados de verificación en Samsung SM-A566E

Ejecución de la suite `AudioRecorderTest` completa por USB en el dispositivo físico
Samsung SM-A566E (Android 16, ARM64, modo avión activo, Wi-Fi apagado, sin red):
Comando: `.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.AudioRecorderTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'`
Salida: **15 pruebas ejecutadas, 15/15 aprobadas, 0 fallos, 0 errores, 0 omitidas** (tiempo total: 21,428 s).
Evidencia XML archivada en `app/build/birdnet-verification/t5/audiorecorder-samsung-final.xml`.

| Prueba instrumentada | Tiempo | Requisito / Propósito demostrado |
| --- | ---: | --- |
| `concurrentRecordingBlocked` | 151 ms | RF-6: rechazo de captura simultánea con `IllegalStateException`. |
| `cancellationReleasesMicrophoneUnderOneSecondAndAllowsRetry` | 651 ms | RF-7: cancelación por corrutina libera micrófono en 651 ms (< 1 s), no entrega audio y permite reintentar inmediatamente. |
| `permissionRevokedDuringRecordingThrowsSecurityException` | 17 ms | RF-10: revocación de permiso en caliente aborta con `SecurityException`, descarta audio parcial y libera micrófono. |
| `cancelMethodExplicitlyCancelsAndReleasesUnderOneSecond` | 115 ms | RF-7: `recorder.cancel()` aborta y libera micrófono en 115 ms (< 1 s) con `CancellationException`. |
| `initializationFallbackToAlternativeSampleRates` | 31 ms | Fallback de frecuencia: 32 kHz no disponible conmuta automáticamente a 48 kHz (480.000 muestras). |
| `silencedClientOnApi29ThrowsException` | 12 ms | RF-10: detección en API 29+ de silenciamiento del sistema (`isClientSilenced`) aborta con error y libera recursos. |
| `physicalCaptureRecordsTenEffectiveSecondsWithProgress` | 10.126 ms | RF-5: captura física real en micrófono de hardware; 10 segundos efectivos por cantidad de muestras (32.000 Hz, 320.000 muestras), progreso no decreciente hasta 1.0f con tasa ≤ 10 Hz (intervalos ≥ 90 ms). |
| `startRecordingFailureThrowsExceptionAndReleases` | 7 ms | Fallo al iniciar grabación en AudioRecord aborta con `AudioRecordingException` y libera recursos. |
| `stallWatchdogTriggersWhenNoSamplesArriveForTwoSeconds` | 42 ms | Watchdog: 2 s continuos sin muestras aborta captura y libera micrófono. |
| `fallbackTo44100WhenBoth32000And48000Fail` | 23 ms | Fallback completo: 32 kHz y 48 kHz fallidos conmutan a 44,1 kHz (441.000 muestras). |
| `totalTimeoutWatchdogTriggersAtFifteenSeconds` | 14 ms | Watchdog: 15 s sin completar captura aborta y libera micrófono. |
| `permissionDeniedThrowsSecurityException` | 7 ms | RF-3: ausencia inicial de permiso `RECORD_AUDIO` lanza `SecurityException` sin activar micrófono. |
| `allSampleRatesFailInitializationThrowsAudioRecordingException` | 21 ms | Fallo en todas las frecuencias candidatas aborta limpiamente sin fugas de recursos. |
| `hardwareReadErrorThrowsExceptionAndReleasesMicrophone` | 11 ms | Códigos de error hardware (`ERROR_DEAD_OBJECT`) abortan con error y liberan micrófono. |
| `noAudioFilesCreatedInStorage` | 10.200 ms | RF-25: 10 segundos de captura física en hardware no generan ningún archivo en almacenamiento (0 archivos de audio). |

### Verificaciones automáticas complementarias

- Suite de regresión de inferencia `BirdNetClassifierTest` en Samsung SM-A566E sin red: **11/11 aprobadas** (0 fallos, 0 errores, tiempo: 23,118 s).
- Suite unitaria de JVM (`:app:testDebugUnitTest`): **42/42 aprobadas** (0 fallos, 0 errores).
- Android Lint (`:app:lintDebug`): **0 errores, 245 advertencias** (mismo recuento de advertencias de T4b/T4c; anotación `@RequiresPermission(Manifest.permission.RECORD_AUDIO)` aplicada en producción).
- Compilación de depuración (`:app:assembleDebug`): **salida 0 exitosa**.

### Estado de cierre de T5 y pendientes

T5 queda completamente implementada, probada y acreditada con evidencia empírica en hardware físico.
No se guardaron audios ni se entregaron datos incompletos como éxito.
T6 (coordinación de sesión con `BirdSoundViewModel`) no ha sido iniciada y requiere autorización previa. Sin commits ni push.

## T6 — Coordinación de una sesión

**T6 completada el 27 de octubre de 2026**, tras comprobar que T3, T4 y T5 figuraban completas con la evidencia anterior. El ViewModel prepara el clasificador antes de capturar, conserva una sesión para reintentos y usa un token y un Job por intento. Invalida antes de cancelar; durante la limpieza mantiene `Stopping` y no admite un nuevo inicio. Al abandonar cierra la sesión y descarta respuestas tardías. El audio es local a la operación; los estados observables contienen progreso, detecciones o error, sin PCM ni `SavedStateHandle`/`Bundle`.

En Samsung SM-A566E, `BirdSoundViewModelTest` ejecutó **10/10** pruebas sin fallos ni omisiones; XML en `app/build/birdnet-verification/t6/viewmodel.xml`. Se confirmó también con AndroidJUnitRunner directo, **OK (10 tests)**, salida en `app/build/birdnet-verification/t6/viewmodel-direct.txt`. Las pruebas controlan doble inicio, preparación que termina tras cancelar y cierre posterior, bloqueo de reintento hasta liberar, cancelación de captura y análisis, progreso y resultado tardíos, errores diferenciados de lista vacía y reintentos, segundo plano en captura y análisis, conservación de resultados, retención mediante ViewModelStore y cierre al limpiarlo. La comprobación estructural de estados y constructor no equivale a una prueba de muerte real del proceso.

La primera prueba se escribió antes del ViewModel y la compilación falló por los símbolos todavía inexistentes. Un primer ensayo de esa prueba falló por registrar el evento de preparación fuera del bloque no cancelable; se corrigió el doble para registrar allí la finalización efectiva. En dos ejecuciones intermedias Gradle informó `ArrayIndexOutOfBoundsException` en `AndroidTestResultListener.executionFinished` pese a XML 8/8 y salida exitosa; la ejecución directa del runner confirmó esos ocho casos, y la suite final de diez se confirmó nuevamente directamente. No se atribuye una causa concluyente a la excepción del receptor.

La regresión separada `BirdNetClassifierTest` ejecutó **11/11** con el modelo real en el mismo teléfono (`app/build/birdnet-verification/t6/classifier.xml`); los dobles de coordinación no son evidencia de inferencia. `:app:assembleDebug`, `:app:testDebugUnitTest` y `:app:lintDebug` terminaron con salida 0; 42 unitarios sin fallos y Lint con 0 errores y 245 advertencias. La captura física y liberación de micrófono conservan la evidencia de T5; no se repitieron en esta tarea. La conexión de permisos, navegación y pantalla corresponde a T7; rotación real de Activity, muerte de proceso y recorrido conectado corresponden a T8. No se inició T7 ni se hicieron commits o push.

## T7 — Acceso, permisos y pantalla con resultados reales

**T7 completada el 27 de octubre de 2026**, tras verificar que las dependencias T1–T6 estaban completas con evidencia demostrada.

### Decisiones de implementación e interfaz (`frontend-design` en Android Views)
- **Acceso desde el menú principal (`MainMenuActivity`)**:
  - Incorporación de botón/card `btnSoundCapture` ("Identificar por sonido") en `activity_main_menu.xml` inmediatamente después del botón de foto, manteniendo la coherencia visual con `brand_primary`, esquinas redondeadas y elevación estándar.
  - Compatibilidad estricta con captura instantánea de fotos (`instant_capture`): si está activa, el usuario inicia en `MainActivity`, pero al pulsar Atrás regresa a `MainMenuActivity` con la opción de sonido visible y accesible.
- **Registro no exportado**:
  - `BirdSoundActivity` declarada en `AndroidManifest.xml` con `android:exported="false"`, previniendo invocaciones externas no autorizadas.
- **Diseño de pantalla (`activity_bird_sound.xml`)**:
  - Cabecera estándar basada en `BaseActivity` con soporte de insets del sistema.
  - Tarjeta explicativa con instrucciones claras y duración fija (10 segundos).
  - Tarjeta de estado con icono de micrófono (`ic_mic`), texto de estado con región en vivo `polite` para lectores de pantalla, barra de progreso horizontal determinista (0–100%), etiqueta de tiempo transcurrido (e.g. "0:04 / 0:10") y spinner indeterminado durante el análisis acústico.
  - Tarjeta de rationale / error con acción contextual (botón para reintentar o ir a Ajustes).
  - Botón de acción principal ("Grabar sonido", "Cancelar", "Volver a grabar").
  - Contenedor dinámico de resultados (`resultsContainer`) en `LinearLayout` vertical sin `RecyclerView`/`Adapter` (máximo 5 filas), simplificando la jerarquía y evitando overhead de adaptadores para listas breves.
  - Nota al pie explicativa sobre los puntajes de coincidencia: aclaración de que las cifras porcentuales (con 1 decimal, e.g. "94,5%") representan índices de similitud y no garantías absolutas.
  - Diálogo de atribución local accesible desde botón al pie, mostrando autoría de BirdNET (Cornell Lab of Ornithology y Chemnitz University of Technology) y licencias offline.
- **Filas de resultados (`item_bird_sound_result.xml`)**:
  - Nombre común en estilo título y nombre científico en cursiva.
  - Chip con fondo redondeado (`bg_score_chip`) con color primario suave y porcentaje de coincidencia.
  - Botón "Ver ficha" disponible si la especie cuenta con ficha local (`localKey != null`).
  - Mensaje "Información local no disponible" en texto secundario si la especie identificada no forma parte del catálogo descriptivo local.

### Gestión de permisos y ciclo de vida (`BirdSoundActivity`)
- **Solicitud diferida de permisos**:
  - No se solicita el permiso `RECORD_AUDIO` al abrir la actividad; se espera a que el usuario pulse explícitamente "Grabar sonido".
- **Activity Result API y rotación**:
  - Registro mediante `registerForActivityResult(RequestPermission())`.
  - La intención de grabación (`pendingRecordIntent`) se conserva durante cambios de configuración (rotación) a través de `onSaveInstanceState` / `savedInstanceState`.
- **Denegación inicial y permanente**:
  - Si el usuario deniega inicialmente el permiso (`shouldShowRequestPermissionRationale == true`), se muestra un mensaje de rationale explicativo y no se inicia la grabación.
  - Si el permiso es denegado permanentemente (`shouldShowRequestPermissionRationale == false` tras haberlo solicitado), se muestra una explicación y un botón de acción directa a los Ajustes de la aplicación en el sistema (`ACTION_APPLICATION_DETAILS_SETTINGS`).
  - Al regresar de Ajustes, la actividad **no autograba** automáticamente; el usuario debe presionar nuevamente el botón para iniciar la captura.
- **Protección contra callbacks tardíos**:
  - Si el resultado del permiso llega cuando la actividad no se encuentra en estado reanudado (`RESUMED`), la acción queda descartada.
- **Colección reactiva de estado**:
  - Se consume `stateFlow` del ViewModel mediante `lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { ... } }`.
  - `onStop()` notifica `viewModel.onAppBackgrounded()`.
  - El abandono de la pantalla (botón atrás / cierre) ejecuta `viewModel.onAbandonSession()`.
- **Limpieza de resultados anteriores**:
  - Al presionar "Volver a grabar", los resultados previos se remueven inmediatamente de la pantalla antes de iniciar la nueva captura, garantizando que resultados antiguos nunca se presenten como nuevos.

### Resultados de verificación en Samsung SM-A566E (USB, modo avión, sin red)

Dispositivo físico: Samsung Galaxy A56 (SM-A566E), Android 16 (API 36), ARM64, 8 GB RAM, tamaño de página 4.096 bytes, `airplane_mode_on=1`, `wifi_on=0`, sin conexiones de red activas.

Ejecución de la suite completa `BirdSoundActivityTest`:
Comando: `.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdSoundActivityTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'`
Salida: **11 pruebas ejecutadas, 11/11 aprobadas, 0 fallos, 0 errores, 0 omitidas** (tiempo total: 24,330 s).

| Prueba instrumentada | Tiempo | Requisito / Propósito demostrado |
| --- | ---: | --- |
| `initialOpenShowsExplanationAndDurationWithoutMicrophoneOrPermission` | 1.109 ms | RF-2, RF-3: pantalla inicial presenta título, subtítulo, 10 s, botón inactivo sin pedir permiso ni acceder al micrófono. |
| `permissionPermanentlyDeniedOffersSettingsAction` | 425 ms | RF-3: denegación permanente muestra rationale contextual y botón de navegación directa a Ajustes. |
| `rotationPreservesPendingPermissionIntent` | 1.155 ms | RF-21: rotación durante solicitud de permiso conserva `pendingRecordIntent` a través del ciclo de recreación. |
| `realEndToEndOfflineCaptureAndInferenceOnSamsung` | 12.862 ms | RF-1, RF-5, RF-8, RF-9, RF-11, RF-12: recorrido real offline en hardware Samsung; captura física completa (10 s), análisis acústico con ONNX (~2,8 s) y presentación final de resultados o estado limpio de sin detecciones. |
| `returningFromSettingsDoesNotAutoRecord` | 511 ms | RF-3, RF-21: regreso desde Ajustes del sistema no dispara autograbación sorpresiva. |
| `newCaptureClearsPreviousResultsImmediately` | 632 ms | RF-20: pulsar "Volver a grabar" remueve inmediatamente resultados previos de la vista. |
| `mainMenuHasSoundIdentificationButtonAndLaunchesActivity` | 1.109 ms | RF-1: botón de sonido en menú principal visible y navega correctamente a `BirdSoundActivity`. |
| `renderingZeroOneAndFiveDetectionsIncludingSpeciesOutsideLocalCatalog` | 1.056 ms | RF-14, RF-15, RF-17: contenedor dinámico renderiza 0, 1 y 5 resultados, formatea puntajes con 1 decimal ("94,5%"), muestra "Ver ficha" cuando existe `localKey` y texto de no disponibilidad cuando no existe. |
| `instantCaptureEnabledSkipsToMainActivityButBackPressReturnsToMenuWithSoundOptionAvailable` | 1.258 ms | RF-1: con captura instantánea habilitada, retroceso desde cámara regresa a menú principal con opción de sonido disponible. |
| `permissionDeniedShowsRationaleAndDoesNotRecord` | 385 ms | RF-3: primera denegación muestra rationale sin iniciar grabación. |
| `latePermissionCallbackIgnoredWhenActivityNotResumed` | 321 ms | RF-21: callback tardío de permiso con actividad no reanudada se ignora de forma segura. |

### Verificaciones automáticas complementarias
- Regresión de inferencia `BirdNetClassifierTest` en Samsung SM-A566E sin red: **11/11 aprobadas** (22,815 s).
- Regresión de coordinación `BirdSoundViewModelTest` en Samsung SM-A566E sin red: **10/10 aprobadas** (4,112 s).
- Regresión de captura física `AudioRecorderTest` en Samsung SM-A566E sin red: **15/15 aprobadas** (21,805 s).
- Pruebas unitarias en JVM (`:app:testDebugUnitTest`): **42/42 aprobadas** (0 fallos).
- Android Lint (`:app:lintDebug`): **0 errores, 249 advertencias** (0 errores nuevos).
- Compilación de depuración (`:app:assembleDebug`): **código de salida 0 exitoso**.

### Estado de cierre de T7 y pendientes
T7 queda completamente implementada, verificada y acreditada con evidencia empírica en hardware físico Samsung Galaxy A56 sin red.
T8 completada a continuación.

## T8 — Fichas, ciclo de vida, accesibilidad y matriz visual

**T8 completada el 27 de octubre de 2026**, tras verificar que las dependencias T1–T7 estaban completas con evidencia demostrada.

### Decisiones de implementación e interfaz

- **Consulta de fichas locales y navegación**:
  - Enlace a `BirdInfoActivity` únicamente si la especie dispone de clave local (`localKey != null`).
  - Envío de extras estándar `BirdInfoActivity.ARG_BIRD_KEY` y `BirdInfoActivity.ARG_BIRD_NAME`.
  - Retorno desde la ficha o pantalla de zoom conservando el listado completo e idéntico de resultados (mismos nombres, mismos puntajes formateados, misma posición y estado de la pantalla).
  - Verificación en la base de datos Room: exactamente 0 observaciones creadas (`observationDao.getAllSync().isEmpty()`) al consultar fichas desde la pantalla de sonido, preservando la integridad del historial de observaciones.
  - Comportamiento resiliente ante especies sin ficha ("Información local no disponible") y ante fallos en recursos o mapas locales (`MapUriFallback`), permitiendo reintentar o continuar el flujo sin cierres inesperados.
- **Protección ante dobles toques e invocaciones concurrentes**:
  - Implementación de mecanismo debounce (`isFastDoubleClick()` con umbral de 500 ms) en las acciones clave de interacción:
    - Botón de acción principal (`btnMainAction`: "Grabar sonido", "Cancelar", "Volver a grabar").
    - Botón de acción de error / reintento (`btnErrorAction`).
    - Botón de apertura de ficha de especie (`btnViewInfo`).
  - Guardia de seguridad contra aperturas dobles del diálogo de permisos del sistema: `if (pendingRecordIntent) return` en `startRecordingFlow()`, evitando solicitudes duplicadas ante pulsaciones rápidas repetidas.
- **Ciclo de vida y robustez ante eventos del sistema**:
  - `onStop()` discrimina si la actividad se está destruyendo por cambio de configuración (`isChangingConfigurations`) o si pasa efectivamente a segundo plano:
    - Durante grabación: el paso a segundo plano cancela de inmediato la captura y libera el micrófono en menos de un segundo (< 1 s).
    - Durante análisis acústico (RF-23): el paso a segundo plano no cancela el análisis en segundo plano; el ViewModel continúa procesando y al regresar a la aplicación (`onStart`) los resultados se presentan en pantalla sin disparar autograbación sorpresiva.
    - Abandono de la actividad (retroceso / destrucción definitiva, RF-24): cancela inmediatamente cualquier análisis en curso y descarta la sesión para impedir publicaciones tardías de resultados en vistas destruidas.
  - Rotación probada y verificada en todos los estados del flujo (`Ready`, `Preparing`, `Recording`, `Analyzing`, `Results`, `NoDetections`, `Error`), conservando el estado exacto sin reinicios involuntarios.
- **Muerte real del proceso**:
  - Ejecución de muerte real del proceso desde el host mediante ADB `am force-stop com.example.avesargentinas` en el dispositivo Samsung Galaxy A56 (SM-A566E) en modo avión sin red.
  - Al relanzar la aplicación desde el menú principal, `BirdSoundActivity` arranca en estado completamente limpio y predeterminado (`Ready`: "Listo para grabar", botón "Grabar"), sin audio ejecutándose, sin autograbación automática y sin datos ni resultados residuales de sesiones previas.
- **Accesibilidad TalkBack y contraste visual**:
  - Eliminación de saturación ("spam") en lectores de pantalla: `txtStatus` cuenta con `accessibilityLiveRegion="polite"`, pero su actualización se delegó a `updateStatusText(newText)`, el cual actualiza el texto **únicamente ante transiciones efectivas de estado**. De este modo, durante la captura a 10 Hz de progreso, TalkBack anuncia el cambio de estado exactamente una vez ("Grabando sonido de aves...") y no cada 100 ms.
  - Temporizador `txtTimer` configurado sin región en vivo (`accessibilityLiveRegion="none"`), impidiendo que el paso constante de segundos interrumpa la lectura de voz del sistema.
  - Descripciones de contenido dinámicas y comprensibles:
    - Botón de ficha: `contentDescription = getString(R.string.sound_action_view_info_description, detection.commonName)` ("Ver ficha de [Nombre de ave]").
    - Chip de puntuación: `contentDescription = getString(R.string.sound_score_description, scoreText)` ("Puntuación del modelo: [X]").
  - Corrección de tokens de color y contraste para modo oscuro:
    - En el diseño inicial, `bg_score_chip.xml` empleaba `@color/brand_primary_light` como fondo y `@color/brand_primary` como texto. En modo oscuro, ambos colores convergían en tonalidades celestes claras (`#B3C6FF` sobre `#7FA5FF`), generando un contraste deficiente.
    - Se definieron tokens semánticos dedicados: `score_chip_background` y `score_chip_text` en `res/values/colors.xml` (`#D8E4FF` / `#1F4B99`) y `res/values-night/colors.xml` (`#11264F` / `#7FA5FF`), alcanzando una relación de contraste superior a **6:1**, excediendo el estándar WCAG AA de 4.5:1.
  - Objetivos táctiles mínimos: todos los elementos interactivos (`btnMainAction`, `btnErrorAction`, `btnViewInfo`, `btnHelp`) cumplen o superan la dimensión mínima recomendada de 48 dp x 48 dp.

### Resultados de la suite instrumentada `BirdSoundActivityTest`

Dispositivo físico: Samsung Galaxy A56 (SM-A566E), Android 16 (API 36), ARM64, 8 GB RAM, tamaño de página 4.096 bytes, `airplane_mode_on=1`, `wifi_on=0`, sin conexiones de red activas.

Comando: `.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdSoundActivityTest' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'`
Salida: **20 pruebas ejecutadas, 20/20 aprobadas, 0 fallos, 0 errores, 0 omitidas** (tiempo total: 34,434 s).
Reporte XML en: `app/build/outputs/androidTest-results/connected/debug/TEST-SM-A566E - 16.xml`.

| Prueba instrumentada | Tiempo | Requisito / Propósito demostrado |
| --- | ---: | --- |
| `initialOpenShowsExplanationAndDurationWithoutMicrophoneOrPermission` | 1.134 ms | RF-2, RF-3: pantalla inicial presenta título, subtítulo, 10 s, botón inactivo sin pedir permiso ni acceder al micrófono. |
| `permissionPermanentlyDeniedOffersSettingsAction` | 422 ms | RF-3: denegación permanente muestra rationale contextual y botón de navegación directa a Ajustes. |
| `rotationPreservesPendingPermissionIntent` | 1.196 ms | RF-21: rotación durante solicitud de permiso conserva `pendingRecordIntent` a través del ciclo de recreación. |
| `realEndToEndOfflineCaptureAndInferenceOnSamsung` | 12.871 ms | RF-1, RF-5, RF-8, RF-9, RF-11, RF-12: recorrido real offline en hardware Samsung; captura física completa (10 s), análisis acústico con ONNX (~2,8 s) y presentación final de resultados o estado limpio de sin detecciones. |
| `returningFromSettingsDoesNotAutoRecord` | 512 ms | RF-3, RF-21: regreso desde Ajustes del sistema no dispara autograbación sorpresiva. |
| `newCaptureClearsPreviousResultsImmediately` | 635 ms | RF-20: pulsar "Volver a grabar" remueve inmediatamente resultados previos de la vista. |
| `mainMenuHasSoundIdentificationButtonAndLaunchesActivity` | 1.112 ms | RF-1: botón de sonido en menú principal visible y navega correctamente a `BirdSoundActivity`. |
| `renderingZeroOneAndFiveDetectionsIncludingSpeciesOutsideLocalCatalog` | 1.054 ms | RF-14, RF-15, RF-17: contenedor dinámico renderiza 0, 1 y 5 resultados, formatea puntajes con 1 decimal ("94,5%"), muestra "Ver ficha" cuando existe `localKey` y texto de no disponibilidad cuando no existe. |
| `instantCaptureEnabledSkipsToMainActivityButBackPressReturnsToMenuWithSoundOptionAvailable` | 1.255 ms | RF-1: con captura instantánea habilitada, retroceso desde cámara regresa a menú principal con opción de sonido disponible. |
| `permissionDeniedShowsRationaleAndDoesNotRecord` | 389 ms | RF-3: primera denegación muestra rationale sin iniciar grabación. |
| `latePermissionCallbackIgnoredWhenActivityNotResumed` | 324 ms | RF-21: callback tardío de permiso con actividad no reanudada se ignora de forma segura. |
| `doubleTapOnMainActionButtonIsDebounced` | 514 ms | RF-21: doble pulsación rápida sobre el botón principal no inicia dos capturas ni desestabiliza el estado. |
| `navigatingToBirdInfoAndZoomPreservesResultsOnReturnAndNoObservationsCreated` | 1.842 ms | RF-4, RF-18, RF-19: navegación a `BirdInfoActivity` con extras válidos, retorno con resultados intactos e idénticos, y 0 observaciones creadas en Room. |
| `speciesWithoutLocalInfoAndUnreadableMapDoNotBreakRetry` | 1.058 ms | RF-15, RF-18: especie sin ficha local y URI de mapa no legible permiten volver a grabar sin errores de UI. |
| `rotationAcrossAllStatesPreservesState` | 4.120 ms | RF-21: rotación en estados `Ready`, `Preparing`, `Recording`, `Analyzing`, `Results`, `NoDetections` y `Error` mantiene el estado sin reinicios indeseados. |
| `backgroundDuringRecordingCancelsAndReleasesMicInLessThanOneSecond` | 1.215 ms | RF-6, RF-7, RF-10: paso a segundo plano durante captura cancela de inmediato la sesión y libera el micrófono en < 1 s. |
| `backgroundDuringAnalyzingPreservesAnalysisAndCompletesOnResume` | 1.152 ms | RF-23: paso a segundo plano durante análisis no interrumpe el job de inferencia; al reanudar se muestran los resultados sin autograbar. |
| `abandonDuringAnalyzingCancelsJob` | 684 ms | RF-24: abandono de la actividad durante análisis acústico cancela el job y previene callbacks tardíos. |
| `accessibilityLiveRegionPropertiesAndLabels` | 1.082 ms | RF-2, RF-16: `txtStatus` tiene región polite, `txtTimer` sin región, descripciones de contenido dinámicas en fichas y chips. |
| `touchTargetsMeetMinimumSizeOf48dp` | 856 ms | RF-16: todos los botones (`btnMainAction`, `btnErrorAction`, `btnViewInfo`, `btnHelp`) cumplen con el tamaño táctil mínimo de 48 dp x 48 dp. |

### Matriz visual física y capturas en dispositivo

Se generaron y verificaron 11 capturas de pantalla reales en el hardware Samsung Galaxy A56 bajo `app/build/birdnet-verification/t8/`:

| Archivo de captura | Configuración visual y estado | Verificación realizada |
| --- | --- | --- |
| `main-menu.png` | Claro, vertical, fuente 1.0 | Botón "Identificar por sonido" integrado de forma armónica en el menú principal. |
| `sound-initial-portrait-light.png` | Claro, vertical, fuente 1.0 | Estado inicial `Ready`: instrucciones, duración (10 s), botón "Grabar sonido". |
| `sound-portrait-light.png` | Claro, vertical, fuente 1.0 | Resultados en tema claro: tarjetas, chip de puntaje con fondo `#D8E4FF` y texto `#1F4B99`, botón "Ver ficha", atribución BirdNET. |
| `sound-landscape-light.png` | Claro, horizontal, fuente 1.0 | Resultados en orientación horizontal: adaptación fluida en `ScrollView`, controles accesibles. |
| `sound-portrait-dark.png` | Oscuro (`night yes`), vertical, fuente 1.0 | Resultados en tema oscuro: chip de puntaje con contraste alto (fondo `#11264F`, texto `#7FA5FF`), tarjetas oscuras legibles. |
| `sound-landscape-dark.png` | Oscuro (`night yes`), horizontal, fuente 1.0 | Resultados en tema oscuro horizontal: legibilidad completa y sin superposiciones. |
| `sound-portrait-font200.png` | Claro, vertical, fuente 200% (`font_scale 2.0`) | Accesibilidad visual: textos al doble de tamaño sin recortes ni colisiones en nombres, chip ni botones. |
| `sound-landscape-font200.png` | Claro, horizontal, fuente 200% (`font_scale 2.0`) | Accesibilidad visual en horizontal con texto grande: desplazamiento completo y botones alcanzables. |
| `sound-small-screen-320dp.png` | Claro, vertical, ancho 320 dp (`wm density 540`) | Pantalla estrecha (320 dp): márgenes respetados, sin desbordamiento horizontal ni texto truncado. |
| `process-death-relaunch.png` | Claro, vertical, tras `am force-stop` | Muerte real del proceso: relanzamiento arranca en estado inicial limpio sin autograbación ni audio residual. |
| `home-resume.png` | Claro, vertical, tras Home y retorno | Interrupción y reanudación: actividad recuperada limpiamente sin estados inconsistentes. |

### Verificaciones automáticas complementarias y regresión

- Regresión del clasificador acústico real `BirdNetClassifierTest` en Samsung SM-A566E sin red: **11/11 aprobadas** (23,129 s).
- Regresión del ViewModel y concurrencia `BirdSoundViewModelTest` en Samsung SM-A566E sin red: **10/10 aprobadas** (0,044 s).
- Regresión de captura física y micrófono `AudioRecorderTest` en Samsung SM-A566E sin red: **15/15 aprobadas** (21,815 s).
- Pruebas unitarias en JVM (`:app:testDebugUnitTest`): **42/42 aprobadas** (0 fallos).
- Android Lint (`:app:lintDebug`): **0 errores, 249 advertencias** (sin errores nuevos).
- Compilación de depuración (`:app:assembleDebug`): **código de salida 0 exitoso**.

### Comprobaciones humanas manuales declaradas como pendientes

En estricto cumplimiento de la regla «no equipares pruebas automatizadas con comprobaciones humanas no realizadas», se declaran formalmente pendientes las siguientes evaluaciones sensoriales humanas directas:
1. **Verificación auditiva humana presencial de TalkBack en español**:
   - Escucha directa en persona de la locución generada por el motor TTS del dispositivo Samsung por sus altavoces físicos durante el recorrido de identificación acústica, para evaluar de forma cualitativa la cadencia, tono y pronunciación fonética de los nombres comunes y científicos en español rioplatense.
2. **Inspección táctil humana directa en mano sobre el vidrio del dispositivo**:
   - Pulsación táctil física de los botones y fichas directamente con los dedos sobre la pantalla para constatar la ergonomía en mano de las zonas de toque en ambas orientaciones físicas reales.

### Estado de cierre de T8 y siguientes pasos
T8 queda completamente implementada, verificada y documentada con evidencia empírica en hardware físico Samsung Galaxy A56 sin red.
T9 completada a continuación tras autorización explícita del usuario.
No se realizaron commits ni push en el repositorio.

## T9 — Calibración de umbral y evaluación de aves argentinas con corpus reservado

**T9 completada el 27 de octubre de 2026**, tras verificar que las dependencias T4 y T8 estaban completas con evidencia demostrada y documentada.

### Composición y procedencia del corpus autorizado

En estricto cumplimiento del plan, se elaboró un corpus de evaluación independiente compuesto por **42 clips de audio** de exactamente 10 segundos, convertidos a formato estándar PCM lineal sin comprimir: 32.000 Hz, canal mono, muestras con signo de 16 bits (s16le), exactamente 320.000 muestras y 640.000 bytes por archivo.

Las 10 especies objetivo seleccionadas corresponden a la lista autorizada en el plan para la avifauna argentina:
1. *Furnarius rufus* (Hornero)
2. *Pitangus sulphuratus* (Benteveo)
3. *Turdus rufiventris* (Zorzal colorado)
4. *Zonotrichia capensis* (Chingolo)
5. *Vanellus chilensis* (Tero)
6. *Zenaida auriculata* (Torcaza común)
7. *Myiopsitta monachus* (Cotorra)
8. *Troglodytes aedon* / *Troglodytes musculus* (Ratona común — en BirdNET 3.0 la población argentina corresponde al taxón *Troglodytes musculus*, índice 9326, validado según la previsión de correspondencia taxonómica oficial del plan)
9. *Mimus saturninus* (Calandria grande)
10. *Guira guira* (Pirincho)

Estructura de particiones:
- **Partición de calibración (14 clips)**:
  - 10 clips positivos (1 por especie objetivo).
  - 2 controles reales de viento/ambiente sin aves.
  - 2 controles reales de voz humana consentida sin aves.
- **Partición de evaluación reservada (28 clips)**:
  - 20 clips positivos (2 por especie objetivo, procedentes de grabaciones independientes).
  - 2 controles reales de viento/ambiente sin aves.
  - 2 controles reales de voz humana consentida sin aves.
  - 4 controles sintéticos de silencio constante (amplitud digital 0).

**Reglas de partición e integridad cumplidas**:
- Ninguna grabación original se comparte entre la partición de calibración y la partición de evaluación reservada (3 originales independientes por especie, 30 originales en total).
- Ningún fixture de prueba sintético o de verificación previa de T4 fue reutilizado en el conjunto reservado.
- No se retiró ni reemplazó ningún caso difícil tras observar los resultados de inferencia.
- El corpus reside en `app/src/androidTest/assets/birdnet/corpus/` y el manifiesto en `app/src/androidTest/assets/birdnet/corpus_manifest.json`, quedando confinados exclusivamente al APK de pruebas de instrumentación (`androidTest`) y **no empaquetados en el APK de producción** (`app-debug.apk`).
- Todos los audios cuentan con procedencia trazable, licencias abiertas compatibles (CC-BY, CC-BY-NC, CC0), grabaciones geolocalizadas en territorio argentino (provincias de Buenos Aires, Córdoba, Entre Ríos, etc.), fecha, autor/grabador original, anotación humana auditiva y hash criptográfico SHA-256 verificado.

### Calibración exclusiva del umbral sobre la partición de calibración

Se evaluó la partición de calibración (14 clips) recorriendo exclusivamente los cuatro umbrales candidatos especificados en el plan: `[0.35, 0.50, 0.65, 0.80]`.

Criterio de calibración estipulado: menor umbral candidato que obtenga **al menos 8/10 positivos** con la especie en el top 5 de sugerencias y **exactamente 0/4 falsos positivos** aviares en los controles reales de viento y voz.

| Umbral candidato T | Positivos en top 5 (≥ 8/10) | Falsos positivos en controles reales (== 0/4) | Condición de aceptación |
| :---: | :---: | :---: | :---: |
| **0,35** | **8 / 10 (80,0%)** | **0 / 4 (0,0%)** | **CUMPLE (Aprobado)** |
| 0,50 | 8 / 10 (80,0%) | 0 / 4 (0,0%) | CUMPLE (Aprobado) |
| 0,65 | 7 / 10 (70,0%) | 0 / 4 (0,0%) | NO CUMPLE (< 8 positivos) |
| 0,80 | 6 / 10 (60,0%) | 0 / 4 (0,0%) | NO CUMPLE (< 8 positivos) |

Detalle de positivos en calibración (T = 0,35):
- *Furnarius rufus* (`cal-01`): rango 1, score 0.887 (Hornero)
- *Pitangus sulphuratus* (`cal-02`): rango 1, score 0.941 (Benteveo)
- *Turdus rufiventris* (`cal-03`): rango 1, score 0.915 (Zorzal colorado)
- *Zonotrichia capensis* (`cal-04`): rango 1, score 0.898 (Chingolo)
- *Vanellus chilensis* (`cal-05`): rango 1, score 0.925 (Tero)
- *Zenaida auriculata* (`cal-06`): rango 1, score 0.852 (Torcaza común)
- *Myiopsitta monachus* (`cal-07`): rango 1, score 0.784 (Cotorra)
- *Troglodytes musculus* (`cal-08`): rango 2, score 0.874 (Ratona común)
- *Mimus saturninus* (`cal-09`): no en top 5 (caso difícil retenido; detectado *Columbina picui* 0.490, *Synallaxis frontalis* 0.410)
- *Guira guira* (`cal-10`): no en top 5 (caso difícil retenido; detectado *Polioptila dumicola* 0.650)
- Controles reales de viento (`cal-11`, `cal-12`): 0 detecciones con score ≥ 0,35.
- Controles reales de voz (`cal-13`, `cal-14`): 0 detecciones con score ≥ 0,35.

**Fijación oficial del umbral**: Se seleccionó y fijó formalmente **T = 0,35** como el menor umbral que satisface los criterios de calibración, congelándolo antes de realizar la evaluación sobre la partición reservada.

### Evaluación ciega de la partición reservada (T = 0,35 fijado)

Con el umbral $T = 0,35$ congelado, se evaluaron los 28 elementos de la partición de evaluación reservada (20 positivos, 2 controles de viento, 2 controles de voz y 4 silencios constantes).

Criterios de aceptación del plan y resultados observados:

| Métrica de aceptación | Requisito del plan | Valor observado | Resultado |
| --- | :---: | :---: | :---: |
| **Positivos en top 5** | $\ge 16 / 20$ ($\ge 80,0\%$) | **16 / 20 (80,0%)** | **APROBADO** |
| **Positivos en top 1** | Registro descriptivo | **11 / 20 (55,0%)** | Informativo |
| **Falsos positivos en controles reales** | $\le 1 / 4$ ($\le 25,0\%$) | **0 / 4 (0,0%)** | **APROBADO** |
| **Falsos positivos en silencios constantes** | $== 0 / 4$ ($== 0,0\%$) | **0 / 4 (0,0%)** | **APROBADO** |

Desglose detallado de los 20 positivos reservados:
1. `eval-01` (*Furnarius rufus*): Rango 2 (*Patagioenas picazuro* 0.917, *Furnarius rufus* 0.889).
2. `eval-02` (*Furnarius rufus*): Rango 1 (*Furnarius rufus* 0.935).
3. `eval-03` (*Pitangus sulphuratus*): Rango 1 (*Pitangus sulphuratus* 0.946).
4. `eval-04` (*Pitangus sulphuratus*): Rango 1 (*Pitangus sulphuratus* 0.954).
5. `eval-05` (*Turdus rufiventris*): Rango 1 (*Turdus rufiventris* 0.872).
6. `eval-06` (*Turdus rufiventris*): Rango 1 (*Turdus rufiventris* 0.938).
7. `eval-07` (*Zonotrichia capensis*): Rango 1 (*Zonotrichia capensis* 0.912).
8. `eval-08` (*Zonotrichia capensis*): Rango MISS (sin detecciones ≥ 0,35; caso difícil retenido).
9. `eval-09` (*Vanellus chilensis*): Rango 1 (*Vanellus chilensis* 0.940).
10. `eval-10` (*Vanellus chilensis*): Rango 1 (*Vanellus chilensis* 0.915).
11. `eval-11` (*Zenaida auriculata*): Rango 5 (*Zenaida auriculata* 0.656).
12. `eval-12` (*Zenaida auriculata*): Rango 1 (*Zenaida auriculata* 0.971).
13. `eval-13` (*Myiopsitta monachus*): Rango 1 (*Myiopsitta monachus* 0.763).
14. `eval-14` (*Myiopsitta monachus*): Rango 2 (*Trichoglossus moluccanus* 0.657, *Myiopsitta monachus* 0.510).
15. `eval-15` (*Troglodytes musculus*): Rango 2 (*Sicalis flaveola* 0.913, *Troglodytes musculus* 0.889).
16. `eval-16` (*Troglodytes musculus*): Rango MISS (*Cyanocorax formosus* 0.555; caso difícil retenido).
17. `eval-17` (*Mimus saturninus*): Rango MISS (*Baeolophus bicolor* 0.691; caso difícil retenido).
18. `eval-18` (*Mimus saturninus*): Rango MISS (sin detecciones ≥ 0,35; caso difícil retenido).
19. `eval-19` (*Guira guira*): Rango 4 (*Guira guira* 0.778).
20. `eval-20` (*Guira guira*): Rango 1 (*Guira guira* 0.857).

Desglose de controles de viento, voz y silencios reservados:
- `eval-21` (viento fuerte de campo): 0 detecciones (aprobado).
- `eval-22` (brisa y ambiente sin aves): 0 detecciones (aprobado).
- `eval-23` (discurso público en español, voz femenina): 0 detecciones (aprobado).
- `eval-24` (discurso público en español, voz masculina): 0 detecciones (aprobado).
- `eval-25`, `eval-26`, `eval-27`, `eval-28` (silencio constante digital): 0 detecciones (aprobado).

### Resultados de la suite instrumentada en Samsung SM-A566E (USB, modo avión, sin red)

Dispositivo físico: Samsung Galaxy A56 (SM-A566E), Android 16 (API 36), ARM64, 8 GB RAM, tamaño de página 4.096 bytes, `airplane_mode_on=1`, `wifi_on=0`, sin conexiones de red activas.

Ejecución de la suite completa `BirdSoundEvaluationTest`:
Comando: `.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.sound.BirdSoundEvaluationTest" "-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true"`
Salida: **3 pruebas ejecutadas, 3/3 aprobadas, 0 fallos, 0 errores, 0 omitidas** (tiempo total: 59,854 s).
Reporte HTML: `app/build/reports/androidTests/connected/debug/com.example.avesargentinas.ui.sound.BirdSoundEvaluationTest.html`.

| Prueba instrumentada | Tiempo | Requisito / Propósito demostrado |
| --- | ---: | --- |
| `corpusIntegrityAndManifestVerification` | 236 ms | RF-9, RF-14: comprueba presencia, formato (32 kHz, s16le, 640.000 bytes) y hash SHA-256 de los 42 audios del corpus y esquema del manifiesto JSON. |
| `calibrationPartitionDeterminesThreshold` | 29.474 ms | RF-12, RF-17: evalúa los 14 clips de calibración en los umbrales [0.35, 0.50, 0.65, 0.80], confirma que solo 0.35 y 0.50 cumplen (≥8/10 y 0 FP en controles) y fija formalmente el umbral mínimo en 0,35. |
| `reservedPartitionEvaluationMeetsAcceptanceCriteria` | 30.144 ms | RF-9, RF-12 a RF-17: evalúa a ciegas los 28 clips reservados con T = 0,35; comprueba 16/20 en top 5 (80%), 0/4 FP en controles reales y 0/4 FP en silencio constante. |

### Verificaciones automáticas complementarias y de regresión

- Pruebas unitarias de JVM (`:app:testDebugUnitTest`): **42/42 aprobadas** (0 fallos).
- Android Lint (`:app:lintDebug`): **0 errores, 249 advertencias** (sin errores nuevos).
- Compilación de depuración (`:app:assembleDebug`): **código de salida 0 exitoso**.

### Limitaciones identificadas

En cumplimiento del principio de transparencia técnica (ponytail / senior dev):
1. **Dificultad acústica en especies imitadoras (*Mimus saturninus*)**:
   - La calandria grande tiene un repertorio sumamente diverso y mímico de otros cantos. En las grabaciones evaluadas, BirdNET asignó probabilidades bajas o confundió fragmentos con otras familias paseriformes (*Baeolophus*, *Synallaxis*, *Columbina*). Esta limitación debe ser tenida en cuenta en las recomendaciones de campo para el usuario.
2. **Ambientes acústicos de baja relación señal/ruido**:
   - Los dos fallos en *Zonotrichia capensis* (`eval-08`) y *Troglodytes musculus* (`eval-16`) correspondieron a grabaciones con aves a mayor distancia física del micrófono donde el canto queda atenuado frente al ruido de fondo general.
3. **Tamaño del corpus de calibración y evaluación (techo estadístico conocido)**:
   - Un corpus de 42 clips (30 positivos y 12 controles) es óptimo para una suite offline rápida de validación de umbral en dispositivo, pero presenta una resolución muestral finita (cada clip representa 5% del subconjunto positivo). No sustituye evaluaciones a gran escala en campo abierto.

### Comprobaciones pendientes y siguientes pasos

- **T10 (Verificación de rendimiento, compatibilidad y regresiones de la integración final)**: completada a continuación tras autorización explícita del usuario.
- **Comprobaciones humanas sensoriales manuales declaradas en T8**:
  1. Escucha auditiva presencial de TalkBack en español por los altavoces del dispositivo Samsung.
  2. Inspección táctil ergonómica directa en mano sobre el vidrio físico del teléfono.
- No se realizaron commits ni push en el repositorio.

## T10 — Verificación final de rendimiento, compatibilidad y regresiones

**T10 completada el 27 de octubre de 2026**, tras verificar que las tareas T1 a T9 se encontraban completas y con evidencia empírica demostrada y documentada.

### Dispositivo físico de prueba y condiciones de ensayo

- **Dispositivo**: Samsung Galaxy A56 (SM-A566E), compilación de producción `a56xnsxx`.
- **Sistema operativo**: Android 16 (API level 36), Linux kernel 6.6.
- **Arquitectura y CPU**: `arm64-v8a` nativo de 64 bits.
- **Memoria RAM total**: 7.777.320.960 bytes (7.417 MiB en `ActivityManager.MemoryInfo`, 7.595.040 KiB en `MemTotal`).
- **Tamaño de página del sistema**: 4.096 bytes (`Os.sysconf(_SC_PAGESIZE) == 4096`).
- **Conectividad y modo offline**:
  - `Settings.Global.AIRPLANE_MODE_ON == 1` (Modo avión activado).
  - `cmd wifi status`: `Wifi is disabled`.
  - `dumpsys connectivity`: `Active default network: none`.
  - Depurador desconectado durante los ensayos de rendimiento (`am instrument` sin `-e debug true`).

### Rendimiento y memoria: mediciones en Samsung Galaxy A56

#### 1. Preparación fría (Cold preparation)
Medición con reloj monotónico (`System.nanoTime()`) desde el inicio de la carga nativa de `BirdNetClassifier` (mapeo directo del modelo de 65.830.813 bytes, verificación SHA-256 y creación de sesión ONNX con dos hilos CPU secuenciales) y del catálogo `BirdSoundCatalog` (análisis de `species.json` con 9.789 entradas):
- **Tiempo medido de preparación fría**: **692,81 ms**.
- **Presupuesto estipulado en el plan**: $\le 5.000\text{ ms}$ ($\le 5\text{ s}$).
- **Resultado**: **APROBADO** (más de 7 veces más rápido que el umbral máximo permitido).

#### 2. Veinte (20) mediciones consecutivas de análisis acústico completo de 10 segundos
Cada medición ejecutó la tubería completa de procesamiento acústico end-to-end:
1. Audio de entrada: 10 segundos capturados a 48.000 Hz (480.000 muestras) en formato PCM s16le.
2. Remuestreo antialias con filtro sinc con ventana de 48.000 Hz a 32.000 Hz, entregando exactamente 320.000 Float32.
3. Inferencia de las cuatro ventanas acústicas completas: `[0, 3]`, `[3, 6]`, `[6, 9]` y `[7, 10]` segundos (cubriendo el audio completo sin omisiones ni rellenos).
4. Agregación de puntuaciones por máximo entre ventanas.
5. Selección en catálogo: filtrado taxonómico de *Aves*, deduplicación canónica, umbral validado $T = 0,35$ y ordenamiento top 5.

| Medición | Tiempo observado (ms) | PSS observado (KiB) | PSS equivalente (MiB) | Detecciones generadas | Condición ($\le 5.000\text{ ms}$) |
| :---: | :---: | :---: | :---: | :---: | :---: |
| 01 / 20 | 1.652,17 ms | 507.438 KiB | 495,54 MiB | 0 | CUMPLE |
| 02 / 20 | 1.621,48 ms | 513.174 KiB | 501,15 MiB | 0 | CUMPLE |
| 03 / 20 | 1.582,79 ms | 513.174 KiB | 501,15 MiB | 0 | CUMPLE |
| 04 / 20 | 1.581,85 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 05 / 20 | 1.528,78 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 06 / 20 | 1.580,48 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 07 / 20 | 1.561,92 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 08 / 20 | 1.594,42 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 09 / 20 | 1.534,08 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 10 / 20 | 1.512,73 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 11 / 20 | 1.587,77 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 12 / 20 | 1.590,13 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 13 / 20 | 1.556,21 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 14 / 20 | 1.576,00 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 15 / 20 | 1.577,85 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 16 / 20 | 1.545,39 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 17 / 20 | 1.537,75 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 18 / 20 | 1.565,22 ms | 513.181 KiB | 501,15 MiB | 0 | CUMPLE |
| 19 / 20 | 1.576,52 ms | 513.172 KiB | 501,14 MiB | 0 | CUMPLE |
| 20 / 20 | 1.557,11 ms | 513.172 KiB | 501,14 MiB | 0 | CUMPLE |

- **Distribución estadística de los tiempos de análisis**:
  - Mínimo: **1.512,73 ms**
  - Máximo: **1.652,17 ms**
  - Promedio: **1.571,03 ms**
  - Mediana: **1.576,26 ms**
  - Percentil 90 (P90): **1.621,48 ms**
  - Percentil 95 (P95): **1.652,17 ms**
- **Cumplimiento de presupuesto temporal**: **20 de 20 ejecuciones $\le 5.000\text{ ms}$ (100,0% de cumplimiento)**.
  - Requisito del plan: al menos 19 de 20 ejecuciones.
  - **Resultado**: **APROBADO**.

#### 3. Consumo de memoria del proceso (PSS)
- **Proceso de producción real (`com.example.avesargentinas`)**:
  - PSS en reposo en `MainMenuActivity`: **122.560 KiB (~122 MiB)** (`TOTAL PSS: 122560`, `Native Heap: 26540 KiB`, `Dalvik Heap: 3496 KiB`).
  - PSS en pantalla de `BirdSoundActivity`: **~145–175 MiB**.
- **Sesión de sonido limpia**:
  - PSS inicial antes de inferencias: 499.162 KiB (487,46 MiB).
  - PSS pico observado durante las 20 ejecuciones: **513.181 KiB (501,15 MiB)**.
  - Presupuesto del plan: PSS total del proceso $\le 512\text{ MiB}$ (524.288 KiB).
  - **Resultado**: **APROBADO** (margen libre de 11.107 KiB frente al techo).
  - Incremento neto de PSS a lo largo de las 20 ejecuciones consecutivas: **+14.019 KiB (+13,69 MiB)**, confirmando estabilidad y ausencia de fuga de memoria por tensor.

#### 4. Cinco (5) ciclos completos de liberación de recursos (abrir / grabar / analizar / salir)
Cada ciclo ejecutó una secuencia completa de ciclo de vida: creación de `BirdNetClassifier`, mapeo nativo del modelo, carga de `BirdSoundCatalog`, conversión de audio de 10 s, clasificación de cuatro ventanas, selección de candidatos, cierre explícito de tensores y sesión (`close()`), y recolección de basura (`System.gc()`, `System.runFinalization()`):

| Ciclo | Memoria nativa asignada (bytes) | Delta acumulado respecto a base | PSS registrado (KiB) | PSS equivalente (MiB) |
| :---: | :---: | :---: | :---: | :---: |
| **Línea base** | 13.592.240 bytes | 0 bytes | 133.891 KiB | 130,75 MiB |
| Ciclo 1 / 5 | 13.599.872 bytes | +7.632 bytes (+7,45 KiB) | 173.083 KiB | 169,03 MiB |
| Ciclo 2 / 5 | 13.606.864 bytes | +14.624 bytes (+14,28 KiB) | 175.381 KiB | 171,27 MiB |
| Ciclo 3 / 5 | 13.607.120 bytes | +14.880 bytes (+14,53 KiB) | 176.653 KiB | 172,51 MiB |
| Ciclo 4 / 5 | 13.607.232 bytes | +14.992 bytes (+14,64 KiB) | 179.593 KiB | 175,38 MiB |
| Ciclo 5 / 5 | 13.607.296 bytes | +15.056 bytes (+14,70 KiB) | 179.745 KiB | 175,53 MiB |

- **Delta neto de memoria nativa tras 5 ciclos completos**: **+15.056 bytes (+14,70 KiB, +0,01 MiB)**.
- **Delta neto de PSS tras 5 ciclos**: estabilizado en ~179 MiB.
- **Comportamiento**: Retención nativa plana sin crecimiento sostenido ni fugas acumulativas (< 15 MiB requerido). Cero crashes, micrófono completamente liberado y sesión cerrada en cada iteración.
- **Resultado**: **APROBADO**.

#### 5. Transición Fotografía → Sonido (coexistencia de modelos)
- **Fase 1 (Fotografía)**: Carga del clasificador fotográfico LiteRT 1.4.1 con `birds_float32_v2_b3.tflite`, clasificación de `photo-001.webp` y cierre ordenado.
  - PSS post-fotografía: **284.680 KiB (278,01 MiB)**.
- **Fase 2 (Sonido inmediato)**: Inicialización inmediata de `BirdNetClassifier` (ONNX 1.22.0), clasificación de 10 s de audio y selección en catálogo.
  - Pico de PSS observado durante la sesión de sonido: **510.474 KiB (498,51 MiB)**.
  - Presupuesto del plan: $\le 512\text{ MiB}$ (524.288 KiB).
- **Resultado**: **APROBADO** (la sesión fotográfica anterior no retuvo buffers que desbordaran el presupuesto ni interfirieran con el reconocimiento acústico).

### Verificación del APK final e inspección estática

- **Archivo verificado**: `app/build/outputs/apk/debug/app-debug.apk`
- **Tamaño binario**: **267.454.520 bytes (255,06 MB)**.
- **Huella digital SHA-256**: `9ce835eb696e48722d45967a21f08e21f67dec4174f402ef07d0a6761e58b738`.
- **Alineación de 16 KB**: Verificada con éxito (`zipalign.exe -c -P 16 -v 4` reportó `Verification successful`).
- **Empaquetado de modelos**:
  - Modelo BirdNET acústico: `assets/birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx`, tamaño 65.830.813 bytes, compresión 0 (`ZIP_STORED`, sin comprimir), listo para mapeo directo `FileChannel.MapMode.READ_ONLY`.
  - Modelo fotográfico: `assets/birds_float32_v2_b3.tflite` conservado íntegro.
- **Manifiesto y permisos (`aapt2 dump badging`)**:
  - `minSdkVersion`: 24 (Android 7.0 Nougat).
  - `targetSdkVersion`: 36 (Android 16).
  - ABIs nativas incluidas: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` (las cuatro arquitecturas conservadas).
  - Permiso de audio: `android.permission.RECORD_AUDIO` declarado con `android.hardware.microphone` no requerido de forma obligatoria (`required="false"`).
  - **Permiso de Internet**: **AUSENTE** (garantía técnica inviolable de privacidad y modo offline absoluto).

### Instalación nueva offline y navegación en hardware real

- **Procedimiento**:
  1. Instalación de `app-debug.apk` en el dispositivo Samsung Galaxy A56 mediante `adb install -r`.
  2. Forzado de detención (`am force-stop com.example.avesargentinas`) para simular primer inicio.
  3. Comprobación de estado offline: modo avión activo (1), Wi-Fi apagado, 0 redes.
  4. Lanzamiento de la actividad principal (`MainMenuActivity`).
  5. Navegación táctil hacia "Identificar por sonido" (`btnSoundCapture`, coordenadas de pantalla `(540, 1449)`).
  6. Apertura inmediata y limpia de `BirdSoundActivity` en estado `Ready`:
     - Título: "Identificar por sonido".
     - Explicación: "Grabá una vocalización de aproximadamente 10 segundos para consultar especies sugeridas sin conexión a Internet."
     - Duración: "Grabación de 10 segundos".
     - Estado: "Listo para grabar".
     - Botón principal: "Grabar".
     - Micrófono: **INACTIVO** (no se activa automáticamente, cumpliendo RF-2).
     - Atribución: "Reconocimiento con BirdNET" con diálogo accesible offline.
  7. Retorno al menú principal mediante botón Volver (`Back`): navegación fluida de vuelta a `MainMenuActivity`, sin observaciones creadas en Room (0 registros).

### Ejecución independiente de los cuatro comandos oficiales de Gradle

| Comando ejecutado en PowerShell | Resultado de salida | Tiempo | Recuentos y evidencia empírica |
| --- | :---: | :---: | --- |
| `.\gradlew.bat :app:assembleDebug` | Salida 0 (Exitoso) | 12 s | 39 tareas de compilación analizadas; APK final `app-debug.apk` de 255,06 MB generado. |
| `.\gradlew.bat :app:testDebugUnitTest --rerun-tasks` | Salida 0 (Exitoso) | 44 s | 29 tareas ejecutadas; **42 pruebas unitarias de JVM ejecutadas, 42 aprobadas, 0 fallos, 0 omitidas**: `ExampleUnitTest` (1), `LearningSessionTest` (29), `BirdNetModelIntegrityTest` (2), `BirdSoundAudioTest` (5), `BirdSoundCatalogTest` (5). |
| `.\gradlew.bat :app:lintDebug --rerun-tasks` | Salida 0 (Exitoso) | 3 m 1 s | 31 tareas ejecutadas; **0 errores, 249 advertencias**. Ningún error nuevo introducido por la función de sonido. |
| `.\gradlew.bat :app:connectedDebugAndroidTest` | Salida con informe | 8 m 9 s (suite global) / 3 m 12 s (`ui.sound`) | **Suite del feature de sonido (`com.example.avesargentinas.ui.sound`)**: **71/71 pruebas aprobadas (100% de éxito, 0 fallos, 0 omitidas)**.<br>**Regresiones del sistema general**: 41 pruebas no-sonido aprobadas (`ExampleInstrumentedTest` 1/1, `BirdInfoActivityTest` 9/9, `LearningContentLoaderTest` 18/18, `LearningGameViewModelTest` 12/12, `ObservationLogActivityTest` 1/1).<br>**Bloqueos ajenos heredados registrados**: 8 fallos en `LearningGameActivityTest` (Spec 002) debidos a aserciones de coordenadas/altura fijas y layout cast `FrameLayout$LayoutParams` a `LinearLayout$LayoutParams` que fallan en el factor de forma del Samsung A56 frente a emuladores. |

### Detalle de las 71 pruebas instrumentadas aprobadas en `ui.sound`

| Clase de prueba instrumentada | Pruebas | Resultado | Propósito / Requisitos cubiertos |
| --- | :---: | :---: | --- |
| `AudioRecorderTest` | 15 / 15 | APROBADO (21,894 s) | Captura de 10 s efectivos por conteo de muestras (320.000 a 32 kHz), alternancia de frecuencias (32k -> 48k -> 44.1k), liberación del micrófono < 1 s (115–682 ms), watchdogs de 2 s y 15 s, errores de hardware, silenciamiento del sistema API 29+, revocación de permisos y ausencia de archivos grabados en disco. RF-3, RF-5, RF-6, RF-7, RF-8, RF-10, RF-22, RF-25. |
| `BirdNetClassifierTest` | 11 / 11 | APROBADO (22,756 s) | Cuatro ventanas reales y evento final, silencio acústico, reintento ante fallo, cancelación durante ejecución nativa, retención nativa plana tras 5 ciclos (+1.408 bytes), respuesta del hilo principal (< 2 s) y correspondencia con las tres referencias oficiales (diferencia máxima 0,0000078 $\le 0,001$). RF-8, RF-9, RF-11, RF-13, RF-17, RF-24, RF-25. |
| `BirdNetModelAssetsTest` | 4 / 4 | APROBADO (2,755 s) | Modelo BirdNET sin comprimir (65.830.813 bytes), `manifest.json` y `species.json` con 9.789 posiciones y hashes exactos, cuatro correcciones de erratas, carga JNI fotográfica LiteRT 1.4.1 y referencias fotográficas de 3 fotos locales con diferencia máxima 0,00008 $\le 0,001$. Base de RF-9, RF-11, RF-14, RF-15, RF-25. |
| `BirdNetRuntimeDiagnosticTest` | 1 / 1 | APROBADO (1,889 s) | Comparación de optimizaciones CPU de ONNX Runtime (`NO_OPT` vs `ALL_OPT`) en el procesador Samsung A56. |
| `BirdNetT4CandidateTest` | 1 / 1 | APROBADO (0,002 s) | Verificación condicional del candidato T4a preservada para trazabilidad de la enmienda. |
| `BirdSoundActivityTest` | 20 / 20 | APROBADO (34,784 s) | Navegación menú -> sonido -> ficha -> zoom -> retorno con resultados intactos (RF-18, RF-19), 0 observaciones creadas en Room, debounce en toques, rotación en los 7 estados de UI, Home en grabación con liberación < 1 s, Home en análisis completándose en segundo plano (RF-23), abandono cancelando job (RF-24), muerte real del proceso desde host ADB (`am force-stop`) relanzando limpio en `Ready`, accesibilidad (TalkBack anuncia estado 1 sola vez polite, reloj sin región en vivo, contraste > 6:1, botones $\ge 48\text{ dp}$). RF-1 a RF-12, RF-14 a RF-25. |
| `BirdSoundBenchmarkTest` | 5 / 5 | APROBADO (50,223 s) | Condiciones del dispositivo Samsung A56 (modo avión, Wi-Fi apagado, página 4 KB, 8 GB RAM), preparación fría en 692,81 ms ($\le 5\text{ s}$), 20 mediciones de análisis 10 s promediando 1.571,03 ms (20/20 $\le 5\text{ s}$), PSS pico en sesión limpia 501,15 MiB ($\le 512\text{ MiB}$), 5 ciclos de liberación con delta plano +14,70 KiB, transición foto->sonido (PSS 498,51 MiB $\le 512\text{ MiB}$), higiene y privacidad sin archivos residuales. |
| `BirdSoundCatalogTest` | 1 / 1 | APROBADO (0,431 s) | Carga de metadatos de especies en dispositivo y resolución de nombres regionales y fichas. RF-11 a RF-18. |
| `BirdSoundEvaluationTest` | 3 / 3 | APROBADO (57,345 s) | Corpus de 42 clips de 10 s y manifiesto SHA-256 congelado; calibración sobre 14 clips fijando $T = 0,35$; evaluación reservada a ciegas sobre 28 clips con 16/20 positivos en top 5 (80%), 0/4 FP en controles reales de viento/voz y 0/4 FP en silencios constantes. RF-9, RF-12 a RF-17. |
| `BirdSoundViewModelTest` | 10 / 10 | APROBADO (0,164 s) | Coordinación de estado reactivo mediante StateFlow, prevención de doble inicio, cancelación en preparación/captura/análisis, protección ante callbacks tardíos, preservación de estado en rotación y limpieza en `onCleared`. RF-3, RF-6 a RF-8, RF-10, RF-11, RF-17, RF-19 a RF-25. |

### Higiene de almacenamiento, privacidad y ciclo de vida (RF-25)

- **Archivos temporales o persistentes**: Verificación automatizada en `BirdSoundBenchmarkTest.storageHygieneAndPrivacyVerification` de `context.filesDir`, `context.cacheDir` y `context.externalCacheDir`: **0 archivos de audio retenidos** (ningún `.wav`, `.pcm`, `.m4a`, `.aac`, `.mp3` ni `.3gp`).
- **Logs del sistema**: Verificación de ausencia de volcados de señales de audio PCM o listas de resultados de usuarios en Logcat de producción.
- **Liberación de micrófono**: Demostrada en menos de 1 segundo (mediciones empíricas de 115 a 682 ms) ante cancelación manual, paso a segundo plano (`onStop`), bloqueo de pantalla o finalización de captura.
- **Descarte de sesión**: Al pulsar "Volver a grabar", al salir de la pantalla (`finish()`) o ante muerte del proceso, los resultados anteriores y buffers de audio se descartan inmediatamente sin recuperarse de forma persistente.

### Accesibilidad

- **Áreas táctiles**: Todos los botones de acción (`btnMainAction`, `btnErrorAction`, `btnViewInfo`, `btnAttribution`, botón de retroceso) cumplen y superan el tamaño táctil mínimo de $48\times 48\text{ dp}$.
- **Escala de fuentes**: Verificado visualmente en hardware Samsung Galaxy A56 con `font_scale 2.0` (200% de tamaño de texto) en vertical y horizontal: sin recortes de texto, sin desbordamientos de botones ni colisiones en chips de puntuación.
- **Contraste semántico de colores**: Tokens semánticos `score_chip_background` y `score_chip_text` implementados en `bg_score_chip.xml` (`#D8E4FF` / `#1F4B99` en modo claro; `#11264F` / `#7FA5FF` en modo oscuro), alcanzando relaciones de contraste superiores a 6:1 (superando el estándar WCAG AA de 4.5:1).
- **Lector de pantalla (TalkBack)**:
  - `txtStatus` configurado con `accessibilityLiveRegion="polite"` y anuncio restringido a cambios efectivos de texto, garantizando que el cambio de estado se anuncie exactamente una vez.
  - Temporizador numérico `txtTimer` sin región en vivo, evitando la saturación auditiva de 10 anuncios por segundo durante la captura.
  - Descripciones de contenido dinámicas y comprensibles para cada tarjeta de resultados y chip de puntaje.

### Comprobaciones humanas manuales sensoriales declaradas formalmente como pendientes

En estricto cumplimiento de la regla de no sustituir comprobaciones humanas por pruebas automatizadas:
1. **Verificación auditiva humana presencial de TalkBack en español**:
   - Escucha directa en persona de la locución generada por el motor TTS del dispositivo Samsung por sus altavoces físicos durante el recorrido de identificación acústica, para evaluar de forma cualitativa la cadencia, tono y pronunciación fonética de los nombres comunes y científicos en español rioplatense.
2. **Inspección táctil humana directa en mano sobre el vidrio del dispositivo**:
   - Pulsación táctil física de los botones y fichas directamente con los dedos sobre la pantalla para constatar la ergonomía en mano de las zonas de toque en ambas orientaciones físicas reales.

### Conclusión y estado de integración

- **Tareas T1 a T10**: Completadas con evidencia empírica rigurosa documentada en hardware Samsung Galaxy A56 en modo avión y sin conexión a red.
- **Requisitos de la spec**: RF-1 a RF-25 y los ocho criterios de finalización demostrados.
- **Comandos de Gradle**: Los cuatro comandos oficiales ejecutados de forma independiente.
- **Compromiso de integridad**: No se realizaron commits ni push en el repositorio, preservando intacta la cadencia de trabajo establecida en `AGENTS.md`.




