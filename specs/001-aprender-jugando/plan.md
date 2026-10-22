# Plan técnico — Spec 001: Aprender jugando

Estado: aprobado por el usuario el 19 de octubre de 2026. Fecha: 19 de octubre de 2026.

## Referencias y alcance

- Spec: [Aprender jugando](spec.md), aprobada por el usuario el 19 de octubre de 2026.
- Contenido: [banco inicial de 40 preguntas](../../docs/contenido/banco-preguntas-aves-v1.md) y selección revisada de la colección de fotografías aportada por el usuario.
- Requisitos cubiertos: RF-1 a RF-30 y requisitos no funcionales de la spec.
- Restricciones: [AGENTS.md](../../AGENTS.md), Android Views y XML, componentes y temas existentes, SDK mínimo 24. El proyecto no tiene `docs/constitution.md`.
- Rama actual: `feature/juego-interactivo`, creada por el usuario. No crear otra rama, hacer commits ni push como parte de este flujo.
- Este documento define el enfoque y la validación. La descomposición en tareas será un artefacto posterior, después de la aprobación de este plan.

## Estado actual

- `MainMenuActivity.kt` abre identificación, observaciones y opciones. Su layout usa un contenido vertical con logo, espacios flexibles y botones; agregar otro acceso exige comprobar el espacio disponible y el desplazamiento.
- La captura instantánea puede omitir el menú al abrir la aplicación. La vuelta desde identificación ya usa `from_back_press` para mostrarlo; se conservará ese recorrido.
- Las pantallas heredan de `BaseActivity` y aplican `ThemeManager`. Existen colores semánticos para claro y oscuro y componentes Material.
- `BirdLabelsManager` obtiene nombres comunes mediante claves de especie de `labels_map.json`. Esas claves también sirven al clasificador; no deben modificarse para el juego.
- Están disponibles Glide, coroutines, AndroidX Activity, Lifecycle, JUnit y Espresso. El módulo declara `lifecycle-runtime-ktx:2.7.0`; algunas versiones del catálogo de Gradle difieren de las declaraciones que realmente usa el módulo. Este trabajo no incluye actualizarlas ni unificarlas.
- No existen una sesión de juego, pantallas educativas ni contenido fotográfico de identificación empaquetado para esta funcionalidad. El banco de preguntas está documentado, pero todavía no es un recurso consumible por la app.

## Enfoque técnico

### Pantalla y navegación

Una sola `LearningGameActivity`, dentro de `ui/learning`, representará tres estados de pantalla: selector de modalidades, tarjeta y resumen. Heredará de `BaseActivity`, aplicará el tema guardado y usará una barra de navegación y un layout XML con contenedores para esos estados. No se incorporarán fragments, navegación adicional ni un framework de interfaz.

El nuevo botón del menú se ubicará entre «Mis Observaciones» y «Opciones». El contenido del menú deberá poder desplazarse cuando el espacio o el tamaño de fuente lo exijan, preservando sus accesos y el tratamiento de insets. La actividad de juego será interna, sin exportación ni permisos propios. Esto cubre RF-1, RF-2, RF-4 y RF-27.

Atrás desde tarjeta o resumen limpiará la sesión y mostrará el selector. Atrás desde el selector terminará la actividad, dejando visible el menú existente. «Volver al menú» limpiará la sesión y terminará la actividad; «Elegir modalidad» la limpiará sin terminarla. Se usará `OnBackPressedDispatcher`, manteniendo la lógica de captura instantánea donde ya existe. RF-22 y RF-23.

### Sesión en memoria

`LearningGameViewModel`, con alcance de la actividad, conservará el estado durante rotaciones y retornos del segundo plano. Se agregará una declaración explícita de `androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0`, alineada con el Lifecycle que ya utiliza el módulo, para `ViewModel` y `viewModelScope`.

El ViewModel poseerá una `LearningSession` de Kotlin puro, responsable de las reglas de ronda, giro, evaluación, resumen y repaso. Expondrá un estado inmutable mediante `StateFlow`; la actividad lo observará con el ciclo de vida existente. Las lecturas de contenido irán fuera del hilo de interfaz. El cargador usará recursos de la aplicación, sin conservar referencias a una actividad.

