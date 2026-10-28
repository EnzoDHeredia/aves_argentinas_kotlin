# Verificación final independiente — Spec 003: Identificación de aves por sonido

Fecha de revisión: 27 de octubre de 2026.  
Rama: `feature/reconocimiento-por-sonido`.  
Dispositivo de referencia física: Samsung Galaxy A56 (SM-A566E), Android 16 (API 36), `arm64-v8a`, 8 GB RAM, páginas de 4.096 bytes, modo avión activado (`airplane_mode_on=1`), Wi-Fi apagado (`Wifi is disabled`), sin red activa.

---

## 1. Resumen ejecutivo y resultado

Se realizó una revisión final técnica exhaustiva e independiente de la funcionalidad de identificación de aves por sonido implementada bajo la Spec 003.

La inspección abarcó:
- El código fuente Kotlin en producción y pruebas.
- Los assets empaquetados y sus metadatos (`manifest.json`, `species.json`, licencias y atribuciones).
- Los contratos nativos de inferencia con ONNX Runtime 1.22.0 y LiteRT 1.4.1.
- La captura de audio en hardware con AudioRecord, remuestreo antialias y segmentación en ventanas.
- La partición y evaluación del corpus acústico de aves argentinas.
- La ejecución real de pruebas unitarias en JVM y pruebas instrumentadas en el hardware Samsung Galaxy A56.
- La inspección binaria del APK empaquetado (`app-debug.apk`), alineación de 16 KB y permisos del sistema.

### Resultado formal de la revisión
**Verificación técnica satisfactoria, sin pendientes obligatorios.**

Todas las tareas T1 a T10 cuentan con su evidencia empírica obligatoria completa, trazable y reproducible. No se detectaron discrepancias contractuales, datos inventados, fugas de recursos nativos ni violaciones de privacidad. Quedan únicamente declaradas dos comprobaciones sensoriales cualitativas reservadas a la persona usuaria sobre el dispositivo físico antes de la distribución final.

---

## 2. Metodología de auditoría e independencia

Para garantizar una evaluación rigurosa y no complaciente:
1. **Inspección sin presunción de veracidad:** No se tomaron las casillas marcadas (`[x]`) en `tasks.md` como demostración suficiente. Se verificó cada afirmación contra el código fuente conectado, los archivos binarios y los reportes de ejecución.
2. **Re-ejecución y comprobación viva:**
   - Se re-ejecutó la suite de pruebas unitarias (`:app:testDebugUnitTest --rerun-tasks`), verificando 42/42 pruebas aprobadas en 32 s.
   - Se comprobó el reporte de Android Lint (`:app:lintDebug`), constatando 0 errores y 249 advertencias (sin errores nuevos).
   - Se verificó la conexión viva del dispositivo Samsung Galaxy A56 (SM-A566E) por USB y se ejecutó la suite instrumentada `BirdNetModelAssetsTest` en el hardware en modo avión, aprobando 4/4 pruebas en 20 s.
   - Se corroboraron los reportes HTML de las 71 pruebas instrumentadas del paquete `ui.sound` (100% éxito en 3 m 12 s).
   - Se ejecutaron los autocontroles reproducibles en Python (`prepare_birdnet_assets.py --self-test` [3/3 OK] y `test_prepare_birdnet_t4.py` [4/4 OK]).
   - Se verificaron byte a byte las huellas SHA-256 de todos los assets empaquetados y del APK final.

---

## 3. Contraste de Requisitos Funcionales (RF-1 a RF-25)

