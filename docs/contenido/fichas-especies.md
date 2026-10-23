# Registro editorial — Fichas de especies (Spec 003)

Consulta y revisión: **2026-10-22**, Argentina. Alcance incorporado: **T3–T5, PHOTO-001 a PHOTO-045: 45 especies y 50 etiquetas**. Estado: **cobertura de catálogo y controles técnicos realizados; cierre editorial pendiente por 49 licencias o permisos y 47 autorías/procedencias individuales**. T6 no iniciada.

El [catálogo local](../../app/src/main/assets/species_info.json) contiene 45 especies, sus 45 representativas originales y las cinco complementarias por sexo de T2. Cada texto biológico es una síntesis propia; los enlaces siguientes identifican fuentes, no contenido copiado. Los títulos, entidades, URL y fechas también están empaquetados para consulta sin red. Ninguna URL es una imagen remota cargada por la app.

## Criterios y límites del primer lote (T3)

- Las claves y fotos son exactamente las de PHOTO-001–015 del manifiesto de aprendizaje. No se alteran etiquetas, mapas, modelo ni orden de tarjetas. Las cinco parejas sexuales de T2 pertenecen a lotes posteriores y no se incorporan aquí.
- `group` es una agrupación divulgativa inferida editorialmente a partir de los nombres/familias de las fuentes, sin rango taxonómico. `order` conserva el orden científico del SIB.
- La categorización nacional corresponde al proceso **2015**, publicado por MAyDS/Aves Argentinas en **2017**. `assessmentYear` es 2015; 2017 figura en el título de la referencia. Se cotejaron metodología y Apéndice I con las categorías por especie informadas por SIB. UICN-N en la tabla nacional es una evaluación regional; **no se usa como categoría global**.
- En conservación global, SIB reproduce UICN vía API pero no informa año de evaluación. Se conserva la categoría atribuida y **14 años globales nulos**, incluido el biguá contrastado con Audubon. Para flamenco, BirdLife explicita NT/2018. Una consulta en 2026 no es una evaluación de 2026. No se afirma haber consultado directamente los expedientes UICN de las 15 especies.
- Los movimientos regionales se redactan con su localización; residencia y dispersión pueden coexistir. No se extrapolan calendarios norteamericanos, ni se convierten hipótesis en migraciones demostradas.
- No hay campos biológicos completos nulos en este lote. Las ausencias verificables son los 14 años globales y las atribuciones/procedencias/fechas fotográficas indicadas por imagen. Las incertidumbres de distribución y movimientos están descritas por especie.
- Las imágenes se conservaron sin transformaciones. Autor y licencia son nulos; `photos.sourceIds` queda vacío cuando no se encontró una publicación individual comprobada. Una referencia biológica no acredita derechos sobre la foto local. Véase el [registro fotográfico](fotos-ficha-especies.md#lote-t3--photo-001-a-photo-015).

## Fuentes consultadas

Todas las referencias del catálogo se consultaron el 22 de octubre de 2026. FCV/UNL se consultó tanto en páginas de identificación como en los enlaces específicos de alimentación, hábitat y distribución. Sus copias HTML de evidencia son locales e ignoradas en `app/build/t3/`; no se redistribuyen como contenido de la app.

| ID | Referencia y entidad | Consulta |
| --- | --- | --- |
| `atlas-formosa-2014` | [Contreras y colaboradores (2014). Atlas ornitogeográfico de Formosa. I. No Passeriformes; movimientos: pp. 54, 62–63, 67–68, 70–71, 73–74 y 195–196](https://www.fundacionazara.org.ar/img/libros/atlas-ornitogeografico-de-la-provincia-de-formosa.pdf) — Fundación de Historia Natural Félix de Azara | 2026-10-22 |
| `audubon-bigua` | [Neotropic Cormorant — Nannopterum brasilianum; categoría global UICN sin año de evaluación indicado](https://www.audubon.org/field-guide/bird/neotropic-cormorant) — National Audubon Society | 2026-10-22 |
| `birdlife-flamenco-2018` | [Laguna Guayatayoc — tabla de especies: Phoenicopterus chilensis, NT, evaluación global 2018](https://datazone.birdlife.org/site/factsheet/19400-laguna-guayatayoc) — BirdLife International | 2026-10-22 |
| `adw-chaja` | [Jessica Nolle. Chauna torquata — residencia permanente en Sudamérica](https://animaldiversity.org/accounts/Chauna_torquata/) — Animal Diversity Web / University of Michigan | 2026-10-22 |
| `fw-chiflon` | [Chiflón (Syrigma sibilatrix) — distribución y desplazamientos asociados a recursos](https://fundacionfw.org.ar/fauna/chiflon-syrigma-sibilatrix/) — Fundación Federico Wildermuth | 2026-10-22 |
| `cornell-spoonbill` | [Roseate Spoonbill — Life History; nombre inglés y ecología](https://www.allaboutbirds.org/guide/Roseate_Spoonbill/lifehistory) — Cornell Lab of Ornithology | 2026-10-22 |
| `sib-001` | [SIB — Biguá (phalacrocorax brasilianus); categoría nacional MADS/AA 2017](https://sib.gob.ar/especies/phalacrocorax-brasilianus) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-001` | [Biblioteca de aves — Biguá: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/phalacrocoracidae/bigua/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-001-diet` | [Biblioteca de aves — Biguá: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6238) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-001-range` | [Biblioteca de aves — Biguá: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6257) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-001-habitat` | [Biblioteca de aves — Biguá: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6242) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-002` | [SIB — Carau (aramus guarauna); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/aramus-guarauna) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-002` | [Biblioteca de aves — Carau: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/aramidae/carau/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-002-diet` | [Biblioteca de aves — Carau: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1403) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-002-range` | [Biblioteca de aves — Carau: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1419) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-002-habitat` | [Biblioteca de aves — Carau: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1411) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-003` | [SIB — Chajá (chauna torquata); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/Chauna-torquata) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-003` | [Biblioteca de aves — Chajá: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/anhimidae/chaja/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-003-diet` | [Biblioteca de aves — Chajá: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1271) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-003-range` | [Biblioteca de aves — Chajá: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1287) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-003-habitat` | [Biblioteca de aves — Chajá: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1275) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-004` | [SIB — Chiflón (syrigma sibilatrix); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/syrigma-sibilatrix) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-004` | [Biblioteca de aves — Chiflón: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/ardeidae/chiflon/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-004-diet` | [Biblioteca de aves — Chiflón: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1996) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-004-range` | [Biblioteca de aves — Chiflón: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2007) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-004-habitat` | [Biblioteca de aves — Chiflón: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1998) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-005` | [SIB — Cigüeña americana (ciconia maguari); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/ciconia-maguari) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-005` | [Biblioteca de aves — Cigüeña americana: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/ciconiidae/ciguena-americana/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-005-diet` | [Biblioteca de aves — Cigüeña americana: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2784) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-005-range` | [Biblioteca de aves — Cigüeña americana: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2795) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-005-habitat` | [Biblioteca de aves — Cigüeña americana: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2786) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-006` | [SIB — Cisne cuello negro (cygnus melancoryphus); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/cygnus-melancoryphus) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-006` | [Biblioteca de aves — Cisne cuello negro: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/anatidae/cisne-cuello-negro/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-006-diet` | [Biblioteca de aves — Cisne cuello negro: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1441) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-006-range` | [Biblioteca de aves — Cisne cuello negro: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1451) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-006-habitat` | [Biblioteca de aves — Cisne cuello negro: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1443) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-007` | [SIB — Coscoroba (coscoroba coscoroba); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/coscoroba-coscoroba) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-007` | [Biblioteca de aves — Coscoroba: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/anatidae/coscoroba/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-007-diet` | [Biblioteca de aves — Coscoroba: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1478) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-007-range` | [Biblioteca de aves — Coscoroba: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1490) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-007-habitat` | [Biblioteca de aves — Coscoroba: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1480) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-008` | [SIB — Cuervillo cara pelada (phimosus infuscatus); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/phimosus-infuscatus?area%5B%5D=APN-CH&tab=fuentes) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-008` | [Biblioteca de aves — Cuervillo cara pelada: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/threskiornithidae/cuervillo-cara-pelada/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-008-diet` | [Biblioteca de aves — Cuervillo cara pelada: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8479) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-008-range` | [Biblioteca de aves — Cuervillo cara pelada: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8490) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-008-habitat` | [Biblioteca de aves — Cuervillo cara pelada: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8481) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-009` | [SIB — Cuervillo de cañada (plegadis chihi); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/plegadis-chihi) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-009` | [Biblioteca de aves — Cuervillo de cañada: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/threskiornithidae/cuervillo-de-canada/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-009-diet` | [Biblioteca de aves — Cuervillo de cañada: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8501) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-009-range` | [Biblioteca de aves — Cuervillo de cañada: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8514) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-009-habitat` | [Biblioteca de aves — Cuervillo de cañada: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8504) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-010` | [SIB — Espátula rosada (platalea ajaja); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/platalea-ajaja) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-010` | [Biblioteca de aves — Espátula rosada: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/threskiornithidae/espatula-rosada/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-010-diet` | [Biblioteca de aves — Espátula rosada: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8525) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-010-range` | [Biblioteca de aves — Espátula rosada: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8536) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-010-habitat` | [Biblioteca de aves — Espátula rosada: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=8527) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-011` | [SIB — Flamenco austral (phoenicopterus chilensis); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/phoenicopterus-chilensis?tab=info-general) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-011` | [Biblioteca de aves — Flamenco austral: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/phoenicopteridae/flamenco/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-011-diet` | [Biblioteca de aves — Flamenco austral: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6379) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-011-range` | [Biblioteca de aves — Flamenco austral: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6391) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-011-habitat` | [Biblioteca de aves — Flamenco austral: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6382) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-012` | [SIB — Gallareta chica (fulica leucoptera); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/fulica-leucoptera) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-012` | [Biblioteca de aves — Gallareta chica: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/rallidae/gallareta-chica/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-012-diet` | [Biblioteca de aves — Gallareta chica: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7410) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-012-range` | [Biblioteca de aves — Gallareta chica: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7421) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-012-habitat` | [Biblioteca de aves — Gallareta chica: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7412) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-013` | [SIB — Gallareta escudete rojo (fulica rufifrons); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/fulica-rufifrons) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-013` | [Biblioteca de aves — Gallareta escudete rojo: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/rallidae/gallareta-escudete-rojo/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-013-diet` | [Biblioteca de aves — Gallareta escudete rojo: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7392) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-013-range` | [Biblioteca de aves — Gallareta escudete rojo: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7402) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-013-habitat` | [Biblioteca de aves — Gallareta escudete rojo: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7394) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-014` | [SIB — Gallareta ligas rojas (fulica armillata); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/fulica-armillata) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-014` | [Biblioteca de aves — Gallareta ligas rojas: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/rallidae/gallareta-ligas-rojas/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-014-diet` | [Biblioteca de aves — Gallareta ligas rojas: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7372) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-014-range` | [Biblioteca de aves — Gallareta ligas rojas: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7382) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-014-habitat` | [Biblioteca de aves — Gallareta ligas rojas: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7374) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-015` | [SIB — Garcita blanca (egretta thula); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/egretta-thula) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-015` | [Biblioteca de aves — Garcita blanca: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/ardeidae/garcita-blanca/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-015-diet` | [Biblioteca de aves — Garcita blanca: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2142) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-015-range` | [Biblioteca de aves — Garcita blanca: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2152) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-015-habitat` | [Biblioteca de aves — Garcita blanca: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2144) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `mads-aa-eval-2015` | [Categorización de las aves de la Argentina (publicación 2017): evaluación 2015; metodología pp. 7–9 y Apéndice I, pp. 75–79](https://www.avesargentinas.org.ar/sites/default/files/Categorizacion-de-aves-de-la-Argentina.pdf) — Ministerio de Ambiente y Desarrollo Sustentable de la Nación y Aves Argentinas | 2026-10-22 |
| `temaiken-flamenco` | [Flamenco austral — alimentación por filtración y distribución](https://www.temaiken.org.ar/ficha-tecnica-flamenco-austral-67) — Fundación Temaikèn | 2026-10-22 |

## Revisión individual

### PHOTO-001 — Biguá (`phalacrocorax_brasilianus`)

La clave heredada phalacrocorax_brasilianus se conserva. Para presentación se adopta Nannopterum brasilianum, usado por Audubon. FCV/UNL muestra Nannopterum brasilianus y SIB mantiene Phalacrocorax brasilianus; no se cambia la clave, el clasificador ni el mapa. Suliformes procede de SIB. La residencia citada se limita a Patagonia.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-001` |
| Nombre científico de presentación | `audubon-bigua` |
| Nombre inglés | `sib-001` |
| Familia | `sib-001` |
| Grupo divulgativo | `sib-001` |
| Orden | `sib-001` |
| Regiones argentinas | `unl-001-range` |
| Morfología | `unl-001`, `sib-001` |
| Hábitat | `unl-001-habitat` |
| Alimentación | `unl-001-diet` |
| Movimientos | `sib-001` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `audubon-bigua` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-001`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-001.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-002 — Carau (`aramus_guarauna`)

La distribución de FCV/UNL llega a Río Negro, más al sur que el resumen continental de eBird. Se usa la referencia argentina detallada. La residencia se presenta con alcance provincial explícito.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-002` |
| Nombre científico de presentación | `sib-002` |
| Nombre inglés | `sib-002` |
| Familia | `sib-002` |
| Grupo divulgativo | `sib-002` |
| Orden | `sib-002` |
| Regiones argentinas | `unl-002-range` |
| Morfología | `unl-002` |
| Hábitat | `unl-002-habitat` |
| Alimentación | `unl-002-diet` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-002` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-002`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-002.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-003 — Chajá (`chauna_torquata`)

Se conserva la identidad Chauna torquata. FCV/UNL extiende la distribución a Río Negro; Temaikèn resume hasta el sur bonaerense. Se sigue FCV/UNL para el alcance regional, sin afirmar abundancia uniforme. La dieta vegetal coincide entre FCV/UNL, Temaikèn y ADW; no se adopta el rótulo insectívoro/carnívoro del manual de USAL hallado en búsqueda, incompatible con esas fuentes.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-003` |
| Nombre científico de presentación | `sib-003` |
| Nombre inglés | `sib-003` |
| Familia | `sib-003` |
| Grupo divulgativo | `sib-003` |
| Orden | `sib-003` |
| Regiones argentinas | `unl-003-range` |
| Morfología | `unl-003`, `sib-003` |
| Hábitat | `unl-003-habitat`, `adw-chaja` |
| Alimentación | `unl-003-diet` |
| Movimientos | `adw-chaja` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-003` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-003`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-003.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-004 — Chiflón (`syrigma_sibilatrix`)

Pelecaniformes es el orden y Garzas el grupo divulgativo. La posibilidad de desplazarse por disponibilidad de recursos no prueba una ruta o estación migratoria fija; no se inventa ese calendario.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-004` |
| Nombre científico de presentación | `sib-004` |
| Nombre inglés | `sib-004` |
| Familia | `sib-004` |
| Grupo divulgativo | `sib-004` |
| Orden | `sib-004` |
| Regiones argentinas | `unl-004-range` |
| Morfología | `unl-004` |
| Hábitat | `unl-004-habitat` |
| Alimentación | `unl-004-diet` |
| Movimientos | `fw-chiflon` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-004` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-004`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-004.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-005 — Cigüeña americana (`ciconia_maguari`)

Cigüeña americana aquí identifica Ciconia maguari. El mismo nombre se aplica a Mycteria americana en guías norteamericanas (p. ej. Audubon); esos textos no se usan para esta ficha. FCV/UNL indica hasta Chubut y el Atlas matiza que la llegada patagónica es ocasional. Se conserva ese matiz. Las diferencias de longitud entre SIB y FCV/UNL no se convierten en una medida propia: se omite la cifra.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-005` |
| Nombre científico de presentación | `sib-005` |
| Nombre inglés | `sib-005` |
| Familia | `sib-005` |
| Grupo divulgativo | `sib-005` |
| Orden | `sib-005` |
| Regiones argentinas | `unl-005-range`, `atlas-formosa-2014` |
| Morfología | `unl-005` |
| Hábitat | `unl-005-habitat` |
| Alimentación | `unl-005-diet` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-005` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-005`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-005.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-006 — Cisne cuello negro (`cygnus_melancoryphus`)

FCV/UNL y SIB coinciden en el contraste de cabeza/cuello y cuerpo. Las longitudes publicadas difieren (SIB 80 cm frente a 115–124 cm en FCV/UNL); se omite la medida. La migración hacia el noreste se respalda en el enlace específico de distribución de FCV/UNL, sin afirmar que todos los individuos migren.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-006` |
| Nombre científico de presentación | `sib-006` |
| Nombre inglés | `sib-006` |
| Familia | `sib-006` |
| Grupo divulgativo | `sib-006` |
| Orden | `sib-006` |
| Regiones argentinas | `unl-006-range` |
| Morfología | `unl-006` |
| Hábitat | `unl-006-habitat` |
| Alimentación | `unl-006-diet`, `sib-006` |
| Movimientos | `unl-006-range` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-006` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-006`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-006.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-007 — Coscoroba (`coscoroba_coscoroba`)

Se mantiene Coscoroba como género propio; Cisnes es un grupo de consulta, no el género Cygnus. Se omiten longitudes discrepantes entre SIB y FCV/UNL. El desplazamiento invernal procede de FCV/UNL y la variación entre años del texto regional reproducido por SIB.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-007` |
| Nombre científico de presentación | `sib-007` |
| Nombre inglés | `sib-007` |
| Familia | `sib-007` |
| Grupo divulgativo | `sib-007` |
| Orden | `sib-007` |
| Regiones argentinas | `unl-007-range` |
| Morfología | `unl-007` |
| Hábitat | `unl-007-habitat` |
| Alimentación | `unl-007-diet`, `sib-007` |
| Movimientos | `unl-007-range`, `sib-007` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-007` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-007`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-007.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-008 — Cuervillo cara pelada (`phimosus_infuscatus`)

No se confunde con Plegadis chihi: la cara desnuda rojiza y el pico claro son rasgos útiles. Se preserva la incertidumbre del Atlas sobre los movimientos.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-008` |
| Nombre científico de presentación | `sib-008` |
| Nombre inglés | `sib-008` |
| Familia | `sib-008` |
| Grupo divulgativo | `sib-008` |
| Orden | `sib-008` |
| Regiones argentinas | `unl-008-range` |
| Morfología | `unl-008` |
| Hábitat | `unl-008-habitat` |
| Alimentación | `unl-008-diet` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-008` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-008`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-008.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-009 — Cuervillo de cañada (`plegadis_chihi`)

Se conserva Plegadis chihi; los cambios de plumaje por edad/estación no se interpretan como sexo. Para movimientos se usa el Atlas regional, sin trasladar calendarios norteamericanos. La firma de la imagen no valida identidad, autoría legal ni licencia.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-009` |
| Nombre científico de presentación | `sib-009` |
| Nombre inglés | `sib-009` |
| Familia | `sib-009` |
| Grupo divulgativo | `sib-009` |
| Orden | `sib-009` |
| Regiones argentinas | `unl-009-range` |
| Morfología | `unl-009` |
| Hábitat | `unl-009-habitat` |
| Alimentación | `unl-009-diet` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-009` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-009`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-009.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-010 — Espátula rosada (`platalea_ajaja`)

Se conserva Platalea ajaja, que SIB relaciona con Ajaia ajaja. El Atlas emplea Ajaia: se usa solo para ecología y movimientos. El inglés correcto Roseate Spoonbill se contrasta con Cornell; no se reproduce la errata Rosate de SIB.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-010` |
| Nombre científico de presentación | `sib-010` |
| Nombre inglés | `cornell-spoonbill` |
| Familia | `sib-010` |
| Grupo divulgativo | `sib-010` |
| Orden | `sib-010` |
| Regiones argentinas | `unl-010-range` |
| Morfología | `unl-010` |
| Hábitat | `unl-010-habitat` |
| Alimentación | `unl-010-diet`, `unl-010` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-010` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-010`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-010.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-011 — Flamenco austral (`phoenicopterus_chilensis`)

Se separan NT global (BirdLife, evaluación 2018) y VU nacional (evaluación 2015, informe publicado en 2017). El NA histórico del Atlas de 2014 no sustituye la evaluación posterior. FCV/UNL exceptúa Misiones; el informe nacional dice todas las provincias. Se expresa distribución amplia sin imponer una exclusión provincial no concordante. El pico claro/negro y las patas grises con articulaciones rojas se contrastaron con FCV/UNL y Temaikèn.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-011` |
| Nombre científico de presentación | `sib-011` |
| Nombre inglés | `sib-011` |
| Familia | `sib-011` |
| Grupo divulgativo | `sib-011` |
| Orden | `sib-011` |
| Regiones argentinas | `unl-011-range`, `mads-aa-eval-2015` |
| Morfología | `unl-011` |
| Hábitat | `unl-011-habitat` |
| Alimentación | `unl-011-diet`, `temaiken-flamenco` |
| Movimientos | `atlas-formosa-2014`, `mads-aa-eval-2015` |
| Conservación global: Casi amenazada (NT); evaluación 2018 | `birdlife-flamenco-2018` |
| Conservación nacional: Vulnerable (VU); evaluación 2015 | `sib-011`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-011.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-012 — Gallareta chica (`fulica_leucoptera`)

El escudete pequeño sin banda roja y el borde claro del ala ayudan a separarla de F. armillata. FCV/UNL exceptúa Misiones y SIB exceptúa el altoandino del noroeste; no se suman esas exclusiones como un mapa comprobado ni se afirma presencia continua. Se usa un resumen regional prudente; falta resolver el límite provincial fino. La estacionalidad del SIB se limita a su contexto patagónico.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-012` |
| Nombre científico de presentación | `sib-012` |
| Nombre inglés | `sib-012` |
| Familia | `sib-012` |
| Grupo divulgativo | `sib-012` |
| Orden | `sib-012` |
| Regiones argentinas | `unl-012-range`, `sib-012` |
| Morfología | `unl-012`, `sib-012` |
| Hábitat | `unl-012-habitat` |
| Alimentación | `unl-012-diet` |
| Movimientos | `sib-012` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-012` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-012`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-012.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-013 — Gallareta escudete rojo (`fulica_rufifrons`)

Se distingue de F. armillata por el escudete rojo estrecho; no basta con observar rojo junto al pico. Se mantiene la distribución argentina detallada de FCV/UNL y el contexto regional del texto estacional reproducido por SIB.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-013` |
| Nombre científico de presentación | `sib-013` |
| Nombre inglés | `sib-013` |
| Familia | `sib-013` |
| Grupo divulgativo | `sib-013` |
| Orden | `sib-013` |
| Regiones argentinas | `unl-013-range` |
| Morfología | `unl-013`, `sib-013` |
| Hábitat | `unl-013-habitat` |
| Alimentación | `unl-013-diet` |
| Movimientos | `sib-013` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-013` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-013`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-013.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-014 — Gallareta ligas rojas (`fulica_armillata`)

Escudete amarillo con banda roja en su unión al pico; la liga roja puede no verse al nadar. La dieta vegetal de SIB y los insectos/cangrejos de FCV/UNL son complementarios. La residencia permanente indicada por SIB se limita al noroeste patagónico.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-014` |
| Nombre científico de presentación | `sib-014` |
| Nombre inglés | `sib-014` |
| Familia | `sib-014` |
| Grupo divulgativo | `sib-014` |
| Orden | `sib-014` |
| Regiones argentinas | `unl-014-range` |
| Morfología | `unl-014`, `sib-014` |
| Hábitat | `unl-014-habitat`, `sib-014` |
| Alimentación | `unl-014-diet` |
| Movimientos | `sib-014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-014` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-014`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-014.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

### PHOTO-015 — Garcita blanca (`egretta_thula`)

Se conserva Egretta thula. Se describe el pico como normalmente negro: FCV/UNL menciona variación de color, sin convertirla en un carácter absoluto. Se conserva el alcance regional de los movimientos del Atlas.

| Campo | Respaldo por ID (referencias anteriores) |
| --- | --- |
| Nombre español | `sib-015` |
| Nombre científico de presentación | `sib-015` |
| Nombre inglés | `sib-015` |
| Familia | `sib-015` |
| Grupo divulgativo | `sib-015` |
| Orden | `sib-015` |
| Regiones argentinas | `unl-015-range` |
| Morfología | `unl-015` |
| Hábitat | `unl-015-habitat` |
| Alimentación | `unl-015-diet` |
| Movimientos | `atlas-formosa-2014` |
| Conservación global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-015` |
| Conservación nacional: No amenazada (NA); evaluación 2015 | `sib-015`, `mads-aa-eval-2015` |

Foto conservada: `learning/photos/photo-015.webp`. Sexo no asignado; no se deduce por plumaje, edad ni carpeta. Publicación individual, autoría y permiso no verificados; fecha de toma no verificada. Ausencias explícitas en el registro fotográfico.

## Comprobaciones de T3

- Control instrumentado del catálogo parcial: 15 claves esperadas, asociación exacta con PHOTO-001–015, campos con referencias resolubles, conservación por ámbito y año nacional efectivo, una representativa por especie, decodificación Android y ausencia de duplicados binarios. El control no certifica derechos ni veracidad biológica; esa revisión es editorial.
- Resultado final y comandos: véase el [registro de T3](../../specs/003-ficha-informativa-especies/tasks.md#registro-de-ejecución-de-t3--22-de-octubre-de-2026).
- Pendiente editorial: resolver titular/publicación individual y licencia o permiso de las 15 fotos conservadas. El acuerdo de continuidad separa este pendiente del desarrollo técnico. No se reemplazaron imágenes ni se contactó a terceros. T3 no se declara cerrada editorialmente. Al cerrar T3 se detuvo el trabajo antes de T4; la autorización posterior y sus resultados están registrados en el apartado siguiente.

## Lote T4 — PHOTO-016 a PHOTO-030

Consulta y evaluación editorial: **2026-10-22 (Argentina)**. Se añaden 15 especies, conservando sin cambios los 15 objetos editoriales y las 83 referencias anteriores. Total: **30 especies, 168 referencias y 30 representativas existentes**. No se exige la cobertura final de 45 especies/50 etiquetas ni se incorporan las parejas de T2, previstas para T5.

### Método, consultas y límites de T4

- Se leyeron las 15 páginas de identificación de FCV/UNL y sus 45 enlaces específicos de alimentación, distribución y hábitat. Para macá grande, la ruta corta dio 404: se siguió el enlace de categoría del menú y se leyeron sus tres páginas temáticas. Copias HTML y resúmenes de consulta en `app/build/t4/research.json` y archivos vecinos, locales e ignorados.
- Se consultaron las 15 fichas SIB mediante el lector web. Ardea alba y Larus dominicanus devolvieron 502 al abrirse y error TLS al intentar lectura directa; se conservaron esos fallos y se leyeron resultados indexados específicos para identidad, categoría y ciclo regional. Se contrastó LC global con Audubon para ambas. No se afirma lectura directa exitosa de esas dos páginas ni de expedientes UICN.
- Las 15 categorías nacionales NA se cotejaron individualmente con el Apéndice I del informe MAyDS/Aves Argentinas: páginas impresas 74–79 y 86 (páginas PDF 75–80 y 87). Su columna UICN-N es regional, no global. El proceso de evaluación fue **2015**, aunque el informe se publicó en **2017**; la metodología se releyó en T4. Las 15 categorías globales del lote son LC según SIB/Audubon, con **año nulo** porque esas fuentes no lo explicitan. Son 29 años globales ausentes entre las 30 fichas acumuladas; la evaluación NT/2018 del flamenco se conserva.
- Se consultaron las secciones pertinentes del Atlas de Formosa (2014), páginas impresas 49–50, 177, 205–206 y 236–237, mediante el lector PDF web. Los intentos de obtener copia local devolvieron 403. Se registra residencia o desplazamiento **regional**, sin convertirlo en un calendario para toda Argentina ni usar sus apreciaciones de conservación local como evaluaciones globales.
- Para Hirundo rustica se leyó el resumen del trabajo de Winkler y colaboradores (2017), no su texto completo. Se contrastó la expansión de cría con el resumen de Bianchini (2023) y el póster de León y colaboradores (RAO 2026, datos de 2025). No se reduce la población argentina a visitantes de Norteamérica. No se crea una sección de cría.
- La primera investigación dejó sin resolver los movimientos de Progne chalybea; la ampliación encontró una entrevista institucional de UNL a Rodrigo Lorenzón (INALI), de 2014, que los describe para Santa Fe e inmediaciones. Se incorpora ese alcance local y se evita una ausencia injustificada. Todos los campos biológicos del lote tienen síntesis propias respaldadas; los quince años globales permanecen nulos por ausencia del dato en las referencias consultadas.
- `group` es una agrupación divulgativa editorial basada en las familias; no es un nuevo rango taxonómico. `order` se mantiene separado. Las claves, etiquetas, fotos, modelo, mapas y manifiesto de juego permanecen intactos.
- Los metadatos fotográficos se investigaron por separado: una página biológica no es procedencia de la foto. PHOTO-026 tiene autor y publicación individual contrastados; **su licencia sigue nula**. Otras 14 autorías/procedencias individuales y los 15 permisos del lote siguen pendientes. Véase el [registro fotográfico de T4](fotos-ficha-especies.md#lote-t4--photo-016-a-photo-030).

### Referencias añadidas en T4

Todas consultadas el 22 de octubre de 2026. Se incluyen completas en el JSON para su consulta offline; los enlaces son metadatos, no solicitudes de red al abrir la ficha.

| ID | Referencia y entidad | Consulta |
| --- | --- | --- |
| `mads-aa-t4-2015` | [Categorización de las aves de la Argentina (2017): proceso 2015, metodología pp. 7–9 y Apéndice I pp. 74–79 y 86](https://www.avesargentinas.org.ar/sites/default/files/Categorizacion-de-aves-de-la-Argentina.pdf) — Ministerio de Ambiente y Desarrollo Sustentable de la Nación y Aves Argentinas | 2026-10-22 |
| `atlas-t4` | [Contreras y colaboradores (2014). Atlas ornitogeográfico de Formosa, I. No Passeriformes: pp. 49–50, 177, 205–206 y 236–237; movimientos regionales](https://www.fundacionazara.org.ar/img/libros/atlas-ornitogeografico-de-la-provincia-de-formosa.pdf) — Fundación de Historia Natural Félix de Azara | 2026-10-22 |
| `winkler-2017` | [Winkler y colaboradores (2017). Long-Distance Range Expansion and Rapid Adjustment of Migration in a Newly Established Population of Barn Swallows Breeding in Argentina. Current Biology 27:1080–1084. DOI 10.1016/j.cub.2017.03.006; resumen](https://pubmed.ncbi.nlm.nih.gov/28318974/) — Cornell University / IBIGEO-CONICET y colaboradores; registro PubMed | 2026-10-22 |
| `uci-jacana` | [Avise’s Birds of the World — Wattled Jacana (Jacana jacana): nombre inglés y rasgos](https://avise-birds.bio.uci.edu/charadriiformes/jacanidae/jacana_jacana/index.html) — John C. Avise, University of California, Irvine | 2026-10-22 |
| `naturfoto-26215` | [Ostnák jihoamerický (Jacana jacana), foto ID 26215, firma Luboš Mráz; Pantanal, Brasil, agosto 2017](https://www.naturfoto.cz/ostnak-jihoamericky-fotografie-26215.html) — NaturFoto.cz / Luboš Mráz | 2026-10-22 |
| `naturfoto-terms` | [Pravidla a podmínky užití fotografií — condiciones de uso; licencia de publicación requerida, no adquirida](https://www.naturfoto.cz/obchodni-informace.html) — NaturFoto.cz | 2026-10-22 |
| `audubon-egret` | [Great Egret — Ardea alba; categoría global UICN, año de evaluación no indicado](https://www.audubon.org/field-guide/bird/great-egret) — National Audubon Society | 2026-10-22 |
| `audubon-kelp` | [Kelp Gull — Larus dominicanus; categoría global UICN, año de evaluación no indicado](https://www.audubon.org/field-guide/bird/kelp-gull) — National Audubon Society | 2026-10-22 |
| `unl-golondrinas-2014` | [¿Volverán antes las golondrinas? (26/08/2014). Entrevista a Rodrigo Lorenzón, INALI: ciclo de Progne chalybea en Santa Fe e inmediaciones](https://www.unl.edu.ar/noticias/news/view/%C2%BFvolver%C3%A1n_antes_las_golondrinas) — Universidad Nacional del Litoral / INALI | 2026-10-22 |
| `rao-tijerita-2026` | [León y colaboradores (2026). Ampliando las fronteras: nuevos datos sobre la nidificación de la Golondrina Tijerita en su límite norte de distribución (Santa Fe, Argentina); resumen de póster XXI RAO, relevamientos 2025](https://rao.avesargentinas.org.ar/sobre/xxi-rao/ampliando-las-fronteras-nuevos-datos-sobre-la-nidificacion-de-la-golondrina-tijerita-hirundo-rustica-en-su-limite-norte-de-distribucion-santa-fe-argentina/) — INALI-CONICET-UNL / FHUC-UNL / FCYT-UADER; Aves Argentinas | 2026-10-22 |
| `sib-016` | [SIB — Garza blanca (Ardea alba); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/ardea-alba) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-016` | [Biblioteca de aves — Garza blanca: descripción e identificación](https://www.fcv.unl.edu.ar/aves/garza-blanca/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-016-diet` | [Biblioteca de aves — Garza blanca: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2215) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-016-range` | [Biblioteca de aves — Garza blanca: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2228) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-016-habitat` | [Biblioteca de aves — Garza blanca: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2220) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-017` | [SIB — Garza bruja (Nycticorax nycticorax); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/nycticorax-nycticorax) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-017` | [Biblioteca de aves — Garza bruja: descripción e identificación](https://www.fcv.unl.edu.ar/aves/garza-bruja/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-017-diet` | [Biblioteca de aves — Garza bruja: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2254) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-017-range` | [Biblioteca de aves — Garza bruja: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2265) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-017-habitat` | [Biblioteca de aves — Garza bruja: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2257) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-018` | [SIB — Garza mora (Ardea cocoi); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/ardea-cocoi) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-018` | [Biblioteca de aves — Garza mora: descripción e identificación](https://www.fcv.unl.edu.ar/aves/garza-mora/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-018-diet` | [Biblioteca de aves — Garza mora: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2279) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-018-range` | [Biblioteca de aves — Garza mora: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2290) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-018-habitat` | [Biblioteca de aves — Garza mora: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2292) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-019` | [SIB — Gaviota capucho café (Chroicocephalus maculipennis); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/chroicocephalus-maculipennis) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-019` | [Biblioteca de aves — Gaviota capucho café: descripción e identificación](https://www.fcv.unl.edu.ar/aves/gaviota-capucho-cafe/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-019-diet` | [Biblioteca de aves — Gaviota capucho café: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5094) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-019-range` | [Biblioteca de aves — Gaviota capucho café: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5108) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-019-habitat` | [Biblioteca de aves — Gaviota capucho café: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5098) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-020` | [SIB — Gaviota capucho gris (Chroicocephalus cirrocephalus); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/chroicocephalus-cirrocephalus) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-020` | [Biblioteca de aves — Gaviota capucho gris: descripción e identificación](https://www.fcv.unl.edu.ar/aves/gaviota-capucho-gris/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-020-diet` | [Biblioteca de aves — Gaviota capucho gris: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5121) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-020-range` | [Biblioteca de aves — Gaviota capucho gris: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5133) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-020-habitat` | [Biblioteca de aves — Gaviota capucho gris: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5125) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-021` | [SIB — Gaviota cocinera (Larus dominicanus); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/larus-dominicanus) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-021` | [Biblioteca de aves — Gaviota cocinera: descripción e identificación](https://www.fcv.unl.edu.ar/aves/gaviota-cocinera/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-021-diet` | [Biblioteca de aves — Gaviota cocinera: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5141) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-021-range` | [Biblioteca de aves — Gaviota cocinera: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5152) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-021-habitat` | [Biblioteca de aves — Gaviota cocinera: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5143) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-022` | [SIB — Golondrina doméstica (Progne chalybea); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/progne-chalybea) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-022` | [Biblioteca de aves — Golondrina doméstica: descripción e identificación](https://www.fcv.unl.edu.ar/aves/golondrina-domestica/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-022-diet` | [Biblioteca de aves — Golondrina doméstica: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4300) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-022-range` | [Biblioteca de aves — Golondrina doméstica: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4312) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-022-habitat` | [Biblioteca de aves — Golondrina doméstica: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4302) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-023` | [SIB — Golondrina tijerita (Hirundo rustica); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/hirundo-rustica) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-023` | [Biblioteca de aves — Golondrina tijerita: descripción e identificación](https://www.fcv.unl.edu.ar/aves/golondrina-tijerita/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-023-diet` | [Biblioteca de aves — Golondrina tijerita: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4427) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-023-range` | [Biblioteca de aves — Golondrina tijerita: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4437) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-023-habitat` | [Biblioteca de aves — Golondrina tijerita: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=4429) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-024` | [SIB — Hocó colorado (Tigrisoma lineatum); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/tigrisoma-lineatum) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-024` | [Biblioteca de aves — Hocó colorado: descripción e identificación](https://www.fcv.unl.edu.ar/aves/hoco-colorado/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-024-diet` | [Biblioteca de aves — Hocó colorado: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2310) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-024-range` | [Biblioteca de aves — Hocó colorado: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2320) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-024-habitat` | [Biblioteca de aves — Hocó colorado: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2312) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-025` | [SIB — Ipacaá (Aramides ypecaha); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/aramides-ypecaha) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-025` | [Biblioteca de aves — Ipacaá: descripción e identificación](https://www.fcv.unl.edu.ar/aves/ipacaa/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-025-diet` | [Biblioteca de aves — Ipacaá: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7130) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-025-range` | [Biblioteca de aves — Ipacaá: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7143) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-025-habitat` | [Biblioteca de aves — Ipacaá: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7132) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-026` | [SIB — Jacana (Jacana jacana); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/jacana-jacana) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-026` | [Biblioteca de aves — Jacana: descripción e identificación](https://www.fcv.unl.edu.ar/aves/jacana/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-026-diet` | [Biblioteca de aves — Jacana: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5011) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-026-range` | [Biblioteca de aves — Jacana: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5030) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-026-habitat` | [Biblioteca de aves — Jacana: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=5018) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-027` | [SIB — Macá común (Rollandia rolland); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/rollandia-rolland) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-027` | [Biblioteca de aves — Macá común: descripción e identificación](https://www.fcv.unl.edu.ar/aves/maca-cara-blanca/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-027-diet` | [Biblioteca de aves — Macá común: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6269) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-027-range` | [Biblioteca de aves — Macá común: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6279) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-027-habitat` | [Biblioteca de aves — Macá común: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6271) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-028` | [SIB — Macá grande (Podiceps major); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/podiceps-major) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-028` | [Biblioteca de aves — Macá grande: descripción e identificación](https://www.fcv.unl.edu.ar/aves/categorias/podicipedidae/maca-grande/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-028-diet` | [Biblioteca de aves — Macá grande: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6291) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-028-range` | [Biblioteca de aves — Macá grande: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6302) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-028-habitat` | [Biblioteca de aves — Macá grande: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6294) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-029` | [SIB — Macá pico grueso (Podilymbus podiceps); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/podilymbus-podiceps) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-029` | [Biblioteca de aves — Macá pico grueso: descripción e identificación](https://www.fcv.unl.edu.ar/aves/maca-pico-grueso/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-029-diet` | [Biblioteca de aves — Macá pico grueso: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6330) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-029-range` | [Biblioteca de aves — Macá pico grueso: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6340) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-029-habitat` | [Biblioteca de aves — Macá pico grueso: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6332) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-030` | [SIB — Pato barcino (Anas flavirostris); categoría nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/anas-flavirostris) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-030` | [Biblioteca de aves — Pato barcino: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-barcino/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-030-diet` | [Biblioteca de aves — Pato barcino: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1508) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-030-range` | [Biblioteca de aves — Pato barcino: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1523) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-030-habitat` | [Biblioteca de aves — Pato barcino: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1510) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |

Consultas adicionales de contraste (no fuentes fotográficas): [Bianchini (2023), Acta Zoológica Lilloana 67(1), DOI 10.30550/j.azl/2023.67.1/2023-03-03](https://www.lillo.org.ar/journals/index.php/acta-zoologica-lilloana/article/view/1722), resumen leído el 22/10/2026; [El Hornero 40(2), 2025](https://elhornero.avesargentinas.org.ar/home/issue/download/40-2/128), resultado indexado con Progne chalybea como migrador reproductor, apertura rechazada por tamaño superior al límite del lector: no se usa como evidencia de un ciclo nacional. [eBird — Wattled Jacana](https://ebird.org/species/watjac1) devolvió protección de acceso; se usó la fuente UCI que sí pudo leerse para corregir el nombre inglés.

### Revisión individual de T4

#### PHOTO-016 — Garza blanca (`ardea_alba`)

Ardea alba coincide en FCV/UNL y SIB; Casmerodius albus y Egretta alba son equivalencias históricas, sin cambiar la clave. La descripción regional del SIB no prueba una migración nacional. La ficha SIB falló al abrirse directamente (502/TLS); la búsqueda indexada permitió leer identidad y ciclo regional. Para LC global se usa además Audubon, sin año de evaluación explícito.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-016` |
| Nombre científico | `sib-016`, `audubon-egret` |
| Nombre inglés | `audubon-egret` |
| Familia | `sib-016` |
| Grupo divulgativo | `sib-016` |
| Orden | `sib-016` |
| Regiones argentinas | `unl-016-range` |
| Morfología | `unl-016`, `sib-016` |
| Hábitat | `unl-016-habitat` |
| Alimentación | `unl-016-diet` |
| Movimientos | `sib-016` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `audubon-egret` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-016`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-016.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-017 — Garza bruja (`nycticorax_nycticorax`)

FCV/UNL indica 60–65 cm y el extracto regional del SIB 50 cm: se omite una talla única ante la diferencia de medidas. La variación juvenil se describe sin asignar sexo a la foto. La residencia citada corresponde al Atlas del Noroeste Patagónico de 2004 reproducido por SIB.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-017` |
| Nombre científico | `sib-017` |
| Nombre inglés | `sib-017` |
| Familia | `sib-017` |
| Grupo divulgativo | `sib-017` |
| Orden | `sib-017` |
| Regiones argentinas | `unl-017-range` |
| Morfología | `unl-017`, `sib-017` |
| Hábitat | `unl-017-habitat`, `sib-017` |
| Alimentación | `unl-017-diet` |
| Movimientos | `sib-017` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-017` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-017`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-017.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-018 — Garza mora (`ardea_cocoi`)

El alcance nacional amplio de FCV/UNL y la escasez patagónica del SIB no son equivalentes: se separa extensión de distribución de frecuencia regional. El texto del SIB procede de una guía regional de 2004, no de un censo nacional reciente.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-018` |
| Nombre científico | `sib-018` |
| Nombre inglés | `sib-018` |
| Familia | `sib-018` |
| Grupo divulgativo | `sib-018` |
| Orden | `sib-018` |
| Regiones argentinas | `unl-018-range`, `sib-018` |
| Morfología | `unl-018`, `sib-018` |
| Hábitat | `unl-018-habitat` |
| Alimentación | `unl-018-diet` |
| Movimientos | `sib-018` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-018` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-018`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-018.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-019 — Gaviota capucho café (`chroicocephalus_maculipennis`)

Se mantiene Chroicocephalus maculipennis; Larus maculipennis es sinónimo registrado en SIB. El capucho es estacional, no un rasgo presente en todos los ejemplares todo el año. La posible residencia regional del SIB no invalida los desplazamientos invernales documentados por FCV/UNL.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-019` |
| Nombre científico | `sib-019` |
| Nombre inglés | `sib-019` |
| Familia | `sib-019` |
| Grupo divulgativo | `sib-019` |
| Orden | `sib-019` |
| Regiones argentinas | `unl-019-range` |
| Morfología | `unl-019`, `sib-019` |
| Hábitat | `unl-019-habitat` |
| Alimentación | `unl-019-diet` |
| Movimientos | `unl-019-range`, `sib-019` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-019` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-019`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-019.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-020 — Gaviota capucho gris (`chroicocephalus_cirrocephalus`)

FCV/UNL omite la s final del género (Chroicocephalu); se usa Chroicocephalus según SIB. El Atlas escribe cirrhocephalus: se mantiene cirrocephalus como en SIB y la clave existente. La frase de FCV/UNL sobre el capucho en reposo es ambigua; no se añade un patrón invernal preciso sin contraste suficiente. No se presenta todo el norte como área de presencia regular: el Atlas advierte rareza en Formosa.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-020` |
| Nombre científico | `sib-020` |
| Nombre inglés | `sib-020` |
| Familia | `sib-020` |
| Grupo divulgativo | `sib-020` |
| Orden | `sib-020` |
| Regiones argentinas | `unl-020-range`, `atlas-t4` |
| Morfología | `unl-020`, `sib-020` |
| Hábitat | `unl-020-habitat` |
| Alimentación | `unl-020-diet` |
| Movimientos | `atlas-t4` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-020` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-020`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-020.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-021 — Gaviota cocinera (`larus_dominicanus`)

Se conserva Larus dominicanus. Para ciclo anual se utiliza el resultado indexado del SIB, que restringe la residencia al NW patagónico; las aperturas directas dieron 502/TLS. Audubon confirma nombre inglés y LC global, sin año de evaluación. No se extrapolan sus registros divagantes de Norteamérica a Argentina.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-021` |
| Nombre científico | `sib-021` |
| Nombre inglés | `audubon-kelp` |
| Familia | `sib-021` |
| Grupo divulgativo | `sib-021` |
| Orden | `sib-021` |
| Regiones argentinas | `unl-021-range` |
| Morfología | `unl-021`, `sib-021` |
| Hábitat | `unl-021-habitat` |
| Alimentación | `unl-021-diet` |
| Movimientos | `sib-021` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `audubon-kelp` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-021`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-021.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-022 — Golondrina doméstica (`progne_chalybea`)

Nombre y taxonomía coinciden en SIB y FCV/UNL. Las fichas iniciales no describían el ciclo anual. Una tabla indexada de El Hornero 40(2), 2025, no se usó porque el volumen excedió el límite del lector y no se verificó su ámbito. La consulta ampliada encontró una entrevista institucional de UNL a Rodrigo Lorenzón (INALI), publicada el 26/08/2014, que sí especifica el ciclo de la golondrina doméstica en Santa Fe e inmediaciones. Se incorpora únicamente ese alcance regional, sin inferir migración por formar bandadas ni atribuirle una evaluación de 2026.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-022` |
| Nombre científico | `sib-022` |
| Nombre inglés | `sib-022` |
| Familia | `sib-022` |
| Grupo divulgativo | `sib-022` |
| Orden | `sib-022` |
| Regiones argentinas | `unl-022-range` |
| Morfología | `unl-022`, `sib-022` |
| Hábitat | `unl-022-habitat` |
| Alimentación | `unl-022-diet` |
| Movimientos | `unl-golondrinas-2014` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-022` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-022`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-022.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-023 — Golondrina tijerita (`hirundo_rustica`)

FCV/UNL describe migración norteamericana y cría en Buenos Aires/La Pampa. Winkler y colaboradores (2017) siguieron nueve aves de la población argentina y demostraron un ciclo austral distinto; no se atribuye ese destino a todos los visitantes. Se evita convertir la enumeración de lugares de cría de FCV/UNL en un límite actual, dado el trabajo de Bianchini (2023) y los nuevos registros de Santa Fe presentados en RAO 2026. No se incorpora una sección de cría.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-023` |
| Nombre científico | `sib-023` |
| Nombre inglés | `sib-023` |
| Familia | `sib-023` |
| Grupo divulgativo | `sib-023` |
| Orden | `sib-023` |
| Regiones argentinas | `unl-023-range`, `rao-tijerita-2026` |
| Morfología | `unl-023`, `sib-023` |
| Hábitat | `unl-023-habitat` |
| Alimentación | `unl-023-diet` |
| Movimientos | `unl-023-range`, `winkler-2017` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-023` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-023`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-023.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-024 — Hocó colorado (`tigrisoma_lineatum`)

Se diferencia adulto rojizo de juvenil barrado. El Atlas aporta residencia de la población formoseña (p. 49); su apreciación de conservación local no se utiliza como categoría global ni nacional.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-024` |
| Nombre científico | `sib-024` |
| Nombre inglés | `sib-024` |
| Familia | `sib-024` |
| Grupo divulgativo | `sib-024` |
| Orden | `sib-024` |
| Regiones argentinas | `unl-024-range` |
| Morfología | `unl-024`, `sib-024` |
| Hábitat | `unl-024-habitat` |
| Alimentación | `unl-024-diet` |
| Movimientos | `atlas-t4` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-024` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-024`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-024.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-025 — Ipacaá (`aramides_ypecaha`)

Se conserva Aramides ypecaha y su clave. El Atlas menciona La Pampa pero advierte registros extralimitales posiblemente inconstantes; se mantiene la síntesis de distribución de FCV/UNL, sin presentar un límite meridional absoluto. Grupo divulgativo Gallinetas, distinto del orden Gruiformes.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-025` |
| Nombre científico | `sib-025` |
| Nombre inglés | `sib-025` |
| Familia | `sib-025` |
| Grupo divulgativo | `sib-025` |
| Orden | `sib-025` |
| Regiones argentinas | `unl-025-range` |
| Morfología | `unl-025`, `sib-025` |
| Hábitat | `unl-025-habitat` |
| Alimentación | `unl-025-diet` |
| Movimientos | `atlas-t4` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-025` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-025`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-025.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-026 — Jacana (`jacana_jacana`)

SIB escribe Watled Jacana: se usa Wattled Jacana, confirmado en la página del biólogo John C. Avise (University of California, Irvine). FCV/UNL extiende los registros más al sur que el Atlas de Formosa; se atribuye el alcance a FCV/UNL sin convertir todos los registros en residencia. PHOTO-026 coincide visualmente con NaturFoto 26215; autoría resuelta como Luboš Mráz, permiso pendiente.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-026` |
| Nombre científico | `sib-026` |
| Nombre inglés | `uci-jacana` |
| Familia | `sib-026` |
| Grupo divulgativo | `sib-026` |
| Orden | `sib-026` |
| Regiones argentinas | `unl-026-range` |
| Morfología | `unl-026`, `sib-026` |
| Hábitat | `unl-026-habitat`, `uci-jacana` |
| Alimentación | `unl-026-diet` |
| Movimientos | `atlas-t4` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-026` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-026`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-026.webp`. Sexo no asignado. Autor Luboš Mráz y procedencia NaturFoto 26215 contrastados; licencia no adquirida. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-027 — Macá común (`rollandia_rolland`)

Macá común es el nombre existente del juego; Macá cara blanca es el encabezado de SIB/FCV/UNL y el propio extracto SIB también usa común. Se conserva la denominación conocida y Rollandia rolland. SIB resume todo el país; la excepción de Misiones de FCV/UNL se mantiene atribuida, sin afirmar ausencia biológica demostrada.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-027`, `mads-aa-t4-2015` |
| Nombre científico | `sib-027` |
| Nombre inglés | `sib-027` |
| Familia | `sib-027` |
| Grupo divulgativo | `sib-027` |
| Orden | `sib-027` |
| Regiones argentinas | `unl-027-range` |
| Morfología | `unl-027`, `sib-027` |
| Hábitat | `unl-027-habitat` |
| Alimentación | `unl-027-diet` |
| Movimientos | `sib-027` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-027` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-027`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-027.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-028 — Macá grande (`podiceps_major`)

Se conserva Podiceps major según SIB y FCV/UNL; SIB registra Aechmophorus major y Podicephorus major como sinónimos. FCV/UNL indica 60 cm y el extracto SIB 38 cm: se omite cifra única. Para movimientos se documenta la diferencia de ámbito y no se fuerza una residencia o migración universal. La URL corta de FCV/UNL dio 404; se leyó la ruta de categoría vinculada en su menú.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-028` |
| Nombre científico | `sib-028` |
| Nombre inglés | `sib-028` |
| Familia | `sib-028` |
| Grupo divulgativo | `sib-028` |
| Orden | `sib-028` |
| Regiones argentinas | `unl-028-range` |
| Morfología | `unl-028`, `sib-028` |
| Hábitat | `unl-028-habitat` |
| Alimentación | `unl-028-diet` |
| Movimientos | `unl-028-range`, `sib-028` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-028` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-028`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-028.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-029 — Macá pico grueso (`podilymbus_podiceps`)

La banda del pico es estacional: no se exige para identificar ejemplares en reposo. El SIB regional cita invertebrados; la lista más amplia de FCV/UNL incluye peces y anfibios y no contradice ese consumo. No se trasladan migraciones norteamericanas de la especie a Argentina.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-029` |
| Nombre científico | `sib-029` |
| Nombre inglés | `sib-029` |
| Familia | `sib-029` |
| Grupo divulgativo | `sib-029` |
| Orden | `sib-029` |
| Regiones argentinas | `unl-029-range` |
| Morfología | `unl-029`, `sib-029` |
| Hábitat | `unl-029-habitat`, `sib-029` |
| Alimentación | `unl-029-diet` |
| Movimientos | `sib-029` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-029` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-029`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-029.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

#### PHOTO-030 — Pato barcino (`anas_flavirostris`)

Se mantiene Anas flavirostris. Ambas fuentes incluyen oxyptera como subespecie; no se adjudica una subespecie a la foto ni se crea una nueva especie. La dieta vegetal del SIB es explícitamente presumida: se prefiere la lista afirmativa de FCV/UNL. La residencia estival regional es compatible con desplazamientos invernales parciales.

| Campo | Respaldo por ID |
| --- | --- |
| Nombre español | `sib-030` |
| Nombre científico | `sib-030` |
| Nombre inglés | `sib-030` |
| Familia | `sib-030` |
| Grupo divulgativo | `sib-030` |
| Orden | `sib-030` |
| Regiones argentinas | `unl-030-range` |
| Morfología | `unl-030`, `sib-030` |
| Hábitat | `unl-030-habitat`, `sib-030` |
| Alimentación | `unl-030-diet` |
| Movimientos | `unl-030-range`, `sib-030` |
| Global: Preocupación menor (LC); evaluación no informada (nulo) | `sib-030` |
| Nacional: No amenazada (NA); evaluación 2015 | `sib-030`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-030.webp`. Sexo no asignado. Autor, publicación individual y permiso sin verificar. Fechas y consultas fotográficas en el registro asociado.

### Controles acumulados y estado de T4

C3 del parser y assets: **12 casos ejecutados, 0 fallos, 0 errores y 0 omitidos**, con el control editorial ampliado a **30 especies**. Se verifican claves exactas PHOTO-001–030, fuentes resolubles, conservación separada, representativas originales, rutas válidas, decodificación Android y unicidad binaria. La comprobación adicional local verifica que el primer lote y sus fuentes no cambiaron, y que los originales seleccionados y assets previos conservan sus hashes. Comandos, resultados y límites en el [registro de T4](../../specs/003-ficha-informativa-especies/tasks.md#registro-de-ejecución-de-t4--22-de-octubre-de-2026).

Preparación técnica y contenido incorporados; **cierre editorial pendiente**: 14 autorías/procedencias individuales y 15 licencias o permisos del segundo lote. Se mantienen separados de los 15 pendientes fotográficos de T3 y los nueve permisos de T2. La ausencia biológica justificada no equivale a un permiso fotográfico. No se inicia T5.

## Lote T5 — PHOTO-031 a PHOTO-045

Consulta: **2026-10-22**. Se agregan 15 especies y cinco fotos complementarias ya preparadas en T2, sin copiar archivos ni modificar los 30 registros y 168 fuentes anteriores. Catálogo acumulado: **249 referencias**, todas con título, entidad, URL y fecha de consulta empaquetados. Las tablas por campo siguientes complementan las de T3/T4.

- Se contrastaron las 15 páginas de identificación y sus 45 páginas de alimentación, distribución y hábitat de FCV/UNL, además de las identidades, familias, órdenes y conservación del SIB. El grupo es una agrupación editorial divulgativa, no otro rango taxonómico.
- Conservación del lote: LC global, con año de evaluación desconocido, y NA nacional, evaluación **2015**, publicación **2017**. La categoría nacional se cotejó con Apéndice I del informe MAyDS/Aves Argentinas ya consultado en T4 (`mads-aa-t4-2015`); no se toma la columna UICN-N como global. Para colorado, LC global se respalda en Audubon. El resto usa UICN API reproducida en SIB. No se afirma consulta directa de expedientes UICN. Total acumulado: **44 años globales nulos**; flamenco conserva NT/2018 y VU nacional/2015.
- Movimientos: [Capllonch, Ortiz y Soria (2008), pp. 108–109 y 114](https://www.bfa.fcnym.unlp.edu.ar/catalogo/doc_num.php?explnum_id=2752), vincula cutirí, collar, gargantilla, tero y tero real con el corredor del litoral. Se mantiene ese alcance regional. [Pérez, Zaccagnini y Pereda (2011), p. 36](https://elhornero.avesargentinas.org.ar/index.php/home/article/download/694/680), respalda los desplazamientos de poblaciones de ambos siriríes hacia Uruguay y sur de Brasil. No se impone un calendario a todos los ejemplares.
- Único campo biológico completo nulo del catálogo: `gallinula_galeata.seasonalMovements`. Las fichas UNL/SIB consultadas describen distribución y ambientes, sin un ciclo estacional regional suficientemente verificable. El trabajo del corredor consultado no proporciona una asociación inequívoca de esa especie que permita completarlo. Se deja ausencia explícita para RF-21; no se la declara sedentaria ni carente de movimientos.
- Hábitat de tero: la página específica de UNL carece de descripción; se usa SIB y se omite esa referencia vacía del catálogo. SIB ofrece pampas, mallines, costas y áreas suburbanas.
- Las aperturas directas de algunas fichas SIB fallaron con 502/cache miss. La lectura indexada y la URL alternativa con `tab=fuentes` permitieron revisar contenido. Estos fallos de acceso no se presentan como lecturas directas exitosas. Las copias consultadas y respuestas web quedan en `app/build/t5-catalog/`, ignoradas.
- Taxonomía: `anas_versicolor`, `anas_cyanoptera` y `anas_platalea` presentan **Spatula**; `anas_sibilatrix` presenta **Mareca**. Se conservan claves, etiquetas y alias de mapas. Para tero real se mantiene **Himantopus mexicanus / White-backed Stilt** bajo el tratamiento explícito del SIB, que incluye `H. melanurus` como sinónimo; no se traslada una categoría o un comportamiento norteamericano por el nombre inglés. Gallinula conserva `galeata`; la equivalencia histórica `chloropus` no crea otra entrada. No se incorpora `oxyura_jamaicensis`.

### Fuentes nuevas empaquetadas

| ID | Referencia / entidad | Consulta |
| --- | --- | --- |
| `audubon-cinnamon` | [Cinnamon Teal — Spatula cyanoptera; UICN LC, año de evaluación no indicado](https://www.audubon.org/field-guide/bird/cinnamon-teal) — National Audubon Society | 2026-10-22 |
| `photo-oxyura-426947693` | [Oxyura vittata, foto 426947693, Mariano Cosentino / iNaturalist. Copia convertida a WebP y reducida, sin recorte; procedencia y licencia contrastadas en T2](https://commons.wikimedia.org/wiki/File:Oxyura_vittata_426947693.jpg) — Wikimedia Commons / iNaturalist | 2026-10-22 |
| `photo-ccby4` | [Creative Commons Attribution 4.0 International — licencia de la foto 426947693](https://creativecommons.org/licenses/by/4.0/) — Creative Commons | 2026-10-22 |
| `photo-collar-651502` | [Pato de Collar, fotografía 651502 — Hernán Tolosa, hembra adulta, 09/09/2025, Laguna de Navarro](https://www.ecoregistros.org/site/imagen.php?id=651502) — EcoRegistros / Hernán Tolosa | 2026-10-22 |
| `photo-ecoregistros-terms` | [Términos y condiciones — reutilización de fotografías sujeta a autorización expresa del autor; permiso pendiente](https://www.ecoregistros.org/site/terminosycondiciones.php) — EcoRegistros | 2026-10-22 |
| `capllonch-2008` | [Capllonch, Ortiz y Soria (2008). Importancia del Litoral fluvial argentino como corredor migratorio de aves. Miscelánea INSUGEO 17:107–120; lista pp. 108–109 y anillado p. 114](https://www.bfa.fcnym.unlp.edu.ar/catalogo/doc_num.php?explnum_id=2752) — CENAA / Instituto Miguel Lillo / Universidad Nacional de Tucumán; copia BFA-UNLP | 2026-10-22 |
| `perez-2011` | [Pérez, Zaccagnini y Pereda (2011). La influenza aviar y sus implicancias para la salud de las aves silvestres de América del Sur. El Hornero 26(1):29–44; movimientos de anátidos, p. 36](https://elhornero.avesargentinas.org.ar/index.php/home/article/download/694/680) — SENASA / INTA; Aves Argentinas | 2026-10-22 |
| `sib-031` | [SIB — Pato capuchino (Spatula versicolor); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/spatula-versicolor) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-031` | [Biblioteca de aves — Pato capuchino: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-capuchino/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-031-diet` | [Biblioteca de aves — Pato capuchino: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1608) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-031-range` | [Biblioteca de aves — Pato capuchino: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1620) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-031-habitat` | [Biblioteca de aves — Pato capuchino: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1610) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-032` | [SIB — Pato colorado (Spatula cyanoptera); nacional MADS/AA 2017](https://sib.gob.ar/especies/spatula-cyanoptera) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-032` | [Biblioteca de aves — Pato colorado: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-colorado/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-032-diet` | [Biblioteca de aves — Pato colorado: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1632) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-032-range` | [Biblioteca de aves — Pato colorado: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1642) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-032-habitat` | [Biblioteca de aves — Pato colorado: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1634) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-033` | [SIB — Pato cuchara (Spatula platalea); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/spatula-platalea) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-033` | [Biblioteca de aves — Pato cuchara: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-cuchara/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-033-diet` | [Biblioteca de aves — Pato cuchara: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1673) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-033-range` | [Biblioteca de aves — Pato cuchara: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1683) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-033-habitat` | [Biblioteca de aves — Pato cuchara: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1675) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-034` | [SIB — Pato cutirí (Amazonetta brasiliensis); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/amazonetta-brasiliensis) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-034` | [Biblioteca de aves — Pato cutirí: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-cutiri/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-034-diet` | [Biblioteca de aves — Pato cutirí: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1696) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-034-range` | [Biblioteca de aves — Pato cutirí: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1708) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-034-habitat` | [Biblioteca de aves — Pato cutirí: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1698) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-035` | [SIB — Pato de collar (Callonetta leucophrys); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/callonetta-leucophrys) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-035` | [Biblioteca de aves — Pato de collar: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-de-collar/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-035-diet` | [Biblioteca de aves — Pato de collar: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1726) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-035-range` | [Biblioteca de aves — Pato de collar: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1739) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-035-habitat` | [Biblioteca de aves — Pato de collar: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1728) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-036` | [SIB — Pato gargantilla (Anas bahamensis); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/anas-bahamensis) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-036` | [Biblioteca de aves — Pato gargantilla: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-gargantilla/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-036-diet` | [Biblioteca de aves — Pato gargantilla: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1775) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-036-range` | [Biblioteca de aves — Pato gargantilla: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1785) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-036-habitat` | [Biblioteca de aves — Pato gargantilla: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1777) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-037` | [SIB — Pato maicero (Anas georgica); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/anas-georgica) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-037` | [Biblioteca de aves — Pato maicero: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-maicero/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-037-diet` | [Biblioteca de aves — Pato maicero: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1798) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-037-range` | [Biblioteca de aves — Pato maicero: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1809) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-037-habitat` | [Biblioteca de aves — Pato maicero: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1801) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-038` | [SIB — Pato overo (Mareca sibilatrix); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/mareca-sibilatrix) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-038` | [Biblioteca de aves — Pato overo: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-overo/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-038-diet` | [Biblioteca de aves — Pato overo: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1836) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-038-range` | [Biblioteca de aves — Pato overo: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1850) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-038-habitat` | [Biblioteca de aves — Pato overo: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1839) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-039` | [SIB — Pato picazo (Netta peposaca); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/netta-peposaca) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-039` | [Biblioteca de aves — Pato picazo: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-picazo/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-039-diet` | [Biblioteca de aves — Pato picazo: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1859) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-039-range` | [Biblioteca de aves — Pato picazo: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1869) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-039-habitat` | [Biblioteca de aves — Pato picazo: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1861) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-040` | [SIB — Sirirí colorado (Dendrocygna bicolor); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/dendrocygna-bicolor) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-040` | [Biblioteca de aves — Sirirí colorado: descripción e identificación](https://www.fcv.unl.edu.ar/aves/siriri-colorado/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-040-diet` | [Biblioteca de aves — Sirirí colorado: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1925) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-040-range` | [Biblioteca de aves — Sirirí colorado: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1935) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-040-habitat` | [Biblioteca de aves — Sirirí colorado: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1927) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-041` | [SIB — Sirirí pampa (Dendrocygna viduata); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/dendrocygna-viduata?area%5B%5D=APN-CH&tab=fuentes) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-041` | [Biblioteca de aves — Sirirí pampa: descripción e identificación](https://www.fcv.unl.edu.ar/aves/siriri-pampa/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-041-diet` | [Biblioteca de aves — Sirirí pampa: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1948) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-041-range` | [Biblioteca de aves — Sirirí pampa: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1961) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-041-habitat` | [Biblioteca de aves — Sirirí pampa: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1950) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-042` | [SIB — Pato zambullidor chico (Oxyura vittata); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/oxyura-vittata) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-042` | [Biblioteca de aves — Pato zambullidor chico: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pato-zambullidor-ch/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-042-diet` | [Biblioteca de aves — Pato zambullidor chico: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1903) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-042-range` | [Biblioteca de aves — Pato zambullidor chico: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1913) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-042-habitat` | [Biblioteca de aves — Pato zambullidor chico: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=1905) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-043` | [SIB — Pollona negra (Gallinula galeata); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/gallinula-galeata) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-043` | [Biblioteca de aves — Pollona negra: descripción e identificación](https://www.fcv.unl.edu.ar/aves/pollona-negra/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-043-diet` | [Biblioteca de aves — Pollona negra: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7262) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-043-range` | [Biblioteca de aves — Pollona negra: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7274) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-043-habitat` | [Biblioteca de aves — Pollona negra: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=7264) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-044` | [SIB — Tero (Vanellus chilensis); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/vanellus-chilensis) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-044` | [Biblioteca de aves — Tero: descripción e identificación](https://www.fcv.unl.edu.ar/aves/tero/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-044-diet` | [Biblioteca de aves — Tero: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2741) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-044-range` | [Biblioteca de aves — Tero: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=2761) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `sib-045` | [SIB — Tero real (Himantopus mexicanus); nacional MADS/AA 2017; global UICN API sin año de evaluación](https://sib.gob.ar/especies/himantopus-mexicanus) — Administración de Parques Nacionales | 2026-10-22 |
| `unl-045` | [Biblioteca de aves — Tero real: descripción e identificación](https://www.fcv.unl.edu.ar/aves/tero-real/) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-045-diet` | [Biblioteca de aves — Tero real: alimentación](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6979) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-045-range` | [Biblioteca de aves — Tero real: distribución geográfica](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6989) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |
| `unl-045-habitat` | [Biblioteca de aves — Tero real: hábitat](https://www.fcv.unl.edu.ar/aves/?post_type=post&p=6981) — Facultad de Ciencias Veterinarias, Universidad Nacional del Litoral | 2026-10-22 |

### PHOTO-031 — Pato capuchino (`anas_versicolor`)

Presentación: *Spatula versicolor* / Silver Teal.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-031` |
| Nombre científico | `sib-031` |
| Nombre inglés | `sib-031` |
| Familia | `sib-031` |
| Grupo divulgativo | `sib-031` |
| Orden | `sib-031` |
| Distribución argentina | `unl-031-range` |
| Descripción y variantes | `unl-031`, `sib-031` |
| Hábitat | `unl-031-habitat` |
| Alimentación | `unl-031-diet` |
| Movimientos | `unl-031-range`, `sib-031` |
| Global LC, año no informado | `sib-031` |
| Nacional NA, evaluación 2015 | `sib-031`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-031.webp`. No se asigna sexo a una consulta general. UNL y SIB difieren en la extensión norte y en medidas; se usa distribución amplia y no se publican longitudes discrepantes. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-032 — Pato colorado (`anas_cyanoptera`)

Presentación: *Spatula cyanoptera* / Cinnamon Teal.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-032` |
| Nombre científico | `sib-032` |
| Nombre inglés | `sib-032` |
| Familia | `sib-032` |
| Grupo divulgativo | `sib-032` |
| Orden | `sib-032` |
| Distribución argentina | `unl-032-range` |
| Descripción y variantes | `unl-032`, `sib-032` |
| Hábitat | `unl-032-habitat` |
| Alimentación | `unl-032-diet` |
| Movimientos | `unl-032-range`, `sib-032` |
| Global LC, año no informado | `audubon-cinnamon` |
| Nacional NA, evaluación 2015 | `sib-032`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-032.webp`. Foto complementaria: `birds/photos/anas_cyanoptera_hembra.webp`. Biología y mapa compartidos bajo una sola clave; sin duplicar textos por sexo. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-033 — Pato cuchara (`anas_platalea`)

Presentación: *Spatula platalea* / Red Shoveler.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-033` |
| Nombre científico | `sib-033` |
| Nombre inglés | `sib-033` |
| Familia | `sib-033` |
| Grupo divulgativo | `sib-033` |
| Orden | `sib-033` |
| Distribución argentina | `unl-033-range` |
| Descripción y variantes | `unl-033`, `sib-033` |
| Hábitat | `unl-033-habitat` |
| Alimentación | `unl-033-diet` |
| Movimientos | `unl-033-range`, `sib-033` |
| Global LC, año no informado | `sib-033` |
| Nacional NA, evaluación 2015 | `sib-033`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-033.webp`. La etiqueta única `anas_platalea_macho` selecciona esta foto de macho; no constituye una sexta pareja. UNL delimita el alcance habitual desde el centro; SIB registra todo el país. Se conserva la diferencia en la distribución redactada. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-034 — Pato cutirí (`amazonetta_brasiliensis`)

Presentación: *Amazonetta brasiliensis* / Brazilian Teal.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-034` |
| Nombre científico | `sib-034` |
| Nombre inglés | `sib-034` |
| Familia | `sib-034` |
| Grupo divulgativo | `sib-034` |
| Orden | `sib-034` |
| Distribución argentina | `unl-034-range` |
| Descripción y variantes | `unl-034`, `sib-034` |
| Hábitat | `unl-034-habitat` |
| Alimentación | `unl-034-diet` |
| Movimientos | `capllonch-2008` |
| Global LC, año no informado | `sib-034` |
| Nacional NA, evaluación 2015 | `sib-034`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-034.webp`. Foto complementaria: `birds/photos/amazonetta_brasiliensis_hembra.webp`. Biología y mapa compartidos bajo una sola clave; sin duplicar textos por sexo. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-035 — Pato de collar (`callonetta_leucophrys`)

Presentación: *Callonetta leucophrys* / Ringed Teal.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-035` |
| Nombre científico | `sib-035` |
| Nombre inglés | `sib-035` |
| Familia | `sib-035` |
| Grupo divulgativo | `sib-035` |
| Orden | `sib-035` |
| Distribución argentina | `unl-035-range` |
| Descripción y variantes | `unl-035`, `sib-035` |
| Hábitat | `unl-035-habitat` |
| Alimentación | `unl-035-diet` |
| Movimientos | `capllonch-2008` |
| Global LC, año no informado | `sib-035` |
| Nacional NA, evaluación 2015 | `sib-035`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-035.webp`. Foto complementaria: `birds/photos/callonetta_leucophrys_hembra.webp`. Biología y mapa compartidos bajo una sola clave; sin duplicar textos por sexo. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-036 — Pato gargantilla (`anas_bahamensis`)

Presentación: *Anas bahamensis* / White-cheeked Pintail.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-036` |
| Nombre científico | `sib-036` |
| Nombre inglés | `sib-036` |
| Familia | `sib-036` |
| Grupo divulgativo | `sib-036` |
| Orden | `sib-036` |
| Distribución argentina | `unl-036-range` |
| Descripción y variantes | `unl-036`, `sib-036` |
| Hábitat | `unl-036-habitat` |
| Alimentación | `unl-036-diet` |
| Movimientos | `capllonch-2008` |
| Global LC, año no informado | `sib-036` |
| Nacional NA, evaluación 2015 | `sib-036`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-036.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-037 — Pato maicero (`anas_georgica`)

Presentación: *Anas georgica* / Yellow-billed Pintail.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-037` |
| Nombre científico | `sib-037` |
| Nombre inglés | `sib-037` |
| Familia | `sib-037` |
| Grupo divulgativo | `sib-037` |
| Orden | `sib-037` |
| Distribución argentina | `unl-037-range` |
| Descripción y variantes | `unl-037`, `sib-037` |
| Hábitat | `unl-037-habitat` |
| Alimentación | `unl-037-diet` |
| Movimientos | `sib-037` |
| Global LC, año no informado | `sib-037` |
| Nacional NA, evaluación 2015 | `sib-037`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-037.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-038 — Pato overo (`anas_sibilatrix`)

Presentación: *Mareca sibilatrix* / Chiloe Wigeon.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-038` |
| Nombre científico | `sib-038` |
| Nombre inglés | `sib-038` |
| Familia | `sib-038` |
| Grupo divulgativo | `sib-038` |
| Orden | `sib-038` |
| Distribución argentina | `unl-038-range` |
| Descripción y variantes | `unl-038`, `sib-038` |
| Hábitat | `unl-038-habitat` |
| Alimentación | `unl-038-diet` |
| Movimientos | `unl-038-range`, `sib-038` |
| Global LC, año no informado | `sib-038` |
| Nacional NA, evaluación 2015 | `sib-038`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-038.webp`. No se asigna sexo a una consulta general. La posible nidificación santafesina de UNL permanece como hipótesis; no se transforma en dato confirmado. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-039 — Pato picazo (`netta_peposaca`)

Presentación: *Netta peposaca* / Rosy-billed Pochard.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-039` |
| Nombre científico | `sib-039` |
| Nombre inglés | `sib-039` |
| Familia | `sib-039` |
| Grupo divulgativo | `sib-039` |
| Orden | `sib-039` |
| Distribución argentina | `unl-039-range` |
| Descripción y variantes | `unl-039`, `sib-039` |
| Hábitat | `unl-039-habitat` |
| Alimentación | `unl-039-diet` |
| Movimientos | `unl-039-range`, `sib-039` |
| Global LC, año no informado | `sib-039` |
| Nacional NA, evaluación 2015 | `sib-039`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-039.webp`. Foto complementaria: `birds/photos/netta_peposaca_hembra.webp`. Biología y mapa compartidos bajo una sola clave; sin duplicar textos por sexo. La referencia de paso en Lanín/Nahuel Huapi se mantiene localizada; no limita la distribución general a esos parques. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-040 — Sirirí colorado (`dendrocygna_bicolor`)

Presentación: *Dendrocygna bicolor* / Fulvous Whistling-duck.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-040` |
| Nombre científico | `sib-040` |
| Nombre inglés | `sib-040` |
| Familia | `sib-040` |
| Grupo divulgativo | `sib-040` |
| Orden | `sib-040` |
| Distribución argentina | `unl-040-range` |
| Descripción y variantes | `unl-040`, `sib-040` |
| Hábitat | `unl-040-habitat` |
| Alimentación | `unl-040-diet` |
| Movimientos | `perez-2011` |
| Global LC, año no informado | `sib-040` |
| Nacional NA, evaluación 2015 | `sib-040`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-040.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-041 — Sirirí pampa (`dendrocygna_viduata`)

Presentación: *Dendrocygna viduata* / White-faced Whistling-duck.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-041` |
| Nombre científico | `sib-041` |
| Nombre inglés | `sib-041` |
| Familia | `sib-041` |
| Grupo divulgativo | `sib-041` |
| Orden | `sib-041` |
| Distribución argentina | `unl-041-range` |
| Descripción y variantes | `unl-041`, `sib-041` |
| Hábitat | `unl-041-habitat` |
| Alimentación | `unl-041-diet` |
| Movimientos | `perez-2011` |
| Global LC, año no informado | `sib-041` |
| Nacional NA, evaluación 2015 | `sib-041`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-041.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-042 — Pato zambullidor chico (`oxyura_vittata`)

Presentación: *Oxyura vittata* / Lake Duck.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-042` |
| Nombre científico | `sib-042` |
| Nombre inglés | `sib-042` |
| Familia | `sib-042` |
| Grupo divulgativo | `sib-042` |
| Orden | `sib-042` |
| Distribución argentina | `unl-042-range` |
| Descripción y variantes | `unl-042`, `sib-042` |
| Hábitat | `unl-042-habitat` |
| Alimentación | `unl-042-diet` |
| Movimientos | `sib-042` |
| Global LC, año no informado | `sib-042` |
| Nacional NA, evaluación 2015 | `sib-042`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-042.webp`. Foto complementaria: `birds/photos/oxyura_vittata_macho.webp`. Biología y mapa compartidos bajo una sola clave; sin duplicar textos por sexo. La advertencia de especies Oxyura similares se conserva; identidad del macho respaldada por la procedencia individual investigada en T2. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-043 — Pollona negra (`gallinula_galeata`)

Presentación: *Gallinula galeata* / Common Gallinule.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-043` |
| Nombre científico | `sib-043` |
| Nombre inglés | `sib-043` |
| Familia | `sib-043` |
| Grupo divulgativo | `sib-043` |
| Orden | `sib-043` |
| Distribución argentina | `unl-043-range` |
| Descripción y variantes | `unl-043`, `sib-043` |
| Hábitat | `unl-043-habitat` |
| Alimentación | `unl-043-diet` |
| Movimientos | Nulo: ausencia justificada arriba |
| Global LC, año no informado | `sib-043` |
| Nacional NA, evaluación 2015 | `sib-043`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-043.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-044 — Tero (`vanellus_chilensis`)

Presentación: *Vanellus chilensis* / Southern Lapwing.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-044` |
| Nombre científico | `sib-044` |
| Nombre inglés | `sib-044` |
| Familia | `sib-044` |
| Grupo divulgativo | `sib-044` |
| Orden | `sib-044` |
| Distribución argentina | `unl-044-range` |
| Descripción y variantes | `unl-044`, `sib-044` |
| Hábitat | `sib-044` |
| Alimentación | `unl-044-diet` |
| Movimientos | `capllonch-2008` |
| Global LC, año no informado | `sib-044` |
| Nacional NA, evaluación 2015 | `sib-044`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-044.webp`. No se asigna sexo a una consulta general. Créditos y permisos por imagen en el registro fotográfico.

### PHOTO-045 — Tero real (`himantopus_mexicanus`)

Presentación: *Himantopus mexicanus* / White-backed Stilt.

| Campo | Referencias de respaldo |
| --- | --- |
| Nombre común | `sib-045` |
| Nombre científico | `sib-045` |
| Nombre inglés | `sib-045` |
| Familia | `sib-045` |
| Grupo divulgativo | `sib-045` |
| Orden | `sib-045` |
| Distribución argentina | `unl-045-range` |
| Descripción y variantes | `unl-045`, `sib-045` |
| Hábitat | `unl-045-habitat` |
| Alimentación | `unl-045-diet` |
| Movimientos | `capllonch-2008` |
| Global LC, año no informado | `sib-045` |
| Nacional NA, evaluación 2015 | `sib-045`, `mads-aa-t4-2015` |

Representativa conservada: `learning/photos/photo-045.webp`. No se asigna sexo a una consulta general. Se conserva el tratamiento taxonómico del SIB; la foto no acredita por sí sola una localidad argentina. Créditos y permisos por imagen en el registro fotográfico.

### Revisión acumulada de las 45 fichas y 50 asociaciones

Se releyeron las descripciones y ciclos regionales de las 30 fichas heredadas y se contrastaron sus referencias con los registros y evidencias T3/T4; se conservaron íntegros esos registros JSON y sus 168 fuentes. Esto es revisión de evidencia existente, no una nueva visita a todas las páginas históricas. Para las nuevas 15 se revisaron las afirmaciones frente a las fuentes consultadas en este turno. El control local recorre todos los campos, fuentes y ausencias; una referencia que resuelve por ID no acredita por sí sola el contenido de una afirmación.

| Foto | Clave revisada | Referencias por campo | Representativa / fotos | Resultado editorial |
| --- | --- | --- | --- | --- |
| PHOTO-001 | `phalacrocorax_brasilianus` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-002 | `aramus_guarauna` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-003 | `chauna_torquata` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-004 | `syrigma_sibilatrix` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-005 | `ciconia_maguari` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-006 | `cygnus_melancoryphus` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-007 | `coscoroba_coscoroba` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-008 | `phimosus_infuscatus` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-009 | `plegadis_chihi` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-010 | `platalea_ajaja` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-011 | `phoenicopterus_chilensis` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-012 | `fulica_leucoptera` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-013 | `fulica_rufifrons` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-014 | `fulica_armillata` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-015 | `egretta_thula` | T3, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-016 | `ardea_alba` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-017 | `nycticorax_nycticorax` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-018 | `ardea_cocoi` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-019 | `chroicocephalus_maculipennis` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-020 | `chroicocephalus_cirrocephalus` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-021 | `larus_dominicanus` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-022 | `progne_chalybea` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-023 | `hirundo_rustica` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-024 | `tigrisoma_lineatum` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-025 | `aramides_ypecaha` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-026 | `jacana_jacana` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-027 | `rollandia_rolland` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-028 | `podiceps_major` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-029 | `podilymbus_podiceps` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-030 | `anas_flavirostris` | T4, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-031 | `anas_versicolor` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-032 | `anas_cyanoptera` | T5, tabla individual | Original conservada / 2 | permisos pendientes por imagen |
| PHOTO-033 | `anas_platalea` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-034 | `amazonetta_brasiliensis` | T5, tabla individual | Original conservada / 2 | permisos pendientes por imagen |
| PHOTO-035 | `callonetta_leucophrys` | T5, tabla individual | Original conservada / 2 | permisos pendientes por imagen |
| PHOTO-036 | `anas_bahamensis` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-037 | `anas_georgica` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-038 | `anas_sibilatrix` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-039 | `netta_peposaca` | T5, tabla individual | Original conservada / 2 | permisos pendientes por imagen |
| PHOTO-040 | `dendrocygna_bicolor` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-041 | `dendrocygna_viduata` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-042 | `oxyura_vittata` | T5, tabla individual | Original conservada / 2 | permisos pendientes por imagen |
| PHOTO-043 | `gallinula_galeata` | T5, tabla individual | Original conservada / 1 | Movimientos nulos justificados; permisos pendientes por imagen |
| PHOTO-044 | `vanellus_chilensis` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |
| PHOTO-045 | `himantopus_mexicanus` | T5, tabla individual | Original conservada / 1 | permisos pendientes por imagen |

Prueba C3 final: **12 casos, 0 fallos, 0 errores, 0 omitidos**, Android 16/API 36.1. Verifica las 45 claves, las 50 etiquetas reales (incluido macho único de cuchara), ambas fotos de las cinco parejas, la selección general original, referencias resolubles y decodificación de las 50 fotos sin duplicados binarios. Las variantes apuntan al mismo objeto de especie; el mapa se sigue resolviendo por esa clave, sin cambiar sus recursos. No se implementa ni verifica aquí la presentación de T6–T8.

Peso de archivos: catálogo **190.090 bytes**, antes **127.607**, incremento T5 **62.483 bytes**. Fotos añadidas por T5: **0 bytes**; las cinco complementarias de T2 suman **352.044 bytes**. Contenido nuevo acumulado de esta feature (JSON + complementarias): **542.134 bytes**, unos **0,517 MiB**. Se reutilizan los 4.213.172 bytes de fotos del juego, sin duplicarlos. No es una medición de la diferencia de APK comprimidos.

**Resultado: cobertura técnica de T5 verificada; cierre editorial pendiente**. Las 50 fotos únicas tienen 47 autorías/procedencias individuales y 49 licencias o permisos sin resolver. No se suman otra vez los diez recursos de T2, cinco de los cuales son representativas del último lote. Detención antes de T6, sin commits ni push. Evidencia detallada en el [registro de T5](../../specs/003-ficha-informativa-especies/tasks.md#registro-de-ejecución-de-t5--22-de-octubre-de-2026).
