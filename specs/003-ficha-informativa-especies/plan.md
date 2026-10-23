# Plan técnico — Spec 003: Ficha informativa y visual de especies

Estado: aprobado explícitamente por el usuario el 22 de octubre de 2026. Fecha: 22 de octubre de 2026.

## Referencias y alcance

Acuerdo del usuario del 22 de octubre de 2026: las atribuciones pendientes se registran por fotografía y no bloquean la implementación posterior. Autor/licencia desconocidos permanecen nulos; no se presentan como permisos obtenidos. La preparación técnica de T2 está verificada y habilita continuar con T3. Las tareas de contenido pueden cumplir su entrega de desarrollo con derechos pendientes documentados; el cierre editorial con atribuciones verificadas sigue siendo un control separado para la entrega final. Se mantiene el tratamiento de imágenes faltantes o inadecuadas y la selección exacta ya diseñados, sin nuevas dependencias ni cambios de arquitectura. No se altera la ejecución de una tarea por turno.

- Spec aprobada: [spec.md](spec.md), RF-1 a RF-26 y RNF-1 a RNF-6.
- Compatibilidad: [spec 002](../002-mejora-ui-ux-aprender-jugando/spec.md), especialmente consulta desde el juego y retorno a la respuesta pendiente de evaluar.
- Reglas: [AGENTS.md](../../AGENTS.md). Se mantienen Kotlin, Android Views/XML, componentes existentes y el flujo SDD con aprobación de cada artefacto. No se crean tareas ni se implementa en esta etapa; no se hacen commits ni push.
- Diseño: [frontend-design](../../.agents/skills/frontend-design/SKILL.md), adaptada a Android y a los recursos visuales existentes. No se utiliza nexus-ui-design.
- Alcance editorial: 45 especies; cobertura de las 50 etiquetas actuales mediante fotos diferenciadas para las cinco parejas de macho y hembra, sin duplicar textos biológicos.

## Estado actual

- `BirdClassifier.kt` produce `Prediction`, que ya contiene `index`, `scientificName`, `regionalName`, `displayName` y `variant`. La variante ya se reconoce y aparece en el nombre mostrado; no es necesario modificar el modelo ni su parser de etiquetas.
- `MainActivity.kt` abre `BirdInfoActivity` pasando nombre común, científico y confianza. Todavía no transmite `Prediction.variant`.
- `LearningGameActivity.openBirdInfo()` pasa nombre común y científico desde la tarjeta revelada y no distingue sexo. Conserva la sesión mediante su ViewModel y la pila normal de actividades.
- `BirdInfoActivity` hereda de `BaseActivity`, aplica el tema guardado y muestra distribución y leyenda. Ya impide ampliar el marcador de sustitución hasta que Glide confirma la carga y conserva equivalencias explícitas para tres mapas.
- `ImageZoomActivity` acepta una URI mediante `EXTRA_IMAGE_URI` y permite ampliar con Zoomage. Ya se utiliza desde distintas pantallas.
- Material Components, ViewPager2, Glide, coroutines y las herramientas de pruebas necesarias están declaradas en `app/build.gradle.kts`.
- `learning/photo_cards.json` relaciona las 45 especies con las fotos existentes. Su formato y la selección de tarjetas del juego se mantienen.
- El [registro de selección existente](../../docs/contenido/seleccion-fotos-aprender-jugando.md) documenta rasgos, fuentes locales y hashes, pero no acredita por sí mismo autoría y permiso de uso de cada imagen.
- Existen pruebas instrumentadas de la ficha para accesibilidad, tema, mapas, alias, carga, ampliación y retorno desde identificación. Sus supuestos de mapa visible inicialmente deberán adaptarse porque ahora la primera pestaña será Información.

## Enfoque técnico

### Identidad de especie y variante — RF-1, RF-2, RF-5 a RF-7, RF-24 a RF-26

Conservar los extras existentes de `BirdInfoActivity` y añadir `EXTRA_VARIANT`, opcional. `MainActivity` enviará `pred.variant` como dato explícito de la misma predicción usada para los nombres. No se extraerá el sexo del texto visible, de una probabilidad ni del nombre de archivo.

