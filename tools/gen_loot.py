#!/usr/bin/env python3
"""Generates the loot of AMC Vehicles' workshops and gas stations (adds to AMC TACZ Loot: it only ADDS, replaces
nothing).

usage: python3 -I tools/gen_loot.py            preview (expected per structure and per city; writes nothing)
       python3 -I tools/gen_loot.py --write    deletes and rewrites
                                               src/main/resources/data/amcvehicles/{loot_table,loot_modifiers}
                                               and data/neoforge/loot_modifiers/global_loot_modifiers.json
The generator checks each item id against the jars in libs/ (assets/mts/models/item/<id>.json).
Mechanism: one loot modifier `neoforge:add_table` per AMC table (`abandonedmoderncity:chests/<theme>/<tier>`), like
AMC TACZ Loot; several mods add to the same table. Pots get nothing.
The class names "Comum", "Incomum", "Raro" (AMC TACZ Loot's Common, Uncommon, Rare) are keys of the code: kept as
they are.
"""
import json, os, shutil, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data")

# ---------------------------------------------------------------------------------------- NUMBERS (2026-11-16)
# Chance per container (in units of 1/10,000) of EACH item, independent of the others: AMC TACZ Loot's classes
# (Common 4.6%, Uncommon 2.2%, Rare 1.0% in the chest) times FATOR (the user's request: x2). Barrel = half of the chest
# (BARREL_MULT, as in AMC TACZ Loot). The hard chest is worth the same as the chest. Luck: quality = 25% of the weight
# (QUALITY_SHARE), as there.
BASE_CHEST = {"Comum": 460, "Incomum": 220, "Raro": 100}
FATOR = 2.0
BARREL_MULT = 0.5
QUALITY_SHARE = 0.25
SCALE = 10000
# Per-item adjustment over the class chance (the user's request, 2026-11-17): the repair kit is a little more common
# than the engine and the jumper pack, which are Rare too (barrel 1.1% against 1.0%; chest 2.2% against 2.0%). The
# draws are independent: there is no competition between the items, this only orders them.
ITEM_MULT = {"repairkit": 1.1}

# The class of each item (the user's choice for blowtorch, repair kit, jumper pack and the two jerry cans; my proposal,
# approved, for seats, wheels and engine).
WORKSHOP = [("seat", "Comum"), ("wheel", "Comum"), ("wrench", "Comum"), ("engine", "Raro"), ("blowtorch", "Incomum"),
            ("repairkit", "Raro"), ("jumperpack", "Raro")]
GAS = [("jerrycan_full", "Incomum")]   # only the full jerry can (the user's request, 2026-11-17): the empty one left
FULL_JERRYCAN_FLUID = "lava"     # 1 bucket (1,000 mB) of lava, the modpack's fuel

# AMC's tables (theme/tier); the hard chest and the hard barrel use the table of the chest and of the barrel.
THEMES = {"workshop": WORKSHOP, "gas_station": GAS}
TIERS = {"chest": "chest", "chest_hard": "chest", "barrel": "barrel", "barrel_hard": "barrel"}

# Measured containers (AMC TACZ Loot's docs/containers.json): (chests, barrels) and copies per city (DrawTest,
# 492 cities)
STRUCTURES = {"medium workshop": ("workshop", 1, 61, 0.70), "small workshop": ("workshop", 2, 39, 1.22),
              "gas station": ("gas_station", 4, 26, 0.61), "small gas station": ("gas_station", 1, 3, 1.21)}

# ---------------------------------------------------------------------------------------- ITEMS
def ids(pack, system, subs):
    return ["mts:%s.%s%s" % (pack, system, s) for s in subs]

