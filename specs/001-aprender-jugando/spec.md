# Spec 001 — Aprender jugando

Estado: aprobada por el usuario el 19 de octubre de 2026. Fecha: 19 de octubre de 2026.

## Contexto y objetivo

La aplicación permite identificar aves y registrar observaciones. Se incorporará una sección «Aprender jugando», accesible desde el menú principal, para aprender mediante tarjetas que muestran una pregunta o una fotografía y revelan la respuesta al girarlas.

La primera versión tendrá dos modalidades: «Preguntas sobre aves», con contenidos generales de diversos temas, y «Reconocé la especie», con fotografías de aves del catálogo. Funcionará sin conexión y sin historial de partidas.

Decisiones confirmadas por el usuario:

- Acceso desde el menú principal.
- Partidas de 10 tarjetas con autoevaluación mediante «La sabía» y «Quiero repasarla».
- Resumen temporal al terminar y posibilidad de repasar las tarjetas marcadas en esa sesión.
- Conservación de la partida activa al girar el teléfono o volver del segundo plano mientras la app siga abierta.
- Descarte de la sesión al salir de la partida o cerrar la app; no se guardan partidas anteriores.

### Contenido inicial

- Preguntas: el [banco inicial de 40 tarjetas](../../docs/contenido/banco-preguntas-aves-v1.md), con cinco preguntas por tema: anatomía y plumaje; vuelo y desplazamiento; alimentación; nidos y reproducción; comunicación y comportamiento; migración; ambientes y funciones ecológicas; conservación y observación responsable. La aprobación de esta spec incluye ese banco como contenido de partida, sujeto a correcciones de redacción o exactitud durante su revisión.
- Fotografías: una selección revisada de la colección `aves_tesis_v2` aportada por el usuario. La colección contiene 3.500 imágenes en 50 carpetas, correspondientes a 45 especies; algunas especies tienen carpetas separadas por sexo. El contenido inicial deberá incluir al menos una fotografía clara y correctamente identificada por cada una de esas 45 especies. No se incorporará una tarjeta para una especie que no tenga una fotografía validada.
- Las etiquetas de sexo no representan especies diferentes. El objetivo del juego fotográfico es reconocer la especie, sin exigir identificar el sexo.

## Usuarios

Personas que usan Aves Argentinas y quieren aprender o repasar conocimientos generales e identificación visual de aves. No requieren una cuenta, observaciones previas ni conocimientos técnicos.

## Historias de usuario

- H1: Como usuario, quiero entrar a «Aprender jugando» desde el menú principal, para acceder fácilmente al contenido educativo.
- H2: Como usuario, quiero pensar una respuesta antes de girar la tarjeta, para practicar conocimientos generales sobre aves.
- H3: Como usuario, quiero intentar reconocer un ave por su foto antes de ver su nombre, para mejorar mi identificación visual.
- H4: Como usuario, quiero indicar si sabía la respuesta y repasar las tarjetas que marqué, para concentrarme en lo que necesito aprender.
- H5: Como usuario, quiero un resumen de la sesión sin guardar un historial, para jugar sin acumular registros de partidas.

## Requisitos funcionales (criterios de aceptación en EARS)

### Acceso y modalidades

- RF-1: EL SISTEMA mostrará una opción «Aprender jugando» en el menú principal, junto con los accesos existentes.
- RF-2: CUANDO el usuario seleccione «Aprender jugando», EL SISTEMA mostrará las modalidades «Preguntas sobre aves» y «Reconocé la especie», con una descripción breve de cada una.
- RF-3: CUANDO el usuario inicie una modalidad, EL SISTEMA comenzará una nueva ronda de 10 tarjetas elegidas al azar entre los contenidos válidos de esa modalidad, salvo la excepción de contenido insuficiente de RF-29.
- RF-4: CUANDO el usuario vuelva desde identificación al menú principal con la captura instantánea activada, EL SISTEMA permitirá acceder a «Aprender jugando» sin reenviarlo a identificación. La apertura inicial configurada por captura instantánea conservará su comportamiento actual.

