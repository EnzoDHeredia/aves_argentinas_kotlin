# Spec 004 — Escucha continua con lista acumulativa de aves

Estado: aprobada explícitamente por el usuario el 27 de octubre de 2026. Fecha: 27 de octubre de 2026.

## Contexto y objetivo

La identificación por sonido de la spec 003 captura diez segundos y luego presenta hasta cinco sugerencias. El usuario quiere escuchar durante el tiempo que decida y ver aparecer las aves reconocidas mientras el micrófono sigue activo, tomando BirdNET Live como referencia de comportamiento.

La función permitirá iniciar la escucha, acumular especies sin duplicados en una lista y detenerla conservando los resultados en pantalla. Cada nuevo inicio limpiará la lista anterior y comenzará otra sesión independiente.

Decisiones confirmadas por el usuario:

- Activar el micrófono manualmente y recibir coincidencias durante la escucha, hasta detenerla.
- Mostrar una animación que indique que el micrófono está escuchando.
- Agregar las especies una debajo de otra sin repetirlas.
- Mantener visible la lista al detener la escucha.
- Limpiar la lista y comenzar una nueva al volver a pulsar Escuchar; no continuar acumulando sobre la sesión detenida.

### Enfoque y propuestas incluidas para aprobación

Se propone sustituir el recorrido de diez segundos por un único modo de escucha continua en la sección actual. Mantener ambos modos añadiría una elección que no hace falta para el recorrido solicitado. El reconocimiento y la información de especies existentes seguirán siendo la base de la función.

La escucha funcionará mientras la pantalla de identificación esté abierta y la app esté en primer plano. Al pasar a segundo plano, bloquear el teléfono o abrir una ficha se detendrá, conservando la lista temporal. Estas interrupciones no reanudarán el micrófono automáticamente. Al abandonar la sección o cerrar el proceso se descartará la sesión, sin historial ni audio guardado.

Cada especie conservará su posición de primera aparición. Una detección posterior de la misma especie podrá mejorar la puntuación mostrada, sin añadir otra fila ni borrar especies que ya no se estén oyendo. La lista representará lo reconocido durante la sesión, no una afirmación de qué aves están vocalizando en ese instante.

## Usuarios

Personas que usan Aves Argentinas para reconocer aves en campo o aprender sus vocalizaciones, incluidas quienes no tienen conexión o utilizan lector de pantalla, texto ampliado o animaciones reducidas.

## Comportamiento actual y compatibilidad

La [spec 003](../003-identificacion-aves-sonido/spec.md) y su implementación constituyen el comportamiento base. Esta evolución modifica expresamente:

- La duración fija de diez segundos pasa a una escucha que termina por acción del usuario o interrupción.
- Los resultados dejan de aparecer solo al terminar: se actualizan mientras se captura audio.
- El máximo de cinco resultados de una captura no limita la lista acumulada de la sesión; esta puede contener más de cinco especies distintas.
- Cancelar y descartar una captura pasa a Detener y conservar las detecciones ya mostradas.
- Ir a segundo plano interrumpe la escucha y conserva la lista, en lugar de descartar toda la captura y sus resultados.
- El temporizador limitado a diez segundos se sustituye por una indicación de escucha activa y tiempo transcurrido sin cuenta regresiva.

Se preservan el reconocimiento real completamente offline, el catálogo y los nombres disponibles, la explicación de las puntuaciones, la consulta de fichas locales, los permisos mínimos y la ausencia de audio persistente u observaciones automáticas. La identificación fotográfica, captura instantánea, aprendizaje, historial, exportación y preferencias conservan su comportamiento y datos.

La verificación histórica de la spec 003 no certifica este flujo nuevo. La frecuencia de actualización, calidad de resultados y consumo durante escucha prolongada deberán validarse nuevamente; no se trasladarán sin medición las conclusiones obtenidas con capturas de diez segundos.

## Historias de usuario

