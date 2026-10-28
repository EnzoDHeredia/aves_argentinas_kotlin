import sys
import urllib.request
import urllib.parse
import json

sys.stdout.reconfigure(encoding='utf-8')
headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def parse_length(val):
    if not val:
        return 0.0
    if isinstance(val, (int, float)):
        return float(val)
    val = str(val).strip()
    if ':' in val:
        parts = val.split(':')
        if len(parts) == 2:
            return float(parts[0]) * 60 + float(parts[1])
        elif len(parts) == 3:
            return float(parts[0]) * 3600 + float(parts[1]) * 60 + float(parts[2])
    try:
        return float(val)
    except:
        return 0.0

def search_archive(query):
    params = {
        'q': f'mediatype:audio AND {query}',
        'fl[]': 'identifier,title,description,licenseurl',
        'rows': 10,
        'output': 'json'
    }
    url = f"https://archive.org/advancedsearch.php?{urllib.parse.urlencode(params)}"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data.get('response', {}).get('docs', [])

def get_item_files(identifier):
    url = f"https://archive.org/metadata/{identifier}/files"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data.get('result', [])

for q, label in [("wind sound", "WIND"), ("stream water sound", "AMBIENT"), ("ocean waves sound", "AMBIENT"), ("rain sound", "AMBIENT")]:
    print(f"=== {label}: {q} ===")
    docs = search_archive(q)
    for d in docs:
        ident = d['identifier']
        licenseurl = d.get('licenseurl', '')
        if not licenseurl:
            continue
        files = get_item_files(ident)
        for f in files:
            name = f.get('name', '')
            length = parse_length(f.get('length', 0))
            fmt = f.get('format', '')
            if length >= 300 and ('VBR MP3' in fmt or 'Ogg' in fmt or 'FLAC' in fmt or 'WAVE' in fmt):
                dl_url = f"https://archive.org/download/{ident}/{name}"
                print(f"[{label}] MATCH: {ident} | file={name} | dur={length:.1f}s | lic={licenseurl}")
                print(f"       Title: {d.get('title')}")
                print(f"       URL: {dl_url}")
                break