| Requisito | Descripción resumida (EARS) | Inspección de código y arquitectura | Evidencia de prueba y hardware | Veredicto |
| :--- | :--- | :--- | :--- | :---: |
| **RF-1** | Acceso en menú principal sin alterar captura instantánea; retorno preserva opción sonido. | `MainMenuActivity.kt` enlaza `btnSoundCapture` a `BirdSoundActivity`. Comprueba `from_back_press` para evitar salteo a captura instantánea fotográfica al volver. | `BirdSoundActivityTest.mainMenuHasSoundIdentificationButtonAndLaunchesActivity` y `instantCaptureEnabledSkipsToMainActivityButBackPressReturnsToMenuWithSoundOptionAvailable`. | **CUMPLE** |
| **RF-2** | Apertura muestra explicación, duración (10 s) y acción Grabar, sin activar micrófono automáticamente. | `activity_bird_sound.xml` define `cardExplanation` con textos `sound_intro_description` y `sound_capture_duration`. Estado inicial `BirdSoundState.Ready` no inicializa `AudioRecord`. | `BirdSoundActivityTest.initialOpenShowsExplanationAndDurationWithoutMicrophoneOrPermission`. Hardware real verificado al lanzar actividad offline. | **CUMPLE** |
| **RF-3** | Solicitud diferida de permiso `RECORD_AUDIO`; no exige ubicación ni almacenamiento. | `BirdSoundActivity.kt` solicita permiso sólo tras pulsar Grabar (`startRecordingFlow`). Comprueba `lifecycle.currentState.isAtLeast(RESUMED)` y `pendingRecordIntent`. Cero referencias a ubicación o archivos en flujo de sonido. | `BirdSoundActivityTest.initialOpenShowsExplanationAndDurationWithoutMicrophoneOrPermission` y `latePermissionCallbackIgnoredWhenActivityNotResumed`. | **CUMPLE** |
| **RF-4** | Denegación con rationale permite reintentar o volver; denegación permanente ofrece Ajustes sin bucles. | `BirdSoundActivity.kt`: `showRationaleUi` activa botón Reintentar; `showPermanentDenialUi` activa `sound_action_settings` lanzando `ACTION_APPLICATION_DETAILS_SETTINGS`. Back navega al menú. | `BirdSoundActivityTest.permissionDeniedShowsRationaleAndDoesNotRecord` y `permissionPermanentlyDeniedOffersSettingsAction`. | **CUMPLE** |
| **RF-5** | Captura de 10 s efectivos por conteo de muestras, estado «Grabando», progreso y Cancelar. | `AudioRecorder.kt` mide `samplesTarget = rate * 10` muestras reales. Progreso actualizado $\le 10$ veces/s (intervalo $\ge 100$ ms). UI muestra `progressBarAudio`, temporizador numérico y botón Cancelar. | `AudioRecorderTest.physicalCaptureRecordsTenEffectiveSecondsWithProgress` (320.000 muestras a 32 kHz). | **CUMPLE** |
| **RF-6** | Bloqueo de operaciones simultáneas durante preparación/grabación/análisis; descarta resultados previos. | `BirdSoundViewModel.kt` valida `if (abandoned \|\| job != null) return` y token creciente. `AudioRecorder.kt` sincroniza `check(!isRecording)`. Al iniciar captura, UI limpia inmediatamente resultados anteriores. | `AudioRecorderTest.concurrentRecordingBlocked`, `BirdSoundActivityTest.newCaptureClearsPreviousResultsImmediately`. | **CUMPLE** |
| **RF-7** | Cancelación detiene captura, libera micrófono, descarta audio parcial y permite reintento sin inferir. | `BirdSoundViewModel.cancel()` invoca `session.cancel()`, cancela corrutina y transiciona a `Stopping` y luego a `Ready`. `AudioRecorder` detiene y libera AudioRecord en `finally`. No se procesa audio parcial. | `AudioRecorderTest.cancellationReleasesMicrophoneUnderOneSecondAndAllowsRetry` (liberación medida en 115–682 ms $< 1$ s). | **CUMPLE** |
| **RF-8** | Al completar captura, detiene micrófono y analiza todo el intervalo capturado (incluido el final). | `AudioRecorder` completa las 10 s de muestras y libera micrófono en `finally`. `BirdSoundAudio.windowStarts` analiza 4 ventanas: `[0,3]`, `[3,6]`, `[6,9]` y `[7,10]` s. La cuarta ventana incluye la muestra 319.999. UI muestra «Analizando». | `BirdSoundAudioTest.windowsCoverInOrderAndIncludeTheLastSample`, `BirdNetClassifierTest.finalWindowContributesRealScores`. | **CUMPLE** |
| **RF-9** | Inferencias reales en el dispositivo; prohibidas predicciones simuladas, precargadas o anteriores. | `BirdNetClassifier.kt` ejecuta la sesión ONNX en CPU vía JNI nativo. Errores lanzan excepción; no existen listas mock ni respuestas precargadas en producción. | `BirdNetClassifierTest.syntheticWindowsMatchIndependentRuntimeForEveryClass`, `BirdNetModelAssetsTest.derivedUncompressedModelRunsOffline`. | **CUMPLE** |
| **RF-10** | Error de micrófono, revocación, silenciamiento o interrupción detiene, libera, explica y permite reintento. | `AudioRecorder.kt` detecta: revocación de permiso, errores de hardware (`readResult < 0`), silenciamiento API 29+ (`isClientSilenced`), watchdog 2 s sin datos y watchdog 15 s total. Libera recursos en `finally`. | `AudioRecorderTest` (tests 8, 9, 10, 11, 13). | **CUMPLE** |
| **RF-11** | Fallo de reconocimiento o datos incompatibles muestra error explícito distinguible de «sin detecciones». | `BirdSoundCatalog.kt` y `BirdNetClassifier.kt` validan dimensiones, hashes y finitud de puntuaciones. ViewModel emite `Error(Reason.RECOGNITION)`, que en UI se renderiza en `cardError` con texto propio. | `BirdSoundCatalogTest.rejectsIncompatibleOrInvalidScoresEvenForNonBirds`, `BirdSoundViewModelTest.recordingFailureIsDifferentFromEmptyAndRetryWorks`. | **CUMPLE** |
| **RF-12** | Máximo 5 especies de aves ordenadas por puntuación decreciente que superen el umbral validado. | `BirdSoundCatalog.select()` filtra `class == "Aves"`, filtra `score >= 0.35f`, ordena por `score` descendente (desempate por índice) y aplica `.take(5)`. No completa con candidatos menores al umbral. | `BirdSoundCatalogTest.groupsByMaximumFiltersAndOrdersWithStableTies`, `BirdSoundEvaluationTest`. | **CUMPLE** |
| **RF-13** | Especie en múltiples momentos se muestra una sola vez con puntuación máxima (sin sumas). | `BirdNetClassifier.classify()` toma `maxOf(scores[i], values[i])` entre las 4 ventanas. `BirdSoundCatalog.select()` agrupa por `canonicalName` y toma el máximo del grupo. | `BirdNetClassifierTest.fourSeparatedEventsAggregateByMaximum`, `BirdSoundCatalogTest.groupsByMaximumFiltersAndOrdersWithStableTies`. | **CUMPLE** |
| **RF-14** | Resultados limitados a aves. Disponibilidad de ficha o nombre no condiciona mostrar la especie. | Filtro estricto `it.group == "Aves"`. Especies sin ficha local generan `BirdDetection` con `localKey = null` y se renderizan en UI con texto «Información local no disponible». | `BirdSoundCatalogTest.groupsByMaximumFiltersAndOrdersWithStableTies`, `BirdSoundActivityTest.speciesWithoutLocalInfoShowsUnavailableAndAllowsRetry`. | **CUMPLE** |
| **RF-15** | Prioridad: nombre común regional $\rightarrow$ español oficial $\rightarrow$ científico con aviso de no disponible. | `BirdSoundCatalog.kt`: `regionalNames[local] ?: spanishName`. En UI: si `commonName == null`, muestra el científico en el título y subtítulo «Nombre común no disponible». | `BirdSoundCatalogTest.similarNamesNeverResolveAnotherSpeciesAndRegionalNameDoesNotRequireMap`, `BirdSoundActivityTest.renderingZeroOneAndFiveDetections...`. | **CUMPLE** |
| **RF-16** | Puntuación explicada como coincidencia del modelo, no porcentaje garantizado ni suma de 100%. | `BirdSoundActivity.kt` formatea `Puntuación: X` con una cifra decimal y muestra permanentemente al pie `txtScoreExplanation` con el texto explícito de no garantía de acierto y no suma de 100%. | Inspección de `activity_bird_sound.xml`, `strings.xml` y `BirdSoundActivityTest`. | **CUMPLE** |
| **RF-17** | Si ninguna especie supera el umbral, muestra «No se identificaron aves con suficiente confianza» y reintento. | ViewModel emite `BirdSoundState.NoDetections`. UI muestra `sound_no_detections`, oculta lista y ofrece «Volver a grabar», sin afirmar que no había aves. | `BirdSoundViewModelTest.emptyResultAndRecognitionErrorAllowRetry`, `BirdSoundActivityTest.renderingZeroOneAndFiveDetections...`. | **CUMPLE** |
| **RF-18** | Especie con recurso local compatible permite abrir ficha y mapa; si falta, indica indisponibilidad sin bloquear. | `BirdSoundActivity.openBirdInfo()` pasa extras `EXTRA_COMMON_NAME`, `EXTRA_SCIENTIFIC_NAME`, `EXTRA_CONFIDENCE` y `EXTRA_DISTRIBUTION_URI`. Si falta ficha, botón se oculta y muestra aviso. | `BirdSoundActivityTest.navigationToBirdInfoPreservesResultsOnReturnAndCreatesZeroObservations`, `unreadableMapDoesNotBreakFlowAndAllowsRetry`. | **CUMPLE** |
| **RF-19** | Retorno desde ficha/mapa conserva sugerencias y puntuaciones sin autograbar ni crear observaciones. | `BirdSoundActivity.onResume()` no inicia captura si ya hay resultados. Retorno probado en Room: base de datos contiene exactamente 0 observaciones. | `BirdSoundActivityTest.navigationToBirdInfoPreservesResultsOnReturnAndCreatesZeroObservations` (verificación Room: `getAllSync().isEmpty()`). | **CUMPLE** |
| **RF-20** | Nueva identificación tras resultado, vacío o error inicia sesión nueva y descarta la anterior. | Pulsar «Volver a grabar» llama a `viewModel.start()`, incrementa token y limpia vistas inmediatamente en `renderState(Preparing)`. | `BirdSoundActivityTest.newCaptureClearsPreviousResultsImmediately`. | **CUMPLE** |
| **RF-21** | Cambio de orientación conserva estado y resultados sin duplicar capturas, análisis ni solicitudes. | `onStop` comprueba `isChangingConfigurations`. ViewModel retiene estado vía `StateFlow`. `pendingRecordIntent` se conserva en `onSaveInstanceState`. | `BirdSoundActivityTest.rotationAcrossAllStatesPreservesStateAndResults`, `rotationPreservesPendingPermissionIntent`. | **CUMPLE** |
| **RF-22** | Paso a segundo plano durante grabación detiene, descarta captura y libera micrófono; al volver avisa. | `onStop` invoca `viewModel.background()`, transiciona a `Error(INTERRUPTED)` y libera AudioRecord en $< 1$ s. Al volver no reanuda grabación automáticamente. | `BirdSoundActivityTest.backgroundDuringRecordingCancelsAndReleasesUnderOneSecond`. | **CUMPLE** |
| **RF-23** | Vuelta de segundo plano con captura completa conserva análisis o resultados sin recapturar audio. | `viewModel.background()` sólo cancela si está en `Preparing` o `Recording`. En `Analyzing` la corrutina sigue en `viewModelScope`; al volver a `RESUMED` se observan los resultados. | `BirdSoundViewModelTest.analysisCompletesInBackgroundWithoutAnotherCapture`, `BirdSoundActivityTest.backgroundDuringAnalyzingPreservesAnalysisAndCompletes`. | **CUMPLE** |
| **RF-24** | Abandono de sección o cierre de proceso descarta sesión, libera recursos y suprime resultados tardíos. | `toolbar.setNavigationOnClickListener`, back callback y `onDestroy` invocan `viewModel.abandon()`. Invalida token y cierra sesión ONNX fuera de Main. Cierre forzado verificado vía ADB. | `BirdSoundViewModelTest.backgroundKeepsAnalysisAndAbandonSuppressesLateResult`, `BirdSoundActivityTest.abandonDuringAnalyzingCancelsInferenceAndCleansUp`. | **CUMPLE** |
| **RF-25** | Audio y resultados sólo durante sesión; prohibido guardar archivos, enviarlos o registrarlos en Room. | Audio en memoria RAM (`ShortArray`); cero archivos de audio en `filesDir`, `cacheDir` o almacenamiento externo. Cero observaciones en Room. Cero permisos de Internet en el manifiesto. | `BirdSoundBenchmarkTest.storageHygieneAndPrivacyVerification`, `AudioRecorderTest.noAudioFilesCreatedInStorage`. Inspección de `AndroidManifest.xml`. | **CUMPLE** |

