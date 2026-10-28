# Plan técnico — Spec 005: Nombres argentinos en detecciones por sonido

Estado: aprobado explícitamente por el usuario el 27 de octubre de 2026.

## Referencias y alcance

- Spec: `specs/005-nombres-argentinos-sonido/spec.md`, RF-1 a RF-7.
- Mantener reconocimiento local, resultados y funciones existentes; no modificar el modelo ni sus etiquetas. No hacer commits ni push. Ejecutar una tarea por turno después de aprobar las tareas.

## Estado actual

`BirdSoundCatalog` carga `birdnet/species.json`, valida su huella y conserva los índices del modelo. Para mostrar nombres consulta `BirdLabelsManager`, compartido con otras funciones, y luego el nombre español del catálogo global. El mismo mapa local también participa en la disponibilidad de fichas y mapas. `BirdSoundActivity` utiliza el nombre de la detección en anuncios y al abrir la ficha. Hay pruebas unitarias e instrumentadas para el catálogo.

## Enfoque técnico

Mantener un catálogo de nombres específico para sonido, separado de `labels_map.json`, para no ampliar los selectores ni declarar fichas inexistentes. Indexarlo por nombre científico canónico normalizado; cada entrada tendrá un solo nombre seleccionado y verificable. El nombre de Buenos Aires prevalecerá sobre el argentino cuando haya evidencia provincial; de otro modo se utilizará el nombre argentino. El catálogo existente será alternativa para las especies sin entrada nueva, seguido por el español global y el nombre científico (RF-1 a RF-3).

La disponibilidad de fichas y mapas seguirá calculándose exclusivamente con el catálogo y recursos locales existentes, independientemente de los nombres nuevos. No se modificarán los índices, identidad, grupos ni huella de `species.json`. Los anuncios y la ficha recibirán el nombre elegido por el catálogo de sonido (RF-4, RF-6 y RF-7).

## Cambios previstos

1. Datos y documentación: evaluar la lista nacional de [Aves Argentinas](https://www.avesargentinas.org.ar/listado-de-las-aves-argentinas) y los [nombres regionales de eBird](https://support.ebird.org/es/support/solutions/articles/48000804865), que incluyen `es_AR`, junto con fuentes verificables para Buenos Aires. Fijar versión, fecha, procedencia y condiciones de uso de cada fuente antes de redistribuir datos. Que una lista sea descargable no demuestra autorización para incorporarla. No afirmar que `es_AR` sea una lista provincial (RF-2, RF-5).
2. Inventario reproducible: comparar la lista de referencia argentina con las especies de `species.json`, usando identidad científica exacta y equivalencias taxonómicas únicamente cuando estén respaldadas por una fuente identificada. Registrar especies presentes, ausentes del modelo, sin nombre adecuado y correspondencias ambiguas; no incorporar coincidencias dudosas. Registrar también cobertura numérica, versiones y método (RF-4 y RF-5).
3. `app/src/main/assets/birdnet/`: incorporar un JSON específico con nombres verificados, su documentación de procedencia y los avisos requeridos. No modificar el CSV ni `species.json`. Si no se confirma el derecho de distribución de una fuente, no incorporarla; buscar una alternativa autorizada y documentar los límites restantes.
4. `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundCatalog.kt`: cargar el nuevo catálogo y resolver el nombre mediante la prioridad indicada; mantener por separado los datos que habilitan ficha y mapa. Validar entradas duplicadas, claves vacías y correspondencias incompatibles antes de distribuir el recurso (RF-1 a RF-4, RF-7).
5. `app/src/test/java/com/example/avesargentinas/ui/sound/BirdSoundCatalogTest.kt` y `app/src/androidTest/java/com/example/avesargentinas/ui/sound/`: ampliar las pruebas existentes para resolución, integridad de recursos y disponibilidad real de fichas. Revisar el flujo de anuncio y apertura de ficha y probarlo en el nivel apropiado (RF-6).

## Datos, contratos y compatibilidad

El recurso de nombres contendrá pares de identidad científica canónica normalizada y nombre común; la procedencia y las excepciones verificadas se documentarán junto con el inventario. No requiere permisos nuevos, migración de base de datos ni conexión durante el uso. La ampliación de nombres no debe agregar aves al selector fotográfico ni habilitar fichas sin respaldo. Las observaciones existentes no se migran.

## Errores y casos límite

- Si no hay nombre verificado nuevo, usar el nombre local existente y después las alternativas actuales; no inventar traducciones.
- No unir automáticamente sinónimos ambiguos, especies divididas o agrupadas; registrarlos para revisión.
- Si un nombre nuevo existe sin ficha o mapa, mostrar la detección sin habilitar recursos ajenos.
- Si el recurso nuevo está ausente o es inválido, preservar el comportamiento anterior y dejar evidencia verificable del fallo; nunca asignar nombres a índices incorrectos.

## Estrategia de validación

- Verificación del inventario: comprobar unicidad y correspondencia con el catálogo del modelo, ejemplos verificados de Buenos Aires y Argentina, cifras de cobertura y casos no resueltos (RF-1 a RF-5).
- Pruebas unitarias: prioridad de nombres, alternativas, nombres vacíos o ausentes, identidad científica, resultados sin ficha y conservación de índices y puntuaciones (RF-1 a RF-4 y RF-7).
- Pruebas instrumentadas cuando haya dispositivo o emulador: carga real sin conexión, presentación del nombre, anuncio y apertura correcta de ficha o indicación de ausencia (RF-6 y RF-7).
- Ejecutar `./gradlew.bat :app:testDebugUnitTest` y `./gradlew.bat :app:assembleDebug`; ejecutar `:app:connectedDebugAndroidTest` si hay dispositivo disponible. Informar cualquier comprobación pendiente.
- Revisar que los usos existentes de `labels_map.json` mantengan sus datos y comportamiento y que no se hayan alterado los recursos de modelo.

## Riesgos y decisiones pendientes

- Las condiciones de redistribución y la cobertura real de las fuentes aún no están verificadas. La obtención y evaluación de datos antecede a su incorporación; no se promete cobertura total antes de medirla.
- Los nombres pueden variar dentro de Buenos Aires y las taxonomías discrepar. Solo se dará prioridad provincial con evidencia concreta y se dejarán explícitas las excepciones.
