# Selección fotográfica — Aprender jugando (T4)

Fecha de revisión: 19 de octubre de 2026 (Argentina). Colección: `aves_tesis_v2`, aportada por el usuario. Alcance: una foto por cada una de sus 45 especies; no se modifica el catálogo del clasificador.

## Selección y revisión

Se compararon visualmente cuatro candidatas por especie, distribuidas a lo largo de los archivos de cada carpeta, y se ampliaron las alternativas cuando el ave resultaba pequeña, oculta, poco nítida o difícil de separar de otras especies. La elección final es individual, no la primera imagen de una carpeta ni el resultado de un clasificador. Las carpetas de macho y hembra se agrupan bajo la clave canónica existente. Para `oxyura_vittata` se eligió una hembra por el contraste de sus franjas faciales y barrado, en lugar de inferir la especie de un macho oscuro sin suficientes rasgos.

Se inspeccionaron las 45 fotos elegidas junto con sus 45 versiones WebP. Cada fila registra los rasgos efectivamente visibles que respaldan su correspondencia con el nombre del catálogo. «Apta / conserva» significa correspondencia visual aceptada para esta selección y conservación de esos rasgos en la copia; no implica una determinación de sexo, edad, lugar de captura ni un dictamen de un especialista externo. Los casos dudosos descartados se registran aparte y no se consideran identificaciones validadas.

Las rutas de origen son relativas a la raíz de `aves_tesis_v2`. La ruta de salida enlaza al archivo empaquetado en `app/src/main/assets/learning/photos/`. Los nombres comunes reproducen el catálogo existente, incluidas sus grafías sin tildes; los científicos se forman desde su clave sin renombrarla. Se conservan `anas_*`, `phalacrocorax_brasilianus` e `himantopus_mexicanus` aunque las referencias puedan usar otra combinación taxonómica. `oxyura_jamaicensis`, presente en el catálogo pero sin carpeta en la colección, no recibe una tarjeta.

## Preparación y peso

- Conversión con Pillow 12.2.0 / libwebp: orientación EXIF aplicada (`ImageOps.exif_transpose`), RGB, reducción proporcional con LANCZOS hasta 1.024 px de lado mayor, WebP con pérdida, calidad 85 y método 6. Sin recortar, ampliar, retocar, reconstruir rasgos ni quitar firmas visibles.
- Las 45 seleccionadas tenían orientación EXIF ausente o normal (1). Se aplicó igualmente la operación de orientación antes de calcular las dimensiones. La salida no conserva EXIF ni GPS.
- `PHOTO-026` conserva sus 1.000 × 800 px y `PHOTO-033` sus 712 × 686 px: no se ampliaron. Las otras 43 tienen lado mayor de 1.024 px.
- 45 WebP: **4.213.172 bytes**, **4,02 MiB** (1 MiB = 1.048.576 bytes). Manifiesto: **5.681 bytes**. Incremento fotográfico total (fotos + manifiesto): **4.218.853 bytes**.
- Incluyendo las 40 preguntas previas (16.151 bytes), `learning/` suma **4.235.004 bytes**. Son tamaños de archivos, no una estimación del aumento del APK comprimido.
- No se modificaron ni incorporaron al repositorio los originales. La comparación SHA-256 de los 3.500 archivos de la colección antes/después queda registrada en la evidencia local; los hashes de las 45 fuentes y las copias se conservan abajo para trazabilidad.

## Registro individual

Dimensiones: ancho × alto del original orientado → WebP. Revisión realizada por Codex durante T4, mediante inspección visual; en todas las filas se comprobó también que la compresión no borra los rasgos descritos. Las aves secundarias o parcialmente recortadas por el fotógrafo no se usan como objetivo de identificación.