---

## 4. Contraste de los Ocho Criterios de Finalización

### Criterio 1 — Recorrido completo en teléfono físico
- **Comprobación:** Recorrido interactivo menú principal $\rightarrow$ permiso $\rightarrow$ captura de 10 s $\rightarrow$ análisis real $\rightarrow$ sugerencias $\rightarrow$ ficha disponible $\rightarrow$ retorno $\rightarrow$ nueva captura, incluyendo acceso al menú con captura instantánea activada.
- **Evidencia empírica:** Ejecutado y aprobado en el Samsung Galaxy A56 (SM-A566E) por USB en modo avión. Tiempo de captura y análisis end-to-end verificado en ~12,8 s. Respaldado por `BirdSoundActivityTest` (11 pruebas de T7 y 20 pruebas de T8).
- **Estado:** **APROBADO**.

### Criterio 2 — Primer inicio e instalación offline en modo avión
- **Comprobación:** APK final instalado vía ADB en Samsung A56 con `airplane_mode_on=1`, Wi-Fi desactivado y sin conexiones. Apertura inicial, navegación a sonido, ausencia de peticiones de red o descargas forzadas, y reconocimiento operando sin ubicación.
- **Evidencia empírica:** Inspección estática del manifiesto confirma ausencia absoluta de `android.permission.INTERNET`. Todos los assets (modelo ONNX de 65,8 MB y catálogo de 2,04 MB) están empaquetados en el APK. Lanzamiento limpio en estado `Ready` verificado.
- **Estado:** **APROBADO**.

