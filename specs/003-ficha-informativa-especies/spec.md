# Spec 003 — Ficha informativa y visual de especies

Estado: aprobada explícitamente por el usuario el 22 de octubre de 2026, incluida la cobertura de las 50 etiquetas con fotografía por sexo e información compartida por especie. Fecha: 22 de octubre de 2026.

## Contexto y objetivo

### Acuerdo de continuidad del desarrollo — 22 de octubre de 2026

El usuario autorizó continuar el desarrollo sin bloquearlo por las atribuciones fotográficas pendientes. Se distingue la preparación técnica de las imágenes de la verificación de sus derechos: pueden avanzar las tareas siguientes, manteniendo autor/licencia no verificados como ausentes y los pendientes trazables. Esta decisión no acredita permisos ni cambia RF-9 o los criterios de una entrega con atribuciones resueltas. Se conserva la asociación exacta por especie/sexo y el estado de fotografía no disponible de RF-20/RF-26 cuando no haya un recurso adecuado verificado; no se agrega un sustituto de otra especie o sexo. No se reemplazan silenciosamente las imágenes acordadas. La integración y presentación continúan en las tareas previstas, una por turno.

La ficha actual permite consultar el mapa de distribución de un ave y ampliarlo, pero no ofrece una fotografía representativa ni una descripción biológica. El objetivo es convertirla en una ficha de campo visual y legible, con dos pestañas: «Información» y «Distribución», disponible completamente sin conexión.

El alcance comprende las **50 etiquetas actuales del clasificador, correspondientes a 45 especies distintas**. Las variantes de macho y hembra compartirán la información biológica y el mapa de su especie, pero mostrarán la fotografía correspondiente al sexo de la etiqueta consultada. Existen 45 fotografías representativas locales; se reutilizarán las que correspondan y se seleccionarán y verificarán las fotografías adicionales necesarias de la colección local, sin duplicar imágenes ya incluidas.

La recopilación, redacción y verificación de los textos biológicos y la investigación de las atribuciones fotográficas forman parte del trabajo. No se considera que el contenido esté completo o validado por el solo hecho de existir una imagen o un nombre en el catálogo.

### Decisiones de alcance confirmadas

- Se extiende la ficha existente, conservando sus accesos desde identificación y «Aprender jugando».
- Se reutilizan las fotografías locales seleccionadas para el juego, los mapas y la posibilidad de ampliación existentes; se incorpora cobertura fotográfica por sexo donde las etiquetas lo distinguen.
- La cobertura es de 50 etiquetas y 45 especies, no las 47 especies indicadas en el documento inicial. No se modifica el modelo ni el orden de sus etiquetas.
- Cinco especies tienen etiquetas de ambos sexos: *Amazonetta brasiliensis*, *Anas cyanoptera*, *Callonetta leucophrys*, *Netta peposaca* y *Oxyura vittata*. Cada una tendrá una fotografía apropiada para macho y otra para hembra, con el mismo contenido biológico y mapa de especie.
- El pato zambullidor chico, *Oxyura vittata*, ya tiene fotografía representativa de una hembra. Se seleccionará y verificará una de macho de la colección local, sin inferir su identidad únicamente por el nombre de la carpeta.
- La entrada auxiliar `oxyura_jamaicensis` no pertenece a las etiquetas actuales del clasificador. No se incorporará como una especie adicional ni se le asignará la fotografía de *Oxyura vittata*.

## Usuarios

Personas que identifican aves o practican su reconocimiento y necesitan consultar sus rasgos y distribución en el campo, incluso sin señal. Incluye usuarios de modo oscuro, texto ampliado, lectores de pantalla y dispositivos con poco espacio disponible.

## Comportamiento actual y compatibilidad

