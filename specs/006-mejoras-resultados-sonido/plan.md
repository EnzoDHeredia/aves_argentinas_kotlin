# Plan técnico — Spec 006: Mejoras de resultados por sonido

Estado: aprobado explícitamente por el usuario el 28 de octubre de 2026. Fecha: 28 de octubre de 2026.

## Referencias y alcance

- Spec aprobada: [spec.md](spec.md); cubre RF-1 a RF-10 y sus casos límite.
- Mantener Android Views, XML, recursos de tema y los componentes existentes. No agregar dependencias, cambiar el reconocimiento ni realizar commits o push.
- La implementación seguirá la cadencia del proyecto: tareas aprobadas, una tarea por turno con sus pruebas y confirmación antes de continuar.

## Estado actual

`BirdSoundViewModel` acumula especies en orden de aparición y publica mejoras de puntuación. `BirdSoundActivity` presenta la lista mediante `ConcatAdapter`: encabezado, `BirdSoundResultAdapter` y pie. `renderResults` usa `submitList` asíncrono, restaura la posición al rotar y anuncia especies nuevas. El adaptador compara identidad y contenido mediante `DiffUtil`; las tarjetas se reutilizan. El encabezado conserva la explicación y el pie muestra la nota de puntuación y la atribución. Hay pruebas instrumentadas que esperan explícitamente que no se desplace y que la nota sea visible; deben actualizarse.

## Enfoque técnico

- Detectar altas reales comparando la lista previamente publicada con la nueva durante la escucha. Tras aplicarse la lista, desplazar a la última posición de resultados dentro del `ConcatAdapter`, teniendo en cuenta el encabezado y evitando dirigir la vista al pie (RF-1). Una mejora aislada no desplaza (RF-2).
- Tratar la primera presentación después de una rotación como restauración, no como lote nuevo; restaurar la posición guardada. Manejar actualizaciones asíncronas sucesivas de forma que se siga el resultado más reciente sin perder eventos ni aplicar desplazamientos obsoletos.
- Distinguir inserciones y mejoras reales de puntuación de los enlaces ordinarios de vistas. Resaltar las tarjetas correspondientes mediante transición del color de fondo de Material al color original, usando colores compatibles con los temas claro y oscuro. Cancelar y limpiar el efecto al reutilizar una vista, y reiniciarlo ante otra mejora (RF-3, RF-4). Respetar animaciones desactivadas y evitar repetir efectos al restaurar o recorrer resultados existentes.
- Reducir espacios verticales de `item_bird_sound_result.xml` sin eliminar contenido ni reducir la zona de toque del botón de ficha; comprobar nombres largos y texto ampliado (RF-5).
- Vincular la visibilidad de la tarjeta introductoria a la ausencia de detecciones y mantener el título de resultados cuando corresponda (RF-6 a RF-8).
- Convertir la puntuación a porcentaje entero redondeado únicamente al presentarla, incluyendo la descripción accesible. Retirar la nota del pie y su control de visibilidad, conservando la atribución BirdNET (RF-9, RF-10).

## Cambios previstos

1. `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundActivity.kt`: estado de encabezado, seguimiento de altas, restauración, y simplificación del pie (RF-1, RF-2, RF-6 a RF-8, RF-10).
2. `app/src/main/java/com/example/avesargentinas/ui/sound/BirdSoundResultAdapter.kt`: identificación de cambios relevantes, resaltado seguro ante reciclado y porcentaje visible y accesible (RF-3, RF-4, RF-9).
3. `app/src/main/res/layout/item_bird_sound_result.xml`, `item_bird_sound_header.xml`, `item_bird_sound_footer.xml`, y recursos de color y texto correspondientes: compactación, visibilidad y presentación en ambos temas (RF-3 a RF-10).
4. `app/src/test/java/com/example/avesargentinas/ui/sound/BirdSoundResultAdapterTest.kt` y `app/src/androidTest/java/com/example/avesargentinas/ui/sound/BirdSoundActivityTest.kt`: adaptar y ampliar comprobaciones de las partes afectadas. Preservar los cambios existentes ajenos en el archivo de prueba.

## Datos, contratos y compatibilidad

No hay migraciones, permisos ni cambios de persistencia. El puntaje `Float` original y su comparación permanecen intactos; solo se transforma la representación. No cambiar las entradas enviadas a la ficha. Conservar navegación, anuncios existentes y atribución.

## Errores y casos límite

- Si llegan varios resultados juntos, mostrar el último resultado, aunque haya un pie después.
- Si llegan altas y mejoras juntas, desplazar por las altas y resaltar los cambios visibles correspondientes.
- Si una actualización llega mientras se procesa otra lista, la presentación final debe reflejar la lista vigente; no interpretar un reloj o un enlace de vista como nueva detección.
- Al rotar o regresar a una lista conservada, no iniciar un desplazamiento o resaltado falso; una detección posterior sí puede desplazar.
- Al reciclar una tarjeta o recibir mejoras repetidas durante la transición, evitar colores residuales y pérdida del resaltado más reciente.
- Con animaciones desactivadas, la información y anuncios siguen disponibles sin depender del color; el texto debe mantener contraste en ambos temas.
- El redondeo transforma 0,5 en 50 % y 0,7 en 70 %; comprobar también límites y valores intermedios. No alterar la precisión usada por el modelo.

## Estrategia de validación

- Pruebas unitarias del adaptador: conversión y redondeo, identidad y distinción de mejora frente a cambios ajenos a la puntuación (RF-3, RF-9).
- Pruebas instrumentadas de pantalla: alta individual y por lotes, actualización sin alta, alta mientras se revisa una posición anterior, restauración tras rotación y actualizaciones consecutivas (RF-1, RF-2).
- Pruebas instrumentadas de tarjeta: inserción, mejora repetida, reciclado y ausencia de animaciones; verificar texto y descripción accesible del porcentaje (RF-3, RF-4, RF-9).
- Pruebas de estados: explicación visible sin resultados y en nueva sesión, oculta con resultados y tras detener; nota final ausente y atribución presente (RF-6 a RF-8, RF-10).
- Revisar tarjetas con y sin ficha, nombres largos, texto ampliado y temas claro/oscuro; inspección visual en dispositivo o emulador cuando esté disponible (RF-5 y requisitos no funcionales).
- Ejecutar `./gradlew.bat :app:testDebugUnitTest`, `./gradlew.bat :app:assembleDebug` y `./gradlew.bat :app:lintDebug`; ejecutar `./gradlew.bat :app:connectedDebugAndroidTest` si hay dispositivo o emulador. Informar resultados y comprobaciones pendientes.

## Riesgos y decisiones pendientes

- Las pruebas instrumentadas y la revisión visual requieren dispositivo o emulador; su ausencia se informará sin declarar verificaciones exitosas.
- La descripción al pie deja de aclarar que la puntuación no garantiza un acierto, conforme a la solicitud; el porcentaje sigue siendo una presentación de la coincidencia del modelo.
- No hay dudas abiertas.
