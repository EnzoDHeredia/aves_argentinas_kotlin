# Plan técnico — Spec 002: Mejora de UI/UX de «Aprender jugando»

Estado: aprobado explícitamente por el usuario el 20 de octubre de 2026. Fecha: 20 de octubre de 2026.

## Referencias y alcance

- [Spec 002 aprobada](spec.md): RF-1 a RF-20, requisitos no funcionales y nueve criterios de finalización.
- [Spec 001](../001-aprender-jugando/spec.md): reglas de juego y conservación o descarte de sesiones que deben preservarse.
- [Validación previa](../001-aprender-jugando/verificacion-t8.md): evidencia histórica y comprobaciones pendientes, sin aprobación final de integración.
- [AGENTS.md](../../AGENTS.md): Android Views y XML, componentes y recursos existentes, aprobación separada de plan y tareas, una tarea por turno durante la implementación y sin commits ni push.
- [frontend-design](../../.agents/skills/frontend-design/SKILL.md): coherencia visual, jerarquía, fotografía protagonista y decoración mínima, adaptadas al diseño existente de Android.

Este plan define el enfoque técnico; no constituye una lista de tareas ni autoriza implementación. Se mantendrá la estructura actual, sin nuevas dependencias ni modificaciones al motor de juego.

## Estado actual

- `LearningGameActivity` representa selector, ronda y resumen en una sola pantalla. Observa `LearningGameViewModel.state` y usa un token por presentación para impedir acciones sobre tarjetas anteriores.
- `activity_learning_game.xml` tiene una cabecera y un `NestedScrollView` con `fillViewport`, pero su contenido vertical no distribuye el espacio libre. Modalidades y resumen se componen de textos y botones poco agrupados.
- `renderCard()` oculta la foto en el dorso. `updateInteraction()` elimina de la distribución los controles de evaluación cuando no corresponden. Ambos comportamientos producen cambios de altura y ubicación al girar.
- El giro reversible, el bloqueo de interacciones, la carga con Glide y el manejo de fotos inválidas ya existen. Se conservarán sus validaciones y sus tokens.
- La tarjeta se expone a TalkBack como un nodo agrupado; sus descendientes visuales se excluyen de accesibilidad. Cualquier botón independiente debe quedar fuera de ese grupo.
- `BirdInfoActivity` recibe los nombres común y científico, busca el mapa local y abre `ImageZoomActivity`. Actualmente conserva una URI incluso si la carga falla y muestra únicamente un icono de error, sin un aviso textual permanente.
- Las pruebas de `LearningGameActivityTest` ya cubren navegación, giro, recreación, accesibilidad, fotos inválidas, temas, capturas y recorridos sin conexión. Algunas aserciones sobre vistas deberán adaptarse a la nueva composición.

## Enfoque técnico

### Distribución e identidad visual — RF-1 a RF-5, RF-18 y RF-19

Conservar una sola actividad y el desplazamiento existente. Bajo la cabecera, el hijo directo del `NestedScrollView` mantendrá altura natural y `gravity="center_vertical"`; `fillViewport` le dará como mínimo el espacio disponible. Así el bloque activo queda centrado cuando cabe y crece con desplazamiento desde arriba cuando no cabe. Los estados inactivos usarán `GONE`, de modo que selector, ronda y resumen no se sumen entre sí.

Los avisos pertenecerán al mismo bloque que el estado activo y participarán de su medida. La cabecera permanecerá fuera del contenido desplazable. Se respetarán los insets actuales de `BaseActivity`, sin sumar dos veces las barras; las separaciones del nuevo contenedor de cabecera se preservarán al aplicar los insets laterales y superiores.

La dirección visual reutilizará los recursos existentes:

| Función | Recursos y tratamiento |
| --- | --- |
| Fondo y superficies | `brand_background`, `brand_surface` y `brand_surface_variant`, con sus variantes nocturnas. |
| Acción y contornos | `brand_primary`, `brand_outline` y los colores de texto sobre acción existentes. |
| Texto | Familia de la app; títulos de 24–28 sp, cuerpo de 16–18 sp y datos secundarios de 14–16 sp, siempre con crecimiento según la fuente del sistema. |
| Ritmo y forma | Márgenes de pantalla existentes de 20–24 dp, separaciones de 12–16 dp y superficies redondeadas de 18–20 dp, sin sombras decorativas ni una paleta adicional. |
| Botones | Reutilizar los estilos `Widget.AvesArgentinas.Button.Primary` y `.Outlined` donde correspondan; acciones de consulta y navegación con menor énfasis, manteniendo 48 dp de área táctil. |

El selector tendrá dos superficies de igual tratamiento. Cada una agrupará título, descripción y disponibilidad, con un único control de inicio claramente identificado. Se conservarán los identificadores `learning_questions` y `learning_species` para esos controles, sin convertir los textos descriptivos en acciones adicionales ni duplicar el foco accesible.

El resumen agrupará total y cantidades en una superficie. Cuando `state.canReview` sea verdadero, «Repasar marcadas» será la acción principal y «Jugar de nuevo» una alternativa delineada. Sin marcadas, se ocultará el repaso y «Jugar de nuevo» adoptará el tratamiento principal. Se aplicarán estilos completos coherentes en ambos casos, incluidos texto, fondo y borde; no se cambiará únicamente el color del fondo.

### Tarjeta estable con foto compartida — RF-6 a RF-12 y RF-20

Mantener una sola imagen de la tarjeta actual y una sola petición de Glide asociada a su token. El `ImageView` será visible tanto en frente como en dorso de `LearningCard.Photo`, con `fitCenter`, sin cambiar su fuente al girar. Para preguntas permanecerá ausente.

Dentro de la tarjeta, separar las dos caras de texto en paneles superpuestos dentro de un `FrameLayout`. Ambos se medirán; el inactivo quedará `INVISIBLE`. El espacio textual será el máximo de ambas caras para esa tarjeta, no una altura fija que pueda cortar nombres largos. La foto compartida quedará fuera de esos paneles, dentro de la tarjeta que gira. El frente de una pregunta y su respuesta usarán los mismos paneles, con altura natural para su contenido.

Los controles estarán fuera del nodo accesible de la tarjeta. Un segundo contenedor superpuesto alojará las acciones de frente y respuesta; su cara inactiva quedará también `INVISIBLE` para reservar el espacio máximo sin permitir interacción ni exponer controles. Esto evita que revelar, ocultar o bloquear temporalmente botones desplace todo el bloque. Las vistas inactivas se excluirán explícitamente del foco y del árbol accesible; no bastará con deshabilitarlas o hacerlas transparentes.

Se mantendrá el giro al activar la tarjeta y habrá un botón explícito para «Mostrar respuesta» o «Volver al frente», conectado a la misma operación `flip(token)`. La consulta de ficha y la evaluación serán botones hermanos, sin propagación de su activación al giro. Los identificadores actuales se conservarán cuando mantengan su responsabilidad; las pruebas que asumían un único texto mutable se ajustarán para consultar la cara activa.

El área de foto será estable por tarjeta y configuración. Se conservará el máximo actual de 360 dp y se añadirá un mínimo de referencia de 180 dp. Tras medir el espacio útil y el resto del bloque, se ajustará el alto del área dentro de esos límites para aprovechar el espacio disponible. Si ni con el mínimo cabe todo, se habilitará el desplazamiento; no se reducirán textos ni objetivos táctiles para forzar el encaje. La imagen conservará sus proporciones dentro de esa área.

La medida se recalculará al cambiar tarjeta, ancho, altura útil o escala de texto, considerando siempre ambas caras. No cambiará por un giro. Las actualizaciones de altura se aplicarán solo si difieren de la medida vigente, evitando ciclos de relayout. No se agregará una vista personalizada ni un sistema general de medición.

La animación existente seguirá cambiando el estado antes de presentar el giro, cancelándose al detenerse y dibujando la cara confirmada al regresar. `updateInteraction()` distinguirá entre estado visible, controles reservados sin interacción y acciones habilitadas. La descripción accesible de la tarjeta se construirá únicamente con la cara activa; antes de revelar no incluirá nombres, rutas ni la ficha de la especie.