La ficha normalizará únicamente los valores reconocidos «macho» y «hembra», sin depender de mayúsculas y usando `Locale.ROOT`. La ausencia de variante significa consulta general; un valor no vacío desconocido no se convertirá en un sexo conocido ni elegirá una fotografía por aproximación: conservará la consulta de especie y mostrará que la fotografía de esa variante no está disponible.

La identidad interna de especie seguirá siendo la clave del catálogo actual. Para el contrato existente se resolverá desde el nombre científico recibido, normalizando espacios, guiones y mayúsculas de forma equivalente a la resolución actual. Los cambios taxonómicos se expresarán en nombres de presentación del contenido, sin cambiar la clave ni la entrada utilizada para buscar el mapa. No se agregará una búsqueda difusa o por nombres comunes.

`LearningGameActivity` conservará su contrato actual sin variante y la foto representativa de cada especie. No se añaden etiquetas de sexo al juego ni se modifica su motor. La cabecera y la descripción accesible distinguirán el sexo consultado del sexo de un ejemplar fotografiado en una consulta general.

### Contenido local y carga — RF-7, RF-9, RF-11 a RF-16, RF-21 a RF-26

Añadir en `ui/bird/` dos piezas concretas:

- `SpeciesInfo.kt`: datos de una especie, fotografías, conservación y referencias, junto con la selección de foto por variante. Los valores de variante se representan como macho/hembra; no se crea un sistema extensible de variantes.
- `SpeciesInfoLoader.kt`: lectura de assets y parser con `org.json`, siguiendo el patrón de `LearningContentLoader`. No se agregan interfaces de repositorio, base de datos, inyección de dependencias ni librerías de serialización para este catálogo estático.

El contenido se guardará en `app/src/main/assets/species_info.json`, con dos colecciones:

| Colección / campo | Contrato |
| --- | --- |
| `sources` | Referencias por identificador: título, entidad, URL y fecha de consulta en formato ISO. |
| `species` | Una entrada por clave canónica de especie; 45 entradas válidas en el contenido final. |
| Identidad y taxonomía | Clave, nombre común, científico de presentación, inglés, familia, grupo y orden. |
| Descripción y ecología | Regiones argentinas, descripción física, hábitat, dieta y movimientos estacionales. |
| `conservationGlobal` / `conservationNational` | Categoría, año de evaluación y referencias, separados por ámbito; admiten ausencia explícita. |
| `fieldSources` | Relación de cada campo biológico con uno o varios identificadores de referencia. Los nombres de presentación también tienen respaldo. |
| `photos` | Lista de fotografías de la especie: ruta relativa al asset, sexo verificado cuando corresponda, indicador de representativa, autor, licencia o permiso y referencia de procedencia. |

Cada especie tendrá exactamente una foto representativa. En las cinco especies con variantes habrá una fotografía verificada de cada sexo; la representativa podrá ser una de ellas, sin repetir ni su archivo ni sus metadatos. La consulta con variante elige el sexo exacto; la consulta general elige la representativa. No se añade selector manual ni galería.

Los campos editoriales no verificados serán nulos y se representarán mediante el recurso «Información no disponible». En el registro editorial se explicará su ausencia. No se guardará ese texto como si fuera un dato biológico, ni se usará una categoría de conservación por defecto.

El parser rechazará documentos mal formados y entradas con identidad inválida o duplicada. Un campo opcional inválido se tratará como no disponible; una referencia no resoluble no invalidará las demás referencias y datos de la ficha. La validación de contenido detectará esas inconsistencias para que no lleguen a una entrega considerada completa. Los fallos de una entrada no descartarán otras entradas válidas.

Se admitirán solo rutas relativas de assets para fotografías, sin segmentos `..`, rutas absolutas, esquemas de red ni rutas al dataset externo. Las URLs de fuentes serán metadatos de consulta, no recursos cargados por Glide ni llamadas a red.

La actividad cargará el contenido con `lifecycleScope` y `Dispatchers.IO`, cancelable al destruirse, mostrando un estado de carga breve. No se añadirá un ViewModel solo para guardar datos estáticos. La selección de pestaña se restaurará mediante `savedInstanceState` y el estado de ViewPager2; la especie y variante procederán del Intent original. El mapa se podrá consultar aunque falle el contenido editorial.