SEATS = {  # 4 seat models (the generic one came in at the user's request, 2026-11-16), the same chance, then the colour
    # the generic seat's colours come without the "_" in the id
    "generic": ids("mtsofficialpack", "seat", ["black", "blue", "brown", "cyan", "grey", "olive", "pink", "red",
                                               "white", "yellow"]),
    "car": ids("mtsofficialpack", "carseat", ["_black", "_brown", "_grey", "_maroon", "_red", "_tan", "_white"]),
    "sport": ids("oamp", "sportseat", ["_beige", "_blackbrown", "_brown", "_coffee", "_cream", "_gray", "_red"]),
    "vintage": ids("oamp", "vintageseat", ["_beige", "_blue", "_brown", "_gray", "_green", "_joepeach", "_light",
                                           "_maroon", "_purple", "_red"]),
}
# Only the offroad and vintage wheels (the user's request, 2026-11-16), the same chance, then the variant; the
# diameter of both (1.0) is taken by the 8 cars (0.4 to 1.0); no `wheelvintage_police` (police is exclusive to the
# police station).
WHEELS = {
    "offroad": ids("oamp", "wheeloffroad", ["_default"]),
    "vintage": ids("oamp", "wheelvintage", ["_lightblue", "_mint", "_sinty"]),
}
# The 5 engines of the 8 cars (Mercedes and Station Wagon, Mustang, Scout, Cagouille and Escargot, Camaro and
# Highwayman)
ENGINES = ids("mtsofficialpack", "enginemercedesm102", [""]) + ids("mtsofficialpack", "enginefordfe428", [""]) \
    + ids("mtsofficialpack", "engineamci4", [""]) + ids("oamp", "engineescargoti6", [""]) \
    + ids("oamp", "enginecamarov8", [""])
SINGLE = {"wrench": "mts:mts.wrench", "blowtorch": "mts:mtsofficialpack.blowtorch",
          "repairkit": "mts:mtsofficialpack.repairkit", "jumperpack": "mts:mts.jumperpack",
          "jerrycan_full": "mts:mts.jerrycan"}


def all_ids():
    out = set(ENGINES) | set(SINGLE.values())
    for g in list(SEATS.values()) + list(WHEELS.values()):
        out |= set(g)
    return out


def check_ids():
    """Each id exists as an item in the jars (the item model `assets/mts/models/item/<pack>.<name>.json`)."""
    have = set()
    libs = os.path.join(ROOT, "libs")
    for jar in ("Immersive Vehicles-1.21.1-24.0.0.jar", "MTS Official Pack-1.21.1-V29.jar", "OAmP-1.21.1-V3.jar"):
        with zipfile.ZipFile(os.path.join(libs, jar)) as z:
            for n in z.namelist():
                if n.startswith("assets/mts/models/item/") and n.endswith(".json"):
                    have.add("mts:" + n[len("assets/mts/models/item/"):-5])
    missing = sorted(all_ids() - have)
    if missing:
        sys.exit("ids that do not exist in the jars: %s" % missing)
    print("ids checked in the jars: %d items" % len(all_ids()))


# ---------------------------------------------------------------------------------------- CHANCES
def chance(cls, tier_kind, group=None):
    """Weight (of SCALE) of a class (and of an item, if given) in a kind of container: chest or barrel."""
    w = BASE_CHEST[cls] * FATOR * (BARREL_MULT if tier_kind == "barrel" else 1.0) * ITEM_MULT.get(group, 1.0)
    return int(round(w))


def pool(entry, w):
    q = round(w * QUALITY_SHARE)
    entry = dict(entry, weight=w, quality=q)
    return {"rolls": 1, "bonus_rolls": 0.0,
            "entries": [entry, {"type": "minecraft:empty", "weight": SCALE - w, "quality": -q}]}


def item_entry(group):
    ref = lambda t: {"type": "minecraft:loot_table", "value": "amcvehicles:pick/" + t}
    if group == "seat":
        return ref("seat")
    if group == "wheel":
        return ref("wheel")
    if group == "engine":
        return ref("engine")
    if group == "jerrycan_full":
        return {"type": "minecraft:item", "name": SINGLE[group], "functions": [{
            "function": "minecraft:set_components",
            "components": {"minecraft:custom_data": {"jerrycanFluid": FULL_JERRYCAN_FLUID}}}]}
    return {"type": "minecraft:item", "name": SINGLE[group]}