| ID / copia | Clave y nombres del catálogo | Origen relativo en la colección | Dimensiones (px) | Bytes WebP | Original / copia | Rasgos y observaciones |
| --- | --- | --- | --- | ---: | --- | --- |
| [PHOTO-001](../../app/src/main/assets/learning/photos/photo-001.webp) | `phalacrocorax_brasilianus` — bigua / *Phalacrocorax brasilianus* | `01_phalacrocorax_brasilianus/059.phalacrocorax_brasilianus.jpg` | 1024 × 683 → 1024 × 683 | 52404 | Apta / conserva | Plumaje oscuro, cuello largo y pico ganchudo con zona gular amarilla; ejemplar erguido a la izquierda. |
| [PHOTO-002](../../app/src/main/assets/learning/photos/photo-002.webp) | `aramus_guarauna` — carau / *Aramus guarauna* | `02_aramus_guarauna/020.aramus_guarauna.jpg` | 949 × 1024 → 949 × 1024 | 219464 | Apta / conserva | Pardo con estrías blancas en cuello y dorso, pico largo levemente curvo y patas largas; ave completa. |
| [PHOTO-003](../../app/src/main/assets/learning/photos/photo-003.webp) | `chauna_torquata` — chaja / *Chauna torquata* | `03_chauna_torquata/013.chauna_torquata.jpg` | 1024 × 683 → 1024 × 683 | 129580 | Apta / conserva | Copete, doble collar negro y blanco, cuerpo gris y patas rosadas; perfil completo. |
| [PHOTO-004](../../app/src/main/assets/learning/photos/photo-004.webp) | `syrigma_sibilatrix` — chiflon / *Syrigma sibilatrix* | `04_syrigma_sibilatrix/051.syrigma_sibilatrix.jpg` | 794 × 1024 → 794 × 1024 | 139036 | Apta / conserva | Cuello ocráceo, dorso gris, cara azulada y pico rosado de punta oscura; ave erguida. |
| [PHOTO-005](../../app/src/main/assets/learning/photos/photo-005.webp) | `ciconia_maguari` — ciguena americana / *Ciconia maguari* | `05_ciconia_maguari/044.ciconia_maguari.jpg` | 1024 × 768 → 1024 × 768 | 21452 | Apta / conserva | Cuello estirado blanco, rémiges negras y patas rojizas extendidas; vuelo de perfil. |
| [PHOTO-006](../../app/src/main/assets/learning/photos/photo-006.webp) | `cygnus_melancoryphus` — cisne cuello negro / *Cygnus melancoryphus* | `06_cygnus_melancoryphus/036.cygnus_melancoryphus.jpg` | 1024 × 684 → 1024 × 684 | 57008 | Apta / conserva | Cuerpo blanco, cuello y cabeza negros con carúncula roja en la base del pico. |
| [PHOTO-007](../../app/src/main/assets/learning/photos/photo-007.webp) | `coscoroba_coscoroba` — coscoroba / *Coscoroba coscoroba* | `07_coscoroba_coscoroba/029.coscoroba_coscoroba.jpg` | 1024 × 575 → 1024 × 575 | 51540 | Apta / conserva | Cuerpo y cuello blancos, pico rojo y patas rosadas; perfil completo sobre la orilla. |
| [PHOTO-008](../../app/src/main/assets/learning/photos/photo-008.webp) | `phimosus_infuscatus` — cuervillo cara pelada / *Phimosus infuscatus* | `08_phimosus_infuscatus/051.phimosus_infuscatus.jpg` | 1024 × 576 → 1024 × 576 | 143576 | Apta / conserva | Cara desnuda rojiza, pico curvo pálido y patas rosadas; plumaje negro verdoso en los ejemplares delanteros. |
| [PHOTO-009](../../app/src/main/assets/learning/photos/photo-009.webp) | `plegadis_chihi` — cuervillo de canada / *Plegadis chihi* | `09_plegadis_chihi/006.plegadis_chihi.jpg` | 1024 × 980 → 1024 × 980 | 63926 | Apta / conserva | Iris rojo visible, cara emplumada y pico gris curvo; cuello pardo con estriado claro, distinto de Phimosus. |
| [PHOTO-010](../../app/src/main/assets/learning/photos/photo-010.webp) | `platalea_ajaja` — espatula rosada / *Platalea ajaja* | `10_platalea_ajaja/044.platalea_ajaja.jpg` | 662 × 1024 → 662 × 1024 | 156534 | Apta / conserva | Pico aplanado en espátula y plumaje rosado intenso; cabeza y pico visibles. |
| [PHOTO-011](../../app/src/main/assets/learning/photos/photo-011.webp) | `phoenicopterus_chilensis` — flamenco austral / *Phoenicopterus chilensis* | `11_phoenicopterus_chilensis/020.phoenicopterus_chilensis.jpg` | 1024 × 768 → 1024 × 768 | 50992 | Apta / conserva | Adulto rosado, cuello largo y pico curvado con amplia mitad distal negra y base blanco rosada; cabeza nítida de perfil. |
| [PHOTO-012](../../app/src/main/assets/learning/photos/photo-012.webp) | `fulica_leucoptera` — gallareta chica / *Fulica leucoptera* | `12_fulica_leucoptera/029.fulica_leucoptera.jpg` | 1024 × 686 → 1024 × 686 | 104792 | Apta / conserva | Escudete pequeño redondeado amarillo más intenso que el pico pálido; cuerpo gris oscuro. |
| [PHOTO-013](../../app/src/main/assets/learning/photos/photo-013.webp) | `fulica_rufifrons` — gallareta escudete rojo / *Fulica rufifrons* | `13_fulica_rufifrons/059.fulica_rufifrons.jpg` | 1200 × 800 → 1024 × 683 | 53684 | Apta / conserva | Escudete rojo estrecho y alargado, pico amarillo, cabeza de perfil; cuerpo y cola visibles. |
| [PHOTO-014](../../app/src/main/assets/learning/photos/photo-014.webp) | `fulica_armillata` — gallareta ligas rojas / *Fulica armillata* | `14_fulica_armillata/013.fulica_armillata.jpg` | 1024 × 768 → 1024 × 768 | 93818 | Apta / conserva | Escudete grande pálido y franja rojiza entre escudete y pico amarillo; cabeza y cuerpo visibles. |
| [PHOTO-015](../../app/src/main/assets/learning/photos/photo-015.webp) | `egretta_thula` — garcita blanca / *Egretta thula* | `15_egretta_thula/059.egretta_thula.jpg` | 1024 × 769 → 1024 × 769 | 31044 | Apta / conserva | Garza blanca esbelta, pico negro, lores amarillos, patas oscuras y dedos amarillos. |
| [PHOTO-016](../../app/src/main/assets/learning/photos/photo-016.webp) | `ardea_alba` — garza blanca / *Ardea alba* | `16_ardea_alba/036.ardea_alba.jpg` | 1024 × 619 → 1024 × 619 | 183408 | Apta / conserva | Garza blanca de cuello largo, pico amarillo largo y patas negras; ojo y comisura visibles. |
| [PHOTO-017](../../app/src/main/assets/learning/photos/photo-017.webp) | `nycticorax_nycticorax` — garza bruja / *Nycticorax nycticorax* | `17_nycticorax_nycticorax/029.nycticorax_nycticorax.jpg` | 1024 × 768 → 1024 × 768 | 58224 | Apta / conserva | Corona y dorso negros, alas grises, vientre claro e iris rojo; ave completa de perfil. |
| [PHOTO-018](../../app/src/main/assets/learning/photos/photo-018.webp) | `ardea_cocoi` — garza mora / *Ardea cocoi* | `18_ardea_cocoi/044.ardea_cocoi.jpg` | 1024 × 683 → 1024 × 683 | 268256 | Apta / conserva | Corona negra, cuello blanco largo y cuerpo gris; patas y pico largo amarillo visibles. |
| [PHOTO-019](../../app/src/main/assets/learning/photos/photo-019.webp) | `chroicocephalus_maculipennis` — gaviota capucho cafe / *Chroicocephalus maculipennis* | `19_chroicocephalus_maculipennis/059.chroicocephalus_maculipennis.jpg` | 1024 × 769 → 1024 × 769 | 45542 | Apta / conserva | Capucho pardo, pico y patas rojizos; los tres adultos muestran el mismo patrón. |
| [PHOTO-020](../../app/src/main/assets/learning/photos/photo-020.webp) | `chroicocephalus_cirrocephalus` — gaviota capucho gris / *Chroicocephalus cirrocephalus* | `20_chroicocephalus_cirrocephalus/013.chroicocephalus_cirrocephalus.jpg` | 1024 × 683 → 1024 × 683 | 47218 | Apta / conserva | Capucho gris claro y ojo pálido, pico y patas rojos; dorso gris, distinto del capucho pardo. |
| [PHOTO-021](../../app/src/main/assets/learning/photos/photo-021.webp) | `larus_dominicanus` — gaviota cocinera / *Larus dominicanus* | `21_larus_dominicanus/044.larus_dominicanus.jpg` | 1024 × 749 → 1024 × 749 | 163436 | Apta / conserva | Cabeza y vientre blancos, manto negro y pico amarillo con marca roja; adultos completos sobre roca. |
| [PHOTO-022](../../app/src/main/assets/learning/photos/photo-022.webp) | `progne_chalybea` — golondrina domestica / *Progne chalybea* | `22_progne_chalybea/013.progne_chalybea.jpg` | 768 × 1024 → 768 × 1024 | 52436 | Apta / conserva | Garganta y pecho grises continuos, vientre blanco, dorso y alas azul oscuros lustrosos y cola ahorquillada; vista de perfil. |
| [PHOTO-023](../../app/src/main/assets/learning/photos/photo-023.webp) | `hirundo_rustica` — golondrina tijerita / *Hirundo rustica* | `23_hirundo_rustica/044.hirundo_rustica.jpg` | 1024 × 768 → 1024 × 768 | 21018 | Apta / conserva | Frente y garganta rojizas, collar oscuro y timoneras externas largas; ave posada. |
| [PHOTO-024](../../app/src/main/assets/learning/photos/photo-024.webp) | `tigrisoma_lineatum` — hoco colorado / *Tigrisoma lineatum* | `24_tigrisoma_lineatum/013.tigrisoma_lineatum.jpg` | 1024 × 768 → 1024 × 768 | 213232 | Apta / conserva | Cabeza y cuello castaño rojizo, garganta estriada y dorso finamente vermiculado; pico largo. |
| [PHOTO-025](../../app/src/main/assets/learning/photos/photo-025.webp) | `aramides_ypecaha` — ipacaa / *Aramides ypecaha* | `25_aramides_ypecaha/029.aramides_ypecaha.jpg` | 1024 × 680 → 1024 × 680 | 186372 | Apta / conserva | Cuello gris, pecho castaño, vientre oscuro, patas rojizas y pico amarillento con base rojiza. |
| [PHOTO-026](../../app/src/main/assets/learning/photos/photo-026.webp) | `jacana_jacana` — jacana / *Jacana jacana* | `26_jacana_jacana/044.jacana_jacana.jpg` | 1000 × 800 → 1000 × 800 | 94890 | Apta / conserva | Escudete rojo, pico amarillo, cabeza negra, dorso castaño y dedos muy largos visibles. |
| [PHOTO-027](../../app/src/main/assets/learning/photos/photo-027.webp) | `rollandia_rolland` — maca comun / *Rollandia rolland* | `27_rollandia_rolland/029.rollandia_rolland.jpg` | 922 × 1024 → 922 × 1024 | 129862 | Apta / conserva | Cabeza negra con penacho auricular blanco contrastado y flancos castaños; perfil nadando. |
| [PHOTO-028](../../app/src/main/assets/learning/photos/photo-028.webp) | `podiceps_major` — maca grande / *Podiceps major* | `28_podiceps_major/029.podiceps_major.jpg` | 874 × 1024 → 874 × 1024 | 106644 | Apta / conserva | Cuello largo castaño, cabeza oscura y pico largo recto; silueta de macá grande de perfil. |
| [PHOTO-029](../../app/src/main/assets/learning/photos/photo-029.webp) | `podilymbus_podiceps` — maca pico grueso / *Podilymbus podiceps* | `29_podilymbus_podiceps/020.podilymbus_podiceps.jpg` | 1024 × 639 → 1024 × 639 | 103834 | Apta / conserva | Pico corto grueso blanquecino con banda negra, ojo con anillo claro y cuerpo compacto pardo. |
| [PHOTO-030](../../app/src/main/assets/learning/photos/photo-030.webp) | `anas_flavirostris` — pato barcino / *Anas flavirostris* | `30_anas_flavirostris/059.anas_flavirostris.jpg` | 1024 × 768 → 1024 × 768 | 28014 | Apta / conserva | Cabeza parda oscura contrastada con cuerpo más claro y moteado, pico amarillo corto con punta negra. |
| [PHOTO-031](../../app/src/main/assets/learning/photos/photo-031.webp) | `anas_versicolor` — pato capuchino / *Anas versicolor* | `31_anas_versicolor/029.anas_versicolor.jpg` | 1024 × 768 → 1024 × 768 | 88016 | Apta / conserva | Corona oscura, mejillas claras y pico azulado con amarillo basal y borde negro; cuerpo punteado. |
| [PHOTO-032](../../app/src/main/assets/learning/photos/photo-032.webp) | `anas_cyanoptera` — pato colorado / *Anas cyanoptera* | `32_anas_cyanoptera_macho/044.anas_cyanoptera_macho.jpg` | 1024 × 768 → 1024 × 768 | 103696 | Apta / conserva | Macho de cuerpo castaño rojizo e iris rojizo, pico negro alargado; perfil nadando. |
| [PHOTO-033](../../app/src/main/assets/learning/photos/photo-033.webp) | `anas_platalea` — pato cuchara / *Anas platalea* | `33_anas_platalea_macho/020.anas_platalea_macho.jpg` | 712 × 686 → 712 × 686 | 163222 | Apta / conserva | Cabeza clara e iris pálido, pico negro espatulado y cuerpo rojizo densamente punteado de negro. |
| [PHOTO-034](../../app/src/main/assets/learning/photos/photo-034.webp) | `amazonetta_brasiliensis` — pato cutiri / *Amazonetta brasiliensis* | `34_amazonetta_brasiliensis_macho/044.amazonetta_brasiliensis_macho.jpg` | 1200 × 800 → 1024 × 683 | 13530 | Apta / conserva | Pico rojo, cabeza parda y espejuelo alar verde azulado brillante; vuelo de perfil. |
| [PHOTO-035](../../app/src/main/assets/learning/photos/photo-035.webp) | `callonetta_leucophrys` — pato de collar / *Callonetta leucophrys* | `35_callonetta_leucophrys_macho/029.callonetta_leucophrys_macho.jpg` | 1200 × 800 → 1024 × 683 | 88756 | Apta / conserva | Nuca y collar negros, cabeza clara, pecho rosado punteado, dorso castaño y parche blanco en flanco. |
| [PHOTO-036](../../app/src/main/assets/learning/photos/photo-036.webp) | `anas_bahamensis` — pato gargantilla / *Anas bahamensis* | `36_anas_bahamensis/044.anas_bahamensis.jpg` | 1024 × 683 → 1024 × 683 | 112608 | Apta / conserva | Mejillas y garganta blancas, corona parda y pico gris con base roja; alas abiertas. |
| [PHOTO-037](../../app/src/main/assets/learning/photos/photo-037.webp) | `anas_georgica` — pato maicero / *Anas georgica* | `37_anas_georgica/029.anas_georgica.jpg` | 1024 × 753 → 1024 × 753 | 94764 | Apta / conserva | Pico amarillo largo con línea negra, cabeza parda clara y cuerpo escamado; cola aguzada. |
| [PHOTO-038](../../app/src/main/assets/learning/photos/photo-038.webp) | `anas_sibilatrix` — pato overo / *Anas sibilatrix* | `38_anas_sibilatrix/055.anas_sibilatrix.jpg` | 1024 × 768 → 1024 × 768 | 32340 | Apta / conserva | Frente y mejilla blancas, nuca verde iridiscente, pecho oscuro, flancos canela y escapulares negras bordeadas de blanco. |
| [PHOTO-039](../../app/src/main/assets/learning/photos/photo-039.webp) | `netta_peposaca` — pato picazo / *Netta peposaca* | `39_netta_peposaca_macho/029.netta_peposaca_macho.jpg` | 1024 × 768 → 1024 × 768 | 54952 | Apta / conserva | Cabeza y cuello negros, pico rosado con gran carúncula roja; flancos grisáceos. |
| [PHOTO-040](../../app/src/main/assets/learning/photos/photo-040.webp) | `dendrocygna_bicolor` — pato siriri colorado / *Dendrocygna bicolor* | `40_dendrocygna_bicolor/029.dendrocygna_bicolor.jpg` | 1024 × 829 → 1024 × 829 | 33878 | Apta / conserva | Cabeza y cuello canela, pico gris y largas estrías claras en flancos; sin cara blanca. |
| [PHOTO-041](../../app/src/main/assets/learning/photos/photo-041.webp) | `dendrocygna_viduata` — pato siriri pampa / *Dendrocygna viduata* | `41_dendrocygna_viduata/044.dendrocygna_viduata.jpg` | 1024 × 683 → 1024 × 683 | 100026 | Apta / conserva | Cara blanca, nuca negra y cuello castaño rojizo; patas largas y flancos vermiculados. |
| [PHOTO-042](../../app/src/main/assets/learning/photos/photo-042.webp) | `oxyura_vittata` — pato zambullidor chico / *Oxyura vittata* | `49_oxyura_vittata_hembra/050.oxyura_vittata_hembra.jpg` | 1200 × 751 → 1024 × 641 | 26862 | Apta / conserva | Hembra: franja subocular y garganta claras muy contrastadas, cuerpo pardo barrado y pico oscuro relativamente fino. |
| [PHOTO-043](../../app/src/main/assets/learning/photos/photo-043.webp) | `gallinula_galeata` — pollona negra / *Gallinula galeata* | `44_gallinula_galeata/029.gallinula_galeata.jpg` | 1200 × 900 → 1024 × 768 | 50736 | Apta / conserva | Escudete rojo con pico de punta amarilla, flancos con línea blanca y subcaudales blancas. |
| [PHOTO-044](../../app/src/main/assets/learning/photos/photo-044.webp) | `vanellus_chilensis` — tero comun / *Vanellus chilensis* | `45_vanellus_chilensis/029.vanellus_chilensis.jpg` | 1024 × 768 → 1024 × 768 | 153786 | Apta / conserva | Pecho negro, copete, ojo rojo, dorso gris y bronceado y patas rosadas; ave completa. |
| [PHOTO-045](../../app/src/main/assets/learning/photos/photo-045.webp) | `himantopus_mexicanus` — tero real / *Himantopus mexicanus* | `46_himantopus_mexicanus/013.himantopus_mexicanus.jpg` | 1024 × 768 → 1024 × 768 | 24764 | Apta / conserva | Patas rosadas muy largas, pico negro fino, dorso negro y cuello blanco con nuca oscura. |

