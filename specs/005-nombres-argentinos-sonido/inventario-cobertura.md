# Inventario de cobertura y correspondencias — Spec 005

Estado: Completada (Tarea T2).  
Fecha: 28 de octubre de 2026.  
Referencias: [Spec 005](spec.md) (RF-2, RF-4, RF-5), [Plan técnico](plan.md) (Sección 2), [Tareas](tasks.md) (T2), [Evaluación de fuentes](evaluacion-fuentes.md) (T1).  
Herramienta de reproducción: `tools/inventory_species_coverage.py` y pruebas en `tools/test_inventory_species_coverage.py`.

---

## 1. Verificación de dependencias previas

De acuerdo con las reglas de ejecución de la tarea T2:
1. **Verificación de T1:** La tarea T1 («Evaluar fuentes de especies y nombres») se encuentra completada y aprobada, con su informe registrado en `evaluacion-fuentes.md`.
2. **Disponibilidad de fuentes adecuadas:**
   - **Lista oficial nacional de referencia:** Catálogo oficial conjunto del Ministerio de Ambiente y Desarrollo Sustentable de la Nación (MAyDS) y Aves Argentinas (*Categorización de las Aves de la Argentina según su estado de conservación*, 2017), complementada con la *Monografía Nº 12* de Aves Argentinas (Monteleone y Pagano, 2022). Totaliza **1.043 especies evaluadas** (1.033 silvestres nativas en el Apéndice 1 y 10 exóticas introducidas con nidificación silvestre documentada en el Apéndice 3).
   - **Taxonomía y nombres eBird Argentina (`es_AR`):** Clements Checklist / eBird Common Names Matrix (v2025/2026), coordinada localmente por eBird Argentina.
   - **Evidencia provincial de Buenos Aires:** *«Lista actualizada de las aves de la provincia de Buenos Aires»* (Darrieu et al., 2013, Fundación Azara / UNLP SEDICI, licenciada bajo **CC BY-NC-SA 4.0**), combinada con el catálogo preexistente de la aplicación (`app/src/main/assets/labels_map.json`, 46 especies bonaerenses prioritarias).
   - **Catálogo del modelo BirdNET+:** `app/src/main/assets/birdnet/species.json` (9.789 clases, 8.927 de clase *Aves*, SHA-256 `5e3598bdbc08a0684ba4edc47e7760c44515ce6febb6e5cfd0b0668f778244b7`, licencia MIT).

Las dependencias se encuentran plenamente satisfechas y sin bloqueos.

---

## 2. Resumen cuantitativo de cobertura

El cotejo reproducible entre la lista oficial nacional de referencia y el catálogo del modelo `species.json` arroja las siguientes cifras exactas:

| Categoría de correspondencia | Cantidad de especies | Porcentaje sobre lista nacional (1.043) | Estado y tratamiento técnico |
| :--- | :---: | :---: | :--- |
| **Coincidencias exactas directas** | **882** | **84,6%** | El binomio científico o canónico coincide de forma idéntica entre la lista de referencia y `species.json`. |
| **Equivalencias taxonómicas verificadas** | **108** | **10,4%** | Discrepancias sistemáticas documentadas (segregación de géneros, sinonimias SACC/Clements o variantes ortográficas) resueltas hacia una clase unívoca del modelo. |
| **TOTAL COBERTURA EFECTIVA EN EL MODELO** | **990** | **94,9%** | **Especies argentinas reconocibles por el modelo que contarán con correspondencia de nombre.** |
| **Especies argentinas ausentes del modelo** | **53** | **5,1%** | Especies de la avifauna argentina no incluidas en la poda global de 10.000 clases de BirdNET+. **No se prometerá su detección ni se crearán entradas huérfanas.** |
| **Total lista nacional de referencia** | **1.043** | **100,0%** | 1.033 especies nativas silvestres + 10 introducidas con nidificación silvestre documentada. |

