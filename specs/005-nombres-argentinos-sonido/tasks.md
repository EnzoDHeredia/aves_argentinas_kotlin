# Tareas — Spec 005: Nombres argentinos en detecciones por sonido

Estado: aprobada explícitamente por el usuario el 27 de octubre de 2026; T1, T2, T3 y T4 completadas.

## Referencias

- [Spec aprobada](spec.md): RF-1 a RF-7.
- [Plan aprobado](plan.md): fuentes, catálogo independiente, compatibilidad y validación.
- [AGENTS.md](../../AGENTS.md): aprobar tareas antes de implementar; una tarea por turno, con comprobaciones y confirmación antes de la siguiente; sin commits ni push.

## Reglas de ejecución

- Las tareas están pendientes. Tras aprobar esta lista, ejecutar solo T1 y esperar confirmación antes de T2; mantener la misma cadencia para cada tarea.
- Conservar los cambios existentes y no modificar el modelo, las etiquetas ni el catálogo compartido `labels_map.json`. No se redistribuirán datos de una fuente sin comprobar las condiciones aplicables.
- Documentar comandos, resultados, fuentes y limitaciones. Una tarea sin sus condiciones de cierre permanece pendiente; una validación instrumentada requiere dispositivo o emulador.

## Tareas

- [x] **T1 — Evaluar fuentes de especies y nombres.**
  - Áreas afectadas: documentación de esta spec; fuentes de Aves Argentinas, eBird y fuentes verificables de Buenos Aires.
  - Cubre: base de RF-2, RF-4 y RF-5.
  - Depende de: ninguna.
  - Entrega: informe en [evaluacion-fuentes.md](evaluacion-fuentes.md) con listas utilizables, versiones, alcance geográfico, acceso y condiciones de uso y redistribución con referencias directas. Se distinguió evidencia provincial (Buenos Aires) de listas nacionales y se registraron las fuentes con restricciones de autorización.
  - Verificación: contraste empírico de 25 especies clave identificadas por nombre científico canónico contra `species.json` y `labels_map.json`; condiciones de redistribución y lagunas documentadas sin incorporar datos no confirmados.
  - Condición de cierre: cumplida; informe de fuentes y estrategia de datos trazable registrada en `evaluacion-fuentes.md`.

- [x] **T2 — Inventariar cobertura y correspondencias.**
  - Áreas afectadas: inventario y método reproducible de esta spec; lectura de `app/src/main/assets/birdnet/species.json` y fuentes evaluadas en T1.
  - Cubre: RF-2, RF-4 y RF-5.
  - Depende de: T1 y fuentes adecuadas disponibles.
  - Entrega: informe en [inventario-cobertura.md](inventario-cobertura.md) y script reproducible en `tools/inventory_species_coverage.py`. Se comparó la lista nacional oficial de referencia (1.043 especies: 1.033 nativas y 10 introducidas) contra las 8.927 aves de `species.json`, documentando 882 coincidencias exactas directas (84,6%), 108 equivalencias taxonómicas verificadas (10,4%) y 53 especies ausentes del modelo (5,1%), además de los casos ambiguos y la prioridad provincial de Buenos Aires sustentada en Darrieu et al. (2013) y `labels_map.json`.
  - Verificación: ejecución de `python tools/inventory_species_coverage.py`, suite unitaria `python -m unittest tools/test_inventory_species_coverage.py` (5 pruebas exitosas) y `./gradlew.bat :app:testDebugUnitTest` (aprobado).
  - Condición de cierre: cumplida; inventario reproducible con casos no resueltos, equivalencias justificadas y cobertura documentada en `inventario-cobertura.md` sin prometer detección de especies ausentes del modelo.