No habrá `SavedStateHandle`, serialización de la partida en `Bundle`, repositorio global de sesiones ni escritura en Room, preferencias o archivos. El ViewModel conservará la instancia al recrear la actividad; al terminarla o morir el proceso se perderá la sesión. Una nueva instancia comenzará en el selector, aun si Android intenta restaurar la jerarquía visual anterior: la presentación se reconstruirá exclusivamente desde el estado nuevo, sin restaurar caras, tarjetas o resultados de las vistas. Esta elección satisface RF-24 a RF-26 y se apoya en la distinción de Android entre [ViewModel y estado guardado](https://developer.android.com/topic/libraries/architecture/saving-states).

### Reglas de juego y contratos internos

Los tipos pequeños vivirán juntos en `LearningModels.kt`:

| Contrato | Contenido y responsabilidad |
| --- | --- |
| `LearningMode` | Preguntas generales o identificación de especies. |
| `LearningCard` | Tarjeta de pregunta, o tarjeta fotográfica con clave canónica de especie, nombres y ruta interna de imagen. |
| `SelfAssessment` | `KNOWN` o `REVIEW`; nunca se presentará como corrección automática. |
| `LearningState` | Pantalla, modalidad, tipo de pasada, tarjetas elegidas, posición, cara visible, evaluaciones de la pasada, aviso vigente y token de la tarjeta actual. |

`LearningSession` recibirá el catálogo validado y una fuente de azar intercambiable para pruebas. Sus operaciones serán iniciar ronda, cambiar cara, evaluar tarjeta actual, iniciar repaso, descartar tarjeta no disponible y limpiar sesión. La sesión será la única autoridad para índices, marcas y totales; el ViewModel delegará esas reglas en ella.

- Ronda normal: elegir hasta 10 preguntas de identificadores distintos. Para fotos, agrupar primero por clave de especie, elegir especies distintas y después una foto de cada una. Así una especie no pesa más por tener más imágenes o carpetas de distinto sexo. RF-3, RF-8 y RF-29.
- Cada presentación de tarjeta tendrá un token nuevo, distinto incluso entre rondas o repasos de la misma tarjeta. Las acciones de giro, evaluación y error de imagen incluirán el token esperado. Una acción de una tarjeta anterior se ignorará. RF-12 y RF-14.
- Evaluación: se aceptará únicamente desde el dorso de una tarjeta vigente y no evaluada. La operación registrará la elección y avanzará de forma conjunta, dejando el nuevo frente visible o produciendo el resumen. RF-9, RF-13 a RF-16.
- Repaso: copiar únicamente las marcadas `REVIEW` de la pasada recién finalizada, conservando la pregunta o fotografía elegida. Reiniciar índice, cara y evaluaciones; no completar hasta 10. El resumen siguiente contará solo esa pasada. RF-17 a RF-20.
- Nueva ronda o salida: descartar las marcas y resultados anteriores. Se permite que una nueva ronda repita contenidos de una anterior. RF-21 a RF-24.

### Tarjeta, animación y accesibilidad

La tarjeta central será el foco visual. Se seguirán [frontend-design](../../.agents/skills/frontend-design/SKILL.md) y los recursos existentes: superficie `brand_surface`, fondo `brand_background`, acción `brand_primary`, textos `text_primary` y `text_secondary`, con sus variantes nocturnas. Como referencia del tema claro, esos recursos son `#F5F7FA`, `#E8EDF5`, `#1F4B99`, `#1F2C40` y `#556177`. Se conservará la familia tipográfica de la app, con pregunta destacada, respuesta legible y nombre científico en cursiva. No se agregarán tipografías descargadas ni una paleta independiente.

El selector tendrá dos opciones con descripciones breves. La pantalla de tarjeta mostrará posición, contenido y, solo al revelar, los dos botones de autoevaluación. En la modalidad fotográfica, Glide mostrará la fotografía completa con ajuste proporcional, evitando recortes automáticos. El resumen mostrará cantidades y las acciones acordadas, sin gráficos ni estadísticas adicionales.

Se utilizará rotación sobre el eje vertical en dos fases, cambiando el contenido cuando la tarjeta quede de canto. La cara de destino se registrará antes de iniciar la animación; esta será una presentación visual y nunca registrará una evaluación en sus callbacks. La actividad bloqueará temporalmente las acciones durante el giro o cambio de tarjeta y cancelará animaciones al detenerse. Al volver o recrearse, dibujará directamente la cara de destino y dejará libres los controles, evitando bloqueos por una animación interrumpida. RF-10 a RF-14 y RF-25.

En API 26 o superior se consultará [ValueAnimator.areAnimatorsEnabled](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled()); en API 24 y 25 se leerá la [escala de duración de animadores](https://developer.android.com/reference/android/provider/Settings.Global#ANIMATOR_DURATION_SCALE). Con animaciones desactivadas, o si no se puede conocer la preferencia, se revelará inmediatamente sin bloquear la interacción. Las animaciones habilitadas respetarán la escala del sistema.

La cara oculta quedará fuera del árbol de accesibilidad. La foto tendrá una descripción neutral, sin usar el nombre del archivo, la especie ni los textos del dorso. Al revelar se actualizarán descripción y foco accesible; el gesto equivalente de activación del lector de pantalla tendrá el mismo resultado que tocar. Los controles tendrán un área táctil mínima de 48 dp. Los contenedores admitirán desplazamiento para texto ampliado y horizontal, sin alturas rígidas que recorten preguntas, respuestas o botones.

## Cambios previstos

Las rutas Kotlin siguientes son relativas a `app/src/main/java/com/example/avesargentinas/`.

| Área o archivo | Cambio y motivo | Requisitos |
| --- | --- | --- |
| `MainMenuActivity.kt` y `app/src/main/res/layout/activity_main_menu.xml` | Agregar el acceso y permitir su uso con poco espacio, sin modificar las rutas de identificación, observaciones u opciones. | RF-1, RF-4 |
| `ui/learning/LearningGameActivity.kt` y `app/src/main/res/layout/activity_learning_game.xml` | Selector, tarjeta, giro, resumen, avisos, navegación y accesibilidad. | RF-2, RF-5, RF-6, RF-7, RF-9 a RF-23, RF-27 a RF-30 |
| `ui/learning/LearningGameViewModel.kt` | Conservar sesión en memoria, cargar catálogo y conectar las acciones con el estado observado. | RF-24, RF-25, RF-26, RF-28 a RF-30 |
| `ui/learning/LearningModels.kt` y `ui/learning/LearningSession.kt` | Tipos y reglas puras de selección, evaluación, repaso y descarte. | RF-3, RF-8, RF-9, RF-11 a RF-24, RF-28 a RF-30 |
| `ui/learning/LearningContentLoader.kt` | Leer, validar y relacionar preguntas, imágenes y nombres existentes. | RF-5 a RF-8, RF-27 a RF-30 |
| `app/src/main/assets/learning/questions.json`, `photo_cards.json` y `photos/` | Empaquetar 40 preguntas y un primer conjunto de 45 fotos revisadas, una por especie. | RF-5, RF-6, RF-7, RF-27 |
| `docs/contenido/seleccion-fotos-aprender-jugando.md` | Registrar la correspondencia entre cada foto elegida y su archivo de origen, sin rutas absolutas del equipo, y el resultado de la revisión visual. | Calidad editorial y cobertura inicial |
| `app/src/main/AndroidManifest.xml` | Registrar la actividad interna del juego, sin nuevos permisos. | RF-1, RF-27 |
| `app/src/main/res/values/strings.xml` y `dimens.xml` | Incorporar etiquetas, formatos de progreso, avisos, descripciones accesibles y dimensiones necesarias. Reutilizar colores y estilos existentes. | RF-2, RF-9, RF-13, RF-16, RF-20, RF-22, RF-28 a RF-30 |
| `app/build.gradle.kts` | Declarar ViewModel KTX 2.7.0; mantener las demás versiones. | RF-25 y RF-26 |
| Pruebas de `LearningSession`, `LearningContentLoader` y `LearningGameActivity` | Cubrir reglas, integridad del contenido y navegación/ciclo de vida con las herramientas existentes. | RF-1 a RF-30 |

## Datos, contratos y compatibilidad

### Contenido estático

`questions.json` conservará los identificadores `AVE-001` a `AVE-040`, el tema, nivel editorial, pregunta, respuesta y referencias de respaldo del documento aprobado. Tema y nivel serán metadatos de contenido; no se agregarán filtros a la interfaz. No se analizará Markdown durante la ejecución.

Cada entrada de `photo_cards.json` tendrá identificador, clave de especie existente y ruta relativa dentro de `learning/photos/`. Los nombres comunes se resolverán usando `BirdLabelsManager`; el nombre científico se formará a partir de la clave canónica, sustituyendo separadores por espacios y capitalizando el género. La normalización de prefijos numéricos y sufijos de sexo se hará al preparar la selección, sin cambiar el catálogo del clasificador.

Se elegirán manualmente 45 fotos claras, una por especie disponible, revisando correspondencia de etiqueta, nitidez y visibilidad del ave; no se tomarán automáticamente las primeras imágenes de cada carpeta. Se exportarán copias WebP con orientación aplicada, lado mayor de hasta 1.024 píxeles, sin ampliar originales pequeños y con calidad inicial 85, revisando que la compresión conserve los rasgos de identificación. Se medirá el peso total del paquete y se registrará junto a la selección. Los originales no se modificarán ni se copiará el dataset completo.

El cargador usará `org.json`, ya disponible en Android. Validará campos obligatorios, identificadores únicos, claves presentes en el catálogo y rutas limitadas a los assets de aprendizaje; descartará referencias externas o fuera de esa ubicación. Comprobará existencia y dimensiones decodificables de las fotos fuera del hilo principal, sin mantener todos sus bitmaps en memoria. La decodificación visible mediante Glide también tendrá tratamiento de errores. RF-5 a RF-8 y RF-27 a RF-30.

### Sesiones, permisos y compatibilidad

No habrá migraciones de datos, endpoints, cuentas, nuevas solicitudes de permisos ni cambios en `ObservationDatabase`, `ObservationRepository`, `SettingsManager` o el clasificador. Glide cargará únicamente recursos locales de esta modalidad, sin caché de disco adicional para las fotos del juego. Los datos estáticos no contienen elecciones del jugador.

La actividad de identificación seguirá siendo responsable de sus permisos. Abrir el juego desde el menú no instanciará el clasificador, abrirá la cámara ni consultará observaciones o ubicación. Se conservarán `minSdk = 24`, el tema y la configuración de captura instantánea.

## Errores y casos límite

- Archivo de contenido ilegible o JSON completo mal formado: deshabilitar solo la modalidad afectada y mostrar un mensaje; la otra modalidad y la navegación seguirán disponibles. Una entrada inválida en un documento legible se descartará sin invalidar las demás.
- Duplicados: conservar solo la primera entrada válida por identificador. Agrupar las fotos por especie antes de elegir una ronda. La validación del paquete publicado exigirá que su contenido no dependa de este descarte para alcanzar las cantidades aprobadas.
- Contenido insuficiente: anunciar el total disponible antes de comenzar; con cero, deshabilitar el inicio. No rellenar con repeticiones.
- Error de imagen durante una ronda normal: el callback incluirá el token actual. Si sigue vigente, descartar esa tarjeta y usar una alternativa válida cuya especie no haya aparecido ni esté reservada en la ronda. Si no existe, reducir el total. Registrar los fallos en memoria para no volver a seleccionar la misma foto fallida en esa sesión.
- Error de imagen en un repaso: omitir la tarjeta y ajustar el total, sin incorporar otra tarjeta o fotografía. Si quedan evaluaciones, el resumen contará únicamente las realizadas; si no queda contenido ni evaluaciones, mostrar indisponibilidad y volver al selector.
- Acción o callback tardío: ignorarlo si su token no coincide con la tarjeta vigente, incluidos los que llegan después de cambiar de modalidad o comenzar otro repaso.
- Giro interrumpido: cancelar la presentación visual y reconstruir la cara ya confirmada, sin evaluar ni volver a elegir tarjetas.
- Diagnóstico: usar mensajes locales de depuración para identificar entradas inválidas o errores de carga, sin registrar elecciones ni incorporar analítica de partidas. Los avisos al jugador no mostrarán rutas o identificadores internos.

## Estrategia de validación

### Reglas de sesión: pruebas unitarias

Crear `app/src/test/java/com/example/avesargentinas/ui/learning/LearningSessionTest.kt`, con JUnit y azar controlado en las pruebas. Verificar invariantes de selección y conteos; no exigir que dos mezclas aleatorias siempre sean diferentes.

| Escenario comprobable | Requisitos |
| --- | --- |
| 40 preguntas producen una ronda de 10 identificadores distintos; múltiples fotos de la misma especie o sexo no producen especies repetidas. | RF-3, RF-5, RF-8 |
| Cada tarjeta nueva empieza por el frente, permite ida y vuelta y solo acepta evaluación desde el dorso. Una segunda evaluación o un token anterior no cambia el resultado. | RF-9, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15 |
| 7 conocidas y 3 marcadas producen resumen 10/7/3; el repaso usa esas mismas 3 tarjetas. Luego 2/1 produce un repaso de 1; cero marcadas no permite otro repaso. | RF-16, RF-17, RF-18, RF-19, RF-20 |
| Nueva ronda, cambio de modalidad y salida limpian marcas y contadores. Una sesión nueva no recupera la anterior. | RF-21, RF-22, RF-23, RF-24, RF-26 |
| Catálogos de 0, 1 y 9 contenidos tienen el estado y total correspondientes. | RF-29, RF-30 |
| Un fallo durante la ronda reemplaza sin repetir o reduce el total; en repaso omite sin agregar otras tarjetas. Un callback tardío no invalida la nueva tarjeta. | RF-12, RF-28 |
| Todas las fotos fallan: con evaluaciones previas hay resumen de las realizadas; sin ellas se vuelve a indisponibilidad, sin un resumen ficticio. | RF-16, RF-28, RF-30 |

### Contenido y recorrido: pruebas instrumentadas

Crear `LearningContentLoaderTest.kt` y `LearningGameActivityTest.kt` en `app/src/androidTest/java/com/example/avesargentinas/ui/learning/`.

- Cargar los assets reales con el contexto Android: exactamente 40 preguntas, identificadores esperados, ocho temas, respuestas no vacías y correspondencia con el banco aprobado. Comprobar 45 especies distintas con nombres resueltos y fotos decodificables. La revisión semántica de la especie fotografiada seguirá siendo manual. RF-5 a RF-8 y calidad editorial.
- Probar entradas inválidas con documentos pequeños de prueba: campo vacío, identificador repetido, especie desconocida, ruta externa o archivo ausente y JSON mal formado. La lectura de texto y las funciones de validación aceptarán entradas de prueba sin tener que alterar los assets publicados. RF-28 a RF-30.
- Usar ActivityScenario y Espresso para menú, modalidades, frente/dorso, visibilidad de autoevaluación, resumen y repaso; comprobar que Atrás y las salidas limpien la sesión. RF-1, RF-2 y RF-9 a RF-24.
- Recrear la actividad durante una tarjeta revelada, una ronda parcialmente evaluada, un repaso y un resumen. Comprobar mismos contenidos, cara, posición y conteos; repetir con giro interrumpido. RF-12 y RF-25.
- Verificar la regresión de captura instantánea mediante el recorrido de identificación al menú, conservando y restaurando la preferencia utilizada por la prueba. RF-4.
- Esperar condiciones reales de interfaz o estado, sin pausas fijas para sincronizar las animaciones.

### Comprobaciones en dispositivo o emulador

- Dos modalidades completas sin conexión y con permisos del juego sin conceder; no deben solicitarse permisos para jugar. RF-27.
- Fondo y regreso mientras la app sigue viva: conservar estado. Cierre de la app y reapertura, y cierre del proceso en segundo plano por el sistema: volver al selector, sin restauración de partidas. RF-25 y RF-26. La recreación de ActivityScenario no sustituye la prueba de muerte del proceso.
- Temas claro/oscuro, vertical/horizontal, texto al 200 %, pantalla pequeña, TalkBack y animaciones desactivadas. Probar al menos API 24 o 25 y otra API 26 o superior para las dos rutas de animación.
- Revisar capturas de las pantallas y las 45 fotografías seleccionadas; comprobar que el frente no filtre respuestas por descripciones accesibles y que no se recorten rasgos importantes de las aves.
- Revisar que no se hayan añadido escrituras de sesiones ni resultados a almacenamiento y que los otros recorridos de la app sigan funcionando.

Comandos desde la raíz del repositorio durante la implementación:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

La ejecución instrumentada y la revisión visual requieren dispositivo o emulador. Registrar sus resultados y cualquier comprobación pendiente; no declarar completada la funcionalidad sin cubrir los criterios de la spec. Durante la preparación de este plan solo corresponde comprobar la documentación, no ejecutar esas validaciones de una implementación todavía inexistente.

## Riesgos y decisiones pendientes

- Selección fotográfica: se inspeccionó una muestra del dataset, no sus 3.500 fotos. Algunas muestran aves pequeñas u ocultas. La curación debe verificar las 45 especies antes de considerar completo el contenido; las etiquetas por sí solas no demuestran que una imagen esté correctamente identificada.
- Ciclo de vida: un ViewModel conserva el estado ante configuración, pero no ante muerte del proceso. Esto coincide con el alcance aprobado y requiere comprobar ambos casos por separado.
- Peso y calidad de las fotos: medir la selección exportada y revisar sus rasgos de identificación antes de cerrar la validación de contenido; el peso final todavía no fue medido.
- Menú: el botón adicional puede quedar fuera de pantalla con el layout actual. El desplazamiento y la prueba con fuente ampliada forman parte de este cambio, sin rediseñar el menú completo.
- Entorno: la disponibilidad del JDK, SDK y dispositivo, y la resolución final de dependencias se comprobarán al ejecutar las tareas de implementación; no se han validado mediante compilación en esta etapa documental.

No hay decisiones funcionales pendientes ni ampliaciones de alcance propuestas. El usuario aprobó este plan el 19 de octubre de 2026. El siguiente artefacto es `tasks.md`, que requiere aprobación explícita antes de implementar; la ejecución posterior seguirá la cadencia de una tarea por turno definida en `AGENTS.md`.