Adicionalmente, el catálogo local preexistente en la app (`labels_map.json`, 46 especies de Buenos Aires) alcanza un **100% de cobertura** en el modelo (41 coincidencias directas y 5 resueltas por equivalencia genérica verificada).

---

## 3. Respaldo de prioridad para Buenos Aires (RF-2 vs RF-3)

La Spec 005 exige formalmente en RF-2 y RF-3:
- Priorizar la denominación tradicional de la provincia de Buenos Aires cuando exista evidencia verificable.
- En caso contrario, utilizar el nombre nacional argentino verificado.
- No suponer que la lista nacional ni la variante `es_AR` constituyan evidencia provincial exclusiva.

### 3.1. Criterios de diferenciación provincial
1. **Evidencia bonaerense verificada (Nivel 1):** Proviene de Darrieu et al. (2013) y de las 46 especies pampeanas de `labels_map.json`.
   - Se caracteriza por nombres vernáculos arraigados en la cultura rioplatense y pampeana:
     - Uso sistemático de **«Pato»** frente a modismos extranjeros: *Pato capuchino* (*Spatula versicolor*), *Pato colorado* (*Spatula cyanoptera*), *Pato cuchara* (*Spatula platalea*), *Pato overo* (*Mareca sibilatrix*), *Pato barcino* (*Anas flavirostris*), *Pato maicero* (*Anas georgica*).
     - Nombres tradicionales simplificados: **«Tero»** / **«Tero común»** (*Vanellus chilensis*, rechazando «Avefría Tero»), **«Hornero»** (*Furnarius rufus*), **«Benteveo»** / **«Benteveo común»** (*Pitangus sulphuratus*), **«Chingolo»** (*Zonotrichia capensis*), **«Torcaza»** (*Zenaida auriculata*), **«Carancho»** (*Caracara plancus*), **«Chimango»** (*Daptrius chimango*), **«Cotorra»** (*Myiopsitta monachus*), **«Jilguero dorado»** (*Sicalis flaveola*), **«Ratona común»** (*Troglodytes aedon* / *musculus*), **«Lechucita vizcachera»** (*Athene cunicularia*).
2. **Alcance nacional general (Nivel 2):** Especies sin presencia regular en la provincia de Buenos Aires (aves exclusivas de la Selva Paranaense de Misiones, Yungas del NOA, Chaco seco, Altos Andes, Monte cuyano, Bosques Subantárticos patagónicos o aguas pelágicas).
   - Estas especies (~630 a 650 taxones) no habitan en Buenos Aires y carecen de tradición vernácula pampeana. Reciben el nombre nacional verificado de la lista oficial del CARO y de eBird `es_AR`, evitando atribuciones geográficas ficticias.

---

## 4. Equivalencias taxonómicas verificadas (108 especies)

Las 108 discrepancias taxonómicas entre la lista oficial argentina y `species.json` responden a actualizaciones sistemáticas de la ornitología neotropical (comités SACC/AOS e IOC, reflejados en Clements Checklist y BirdNET), así como a erratas tipográficas y variantes ortográficas menores en los catálogos impresos:

### 4.1. Principales grupos de revisión taxonómica

1. **Anatidae (Segregación de *Anas* en *Spatula* y *Mareca*):**
   - *Anas sibilatrix* -> `Mareca sibilatrix` (idx: 470) — *Pato overo*
   - *Anas versicolor* -> `Spatula versicolor` (idx: 8520) — *Pato capuchino*
   - *Anas puna* -> `Spatula puna` (idx: 8518) — *Pato puneño*
   - *Anas discors* -> `Spatula discors` (idx: 8514) — *Pato media luna*
   - *Anas cyanoptera* -> `Spatula cyanoptera` (idx: 451) — *Pato colorado*
   - *Anas platalea* -> `Spatula platalea` (idx: 465) — *Pato cuchara*
