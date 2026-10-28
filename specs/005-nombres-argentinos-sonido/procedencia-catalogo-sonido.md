# Procedencia y especificación técnica del catálogo distribuible de sonido (sound_names_ar.json)

Estado: Completada (Tarea T3).  
Fecha: 28 de octubre de 2026.  
Referencias: [Spec 005](spec.md) (RF-1 a RF-5), [Plan técnico](plan.md) (Sección 3), [Tareas](tasks.md) (T3), [Evaluación de fuentes](evaluacion-fuentes.md) (T1), [Inventario de cobertura](inventario-cobertura.md) (T2).  
Herramienta de preparación: `tools/prepare_sound_names_catalog.py`.  
Herramientas de verificación: `tools/test_sound_names_catalog.py` y `app/src/test/java/com/example/avesargentinas/ui/sound/BirdSoundNamesResourceTest.kt`.

---

## 1. Resumen del recurso generado

El recurso generado es `app/src/main/assets/birdnet/sound_names_ar.json`.
- **Estructura:** Diccionario JSON determinista ordenado alfabéticamente por clave binominal canónica normalizada (`snake_case`).
- **Pares incluidos:** 988 pares de correspondencia única entre identidad científica canónica del modelo y nombre común seleccionado.
- **Tamaño:** 45.623 bytes.
- **Huella digital SHA-256:** `a3b2e39646b1b9acceddccc4958bf45ea4198374a4cb6c34424a3a6f61f6e0e6`.
- **Avisos legales asociados:** `app/src/main/assets/birdnet/NOTICE_SOUND_NAMES.txt`.

---

## 2. Fuentes, versiones y condiciones de redistribución

En cumplimiento estricto del requisito de verificación de procedencia y derechos de redistribución:

### 2.1. Prioridad Provincial de Buenos Aires (RF-2)
1. **Darrieu et al. (2013) — UNLP / Fundación Azara:**
   - *Obra:* «Lista actualizada de las aves de la provincia de Buenos Aires» (Carlos Aquiles Darrieu, Aníbal Raúl Camperi, Gabriel Piloni y Natalia R. Bogado, 2013).
   - *Licencia verificada:* **Creative Commons Atribución-NoComercial-CompartirIgual 4.0 Internacional (CC BY-NC-SA 4.0)**, registrada en el Repositorio Abierto UNLP SEDICI.
   - *Condición de uso:* Autoriza el uso, adaptación y redistribución para fines educativos y no comerciales con crédito a los autores e instituciones.
   - *Rol en el recurso:* Sustento primario para 438 especies bonaerenses y nombres populares pampeanos (ej. *Macá grande*, *Pato picazo*, *Hornero*, *Benteveo común*, *Ratona común*, *Tero común*).
2. **Catálogo local preexistente (`labels_map.json`):**
   - *Origen:* Proyecto Aves Argentinas (46 especies pampeanas comunes de Buenos Aires).
   - *Licencia:* Propia del proyecto / MIT.
   - *Rol en el recurso:* Prioridad plena para las 46 especies con nombres normalizados y con tildes ortográficas correctas.

### 2.2. Referencia Nacional Argentina (RF-3)
1. **Catálogo Oficial MAyDS / Aves Argentinas (2017) & CARO:**
   - *Obra:* «Categorización de las Aves de la Argentina según su estado de conservación» (Ministerio de Ambiente y Desarrollo Sustentable de la Nación y Aves Argentinas, 2017), complementada con la Monografía Nº 12 (Monteleone y Pagano, 2022) y dictámenes del Comité Argentino de Registros Ornitológicos (CARO).
   - *Régimen de derechos:* Nombres comunes y binomios científicos tratados como hechos ornitológicos y patrimonio cultural lingüístico (sin volcado masivo de textos ni capítulos editoriales protegidos por copyright).
   - *Rol en el recurso:* Base nacional estandarizada para las especies argentinas sin particularismo bonaerense (aves del NOA, NEA, Cuyo, Patagonia y especies pelágicas).