- La ficha se abre para la especie seleccionada desde identificación y desde una respuesta fotográfica revelada del juego.
- Actualmente muestra un mapa con referencias, permite ampliarlo cuando está disponible y avisa cuando no puede cargarlo.
- Se conservarán la resolución de nombres y equivalencias ya admitidas para los mapas y la prioridad de una imagen de distribución proporcionada explícitamente por el acceso existente.
- Consultar la ficha no registra una observación ni modifica resultados del clasificador, fotografías de observaciones o preferencias.
- El retorno al juego conservará los comportamientos de la [spec 002](../002-mejora-ui-ux-aprender-jugando/spec.md): misma tarjeta revelada, pendiente de evaluar, sin perder modalidad, pasada, orden, progreso ni autoevaluaciones mientras la app siga abierta.
- Esta propuesta no modifica las reglas de identificación, las etiquetas ni el orden del modelo, ni amplía las garantías de recuperación del juego después del cierre del proceso.
- Los pendientes de validación de las specs anteriores conservan su estado; este documento no los da por resueltos.

## Historias de usuario

- H1: Como usuario, quiero ver una fotografía representativa y los nombres del ave, para relacionar su aspecto con su identidad.
- H2: Como usuario, quiero consultar rasgos físicos, hábitat, alimentación, movimientos y conservación, para conocer la especie en el campo.
- H3: Como usuario, quiero alternar entre información y distribución, para consultar ambos contenidos sin salir de la ficha.
- H4: Como usuario, quiero ampliar la fotografía y el mapa y regresar a mi consulta, para observar detalles sin perder el contexto.
- H5: Como usuario, quiero consultar contenido con fuentes identificables y créditos fotográficos, para conocer su procedencia.
- H6: Como usuario, quiero que la consulta funcione sin internet y siga siendo utilizable ante un recurso faltante, para continuar mi actividad.
- H7: Como usuario, quiero ver una fotografía de macho o hembra que corresponda a la etiqueta identificada, para comparar su aspecto sin perder la información general de la especie.

## Requisitos funcionales (criterios de aceptación en EARS)

### Apertura, navegación e identidad

- RF-1: CUANDO el usuario abra una ficha desde identificación o desde una respuesta fotográfica del juego, EL SISTEMA mostrará la especie solicitada y seleccionará inicialmente «Información».
- RF-2: EL SISTEMA mostrará una cabecera con el nombre común centrado y una acción accesible de regreso; cuando el acceso identifique una variante, indicará «Macho» o «Hembra» de forma visible y accesible.
- RF-3: EL SISTEMA ofrecerá las pestañas «Información» y «Distribución», con indicación visual y accesible de cuál está seleccionada.
- RF-4: CUANDO el usuario cambie de pestaña, EL SISTEMA conservará la especie consultada y permitirá recorrer verticalmente todo el contenido de cada pestaña.
- RF-5: CUANDO el usuario vuelva de una ampliación o se reoriente la pantalla mientras la ficha siga abierta, EL SISTEMA conservará la especie, la variante consultada cuando exista y la pestaña seleccionada.
- RF-6: CUANDO el usuario regrese desde la ficha a su pantalla de origen, EL SISTEMA conservará el resultado de identificación o la sesión de juego que motivó la consulta, sin registrar una evaluación ni avanzar una tarjeta por esa consulta.

### Pestaña Información

