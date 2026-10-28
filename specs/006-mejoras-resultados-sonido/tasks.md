# Tareas — Spec 006: Mejoras de resultados por sonido

Estado: en progreso, T1, T2, T3 y T4 completadas. Fecha: 28 de octubre de 2026.

## Referencias

- [Spec aprobada](spec.md).
- [Plan aprobado](plan.md).
- [Instrucciones del proyecto](../../AGENTS.md): ejecutar una sola tarea por turno, comprobarla, actualizar su estado y esperar confirmación antes de la siguiente; no hacer commits ni push.

## Tareas

- [x] **T1 — Simplificar los textos y compactar las tarjetas.**
  - Áreas afectadas: layouts de resultado, encabezado y pie; `BirdSoundActivity.kt`; recursos de textos; pruebas de pantalla correspondientes.
  - Cubre: RF-5 a RF-8 y RF-10.
  - Depende de: ninguna.
  - Verificación: comprobado menor espacio vertical en `item_bird_sound_result.xml` (reducción de márgenes y paddings verticales de ~22dp por tarjeta conservando nombres, puntuación, aviso de indisponibilidad y área de toque `minHeight >= 48dp` en `btnViewBirdInfo`); explicación visible sin resultados (`cardExplanation` VISIBLE), oculta al llegar el primero (GONE) y al detener, y visible de nuevo en una sesión nueva; nota final de puntuación retirada y atribución `btnAttribution` conservada en `item_bird_sound_footer.xml` y `strings.xml`.
  - Evidencia:
    - Pruebas instrumentadas en dispositivo SM-A566E (`connectedDebugAndroidTest`): 31/31 pasaron (100%), incluyendo `explanationCardVisibilityTogglesWithResultsAndFooterHidesScoreExplanation` y `resultsStateShowsFormattedResultsAndFooterExplanation`.
    - Pruebas unitarias (`testDebugUnitTest`): exitoso.
    - Compilación (`assembleDebug`): APK de depuración generado exitosamente.
    - Android Lint (`lintDebug`): completado sin errores.
    - Pendiente: inspección visual interactiva de la pantalla en dispositivo real para contrastes de temas y texto ampliado (se evaluará en la verificación final).

- [x] **T2 — Mostrar puntuaciones como porcentajes.**
  - Áreas afectadas: `BirdSoundResultAdapter.kt`, recursos de textos y pruebas del adaptador y pantalla.
  - Cubre: RF-9.
  - Depende de: ninguna; ejecutar después de T1 por la cadencia acordada.
  - Verificación: comprobado en `BirdSoundResultAdapter.kt` y `strings.xml` que las puntuaciones se formatean y muestran como porcentajes enteros redondeados (0,5 → 50 %, 0,7 → 70 %, 0,74 → 74 %, 0,95 → 95 %, 0,0 → 0 %, 1,0 → 100 %, redondeo de decimales intermedios 0,504 → 50 % y 0,506 → 51 %, y límites con clamping 0..100 y NaN → 0). La descripción accesible (`contentDescription`) comunica de forma equivalente el porcentaje entero ("Puntuación del modelo: %1$d %%"). El valor `Float` original de `BirdDetection.score` no se altera ni muta para comparaciones de modelo ni para abrir fichas.
  - Evidencia:
    - Pruebas unitarias (`testDebugUnitTest`): 100% exitosas, incluyendo `scoreToPercentConvertsAndRoundsFloatScoreCorrectly` y `detectionPreservesExactScoreValueWithoutAlteringForPresentation` en `BirdSoundResultAdapterTest.kt`.
    - Pruebas instrumentadas en dispositivo SM-A566E (`connectedDebugAndroidTest`): 32/32 pruebas pasaron (100% de éxito) en `BirdSoundActivityTest`, incluyendo la nueva prueba `scoresAreDisplayedAsRoundedPercentagesWithEquivalentAccessibilityDescriptions` que verifica directamente en pantalla los textos visuales ("50 %", "70 %", "0 %", "100 %") y las descripciones accesibles equivalentes.
    - Compilación (`assembleDebug`): APK de depuración generado exitosamente.
    - Android Lint (`lintDebug`): completado sin errores.

- [x] **T3 — Seguir automáticamente los nuevos resultados.**
  - Áreas afectadas: `BirdSoundActivity.kt` y `BirdSoundActivityTest.kt`.
  - Cubre: RF-1 y RF-2.
  - Depende de: T1, por el cambio de altura y visibilidad del encabezado.
  - Verificación: comprobado en `BirdSoundActivity.kt` el seguimiento automático ante altas durante la escucha (desplazando mediante `smoothScrollToPosition` cuando las animaciones están activas y `scrollToPosition` cuando están desactivadas al índice del último resultado dentro de `ConcatAdapter`: `headerAdapter.itemCount + resultAdapter.itemCount - 1`, dejando la tarjeta visible sin dirigir la vista al pie). Se comprobó la conservación de posición ante mejoras de puntuación sin altas (RF-2), el desplazamiento ante mejoras conjuntas con alta, la gestión de cambios consecutivos asíncronos sin pérdida ni desfase (`pendingScrollToEnd`), y la restauración de la posición guardada tras rotación de pantalla (`savedRecyclerState` sin autodesplazamiento falso, activándose el seguimiento ante altas posteriores).
  - Evidencia:
    - Pruebas instrumentadas en dispositivo SM-A566E (`connectedDebugAndroidTest`): 32/32 pruebas pasaron (100% de éxito) en `BirdSoundActivityTest`, incluyendo la prueba ampliada `incrementalAdditionsAutoScrollToLastResultWhileScoreImprovementsMaintainPosition` que verifica de forma integral:
      1. Alta individual durante la escucha (`LISTENING`) con desplazamiento automático al último resultado.
      2. Lote con varias aves (especies 8 a 10) dejando visible el último resultado sin desplazar al pie.
      3. Alta mientras el usuario revisa resultados anteriores (arriba), desplazando hacia la nueva detección.
      4. Mejora de puntuación sin alta en posición intermedia, conservando estrictamente la posición del scroll y la tarjeta.
      5. Mejora junto con alta en el mismo lote, desplazando al nuevo resultado y actualizando el puntaje.
      6. Cambios consecutivos rápidos procesados sin desincronización.
      7. Restauración exacta de posición tras rotación de pantalla (`recreate()`) sin scroll falso, y respuesta con autodesplazamiento ante una nueva alta posterior.
    - Pruebas unitarias (`testDebugUnitTest`): 100% exitosas (29 tareas completadas sin fallos).
    - Compilación (`assembleDebug`): APK de depuración generado exitosamente.
    - Android Lint (`lintDebug`): completado sin errores.