### Contenido de las tarjetas

- RF-5: DONDE la modalidad sea «Preguntas sobre aves», EL SISTEMA ofrecerá las 40 tarjetas del banco inicial y mostrará en el frente solo la pregunta y en el dorso su respuesta y aclaración, cuando la haya.
- RF-6: DONDE la modalidad sea «Reconocé la especie», EL SISTEMA mostrará en el frente una fotografía y la consigna «¿Qué especie es?», sin revelar el nombre común, científico ni identificadores que permitan deducir la respuesta.
- RF-7: CUANDO se revele una tarjeta de «Reconocé la especie», EL SISTEMA mostrará el nombre común y el nombre científico de la especie de la fotografía en un formato legible, sin códigos de carpetas ni guiones bajos.
- RF-8: MIENTRAS se prepare una ronda normal, EL SISTEMA evitará preguntas repetidas en la modalidad general y especies repetidas en la modalidad fotográfica, incluso si existen varias fotos o carpetas de macho y hembra de una misma especie.
- RF-9: CUANDO comience una tarjeta, EL SISTEMA mostrará su frente y un indicador de posición y total, por ejemplo «Tarjeta 3 de 10».

### Giro y autoevaluación

- RF-10: CUANDO el usuario toque el frente de una tarjeta, EL SISTEMA girará la tarjeta y revelará su dorso.
- RF-11: CUANDO el usuario toque el dorso antes de evaluarlo, EL SISTEMA permitirá volver al frente sin cambiar de tarjeta ni registrar una autoevaluación.
- RF-12: MIENTRAS una tarjeta esté girando o se esté cambiando de tarjeta, EL SISTEMA impedirá que toques repetidos provoquen giros superpuestos, evaluaciones duplicadas o saltos de tarjetas.
- RF-13: MIENTRAS se muestre el dorso de una tarjeta pendiente de evaluación, EL SISTEMA ofrecerá «La sabía» y «Quiero repasarla».
- RF-14: CUANDO el usuario elija una de esas opciones, EL SISTEMA registrará una sola autoevaluación para esa tarjeta en la pasada actual y avanzará a la siguiente con el frente visible; si era la última, mostrará el resumen.
- RF-15: EL SISTEMA tratará las elecciones como autoevaluaciones del usuario y no como aciertos comprobados. No solicitará escribir una respuesta ni elegir entre posibles respuestas.

### Resumen y repaso

- RF-16: CUANDO se complete una ronda o una pasada de repaso, EL SISTEMA mostrará el total de tarjetas evaluadas y las cantidades de «La sabía» y «Quiero repasarla» de esa pasada. Ambas cantidades deberán sumar el total evaluado.
- RF-17: MIENTRAS se muestre un resumen con tarjetas marcadas «Quiero repasarla», EL SISTEMA ofrecerá «Repasar marcadas».
- RF-18: CUANDO el usuario elija «Repasar marcadas», EL SISTEMA iniciará una pasada con únicamente las tarjetas marcadas en la pasada recién finalizada, conservando sus preguntas o fotografías, con el frente visible y con contadores nuevos. El total será el número de marcadas, entre 1 y 10; no se agregarán tarjetas para completar 10.
- RF-19: CUANDO termine un repaso, EL SISTEMA mostrará un resumen de esa pasada y permitirá volver a repasar únicamente las que se hayan marcado otra vez. No sumará resultados de pasadas anteriores ni mostrará estadísticas acumuladas.
- RF-20: MIENTRAS se muestre un resumen sin tarjetas marcadas, EL SISTEMA omitirá o deshabilitará «Repasar marcadas» y permitirá iniciar una nueva ronda o salir.
- RF-21: CUANDO el usuario elija «Jugar de nuevo» desde un resumen, EL SISTEMA descartará los resultados y marcas de la sesión anterior y comenzará una nueva ronda de la misma modalidad. Las tarjetas podrán coincidir con las de rondas anteriores.
- RF-22: MIENTRAS se muestre un resumen, EL SISTEMA ofrecerá además volver a elegir modalidad o regresar al menú principal, descartando la sesión que termina.