- RF-7: EL SISTEMA cubrirá las 50 etiquetas del alcance con información biológica y mapa compartidos por especie; las variantes de macho y hembra no generarán textos biológicos independientes.
- RF-8: CUANDO esté disponible la fotografía representativa de la especie, EL SISTEMA la mostrará completa, con sus proporciones originales y sin deformar ni recortar partes para llenar el espacio.
- RF-9: EL SISTEMA mostrará al pie de la fotografía su atribución verificada: autor y licencia o permiso documentado, con la procedencia disponible; no deducirá la autoría a partir del nombre de carpeta ni inventará créditos.
- RF-10: CUANDO el usuario active una fotografía cargada correctamente, EL SISTEMA permitirá verla en pantalla completa, ampliarla y volver a la ficha.
- RF-11: EL SISTEMA mostrará el nombre común en español, el nombre científico, el nombre común en inglés, la familia, el grupo de aves y el orden, distinguiendo grupo y orden como conceptos separados.
- RF-12: EL SISTEMA mostrará la distribución por regiones de Argentina, la descripción morfológica, el hábitat, la alimentación y los movimientos estacionales de la especie.
- RF-13: DONDE existan diferencias físicas relevantes documentadas entre macho, hembra o juvenil, EL SISTEMA las explicará dentro de la descripción, sin presentar la fotografía elegida como representación exclusiva de todas sus variantes.
- RF-14: EL SISTEMA mostrará el estado de conservación global y el nacional por separado, identificando el ámbito, la fuente y el año de la evaluación disponible; no tratará sus categorías como equivalentes ni las fusionará en una sola categoría.
- RF-15: EL SISTEMA presentará la información con etiquetas claramente distinguibles y destacará visualmente la familia y los estados de conservación, manteniendo sus textos comprensibles sin depender del color.
- RF-16: EL SISTEMA permitirá consultar dentro de la ficha las referencias del contenido biológico y sus fechas de consulta sin requerir abrir una página de internet; las direcciones de las fuentes podrán conservarse como referencia.

### Pestaña Distribución

- RF-17: CUANDO el usuario seleccione «Distribución», EL SISTEMA mostrará el mapa de la misma especie y su tarjeta de referencias, conservando el contenido y las equivalencias de mapas existentes.
- RF-18: CUANDO el mapa se haya cargado correctamente y el usuario lo active, EL SISTEMA permitirá ampliarlo y volver a la ficha.
- RF-19: MIENTRAS el mapa no esté cargado correctamente, EL SISTEMA impedirá abrir una ampliación vacía o de una imagen de sustitución.

### Ausencias y errores

- RF-20: SI una fotografía o un mapa faltan o no pueden cargarse, ENTONCES EL SISTEMA indicará cuál de los recursos no está disponible, deshabilitará su ampliación y mantendrá accesibles el contenido restante, las pestañas y el regreso.
- RF-21: SI un dato biológico no puede verificarse, ENTONCES EL SISTEMA indicará «Información no disponible» en el campo correspondiente, sin inventar valores ni mostrarlo como una característica ausente de la especie.
- RF-22: SI la especie solicitada no pertenece al contenido disponible o su ficha no puede leerse, ENTONCES EL SISTEMA indicará que la información no está disponible, conservará el nombre recibido cuando exista y permitirá regresar; no sustituirá la especie por otra ni impedirá consultar un mapa existente que pueda resolverse.
- RF-23: SI falla una referencia o un recurso de una especie, ENTONCES EL SISTEMA evitará que ese fallo impida consultar las fichas válidas de las demás especies.

### Fotografías según la variante

- RF-24: CUANDO se abra la ficha desde una identificación cuya etiqueta distinga macho o hembra, EL SISTEMA conservará esa variante y mostrará una fotografía verificada del sexo correspondiente, manteniendo los mismos nombres de especie, taxonomía, descripción, ecología, conservación y mapa que para el otro sexo.
- RF-25: CUANDO se abra la ficha desde el juego o desde otro acceso que no indique sexo, EL SISTEMA mostrará la fotografía representativa existente de la especie; no atribuirá un sexo al resultado de origen. Donde el sexo de la fotografía esté verificado, lo identificará como característica del ejemplar fotografiado.
- RF-26: SI falta o falla la fotografía de una variante solicitada, ENTONCES EL SISTEMA indicará que su fotografía no está disponible y mantendrá la información compartida y el mapa accesibles; no mostrará silenciosamente una fotografía del otro sexo como si correspondiera a la variante solicitada.

## Requisitos no funcionales