- H1: Como usuario, quiero iniciar y detener la escucha cuando lo decida, para reconocer aves sin repetir capturas cortas.
- H2: Como usuario, quiero saber cuándo el micrófono está capturando audio, para controlar la escucha.
- H3: Como usuario, quiero ver nuevas especies durante la sesión sin filas repetidas, para saber cuáles se reconocieron.
- H4: Como usuario, quiero conservar y consultar la lista después de detener, para revisar los resultados con tranquilidad.
- H5: Como usuario, quiero que cada nuevo inicio tenga una lista vacía, para separar las detecciones de sesiones distintas.

## Requisitos funcionales (criterios de aceptación en EARS)

### Inicio, permiso y actividad del micrófono

- RF-1: CUANDO el usuario abra «Identificar por sonido», EL SISTEMA mostrará la explicación de escucha continua y la acción Escuchar sin activar el micrófono ni solicitar su permiso automáticamente. Conservará el acceso desde el menú y la captura instantánea existentes.
- RF-2: CUANDO el usuario pulse Escuchar sin permiso de micrófono, EL SISTEMA solicitará autorización y solo iniciará una sesión si obtiene el permiso mientras la solicitud siga vigente y la pantalla esté activa. Una denegación permitirá volver o reintentar; una denegación permanente ofrecerá acceder a Ajustes sin repetir solicitudes imposibles.
- RF-3: CUANDO se acepte un nuevo inicio de sesión, EL SISTEMA limpiará los resultados de la sesión anterior antes de mostrar detecciones nuevas, preparará el reconocimiento y activará el micrófono sin exigir otra pulsación. La operación será una sesión nueva, no una reanudación de la lista anterior. Si la preparación falla, mostrará el error sin recuperar resultados previos como si fueran actuales.
- RF-4: MIENTRAS la sesión esté escuchando, EL SISTEMA mantendrá la captura activa sin detenerse automáticamente a los diez segundos, permitirá Detener y mostrará el tiempo transcurrido desde el inicio efectivo de la captura. No mostrará progreso hacia un final de diez segundos.
- RF-5: MIENTRAS el micrófono capture audio, EL SISTEMA mostrará el estado Escuchando y una animación de actividad; durante preparación, detención, error o sesión detenida no indicará falsamente que está escuchando. Una preferencia de animación reducida conservará una indicación estática y textual equivalente.
- RF-6: MIENTRAS se prepare, escuche o detenga una sesión, EL SISTEMA impedirá inicios simultáneos y evitará que pulsaciones repetidas activen más de una escucha o mezclen resultados entre sesiones.

### Resultados durante la escucha y acumulación

- RF-7: MIENTRAS la sesión escuche, EL SISTEMA analizará audio reciente y publicará nuevas sugerencias sin esperar a Detener ni cerrar periódicamente el micrófono para analizar. Tras reunir audio suficiente para la primera identificación, seguirá actualizando resultados conforme se procese audio nuevo.
- RF-8: CUANDO una especie de ave supere el criterio de confianza validado y todavía no figure en la sesión, EL SISTEMA la añadirá al final de la lista, después de las especies ya mostradas. Solo admitirá resultados reales de la sesión vigente y mantendrá la lista acumulada aunque supere cinco especies.
- RF-9: CUANDO vuelva a reconocerse una especie ya presente, EL SISTEMA conservará una sola fila y su posición original. Si la nueva puntuación es mayor, actualizará la puntuación mostrada; en caso contrario conservará la anterior. Distintas etiquetas de una misma identidad científica no crearán especies duplicadas.
- RF-10: MIENTRAS continúe una sesión, EL SISTEMA conservará las especies previamente detectadas aunque no aparezcan en las identificaciones posteriores. La ausencia de detecciones nuevas no vaciará ni reordenará la lista, ni mostrará un estado que contradiga las detecciones ya acumuladas.
- RF-11: EL SISTEMA mostrará únicamente aves, conservará la prioridad de nombres regionales y españoles existentes y usará el científico con indicación de nombre común no disponible cuando corresponda. La falta de ficha o traducción no ocultará una detección válida ni la asociará a otra especie.
- RF-12: MIENTRAS se muestren resultados, EL SISTEMA presentará nombre científico y puntuación de cada especie, explicando que es una coincidencia del modelo y no un porcentaje garantizado de acierto. Identificará la lista como detecciones de la sesión; no afirmará que todas esas especies siguen vocalizando.
- RF-13: SI todavía no se acumularon detecciones durante una escucha activa, ENTONCES EL SISTEMA mostrará que sigue escuchando y esperando coincidencias, sin afirmar que no hay aves ni finalizar la sesión por ese motivo.

