#!/usr/bin/env python3
"""Check every supported Android string catalog without an Android SDK."""
from collections import Counter
from pathlib import Path
import argparse
import json
import re
import xml.etree.ElementTree as ET

FORMAT = re.compile(r"%(?:(\d+)\$)?([-#+ 0,(<]*)(?:\d+)?(?:\.\d+)?([tT])?([A-Za-z%])")


def placeholder_signature(value):
    """Compare argument indices/types, allowing translated positional reordering."""
    signature = Counter()
    implicit_index = 0
    previous_index = None
    for match in FORMAT.finditer(value):
        position, flags, date_prefix, conversion = match.groups()
        if conversion in ("%", "n") and not date_prefix:
            continue
        if "<" in flags:
            if previous_index is None:
                raise ValueError("Relative placeholder has no preceding argument")
            index = previous_index
        elif position:
            index = int(position)
        else:
            implicit_index += 1
            index = implicit_index
        previous_index = index
        signature[(index, (date_prefix or "") + conversion)] += 1
    return signature


def read_catalog(directory):
    catalog = {}
    for path in sorted(directory.glob("*.xml")):
        for entry in ET.parse(path).getroot():
            if entry.tag != "string":
                continue
            name = entry.attrib["name"]
            if name in catalog:
                raise ValueError(f"Duplicate string {name} in {directory}")
            catalog[name] = "".join(entry.itertext())
    return catalog


def compare_catalog(base, localized):
    common = base.keys() & localized.keys()
    return {
        "keys": len(localized),
        "missing": sorted(base.keys() - localized.keys()),
        "extra": sorted(localized.keys() - base.keys()),
        "placeholder_mismatches": sorted(
            key for key in common
            if placeholder_signature(base[key]) != placeholder_signature(localized[key])
        ),
        "empty": sorted(key for key, value in localized.items() if not value.strip()),
        "identical_to_base": sum(base[key] == localized[key] for key in common),
    }


def audit_repository(repo):
    option_source = repo / "app/src/main/java/cn/gdeiassistant/ui/profile/SupportedLanguageOptions.kt"
    locales = re.findall(r'SupportedLanguageOption\("([^"\n]+)"', option_source.read_text())
    if not locales or len(set(locales)) != len(locales):
        raise ValueError("Supported locale registry is empty or duplicated")
    resources = repo / "app/src/main/res"
    base = read_catalog(resources / "values")
    reports = {}
    for locale in locales:
        language, _, region = locale.partition("-")
        folder = "values" if locale == "zh-CN" else "values-" + language + ("-r" + region if region else "")
        reports[locale] = compare_catalog(base, read_catalog(resources / folder))
    references = set()
    for source in (repo / "app/src/main/java").rglob("*.kt"):
        references.update(re.findall(r"(?<!android\.)\bR\.string\.(\w+)", source.read_text()))
    return {
        "locales": reports,
        "unresolved_string_references": sorted(references - base.keys()),
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", type=Path, help="Optional path for the complete scan report")
    args = parser.parse_args()
    repo = Path(__file__).resolve().parents[1]
    report = audit_repository(repo)
    if args.json:
        args.json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    for locale, result in report["locales"].items():
        print(f'{locale}: {result["keys"]} keys; missing={len(result["missing"])}; extra={len(result["extra"])}; placeholders={len(result["placeholder_mismatches"])}; empty={len(result["empty"])}')
    print("Unresolved R.string references:", len(report["unresolved_string_references"]))
    failed = report["unresolved_string_references"] or any(
        any(result[key] for key in ("missing", "extra", "placeholder_mismatches", "empty"))
        for result in report["locales"].values()
    )
    if failed:
        print(json.dumps(report, ensure_ascii=False, indent=2))
        raise SystemExit(1)


if __name__ == "__main__":
    main()
