import sys
import urllib.request
import urllib.parse
import json

sys.stdout.reconfigure(encoding='utf-8')
headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

files = [
    "File:Luciano-pereyra-conferencia-obera-misiones.ogg",
    "File:IJzige wind - SoundCloud - Beeld en Geluid.ogg",
    "File:Uetersen Regen und Gewitter 01.ogg"
]

for f in files:
    url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.parse.quote(f)}&prop=imageinfo&iiprop=url|size|extmetadata|duration&format=json"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        for _, page in data.get('query', {}).get('pages', {}).items():
            info = page.get('imageinfo', [{}])[0]
            extmeta = info.get('extmetadata', {})
            print(f"=== {f} ===")
            print("  URL:", info.get('url'))
            print("  Duration:", info.get('duration'))
            print("  Artist:", extmeta.get('Artist', {}).get('value'))
            print("  License:", extmeta.get('LicenseShortName', {}).get('value'))
            print("  DateTime:", extmeta.get('DateTimeOriginal', {}).get('value'))
            print("  Description:", extmeta.get('ImageDescription', {}).get('value')[:150] if extmeta.get('ImageDescription') else "None")