### Navegación y duración de la sesión

- RF-23: CUANDO el usuario salga de una ronda o repaso mediante la navegación Atrás, EL SISTEMA descartará la sesión y regresará al selector de modalidades. Desde un resumen, Atrás también regresará al selector descartando la sesión. Desde el selector, Atrás volverá al menú principal.
- RF-24: EL SISTEMA omitirá el guardado de partidas anteriores, resultados, marcas de repaso, rachas, récords y estadísticas acumuladas. Tampoco ofrecerá un historial ni la recuperación de sesiones abandonadas.
- RF-25: CUANDO se gire el teléfono o se vuelva del segundo plano mientras la app continúe abierta, EL SISTEMA conservará la modalidad, las tarjetas elegidas, su posición, el lado visible, las autoevaluaciones y el estado de ronda, repaso o resumen, sin registrar una nueva evaluación.
- RF-26: SI el usuario cierra la app o el sistema la cierra, ENTONCES EL SISTEMA descartará la sesión; la próxima entrada a «Aprender jugando» comenzará desde la elección de modalidad.

### Disponibilidad y errores

- RF-27: EL SISTEMA permitirá completar las dos modalidades, consultar sus respuestas y repasar las marcadas sin conexión y sin solicitar permisos de cámara, ubicación o acceso a las fotos del dispositivo para jugar.
- RF-28: SI una tarjeta tiene una pregunta, respuesta o nombre obligatorio vacío, o una fotografía ausente, ilegible o sin correspondencia validada con una especie, ENTONCES EL SISTEMA excluirá esa tarjeta del contenido jugable. Si el problema se detecta durante la sesión, mostrará un aviso breve sin asignarle una autoevaluación. En una ronda normal intentará reemplazarla por otra válida sin repetir preguntas o especies; si no hay reemplazo, ajustará el total. En un repaso la omitirá y ajustará el total, sin agregar tarjetas ajenas al repaso. Si no queda ninguna tarjeta jugable ni evaluada, informará la indisponibilidad y permitirá volver al selector, sin mostrar un resumen de una pasada no realizada.
- RF-29: SI una modalidad tiene menos de 10 tarjetas válidas o especies válidas, según corresponda, ENTONCES EL SISTEMA informará cuántas están disponibles y permitirá una ronda con ese total, sin introducir repeticiones para completar 10.
- RF-30: SI una modalidad no tiene contenido válido, ENTONCES EL SISTEMA mostrará un mensaje de indisponibilidad e impedirá iniciarla, manteniendo la navegación al selector y al menú principal.

## Requisitos no funcionales

- Idioma: textos y consignas en español, coherentes con la aplicación; nombres científicos con su escritura convencional.
- Accesibilidad: las tarjetas y acciones deberán poder operarse con lector de pantalla. La descripción accesible de la foto y del frente no revelará la respuesta antes del giro; al revelar el dorso, su contenido estará disponible para lectura.
- Legibilidad: las preguntas, respuestas, fotografías, controles y resumen deberán seguir siendo utilizables con texto ampliado y en orientaciones vertical y horizontal, sin contenido inaccesible por recortes o superposiciones.
- Movimiento: la interacción respetará la configuración del dispositivo para reducir o desactivar animaciones, manteniendo disponible la revelación de la respuesta.
- Apariencia: la sección respetará los temas claro y oscuro y el lenguaje visual de la aplicación.
- Privacidad: el juego no requerirá cuenta ni recopilará registros persistentes de actividad del jugador.
- Calidad editorial: cada pregunta deberá tener una respuesta clara y respaldada; cada fotografía incluida deberá revisarse para que el ave sea identificable y corresponda con el nombre mostrado.
- Compatibilidad funcional: identificación, observaciones, preferencias y navegación existentes deberán conservar su funcionamiento; la partida no generará observaciones de aves.

