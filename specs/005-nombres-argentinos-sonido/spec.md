# Spec 005 — Nombres argentinos en detecciones por sonido

Estado: aprobada explícitamente por el usuario el 27 de octubre de 2026.

## Contexto y objetivo

Las detecciones por canto ya priorizan los nombres locales disponibles, pero estos cubren solo una parte de las especies. Se busca mostrar nombres comunes utilizados en Argentina, dando preferencia a los conocidos en la provincia de Buenos Aires cuando exista una referencia verificable.

## Usuarios

Personas que identifican aves por sonido y necesitan reconocerlas por nombres comunes familiares en Argentina.

## Comportamiento actual y compatibilidad

Los resultados incluyen el nombre científico y una puntuación. Si hay un nombre local se prioriza; de lo contrario se utiliza el nombre español disponible y, si falta, el científico. La escucha, los resultados y las fichas disponibles deben seguir funcionando sin conexión.

### Enfoques considerados

- Ampliar los nombres argentinos vinculados a la identidad científica, manteniendo las alternativas existentes cuando no haya una correspondencia confiable. Es el enfoque acordado: permite ampliar cobertura sin alterar el reconocimiento.
- Usar exclusivamente los nombres globales actuales requiere menos trabajo, pero no satisface el objetivo de nombres familiares en Argentina.

## Historias de usuario

- H1: Como persona que escucha aves, quiero ver un nombre común argentino reconocible junto al nombre científico, para interpretar mejor las detecciones.

## Requisitos funcionales (criterios de aceptación en EARS)

- RF-1: CUANDO se muestre una detección de una especie con nombre argentino verificado, EL SISTEMA mostrará ese nombre común y su nombre científico.
- RF-2: CUANDO exista un nombre de uso en Buenos Aires respaldado por una fuente verificable, EL SISTEMA lo priorizará para la especie correspondiente.
- RF-3: SI no se dispone de un nombre verificado de Buenos Aires, ENTONCES EL SISTEMA utilizará un nombre argentino verificado disponible; si tampoco existe, conservará el nombre español disponible o, en su ausencia, el nombre científico y la indicación existente.
- RF-4: EL SISTEMA asociará los nombres a la especie correcta, sin suponer equivalencias entre nombres científicos distintos que no estén verificadas.
- RF-5: EL SISTEMA procurará cubrir las especies registradas en Argentina que el reconocimiento pueda identificar y documentará la cobertura y las especies sin correspondencia verificada.
- RF-6: CUANDO se anuncie una detección o se abra su ficha, EL SISTEMA usará de manera coherente el nombre mostrado en los resultados, sin atribuirle información de otra especie.
- RF-7: MIENTRAS una especie reconocida carezca de nombre argentino o de ficha local, EL SISTEMA seguirá mostrándola si cumple los criterios existentes de detección.

## Requisitos no funcionales

- Los nombres estarán disponibles sin conexión y tendrán procedencia documentada y condiciones de uso compatibles con su distribución.
- La correspondencia entre las salidas del reconocimiento y sus especies se preservará; los cambios de taxonomía y las coincidencias ambiguas se revisarán antes de incorporarlas.
- No se alterarán la identificación fotográfica, los selectores, las observaciones, el aprendizaje ni otros usos existentes del catálogo local.

## Casos límite ya cubiertos

- Especies argentinas ausentes del modelo: no pueden aparecer como detección; se documentará la diferencia de cobertura.
- Nombres científicos sin coincidencia o con equivalencia dudosa: se conserva la alternativa existente y se registra el caso pendiente de revisión.
- Detecciones sin nombre común español: se mantiene la presentación existente del nombre científico.

## Fuera de alcance

- Modificar o entrenar el modelo, garantizar la detección de todas las especies, filtrar por ubicación o cambiar las puntuaciones.
- Añadir fichas o mapas a especies que actualmente no los tengan, modificar registros anteriores o incluir todos los nombres populares alternativos.

## Criterios de finalización

- Comparar una lista de referencia de aves argentinas con las especies reconocibles y registrar coincidencias, ausencias y casos ambiguos.
- Comprobar ejemplos de nombres de Buenos Aires, nombres argentinos y alternativas sin correspondencia, además de la integridad del reconocimiento y la compatibilidad de las demás funciones afectadas.
- Documentar las fuentes, sus condiciones de uso y los límites de cobertura; no presentar como completo un catálogo cuya cobertura no se haya verificado.

## Dudas abiertas

- Ninguna para el alcance funcional; la selección y disponibilidad de fuentes se comprobarán durante la planificación.