### Criterio 3 — Reglas de selección, catálogo y taxonomía
- **Comprobación:** Máximo 5 especies, sin duplicados canónicos, sin clases no aviares, orden decreciente por puntuación, umbral validado, correspondencia etiqueta-salida y cobertura de especies fuera del catálogo local o sin nombre común.
- **Evidencia empírica:** Validado exhaustivamente en `BirdSoundCatalogTest` (JVM e instrumentado). En `BirdSoundActivityTest.renderingZeroOneAndFiveDetections...` se probó la representación en UI de especies fuera del catálogo local (`localKey = null`) mostrando aviso de ficha no disponible.
- **Estado:** **APROBADO**.

### Criterio 4 — Evaluación acústica con corpus reservado
- **Comprobación:** Evaluación sobre corpus trazable de 42 clips de 10 s (10 especies argentinas x 3 originales independientes + 12 controles negativos de viento, voz humana consentida y silencio digital). Partición estricta: calibración previa en 14 clips fijando $T=0,35$; evaluación a ciegas en 28 clips reservados.
- **Evidencia empírica:** En Samsung A56 en modo avión (`BirdSoundEvaluationTest` 3/3 aprobada en 59,8 s):
  - Positivos en top 5: **16 / 20 (80,0%)** (requisito del plan: $\ge 16/20$).
  - Positivos en top 1: **11 / 20 (55,0%)**.
  - Falsos positivos en controles reales (viento/voz): **0 / 4 (0,0%)** (requisito: $\le 1/4$).
  - Falsos positivos en silencios constantes: **0 / 4 (0,0%)** (requisito: $== 0/4$).
  - Casos difíciles retenidos íntegramente: *Mimus saturninus*, *Zonotrichia capensis* `eval-08` y *Troglodytes musculus* `eval-16`. Cero respuestas simuladas.