## Casos límite ya cubiertos

- Varios toques durante el giro, al evaluar o al avanzar: RF-12 y RF-14.
- Volver al frente después de revelar, sin haber evaluado: RF-11.
- Dos fotografías de una especie, incluso de distinto sexo: RF-8.
- Falta de imágenes para una especie del catálogo: se excluye de la modalidad fotográfica; no se exige cubrir especies sin material validado.
- Cero, una o varias tarjetas marcadas para repasar: RF-17 a RF-20.
- Nuevos repasos: solo se usan las marcas de la pasada recién terminada, sin acumular resultados: RF-18 y RF-19.
- Contenido inválido, insuficiente o inexistente: RF-28 a RF-30.
- Rotación y retorno desde segundo plano, tanto en una tarjeta como en un resumen: RF-25.
- Cierre de la app por el usuario o por el sistema: RF-26.
- Abandono de una sesión y entrada posterior: RF-23, RF-24 y RF-26.
- Entrada al menú desde identificación con captura instantánea activada: RF-4.

## Fuera de alcance

- Historial de partidas, guardado para continuar después de cerrar la app y estadísticas entre sesiones.
- Cuentas, sincronización, rankings, premios, rachas y competencia entre jugadores.
- Corrección automática de respuestas, respuestas escritas, opciones múltiples y límites de tiempo.
- Filtros por tema o dificultad y configuración de la cantidad de tarjetas por ronda.
- Programación de repasos para otros días y notificaciones.
- Preguntas de audio, identificación de cantos y reconocimiento específico de machos y hembras.
- Creación o edición de tarjetas por parte del jugador.
- Descarga de paquetes de contenido y uso del conjunto completo de 3.500 imágenes.
- Cambios al catálogo de especies, al clasificador o al historial de observaciones.

## Criterios de finalización

1. Desde el menú principal se puede entrar, elegir cualquiera de las dos modalidades y volver sin alterar los otros accesos, incluida la navegación con captura instantánea.
2. El contenido publicado incluye las 40 preguntas generales y fotografías revisadas de las 45 especies disponibles en la colección aportada.
3. Cada ronda normal contiene 10 tarjetas válidas sin preguntas ni especies repetidas, con la excepción informada de contenido insuficiente.
4. Se puede revelar y volver al frente de una tarjeta; las acciones de autoevaluación aparecen al mostrar el dorso y cada tarjeta cuenta una sola vez por pasada.
5. El resumen refleja exactamente las autoevaluaciones de la pasada recién completada. Una ronda con 7 elecciones «La sabía» y 3 «Quiero repasarla» muestra esas cantidades y permite un repaso de exactamente esas 3 tarjetas.
6. Si durante ese repaso se marcan 2 como sabidas y 1 para repasar, el siguiente resumen corresponde a 3 tarjetas y el siguiente repaso contiene únicamente esa 1. Si ninguna queda marcada, no se inicia un repaso vacío.
7. Rotar o pasar brevemente a segundo plano conserva el estado sin duplicar evaluaciones. Salir de la sesión o cerrar la app impide recuperarla en una entrada posterior.
8. Las modalidades y los repasos funcionan sin conexión ni permisos específicos para jugar; no aparecen historiales ni resultados de sesiones anteriores.
9. Se comprueban los casos de contenido vacío, incompleto y fotografías que no pueden mostrarse, además de los temas, la legibilidad y la operación accesible.

## Dudas abiertas

Ninguna decisión funcional pendiente. El usuario aprobó esta spec, su contenido inicial y el plan técnico el 19 de octubre de 2026. El siguiente artefacto es la lista de tareas, que requiere su propia aprobación antes de implementar.
