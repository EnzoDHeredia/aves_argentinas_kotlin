# Spec 006 — Mejoras de resultados por sonido

Estado: aprobada explícitamente por el usuario el 28 de octubre de 2026. Fecha: 28 de octubre de 2026.

## Contexto y objetivo

La lista de escucha continua crece fuera de la zona visible, por lo que pueden pasar inadvertidas nuevas detecciones o mejoras de puntuación. Las tarjetas y la explicación inicial también ocupan espacio que podría mostrar resultados. Esta mejora busca hacer visibles los cambios y aprovechar mejor el espacio.

## Usuarios

Personas que usan la identificación de aves por sonido, incluidas quienes utilizan lector de pantalla, texto ampliado o tienen desactivadas las animaciones.

## Comportamiento actual y compatibilidad

La [spec 004](../004-escucha-continua-aves/spec.md) define la escucha continua: las especies se agregan en orden de primera aparición y una puntuación solo se actualiza si mejora. Actualmente la lista no sigue automáticamente los resultados, la explicación inicial permanece visible cuando existen detecciones y los puntajes se presentan como decimales con una aclaración al final. Esta propuesta reemplaza expresamente la presentación de la puntuación y la aclaración final previstas en esa spec.

Se conserva el orden, la acumulación sin duplicados, la puntuación máxima, las fichas disponibles, los anuncios de accesibilidad, el control de la escucha y la conservación de resultados al detenerla. Se conserva la posición de lectura al restaurar la pantalla tras una rotación, salvo que llegue posteriormente una especie nueva.

### Enfoque acordado

Se seguirá automáticamente el final al agregar especies nuevas; una mejora de puntuación resaltará su tarjeta sin desplazar ni reordenar la lista. La alternativa de mover al final las especies actualizadas alteraría el orden de primera aparición y generaría desplazamientos adicionales. El usuario aprobó el primer enfoque.

## Historias de usuario

- H1: Como usuario, quiero ver las especies nuevas al aparecer para no perder resultados fuera de la pantalla.
- H2: Como usuario, quiero reconocer las tarjetas nuevas o actualizadas para detectar cambios rápidamente.
- H3: Como usuario, quiero ver más resultados a la vez para aprovechar la pantalla durante la escucha.
- H4: Como usuario, quiero leer los puntajes como porcentajes y quitar la aclaración final para simplificar la pantalla.

## Requisitos funcionales (criterios de aceptación en EARS)

- RF-1: CUANDO se agreguen una o más especies nuevas durante la escucha, EL SISTEMA desplazará la lista para mostrar la última tarjeta de resultados, incluso si el usuario estaba revisando resultados anteriores.
- RF-2: CUANDO mejore la puntuación de una especie existente sin agregarse una nueva, EL SISTEMA mantendrá la posición de la lista y de la especie.
- RF-3: CUANDO aparezca una tarjeta nueva o mejore su puntuación, EL SISTEMA la resaltará temporalmente con un tono suave de la paleta existente y hará que vuelva gradualmente a su color original.
- RF-4: CUANDO una tarjeta vuelva a actualizarse durante el resaltado, EL SISTEMA indicará el nuevo cambio y finalmente recuperará su color original.
- RF-5: MIENTRAS se muestren resultados, EL SISTEMA presentará tarjetas de menor altura que las actuales, conservando nombres, puntuación y acceso a información o su aviso de indisponibilidad.
- RF-6: CUANDO aparezca el primer resultado, EL SISTEMA ocultará la tarjeta introductoria de escucha continua para liberar espacio.
- RF-7: MIENTRAS no haya resultados en la sesión, EL SISTEMA mostrará la explicación inicial. Al iniciar una sesión nueva sin resultados, volverá a mostrarla.
- RF-8: CUANDO se detenga la escucha con resultados, EL SISTEMA conservará la lista y mantendrá oculta la explicación inicial mientras esos resultados permanezcan.
- RF-9: MIENTRAS se muestren resultados, EL SISTEMA expresará cada puntuación como porcentaje entero redondeado: por ejemplo, 0,5 se mostrará como 50 % y 0,7 como 70 %. La lectura accesible comunicará el mismo porcentaje.
- RF-10: EL SISTEMA no mostrará al final de la lista la aclaración sobre las puntuaciones. Mantendrá la atribución del reconocimiento.

## Requisitos no funcionales

- El texto y las acciones deben seguir siendo legibles y accesibles con texto ampliado; las acciones conservan un área de interacción adecuada.
- Con animaciones desactivadas, los resultados y sus cambios siguen siendo accesibles sin depender del efecto de color.
- El resaltado debe respetar los temas disponibles y mantener legible el contenido durante la transición.

## Casos límite ya cubiertos

- Un lote de varias especies termina mostrando la última tarjeta de resultados, no el contenido posterior a la lista.
- Una mejora de puntuación y una especie nueva en el mismo lote desplazan al último resultado y resaltan las tarjetas afectadas.
- Los cambios de reloj, la restauración tras rotar y volver a mostrar tarjetas existentes no se consideran detecciones nuevas.
- Al comenzar una sesión nueva se limpia la lista anterior y reaparece la explicación inicial.
- Las tarjetas que dejan de estar visibles y se reutilizan no deben conservar un color de resaltado incorrecto.
- La conversión a porcentaje solo modifica la presentación: no cambia los valores usados para comparar puntuaciones ni los umbrales de detección.

## Fuera de alcance

- Cambios al modelo, umbrales, nombres de especies, orden de resultados, permisos, almacenamiento o funcionamiento de la captura.
- Nuevas opciones de configuración o indicadores sonoros.

## Criterios de finalización

- Verificar la aparición de una y varias especies, la actualización de puntuación y el desplazamiento solicitado, incluida la posición al rotar.
- Verificar la explicación con lista vacía, primer resultado, detención y sesión nueva.
- Verificar la conversión, el redondeo y la lectura accesible de porcentajes, y que la aclaración final no aparezca.
- Comprobar tarjetas con y sin ficha, accesibilidad, temas y animaciones desactivadas; revisar visualmente en dispositivo o emulador si está disponible.
- Ejecutar las comprobaciones automatizadas apropiadas e informar cualquier validación pendiente.

## Dudas abiertas

Ninguna.
