# Spec 002 — Mejora de UI/UX de «Aprender jugando»

Estado: aprobada explícitamente por el usuario el 20 de octubre de 2026. Fecha: 20 de octubre de 2026.

## Contexto y objetivo

La sección «Aprender jugando» ya permite elegir una modalidad, revelar tarjetas, autoevaluarse y repasar. Su presentación concentra el contenido en la parte superior, deja espacios vacíos desproporcionados y cambia bruscamente la altura de la tarjeta fotográfica cuando se revela la respuesta. Tampoco ofrece un acceso desde el juego a la ficha de distribución del ave.

El objetivo es mejorar la sección completa para que se perciba como parte de la misma aplicación: composición equilibrada, jerarquía clara, fotografías protagonistas y acciones fáciles de encontrar. La mejora abarca el selector, las dos modalidades, los frentes y respuestas, la autoevaluación, el repaso y el resumen.

Decisión confirmada por el usuario: la fotografía también debe verse en la respuesta, junto con el nombre común y el científico. Se conservará la posibilidad existente de volver al frente. Después de revelar una especie, se podrá consultar su ficha con el mapa y regresar a la misma tarjeta.

## Usuarios

Personas que usan Aves Argentinas para aprender sobre aves o practicar su identificación visual, incluidas quienes usan texto ampliado, lector de pantalla o dispositivos con poco espacio disponible.

## Comportamiento actual y compatibilidad

La [spec 001](../001-aprender-jugando/spec.md) define el comportamiento base. Esta propuesta extiende la presentación de la respuesta fotográfica y agrega la consulta de la ficha durante una partida. Los demás comportamientos de aquella spec se conservan:

- Dos modalidades, rondas normales de 10 tarjetas y las excepciones existentes de contenido insuficiente o inválido.
- Respuestas ocultas inicialmente, giro reversible y autoevaluación mediante «La sabía» o «Quiero repasarla», con avance tras una única evaluación.
- Repaso de las tarjetas marcadas en la última pasada y resumen temporal, sin convertir las autoevaluaciones en aciertos comprobados.
- Funcionamiento sin conexión, sin cuenta y sin permisos específicos para jugar.
- Conservación durante rotaciones y retornos del segundo plano mientras la app siga abierta; descarte al abandonar la sesión o cerrarse el proceso.
- Navegación desde el menú y compatibilidad con identificación, observaciones y preferencias.

El acceso a la ficha será una consulta temporal dentro de la sesión: abrirla no equivale a abandonar el juego. El botón Atrás desde la propia partida seguirá teniendo el comportamiento de salida definido en la spec 001.

La [validación pendiente de la spec 001](../001-aprender-jugando/tasks.md) conserva su estado. Aprobar o implementar esta propuesta no resuelve ni da por superados aquellos pendientes.

## Historias de usuario

- H1: Como usuario, quiero una sección visualmente coherente con la aplicación y bien distribuida, para entender las opciones y jugar cómodamente.
- H2: Como usuario, quiero seguir viendo la fotografía al conocer la respuesta, para relacionar los rasgos del ave con su nombre.
- H3: Como usuario, quiero consultar la distribución de la especie revelada y volver a la tarjeta, para ampliar lo aprendido sin perder mi partida.
- H4: Como usuario, quiero distinguir las acciones de consulta, autoevaluación y continuación, para evitar avanzar o marcar una tarjeta por error.
- H5: Como usuario, quiero un resumen claro con opciones de repaso y nueva partida, para decidir cómo continuar.

## Requisitos funcionales (criterios de aceptación en EARS)

### Composición y selección de modalidad

