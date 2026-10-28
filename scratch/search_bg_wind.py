import sys
import urllib.request
import urllib.parse
import json

sys.stdout.reconfigure(encoding='utf-8')
headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def search_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&format=json&srlimit=25"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data.get('query', {}).get('search', [])

def get_file_info(title):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.parse.quote(title)}&prop=imageinfo&iiprop=url|size|extmetadata|duration&format=json"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        pages = data.get('query', {}).get('pages', {})
        for _, page in pages.items():
            imageinfo = page.get('imageinfo', [])
            if imageinfo:
                return imageinfo[0]
    return None

res = search_commons('"Beeld en Geluid" wind')
for r in res:
    title = r['title']
    if not any(title.lower().endswith(ext) for ext in ['.ogg', '.oga', '.wav', '.flac']):
        continue
    info = get_file_info(title)
    if info:
        dur = info.get('duration', 0)
        print(f"B&G WIND: {title} | dur={dur:.1f}s | url={info.get('url')}")
