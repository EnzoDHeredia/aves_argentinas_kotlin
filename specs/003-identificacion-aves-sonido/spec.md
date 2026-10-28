# Spec 003 — Identificación de aves por sonido

Estado: aprobada explícitamente por el usuario el 26 de octubre de 2026. Fecha: 26 de octubre de 2026.

## Contexto y objetivo

Aves Argentinas permite identificar aves mediante fotografías, consultar mapas de distribución y registrar observaciones. Se incorporará «Identificar por sonido» para grabar una vocalización con el micrófono del teléfono y consultar las especies sugeridas sin conexión a Internet.

La función está destinada al uso personal o educativo sin monetización. Las sugerencias ayudarán a reconocer aves; no se presentarán como identificaciones confirmadas ni como observaciones registradas automáticamente.

Decisiones confirmadas durante el análisis:

- Incorporar la función a la aplicación existente, desde su navegación principal.
- Grabar aproximadamente 10 segundos y presentar hasta cinco especies sugeridas, con sus nombres y puntuaciones.
- Realizar el reconocimiento con el modelo real seleccionado, completamente en el dispositivo y sin enviar audio a servicios externos.
- Preservar la identificación fotográfica y reutilizar la información de especies disponible.

Propuestas de alcance incluidas en la aprobación de esta spec:

- Mostrar cualquier especie de ave admitida por el reconocimiento, aunque no pertenezca al catálogo fotográfico actual. La ausencia de una ficha local no ocultará una detección válida.
- Mantener las grabaciones y los resultados únicamente durante la sesión; no guardar audio ni crear observaciones en el historial.
- Ofrecer grabación manual de 10 segundos y cancelación, sin escucha continua ni grabación en segundo plano.
- Posponer el filtrado geográfico y estacional, la reproducción del audio y la visualización de la señal.

### Enfoques considerados

- Limitar los resultados al catálogo actual facilitaría que todas las sugerencias tuvieran recursos locales, pero impediría mostrar otras aves reconocibles. Se propone ampliar los resultados y distinguir la disponibilidad de las fichas.
- El reconocimiento mediante un servicio remoto incumpliría el uso sin conexión. El usuario eligió el reconocimiento local con la variante reducida de la generación seleccionada durante el análisis; su integración y versión exacta se documentarán en el plan técnico.

## Usuarios

Personas que usan Aves Argentinas para reconocer aves en campo o aprender sus vocalizaciones, incluidas quienes no tienen conexión, deniegan permisos opcionales o necesitan texto ampliado y lector de pantalla.

## Historias de usuario

- H1: Como usuario, quiero acceder a «Identificar por sonido» desde el menú, para reconocer un ave sin fotografiarla.
- H2: Como usuario, quiero iniciar y cancelar una grabación breve sabiendo cuándo se usa el micrófono, para controlar qué se escucha.
- H3: Como usuario, quiero recibir sugerencias reales sin conexión, para utilizar la función en campo.
- H4: Como usuario, quiero ver los nombres y las puntuaciones de las aves sugeridas y consultar los recursos disponibles, para interpretar el resultado.
- H5: Como usuario, quiero poder reintentar cuando no haya una detección útil o se produzca un error, para continuar usando la app.

## Requisitos funcionales (criterios de aceptación en EARS)

### Acceso y permisos

- RF-1: EL SISTEMA mostrará «Identificar por sonido» en el menú principal junto con las secciones existentes, sin alterar el comportamiento configurado de apertura por captura instantánea. Al volver al menú desde identificación, la opción de sonido seguirá accesible.
- RF-2: CUANDO el usuario abra la sección, EL SISTEMA mostrará una explicación breve, la duración de la captura y una acción para iniciarla, sin activar el micrófono automáticamente.
- RF-3: CUANDO el usuario solicite grabar y falte autorización para el micrófono, EL SISTEMA solicitará el permiso y solo iniciará la captura si obtiene la autorización mientras la sección siga activa. No exigirá ubicación ni acceso a archivos para identificar por sonido.
- RF-4: SI se deniega el permiso, ENTONCES EL SISTEMA explicará que la captura no puede comenzar y permitirá volver al menú o reintentar; si ya no puede mostrar la solicitud del sistema, ofrecerá acceder a los ajustes de la aplicación sin repetir solicitudes imposibles.