2. **eBird Argentina / Clements Checklist (`es_AR`):**
   - *Institución:* Cornell Lab of Ornithology y eBird Argentina (coordinación por Fabricio Gorleri e Ignacio Areta).
   - *Condición:* Atribución documental bajo los términos de uso educativo de Cornell Lab.

---

## 3. Criterios de resolución y prioridad aplicados

De acuerdo con RF-1, RF-2 y RF-3:

1. **Prioridad 1 — Evidencia de Buenos Aires (RF-2):**
   - Se prioriza el uso pampeano y bonaerense documentado en Darrieu et al. (2013) y `labels_map.json`.
   - Ejemplos de reemplazo documentado frente a modismos foráneos o denominaciones nacionales alternativas:
     - `podiceps_major`: **Macá grande** (sustenta el uso bonaerense; en la lista nacional figura como *Huala*).
     - `netta_peposaca`: **Pato picazo** (grafía tradicional bonaerense frente a *Pato picaso*).
     - `vanellus_chilensis`: **Tero común** (frente a *Avefría Tero* de la traducción global).
     - `troglodytes_aedon` y `troglodytes_musculus`: **Ratona común** (frente a *Chochín Criollo*).
     - `furnarius_rufus`: **Hornero** (forma tradicional simplificada pampeana).
     - `pitangus_sulphuratus`: **Benteveo común** (fonética bonaerense tradicional).
     - `myiopsitta_monachus`: **Cotorra** (frente a *Perico Monje* o *Cotorra común*).
     - `nannopterum_brasilianum`: **Biguá** (frente a *Cormorán Biguá*).
     - Especies de Anatidae: Uso sistemático de **Pato** (*Pato capuchino*, *Pato colorado*, *Pato cuchara*, *Pato overo*, *Pato barcino*, *Pato maicero*) frente a *Cerceta* o *Silbón*.
2. **Prioridad 2 — Nombre Nacional Argentino Verificado (RF-3):**
   - Para las ~550 especies restantes registradas en Argentina pero sin presencia o tradición exclusiva en Buenos Aires (aves exclusivas de yungas, selva paranaense, puna o bosques andino-patagónicos), se asigna el nombre nacional oficial normalizado.
   - Limpieza de cadenas: Se eliminaron notas al pie de página (`*`), marcas de endemismo (`( e )`) y especificadores de poblaciones geográficas (`(continental)`, `(malvinas)`), conservando el nombre vernáculo puro con mayúscula inicial en Sentence case.
3. **Casos taxonómicos especiales resueltos (RF-4):**
   - *Troglodytes musculus* / *Troglodytes aedon*: Se asignó *Ratona común* a ambas claves en el modelo, asegurando que las grabaciones de la forma residente argentina muestren el nombre familiar rioplatense.
   - *Thlypopsis pyrrhocoma* vs. *Thlypopsis ruficeps*: Al fusionar el género *Pyrrhocoma* en *Thlypopsis*, Clements/BirdNET renombró la especie a *Thlypopsis pyrrhocoma* (idx: 9107) para evitar la homonimia con *Thlypopsis ruficeps* (idx: 9108, Tangará alisero). Ambos taxones cuentan con correspondencia diferenciada y exacta en el catálogo:
     - `thlypopsis_pyrrhocoma` -> **Pioró**
     - `thlypopsis_ruficeps` -> **Tangará alisero**
   - Las 108 equivalencias taxonómicas documentadas en T2 fueron consolidadas hacia las claves canónicas del modelo (`nannopterum_brasilianum`, `spatula_versicolor`, `mareca_sibilatrix`, `daptrius_chimango`, `astur_bicolor`, `astur_chilensis`, etc.).

---

## 4. Cuantificación y cobertura