- RF-1: EL SISTEMA mantendrá una cabecera y navegación coherentes con las pantallas existentes, con los mismos roles visuales para títulos, superficies, bordes y acciones.
- RF-2: MIENTRAS el contenido de un estado quepa en el área útil bajo la cabecera, EL SISTEMA distribuirá el espacio libre por encima y por debajo del bloque principal para centrarlo verticalmente; el bloque incluirá sus textos y acciones y no quedará concentrado en la parte superior con todo el espacio sobrante debajo.
- RF-3: SI el contenido excede el área útil, ENTONCES EL SISTEMA permitirá recorrerlo mediante desplazamiento desde su inicio hasta su última acción, sin recortes, superposiciones ni desplazamiento horizontal obligatorio.
- RF-4: CUANDO se muestre el selector, EL SISTEMA presentará cada modalidad como una opción visualmente agrupada con su título, descripción y disponibilidad, con igual jerarquía entre ambas modalidades y una zona de activación claramente identificable.
- RF-5: MIENTRAS se cargue el contenido o una modalidad no esté disponible, EL SISTEMA mostrará su estado junto a la opción correspondiente y evitará iniciar una modalidad no disponible, conservando los avisos y excepciones de la spec 001.

### Tarjetas y respuesta

- RF-6: CUANDO comience una tarjeta de ronda o repaso, EL SISTEMA mostrará el frente y el progreso de la pasada, sin revelar por texto, descripción accesible ni acciones de consulta la respuesta de una especie todavía oculta.
- RF-7: CUANDO el usuario revele una tarjeta fotográfica, EL SISTEMA mostrará la misma fotografía junto con el nombre común y el nombre científico de esa especie, sin requerir un nuevo giro para comparar la imagen con los nombres.
- RF-8: MIENTRAS se alterne entre frente y respuesta de una tarjeta fotográfica, EL SISTEMA conservará el tamaño y la ubicación del área de fotografía; si ambos estados caben en pantalla, conservará también el espacio total ocupado por el bloque de tarjeta y acciones para evitar saltos de su composición vertical.
- RF-9: EL SISTEMA mostrará las fotografías completas, con sus proporciones originales y sin deformarlas ni recortar partes del ave para llenar el espacio disponible.
- RF-10: CUANDO el usuario quiera volver al frente de una tarjeta revelada, EL SISTEMA ofrecerá una acción explícita para hacerlo, conservará la interacción de giro existente y mantendrá la tarjeta, la fotografía y el progreso sin registrar una evaluación. En el frente volverán a ocultarse la respuesta y el acceso a la ficha.
- RF-11: DONDE la modalidad sea «Preguntas sobre aves», EL SISTEMA aplicará la misma jerarquía visual y distribución equilibrada, conservando la pregunta en el frente y la respuesta y su aclaración, cuando exista, en el dorso; no ofrecerá una ficha de especie para preguntas generales.
- RF-12: MIENTRAS se muestre una respuesta pendiente de evaluación, EL SISTEMA distinguirá visualmente las acciones de consultar o volver al frente de «La sabía» y «Quiero repasarla». Consultar o girar no registrará una evaluación ni avanzará; evaluar registrará una sola elección y avanzará según la spec 001.

### Consulta de la ficha del ave

- RF-13: MIENTRAS se muestre la respuesta de una tarjeta fotográfica, EL SISTEMA ofrecerá «Ver información del ave» como acción separada de la autoevaluación y del giro.
- RF-14: CUANDO el usuario seleccione «Ver información del ave», EL SISTEMA abrirá la ficha existente correspondiente a la especie de esa tarjeta, con su mapa de distribución y la posibilidad existente de ampliarlo; no mostrará información de otra especie ni registrará una observación.
- RF-15: CUANDO el usuario regrese de la ficha o de la ampliación del mapa al juego mientras la app siga abierta, EL SISTEMA conservará la modalidad, la pasada, el orden de tarjetas, la tarjeta actual, el lado de respuesta, la fotografía, el progreso y las autoevaluaciones previas. La tarjeta consultada seguirá pendiente de evaluar.
- RF-16: SI el mapa de una especie no está disponible o no puede mostrarse, ENTONCES EL SISTEMA indicará de forma legible su indisponibilidad y permitirá volver a la misma respuesta y continuar jugando sin evaluar, omitir ni reemplazar la tarjeta por ese motivo.
- RF-17: MIENTRAS se abra una ficha o se produzca una transición de giro o avance, EL SISTEMA impedirá que toques repetidos provoquen aperturas duplicadas, giros superpuestos o evaluaciones múltiples. Activar el acceso a la ficha no ejecutará también el giro.