### Grabación y procesamiento

- RF-5: CUANDO comience una captura autorizada, EL SISTEMA grabará durante 10 segundos, mostrará el estado «Grabando» y el tiempo transcurrido respecto del total, y ofrecerá cancelar. La duración se referirá al audio efectivamente capturado, no al tiempo de preparación del micrófono.
- RF-6: MIENTRAS se prepare el reconocimiento, se grabe o se analice una captura, EL SISTEMA impedirá iniciar otra operación simultánea y distinguirá el estado activo de los resultados anteriores, que no aparecerán como resultado de la nueva captura.
- RF-7: CUANDO el usuario cancele una grabación, EL SISTEMA detendrá la captura, liberará el micrófono, descartará el audio parcial y permitirá iniciar otra sin analizar la captura cancelada.
- RF-8: CUANDO se complete una grabación, EL SISTEMA detendrá el micrófono y analizará todo el intervalo capturado, incluido su final, mostrando el estado «Analizando» hasta obtener resultados o un error.
- RF-9: EL SISTEMA obtendrá las sugerencias mediante inferencias reales realizadas localmente; no sustituirá una inferencia fallida por predicciones inventadas, ejemplos precargados o resultados de una captura anterior.
- RF-10: SI no hay micrófono disponible, otro uso impide capturar, se revoca la autorización o la captura se interrumpe antes de completar el audio, ENTONCES EL SISTEMA detendrá la operación, liberará sus recursos, explicará el problema y permitirá reintentar cuando la condición lo permita. No presentará la captura incompleta como exitosa.
- RF-11: SI el reconocimiento no está disponible, los datos necesarios son incompatibles o la inferencia falla, ENTONCES EL SISTEMA mostrará un error distinguible de «sin detecciones», descartará cualquier resultado inválido y mantendrá disponibles la salida y las demás secciones.

### Resultados e información de especies

- RF-12: CUANDO finalice correctamente el análisis, EL SISTEMA mostrará como máximo cinco especies de aves diferentes, ordenadas de mayor a menor puntuación y admitidas por el criterio de confianza definido y validado para esta función. No completará la lista con candidatos que no superen dicho criterio.
- RF-13: SI una especie aparece en varios momentos de la grabación, ENTONCES EL SISTEMA la mostrará una sola vez, con una puntuación representativa de sus detecciones; no sumará las puntuaciones ni contará variantes de una misma especie como especies diferentes.
- RF-14: EL SISTEMA limitará los resultados a aves, aunque el reconocimiento admita otros animales o sonidos. La disponibilidad de un nombre regional, fotografía o mapa local no será una condición para mostrar una especie reconocida.
- RF-15: MIENTRAS se muestre una especie sugerida, EL SISTEMA presentará su nombre científico y su puntuación, priorizando el nombre común regional existente y, si falta, un nombre común en español disponible. Si no existe un nombre común disponible en español, mostrará el científico sin inventar una traducción e indicará la ausencia del nombre común.
- RF-16: EL SISTEMA explicará que la puntuación expresa una coincidencia del modelo y no un porcentaje garantizado de acierto; las puntuaciones de especies distintas no se presentarán como partes que deban sumar 100 %.
- RF-17: SI ninguna especie de ave supera el criterio de confianza, ENTONCES EL SISTEMA mostrará «No se identificaron aves con suficiente confianza» y permitirá repetir la grabación, sin afirmar que no había aves en el entorno.
- RF-18: DONDE una especie tenga información local compatible, EL SISTEMA permitirá abrir su ficha existente y consultar el mapa disponible. Si falta la información o no puede mostrarse, indicará su indisponibilidad sin presentar datos de otra especie ni impedir una nueva identificación.
- RF-19: CUANDO el usuario vuelva desde una ficha o la ampliación de su mapa a los resultados mientras el proceso siga abierto, EL SISTEMA conservará las sugerencias de la misma captura y sus puntuaciones, sin iniciar otra grabación ni registrar una observación.
- RF-20: CUANDO el usuario solicite una nueva identificación tras un resultado, un estado sin detecciones o un error recuperable, EL SISTEMA iniciará una sesión de captura nueva y descartará la anterior.