### Detención y consulta

- RF-14: CUANDO el usuario pulse Detener, EL SISTEMA dejará de capturar, detendrá la animación y conservará exactamente las especies y puntuaciones ya publicadas. No aceptará resultados tardíos ni nuevas filas de esa sesión después de aceptar la detención; indicará cualquier breve transición de detención hasta liberar el micrófono.
- RF-15: CUANDO se complete la detención, EL SISTEMA mantendrá la lista consultable y ofrecerá Escuchar para iniciar una sesión nueva según RF-3. Si la lista está vacía, indicará que no se identificaron aves con suficiente confianza durante esa sesión, sin afirmar ausencia de aves en el entorno.
- RF-16: DONDE una especie tenga información local compatible, EL SISTEMA permitirá abrir su ficha y el mapa disponible. Si la escucha está activa, la detendrá conservando la lista antes de navegar. Si falta información o una imagen no puede mostrarse, indicará su indisponibilidad sin inventar datos ni impedir otra sesión.
- RF-17: CUANDO el usuario vuelva de una ficha o su ampliación mientras el proceso siga abierto, EL SISTEMA conservará la misma lista y puntuaciones en estado detenido. No reactivará el micrófono; pulsar Escuchar iniciará una sesión nueva y limpiará esa lista.

### Ciclo de vida, errores y privacidad

- RF-18: CUANDO se cambie la orientación dentro de la sección, EL SISTEMA conservará la sesión, el tiempo transcurrido y la lista en su orden, sin duplicar solicitudes de permiso, captura ni detecciones; una escucha activa continuará mientras la app siga en primer plano.
- RF-19: CUANDO la app pase realmente a segundo plano o se bloquee el teléfono durante una escucha, EL SISTEMA detendrá la captura y su indicador, conservará las detecciones ya publicadas e informará la interrupción al regresar. Si la preparación estaba pendiente, no activará el micrófono más tarde ni reanudará automáticamente al volver.
- RF-20: SI el micrófono falta, está ocupado, se revoca el permiso o se interrumpe la captura, ENTONCES EL SISTEMA detendrá la sesión, liberará sus recursos, mostrará un error comprensible y conservará las detecciones ya publicadas. Permitir reintentar iniciará una sesión nueva; no rellenará con audio ficticio un intervalo que no pudo capturarse.
- RF-21: SI la preparación, los datos de reconocimiento o una inferencia fallan, ENTONCES EL SISTEMA distinguirá el error de una ausencia de coincidencias, detendrá la escucha y conservará únicamente los resultados válidos ya publicados. No añadirá resultados parciales inválidos ni ejemplos de reemplazo.
- RF-22: CUANDO el usuario abandone la sección o se cierre el proceso, EL SISTEMA descartará la sesión y liberará sus recursos; al volver a abrir la sección comenzará sin micrófono activo ni resultados recuperados. Abrir una ficha y regresar se regirá por RF-16 y RF-17 y no contará como abandonar definitivamente la sección.
- RF-23: EL SISTEMA procesará el audio localmente, sin guardarlo en archivos, enviarlo a servicios externos, incluirlo en logs o recuperarlo tras cerrar el proceso. La lista permanecerá solo durante la sesión de pantalla y no creará observaciones ni un historial persistente.

## Requisitos no funcionales

