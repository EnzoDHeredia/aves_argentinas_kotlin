import urllib.request
import urllib.parse
import json

headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def search_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&format=json&srlimit=30"
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

terms = [
    "viento ogg", "blizzard ogg", "gale ogg", "breeze ogg",
    "thunderstorm ogg", "rain ogg", "water ogg", "stream ogg", "creek ogg", "river ogg"
]

for t in terms:
    print(f"=== Search: {t} ===")
    res = search_commons(t)
    for r in res:
        title = r['title']
        if not any(title.lower().endswith(ext) for ext in ['.ogg', '.oga', '.wav', '.flac']):
            continue
        info = get_file_info(title)
        if info:
            dur = info.get('duration', 0)
            if dur and dur >= 290:
                print(f"  FOUND: {title} | dur={dur:.1f}s | url={info.get('url')}")