### Ciclo de vida y privacidad

- RF-21: CUANDO el dispositivo cambie de orientación dentro de la sección, EL SISTEMA conservará el estado de la operación y los resultados mientras el proceso siga abierto, sin duplicar capturas, análisis ni solicitudes de permiso.
- RF-22: CUANDO la aplicación pase a segundo plano durante una grabación, EL SISTEMA detendrá y descartará esa captura y liberará el micrófono; al regresar informará la interrupción y permitirá iniciar otra. No reanudará la grabación automáticamente.
- RF-23: CUANDO la aplicación vuelva del segundo plano después de haber completado la captura y sin cierre del proceso, EL SISTEMA conservará el estado del análisis o sus resultados, sin volver a capturar audio. Un análisis pendiente podrá completarse mientras no se abandone la sección.
- RF-24: CUANDO el usuario abandone la sección o se cierre su proceso, EL SISTEMA descartará la sesión y liberará los recursos de sonido; no publicará resultados tardíos de esa sesión. Una apertura posterior comenzará sin grabación ni resultados recuperados.
- RF-25: EL SISTEMA mantendrá el audio y sus resultados únicamente durante la sesión, sin conservar archivos de grabación, enviarlos fuera del dispositivo, incluirlos en registros de diagnóstico ni incorporarlos al historial de observaciones.

## Requisitos no funcionales

- Disponibilidad: reconocer aves y consultar los recursos locales disponibles funcionará sin conexión desde el primer inicio posterior a una instalación completa, sin cuenta, descarga inicial obligatoria ni API remota.
- Compatibilidad: conservar las versiones de Android admitidas por la aplicación actual. Una dependencia que obligue a restringirlas requerirá una decisión explícita antes de implementar esa restricción.
- Capacidad de respuesta: la preparación y el análisis no bloquearán las acciones de navegación ni la actualización de la pantalla. Se medirán tiempo de preparación, análisis y consumo máximo de memoria en un teléfono físico; el plan concretará el dispositivo y los presupuestos de aceptación antes de implementar.
- Calidad de identificación: evaluar grabaciones identificadas de aves argentinas y controles de silencio, voces, viento y sonidos no aviares. Registrar aciertos, omisiones y falsos positivos con la configuración utilizada. No se presupone que el reconocimiento pueda excluir todas las voces o ruidos, ni se promete precisión universal; el plan definirá el conjunto de evaluación y los umbrales de aceptación antes de implementar.
- Identidad visual e idioma: mantener el español y reutilizar los estilos, temas claro y oscuro y patrones de interacción de las pantallas existentes. El detalle del diseño visual corresponde al plan.
- Accesibilidad: ofrecer controles con nombres comprensibles y áreas táctiles de al menos 48 × 48 dp; estados distinguibles sin depender solo del color; lectura y acciones utilizables con TalkBack, texto al 200 %, pantalla pequeña y ambas orientaciones. La actualización del tiempo no impedirá escuchar las acciones con TalkBack.
- Integridad: las especies mostradas corresponderán a las salidas de la versión seleccionada. Una discrepancia entre los recursos de reconocimiento y sus nombres no podrá producir identificaciones con etiquetas desplazadas.
- Distribución: conservar la procedencia y los avisos de licencia y atribución del material incorporado, comprobando las condiciones de la variante concreta antes de distribuirla.
- Regresión: no alterar el comportamiento de identificación fotográfica, captura instantánea, observaciones, exportación, aprendizaje y preferencias, ni modificar o eliminar observaciones existentes.

## Casos límite ya cubiertos