### Resumen y repaso

- RF-18: CUANDO termine una ronda o un repaso, EL SISTEMA mostrará el total evaluado y las dos cantidades de autoevaluación como un conjunto visual, con etiquetas legibles y sin presentarlas como una puntuación de aciertos.
- RF-19: MIENTRAS existan tarjetas marcadas en el resumen, EL SISTEMA dará prioridad visual a «Repasar marcadas», mostrará «Jugar de nuevo» como alternativa y distinguirá las acciones de elegir modalidad y volver al menú. Si no hay marcadas, omitirá la acción de repaso y dará prioridad a «Jugar de nuevo».
- RF-20: CUANDO se inicie un repaso, EL SISTEMA aplicará las mismas mejoras de tarjeta, fotografía visible en la respuesta y consulta de ficha, conservando la selección y los contadores definidos por la spec 001.

## Requisitos no funcionales

- Identidad visual: reutilizar la paleta, la tipografía y los criterios de superficies, bordes y botones de la aplicación. La fotografía será el elemento protagonista de la modalidad de especies; la decoración no competirá con ella ni con las acciones.
- Consistencia: mantener márgenes y separaciones regulares, tamaños de títulos y textos por función y el mismo tratamiento para acciones equivalentes a lo largo del recorrido. El nombre común tendrá jerarquía sobre el científico, que conservará su escritura convencional y presentación en cursiva.
- Adaptación: comprobar pantallas pequeñas, orientaciones vertical y horizontal y texto al 200 %. El centrado se aplicará cuando el bloque quepa; la lectura completa y el acceso a las acciones tendrán prioridad cuando no quepa.
- Accesibilidad: controles con nombres comprensibles y áreas táctiles de al menos 48 × 48 dp, orden de lectura coherente, contraste legible en ambos temas y acciones operables con TalkBack. La ficha tendrá su propio control accesible, sin quedar absorbido por la acción de giro de la tarjeta.
- Movimiento: respetar las animaciones desactivadas del dispositivo; la foto y la respuesta seguirán siendo consultables y el estado será el mismo con o sin animación.
- Temas: aplicar el tema claro u oscuro elegido por el usuario en selector, tarjetas, respuesta, resumen y consulta de ficha.
- Disponibilidad y privacidad: jugar, ver la foto y consultar los mapas disponibles funcionará sin conexión y sin incorporar permisos, cuentas, servicios externos ni persistencia de partidas.
- Compatibilidad: conservar el contenido del juego y el comportamiento de las otras secciones. Los cambios a la ficha se limitarán a lo necesario para la consulta desde el juego y el aviso de mapa no disponible, sin alterar sus accesos actuales.

## Casos límite ya cubiertos

- Foto vertical, horizontal o con proporciones distintas: imagen completa y composición estable dentro de cada tarjeta; RF-8 y RF-9.
- Nombre largo, aclaración extensa, pantalla pequeña, horizontal o texto ampliado: desplazamiento y acciones alcanzables; RF-3 y requisitos de adaptación.
- Volver al frente y revelar otra vez: misma fotografía y tarjeta, sin evaluación; RF-7 y RF-10.
- Abrir la ficha antes de autoevaluar, consultar el zoom y regresar: respuesta y sesión conservadas; RF-14 y RF-15.
- Rotar en el juego o durante la consulta, o pasar a segundo plano sin cierre del proceso: conservación de la sesión según la spec 001 y RF-15.
- Cierre del proceso durante la consulta: se mantiene el descarte de sesión de la spec 001; no se promete recuperación de la partida.
- Mapa ausente o ilegible: aviso y retorno al juego sin penalización; RF-16. Una foto jugable ausente sigue el manejo de contenido inválido de la spec 001.
- Toques repetidos y activación del enlace dentro de una tarjeta que puede girarse: una sola acción efectiva; RF-17.
- Cero, una o varias tarjetas para repasar y sucesivas pasadas: RF-19 y RF-20, conservando las reglas originales.
- Lector de pantalla antes de revelar: no filtrar la respuesta ni el acceso identificable a la especie; RF-6.

