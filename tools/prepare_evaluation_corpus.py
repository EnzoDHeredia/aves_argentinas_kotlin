import urllib.request
import subprocess
import json
import os
import hashlib
import time
import numpy as np

FFMPEG_PATH = "D:/ffmpeg/ffmpeg.exe"

BIRD_ENTRIES = [
    # 1. Furnarius rufus (Hornero)
    {
        "id": "cal-01-furnarius-rufus",
        "partition": "calibration",
        "species": "Furnarius rufus",
        "common_name": "Hornero",
        "target_canonical": "Furnarius rufus",
        "source": "iNaturalist",
        "obs_id": 400973504,
        "recordist": "bird0",
        "license": "CC BY-NC 4.0",
        "location": "Junín, San Luis, Argentina (-32.316, -65.011)",
        "date": "2024-09-22",
        "sound_url": "https://static.inaturalist.org/sounds/2161805.wav?1789651922",
        "cut_start": 0.0,
        "human_annotation": "Canto territorial estructurado con trino característico de Furnarius rufus en ambiente serrano."
    },
    {
        "id": "eval-01-furnarius-rufus",
        "partition": "evaluation",
        "species": "Furnarius rufus",
        "common_name": "Hornero",
        "target_canonical": "Furnarius rufus",
        "source": "iNaturalist",
        "obs_id": 393272669,
        "recordist": "bilbaomdq",
        "license": "CC BY-NC 4.0",
        "location": "Mar de Cobo, Provincia de Buenos Aires, Argentina (-37.758, -57.447)",
        "date": "2024-08-18",
        "sound_url": "https://static.inaturalist.org/sounds/2128545.wav?1787340499",
        "cut_start": 0.0,
        "human_annotation": "Llamadas de contacto y trino activo de Hornero en arboleda costera bonaerense."
    },
    {
        "id": "eval-02-furnarius-rufus",
        "partition": "evaluation",
        "species": "Furnarius rufus",
        "common_name": "Hornero",
        "target_canonical": "Furnarius rufus",
        "source": "iNaturalist",
        "obs_id": 392544493,
        "recordist": "jdk42",
        "license": "CC BY 4.0",
        "location": "Ramos Mejía, Provincia de Buenos Aires, Argentina (-34.646, -58.563)",
        "date": "2024-08-15",
        "sound_url": "https://static.inaturalist.org/sounds/2125097.mp3?1787103820",
        "cut_start": 0.0,
        "human_annotation": "Dueto vocal potente de Hornero común en jardín suburbano."
    },

    # 2. Pitangus sulphuratus (Benteveo común)
    {
        "id": "cal-02-pitangus-sulphuratus",
        "partition": "calibration",
        "species": "Pitangus sulphuratus",
        "common_name": "Benteveo común",
        "target_canonical": "Pitangus sulphuratus",
        "source": "iNaturalist",
        "obs_id": 405624196,
        "recordist": "fran09952",
        "license": "CC BY-NC 4.0",
        "location": "Belgrano, CABA, Argentina (-34.561, -58.455)",
        "date": "2024-10-14",
        "sound_url": "https://static.inaturalist.org/sounds/2183598.m4a?1791069805",
        "cut_start": 0.0,
        "human_annotation": "Gritos sonoros y enérgicos 'bicho-feo' repetidos con alta intensidad en entorno urbano arbolado."
    },
    {
        "id": "eval-03-pitangus-sulphuratus",
        "partition": "evaluation",
        "species": "Pitangus sulphuratus",
        "common_name": "Benteveo común",
        "target_canonical": "Pitangus sulphuratus",
        "source": "iNaturalist",
        "obs_id": 401771723,
        "recordist": "lilibou",
        "license": "CC BY-NC 4.0",
        "location": "Balvanera, CABA, Argentina (-34.611, -58.404)",
        "date": "2024-09-28",
        "sound_url": "https://static.inaturalist.org/sounds/2165271.m4a?1789881898",
        "cut_start": 0.0,
        "human_annotation": "Secuencia vocal de reclamo agudo 'pit-pit-bicho-feo' con eco leve entre edificios."
    },
    {
        "id": "eval-04-pitangus-sulphuratus",
        "partition": "evaluation",
        "species": "Pitangus sulphuratus",
        "common_name": "Benteveo común",
        "target_canonical": "Pitangus sulphuratus",
        "source": "iNaturalist",
        "obs_id": 400973496,
        "recordist": "bird0",
        "license": "CC BY-NC 4.0",
        "location": "Junín, San Luis, Argentina (-32.316, -65.011)",
        "date": "2024-09-22",
        "sound_url": "https://static.inaturalist.org/sounds/2161803.wav?1789651921",
        "cut_start": 0.0,
        "human_annotation": "Vocalizaciones matutinas insistentes y silbidos claros en estrato arbóreo abierto."
    },

    # 3. Turdus rufiventris (Zorzal colorado)
    {
        "id": "cal-03-turdus-rufiventris",
        "partition": "calibration",
        "species": "Turdus rufiventris",
        "common_name": "Zorzal colorado",
        "target_canonical": "Turdus rufiventris",
        "source": "iNaturalist",
        "obs_id": 408529224,
        "recordist": "ivan1807",
        "license": "CC BY-NC 4.0",
        "location": "Floresta, CABA, Argentina (-34.629, -58.472)",
        "date": "2024-10-22",
        "sound_url": "https://static.inaturalist.org/sounds/2197565.wav?1792084983",
        "cut_start": 0.0,
        "human_annotation": "Canto melodioso aflautado y complejo de Zorzal colorado con frases variadas al amanecer."
    },
    {
        "id": "eval-05-turdus-rufiventris",
        "partition": "evaluation",
        "species": "Turdus rufiventris",
        "common_name": "Zorzal colorado",
        "target_canonical": "Turdus rufiventris",
        "source": "iNaturalist",
        "obs_id": 407907002,
        "recordist": "fran09952",
        "license": "CC BY-NC 4.0",
        "location": "Almagro, CABA, Argentina (-34.613, -58.421)",
        "date": "2024-10-20",
        "sound_url": "https://static.inaturalist.org/sounds/2194590.m4a?1791839521",
        "cut_start": 0.0,
        "human_annotation": "Trinos y gorjeos continuos ricos en armónicos con pausas breves de Zorzal en jardín."
    },
    {
        "id": "eval-06-turdus-rufiventris",
        "partition": "evaluation",
        "species": "Turdus rufiventris",
        "common_name": "Zorzal colorado",
        "target_canonical": "Turdus rufiventris",
        "source": "iNaturalist",
        "obs_id": 406811998,
        "recordist": "taniaalbouy",
        "license": "CC BY-NC 4.0",
        "location": "El Pinar, Mendoza, Argentina (-32.868, -68.861)",
        "date": "2024-10-17",
        "sound_url": "https://static.inaturalist.org/sounds/2189439.m4a?1791475745",
        "cut_start": 0.0,
        "human_annotation": "Canto musical melodioso en arboleda cuyana con cadencia pausada."
    },

    # 4. Zonotrichia capensis (Chingolo)
    {
        "id": "cal-04-zonotrichia-capensis",
        "partition": "calibration",
        "species": "Zonotrichia capensis",
        "common_name": "Chingolo",
        "target_canonical": "Zonotrichia capensis",
        "source": "iNaturalist",
        "obs_id": 408380680,
        "recordist": "francopalandri",
        "license": "CC BY-NC 4.0",
        "location": "Rosario, Santa Fe, Argentina (-32.923, -60.701)",
        "date": "2024-10-21",
        "sound_url": "https://static.inaturalist.org/sounds/2196858.wav?1792020527",
        "cut_start": 0.0,
        "human_annotation": "Silbido temático 'ti-ti-fii' seguido de trino rápido descendente característico de Chingolo."
    },
    {
        "id": "eval-07-zonotrichia-capensis",
        "partition": "evaluation",
        "species": "Zonotrichia capensis",
        "common_name": "Chingolo",
        "target_canonical": "Zonotrichia capensis",
        "source": "iNaturalist",
        "obs_id": 401849409,
        "recordist": "juan_patricio3",
        "license": "CC BY-NC 4.0",
        "location": "San Andrés de Giles, Provincia de Buenos Aires, Argentina (-34.444, -59.448)",
        "date": "2024-09-28",
        "sound_url": "https://static.inaturalist.org/sounds/2165576.m4a?1789910191",
        "cut_start": 0.0,
        "human_annotation": "Canto nítido y repetido de Zonotrichia capensis desde percha en alambrado rural."
    },
    {
        "id": "eval-08-zonotrichia-capensis",
        "partition": "evaluation",
        "species": "Zonotrichia capensis",
        "common_name": "Chingolo",
        "target_canonical": "Zonotrichia capensis",
        "source": "iNaturalist",
        "obs_id": 400589919,
        "recordist": "virginiabe",
        "license": "CC BY 4.0",
        "location": "Santa Fe, Argentina (-31.633, -60.701)",
        "date": "2024-09-20",
        "sound_url": "https://static.inaturalist.org/sounds/2159752.m4a?1789498226",
        "cut_start": 0.0,
        "human_annotation": "Estrofas claras de Chingolo con trino modulado en zona costera santafesina."
    },

    # 5. Vanellus chilensis (Tero común)
    {
        "id": "cal-05-vanellus-chilensis",
        "partition": "calibration",
        "species": "Vanellus chilensis",
        "common_name": "Tero común",
        "target_canonical": "Vanellus chilensis",
        "source": "iNaturalist",
        "obs_id": 397336059,
        "recordist": "juan_naturea",
        "license": "CC BY-NC 4.0",
        "location": "Isla Verde, Córdoba, Argentina (-33.241, -62.404)",
        "date": "2024-09-07",
        "sound_url": "https://static.inaturalist.org/sounds/2146744.wav?1788575996",
        "cut_start": 0.0,
        "human_annotation": "Gritos de alarma estridentes y metálicos 'teu-teu' de Tero común en campo agrícola."
    },
    {
        "id": "eval-09-vanellus-chilensis",
        "partition": "evaluation",
        "species": "Vanellus chilensis",
        "common_name": "Tero común",
        "target_canonical": "Vanellus chilensis",
        "source": "iNaturalist",
        "obs_id": 392264868,
        "recordist": "jdk42",
        "license": "CC BY 4.0",
        "location": "Ramos Mejía, Provincia de Buenos Aires, Argentina (-34.646, -58.563)",
        "date": "2024-08-14",
        "sound_url": "https://static.inaturalist.org/sounds/2123823.mp3?1787017960",
        "cut_start": 0.0,
        "human_annotation": "Vocalización penetrante de alerta de Tero en parque abierto."
    },
    {
        "id": "eval-10-vanellus-chilensis",
        "partition": "evaluation",
        "species": "Vanellus chilensis",
        "common_name": "Tero común",
        "target_canonical": "Vanellus chilensis",
        "source": "iNaturalist",
        "obs_id": 349239646,
        "recordist": "msantillan",
        "license": "CC BY-NC 4.0",
        "location": "Toay, La Pampa, Argentina (-36.671, -64.380)",
        "date": "2024-03-24",
        "sound_url": "https://static.inaturalist.org/sounds/1854764.mp3?1776002119",
        "cut_start": 0.0,
        "human_annotation": "Vocalización territorial ruidosa continua de Vanellus chilensis en pastizal pampeano."
    },

    # 6. Zenaida auriculata (Torcaza común)
    {
        "id": "cal-06-zenaida-auriculata",
        "partition": "calibration",
        "species": "Zenaida auriculata",
        "common_name": "Torcaza común",
        "target_canonical": "Zenaida auriculata",
        "source": "iNaturalist",
        "obs_id": 408881994,
        "recordist": "deboraaye",
        "license": "CC BY 4.0",
        "location": "Posadas, Misiones, Argentina (-27.371, -55.898)",
        "date": "2024-10-23",
        "sound_url": "https://static.inaturalist.org/sounds/2199506.m4a?1792228360",
        "cut_start": 0.0,
        "human_annotation": "Arrullo suave, grave y rítmico 'cu-cu-cuuu' repetido continuamente por Torcaza común."
    },
    {
        "id": "eval-11-zenaida-auriculata",
        "partition": "evaluation",
        "species": "Zenaida auriculata",
        "common_name": "Torcaza común",
        "target_canonical": "Zenaida auriculata",
        "source": "iNaturalist",
        "obs_id": 149836479,
        "recordist": "goncrisdi",
        "license": "CC BY-NC 4.0",
        "location": "Ramón Biaus, Provincia de Buenos Aires, Argentina (-35.041, -59.954)",
        "date": "2023-02-27",
        "sound_url": "https://static.inaturalist.org/sounds/605527.mp3?1677520112",
        "cut_start": 0.0,
        "human_annotation": "Arrullos característicos de Torcaza común en arboleda de pueblo pampeano."
    },
    {
        "id": "eval-12-zenaida-auriculata",
        "partition": "evaluation",
        "species": "Zenaida auriculata",
        "common_name": "Torcaza común",
        "target_canonical": "Zenaida auriculata",
        "source": "iNaturalist",
        "obs_id": 191954758,
        "recordist": "jgvelasco",
        "license": "CC BY-SA 4.0",
        "location": "Diamante, Entre Ríos, Argentina (-32.066, -60.640)",
        "date": "2023-11-24",
        "sound_url": "https://static.inaturalist.org/sounds/850182.mp3?1700841117",
        "cut_start": 0.0,
        "human_annotation": "Canto rítmico en tono bajo característico de Zenaida auriculata en árbol entrerriano."
    },

    # 7. Myiopsitta monachus (Cotorra común)
    {
        "id": "cal-07-myiopsitta-monachus",
        "partition": "calibration",
        "species": "Myiopsitta monachus",
        "common_name": "Cotorra común",
        "target_canonical": "Myiopsitta monachus",
        "source": "iNaturalist",
        "obs_id": 393274198,
        "recordist": "bilbaomdq",
        "license": "CC BY-NC 4.0",
        "location": "Mar de Cobo, Provincia de Buenos Aires, Argentina (-37.758, -57.447)",
        "date": "2024-08-18",
        "sound_url": "https://static.inaturalist.org/sounds/2128555.wav?1787340974",
        "cut_start": 0.0,
        "human_annotation": "Cháchara áspera, chillidos estridentes y graznidos guturales grupales de Cotorra común en nido comunal."
    },
    {
        "id": "eval-13-myiopsitta-monachus",
        "partition": "evaluation",
        "species": "Myiopsitta monachus",
        "common_name": "Cotorra común",
        "target_canonical": "Myiopsitta monachus",
        "source": "iNaturalist",
        "obs_id": 385976768,
        "recordist": "mauge1",
        "license": "CC BY-NC 4.0",
        "location": "CABA, Argentina (-34.604, -58.438)",
        "date": "2024-07-16",
        "sound_url": "https://static.inaturalist.org/sounds/2092357.m4a?1785252229",
        "cut_start": 0.0,
        "human_annotation": "Gritos nasales chillones en vuelo sobre plaza urbana con múltiples individuos interactuando."
    },
    {
        "id": "eval-14-myiopsitta-monachus",
        "partition": "evaluation",
        "species": "Myiopsitta monachus",
        "common_name": "Cotorra común",
        "target_canonical": "Myiopsitta monachus",
        "source": "iNaturalist",
        "obs_id": 380075587,
        "recordist": "liam9277",
        "license": "CC BY-NC 4.0",
        "location": "Villa Gesell, Provincia de Buenos Aires, Argentina (-37.262, -56.974)",
        "date": "2024-06-25",
        "sound_url": "https://static.inaturalist.org/sounds/2065831.m4a?1783708849",
        "cut_start": 0.0,
        "human_annotation": "Reclamos ásperos vigorosos y modulados de Myiopsitta monachus en pinada costera."
    },

    # 8. Troglodytes musculus (Ratona común, correspondencia taxonómica de T. aedon)
    {
        "id": "cal-08-troglodytes-musculus",
        "partition": "calibration",
        "species": "Troglodytes musculus",
        "common_name": "Ratona común",
        "target_canonical": "Troglodytes musculus",
        "source": "iNaturalist",
        "obs_id": 224532228,
        "recordist": "naelin",
        "license": "CC BY 4.0",
        "location": "Belgrano R, CABA, Argentina (-34.571, -58.463)",
        "date": "2024-06-22",
        "sound_url": "https://static.inaturalist.org/sounds/1105302.wav?1719104538",
        "cut_start": 0.0,
        "human_annotation": "Cascada rápida y efervescente de trinos agudos y notas burbujeantes de Ratona común."
    },
    {
        "id": "eval-15-troglodytes-musculus",
        "partition": "evaluation",
        "species": "Troglodytes musculus",
        "common_name": "Ratona común",
        "target_canonical": "Troglodytes musculus",
        "source": "iNaturalist",
        "obs_id": 402070966,
        "recordist": "tomascarper",
        "license": "CC BY 4.0",
        "location": "Córdoba, Argentina (-31.438, -64.195)",
        "date": "2024-09-29",
        "sound_url": "https://static.inaturalist.org/sounds/2166454.m4a?1789956282",
        "cut_start": 0.0,
        "human_annotation": "Canto vivaz y continuo de Troglodytes musculus en campus universitario arbolado."
    },
    {
        "id": "eval-16-troglodytes-musculus",
        "partition": "evaluation",
        "species": "Troglodytes musculus",
        "common_name": "Ratona común",
        "target_canonical": "Troglodytes musculus",
        "source": "iNaturalist",
        "obs_id": 406411072,
        "recordist": "watergodnature",
        "license": "CC BY-NC 4.0",
        "location": "Zárate, Provincia de Buenos Aires, Argentina (-34.095, -59.028)",
        "date": "2024-10-16",
        "sound_url": "https://static.inaturalist.org/sounds/2187494.m4a?1791321074",
        "cut_start": 0.0,
        "human_annotation": "Frases sonoras aceleradas de trinos con llamada de alarma seca intermedia de Ratona común."
    },

    # 9. Mimus saturninus (Calandria grande)
    {
        "id": "cal-09-mimus-saturninus",
        "partition": "calibration",
        "species": "Mimus saturninus",
        "common_name": "Calandria grande",
        "target_canonical": "Mimus saturninus",
        "source": "iNaturalist",
        "obs_id": 406863482,
        "recordist": "mariano_aimar",
        "license": "CC BY-NC 4.0",
        "location": "San Justo, Santa Fe, Argentina (-30.789, -60.591)",
        "date": "2024-10-17",
        "sound_url": "https://static.inaturalist.org/sounds/2189733.wav?1791491493",
        "cut_start": 0.0,
        "human_annotation": "Canto imitador rico, potente y variado de Calandria grande combinando silbidos y chasquidos."
    },
    {
        "id": "eval-17-mimus-saturninus",
        "partition": "evaluation",
        "species": "Mimus saturninus",
        "common_name": "Calandria grande",
        "target_canonical": "Mimus saturninus",
        "source": "iNaturalist",
        "obs_id": 396479773,
        "recordist": "ignaciocostagliola",
        "license": "CC BY-NC 4.0",
        "location": "CABA, Argentina (-34.621, -58.455)",
        "date": "2024-09-04",
        "sound_url": "https://static.inaturalist.org/sounds/2143202.mp3?1788295117",
        "cut_start": 0.0,
        "human_annotation": "Largo fraseo melódico con repetición de motivos vocales típicos de Mimus saturninus."
    },
    {
        "id": "eval-18-mimus-saturninus",
        "partition": "evaluation",
        "species": "Mimus saturninus",
        "common_name": "Calandria grande",
        "target_canonical": "Mimus saturninus",
        "source": "iNaturalist",
        "obs_id": 394868612,
        "recordist": "virginiabe",
        "license": "CC BY 4.0",
        "location": "Santa Fe, Argentina (-31.633, -60.701)",
        "date": "2024-08-27",
        "sound_url": "https://static.inaturalist.org/sounds/2135823.m4a?1787806267",
        "cut_start": 0.0,
        "human_annotation": "Canto territorial de percha alta con modulación fluida y variada."
    },

    # 10. Guira guira (Pirincho)
    {
        "id": "cal-10-guira-guira",
        "partition": "calibration",
        "species": "Guira guira",
        "common_name": "Pirincho",
        "target_canonical": "Guira guira",
        "source": "iNaturalist",
        "obs_id": 409788494,
        "recordist": "francopalandri",
        "license": "CC BY-NC 4.0",
        "location": "Rosario, Santa Fe, Argentina (-32.923, -60.701)",
        "date": "2024-10-24",
        "sound_url": "https://static.inaturalist.org/sounds/2203751.wav?1792542777",
        "cut_start": 0.0,
        "human_annotation": "Silbido lastimero descendente 'piiiirrrr-pui' y reclamos roncos y ásperos de Pirincho."
    },
    {
        "id": "eval-19-guira-guira",
        "partition": "evaluation",
        "species": "Guira guira",
        "common_name": "Pirincho",
        "target_canonical": "Guira guira",
        "source": "iNaturalist",
        "obs_id": 376708423,
        "recordist": "pablohcapovilla",
        "license": "CC BY 4.0",
        "location": "Santa Fe de la Vera Cruz, Santa Fe, Argentina (-31.633, -60.701)",
        "date": "2024-06-18",
        "sound_url": "https://static.inaturalist.org/sounds/2049626.wav?1782779621",
        "cut_start": 0.0,
        "human_annotation": "Vocalizaciones ásperas y silbidos agudos en bandada pequeña de pirinchos."
    },
    {
        "id": "eval-20-guira-guira",
        "partition": "evaluation",
        "species": "Guira guira",
        "common_name": "Pirincho",
        "target_canonical": "Guira guira",
        "source": "iNaturalist",
        "obs_id": 378768091,
        "recordist": "andrea_janin",
        "license": "CC BY-NC 4.0",
        "location": "Resistencia, Chaco, Argentina (-27.460, -58.983)",
        "date": "2024-06-21",
        "sound_url": "https://static.inaturalist.org/sounds/2059631.m4a?1783348910",
        "cut_start": 0.0,
        "human_annotation": "Llamadas de alarma roncas y estridentes con trino gutural típico de pirinchos en pastizal chaqueño."
    }
]