2. **Phalacrocoracidae (Segregación de *Phalacrocorax* en *Nannopterum*, *Leucocarbo* y *Poikilocarbo*):**
   - *Phalacrocorax brasilianus* -> `Nannopterum brasilianum` (idx: 5946) — *Biguá*
   - *Phalacrocorax gaimardi* -> `Poikilocarbo gaimardi` (idx: 7268) — *Cormorán gris*
   - *Phalacocorax magallanicus* -> `Leucocarbo magellanicus` (idx: 4940) — *Cormorán cuello negro*
3. **Falconidae (Fusión sistemática en *Daptrius*):**
   - *Milvago chimango* -> `Daptrius chimango` (idx: 2924) — *Chimango*
   - *Milvago chimachima* -> `Daptrius chimachima` (idx: 2923) — *Chimachima*
   - *Phalcoboenus megalopterus* -> `Daptrius megalopterus` (idx: 2925) — *Matamico andino*
   - *Phalcoboenus australis* -> `Daptrius australis` (idx: 2921) — *Matamico grande*
4. **Charadriiformes (Segregaciones a *Anarhynchus*, *Calidris*, *Zonibyx* y *Hoploxypterus*):**
   - *Charadrius falklandicus* -> `Anarhynchus falklandicus` (idx: 409) — *Chorlito doble collar*
   - *Charadrius collaris* -> `Anarhynchus collaris` (idx: 408) — *Chorlito de collar*
   - *Charadrius alticola* -> `Anarhynchus alticola` (idx: 406) — *Chorlito puneño*
   - *Charadrius modestus* -> `Zonibyx modestus` (idx: 1875) — *Chorlito pecho canela*
   - *Charadrius mongolus* -> `Anarhynchus mongolus` (idx: 412) — *Chorlito mongol*
   - *Aphriza virgata* -> `Calidris virgata` (idx: 1428) — *Playero de rompiente*
   - *Tryngites subruficollis* -> `Calidris subruficollis` (idx: 1426) — *Playerito canela*
   - *Vanellus cayanus* -> `Hoploxypterus cayanus` (idx: 4410) — *Chorlo de espolón*
5. **Thraupidae y Emberizoideos (Segregaciones de *Phrygilus*, *Poospiza*, *Tangara*, *Tiaris*):**
   - *Phrygilus fruticeti* -> `Rhopospina fruticeti` (idx: 8122) — *Yal negro*
   - *Phrygilus unicolor* -> `Geospizopsis unicolor` (idx: 3925) — *Yal plomizo*
   - *Phrygilus carbonarius* -> `Rhopospina carbonaria` (idx: 7447) — *Yal carbonero*
   - *Phrygilus alaudinus* -> `Rhopospina alaudina` (idx: 7445) — *Yal platero*
   - *Phrygilus plebejus* -> `Geospizopsis plebejus` (idx: 3924) — *Yal chico*
   - *Phrygilus dorsalis* -> `Idiopsar dorsalis` (idx: 4560) — *Comesebo puneño*
   - *Poospiza erythrophrys* -> `Microspingus erythrophrys` (idx: 5543) — *Monterita ceja rojiza*
   - *Poospiza lateralis* -> `Microspingus lateralis` (idx: 5545) — *Monterita litoral*
   - *Poospiza torquata* -> `Microspingus torquatus` (idx: 5548) — *Monterita de collar*
   - *Poospiza melanoleuca* -> `Microspingus melanoleucus` (idx: 5546) — *Monterita cabeza negra*
   - *Poospiza hypochondria* -> `Poospizopsis hypocondria` (idx: 7434) — *Monterita pecho gris*
   - *Compsospiza baeri* -> `Poospiza baeri` (idx: 7429) — *Monterita serrana*
   - *Tangara preciosa* -> `Stilpnia preciosa` (idx: 8758) — *Saíra castaña*
   - *Tangara cayana* -> `Stilpnia cayana` (idx: 8746) — *Saíra pecho negro*
   - *Tiaris obscurus* -> `Asemospiza obscura` (idx: 888) — *Espiguero pardo*
   - *Tiaris fuliginosus* -> `Asemospiza fuliginosa` (idx: 887) — *Espiguero negro*
   - *Pipraeidea bonariensis* -> `Rauenia bonariensis` (idx: 7984) — *Naranjero*
   - *Pyrrhocoma ruficeps* -> `Thlypopsis pyrrhocoma` (idx: 9107) — *Pioró*
   - *Cyanocompsa brissonii* -> `Cyanoloxia brissonii` (idx: 2807) — *Reinamora grande*
   - *Saltator rufiventris* -> `Pseudosaltator rufiventris` (idx: 7578) — *Pepitero colorado*