### Investigación y fotografías — RF-8 a RF-16, RF-24 a RF-26; RNF-5 y RNF-6

Preparar `docs/contenido/fichas-especies.md` como registro editorial: por especie, referencias de cada campo, consulta, decisiones taxonómicas, discrepancias resueltas y ausencias justificadas. La app incluirá en el catálogo la información necesaria para consultar esas referencias offline; el documento de trabajo no sustituye las referencias dentro de la ficha.

Recopilar textos propios en español, priorizando instituciones y fuentes ornitológicas autorizadas. Contrastar los campos dudosos con otra fuente pertinente. Identificar por separado evaluaciones nacionales y globales con sus años efectivos; no interpretar ausencia de evaluación como «Preocupación menor». No reutilizar automáticamente nombres o categorías de los ejemplos del documento inicial.

Las fotografías reutilizables por sexo identificadas en el registro actual son:

| Especie / clave existente | Foto reutilizable | Variante adicional necesaria |
| --- | --- | --- |
| `amazonetta_brasiliensis` | `learning/photos/photo-034.webp`, macho | Hembra |
| `anas_cyanoptera` | `learning/photos/photo-032.webp`, macho | Hembra |
| `callonetta_leucophrys` | `learning/photos/photo-035.webp`, macho | Hembra |
| `netta_peposaca` | `learning/photos/photo-039.webp`, macho | Hembra |
| `oxyura_vittata` | `learning/photos/photo-042.webp`, hembra | Macho |

La tabla describe el registro previo, no una nueva validación visual. Se revalidarán las asociaciones al preparar el contenido. Las cinco carpetas complementarias ya existen en la colección externa indicada por el usuario; se inspeccionarán candidatos y rasgos sin confiar únicamente en sus rótulos. En particular, el registro previo advierte de machos de zambullidor ambiguos que no deben darse por identificados.

Las imágenes adicionales verificadas se convertirán a WebP en `app/src/main/assets/birds/photos/`, con clave y sexo en el nombre. Se seguirá el criterio de las existentes: orientación correcta, proporción original, lado mayor de hasta 1.024 px sin ampliar, calidad 85, sin recortar rasgos ni eliminar firmas visibles. Se eliminarán metadatos EXIF/GPS de las copias. Los originales se leerán sin modificarlos.

Registrar en `docs/contenido/fotos-ficha-especies.md` fuente relativa a la colección, rasgos de identidad y sexo, dimensiones, bytes, hashes, autor y licencia o permiso. Las fotos existentes seguirán en su ruta y el catálogo las referenciará allí. No se incorporarán rutas absolutas del equipo a los archivos versionados.

La investigación de atribuciones forma parte del trabajo para todas las imágenes utilizadas. Si no se resuelve un crédito o permiso, se deja el pendiente explícito y se continúa el trabajo independiente, sin declarar completa la entrega. Una sustitución de una foto ya acordada se presenta al usuario antes de realizarla, conforme a la spec.

### Pestañas y composición visual — RF-1 a RF-5, RF-8 a RF-20; RNF-2 a RNF-4

Mantener `BirdInfoActivity` como única actividad de ficha y `activity_bird_info.xml` como contenedor de cabecera, `TabLayout` y `ViewPager2`. Un adaptador pequeño `BirdInfoPagerAdapter.kt`, basado en `RecyclerView.Adapter`, tendrá dos vistas XML fijas, sin fragments ni navegación adicional. Se conectará con `TabLayoutMediator`; callbacks y solicitudes de imagen se limpiarán al destruir o reciclar sus vistas.

Crear `view_bird_information.xml` y `view_bird_distribution.xml`. Cada página tendrá su propio `NestedScrollView` y un hijo vertical con altura natural. El mapa y su leyenda se trasladarán a la segunda página conservando identificadores útiles y el comportamiento existente. La carga y los estados permanecerán asociados a su página; volver a enlazar una vista no conservará una URI de ampliación de una especie anterior.