CONTROL_ENTRIES = [
    # 4 Wind controls (2 cal, 2 eval)
    {
        "id": "cal-11-viento-1",
        "partition": "calibration",
        "species": "Control - Viento",
        "common_name": "Viento en bosque",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Wind in forest (Gravity Sound).wav",
        "recordist": "Gravity Sound",
        "license": "CC BY 4.0",
        "location": "Estudio / Exterior controlado",
        "date": "2021",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/4/47/Wind_in_forest_%28Gravity_Sound%29.wav",
        "cut_start": 0.0,
        "human_annotation": "Viento moderado silbando suavemente a través de árboles forestales; ausencia total de aves y animales."
    },
    {
        "id": "cal-12-viento-2",
        "partition": "calibration",
        "species": "Control - Viento",
        "common_name": "Viento racheado ambiental",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Wind sounds 2020-05-10 1625.mp3",
        "recordist": "Robert EA Harvey",
        "license": "CC BY-SA 4.0",
        "location": "Reino Unido",
        "date": "2020-05-10",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/d/d0/Wind_sounds_2020-05-10_1625.mp3",
        "cut_start": 30.0,
        "human_annotation": "Viento racheado continuo contra micrófono en campo abierto; sin presencia de vocalizaciones biológicas."
    },
    {
        "id": "eval-21-viento-3",
        "partition": "evaluation",
        "species": "Control - Viento",
        "common_name": "Hojas en el viento",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Leaves in the wind (Gravity Sound).wav",
        "recordist": "Gravity Sound",
        "license": "CC BY 4.0",
        "location": "Estudio / Exterior controlado",
        "date": "2021",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/0/05/Leaves_in_the_wind_%28Gravity_Sound%29.wav",
        "cut_start": 0.0,
        "human_annotation": "Ruido blanco ambiental de hojas agitadas por viento sostenido; sin sonidos biológicos."
    },
    {
        "id": "eval-22-viento-4",
        "partition": "evaluation",
        "species": "Control - Viento",
        "common_name": "Viento fuerte en bosque de pinos",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Wind in Swedish pine forest at 25 mps.ogg",
        "recordist": "Arvid Rudling",
        "license": "CC BY-SA 4.0",
        "location": "Suecia",
        "date": "2011",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/f/f3/Wind_in_Swedish_pine_forest_at_25_mps.ogg",
        "cut_start": 5.0,
        "human_annotation": "Viento intenso y turbulento a 25 m/s golpeando copas de pinos; estruendo eólico limpio sin aves."
    },

    # 4 Voice controls (2 cal, 2 eval)
    {
        "id": "cal-13-voz-1",
        "partition": "calibration",
        "species": "Control - Voz humana",
        "common_name": "Discurso público en español 1",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Discurso de Lucina Jiménez durante el XLII ENAJ.wav",
        "recordist": "Luis Alvaz",
        "license": "CC BY-SA 4.0",
        "location": "Ciudad de México",
        "date": "2019-11-09",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/f/f6/Discurso_de_Lucina_Jim%C3%A9nez_durante_el_XLII_ENAJ.wav",
        "cut_start": 10.0,
        "human_annotation": "Voz humana consentida de oradora femenina en español en atril de auditorio; sin sonidos aviares."
    },
    {
        "id": "cal-14-voz-2",
        "partition": "calibration",
        "species": "Control - Voz humana",
        "common_name": "Discurso público en español 2",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Discurso de Isabel Cabrera Manuel durante el XLII ENAJ.wav",
        "recordist": "Luis Alvaz",
        "license": "CC BY-SA 4.0",
        "location": "Ciudad de México",
        "date": "2019-11-09",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/b/be/Discurso_de_Isabel_Cabrera_Manuel_durante_el_XLII_ENAJ.wav",
        "cut_start": 10.0,
        "human_annotation": "Voz humana consentida en español de locutora en presentación protocolar; ambiente de sala cerrada sin aves."
    },
    {
        "id": "eval-23-voz-3",
        "partition": "evaluation",
        "species": "Control - Voz humana",
        "common_name": "Discurso histórico 1",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Jfk berlin address high.ogg",
        "recordist": "John F. Kennedy / White House",
        "license": "Public Domain",
        "location": "Berlín",
        "date": "1963-06-26",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/3/3a/Jfk_berlin_address_high.ogg",
        "cut_start": 30.0,
        "human_annotation": "Discurso histórico en inglés con entonación oratoria clara y resonancia de megafonía pública; sin aves."
    },
    {
        "id": "eval-24-voz-4",
        "partition": "evaluation",
        "species": "Control - Voz humana",
        "common_name": "Discurso histórico 2",
        "target_canonical": None,
        "source": "Wikimedia Commons",
        "obs_id": "File:Bush Addresses Congress 9-20-01.ogg",
        "recordist": "George W. Bush / US Gov",
        "license": "Public Domain",
        "location": "Washington D.C.",
        "date": "2001-09-20",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/1/1a/Bush_Addresses_Congress_9-20-01.ogg",
        "cut_start": 60.0,
        "human_annotation": "Alocución política masculina pausada en inglés en recinto parlamentario interior; sin presencia animal."
    },

    # 4 Constant silence controls (all 4 in evaluation)
    {
        "id": "eval-25-silencio-1",
        "partition": "evaluation",
        "species": "Control - Silencio",
        "common_name": "Silencio digital constante 1",
        "target_canonical": None,
        "source": "Generación sintética",
        "obs_id": "synthetic-silence-001",
        "recordist": "Generado localmente",
        "license": "CC0 1.0 Universal",
        "location": "N/A",
        "date": "2026-10-27",
        "sound_url": None,
        "cut_start": 0.0,
        "human_annotation": "Señal continua de varianza estrictamente cero (amplitud 0.0 constante en los 10 segundos)."
    },
    {
        "id": "eval-26-silencio-2",
        "partition": "evaluation",
        "species": "Control - Silencio",
        "common_name": "Silencio digital constante 2",
        "target_canonical": None,
        "source": "Generación sintética",
        "obs_id": "synthetic-silence-002",
        "recordist": "Generado localmente",
        "license": "CC0 1.0 Universal",
        "location": "N/A",
        "date": "2026-10-27",
        "sound_url": None,
        "cut_start": 0.0,
        "human_annotation": "Señal continua de varianza estrictamente cero (amplitud 0.0 constante en los 10 segundos)."
    },
    {
        "id": "eval-27-silencio-3",
        "partition": "evaluation",
        "species": "Control - Silencio",
        "common_name": "Silencio digital constante 3",
        "target_canonical": None,
        "source": "Generación sintética",
        "obs_id": "synthetic-silence-003",
        "recordist": "Generado localmente",
        "license": "CC0 1.0 Universal",
        "location": "N/A",
        "date": "2026-10-27",
        "sound_url": None,
        "cut_start": 0.0,
        "human_annotation": "Señal continua de varianza estrictamente cero (amplitud 0.0 constante en los 10 segundos)."
    },
    {
        "id": "eval-28-silencio-4",
        "partition": "evaluation",
        "species": "Control - Silencio",
        "common_name": "Silencio digital constante 4",
        "target_canonical": None,
        "source": "Generación sintética",
        "obs_id": "synthetic-silence-004",
        "recordist": "Generado localmente",
        "license": "CC0 1.0 Universal",
        "location": "N/A",
        "date": "2026-10-27",
        "sound_url": None,
        "cut_start": 0.0,
        "human_annotation": "Señal continua de varianza estrictamente cero (amplitud 0.0 constante en los 10 segundos)."
    }
]

