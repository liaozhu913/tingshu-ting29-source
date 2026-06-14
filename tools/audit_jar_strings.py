#!/usr/bin/env python3
"""Static audit for the published custom-source jar.

The repo currently ships the runtime implementation as a dex jar. This script
extracts readable strings from classes.dex and verifies that each registered site
still has key parser traces in the binary before publishing a subscription update.
"""
from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAR = ROOT / "sources_by_ting29.jar"
SITES = {
    "29听书网": ["https://m.ting29.com", "Ting29"],
    "恋听网": ["恋听网"],
    "乐听网": ["乐听网"],
    "麒麟听书": ["QilinTingShu", "麒麟听书"],
    "275听书": ["https://m.i275.com", "275听书"],
    "有听网": ["有听网"],
    "听13网": ["https://www.ting13.cc", "Ting13"],
    "爱听书": ["爱听书"],
}


def dex_strings() -> set[str]:
    with zipfile.ZipFile(JAR) as jar:
        data = jar.read("classes.dex")
    values: set[str] = set()
    for raw in re.findall(rb"[\x20-\x7e\x80-\xff]{4,}", data):
        try:
            values.add(raw.decode("utf-8"))
        except UnicodeDecodeError:
            continue
    return values


def main() -> int:
    strings = dex_strings()
    report = {}
    failed = False
    for site, needles in SITES.items():
        hits = [needle for needle in needles if any(needle in value for value in strings)]
        ok = len(hits) == len(needles)
        failed = failed or not ok
        report[site] = {"ok": ok, "hits": hits, "expected": needles}
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