- RNF-1 — Sin conexión: fotografías, mapas, textos, atribuciones y referencias necesarias para consultar la ficha estarán incluidos localmente. Su consulta no requerirá cuenta, permisos adicionales, descargas ni solicitudes de red.
- RNF-2 — Accesibilidad: la ficha será recorrible con lector de pantalla, identificará las pestañas y las acciones de ampliación y regreso, y ofrecerá descripciones de las imágenes. Los controles tendrán áreas táctiles de al menos 48 × 48 dp.
- RNF-3 — Legibilidad: se respetarán los modos claro y oscuro de la aplicación. Con texto al 200 %, en orientación vertical u horizontal y en pantallas pequeñas, todo el contenido y las acciones seguirán siendo alcanzables, sin superposiciones ni desplazamiento horizontal obligatorio.
- RNF-4 — Coherencia visual: la ficha mantendrá los roles visuales de la aplicación, con fotografía protagonista, jerarquía de textos y agrupación legible de datos; no incorporará un lenguaje visual ajeno al resto de pantallas.
- RNF-5 — Recursos: se reutilizarán las fotografías locales que correspondan y se incorporarán únicamente las adicionales necesarias para cubrir las variantes; no se duplicarán las existentes ni se exigirá acceso al directorio externo del dataset en el dispositivo. El peso adicional se medirá al preparar las nuevas imágenes, sin reutilizar la estimación inicial de 3–5 MB.
- RNF-6 — Integridad: no se modificarán las etiquetas, su orden, el modelo ni las asociaciones de especie para adaptar el contenido editorial. Los nombres científicos de presentación podrán reflejar una nomenclatura verificada, manteniendo identificable su correspondencia con la especie existente.

## Recopilación y verificación del contenido

Este trabajo incluye preparar contenido para las 45 especies y cobertura fotográfica para las 50 etiquetas, no solamente construir una pantalla que espere recibirlo posteriormente.

- Se redactarán textos propios y concisos en español a partir de fuentes identificables. Cada campo biológico tendrá respaldo trazable en el registro de investigación de la especie.
- Se priorizarán organismos de biodiversidad y conservación, instituciones científicas y fuentes ornitológicas especializadas. Para distribución y conservación nacionales se utilizarán referencias pertinentes a Argentina; para conservación global, la evaluación global correspondiente.
- Se registrarán título o entidad, dirección de la fuente, fecha de consulta y año de evaluación cuando corresponda. Una fecha de consulta reciente no convierte una evaluación antigua en una evaluación nueva.
- Se contrastarán identidad, equivalencias taxonómicas y afirmaciones conflictivas antes de incorporarlas. Si las fuentes discrepan, se documentará la decisión y su fundamento; si no puede resolverse, el dato quedará como no disponible.
- La investigación distinguirá información general de la especie de variaciones regionales, estacionales y por sexo. No atribuirá a toda Argentina un comportamiento descrito únicamente para una localidad.
- Se revisará la procedencia y atribución de las fotografías reutilizadas y adicionales. Las nuevas selecciones se contrastarán visualmente y con referencias para verificar tanto especie como sexo; no se consideran validadas por existir una carpeta rotulada. Un rótulo de carpeta, una firma visible o el registro de selección por sí solos no acreditan una licencia o permiso de reutilización.
- Los créditos o permisos que no puedan verificarse quedarán registrados como pendientes de contenido y no se darán por cumplidos con un autor o licencia inventados. La entrega final no se considerará completa mientras queden atribuciones requeridas sin resolver; cualquier necesidad de reemplazar imágenes se presentará al usuario antes de cambiar la selección acordada.

## Casos límite ya cubiertos

Estos casos están contemplados por los requisitos; no se afirma que su validación ya haya sido ejecutada.