Dirección visual: ficha de campo centrada en la foto, seguida de nombres y bloques de lectura, con texto alineado al inicio. Se conservará la cabecera centrada de la app. La familia y conservación se mostrarán con chips informativos no interactivos y de varias líneas cuando sea necesario; si la categoría no cabe, se usa texto con fondo temático y altura natural, sin truncarla.

Se reutilizarán recursos semánticos, sin nueva paleta ni fuentes descargadas:

| Rol | Claro | Oscuro |
| --- | --- | --- |
| Fondo `brand_background` | `#E8EDF5` | `#0D1420` |
| Superficie `brand_surface` | `#F5F7FA` | `#151F2E` |
| Texto principal `text_primary` | `#1F2C40` | `#F3F6FF` |
| Texto secundario `text_secondary` | `#556177` | `#C0CAE6` |
| Selección `brand_primary` | `#1F4B99` | `#7FA5FF` |
| Borde `brand_outline` | `#B5C2D9` | `#324067` |

Se mantendrá la tipografía del tema, con nombres destacados, nombre científico en cursiva, etiquetas en negrita y cuerpos legibles en sp. Se reutilizarán dimensiones de espaciado existentes; los valores nuevos que hagan falta serán recursos específicos de la ficha. No habrá animaciones ornamentales ni una tarjeta idéntica por cada campo.

La fotografía usará `fitCenter` y un área con altura limitada para evitar desplazar todos los datos fuera de pantalla; la imagen se verá completa. Los textos y créditos tendrán altura natural. Las fuentes se mostrarán al final de Información con título, entidad, fechas y URL textual, sin WebView ni necesidad de abrir navegador.

Mantener `BaseActivity`, `insets_container`, el tema guardado y la adaptación de iconos de barras del sistema. El estado de carga no bloqueará Atrás ni la selección de pestañas. Las páginas inactivas no expondrán controles al lector de pantalla; las acciones tendrán nombres accesibles y objetivos mínimos de 48 dp.

### Mapas y ampliación — RF-10, RF-17 a RF-20, RF-22 y RF-26

Conservar un único punto de resolución de mapa en `BirdInfoActivity`, con la prioridad actual: URI explícita, nombres de recursos originales, alias admitidos y ruta de assets. Usar siempre la identidad recibida, sin añadir el sexo al nombre del mapa ni sustituirla por el nombre científico editorial.

Mantener los alias actuales `anas_versicolor` → `spatula_versicolor_dist`, `anas_cyanoptera` → `spatula_cyanoptera_dist` y `anas_sibilatrix` → `mareca_sibilatrix_dist`. No se agregan mapas ni equivalencias no verificadas. La ausencia previamente documentada de un mapa de `aramides_ypecaha` seguirá mostrando aviso si continúa sin recurso al implementar.

Foto y mapa tendrán estados y URI de ampliación independientes: al comenzar una solicitud se limpia su URI y se desactiva la acción; solo `onResourceReady` habilita esa imagen; un fallo deja aviso visible y ampliación deshabilitada. Los listeners no actualizarán una vista reciclada o una consulta que ya no sea la activa.

Ambos abrirán `ImageZoomActivity` mediante su contrato existente y un Intent explícito. No se necesita modificar el visor ni sus otros llamadores. La pila seguirá siendo origen → ficha → ampliación, sin `finish()`, flags especiales ni resultados que alteren la partida. Volver conserva la pestaña seleccionada y la variante de la ficha.

## Cambios previstos

Las rutas Kotlin siguientes son relativas a `app/src/main/java/com/example/avesargentinas/`.