ALL_ENTRIES = BIRD_ENTRIES + CONTROL_ENTRIES

def download_and_process():
    raw_dir = "app/build/birdnet-verification/corpus-raw"
    target_dir = "app/src/androidTest/assets/birdnet/corpus"
    os.makedirs(raw_dir, exist_ok=True)
    os.makedirs(target_dir, exist_ok=True)

    manifest_entries = []

    print(f"Total corpus items to prepare: {len(ALL_ENTRIES)}")
    for entry in ALL_ENTRIES:
        item_id = entry["id"]
        out_pcm = os.path.join(target_dir, f"{item_id}.pcm")
        
        if entry["sound_url"] is None:
            # Generate constant silence (320,000 shorts = 640,000 bytes of zeros)
            shorts = np.zeros(320000, dtype=np.int16)
            shorts.tofile(out_pcm)
        else:
            # Download raw file
            ext = entry["sound_url"].split("?")[0].split(".")[-1]
            if len(ext) > 4:
                ext = "wav"
            raw_file = os.path.join(raw_dir, f"{item_id}.{ext}")
            
            if not os.path.exists(raw_file):
                print(f"Downloading {item_id} from {entry['sound_url']}...")
                req = urllib.request.Request(entry["sound_url"], headers={'User-Agent': 'AvesArgentinasCorpus/1.0 (aves@argentinas.org)'})
                with urllib.request.urlopen(req, timeout=30) as resp, open(raw_file, 'wb') as out_f:
                    out_f.write(resp.read())
                time.sleep(0.5)

            # Convert with ffmpeg to 10s mono 32000Hz s16le
            cmd = [
                FFMPEG_PATH, "-y",
                "-ss", str(entry["cut_start"]),
                "-i", raw_file,
                "-t", "10",
                "-ac", "1",
                "-ar", "32000",
                "-f", "s16le",
                out_pcm
            ]
            subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        # Ensure exactly 320,000 samples (640,000 bytes)
        shorts = np.fromfile(out_pcm, dtype=np.int16)
        if len(shorts) < 320000 and len(shorts) >= 315000:
            shorts = np.pad(shorts, (0, 320000 - len(shorts)))
        assert len(shorts) >= 320000, f"Expected at least 320000 samples for {item_id}, got {len(shorts)}"
        if len(shorts) > 320000:
            shorts = shorts[:320000]
        shorts.tofile(out_pcm)

        # Verify output size is exactly 640,000 bytes
        file_size = os.path.getsize(out_pcm)
        assert file_size == 640000, f"Expected 640000 bytes for {out_pcm}, got {file_size}"

        # Calculate sha256
        with open(out_pcm, "rb") as f:
            h = hashlib.sha256(f.read()).hexdigest()

        manifest_item = dict(entry)
        manifest_item["filename"] = f"{item_id}.pcm"
        manifest_item["sha256"] = h
        manifest_item["bytes"] = file_size
        manifest_item["samples"] = 320000
        manifest_item["sample_rate_hz"] = 32000
        manifest_item["duration_s"] = 10.0
        manifest_entries.append(manifest_item)
        print(f"Prepared {item_id}: {file_size} bytes, sha256={h[:16]}...")

    # Write manifest
    manifest_path = "app/src/androidTest/assets/birdnet/corpus_manifest.json"
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump({
            "version": "1.0",
            "date": "2026-10-27",
            "total_items": len(manifest_entries),
            "partitions": {
                "calibration": len([e for e in manifest_entries if e["partition"] == "calibration"]),
                "evaluation": len([e for e in manifest_entries if e["partition"] == "evaluation"])
            },
            "items": manifest_entries
        }, f, indent=2, ensure_ascii=False)
    print(f"Wrote corpus manifest to {manifest_path}")

if __name__ == "__main__":
    download_and_process()