6. **Icteridae y Fringillidae:**
   - *Sturnella superciliaris* -> `Leistes superciliaris` (idx: 4867) — *Pecho colorado*
   - *Sturnella defilippii* -> `Leistes defilippii` (idx: 4864) — *Loica pampeana*
   - *Sturnella loyca* -> `Leistes loyca` (idx: 4866) — *Loica común*
   - *Sporagra crassirostris* -> `Spinus crassirostris` (idx: 8568) — *Cabecitanegra picudo*
   - *Sporagra magellanica* -> `Spinus magellanicus` (idx: 8573) — *Cabecitanegra común*
   - *Sporagra atrata* -> `Spinus atratus` (idx: 8563) — *Negrillo*
   - *Sporagra uropygialis* -> `Spinus uropygialis` (idx: 8585) — *Cabecitanegra andino*
   - *Sporagra barbata* -> `Spinus barbatus` (idx: 8566) — *Cabecitanegra austral*
   - *Carduelis chloris* -> `Chloris chloris` (idx: 1934) — *Verderón*
   - *Euphonia cyanocephala* -> `Chlorophonia cyanocephala` (idx: 1961) — *Tangará cabeza celeste*
7. **Tyrannidae, Cotingidae y afines:**
   - *Xolmis pyrope* -> `Pyrope pyrope` (idx: 7904) — *Diucón*
   - *Xolmis cinereus* -> `Nengetus cinereus` (idx: 6013) — *Monjita gris*
   - *Xolmis coronatus* -> `Neoxolmis coronatus` (idx: 6023) — *Monjita coronada*
   - *Xolmis dominicanus* -> `Heteroxolmis dominicana` (idx: 4323) — *Monjita dominica*
   - *Pitangus lictor* -> `Philohydor lictor` (idx: 6862) — *Benteveo de pico fino*
   - *Polioxolmis rufipennis* -> `Cnemarchus rufipennis` (idx: 2273) — *Birro gris*
   - *Phyllomyias burmeisteri* -> `Acrochordopus burmeisteri` (idx: 6941) — *Mosqueta pico curvo*
   - *Phylloscartes paulista* -> `Pogonotriccus paulista` (idx: 6966) — *Mosqueta oreja negra*
   - *Ramphotrigon megacephala* -> `Ramphotrigon megacephalum` (idx: 7977) — *Picochato cabezón*
   - *Pyrrhomyias cinnamomea* -> `Pyrrhomyias cinnamomeus` (idx: 7911) — *Birro chico*
8. **Strigiformes, Accipitriformes y Piciformes:**
   - *Ciccaba virgata* -> `Strix virgata` (idx: 8782) — *Lechuza estriada*
   - *Ciccaba huhula* -> `Strix huhula` (idx: 8769) — *Lechuza negra*
   - *Pseudoscops clamator* -> `Asio clamator` (idx: 890) — *Lechuzón orejudo*
   - *Glaucidium nanum* -> `Glaucidium nana` (idx: 3987) — *Caburé grande*
   - *Accipiter superciliosus* -> `Microspizias superciliosus` (idx: 57) — *Esparvero chico*
   - *Accipiter bicolor* -> `Astur bicolor` (idx: 30) — *Esparvero variado*
   - *Accipiter bicolor chilensis* -> `Astur chilensis` (idx: 32) — *Esparvero araucano*
   - *Picoides fumigatus* -> `Leuconotopicus fumigatus` (idx: 3152) — *Carpintero oliva oscuro*
   - *Dryocopus galeatus* -> `Celeus galeatus` (idx: 1714) — *Carpintero cara canela*
   - *Spiziapteryx circumcinctus* -> `Spiziapteryx circumcincta` (idx: 8591) — *Halconcito gris*
