import urllib.request
import urllib.parse
import json

headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def search_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.parse.quote(query)}&srnamespace=6&format=json&srlimit=15"
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

for query, label in [("speech ogg", "VOICE"), ("conferencia ogg", "VOICE"), ("podcast ogg", "VOICE"), ("wind ogg", "WIND"), ("storm ogg", "AMBIENT"), ("ocean ogg", "AMBIENT"), ("rain ogg", "AMBIENT"), ("waterfall ogg", "AMBIENT")]:
    print(f"=== {label}: {query} ===")
    results = search_commons(query)
    for r in results[:10]:
        t = r['title']
        if not (t.endswith('.ogg') or t.endswith('.oga') or t.endswith('.wav') or t.endswith('.flac')):
            continue
        info = get_file_info(t)
        if info:
            dur = info.get('duration', 0)
            if dur and dur >= 300:
                print(f"[{label}] MATCH: {t} | dur={dur:.1f}s | url={info.get('url')}")