## Procedencia y créditos

La procedencia comprobable es la colección local suministrada. Los archivos seleccionados no contienen campos EXIF de autor, copyright o descripción. Las únicas firmas o marcas legibles detectadas en las seleccionadas son las indicadas debajo; se conservaron en las copias. Para las demás no se atribuye autor ni URL de publicación original a partir del nombre de carpeta. No consta una licencia individual en estos archivos; esta revisión documenta procedencia e identificación, sin inferir licencias de redistribución.

| Foto | Crédito que puede leerse en el original y la copia |
| --- | --- |
| PHOTO-009 | Lynn Marie © 2018 (firma visible) |
| PHOTO-020 | M. A. Casado (firma visible) |
| PHOTO-026 | NATURFOTO.cz y firma conservada en la imagen |

## Candidatas dudosas o menos informativas que no se publican

Estos descartes no cambian las etiquetas ni los archivos del dataset, y no pretenden revisar el conjunto completo de 3.500 imágenes.

| Origen relativo | Resultado de la inspección y decisión |
| --- | --- |
| `11_phoenicopterus_chilensis/059.phoenicopterus_chilensis.jpg` | Ave pálida en vuelo: los colores de pico y patas no se valoran con suficiente seguridad frente a otros flamencos sin localidad o edad. Identificación no validada para la tarjeta; se reemplazó por `020`, con la extensa mitad negra del pico y base blanco rosada visibles. |
| `22_progne_chalybea/020.progne_chalybea.jpg` | Vista casi frontal y pecho muy iluminado: por sí sola deja más duda frente a otras golondrinas del género. No se usa como identificación validada; se eligió `013`, de perfil, con garganta y pecho grises, manto azul oscuro y vientre blanco. |
| `33_anas_platalea_macho/059.anas_platalea_macho.jpg` | Vista frontal y sombras que dificultan valorar el patrón de cabeza y moteado; no se da por identificada por su carpeta ni se reasigna a otra especie. Se publica `020`, con pico espatulado, iris claro y moteado corporal visibles. |
| `38_anas_sibilatrix/065.anas_sibilatrix.jpg` | Plumaje menos típico, postura frontal y otras aves al lado; no se determina si la apariencia se debe a edad, plumaje u otra causa. No se valida esa identificación para publicación. Se eligió `055`, con nuca verde, cara blanca y flancos canela claramente visibles. |
| `42_oxyura_vittata_macho/029.oxyura_vittata_macho.jpg` y `044.oxyura_vittata_macho.jpg` en la misma carpeta | Cabeza negra y pico azul son compartidos con otros zambullidores. No bastan para validar estas candidatas sin más rasgos o contexto. Se eligió la hembra `49_oxyura_vittata_hembra/050.oxyura_vittata_hembra.jpg`, con franjas faciales contrastadas y barrado corporal. |

