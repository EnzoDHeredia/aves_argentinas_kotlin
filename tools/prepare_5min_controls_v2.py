import urllib.request
import subprocess
import os
import hashlib
import json
import numpy as np

FFMPEG_PATH = "D:/ffmpeg/ffmpeg.exe"
TARGET_DIR = "app/src/androidTest/assets/birdnet/controls_5min_v2"
RAW_DIR = "app/build/birdnet-verification/spec004/raw_controls_v2"
MANIFEST_PATH = "app/src/androidTest/assets/birdnet/controls_5min_v2_manifest.json"

os.makedirs(TARGET_DIR, exist_ok=True)
os.makedirs(RAW_DIR, exist_ok=True)

CONTROLS = [
    {
        "id": "ctrl-5m-v2-01-voz-consentida",
        "type": "voz_consentida",
        "title": "Conferencia de prensa de Luciano Pereyra en Oberá, Misiones",
        "source": "Wikimedia Commons",
        "obs_id": "File:Luciano-pereyra-conferencia-obera-misiones.ogg",
        "recordist": "User:Loco085",
        "license": "CC BY-SA 2.5",
        "location": "Oberá, Misiones, Argentina",
        "date": "2007-09-09",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/b/b2/Luciano-pereyra-conferencia-obera-misiones.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-v2-01-voz-consentida.pcm",
        "human_annotation": "Conferencia de prensa consentida en español durante la Fiesta Nacional del Inmigrante en Oberá, Misiones; diálogo entre artista y periodistas en sala interior con acústica de auditorio, sin vocalizaciones ni sonidos de aves."
    },
    {
        "id": "ctrl-5m-v2-02-viento",
        "type": "viento",
        "title": "IJzige wind (SoundCloud - Beeld en Geluid)",
        "source": "Wikimedia Commons",
        "obs_id": "File:IJzige wind - SoundCloud - Beeld en Geluid.ogg",
        "recordist": "Nederlands Instituut voor Beeld en Geluid",
        "license": "CC BY-SA 3.0",
        "location": "Hilversum, Países Bajos (Archivo sonoro)",
        "date": "1979-01-26",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/b/b6/IJzige_wind_-_SoundCloud_-_Beeld_en_Geluid.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-v2-02-viento.pcm",
        "human_annotation": "Viento gélido continuo y sostenido con silbidos y ráfagas sobre llanura abierta; registro eólico de archivo sin aves, vegetación ni presencia humana."
    },
    {
        "id": "ctrl-5m-v2-03-ambiente-no-aviar",
        "type": "ambiente_no_aviar",
        "title": "Uetersen Regen und Gewitter 01",
        "source": "Wikimedia Commons",
        "obs_id": "File:Uetersen Regen und Gewitter 01.ogg",
        "recordist": "User:Huhu Uet",
        "license": "CC BY 3.0",
        "location": "Uetersen, Schleswig-Holstein, Alemania",
        "date": "2013-09",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/2/27/Uetersen_Regen_und_Gewitter_01.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-v2-03-ambiente-no-aviar.pcm",
        "human_annotation": "Lluvia de tormenta continua cayendo sobre asfalto y suelo, con truenos distantes y rodadura de vehículos lejanos sobre agua; sin vocalizaciones ni cantos de aves."
    }
]

headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

manifest_items = []

for ctrl in CONTROLS:
    item_id = ctrl["id"]
    raw_ext = ctrl["sound_url"].split("?")[0].split(".")[-1]
    raw_path = os.path.join(RAW_DIR, f"{item_id}.{raw_ext}")
    pcm_path = os.path.join(TARGET_DIR, ctrl["filename"])

    if not os.path.exists(raw_path):
        print(f"Downloading {item_id} from {ctrl['sound_url']}...")
        req = urllib.request.Request(ctrl["sound_url"], headers=headers)
        with urllib.request.urlopen(req, timeout=60) as resp, open(raw_path, "wb") as out_f:
            out_f.write(resp.read())
        print(f"Downloaded {item_id}: {os.path.getsize(raw_path)} bytes")

    # Convert with ffmpeg to exactly 300s, 32000Hz, 1 channel, s16le PCM
    print(f"Converting {item_id} to 5-minute s16le PCM...")
    cmd = [
        FFMPEG_PATH, "-y",
        "-ss", str(ctrl["cut_start"]),
        "-i", raw_path,
        "-t", "300",
        "-ac", "1",
        "-ar", "32000",
        "-f", "s16le",
        pcm_path
    ]
    subprocess.run(cmd, check=True)

    size = os.path.getsize(pcm_path)
    expected_bytes = 19200000
    if size != expected_bytes:
        raise ValueError(f"File size for {pcm_path} is {size}, expected {expected_bytes}")

    with open(pcm_path, "rb") as f:
        data = f.read()
        sha256_hash = hashlib.sha256(data).hexdigest()

    print(f"Verified {item_id}: {size} bytes, SHA-256: {sha256_hash}")

    manifest_item = dict(ctrl)
    manifest_item["sha256"] = sha256_hash
    manifest_items.append(manifest_item)

manifest = {
    "version": "1.0",
    "description": "Controles prolongados independientes reservados de 5 minutos (Set B / V2) para evaluacion continua a umbral revisado T=0.50",
    "total_controls": len(manifest_items),
    "sample_rate_hz": 32000,
    "duration_s": 300.0,
    "items": manifest_items
}

with open(MANIFEST_PATH, "w", encoding="utf-8") as f:
    json.dump(manifest, f, indent=2, ensure_ascii=False)

print("Generated V2 manifest at", MANIFEST_PATH)
