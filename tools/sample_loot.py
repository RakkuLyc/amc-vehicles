#!/usr/bin/env python3
"""Samples an AMC table on the test server (RCON, Luck 0) and compares each AMC Vehicles item with the expected.

usage: python3 -I tools/sample_loot.py <table, e.g. workshop/barrel> [N]
Generates the table `abandonedmoderncity:chests/<table>` N times in a chest (the loot modifiers of every mod apply)
and counts, per container: seats, wheels, engines, blowtorch, repair kit, jumper pack, empty and full jerry can (the
full one is the jerry can with `jerrycanFluid` in the item's data) and, to prove that AMC TACZ Loot still adds, the
items of `tacz:`/`lrtactical:`/`firstaid:`. The expected comes from gen_loot.py (chance per class, chest or barrel; _hard =
chest/barrel). Shows the deviation (in standard deviations of the binomial): up to 1.96 = ok; up to 3 = limit (expected
in 5% of the items by chance); above 3 = defect.
"""
import collections, math, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rconlib import Rcon
import importlib.util

spec = importlib.util.spec_from_file_location("gen", os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                                                  "gen_loot.py"))
gen = importlib.util.module_from_spec(spec)
src = open(spec.origin).read().replace('\nif __name__ == "__main__":', '\nif False:')
exec(compile(src, "gen_loot", "exec"), gen.__dict__)


# the whole `id` key (not the end of `jerrycanFluid: "lava"`, which also ends in `id: "`)
ID_KEY = re.compile(r'(?:^|[\s,{])id: "([a-z_0-9:.]+)"')


def group(chunk):
    m = ID_KEY.search(chunk)
    iid = m.group(1) if m else ""
    if iid.startswith("mts:"):
        if "seat" in iid: return "seat"
        if "wheel" in iid: return "wheel"
        if ".engine" in iid: return "engine"
        if iid == "mts:mts.wrench": return "wrench"
        if "blowtorch" in iid: return "blowtorch"
        if "repairkit" in iid: return "repairkit"
        if "jumperpack" in iid: return "jumperpack"
        if "jerrycan" in iid: return "jerrycan_full" if "jerrycanFluid" in chunk else "jerrycan_empty"
        return "mts_other"
    if iid.split(":")[0] in ("tacz", "lrtactical", "firstaid"):
        return "addon"
    return "other"


def split_items(text):
    """The items of `data get block ... Items`: each compound {...} at the list's level (the key order varies)."""
    out, depth, start = [], 0, None
    i = text.find("[")
    for k in range(i, len(text)):
        ch = text[k]
        if ch == "{":
            if depth == 0:
                start = k
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0 and start is not None:
                out.append(text[start:k + 1])
    return out


def main():
    table = sys.argv[1]
    n = int(sys.argv[2]) if len(sys.argv) > 2 else 2000
    theme, tier = table.split("/")
    kind = gen.TIERS[tier]
    rc = Rcon()
    rc.cmd("forceload add 0 0")
    have = collections.Counter()
    units = collections.Counter()
    items_seen = collections.Counter()
    fluids = collections.Counter()
    for _ in range(n):
        rc.cmd("setblock 0 100 0 minecraft:air")
        rc.cmd("setblock 0 100 0 minecraft:chest")
        rc.cmd("loot insert 0 100 0 loot abandonedmoderncity:chests/" + table)
        items = rc.cmd("data get block 0 100 0 Items")
        per = collections.Counter()
        for chunk in split_items(items):
            g = group(chunk)
            c = re.search(r"count: (\d+)", chunk)
            per[g] += int(c.group(1)) if c else 1
            if g not in ("other", "addon"):
                items_seen[ID_KEY.search(chunk).group(1)] += 1
            fl = re.search(r'jerrycanFluid: "([^"]*)"', chunk)
            if fl:
                fluids[fl.group(1)] += 1
        for g, v in per.items():
            have[g] += 1
            units[g] += v
    print("table %s: %d containers (kind %s)" % (table, n, kind))
    ok = True
    for g, cls in gen.THEMES[theme]:
        p = gen.chance(cls, kind, g) / gen.SCALE
        obs = have[g] / n
        sd = math.sqrt(p * (1 - p) / n)
        z = (obs - p) / sd
        inside = abs(z) <= 3
        ok &= inside
        print("  %-15s expected %5.2f%%  observed %5.2f%% (%d of %d)  deviation %+.1f  %s" % (
            g, 100 * p, 100 * obs, have[g], n, z,
            "ok" if abs(z) <= 1.96 else ("limit (95%)" if inside else "OUT (3 deviations)")))
    # any AMC Vehicles item that should NOT come out in this table (e.g. empty jerry can, wrench at a gas station)
    # is a defect
    stray = sorted(g for g in have if g not in gen_names(theme) and g not in ("addon", "other"))
    if stray:
        ok = False
        print("  ITEMS THAT SHOULD NOT COME OUT:", {g: have[g] for g in stray})
    print("  AMC TACZ Loot items (tacz/lrtactical/firstaid) in %.1f%% of the containers" % (100 * have["addon"] / n))
    print("  ids seen:", dict(items_seen))
    if fluids:
        print("  full jerry can: fluid", dict(fluids))
    print("RESULT:", "no item beyond 3 deviations" if ok else "AN ITEM IS OUT (more than 3 deviations)")


def gen_names(theme):
    return [g for g, _ in gen.THEMES[theme]]


main()