### Consulta de la ficha y retorno — RF-13 a RF-17

Añadir un botón `learning_bird_info`, usando el texto ya disponible `bird_info_view`. Estará disponible solo para una fotografía revelada y lista para interactuar, tanto en ronda como en repaso. Las preguntas y los frentes no lo expondrán.

En `LearningGameActivity`, una operación local `openBirdInfo(expectedToken: Long)` comprobará token, pantalla, cara revelada, tipo `LearningCard.Photo`, imagen lista y ausencia de transición. Abrirá `BirdInfoActivity` con un `Intent` explícito y los contratos existentes `EXTRA_COMMON_NAME` y `EXTRA_SCIENTIFIC_NAME`, tomados de esa misma tarjeta. No utilizará predicciones previas ni inferirá la especie a partir de textos de la pantalla.

La apertura usará un indicador local para bloquear dobles toques antes de que la actividad pierda el foco. Se restablecerá al regresar por `onResume()`, o si la apertura falla, y participará del bloqueo de giro y evaluación mientras se navega. Un fallo de apertura mostrará un aviso breve y dejará la respuesta disponible, sin evaluar ni limpiar la sesión.

Se mantendrá la pila normal juego → ficha → zoom, sin `finish()`, limpieza de sesión, flags que reordenen actividades ni resultados de actividad. Atrás desde el zoom regresará a la ficha y desde la ficha al juego. La sesión continuará en el ViewModel de la actividad del juego; `onStop()` cancelará solo la presentación visual. No se añadirá serialización: tras muerte del proceso, volver al juego mostrará el selector, conforme a la spec 001.

### Mapas locales y ausencia de imagen — RF-14 y RF-16

Mantener `resolveDefaultDistributionImage()` en `BirdInfoActivity` como punto único de resolución. Respetar la prioridad actual: URI explícita si existe, recursos locales por nombre y ruta de assets como alternativa. No introducir un repositorio de mapas ni modificar el catálogo del clasificador.

El inventario local contiene 45 claves fotográficas; cuatro no coinciden directamente con los nombres de mapas examinados. Tres tienen un mapa existente bajo otro nombre científico. Se resolverán con estas correspondencias explícitas, limitadas a la búsqueda del recurso; no cambiarán las etiquetas ni la identidad del juego:

| Clave del juego | Recurso local existente | Respaldo de la correspondencia |
| --- | --- | --- |
| `anas_versicolor` | `spatula_versicolor_dist` | [IUCN: Spatula versicolor y su sinónimo Anas versicolor](https://doi.org/10.2305/IUCN.UK.2016-3.RLTS.T22680329A92855919.en). |
| `anas_cyanoptera` | `spatula_cyanoptera_dist` | [NatureServe: Spatula cyanoptera, antes incluida en Anas](https://explorer.natureserve.org/Taxon/ELEMENT_GLOBAL.2.100234/Spatula_cyanoptera). |
| `anas_sibilatrix` | `mareca_sibilatrix_dist` | [GBIF: Anas sibilatrix como sinónimo de Mareca sibilatrix](https://www.gbif.org/taxon/DGPD/metrics). |

Las referencias se consultaron el 20 de octubre de 2026 para respaldar la asociación de nombres; la app no las consultará en ejecución. Se probará primero el nombre original y luego su correspondencia explícita. No se sustituirán géneros por semejanza de texto ni se elegirá otro mapa aproximado.

Para `aramides_ypecaha` no se encontró un recurso correspondiente en las rutas locales examinadas. Se mostrará el estado de mapa no disponible definido por la spec; no se descargará ni fabricará uno. La cobertura se comprobará de nuevo en implementación, ya que el árbol de trabajo puede cambiar.

Añadir un aviso textual de indisponibilidad dentro del área del mapa, usando recursos de texto. Al iniciar una carga, ocultar ese aviso y dejar la ampliación deshabilitada. Solo tras una carga exitosa se conservará `distributionImageUri` y se habilitará el zoom. Si Glide falla o no hay fuente, limpiar la URI, mostrar el aviso y mantener disponible Atrás. La ficha no comunicará este fallo al motor del juego, por lo que la tarjeta no se descartará ni evaluará.

La intervención en `activity_bird_info.xml` se limitará al aviso y a su accesibilidad. `ImageZoomActivity`, sus gestos y las entradas existentes desde identificación conservarán sus contratos.

## Cambios previstos

Las rutas Kotlin de producción de la tabla son relativas a `app/src/main/java/com/example/avesargentinas/`.

| Archivo o área | Cambio | Requisitos |
| --- | --- | --- |
| `app/src/main/res/layout/activity_learning_game.xml` | Cabecera coherente, bloque centrable y desplazable, modalidades agrupadas, caras de texto y controles medidos conjuntamente, foto compartida y resumen con jerarquía. | RF-1 a RF-13, RF-18 a RF-20 |
| `ui/learning/LearningGameActivity.kt` | Renderizado, medida estable de foto, acciones y accesibilidad; apertura protegida de ficha y estilos de resumen según `canReview`. | RF-5 a RF-15, RF-17 a RF-20 |
| `ui/bird/BirdInfoActivity.kt` | Correspondencias locales explícitas, manejo de carga y fallo de mapa y zoom habilitado solo con imagen válida. | RF-14, RF-16 |
| `app/src/main/res/layout/activity_bird_info.xml` | Aviso textual de mapa no disponible, sin rediseñar la ficha. | RF-16 |
| `app/src/main/res/values/strings.xml` y `dimens.xml` | Solo textos y dimensiones necesarios; reutilizar `bird_info_view`, nombres de acciones, colores, temas y estilos existentes. | RF-1 a RF-4, RF-8 a RF-13, RF-16, RF-18 y RF-19 |
| `app/src/androidTest/java/com/example/avesargentinas/ui/learning/LearningGameActivityTest.kt` | Adaptar aserciones a las caras activas y añadir pruebas de geometría, foto en respuesta, ficha, retorno y prioridad de resumen, reutilizando sus helpers. | RF-1 a RF-20 y regresiones de la spec 001 |
| `app/src/androidTest/java/com/example/avesargentinas/ui/bird/BirdInfoActivityTest.kt` (nuevo) | Probar mapas originales, correspondencias, URI explícita, fallos, aviso y disponibilidad del zoom. | RF-14 y RF-16 |

No se prevén cambios en `LearningSession`, modelos, ViewModel, cargador de contenido, assets del juego, base de datos, manifiesto, Gradle o estilos compartidos. No se creará un framework de navegación o de componentes para este cambio.

## Datos, contratos y compatibilidad

- No hay migraciones, nuevas tablas, preferencias, permisos, endpoints ni telemetría. No se persisten partidas ni se escriben observaciones.
- `LearningCard.Photo` ya contiene nombres, clave canónica y ruta de foto: no requiere campos adicionales ni cambios al orden de etiquetas del modelo.
- Se conservan el `StateFlow`, los tokens, `SelfAssessment`, la selección de tarjetas y los conteos. La navegación es un efecto de la interfaz, sin una nueva pantalla en `LearningScreen`.
- La ficha mantiene sus extras actuales, incluida la precedencia de `EXTRA_DISTRIBUTION_URI` para consumidores anteriores. El juego enviará nombres y dejará que la ficha resuelva el mapa local.
- Se mantiene `minSdk = 24` y la configuración de dependencias presente. Los cambios previos del usuario, incluido Room 2.7.2 actualmente declarado, se preservarán.
- Los colores y estilos compartidos se consumirán sin cambiar su comportamiento en otras pantallas. La cabecera del juego tomará como referencia la ficha y las superficies actuales, sin copiar separaciones superiores fijas que sustituyan los insets del dispositivo.

## Errores y casos límite

- Foto inválida: conservar el descarte o reemplazo por token que ya implementa el juego. Un error de mapa no recorrerá ese camino.
- Giro o consulta simultánea: bloquear las acciones mientras el giro o la apertura de ficha esté en curso; los callbacks antiguos seguirán verificando el token. Una evaluación aceptada invalidará las acciones de la tarjeta anterior.
- Cara oculta medida: `INVISIBLE` y exclusión de accesibilidad para no revelar nombres, botones ni respuestas. El contenido visual reservado no será alcanzable por toque, teclado ni TalkBack.
- Texto largo o fuente al 200 %: medir ambas caras con el ancho y la fuente reales; priorizar el desplazamiento sobre reducir texto o controles. Las reservas se recalcularán tras una nueva configuración.
- Cambio de tarjeta: recalcular medidas para el nuevo contenido y conservar el reinicio de desplazamiento existente. Girar o volver de la ficha no reiniciará la partida ni seleccionará otra foto.
- Rotación con ficha o zoom en primer plano: al regresar, reconstruir desde el ViewModel retenido y volver a cargar la misma foto si se recrearon las vistas. La recreación no contará como una evaluación.
- Mapa inexistente, URI ilegible o archivo no decodificable: mismo aviso permanente y zoom deshabilitado; Atrás seguirá operativo. La URI explícita fallida no se reemplazará silenciosamente por otra fuente.
- Sin tarjetas marcadas: ocultar por completo la acción de repaso y aplicar a nueva partida el estilo principal; las cantidades seguirán siendo autoevaluaciones de la última pasada.

## Estrategia de validación

### Pruebas automatizadas

Usar JUnit, ActivityScenario, Espresso y los helpers existentes, sin nuevas dependencias. Para lógica de interacción nueva, escribir primero el caso que falla y luego el cambio mínimo. Las pruebas visuales de geometría se sincronizarán con carga y layout terminados, sin pausas fijas.

| Comprobación y aserciones | Requisitos |
| --- | --- |
| Selector y resumen: cuando el contenido cabe, el espacio libre superior e inferior del bloque difiere como máximo 2 dp; con contenido mayor, se alcanza la última acción y el comienzo mediante scroll. Medir dentro del viewport útil, descontando padding e insets. | RF-1 a RF-4, RF-18 y RF-19 |
| Modalidades cargando, disponibles, insuficientes y vacías: etiquetas y disponibilidad agrupadas, inicio bloqueado cuando corresponde y navegación conservada. | RF-4 y RF-5 |
| Foto vertical y horizontal de prueba: mismo contenido cargado en frente y dorso, mismos límites del área de foto y del bloque al girar, con tolerancia de 2 dp cuando ambos estados caben. Comprobar proporción y ausencia de recorte. | RF-6 a RF-10 |
| Frente → respuesta → frente → respuesta: misma tarjeta y fotografía, controles correctos, cero evaluaciones y ninguna respuesta o ficha en el árbol accesible del frente. Repetir con nombre largo y texto al 200 %. | RF-6 a RF-12 |
| Preguntas: frente y respuesta legibles, aclaración cuando exista, ficha ausente y autoevaluación única. | RF-11 y RF-12 |
| Respuesta fotográfica → ficha → zoom → ficha → juego: nombres y URI esperados; estado de sesión completo idéntico al previo a consultar. Repetir durante un repaso y con rotación; después, evaluar una sola vez. | RF-13 a RF-15 y RF-20 |
| Abrir ficha mediante toques rápidos: una sola instancia de ficha en primer plano, sin giros, avance ni evaluaciones. Repetir un acceso con token anterior y un toque durante el giro. | RF-12 y RF-17 |
| Ficha con nombre original, cada una de las tres correspondencias y URI explícita válida: recurso esperado y zoom disponible solo al completar carga. Mapa ausente e ilegible: aviso visible, URI no utilizable y zoom bloqueado, sin bloqueo de Atrás. | RF-14 y RF-16 |
| Mapa fallido durante partida: volver a la misma respuesta con la misma foto, progreso y evaluaciones. Probar una fuente deliberadamente inválida sin modificar assets publicados. | RF-15 y RF-16 |
| Resúmenes con 0, 1 y varias marcadas: conteos exactos y prioridad visual correcta. Repetir la secuencia 10/7/3 → 3/2/1 → 1/1/0 sin alterar las tarjetas seleccionadas. | RF-18 a RF-20 |

Para fotos de proporciones distintas, reutilizar imágenes de prueba o assets reales ya existentes; no incorporar contenido editorial nuevo. Los casos de mapa ilegible usarán archivos temporales de prueba con limpieza al terminar. Mantener las pruebas puras de `LearningSessionTest` como regresión; no añadir pruebas unitarias que dupliquen un motor sin cambios.

### Revisión en dispositivo o emulador

- Capturar selector, pregunta, respuesta general, frente fotográfico, respuesta fotográfica y resúmenes con y sin marcadas. Comparar con la ficha y el menú actuales, comprobando jerarquía, centrado y acciones, además de los límites geométricos automatizados.
- Cubrir claro y oscuro, vertical y horizontal, tamaño pequeño y texto al 200 %. Revisar fotografías verticales y horizontales, nombres largos, avisos y desplazamiento hasta la última acción.
- Usar TalkBack real para revelar, consultar ficha, regresar, volver al frente y evaluar. Escuchar los nombres de las acciones y comprobar que el frente no filtre la respuesta; una inspección del árbol por sí sola no completa esta prueba.
- Probar animaciones desactivadas y activadas, incluido un giro interrumpido, en API 24 o 25 y en API 26 o superior, según la compatibilidad ya requerida.
- Completar ambas modalidades y consultas de mapas disponibles sin conexión, con permisos de cámara, ubicación y fotos denegados. Restaurar preferencias y conectividad utilizadas en la prueba.
- Comprobar segundo plano y retorno, rotación en ficha y zoom y cierre real del proceso desde el host. Tras un cierre, el regreso al juego debe mostrar selector; `ActivityScenario.recreate()` no demuestra muerte del proceso.
- Comprobar acceso anterior a ficha desde identificación, ampliación de mapa, Atrás de la partida y regreso al menú, sin crear observaciones.

### Comandos y tratamiento de bloqueos

Desde la raíz y con la configuración oficial del proyecto, ejecutar durante la implementación:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

El primer ciclo de implementación comprobará la compilación oficial para establecer la situación actual. Las tareas posteriores usarán las pruebas de su cambio; la validación final ejecutará el conjunto pertinente y registrará dispositivo, API y resultados. No se ejecutan estos comandos para aprobar este plan documental.

El informe de la spec 001 describe un fallo histórico Room 2.6.1/Kotlin 2.2.10, pero el árbol actual declara Room 2.7.2. No se supondrá ni que el fallo sigue presente ni que está resuelto: se requiere evidencia nueva con el Wrapper. Si aparece un bloqueo ajeno al alcance, registrar el diagnóstico y la comprobación pendiente; no alterar versiones ni usar una copia con otra configuración como prueba de integración oficial. Los pendientes heredados de Lint, API 24/25 y TalkBack conservan su estado hasta verificarse de forma explícita.

## Riesgos y decisiones pendientes

- La estabilidad de altura exige medir ambas caras y controles ocultos. Se validará el árbol accesible completo para que esa reserva no revele la respuesta; mantener solo una fotografía evita duplicar cargas y memoria.
- El ajuste de foto depende del espacio útil real y de la fuente. Se compararán posiciones antes y después de girar y se evitarán actualizaciones de medida que produzcan relayout continuo.
- La conservación de sesión depende del ViewModel y no cubre muerte del proceso, por decisión aprobada. El retorno desde ficha y zoom requiere pruebas con recreación y con cierre real por separado.
- La cobertura de mapas es la del paquete local. Las tres correspondencias explícitas reutilizan mapas existentes; `aramides_ypecaha` mostrará indisponibilidad mientras no se incorpore un mapa en un alcance posterior.
- Los cambios del usuario y la evidencia histórica de compilación difieren en la versión de Room. La validación actual de la configuración oficial está pendiente y no se ha inferido a partir de informes anteriores.

No hay decisiones funcionales pendientes ni ampliaciones propuestas. El usuario aprobó explícitamente este plan el 20 de octubre de 2026. El siguiente artefacto es `tasks.md`, que requiere su propia aprobación antes de implementar. Tras aprobar las tareas se implementará una sola por turno, con sus comprobaciones y confirmación antes de la siguiente, sin commits ni push.