9. **Trochilidae y otras familias:**
   - *Amazilia chionogaster* -> `Elliotomyia chionogaster` (idx: 3293) — *Picaflor vientre blanco*
   - *Amazilia versicolor* -> `Chrysuronia versicolor` (idx: 2070) — *Picaflor esmeralda*
   - *Amazilia lactea* -> `Chionomesa lactea` (idx: 1902) — *Picaflor pecho azul*
   - *Hylocharis cyanus* -> `Chlorestes cyanus` (idx: 1927) — *Picaflor lazulita*
   - *Sappho sparganura* -> `Sappho sparganurus` (idx: 8218) — *Picaflor cometa*
   - *Sephanoides sephanoides* -> `Sephanoides sephaniodes` (idx: 8383) — *Picaflor rubí*
   - *Cranioleuca sulphurifera* -> `Limnoctites sulphuriferus` (idx: 4991) — *Curutié ocráceo*
   - *Leptasthenura yanacensis* -> `Sylviorthorhynchus yanacensis` (idx: 4886) — *Coludito ocráceo*
   - *Hylopezus nattereri* -> `Cryptopezus nattereri` (idx: 2698) — *Chululú chico*
   - *Tachycineta meyeni* -> `Tachycineta leucopyga` (idx: 8887) — *Golondrina patagónica*
   - *Cinclus schulzi* -> `Cinclus schulzii` (idx: 2125) — *Mirlo de agua*
   - *Cissopis leveriana* -> `Cissopis leverianus` (idx: 2132) — *Frutero overo*
   - *Bubulcus ibis* -> `Ardea ibis` (idx: 792) — *Garcita bueyera*
   - *Ixobrychus exilis* -> `Botaurus exilis` (idx: 1221) — *Mirasol chico*
   - *Ixobrychus involucris* -> `Botaurus involucris` (idx: 1222) — *Mirasol común*
   - *Laterallus leucopyrrhus* -> `Rufirallus leucopyrrhus` (idx: 8171) — *Burrito colorado*
   - *Porzana flaviventer* -> `Laterallus flaviventer` (idx: 4820) — *Burrito amarillo*
   - *Porzana albicollis* -> `Mustelirallus albicollis` (idx: 5731) — *Burrito grande*
   - *Neocrex erythrops* -> `Mustelirallus erythrops` (idx: 5732) — *Burrito pico rojo*
   - *Gallinula melanops* -> `Porphyriops melanops` (idx: 7443) — *Pollona pintada*
   - *Hydropsalis maculicaudus* -> `Antiurus maculicaudus` (idx: 538) — *Atajacaminos ceja blanca*
   - *Anthus lutescens* -> `Anthus chii` (idx: 531) — *Cachirla chica*