- Disponibilidad y compatibilidad: preservar el funcionamiento sin conexión desde el primer uso tras instalación completa, los permisos mínimos y las versiones de Android admitidas. La función no requerirá ubicación, almacenamiento, cuentas ni descargas dentro de la app.
- Capacidad de respuesta: capturar, reconocer y actualizar resultados sin bloquear navegación o Detener. La demora de actualización no crecerá indefinidamente cuanto más dure la sesión; una escucha prolongada no consumirá cada vez más memoria por conservar todo el audio o trabajo pendiente. Los datos necesarios para la lista podrán crecer con las especies distintas de la sesión.
- Calidad y latencia: el plan fijará una duración de escucha de prueba, frecuencia objetivo de actualización, presupuesto de latencia y criterio de confianza verificables en el Samsung Galaxy A56 del usuario. Se evaluarán aves argentinas, voces, viento, silencio y periodos sin detecciones. Una puntuación suficiente en capturas de diez segundos no certifica por sí sola el mismo comportamiento en escucha continua.
- Consumo y recursos: medir memoria, uso sostenido de procesamiento, calentamiento y consumo durante sesiones prolongadas, además de la liberación del micrófono al detener. Los presupuestos y condiciones del ensayo se acordarán en el plan antes de implementar. Ninguna medición anterior se presentará como una ejecución de este nuevo flujo.
- Identidad visual: reutilizar la paleta, estilos y componentes de la app. La animación comunicará actividad del micrófono; no simulará una medición de volumen o espectrograma que no se esté calculando. La lista y la acción Detener deberán poder consultarse y operarse mientras llegan detecciones.
- Accesibilidad: acciones de al menos 48 × 48 dp, ambos temas, orientaciones y texto al 200 % sin contenido inaccesible. Los cambios no robarán el foco ni desplazarán al usuario mientras consulta resultados anteriores. TalkBack podrá anunciar nuevas especies sin repetir toda la lista ni recibir anuncios continuos del reloj o de la animación; las especies ya mostradas seguirán consultables.
- Integridad y regresión: conservar correspondencias de modelo, etiquetas y especies, avisos de licencia y atribución, datos existentes y funciones ajenas. No se dará por revalidada la spec 003 completa por aprobar este documento.

## Casos límite ya cubiertos

- Detener antes de obtener una primera identificación: RF-14 y RF-15; lista vacía detenida y reintento, sin resultados tardíos.
- Nueva escucha tras una lista con resultados y fallo durante la preparación: RF-3 y RF-21; la sesión nueva permanece vacía con error, sin recuperar especies anteriores.
- Repetición de una especie con puntuaciones mejores, peores o iguales, o etiquetas equivalentes: RF-9; una fila estable y máximo de las detecciones publicadas.
- Más de cinco especies distintas y lectura de filas anteriores mientras llegan nuevas: RF-8 y requisitos de accesibilidad; acumulación con orden estable y contenido alcanzable.
- Intervalos de silencio, ruido o sin especies que superen confianza: RF-10 y RF-13; seguir escuchando sin borrar lo acumulado ni inventar coincidencias.
- Inferencia en curso al detener, ir a segundo plano, salir o iniciar otra sesión: RF-6, RF-14 y RF-19 a RF-22; las respuestas antiguas no alteran la lista congelada ni la nueva.
- Permiso denegado, revocado o concedido tarde: RF-2, RF-19 y RF-20; sin activación automática fuera de una solicitud vigente en pantalla.
- Rotación durante preparación, escucha, detención o consulta: RF-17 y RF-18; no reiniciar la sesión ni duplicar filas.
- Abrir ficha con micrófono activo, doble toque o recurso ilegible: RF-6, RF-16 y RF-17; detener, consultar y regresar sin autograbación ni pérdida de lista.
- Bloqueo, retorno del segundo plano, cierre del proceso o reapertura: RF-19 y RF-22; distinguir conservación temporal de persistencia.
- Animaciones desactivadas, texto ampliado y lector de pantalla mientras se agregan especies: RF-5 y requisitos de accesibilidad; misma operación y estado comprensible.