## Fuera de alcance

- Nuevas modalidades, puntuación automática, temporizadores, premios, rankings o cambios a la cantidad de tarjetas.
- Historial, estadísticas persistentes, guardado de partidas o recuperación tras el cierre del proceso.
- Modificaciones al banco de preguntas, catálogo de especies, fotografías seleccionadas o clasificador.
- Un mapa nuevo, descargas de mapas o rediseño general de la ficha del ave.
- Rediseño del menú, identificación, observaciones, preferencias u otras pantallas ajenas al juego.
- Migraciones de tecnología, nuevas dependencias y cambios de arquitectura que no sean necesarios para este alcance.
- Resolución de los bloqueos previos de compilación o validación registrados en la spec 001; deberán tratarse explícitamente en el plan como dependencias o comprobaciones pendientes, sin declararlos resueltos por esta mejora.

## Criterios de finalización

1. Revisar capturas de selector, pregunta, respuesta general, frente fotográfico, respuesta fotográfica y resumen, junto con pantallas existentes de la app. Confirmar la coherencia visual y el centrado del bloque bajo la cabecera cuando quepa, sin espacios vacíos desproporcionados concentrados debajo.
2. Comparar frente y respuesta de una misma tarjeta fotográfica: la fotografía permanece completa y conserva su área; cuando ambos estados caben, el bloque no cambia de ubicación o altura al revelar. Los nombres, la ficha y las dos autoevaluaciones son legibles y distinguibles.
3. Comprobar el recorrido revelar → volver al frente → revelar: fotografía idéntica, respuesta oculta en el frente, mismo progreso y ninguna evaluación registrada.
4. Comprobar revelar → ver información → ampliar mapa → volver al juego: corresponde a la especie de la tarjeta, se recupera la respuesta y solo una autoevaluación posterior avanza la partida. Repetir con rotación y retorno del segundo plano sin cierre del proceso.
5. Comprobar un mapa no disponible: aviso legible, retorno a la misma respuesta y continuación normal. Los accesos anteriores a la ficha siguen funcionando.
6. Completar ambas modalidades y un repaso con la secuencia 10/7/3 → 3/2/1 → 1/1/0. Los resúmenes corresponden a cada pasada y la prioridad de acciones cambia cuando no quedan tarjetas marcadas.
7. Revisar temas claro y oscuro, vertical y horizontal, pantalla pequeña, texto al 200 %, TalkBack y animaciones desactivadas. No hay contenido inaccesible ni respuesta expuesta antes de revelar, y todos los controles son operables.
8. Verificar juego y consulta de mapas disponibles sin conexión y sin permisos específicos; comprobar toques repetidos y navegación Atrás sin duplicar acciones ni alterar las reglas de descarte.
9. Registrar los resultados automatizados y visuales, distinguiendo verificaciones realizadas, fallos y pendientes heredados. No declarar completa la implementación con comprobaciones obligatorias pendientes.

## Dudas abiertas

No quedan decisiones funcionales abiertas. La fotografía visible también en la respuesta fue confirmada por el usuario y la spec completa fue aprobada explícitamente el 20 de octubre de 2026. El siguiente artefacto es el plan técnico, que requiere su propia aprobación antes de generar tareas. La aprobación de esta spec no autoriza todavía la implementación.