10. **Ajustes ortográficos verificados:**
    - *Crypturellus tatuapa* -> `Crypturellus tataupa` (idx: 2673)
    - *Pygoscelis adelia* -> `Pygoscelis adeliae` (idx: 7788)
    - *Thalassarche melanophrys* -> `Thalassarche melanophris` (idx: 9024)
    - *Thalasarche salvini* -> `Thalassarche salvini` (idx: 9026)
    - *Fulmarius glacialoides* -> `Fulmarus glacialoides` (idx: 3760)
    - *Gampsonyx swansonii* -> `Gampsonyx swainsonii` (idx: 3830)
    - *Dromococcyx phasianelus* -> `Dromococcyx phasianellus` (idx: 3144)
    - *Phaetornis pretrei* -> `Phaethornis pretrei` (idx: 6788)
    - *Phaetornis eurynome* -> `Phaethornis eurynome` (idx: 6777)
    - *Chlorostilbon lucidius* -> `Chlorostilbon lucidus` (idx: 1953)
    - *Ochetoprhynchus andaecola* -> `Ochetorhynchus andaecola` (idx: 6161)
    - *Piprites cholris* -> `Piprites chloris` (idx: 7155)
    - *Icterus pyrroptherus* -> `Icterus pyrrhopterus` (idx: 4554)
    - *Dryocopus schulzi* -> `Dryocopus schulzii` (idx: 3171)

---

## 5. Especies ausentes del modelo BirdNET+ (53 especies)

Las siguientes 53 especies de la lista oficial argentina **no forman parte de las 8.927 aves entrenadas e incluidas en el modelo podado BirdNET+ V3.0 (10K pruned)**.

> [!IMPORTANT]
> En cumplimiento estricto del requisito **RF-5** y de la condición de cierre de la tarea T2:
> 1. Ninguna de estas 53 especies puede ser detectada por sonido con los activos vigentes del modelo.
> 2. No se crearán entradas en el catálogo de sonido para estas especies, evitando falsas promesas de detección o mapeos erróneos a índices inexistentes.
> 3. Si en futuras versiones del modelo se incorporasen nuevas clases, podrán vincularse mediante la misma metodología aquí documentada.

### Listado clasificado de especies ausentes

1. **Aves marinas pelágicas, subantárticas y antárticas (24 especies):**
   - *Eudyptes robustus* (ID 84, Pingüino de Snares)
   - *Eudyptes sclateri* (ID 85, Pingüino crestudo)
   - *Eudyptes moseleyi* (ID 87, Pingüino de Tristán)
   - *Diomedea sanfordi* (ID 90, Albatros real del norte)
   - *Diomedea dabbenena* (ID 93, Albatros de Tristán)
   - *Phoebetria fusca* (ID 94, Albatros oscuro)
   - *Thalassarche chrysostoma* (ID 98, Albatros cabeza gris)
   - *Thalassarche steadi* (ID 99, Albatros corona blanca)
   - *Thalassoica antarctica* (ID 105, Petrel antártico)
   - *Aphrodroma brevirostris* (ID 108, Petrel pizarra)
   - *Pterodroma macroptera* (ID 109, Petrel alas grandes)
   - *Pterodroma incerta* (ID 111, Petrel cabeza parda)
   - *Pterodroma lessonii* (ID 112, Petrel cabeza blanca)
   - *Pterodroma arminjoniana* (ID 113, Petrel de Trinidad)
   - *Procellaria conspicillata* (ID 122, Petrel de anteojos)
   - *Pelecanoides georgicus* (ID 131, Yunco geórgico)
   - *Pelecanoides magellani* (ID 132, Yunco magallánico)
   - *Fregetta grallaria* (ID 133, Paíño vientre blanco)
   - *Garrodia nereis* (ID 136, Paíño gris)
   - *Pelagodroma marina* (ID 137, Paíño cara blanca)
   - *Oceanodroma leucorrhoa* (ID 138, Paíño boreal)
   - *Phalacrocorax bougainvillii* / *Leucocarbo bougainvillii* (ID 149, Guanay)
   - *Phalacrocorax atriceps* / *Leucocarbo atriceps* (ID 150, Cormorán imperial)
   - *Phalacrocorax bransfieldensis* / *Leucocarbo bransfieldensis* (ID 151, Cormorán antártico)
   - *Phalacrocorax georgianus* / *Leucocarbo georgianus* (ID 152, Cormorán de las Georgias)
   - *Pelecanus thagus* (ID 154, Pelícano pardo)