| Archivo o área | Cambio y razón | Requisitos |
| --- | --- | --- |
| `MainActivity.kt` | Transmitir la variante de la misma predicción consultada. | RF-1, RF-2, RF-24 |
| `ui/bird/BirdInfoActivity.kt` | Extras, carga local, selección de especie/variante, estado de pestaña, mapas y navegación a ampliación. | RF-1 a RF-7, RF-17 a RF-26 |
| `ui/bird/SpeciesInfo.kt` | Datos editoriales compartidos y selección exacta de fotografía. | RF-7 a RF-16, RF-24 a RF-26 |
| `ui/bird/SpeciesInfoLoader.kt` | Lectura y validación local con aislamiento de errores. | RF-21 a RF-23; RNF-1, RNF-6 |
| `ui/bird/BirdInfoPagerAdapter.kt` | Dos páginas XML y enlace de sus datos y estados accesibles. | RF-3, RF-4, RF-8 a RF-20 |
| `res/layout/activity_bird_info.xml` y nuevas páginas | Cabecera y pestañas; foto, datos, créditos, fuentes; mapa y leyenda. | RF-2 a RF-4, RF-8 a RF-20 |
| `res/values/strings.xml` y `dimens.xml` | Etiquetas, avisos, descripciones accesibles y dimensiones necesarias, reutilizando lo existente. | RF-2, RF-9, RF-11 a RF-16, RF-20 a RF-22; RNF-2 a RNF-4 |
| `assets/species_info.json` | Datos de 45 especies y fotografías para las 50 etiquetas, con referencias. | RF-7 a RF-16, RF-24 a RF-26 |
| `assets/birds/photos/` | Cinco fotografías complementarias verificadas, si se confirma la reutilización prevista. | RF-8 a RF-10, RF-24; RNF-5 |
| `docs/contenido/fichas-especies.md` y `fotos-ficha-especies.md` | Investigación trazable, ausencias justificadas y atribuciones. | RF-9, RF-11 a RF-16; criterios de finalización 2 y 3 |
| Pruebas de ficha, contenido y juego | Cobertura de nuevos contratos y adaptación a Información como pestaña inicial. | RF-1 a RF-26 |

No se prevén cambios en Gradle, Manifest, Room, `BirdClassifier`, `labels.txt`, `labels_map.json`, el manifiesto de fotos del juego ni el visor compartido. Si una comprobación revela una necesidad real de cambio dentro del alcance, se documentará antes de implementarla.

## Datos, contratos y compatibilidad

- **Entrada de ficha:** se preservan los extras existentes y se añade una variante opcional; los llamadores antiguos sin variante mantienen la consulta general.
- **Contenido:** JSON empaquetado de solo lectura; no hay migraciones ni persistencia de una selección de sexo. Los textos se almacenan una vez por especie.
- **Estado:** Intent para identidad y variante, estado de actividad/paginador para pestaña. El juego conserva su ViewModel y sus garantías actuales; no se promete recuperación de partida tras muerte del proceso.
- **Permisos y red:** no hay nuevos permisos ni endpoints. Las fuentes externas se consultan durante la investigación, no al usar la ficha.
- **Observabilidad:** registros breves con clave de especie y recurso fallido para diagnóstico, sin textos completos, rutas externas del equipo ni datos de observaciones.
- **Contenido heredado:** no se elimina `oxyura_jamaicensis` del catálogo auxiliar ni se le inventa una correspondencia con otra especie.

## Errores y casos límite

| Situación | Tratamiento |
| --- | --- |
| Especie desconocida o catálogo ilegible | Aviso en Información y nombre recibido si existe; mapa resuelto de forma independiente; Atrás disponible. |
| Campo ausente o no verificado | «Información no disponible», sin categoría o valor inventado. |
| Referencia inválida | Otras fuentes y datos siguen disponibles; error detectado por validación de contenido y registrado para corregir. |
| Variante conocida sin foto | Aviso de fotografía de variante no disponible, sin usar la del otro sexo. |
| Variante no vacía desconocida | Sin inferencia de sexo ni elección aproximada; datos comunes y mapa conservados. |
| Foto o mapa no cargado | Estado independiente, sin URI de ampliación; el otro recurso puede utilizarse. |
| Volver a enlazar una página o completar una carga antigua | Limpiar solicitud/estado anterior y aceptar solo el resultado correspondiente a la vista y consulta vigentes. |
| Rotación o regreso del visor | Mismo Intent, pestaña restaurada y variante conservada. |
| Apertura desde juego | Consulta general con foto representativa actual; retorno sin evaluación ni pérdida de sesión. |
| Crédito sin verificar o candidato fotográfico ambiguo | Pendiente editorial explícito; no se inventa ni se da por completa la entrega. |

## Estrategia de validación

La validación automatizada y la revisión editorial son complementarias: una estructura JSON válida no acredita la exactitud biológica ni el permiso fotográfico.