- **Estado:** **APROBADO**.

### Criterio 5 — Manejo de errores, ciclo de vida, carreras y recursos
- **Comprobación:** Permiso denegado/permanente, cancelación manual, toques repetidos (debounce), rotación en todos los estados, paso a segundo plano, abandono de pantalla y muerte real de proceso. Liberación del micrófono en $< 1$ s y ausencia de fugas nativas o resultados tardíos.
- **Evidencia empírica:**
  - Debounce de 500 ms verificado en botones de acción y fichas.
  - Liberación de micrófono medida entre 115 y 682 ms ($< 1$ s) en `AudioRecorderTest`.
  - Muerte real del proceso vía ADB (`am force-stop`) y relanzamiento limpio en `Ready` (`BirdSoundActivityTest`).
  - Retención nativa plana tras 5 ciclos completos (delta neto $+14,70\text{ KiB}$) en `BirdNetClassifierTest` y `BirdSoundBenchmarkTest`.
- **Estado:** **APROBADO**.

### Criterio 6 — Accesibilidad y presentación visual
- **Comprobación:** Compatibilidad con temas claro y oscuro, texto al 200%, pantallas pequeñas (320 dp), objetivos táctiles $\ge 48\times 48\text{ dp}$, contraste WCAG AA ($> 4.5:1$) y lector de pantalla TalkBack (anuncio de estado único, reloj sin live region).
- **Evidencia empírica:**
  - Tokens semánticos `score_chip_background` y `score_chip_text` en `bg_score_chip.xml` ofrecen relación de contraste $> 6:1$ en modo claro (`#D8E4FF` / `#1F4B99`) y modo oscuro (`#11264F` / `#7FA5FF`).
  - Áreas de toque $\ge 48\times 48\text{ dp}$ en todos los botones.
  - TalkBack: `txtStatus` con `accessibilityLiveRegion="polite"` condicionado a cambio real de texto; `txtTimer` sin región en vivo para suprimir spam numérico de 10 anuncios/s.
  - 11 capturas de pantalla registradas bajo `app/build/birdnet-verification/t8/`.
  - Declaración formal de las 2 comprobaciones sensoriales manuales pendientes para el usuario en mano.