2. **Aves continentales de humedales y pastizales (7 especies):**
   - *Heteronetta atricapilla* (ID 57, Pato cabeza negra)
   - *Podiceps juninensis* (ID 73, Macá plateado norteño)
   - *Coturnicops notatus* (ID 227, Burrito enano)
   - *Porzana spiloptera* (ID 236, Burrito negruzco)
   - *Tigrisoma fasciatum* (ID 156, Hocó obscuro)
   - *Agriornis murinus* (ID 763, Gaucho chico)
   - *Neoxolmis rubetra* / *Xolmis rubetra* (ID 769, Monjita castaña)
3. **Rapaces diurnas y caranchos (6 especies):**
   - *Cathartes burrovianus* (ID 179, Jote cabeza amarilla chico)
   - *Sarcoramphus papa* (ID 181, Jote real)
   - *Phalcoboenus albogularis* (ID 511, Matamico blanco)
   - *Falco deiroleucus* (ID 517, Halcón negro grande)
   - *Xolmis salinarum* / *Neoxolmis salinarum* (ID 768, Monjita salinera — endémica)
4. **Colúmbidos y vencejos (4 especies):**
   - *Columbina squamata* (ID 355, Torcacita escamada)
   - *Claravis geoffroyi* / *Paraclaravis geoffroyi* (ID 358, Palomita morada — en peligro crítico)
   - *Macropsalis forcipata* (ID 413, Atajacaminos coludo)
   - *Cypseloides rothschildi* (ID 416, Vencejo parduzco)
5. **Colibríes y carpinteros (3 especies):**
   - *Eriocnemis glaucopoides* (ID 439, Picaflor frente azul)
   - *Veniliornis frontalis* (ID 486, Carpintero oliva yungeño)
   - *Philydor rufum* (ID 628, Ticotico grande)
6. **Tiránidos, passeriformes y fringílidos (7 especies):**
   - *Asthenes sclateri* (ID 654, Espartillero serrano)
   - *Serpophaga munda* (ID 699, Piojito vientre blanco)
   - *Phaeomyias murina* (ID 701, Piojito pardo)
   - *Phylloscartes eximius* (ID 712, Mosqueta media luna)
   - *Pipra fascicauda* / *Ceratopipra fascicauda* (ID 814, Bailarín naranja)
   - *Molothrus rufaxillaris* (ID 1016, Tordo pico corto)
7. **Especies extintas o históricas sin registros recientes (2 especies):**
   - *Anodorhynchus glaucus* (ID 538, Guacamayo azul — extinto/crítico sin registros confirmados)
   - *Numenius borealis* (ID 276, Playero esquimal — presumiblemente extinto)

---

## 6. Casos taxonómicos ambiguos y tratamiento técnico

Para dar cumplimiento estricto a **RF-4** («sin suponer equivalencias no verificadas»), se analizaron en profundidad los casos de división sistemática (*splits*) o tratamiento subespecífico:

### 6.1. *Troglodytes aedon* vs. *Troglodytes musculus* (Ratona común)
- **Problema:** En la ornitología clásica argentina (CARO / Navas et al. 1991 / MAyDS 2017), la ratona común se clasifica bajo *Troglodytes aedon* (abarcando las subespecies sudamericanas *T. a. musculus*, *T. a. bonariae*, *T. a. chilensis*). En la taxonomía moderna de Clements / BirdNET, la forma sudamericana se escinde como especie plena: `Troglodytes musculus` (idx: 9326, *Chochín Criollo Sureño*), mientras que `Troglodytes aedon` (idx: 9323, *Chochín Criollo Norteño*) se reserva para las poblaciones norteamericanas.
- **Tratamiento:** Ambas clases existen en el modelo. En las grabaciones realizadas en Argentina, las detecciones de canto corresponden a la especie biológica residente (`Troglodytes musculus`). Se asignará **«Ratona común»** a `Troglodytes musculus` (con prioridad bonaerense plena), y secundariamente a `Troglodytes aedon`, garantizando que cualquier detección de la especie en el territorio muestre el nombre vernáculo argentino.