| Comprobación | Evidencia / requisitos |
| --- | --- |
| Pruebas unitarias de selección de foto en `app/src/test/.../ui/bird/SpeciesInfoTest.kt` | General, macho, hembra, ausencia exacta y variante desconocida; textos/mapa compartidos. RF-7, RF-24 a RF-26. |
| Pruebas instrumentadas de parser y assets en `SpeciesInfoLoaderTest.kt` | Documento inválido, duplicados, entradas aisladas, campos nulos, referencias inválidas y rutas no locales rechazadas. RF-21 a RF-23; RNF-1, RNF-6. |
| Verificación instrumentada del catálogo final | 45 claves y cobertura de las 50 etiquetas reales; 5 parejas con fotos distintas por sexo; rutas decodificables, una representativa por especie, referencias completas y atribuciones resueltas. No hay foto de otro sexo como sustitución ni duplicados binarios innecesarios. |
| Revisión editorial de las 45 especies y fotografías utilizadas | Cada campo vinculado a fuentes, discrepancias justificadas, años de conservación correctos y ausencia documentada cuando corresponda. RF-9, RF-11 a RF-16. |
| Ampliación de `BirdInfoActivityTest.kt` | Primera pestaña, cabecera/variante, cambio de pestaña, foto completa, zoom de ambas imágenes, errores independientes y retorno tras rotación. RF-1 a RF-5, RF-8 a RF-10, RF-17 a RF-26. |
| Regresiones actuales de mapas e identificación | Prioridad de URI explícita, originales y tres alias, mapa ausente, bloqueo durante carga y transmisión explícita de macho/hembra desde identificación. RF-6, RF-17 a RF-20, RF-24. |
| Ajuste de `LearningGameActivityTest.kt` | Consulta sin variante, foto representativa, visita a Distribución y zoom, regreso a la misma respuesta y posterior evaluación única. RF-6, RF-25 y compatibilidad spec 002. |
| Revisión visual y accesible en emulador/dispositivo | Ambos temas, vertical/horizontal, pantalla pequeña, texto 200 %, lector de pantalla, objetivos de 48 dp, fuentes y créditos alcanzables. RNF-2 a RNF-4. |
| Consulta sin conectividad antes de abrir la ficha | Recursos, textos y referencias offline desde ambos accesos, sin permisos ni descargas nuevos. RNF-1. |
| Inventario de cambios y tamaños | Originales externos intactos, fotos existentes reutilizadas, incremento real de bytes y etiquetas/modelo sin modificaciones. RNF-5 y RNF-6. |

Ejecutar desde la raíz con el Wrapper y un JDK/SDK compatibles:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las instrumentadas requieren dispositivo o emulador conectado. Durante la implementación se ejecutarán los checks pertinentes a la única tarea autorizada en cada turno; la validación final reunirá los resultados. Si el entorno bloquea una comprobación, se registrará el comando y su fallo concreto, sin declarar validado lo que no se haya ejecutado.

## Riesgos y decisiones pendientes

- No hay decisiones funcionales pendientes para este plan. El documento sigue sujeto a aprobación explícita antes de generar tareas.
- Las atribuciones y permisos de las imágenes todavía no están verificados. Resolverlos puede requerir evidencia adicional o proponer sustituciones al usuario; no se oculta ese pendiente con un crédito genérico.
- Las cinco nuevas fotos son una previsión basada en el registro existente. Su identificación y sexo deberán verificarse; los candidatos ambiguos, especialmente machos de zambullidor, no se aceptarán por su carpeta.
- La investigación puede encontrar campos no verificables o fuentes taxonómicas discrepantes. Se aplicará la política aprobada de documentar decisiones y mostrar ausencia explícita, sin cambiar silenciosamente la identidad del clasificador.
- Debe comprobarse el entorno JDK/SDK y la disponibilidad de dispositivo al implementar. Este plan no afirma que Gradle ni las pruebas de las etapas anteriores estén aprobados.

Tras aprobar este plan se redactará `tasks.md` con la skill local correspondiente. La ejecución posterior será de una sola tarea por turno, con su validación, actualización de estado y confirmación antes de la siguiente.