- [x] **T3 — Preparar el catálogo distribuible para sonido.**
  - Áreas afectadas: nuevo recurso en `app/src/main/assets/birdnet/`, documentación de procedencia y avisos aplicables; pruebas o comprobaciones de integridad del recurso.
  - Cubre: datos para RF-1 a RF-5.
  - Depende de: T2 y condiciones de redistribución verificadas para los datos incorporados.
  - Entrega: pares de nombre científico canónico normalizado y nombre común seleccionado en `app/src/main/assets/birdnet/sound_names_ar.json` (988 especies), con prioridad de Buenos Aires sustentada en Darrieu et al. (2013) y `labels_map.json`, y nombres nacionales verificados en los demás casos. Documentación y avisos en `NOTICE_SOUND_NAMES.txt` y `procedencia-catalogo-sonido.md`. Alternativas conservadas para especies no presentes en el recurso.
  - Verificación: `python -m unittest tools/test_sound_names_catalog.py` (8 pruebas exitosas), `python -m unittest tools/test_inventory_species_coverage.py` (5 pruebas exitosas) y `./gradlew.bat :app:testDebugUnitTest` (66 pruebas exitosas incluyendo `BirdSoundNamesResourceTest`). Comprobación de que el modelo ONNX, las etiquetas CSV, `species.json` y `labels_map.json` conservan sus huellas SHA-256 intactas.
  - Condición de cierre: cumplida; recurso trazable e íntegro, sin modificar todavía la selección de nombres en la app.

- [x] **T4 — Integrar los nombres en las detecciones.**
  - Áreas afectadas: `BirdSoundCatalog.kt`, sus pruebas unitarias e instrumentadas y los flujos de anuncio y ficha que consumen `BirdDetection`.
  - Cubre: RF-1 a RF-4, RF-6 y RF-7.
  - Depende de: T3.
  - Entrega: se cargó el catálogo de sonido `birdnet/sound_names_ar.json` en `BirdSoundCatalog`; se priorizaron sus nombres comunes (que incorporan la prioridad de Buenos Aires respaldada y nombres nacionales), con fallback al catálogo regional (`labels_map.json`), luego al español global de `species.json` y finalmente `null` (nombre científico). La disponibilidad de fichas y mapas (`localKey` y `distributionUri`) se mantuvo estrictamente desacoplada del catálogo de nombres. Se preservaron las identidades, los índices del modelo y las puntuaciones. Ante recurso ausente o inválido, la carga se degrada de forma tolerante al comportamiento anterior y registra evidencia del fallo en `lastLoadError` y log de advertencia.
  - Verificación: suite unitaria `./gradlew.bat :app:testDebugUnitTest` aprobada (73 pruebas exitosas, incluyendo prioridad, alternativas, rechazo de duplicados y vacíos, ausencia de ficha, estabilidad de índices y coherencia de anuncios/navegación en `BirdSoundCatalogTest`, `BirdSoundNamesResourceTest` y `BirdSoundResultAdapterTest`); compilación de APK `./gradlew.bat :app:assembleDebug` exitosa; `./gradlew.bat :app:lintDebug` aprobado sin errores; y suite de herramientas Python (13 pruebas exitosas). Las pruebas instrumentadas en dispositivo físico/emulador quedan registradas como pendientes debido a la ausencia de dispositivos conectados (`adb devices` vacío).
  - Condición de cierre: cumplida; integración operativa y validada con pruebas unitarias y de compilación, y limitación de dispositivo documentada explícitamente.

- [ ] **T5 — Validar cobertura y compatibilidad final.**
  - Áreas afectadas: pruebas existentes de sonido y documentación de esta spec; revisión de consumidores de `labels_map.json`.
  - Cubre: RF-1 a RF-7 y criterios de finalización.
  - Depende de: T4.
  - Entrega: resumen final de cobertura, fuentes, licencias, excepciones y resultados de las comprobaciones, incluyendo las pendientes.
  - Verificación: ejecutar `./gradlew.bat :app:testDebugUnitTest` y `./gradlew.bat :app:assembleDebug`; con dispositivo o emulador, `./gradlew.bat :app:connectedDebugAndroidTest` y comprobar uso sin conexión, anuncio y ficha. Revisar regresiones de selectores y otras funciones consumidoras, e integridad de los recursos de reconocimiento.
  - Condición de cierre: criterios demostrados con resultados observables; si falta dispositivo, datos o una comprobación obligatoria, informar el límite sin afirmar que fue realizada.

## Verificación final

- [ ] Ejecutar la validación definida en el plan e informar resultados y pendientes.
- [ ] Comprobar cada criterio de finalización de la spec y documentar la cobertura efectiva.