Las fotos finalmente incluidas tienen los rasgos aceptados descritos en el registro individual. Las dudas anteriores permanecen explícitas y excluidas del manifiesto; ninguna se resolvió únicamente por el rótulo de carpeta.

## Referencias usadas para contrastar rasgos

Consultadas durante la revisión del 19 de octubre de 2026. No se descargaron ni incorporaron fotografías de estas páginas; se usaron sus descripciones para contrastar las fotos locales.

- Cuervillos: [Phimosus infuscatus — eBird/Merlin](https://ebird.org/species/bafibi1) y [Plegadis chihi — Cornell Lab](https://www.allaboutbirds.org/guide/white-faced_ibis/id): cara desnuda/pico rosados frente a iris rojo y pico gris, respectivamente.
- Gallaretas: [Fulica leucoptera](https://ebird.org/species/whwcoo1), [Fulica armillata](https://ebird.org/species/regcoo1) y [Fulica rufifrons](https://ebird.org/species/refcoo1), eBird/Merlin: tamaño y color del escudete y patrón de la base del pico.
- Pato cuchara: [Spatula platalea — eBird/Merlin](https://ebird.org/species/redsho1): pico negro grande, cabeza clara, iris pálido y cuerpo rojizo moteado; se mantiene la clave `anas_platalea`.
- Golondrina doméstica: [observación y descripción de Progne chalybea — Mexican Birding](https://www.mexicanbirding.com/gray-breasted-martin/): garganta y pecho grises, además de valorar forma y color en la foto local.
- Zambullidor chico: [Oxyura vittata — SIB/APN](https://sib.gob.ar/especies/oxyura-vittata), [descripción de la UNL](https://www.fcv.unl.edu.ar/aves/pato-zambullidor-chico/) y [Lake Duck — eBird/Merlin](https://ebird.org/species/lakduc1): dificultad de los machos y franjas faciales/barrado de la hembra.
- Flamenco austral: [Phoenicopterus chilensis — SIB/APN](https://sib.gob.ar/especies/phoenicopterus-chilensis): pico curvado negro con base clara; se descartó la vista juvenil en vuelo para evitar inferir la especie por su carpeta.

## Huellas SHA-256

Permiten localizar exactamente el archivo de origen y comprobar que la copia es la revisada. No son una prueba de identificación biológica.

| ID | Original | WebP |
| --- | --- | --- |
| PHOTO-001 | `5ed4985e792409ec703130663b9b9c9d67998bc2a2b6058e4f3f9d4d94834512` | `f404ad2cd7e870c3e4c2d5ea3b638a95c4b5ce4d9650961113227e8160fca3c1` |
| PHOTO-002 | `dac89e5382f1ec645decaa9345022d3a3c46290b47ec9be3ed886961eed24731` | `84dc36d77124057e69a1bceff675dac7cf42a80e408d745fe6e62700047f53d0` |
| PHOTO-003 | `4c517011ff3ff06c3821193763f80c1b537eadd038f6ebaaf7298684ba21099a` | `351280d35abd2bce82be6352dc9095dc8fd98585935ea286eeba91c9939e16a7` |
| PHOTO-004 | `a411a45727c2a6d0d26b57f0513a9a5de6348fbaffe3a2e0d6d0d03d54927a8e` | `17a17b35e33bea6f962d2679310e7e8e22fcf0e04dd2fc54005751bf6c57fe74` |
| PHOTO-005 | `1d012e599cbe2a9e5f39b44a3cbd4becd080a1cd78185c8daaecf024dc439cfe` | `7e60bb64a592ac5738e81bea0c8dada3983d1584d993d718c7fd149402932dc3` |
| PHOTO-006 | `114a3657704773e73d525d8c91937ad494848267db1a2199aa90a1d7bee4120c` | `a2f691f86f554ddc5b0e8c09a01d5a6f75e79f57f017478a5ad31c423ff1fed5` |
| PHOTO-007 | `fb6f89737fc9018ed3acf0660d3ce0eb6efdfdab91b39b1576abba83ad817690` | `503f68128f56f9d794e50c618f7f3acb1f9b3c018c29f04d475e157dbbe3fc17` |
| PHOTO-008 | `3ad08bad5dd0459a096b849a3168f462d6604c85dd6e071d710087f429e4cdfa` | `495e4aeaf7cfe56ceb23c0142c91513e16bbc150303719d98af2309d44a1d898` |
| PHOTO-009 | `ac627313fe204781abb0f76a8dd78ca2b237f70cee8ef38d1e2e8d8eb1c6b117` | `6111371f47943eaee146aad46295a2c8798143504207f748f639a0ac09773ed8` |
| PHOTO-010 | `11de381a65a79670542cd25a417ed065b32fc48b659c7a226880e07e75ec0cdf` | `f458621a61a06599b621d7c5cfe3b79a57aff4a50cbe7cb825d078c26e5f6100` |
| PHOTO-011 | `a2477d07c843fb2589e547f17041a827c6950e9d5528bb0770a08b12b5317d46` | `463e493c84019b8ef5bef194a63c824141a3dbc348bbf0788c8b901c9f88137e` |
| PHOTO-012 | `814a8767d90dc3e381e03e67b3558ca0092463a52b8574e098bff47482e43e8e` | `43cc125def15fa42275f8b8b14fbc7e1226b7826bb313ff25605fc5939b5c609` |
| PHOTO-013 | `be76e150a0eb1ba5df3928767e43a3c22a49775d33ce046b19e3264ff2a1082b` | `0357eafc0b1b6e2155892f419a2233eb16b7373c0b48919369ba5c4d4d5aec3d` |
| PHOTO-014 | `fa7fa54660b6fe57e6456c5cf1aa5ff5cd5fa53620a95e66a0d5abd279b3b67c` | `fcaec4a7d3639e9d6d7826cb804050292ec97311d3185729308328349e3a310c` |
| PHOTO-015 | `d1df1b30ee5bac8f1c677b6c5c75e2bc28b6ea247764be1c21f7941d2951a315` | `d7572c0cdf2f0405a93f16987296c4a73e26eea7da6f613061f8cab05d53c72f` |
| PHOTO-016 | `f94554020f79f52cbc215d9d308c34728693d497f2b7d9104dc44dc96eec454e` | `e0225eb7c2b505ac6bd50503a009f7c0ee5d716561407364c74ec6709125e49b` |
| PHOTO-017 | `e13f6db5927e51efda5dc317479d257faa69a30f548a0d8dfd15b5969da5269c` | `e20e246bde3be97aebcc85c19d5a453ac628f980c9429b08b9fc09f47d7f3d0b` |
| PHOTO-018 | `2cebb4fecbb6ad64832c04b2cd9db09edd5f8dc474f478f0a3dcd091d8d537cd` | `8cfe5761e451c689037d6e8ff5a6c29ed9e0193144fe22d20500c79337ba6b6f` |
| PHOTO-019 | `cc5f00e0ed37f5252f54865bae11a31172839245d9f2ba53a2002103dbb5a0c8` | `3597af0c776cfc948363942a28798dbe06ad14d51baaae19f88a35d60803cfda` |
| PHOTO-020 | `3abb0974c2859747607cce9418342f303cde12319ff8819581f3bd1a28e3bafa` | `dd6f77526f53878f6f6d57a7922f4c210e0567a263baeae722660bdf3d8e11eb` |
| PHOTO-021 | `365f8915626409d0d2f75861b34ced71789efcd89833815365d757b19bd09e9c` | `cba3e544a27a02703eb76d8f4faeec05edaa5e1a978d3d02f28d74363e7d121a` |
| PHOTO-022 | `8bef3018b85c3b068f760b0f45c3759d8b16da4c3ef7769a8cc2b464575715e5` | `f1dc8d0c8ba4aab157c1f3fe0ae3c2e249ec4764a8ab19d376ef1c2ace62859e` |
| PHOTO-023 | `7228e85e44ec879659be4ec2f8d692a9c414a4350c207bc95634017d8065a79a` | `16a0ec3a7c2056e299883a96cd8a5b7b86f1eb1d9ee91b6cc4da41ba6c56836a` |
| PHOTO-024 | `e40cb4074e84c72c8d66e30f33b55ecf35337cbcb74662651d7d34fbbf2f316e` | `a918c85bf6f9d32c87f184573c3f1d4b86f1f506dc0bb67166f9e1af6921c19d` |
| PHOTO-025 | `c2ea7db35e3d73538551c7f413af39b6c7aa1a71fd37506fa2fc5cdf638d5ba5` | `3230a0bd493c1b468002a25b9a7b0c6601f1015a24f4492997bfe2746e3da2db` |
| PHOTO-026 | `57ced9afc2aae09e77c8e2bd9b62cdad27451db43632a4569f062685e2cbe020` | `7dd9e1478644798c64b92e506867fe2b6146ad29d58f1b4d333fbd13107f4abf` |
| PHOTO-027 | `fd8d6ce2471a1d15208cc811a6d51a179adc406b7ffcc154edc8a5f24a414c52` | `d0d0a99434a76e2da7cdba305191f94a870716126fc3dbac12d985dc49f3a2ae` |
| PHOTO-028 | `169e062c30f4dd4f386222b4fbf695b4df64fada44343ba3d5b5c453f2ef5d1c` | `c8b6686a3026aa668c3aabc0ba4522f29d1a93bd6ed02c473921031cfc506a0b` |
| PHOTO-029 | `d55f902caaff7134c2440cb008660e011bef355b60ae8bc227ff16a1355bf773` | `70727c52ab1f0358e48b1dfe0c9bc06ddd71772dfd0fd0a69b57970a336e472c` |
| PHOTO-030 | `6078b920f4eb89fdb2ef1a2d369d98f9da79c3aee16ac4dcb657fa81b2bd3a5f` | `0e06670fdffa98b9cbf33dbe1a49f9149fa4456930d44b9a1b000e939afabaca` |
| PHOTO-031 | `7e6bc8f4f6088dff34183c67abdc3c0feb948ead085340a3cecc985672b11a38` | `f5292da3c10ede7c5d280acd39a1bbad1558998a54f07ee891e8b69e20e5d776` |
| PHOTO-032 | `f1e34dd2c62a5e0a8e43e49be0cb9bb8766d3fb8dc91231ef8d01aa1911cf90c` | `fa345c2c7380b56977a85334cabbdecb8dffc666f4a2e5613c719038e3775d93` |
| PHOTO-033 | `835855ff4403d3d45bc4ee54839093f0f9571bc9fc50b8ad0c7f7c33d78586f8` | `cde664e753ac416cb28c5101fd3cc6eee0d485ca5e6da492fd0b51b2baa8fbd4` |
| PHOTO-034 | `8b274cc2aa4aaefcc1bb716aa91e51c0fd08b66ffe690e8950d61717ca943d3f` | `d81965cca3ac75b494d48d7a91493ec6a0842a34997e8afa5d11d5c1f4509d4a` |
| PHOTO-035 | `ddbe3ad8eeaa8250e57728f48f1909a281a1de36853a3c55f10cdd1c627cf47f` | `75b5d2ffcd1ed6ab57656c9419634008ddde0f24ed77a79fe148bf401c443007` |
| PHOTO-036 | `3a1ff4eb49fcfbc097e073b1dbb430b13f01792d462e18250e38620d2ea05dab` | `89f765856ec0be8aacce7305254e71e4a19f2de4fb876bc6c35e916c09a721f4` |
| PHOTO-037 | `19ba571d2b2b139e47396b071eb6a59e31dcd440b79660dda6259980ceb2124e` | `f524354198ff9398974ad19cd179a42ffa3ac2c77420ab78d1676d4c1c7e83b2` |
| PHOTO-038 | `7b790ea73b10032aa33b4d3bb8e81be8384b9e82c28ad9dea4f974efa5d590a4` | `295c0ec30204048327740c37e59b3b6cbf323c1a793cb30f816869efeea4a531` |
| PHOTO-039 | `6cf4ac3f64c6e23a4045702fa53009c6bbeec8ad2cda94538eaa80073eb6fc4f` | `127f986baf98f87a2be8d989a0147049620e6c50c0f425d0344b809e288a93c2` |
| PHOTO-040 | `f83b34d5dbf40394c4b4c16f86e55d9fbdd59f5b03e62f0657f72c545da4fb2e` | `67d2dd75a7080f594c549e7dcb85543832af65f68d1f169e0070a2870c612515` |
| PHOTO-041 | `33c6f337115105b375033d2ed973fdbe3b2b4802f867915c9c68383e857480a1` | `a1a18bb761ad678f1be46e2a9fc9e84f828b59572dbd56c1715218b85def7ae5` |
| PHOTO-042 | `778281b1d2559eac2f10d7ebf67efcac254ceeb275acae8140bc42d48ee6dc7e` | `1bcd3a1a5019c6f03e3fdb16ff91f085faead6306c4e272007d019c6aadde57a` |
| PHOTO-043 | `000ddf74fd11fa2b2a94d5dc2973f3786f5c2e81c4032ba31a6e19fb4961a88b` | `15c77a0200ee45106102d0817401d004e23ad5fb44604eee32955a3ef98f54fe` |
| PHOTO-044 | `e863782da78b82b113a61afc62316395f574d3e03ac29450965cbae906d44396` | `807f7f1c7c6a9c7cbd8ccb462a5d6c4dae94cd4089f836057d2e55afde631c9f` |
| PHOTO-045 | `24d0620457a6686d5937409b2460e6250b8d75dd698db1e3878798fc01777571` | `a8b354c986b25240d683b80f19d548e63d7bef8e44b286aa3c4f2f391fb40e7e` |

## Comprobaciones de T4

Resultado: material preparado y revisado; **validación integrada pendiente**, por lo que T4 permanece sin marcar como completada en [tasks.md](../../specs/001-aprender-jugando/tasks.md).

- Antes de T4: comparación exacta de las 40 preguntas con el banco y **17 pruebas Android de T3 aprobadas** en el emulador API 36.1. El comando oficial ya presentaba el bloqueo Room/Kotlin descrito debajo.
- Prueba nueva del paquete: primero falló con `expected:<45> but was:<0>` antes de incorporar las fotos (18 tests, un fallo). Con el paquete final, **18 pruebas Android aprobadas, cero fallos/errores**, incluidas las 40 preguntas, 45 especies distintas, nombres, manifiesto, archivos, decodificación completa WebP, dimensiones y selección de una ronda sin repetir especies.
- **30 pruebas unitarias aprobadas** en el mismo verificador aislado (29 de reglas de sesión y una existente).
- Comprobación de archivos aprobada: cobertura exacta, dimensiones y ausencia de ampliación, hashes de copias, tamaños de las tablas y enlaces relativos. Los **3.500 originales mantienen nombres, bytes y SHA-256**, y las huellas previas de catálogo, etiquetas, cargador, modelos, sesión y preguntas no cambiaron.
- El comando oficial `.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.avesargentinas.ui.learning.LearningContentLoaderTest' --stacktrace` y la suite oficial `:app:testDebugUnitTest` fallan **antes de ejecutar tests**, en `:app:kaptDebugKotlin`, por la incompatibilidad previa de Room 2.6.1 con metadatos Kotlin 2.2.0 (máximo admitido: 2.0.0). No se modificaron sus dependencias.
- La evidencia aprobada proviene del verificador Android temporal de T4, que reutiliza la configuración de T3, compila copias iguales de los fuentes y carga estos mismos assets con sus pruebas originales en `Medium_Phone_API_36.1`, Android 16 / API 36.1. No demuestra que compile la app completa. Pendiente resolver el bloqueo y repetir el comando oficial.
- Evidencia local ignorada: `app/build/t4-verification/`, con logs/XML de las fases roja y verde, comparación del banco, 15 planchas original/WebP, registro de exportación, inventario y verificador de hashes. No se incorporan esos archivos generados al control de versiones.

El trabajo se detiene en T4, sin iniciar T5, crear rama/worktree ni hacer commits o push.