- [x] **T4 — Resaltar resultados nuevos y mejoras.**
  - Áreas afectadas: `BirdSoundResultAdapter.kt`, `BirdSoundActivity.kt`, recursos de colores `colors.xml` (modo claro y oscuro), layout `item_bird_sound_result.xml` y pruebas unitarias e instrumentadas.
  - Cubre: RF-3 y RF-4.
  - Depende de: T3, para coordinar eventos de resultados y presentación.
  - Verificación: comprobado resaltado visual transitorio suave (1,2 s con interpolación `ArgbEvaluator` desde el tinte `sound_card_highlight` hasta el fondo original `brand_surface`) al ingresar una nueva especie (RF-3) o al mejorar la puntuación de una especie existente (RF-4). Verificado que una nueva mejora durante una animación activa cancela el animador anterior, reinicia el resaltado desde el valor inicial y recupera limpiamente el color base. Comprobado que el reciclado de vistas en scroll vertical (`onViewRecycled` / `onViewDetachedFromWindow`) cancela cualquier animación en curso y restablece el color de fondo original, impidiendo que tarjetas reutilizadas o desplazadas conserven o disparen falsos resaltados. Comprobado que las actualizaciones sin cambio de puntaje o con decrecimiento de puntaje no disparan animación, que la rotación de pantalla (`recreate()`) restaura la lista sin falsos resaltados, y que al desactivar animaciones en el sistema o prueba (`animationsEnabledProvider`) se actualiza el puntaje de inmediato sin animación cromática. Comprobado contraste cromático accesible superior a 10:1 en ambos temas (`#D8E4FF` en modo claro y `#1E355B` en modo oscuro frente a sus respectivos textos primarios).
  - Evidencia:
    - Pruebas unitarias (`testDebugUnitTest`): 100% exitosas (29 tareas completadas sin fallos, 0 errores), incluyendo 6 pruebas específicas en `BirdSoundResultAdapterTest`:
      1. `diffCallbackDetectsContentChangesAndReturnsPayloadForScoreUpdate`: valida emisión de `SCORE_IMPROVED_PAYLOAD` exclusivamente ante mejoras estrictas de puntaje (`newItem.score > oldItem.score`), y `SCORE_CHANGE_PAYLOAD` en otros cambios.
      2. `findNewSpeciesKeysIdentifiesOnlyNewDetectionsCaseInsensitively`: verifica detección pura e insensible a mayúsculas/espacios de especies nuevas para resaltado.
      3. `scoreToPercentConvertsAndRoundsFloatScoreCorrectly`, `detectionPreservesExactScoreValueWithoutAlteringForPresentation`, `diffCallbackAllows200DistinctSpeciesItemsWithoutCollisions` y `diffCallbackMatchesSameCanonicalScientificNameCaseInsensitiveAndTrimmed`.
    - Pruebas instrumentadas en dispositivo SM-A566E (`BirdSoundActivityTest`): 33/33 pruebas pasaron (100% de éxito, 0 fallos, tiempo de ejecución 52,59 s), incluyendo la prueba completa `newDetectionsAndScoreImprovementsTriggerTemporaryHighlightWhileRecyclingRotationAndNoChangesPreserveOriginalColor` que verifica:
      1. Legibilidad de textos y contraste cromático superior a 4,5:1 en el color de resaltado.
      2. Alta de especie durante la escucha disparando la animación de resaltado y retornando a `brand_surface`.
      3. Mejora de puntuación disparando resaltado in situ en la tarjeta existente sin recrear vistas.
      4. Mejora consecutiva durante la transición (RF-4) cancelando la animación previa, reiniciando y restaurando el color base.
      5. Actualización sin cambio de puntaje conservando el color original sin disparar animación.
      6. Rotación de pantalla (`recreate()`) sin disparo de falsos resaltados.
      7. Entornos con animaciones desactivadas actualizando puntajes de forma instantánea y limpia.
      8. Desplazamiento vertical y reciclado de vistas a través de 20 tarjetas sin retención ni disparo espurio de resaltado.
    - Compilación (`assembleDebug`): APK de depuración generado exitosamente.
    - Android Lint (`lintDebug`): completado sin errores.

## Verificación final

- [ ] Ejecutar `./gradlew.bat :app:testDebugUnitTest`, `./gradlew.bat :app:assembleDebug` y `./gradlew.bat :app:lintDebug`; ejecutar `./gradlew.bat :app:connectedDebugAndroidTest` si hay dispositivo o emulador.
- [ ] Comprobar todos los criterios de finalización de la spec, incluida revisión visual con y sin ficha, nombres largos, texto ampliado y ambos temas cuando haya dispositivo o emulador. Registrar verificaciones pendientes sin declararlas exitosas.