- **Total clases en el modelo BirdNET+ pruned:** 9.789 (8.927 de la clase *Aves*).
- **Total lista oficial de referencia argentina (MAyDS 2017):** 1.043 especies evaluadas.
- **Especies argentinas reconocibles por el modelo:** 990 especies (94,9% de la avifauna nacional).
- **Entradas efectivas en `sound_names_ar.json`:** **988 claves únicas** (las 990 especies argentinas reconocibles representadas unívocamente, unificando poblaciones continentales/insulares de *Chloephaga picta*, *Chloephaga rubidiceps* y *Melanodera melanodera*, e incorporando la escisión taxonómica de *Troglodytes musculus*).
- **Cobertura de especies locales preexistentes (`labels_map.json`):** **46 de 46 especies (100%)**.
- **Especies argentinas ausentes del modelo:** **53 especies (5,1%)**. En cumplimiento estricto de **RF-5**, no se crearon entradas para estas 53 especies, evitando promesas falsas de reconocimiento o asignaciones erróneas.
- **Aves exóticas/extranjeras sin presencia en Argentina:** Permanecen deliberadamente ausentes de `sound_names_ar.json`, preservando su fallback natural hacia `common_name_es` o `scientific_name` según RF-3 y RF-7.

---

## 5. Pruebas y validaciones ejecutadas

1. **Unicidad y formato de claves:**
   - 988 claves únicas, no vacías, en minúsculas y separadas por guión bajo (`^[a-z]+(_[a-z]+)+$`).
2. **Valores no vacíos y limpios:**
   - 988 nombres comunes válidos, sin caracteres parásitos, notas al pie ni marcas de edición.
3. **Correspondencia con el modelo (RF-4):**
   - El 100% de las 988 claves existe en `species.json` y pertenece a la clase *Aves*.
4. **Verificación de ejemplos clave:**
   - 27 taxones de control (Buenos Aires, nacionales, sinónimos y escisiones) verificados con aserciones exactas.
5. **Preservación e integridad de activos preexistentes:**
   - Modelo ONNX (`BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx`): SHA-256 `cc696820...` (idéntico).
   - Etiquetas CSV (`BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv`): SHA-256 `b2ba8b13...` (idéntico).
   - Taxonomía del modelo (`species.json`): SHA-256 `5e3598bd...` (idéntico).
   - Catálogo local (`labels_map.json`): SHA-256 `ef23d3a6...` (idéntico).
6. **Ejecución de suites de prueba:**
   - Suite Python de integridad: `python -m unittest tools/test_sound_names_catalog.py` (8 pruebas exitosas en 0,094 s).
   - Suite Python de inventario: `python -m unittest tools/test_inventory_species_coverage.py` (5 pruebas exitosas en 0,030 s).
   - Suite unitaria Android: `.\gradlew.bat :app:testDebugUnitTest` (66 pruebas exitosas, incluyendo `BirdSoundNamesResourceTest`, en 6 s).

---

## 6. Estado y condición de cierre de la Tarea T3

La condición de cierre estipula:
> *«Recurso trazable e íntegro, sin modificar todavía la selección de nombres en la app.»*

- [x] El recurso `app/src/main/assets/birdnet/sound_names_ar.json` fue creado, estructurado y validado.
- [x] Los avisos aplicables fueron redactados en `app/src/main/assets/birdnet/NOTICE_SOUND_NAMES.txt`.
- [x] La documentación de procedencia y trazabilidad fue registrada en este documento.
- [x] Las pruebas unitarias automatizadas (`test_sound_names_catalog.py` y `BirdSoundNamesResourceTest.kt`) demuestran integridad completa.
- [x] Los archivos del modelo, sus etiquetas y `labels_map.json` no fueron alterados.
- [x] `BirdSoundCatalog.kt` y la selección de nombres en la app permanecen intactos, listos para la integración en la Tarea T4.

**La Tarea T3 cumple satisfactoriamente todas sus condiciones de cierre.**