### 6.2. Complejo de cormoranes de ojos azules (*Leucocarbo*)
- **Problema:** Tradicionalmente clasificados como *Phalacrocorax atriceps*, *P. bransfieldensis*, *P. georgianus* y *P. magellanicus*. El modelo BirdNET+ pruned solo incluye a `Leucocarbo magellanicus` (idx: 4940, *Cormorán cuello negro*), habiendo podado a *atriceps*, *bransfieldensis* y *georgianus*.
- **Tratamiento:** Se asocia *Phalacocorax magallanicus* a `Leucocarbo magellanicus` (coincidencia unívoca verificada). No se fuerza la asignación de *atriceps* a *magellanicus*, manteniéndose como especie ausente en el modelo de acuerdo con RF-4.

### 6.3. *Astur bicolor* y *Astur chilensis* (Esparvero variado y Esparvero araucano)
- **Problema:** En la lista de 2017 figuran como *Accipiter bicolor* (ID 205) y *Accipiter bicolor chilensis* (ID 206). En BirdNET/Clements figuran como dos clases independientes: `Astur bicolor` (idx: 30) y `Astur chilensis` (idx: 32).
- **Tratamiento:** Ambas se mapean de forma independiente y unívoca:
  - *Accipiter bicolor* -> `Astur bicolor` (idx: 30, *Esparvero variado*)
  - *Accipiter bicolor chilensis* -> `Astur chilensis` (idx: 32, *Esparvero araucano*)

---

## 7. Método reproducible de verificación

La verificación de este inventario es 100% automatizada y reproducible mediante scripts locales:

1. **Ejecutar el inventario completo:**
   ```powershell
   python tools/inventory_species_coverage.py
   ```
2. **Ejecutar la suite de pruebas unitarias del inventario:**
   ```powershell
   python -m unittest tools/test_inventory_species_coverage.py
   ```
   *Resultado comprobado:* 5 pruebas ejecutadas y aprobadas en 0.064 s.
3. **Verificar que no existen regresiones en las pruebas unitarias de la aplicación:**
   ```powershell
   .\gradlew.bat :app:testDebugUnitTest
   ```
   *Resultado comprobado:* `BUILD SUCCESSFUL in 28s`, 29 tareas up-to-date.

---

## 8. Cumplimiento de la condición de cierre de la Tarea T2

La condición de cierre de T2 en `tasks.md` establece:
> *«Inventario reproducible con casos no resueltos y cobertura expresada sin prometer detección de especies ausentes del modelo.»*

- [x] **Comparación sistemática efectuada:** Se contrastaron las 1.043 especies de la referencia nacional contra las 8.927 aves de `species.json`.
- [x] **Cifras documentadas:** 882 coincidencias exactas (84,6%), 108 equivalencias verificadas (10,4%), 990 cubiertas en modelo (94,9%), 53 ausentes (5,1%).
- [x] **Respaldo formal de equivalencias:** Se explicaron y respaldaron las 108 revisiones taxonómicas (Anatidae, Phalacrocoracidae, Falconidae, Charadriiformes, Thraupidae, Icteridae, Fringillidae, Strigidae, Tyrannidae).
- [x] **Respaldo de prioridad bonaerense:** Se distinguió la evidencia provincial (Darrieu et al. 2013 y `labels_map.json`) de la lista nacional general.
- [x] **Explicitación de ausencias:** Se listaron las 53 especies ausentes del modelo, dejando explícito que no pueden ser detectadas en la versión actual del modelo y que no se prometerá su detección.
- [x] **Método reproducible verificado:** Script `tools/inventory_species_coverage.py` y pruebas `tools/test_inventory_species_coverage.py` validadas con tests unitarios.

**La Tarea T2 cumple plenamente su condición de cierre.**
