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

for q in ["wind incategory:Freesound", "rain incategory:Freesound", "stream incategory:Freesound", "water incategory:Freesound"]:
    print(f"=== {q} ===")
    res = search_commons(q)
    for r in res:
        title = r['title']
        info = get_file_info(title)
        if info:
            dur = info.get('duration', 0)
            if dur and dur >= 290:
                print(f"MATCH: {title} | dur={dur:.1f}s | url={info.get('url')}")