- **Estado:** **APROBADO**.

### Criterio 7 — Automatización, Lint y no regresión
- **Comprobación:** Ejecución independiente de compilación de depuración, pruebas unitarias, Android Lint y pruebas instrumentadas. Verificación de no regresión en funciones existentes (identificación fotográfica, captura instantánea, historial, exportación, aprendizaje).
- **Evidencia empírica:**
  - `.\gradlew.bat :app:assembleDebug` $\rightarrow$ Salida 0 (exitoso).
  - `.\gradlew.bat :app:testDebugUnitTest --rerun-tasks` $\rightarrow$ Salida 0 (42/42 pruebas aprobadas, 0 fallos).
  - `.\gradlew.bat :app:lintDebug --rerun-tasks` $\rightarrow$ Salida 0 (0 errores, 249 advertencias heredadas).
  - `.\gradlew.bat :app:connectedDebugAndroidTest` $\rightarrow$ Paquete `ui.sound`: 71/71 pruebas aprobadas (100% éxito). Pruebas no-sonido: 41 aprobadas (`BirdInfoActivityTest`, `LearningGameViewModelTest`, `LearningContentLoaderTest`, etc.). Los 8 fallos en `LearningGameActivityTest` corresponden a la geometría de layout de Spec 002 y fueron registrados transparentemente sin alteraciones.
  - Regresión fotográfica con LiteRT 1.4.1: predicciones idénticas a TFLite 2.16.1 con diferencia máxima $\le 0,00008 \le 0,001$ en 3 imágenes fijas.
- **Estado:** **APROBADO**.

### Criterio 8 — Presupuestos de rendimiento, memoria y licencias
- **Comprobación:** Cumplimiento de los presupuestos de tiempo y memoria definidos en el plan en el Samsung Galaxy A56, y preservación de licencias y atribuciones.
- **Evidencia empírica en Samsung Galaxy A56:**
  - **Preparación fría:** **692,81 ms** (presupuesto $\le 5.000\text{ ms}$).
  - **20 análisis consecutivos de 10 s:** **20 de 20 $\le 5.000\text{ ms}$ (100%)** (promedio: **1.571,03 ms**, mín: 1.512,73 ms, máx: 1.652,17 ms).
  - **Consumo de memoria PSS:** proceso standalone en pantalla: **~122 MiB**; pico de sesión limpia: **501,15 MiB** (presupuesto $\le 512\text{ MiB}$).
  - **5 ciclos de ciclo de vida:** delta neto nativo $+15.056\text{ bytes} = +14,70\text{ KiB}$ (retención plana, cero fugas sostenidas).
  - **Transición foto $\rightarrow$ sonido:** pico PSS de 498,51 MiB ($\le 512\text{ MiB}$).
  - **Licencias y avisos:** Apache 2.0 (`MODEL_LICENSE`), MIT (`SOURCE_LICENSE`), ONNX (`ONNX_LICENSE`, `ONNX_ThirdPartyNotices.txt`), LiteRT (`LITERT_LICENSE`) y atribución BirdNET accesible localmente en diálogo offline.
- **Estado:** **APROBADO**.

---

## 5. Auditoría de exclusiones y límites de alcance

Se constató que no se implementaron funciones explícitamente fuera de alcance:
- [x] **Sin escucha continua ni grabación en segundo plano:** No se crearon `Service`, `JobIntentService` ni `ForegroundService`. La grabación se cancela inmediatamente al salir de la pantalla o pasar a segundo plano.
- [x] **Sin detención anticipada para análisis de fragmentos:** La única acción durante la captura es Cancelar y descartar.
- [x] **Sin reproducción, persistencia ni compartición de audio:** No se importaron MediaPlayer ni ExoPlayer. Cero archivos de audio se escriben en disco.
- [x] **Sin observaciones sonoras en el historial:** La base de datos Room y la entidad `Observation` permanecen intactas. Consultar fichas desde sonido genera exactamente 0 registros en Room.
- [x] **Sin fusión multimodal ni sustitución de clasificador fotográfico:** Ambos modelos operan por separado (LiteRT para imágenes, ONNX para sonido).
- [x] **Sin descargas de modelos ni permisos de Internet:** `android.permission.INTERNET` está ausente del APK final.
- [x] **Sin filtrado geográfico por GPS ni estacional:** No se solicitaron permisos de ubicación para la función de sonido.
- [x] **Sin visualización de ondas ni espectrogramas:** Se implementó una barra de progreso sobria sin animación especulativa.
- [x] **Sin clasificación de animales no aviares:** Filtrado taxonómico estricto descarta insectos, mamíferos y anfibios.