| Caso | Resultado esperado |
| --- | --- |
| Etiquetas de macho y hembra de una misma especie | Fotografía correspondiente a cada sexo, con la misma información biológica y mapa. |
| Consulta de *Oxyura vittata* | Foto de hembra o macho según la variante; sin variante, foto representativa existente. No se reasigna a `oxyura_jamaicensis`. |
| Fotografía de variante no disponible | Aviso explícito, sin sustituirla silenciosamente por el otro sexo ni perder los datos comunes. |
| Acceso del juego sin variante | Foto representativa existente, sin inferir una variante del resultado de origen ni modificar las tarjetas. |
| Nombre científico de presentación diferente de la denominación existente | Correspondencia documentada, sin cambiar el clasificador ni perder el mapa. |
| Estado global diferente del nacional | Ambas categorías identificadas por ámbito, fuente y año. |
| Dato no verificable | Campo marcado como no disponible y pendiente documentado, sin texto inventado. |
| Foto ausente con mapa disponible, o situación inversa | Recurso válido consultable y fallo del otro explicado. |
| Mapa explícito o equivalencia ya admitida | Conservación del comportamiento existente. |
| Error de carga de imagen | No se abre una ampliación del marcador de sustitución. |
| Rotación o retorno desde ampliación | Misma especie, variante cuando exista y pestaña seleccionada. |
| Regreso a una respuesta del juego | Misma respuesta pendiente de evaluar, sin avanzar ni alterar el progreso. |
| Dispositivo sin conectividad desde antes de abrir la ficha | Consulta completa de los recursos locales y sus referencias. |
| Texto ampliado, pantalla pequeña o modo oscuro | Contenido y acciones legibles y alcanzables. |

## Fuera de alcance

- Secciones de comportamiento y cría; pestaña de canto, audios y reproducción.
- Preparar estructuras o controles específicos para una futura pestaña de canto.
- Nuevas especies ajenas a las 45 representadas por las etiquetas actuales, incluida `oxyura_jamaicensis`.
- Galerías de fotografías, selector manual de sexo dentro de la ficha o reemplazo general de las fotos existentes. La variante procede del acceso que abre la consulta.
- Nuevas variantes o cambios de etiquetas y reglas de selección de tarjetas del juego.
- Actualizaciones de contenido en vivo, consultas a servicios externos desde la app, descargas y sincronización.
- Reentrenamiento del clasificador, cambios de etiquetas o limpieza general del catálogo auxiliar.
- Nuevos mapas, cambios de su contenido biológico, mapas interactivos o geolocalización.
- Modificaciones de observaciones, exportaciones, puntuación o reglas de las partidas.
- Recuperación de una sesión del juego después del cierre del proceso más allá de lo ya establecido.
- Migración de la tecnología de interfaz o rediseño de pantallas ajenas a la ficha y su ampliación.

## Criterios de finalización

1. Se cubren las 50 etiquetas de las 45 especies, con correspondencia comprobada entre identidad, variante, fotografía, textos y mapa disponible. Las cinco parejas de macho y hembra tienen fotos verificadas para cada sexo y comparten información biológica y mapa.
2. Los campos solicitados tienen contenido investigado y respaldo verificable o una ausencia explícita justificada. Se han revisado las equivalencias taxonómicas y las diferencias entre conservación global y nacional.
3. Se reutilizan las fotografías apropiadas existentes y se incorporan las adicionales necesarias, con sus atribuciones y condiciones de uso documentadas; no quedan atribuciones requeridas sin resolver ni sustituciones hechas sin acordarlas.
4. La consulta desde identificación y juego, ambas pestañas, las ampliaciones y sus retornos cumplen los requisitos funcionales; se verifica la conservación de la sesión del juego.
5. Se comprueban las ausencias y errores de contenido, incluida una especie desconocida y una fotografía de variante ausente, sin confundir especies o sexos ni bloquear los recursos válidos. Se verifica también la apertura sin variante desde el juego.
6. Se ejecutan las comprobaciones automatizadas apropiadas y la revisión visual en dispositivo o emulador: ambos temas, ambas orientaciones, texto al 200 %, pantalla pequeña, navegación accesible y funcionamiento sin conexión. Se informan resultados y cualquier comprobación pendiente sin declararla aprobada.

## Dudas abiertas

No hay preguntas de alcance pendientes para redactar esta spec. La investigación deberá resolver y documentar los datos biológicos y las atribuciones todavía no verificados; su aprobación no equivale a afirmar que dicha investigación ya se haya realizado.

La siguiente etapa requiere la aprobación explícita de este documento. Después se redactará el plan técnico y, tras su aprobación, las tareas. No se implementará ni se harán commits o push en esta etapa.
