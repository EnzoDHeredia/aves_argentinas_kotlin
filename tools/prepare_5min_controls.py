import urllib.request
import subprocess
import os
import hashlib
import json
import numpy as np

FFMPEG_PATH = "D:/ffmpeg/ffmpeg.exe"
TARGET_DIR = "app/src/androidTest/assets/birdnet/controls_5min"
RAW_DIR = "app/build/birdnet-verification/spec004/raw_controls"
MANIFEST_PATH = "app/src/androidTest/assets/birdnet/controls_5min_manifest.json"

os.makedirs(TARGET_DIR, exist_ok=True)
os.makedirs(RAW_DIR, exist_ok=True)

CONTROLS = [
    {
        "id": "ctrl-5m-01-voz-consentida",
        "type": "voz_consentida",
        "title": "Conferencia gastronómica en el Mazatlán International Center",
        "source": "Wikimedia Commons",
        "obs_id": "File:Conferencia gastronómica en el Mazatlán International Center, 18 de junio de 2023.ogg",
        "recordist": "DogeGamer2015MZT",
        "license": "CC0 1.0 Universal",
        "location": "Mazatlán International Center, Sinaloa, México",
        "date": "2023-06-18",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/a/a5/Conferencia_gastron%C3%B3mica_en_el_Mazatl%C3%A1n_International_Center%2C_18_de_junio_de_2023.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-01-voz-consentida.pcm",
        "human_annotation": "Alocución y conferencia gastronómica consentida de orador en español en auditorio cerrado del centro de convenciones; acústica interior de sala, amplificación por micrófono, sin trinos ni sonidos biológicos de aves."
    },
    {
        "id": "ctrl-5m-02-viento",
        "type": "viento",
        "title": "Het ruisen van de wind (SoundCloud - Beeld en Geluid)",
        "source": "Wikimedia Commons",
        "obs_id": "File:Het ruisen van de wind - SoundCloud - Beeld en Geluid.ogg",
        "recordist": "Nederlands Instituut voor Beeld en Geluid",
        "license": "CC BY-SA 3.0",
        "location": "Hilversum, Países Bajos (Archivo sonoro)",
        "date": "Archivo histórico",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/d/df/Het_ruisen_van_de_wind_-_SoundCloud_-_Beeld_en_Geluid.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-02-viento.pcm",
        "human_annotation": "Sonido continuo de viento natural y susurro eólico en ambiente exterior ('zacht ruisen'); flujo de aire moderado y sostenido sin aves, fauna ni presencia humana."
    },
    {
        "id": "ctrl-5m-03-ambiente-no-aviar",
        "type": "ambiente_no_aviar",
        "title": "Heavy rain in Glenshaw, PA",
        "source": "Wikimedia Commons",
        "obs_id": "File:Heavy rain in Glenshaw, PA.ogg",
        "recordist": "Sage Ross (User:Ragesoss)",
        "license": "CC BY-SA 3.0",
        "location": "Glenshaw, Pennsylvania, Estados Unidos",
        "date": "2010",
        "sound_url": "https://upload.wikimedia.org/wikipedia/commons/c/cb/Heavy_rain_in_Glenshaw%2C_PA.ogg",
        "cut_start": 0.0,
        "duration_s": 300.0,
        "sample_rate_hz": 32000,
        "samples": 9600000,
        "bytes": 19200000,
        "filename": "ctrl-5m-03-ambiente-no-aviar.pcm",
        "human_annotation": "Lluvia de moderada a intensa continua cayendo sobre suelo y vegetación en entorno residencial exterior; registro ambiental de precipitación acústica sin cantos ni vocalizaciones de aves."
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

    # Validate exact length: 9,600,000 samples, 19,200,000 bytes
    shorts = np.fromfile(pcm_path, dtype=np.int16)
    assert len(shorts) == 9600000, f"Expected 9600000 samples, got {len(shorts)}"
    assert os.path.getsize(pcm_path) == 19200000, f"Expected 19200000 bytes, got {os.path.getsize(pcm_path)}"

    # Compute SHA-256
    with open(pcm_path, "rb") as f:
        file_hash = hashlib.sha256(f.read()).hexdigest()

    item_meta = dict(ctrl)
    item_meta["sha256"] = file_hash
    manifest_items.append(item_meta)
    print(f"Control {item_id} generated: {pcm_path} (SHA-256: {file_hash})")

manifest_data = {
    "version": "1.0",
    "description": "Controles prolongados de 5 minutos independientes para evaluación continua de audio (Spec 004). Reservados exclusivamente para validación final; no utilizados para calibración.",
    "date": "2026-10-27",
    "total_controls": len(manifest_items),
    "duration_seconds_each": 300.0,
    "sample_rate_hz": 32000,
    "items": manifest_items
}

with open(MANIFEST_PATH, "w", encoding="utf-8") as f:
    json.dump(manifest_data, f, indent=2, ensure_ascii=False)

print(f"Manifest written to {MANIFEST_PATH}")