- Permiso denegado, denegación permanente y revocación durante captura: RF-3, RF-4 y RF-10.
- Micrófono ausente, ocupado o captura parcial: RF-10; error explícito y reintento, sin resultados simulados.
- Toques repetidos, cancelación y resultados de operaciones anteriores: RF-6, RF-7, RF-20 y RF-24.
- Vocalización al final de la grabación y varias especies simultáneas: RF-8 y RF-12; el límite es de especies sugeridas, no de aves presentes.
- Detecciones repetidas de una especie, clases no aviares y nombres faltantes: RF-13 a RF-15.
- Silencio o puntuaciones insuficientes: RF-17; una salida inválida de inferencia se trata como error según RF-11, no como detección.
- Cambio taxonómico, ficha ausente o mapa ilegible: RF-18 y requisito de integridad; nunca asociar información por una coincidencia ambigua de nombres.
- Rotación, segundo plano, retorno desde ficha, salida y cierre del proceso: RF-19 y RF-21 a RF-25.
- Proceso cerrado mientras se solicita un permiso o se analiza: RF-24; no recuperar la grabación ni iniciar el micrófono automáticamente.

## Fuera de alcance

- Escucha continua, detecciones en tiempo real, grabación con pantalla bloqueada y servicios de grabación en segundo plano.
- Detener anticipadamente para analizar un fragmento; la acción durante captura será cancelar y descartar.
- Reproducir, guardar, compartir, importar o exportar audio.
- Crear observaciones sonoras, ampliar el historial o modificar sus datos existentes.
- Identificación conjunta de fotografía y audio o sustitución del clasificador fotográfico.
- Incorporar fotografías, mapas o fichas nuevas para todas las especies reconocibles.
- Obtener ubicación para priorizar o excluir especies por región, fecha o estación en esta primera versión.
- Visualización de onda, espectrograma, selección manual de fragmentos y ajustes avanzados de sensibilidad.
- Entrenar modelos personalizados, incorporar varias alternativas seleccionables por el usuario o actualizar modelos mediante descargas dentro de la app.
- Identificar insectos, mamíferos y anfibios, aunque el motor los admita.

## Criterios de finalización

1. Completar en un teléfono físico el recorrido menú → permiso → captura de 10 segundos → análisis real → sugerencias → ficha disponible → retorno → nueva captura, incluido el acceso al menú con captura instantánea activada.
2. Repetir el recorrido en modo avión desde el primer inicio tras una instalación completa. Verificar que no hay descargas ni transferencias de audio y que la ausencia de ubicación no bloquea el reconocimiento.
3. Verificar el límite de cinco especies, la ausencia de duplicados y clases no aviares, el orden, el umbral acordado y la correspondencia entre las salidas y los nombres; cubrir especies fuera del catálogo local y ausencia de nombre común o ficha.
4. Ejecutar la evaluación acústica acordada en el plan, documentando la configuración y los resultados, incluidos los controles negativos. Comprobar la identificación real de aves argentinas conocidas y los estados sin detecciones y de error sin recurrir a respuestas simuladas.
5. Comprobar permisos, micrófono ocupado o ausente, interrupción, cancelación, toques repetidos, rotación, segundo plano, salida durante análisis y cierre del proceso. Confirmar que no queda el micrófono activo ni se muestran resultados tardíos o de otra captura.
6. Revisar temas, texto ampliado, TalkBack, ambas orientaciones y pantallas pequeñas; todas las acciones y estados son accesibles.
7. Ejecutar las comprobaciones automatizadas adecuadas, compilación, pruebas y Lint; revisar las pantallas en dispositivo y las regresiones de las funciones existentes. Registrar resultados y bloqueos; no declarar finalizada la implementación con comprobaciones obligatorias pendientes.
8. Documentar las medidas de tiempo y memoria y cumplir los presupuestos definidos en el plan, junto con la procedencia y los avisos de licencia del modelo distribuido.

## Dudas abiertas

No quedan preguntas funcionales abiertas. El usuario aprobó explícitamente esta spec y las propuestas de alcance incluidas el 26 de octubre de 2026. El siguiente artefacto es el plan técnico, que requiere su propia aprobación.

La versión y compatibilidad del motor, el tratamiento de puntuaciones, el conjunto de evaluación, el umbral, el dispositivo de referencia y los presupuestos de rendimiento se concretarán en el plan técnico y requerirán su aprobación antes de generar tareas. Si esa evaluación exige recortar la cobertura o cambiar requisitos funcionales, se revisará esta spec antes de implementar.

La aprobación de la spec no autoriza por sí sola a implementar ni aprueba todavía un plan o unas tareas. Se mantendrá la cadencia del repositorio, sin commits ni push.