## Fuera de alcance

- Mantener un segundo modo seleccionable de captura fija de diez segundos en esta iteración.
- Escucha con pantalla bloqueada, en segundo plano o después de abandonar la sección; reanudación automática.
- Reanudar sobre una lista detenida: cualquier nueva pulsación de Escuchar inicia una lista vacía.
- Historial persistente de sesiones, observaciones automáticas, guardar, reproducir, importar, compartir o exportar audio o listas de sesiones.
- Visualización de espectrograma o medición de volumen; la animación solicitada es un indicador de escucha activa.
- Filtrado geográfico o estacional, entrenamiento de modelos, migraciones de interfaz o sustitución del motor de reconocimiento.
- Incorporar fotografías o fichas para todo el catálogo, identificar otros animales o combinar fotografía y sonido.
- Reproducir todos los modos, pantallas, animaciones o funciones de BirdNET Live; se toma como referencia la escucha con lista acumulativa descrita por el usuario.

## Criterios de finalización

1. En el Samsung Galaxy A56, comprobar Escuchar → preparación → micrófono activo y animación → nuevas especies durante captura → Detener → misma lista consultable. La escucha supera diez segundos sin detenerse automáticamente.
2. Demostrar una lista de más de cinco especies diferentes en orden de incorporación, sin duplicados ni cambios de posición. Una especie repetida solo aumenta su puntuación si mejora; un intervalo sin nuevas detecciones no borra filas.
3. Repetir Detener → Escuchar y demostrar limpieza de la lista antes de cualquier resultado nuevo. Cubrir un fallo del segundo inicio y respuestas tardías de la sesión anterior; ninguna debe recuperar o contaminar resultados.
4. Verificar vacío durante escucha y después de detener; errores de permiso, micrófono y reconocimiento; Detener antes de la primera inferencia y durante una llamada en curso. El micrófono y el indicador no quedan activos tras finalizar o fallar.
5. Verificar rotación, Home, bloqueo, salida, cierre real del proceso y consulta de ficha/zoom. Durante rotación se conserva la sesión; tras segundo plano o ficha se conserva la lista detenida; tras abandonar o cerrar el proceso no se recupera la sesión.
6. Repetir el recorrido offline desde una instalación completa y comprobar que no hay audio persistente, transferencias externas, historial nuevo ni alteración de observaciones existentes.
7. Evaluar la calidad y la latencia con audio identificado y controles negativos, y la estabilidad de una escucha prolongada con los presupuestos y condiciones aprobados en el plan. Registrar falsos positivos, omisiones, tiempos, consumo y memoria; no declarar éxito con resultados de capturas cortas o simulaciones de UI.
8. Revisar ambos temas, orientaciones, pantalla pequeña, texto al 200 %, animaciones desactivadas y recorrido manual con TalkBack mientras llegan resultados. Detener permanece accesible y las actualizaciones no impiden leer la lista.
9. Ejecutar compilación, pruebas y Lint adecuados al estado final, además de regresiones de identificación fotográfica, navegación, fichas, observaciones, exportación, aprendizaje y preferencias. Registrar evidencia y pendientes sin convertir verificaciones omitidas en aprobadas.

## Dudas abiertas

No quedan preguntas funcionales abiertas. El usuario aprobó explícitamente esta spec el 27 de octubre de 2026, incluidas las propuestas de modo continuo único, orden de primera aparición, puntuación máxima, conservación al interrumpir y descarte al abandonar. El siguiente artefacto es el plan técnico, que requiere su propia aprobación.

El detalle de la captura continua, frecuencia de reconocimiento, latencia, criterio de confianza, duración de las pruebas y presupuestos de consumo corresponde al plan técnico, que requerirá aprobación independiente. Si la evaluación obliga a cambiar el comportamiento aquí definido, se revisará primero esta spec.

Este documento no modifica los artefactos históricos de la spec 003 ni autoriza implementación. Después de aprobarlo se prepararán plan y tareas, respetando una tarea por turno y sin commits ni push.