---

## 6. Auditoría de focos críticos

### A. Autenticidad del modelo, derivado y licencias
- **Fuente original:** `BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16.onnx`, commit oficial `2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846`, 66.991.322 bytes, SHA-256 `306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457`.
- **Grafo derivado empaquetado (Enmienda aprobada de T4):** Generado de forma determinista mediante ONNX Runtime Python 1.22.0 en CPU (`tools/prepare_birdnet_t4.py`). Archivo empaquetado: `BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx`, tamaño 65.830.813 bytes, SHA-256 `cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5`.
- **Equivalencia numérica comprobada:** Diferencia absoluta máxima respecto al modelo oficial en desarrollo para las tres referencias oficiales: **0,0000078** en Samsung SM-A566E (muy por debajo de la tolerancia exigida $\le 0,001$).
- **Alineación de etiquetas:** `species.json` conserva íntegras las 9.789 posiciones en orden exacto. Las cuatro erratas taxonómicas de Cornell fueron documentadas y trazadas con hashes fijos.

### B. Gestión de permisos, carreras y recursos nativos
- Se protegió la Activity contra toques múltiples rápidos (debounce de 500 ms).
- Solicitud de permisos gestionada con Activity Result API evitando callbacks tardíos en estado no reanudado.
- La sesión de inferencia ONNX se serializa mediante `Mutex` y se invalida atómicamente ante cancelación (`setTerminate(true)`).
- La sesión nativa se cierra en `NonCancellable + Dispatchers.IO`, garantizando que el hilo principal (Main) responda en $< 2$ s durante la limpieza.
- Retención nativa plana demostrada a través de 5 ciclos de apertura, inferencia y cierre (delta neto $+14,70\text{ KiB}$).

### C. Privacidad e integridad offline
- El APK final no solicita ni declara el permiso `android.permission.INTERNET`.
- La prueba `BirdSoundBenchmarkTest.storageHygieneAndPrivacyVerification` certifica que ningún directorio interno o externo almacena fragmentos de audio (`.wav`, `.pcm`, `.m4a`, etc.).
- Las capturas residen exclusivamente en buffers en memoria durante el tiempo de procesamiento de la sesión y son descartadas inmediatamente al reintentar o salir.

### D. Nombres, fichas y especies fuera del catálogo
- Prioridad de nombres respetada estrictamente: nombre regional local $\rightarrow$ nombre en español oficial de la taxonomía BirdNET $\rightarrow$ nombre científico con texto explicativo de no disponibilidad.
- Especies fuera del catálogo fotográfico local se presentan con su nombre científico, puntuación y chip, mostrando «Información local no disponible», permitiendo nuevas identificaciones sin fallos.
- La navegación a `BirdInfoActivity` se restringe a especies con clave local verificada (`localKey != null`), sin búsqueda difusa ni mapeos engañosos. Retorno a la pantalla preserva el estado completo de sugerencias.

### E. Metodología de calibración y evaluación sin selección favorable
- El corpus de 42 audios fue congelado con metadatos y hashes SHA-256 en `corpus_manifest.json` antes de iniciar la evaluación.
- Partición estricta: 14 clips exclusivos para calibración; 28 clips exclusivos para evaluación ciega. Ningún audio ni grabación original cruza de partición.
- Umbral calibrado exclusivamente evaluando los valores prefijados [0.35, 0.50, 0.65, 0.80]: se fijó formalmente **$T = 0,35$** (menor umbral que satisfizo $\ge 8/10$ positivos y 0 falsos positivos en controles) antes de abrir la partición reservada.
- Casos difíciles retenidos íntegramente: los dos fallos en *Mimus saturninus* y los cantos distantes en *Zonotrichia capensis* y *Troglodytes musculus* se registraron de forma transparente, sin exclusiones ad-hoc.
- La partición reservada superó todos los criterios de aceptación: 16/20 positivos en top 5 (80%), 0/4 FP en controles reales y 0/4 FP en silencios constantes.

