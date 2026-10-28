import urllib.request
import subprocess
import os

from prepare_evaluation_corpus import ALL_ENTRIES

def check_all():
    temp = "app/build/birdnet-verification/temp_check.bin"
    ok_count = 0
    fail_count = 0
    for entry in ALL_ENTRIES:
        url = entry["sound_url"]
        if not url:
            print(f"{entry['id']}: Constant silence (OK)")
            ok_count += 1
            continue
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req, timeout=10) as resp, open(temp, "wb") as f:
                f.write(resp.read())
            cmd = ["D:/ffmpeg/ffmpeg.exe", "-i", temp]
            res = subprocess.run(cmd, stderr=subprocess.PIPE, text=True)
            dur = 0.0
            for l in res.stderr.splitlines():
                if "Duration:" in l:
                    parts = l.strip().split(",")[0].split("Duration:")[1].strip()
                    h, m, s = parts.split(":")
                    dur = float(h)*3600 + float(m)*60 + float(s)
            start = entry["cut_start"]
            remaining = dur - start
            if remaining >= 10.0:
                print(f"OK: {entry['id']} (total={dur:.1f}s, start={start}s)")
                ok_count += 1
            else:
                print(f"SHORT: {entry['id']} (total={dur:.1f}s, start={start}s, remaining={remaining:.1f}s)")
                fail_count += 1
        except Exception as e:
            print(f"FAIL {entry['id']} ({url}): {e}")
            fail_count += 1

    print(f"Summary: {ok_count} OK, {fail_count} Failed out of {len(ALL_ENTRIES)}")

if __name__ == "__main__":
    check_all()