def pick_table(entries):
    return {"type": "minecraft:chest", "pools": [{"rolls": 1, "bonus_rolls": 0.0, "entries": [
        dict(e, weight=1) for e in entries]}]}


def item(name):
    return {"type": "minecraft:item", "name": name}


def ref(t):
    return {"type": "minecraft:loot_table", "value": "amcvehicles:pick/" + t}


def build():
    """Returns {relative path in data/amcvehicles/loot_table: table}, {path in loot_modifiers: modifier}."""
    tables, mods = {}, {}
    picks = {"seat": pick_table([ref("seat_" + k) for k in SEATS]), "engine": pick_table([item(i) for i in ENGINES]),
             "wheel": pick_table([ref("wheel_" + k) if len(v) > 1 else item(v[0]) for k, v in WHEELS.items()])}
    for k, v in SEATS.items():
        picks["seat_" + k] = pick_table([item(i) for i in v])
    for k, v in WHEELS.items():
        if len(v) > 1:
            picks["wheel_" + k] = pick_table([item(i) for i in v])
    for n, t in picks.items():
        tables["pick/%s.json" % n] = t
    for theme, groups in THEMES.items():
        for kind in ("chest", "barrel"):
            pools = [pool(item_entry(g), chance(cls, kind, g)) for g, cls in groups]
            tables["%s/%s.json" % (theme, kind)] = {"type": "minecraft:chest", "pools": pools}
        for tier, kind in TIERS.items():
            mods["%s_%s.json" % (theme, tier)] = {
                "type": "neoforge:add_table",
                "conditions": [{"condition": "neoforge:loot_table_id",
                                "loot_table_id": "abandonedmoderncity:chests/%s/%s" % (theme, tier)}],
                "table": "amcvehicles:%s/%s" % (theme, kind)}
    return tables, mods


def preview():
    print("chance per container (chest / barrel): " + ", ".join("%s %.2f%% / %.2f%%" % (
        c, chance(c, "chest") / 100, chance(c, "barrel") / 100) for c in BASE_CHEST))
    for name, (theme, chests, barrels, per_city) in STRUCTURES.items():
        row = []
        for g, cls in THEMES[theme]:
            e = chests * chance(cls, "chest", g) / SCALE + barrels * chance(cls, "barrel", g) / SCALE
            row.append("%s %.2f" % (g, e))
        print("  %-18s (%d chests, %d barrels): %s" % (name, chests, barrels, "; ".join(row)))
    for theme, groups in THEMES.items():
        for g, cls in groups:
            e = sum(pc * (c * chance(cls, "chest", g) + b * chance(cls, "barrel", g)) / SCALE
                    for (th, c, b, pc) in STRUCTURES.values() if th == theme)
            print("  per city: %-15s %.2f" % (g, e))


def write():
    tables, mods = build()
    base = os.path.join(DATA, "amcvehicles")
    for d in ("loot_table", "loot_modifiers"):
        shutil.rmtree(os.path.join(base, d), ignore_errors=True)
    for rel, t in tables.items():
        p = os.path.join(base, "loot_table", rel)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        json.dump(t, open(p, "w"), indent=2)
    os.makedirs(os.path.join(base, "loot_modifiers"), exist_ok=True)
    for rel, m in mods.items():
        json.dump(m, open(os.path.join(base, "loot_modifiers", rel), "w"), indent=2)
    g = os.path.join(DATA, "neoforge/loot_modifiers/global_loot_modifiers.json")
    os.makedirs(os.path.dirname(g), exist_ok=True)
    json.dump({"replace": False, "entries": ["amcvehicles:" + r[:-5] for r in sorted(mods)]}, open(g, "w"), indent=2)
    print("written: %d tables, %d modifiers" % (len(tables), len(mods)))


if __name__ == "__main__":
    check_ids()
    preview()
    if "--write" in sys.argv:
        write()
