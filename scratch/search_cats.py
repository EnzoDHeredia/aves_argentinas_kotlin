import urllib.request
import urllib.parse
import json

headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def get_category_files(cat_name):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=categorymembers&cmtitle={urllib.parse.quote('Category:' + cat_name)}&cmtype=file&cmlimit=50&format=json"
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data.get('query', {}).get('categorymembers', [])

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

for cat in ["Wind_sounds", "Rain_sounds", "Thunder_sounds", "Sea_sounds", "Water_sounds", "Stream_sounds", "River_sounds", "Waves_sounds", "Noise"]:
    print(f"=== Category:{cat} ===")
    files = get_category_files(cat)
    for f in files:
        t = f['title']
        info = get_file_info(t)
        if info:
            dur = info.get('duration', 0)
            if dur and dur >= 295:
                print(f"FOUND IN {cat}: {t} | dur={dur:.1f}s | url={info.get('url')}")