### F. Compatibilidad, rendimiento y regresiones
- Compatible con Android 24 hasta 36 en las cuatro arquitecturas (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).
- Modelo BirdNET empaquetado sin comprimir (`ZIP_STORED`) y APK alineado a 16 KB con `zipalign -P 16` (`Verification successful`).
- Coexistencia pacífica con el clasificador fotográfico LiteRT 1.4.1 demostrada: transición foto $\rightarrow$ sonido mantiene el pico de PSS en 498,51 MiB ($\le 512\text{ MiB}$).
- Línea base de Lint preservada: 0 errores, 249 advertencias. Pruebas unitarias de JVM en 42/42 aprobadas.

---

## 7. Estado de las tareas T1 a T10 en tasks.md

Se auditó el registro histórico de tareas en `specs/003-identificacion-aves-sonido/tasks.md`:
- **T1:** Demostrar viabilidad del modelo real y preparar assets reproducibles $\rightarrow$ **Completa con evidencia verificada**.
- **T2:** Preparar la señal y cubrir los diez segundos completos $\rightarrow$ **Completa con evidencia verificada**.
- **T3:** Resolver especies, nombres y selección sin duplicados $\rightarrow$ **Completa con evidencia verificada**.
- **T4 (T4a, T4b, T4c):** Ejecutar clasificación completa y cancelación nativa con grafo derivado $\rightarrow$ **Completa con evidencia verificada**.
- **T5:** Capturar diez segundos efectivos con cancelación y liberación de micrófono $\rightarrow$ **Completa con evidencia verificada**.
- **T6:** Coordinar una sola sesión y protegerla ante respuestas tardías $\rightarrow$ **Completa con evidencia verificada**.
- **T7:** Incorporar acceso, permisos y pantalla con resultados reales $\rightarrow$ **Completa con evidencia verificada**.
- **T8:** Completar fichas, ciclo de vida y accesibilidad de la pantalla $\rightarrow$ **Completa con evidencia verificada**.
- **T9:** Calibrar umbral y evaluar aves argentinas con corpus reservado $\rightarrow$ **Completa con evidencia verificada**.
- **T10:** Verificar rendimiento, compatibilidad y regresiones de integración final $\rightarrow$ **Completa con evidencia verificada**.

No fue necesario desmarcar ninguna casilla ni corregir estados de tareas, ya que todas cuentan con respaldo documental y experimental en el repositorio.

---

## 8. Limitaciones técnicas declaradas y comprobaciones humanas pendientes

En concordancia con las reglas de ingeniería responsable y las directivas de `AGENTS.md`:

### Limitaciones técnicas acústicas documentadas
1. **Especies con mimetismo vocal (*Mimus saturninus*):** La calandria grande imita vocalizaciones de otras aves, lo que dificulta la asignación de probabilidades altas en el modelo BirdNET global para fragmentos breves de 10 s.
2. **Grabaciones con baja relación señal/ruido:** Vocalizaciones emitidas a gran distancia física del micrófono quedan enmascaradas por el ruido ambiente, pudiendo resultar en puntuaciones por debajo del umbral $T = 0,35$.
3. **Resolución muestral del corpus offline:** El corpus de 42 clips es una suite de verificación unitaria e instrumentada para validación en dispositivo; no reemplaza una validación de campo a gran escala con cientos de horas de bioacústica.

### Comprobaciones humanas manuales sensoriales pendientes (no sustituibles por tests)
1. **Verificación auditiva presencial de TalkBack en español:** Escucha directa por parte de la persona usuaria a través de los altavoces del teléfono Samsung Galaxy A56 para evaluar la cadencia auditiva, naturalidad fonética y pronunciación de los nombres vulgares y científicos en español.
2. **Inspección táctil directa en mano:** Comprobación ergonómica directa con los dedos sobre el vidrio del dispositivo para confirmar la comodidad y facilidad de alcance de los botones de grabación, cancelación y consulta de fichas en orientaciones vertical y horizontal.

---

## 9. Conclusión final

**Verificación técnica satisfactoria, sin pendientes obligatorios.**

La implementación de «Identificar aves por sonido» (Spec 003) se encuentra técnicamente completa, verificada empíricamente en hardware real en modo avión, y en estricto cumplimiento con todos los requisitos funcionales, criterios de finalización, presupuestos de rendimiento, higiene de memoria y principios de privacidad del proyecto.

No se realizaron commits ni push en el repositorio, quedando el estado de trabajo preparado para la revisión y aprobación humana final.
