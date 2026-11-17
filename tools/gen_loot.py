#!/usr/bin/env python3
"""Gera o loot de oficinas e postos do AMC Vehicles (complemento ao AMC TACZ Loot: só SOMA, não substitui nada).

Uso: python3 -I tools/gen_loot.py            prévia (esperado por estrutura e por cidade; não grava nada)
     python3 -I tools/gen_loot.py --write    apaga e reescreve src/main/resources/data/amcvehicles/{loot_table,loot_modifiers}
                                             e data/neoforge/loot_modifiers/global_loot_modifiers.json
O gerador confere cada id de item contra os jars em libs/ (assets/mts/models/item/<id>.json).
Mecanismo: um modificador de loot `neoforge:add_table` por tabela do AMC (`abandonedmoderncity:chests/<tema>/<tier>`),
como o AMC TACZ Loot; vários mods somam à mesma tabela. Vasos não recebem nada.
"""
import json, os, shutil, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data")

# ---------------------------------------------------------------------------------------- NÚMEROS (16 Nov 2026)
# Chance por container (em unidades de 1/10.000) de CADA item, independente dos outros: classes do AMC TACZ Loot
# (Comum 4,6%, Incomum 2,2%, Raro 1,0% no baú) vezes FATOR (pedido do usuário: x2). Barril = metade do baú (BARREL_MULT,
# como no AMC TACZ Loot). O baú difícil vale o mesmo que o baú. Sorte: quality = 25% do peso (QUALITY_SHARE), como lá.
BASE_CHEST = {"Comum": 460, "Incomum": 220, "Raro": 100}
FATOR = 2.0
BARREL_MULT = 0.5
QUALITY_SHARE = 0.25
SCALE = 10000
# Ajuste por item sobre a chance da classe (pedido do usuário, 17 Nov 2026): o repair kit é um pouco mais comum que o motor
# e o jumper pack, que também são Raro (barril 1,1% contra 1,0%; baú 2,2% contra 2,0%). Os sorteios são independentes: não
# há competição entre os itens, isto só os ordena.
ITEM_MULT = {"repairkit": 1.1}

# Que classe cada item tem (escolha do usuário para blowtorch, repair kit, jumper pack e os dois galões; minha proposta,
# aprovada, para bancos, rodas e motor).
WORKSHOP = [("seat", "Comum"), ("wheel", "Comum"), ("wrench", "Comum"), ("engine", "Raro"), ("blowtorch", "Incomum"),
            ("repairkit", "Raro"), ("jumperpack", "Raro")]
GAS = [("jerrycan_empty", "Incomum"), ("jerrycan_full", "Incomum")]
FULL_JERRYCAN_FLUID = "lava"     # 1 balde (1.000 mB) de lava, o combustível do modpack

# Tabelas do AMC (tema/tier); o baú difícil e o barril difícil usam a tabela do baú e do barril.
THEMES = {"workshop": WORKSHOP, "gas_station": GAS}
TIERS = {"chest": "chest", "chest_hard": "chest", "barrel": "barrel", "barrel_hard": "barrel"}

# Containers medidos (docs/containers.json do AMC TACZ Loot): (baús, barris) e cópias por cidade (DrawTest, 492 cidades)
STRUCTURES = {"oficina média": ("workshop", 1, 61, 0.70), "oficina pequena": ("workshop", 2, 39, 1.22),
              "posto": ("gas_station", 4, 26, 0.61), "posto pequeno": ("gas_station", 1, 3, 1.21)}

# ---------------------------------------------------------------------------------------- ITENS
def ids(pack, system, subs):
    return ["mts:%s.%s%s" % (pack, system, s) for s in subs]

SEATS = {  # 4 modelos de banco (o genérico entrou a pedido do usuário, 16 Nov 2026) com a mesma chance, depois a cor
    "generic": ids("mtsofficialpack", "seat", ["black", "blue", "brown", "cyan", "grey", "olive", "pink", "red", "white",
                                               "yellow"]),   # as cores do banco genérico vêm sem o "_" no id
    "car": ids("mtsofficialpack", "carseat", ["_black", "_brown", "_grey", "_maroon", "_red", "_tan", "_white"]),
    "sport": ids("oamp", "sportseat", ["_beige", "_blackbrown", "_brown", "_coffee", "_cream", "_gray", "_red"]),
    "vintage": ids("oamp", "vintageseat", ["_beige", "_blue", "_brown", "_gray", "_green", "_joepeach", "_light",
                                           "_maroon", "_purple", "_red"]),
}
# Só as rodas offroad e vintage (pedido do usuário, 16 Nov 2026), com a mesma chance, depois a variante; o diâmetro de
# ambas (1,0) é aceito pelos 8 carros (0,4 a 1,0); sem `wheelvintage_police` (polícia é exclusiva da delegacia).
WHEELS = {
    "offroad": ids("oamp", "wheeloffroad", ["_default"]),
    "vintage": ids("oamp", "wheelvintage", ["_lightblue", "_mint", "_sinty"]),
}
# Os 5 motores dos 8 carros (Mercedes e Station Wagon, Mustang, Scout, Cagouille e Escargot, Camaro e Highwayman)
ENGINES = ids("mtsofficialpack", "enginemercedesm102", [""]) + ids("mtsofficialpack", "enginefordfe428", [""]) \
    + ids("mtsofficialpack", "engineamci4", [""]) + ids("oamp", "engineescargoti6", [""]) \
    + ids("oamp", "enginecamarov8", [""])
SINGLE = {"wrench": "mts:mts.wrench", "blowtorch": "mts:mtsofficialpack.blowtorch", "repairkit": "mts:mtsofficialpack.repairkit",
          "jumperpack": "mts:mts.jumperpack", "jerrycan_empty": "mts:mts.jerrycan", "jerrycan_full": "mts:mts.jerrycan"}


def all_ids():
    out = set(ENGINES) | set(SINGLE.values())
    for g in list(SEATS.values()) + list(WHEELS.values()):
        out |= set(g)
    return out


def check_ids():
    """Cada id existe como item nos jars (o modelo de item `assets/mts/models/item/<pack>.<nome>.json`)."""
    have = set()
    libs = os.path.join(ROOT, "libs")
    for jar in ("Immersive Vehicles-1.21.1-24.0.0.jar", "MTS Official Pack-1.21.1-V29.jar", "OAmP-1.21.1-V3.jar"):
        with zipfile.ZipFile(os.path.join(libs, jar)) as z:
            for n in z.namelist():
                if n.startswith("assets/mts/models/item/") and n.endswith(".json"):
                    have.add("mts:" + n[len("assets/mts/models/item/"):-5])
    missing = sorted(all_ids() - have)
    if missing:
        sys.exit("ids que não existem nos jars: %s" % missing)
    print("ids conferidos nos jars: %d itens" % len(all_ids()))


# ---------------------------------------------------------------------------------------- CHANCES
def chance(cls, tier_kind, group=None):
    """Peso (de SCALE) de uma classe (e de um item, se dado) num tipo de container: baú ou barril."""
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
    """Devolve {caminho relativo em data/amcvehicles/loot_table: tabela}, {caminho em loot_modifiers: modificador}."""
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
    print("chance por container (baú / barril): " + ", ".join("%s %.2f%% / %.2f%%" % (
        c, chance(c, "chest") / 100, chance(c, "barrel") / 100) for c in BASE_CHEST))
    for name, (theme, chests, barrels, per_city) in STRUCTURES.items():
        row = []
        for g, cls in THEMES[theme]:
            e = chests * chance(cls, "chest", g) / SCALE + barrels * chance(cls, "barrel", g) / SCALE
            row.append("%s %.2f" % (g, e))
        print("  %-16s (%d baús, %d barris): %s" % (name, chests, barrels, "; ".join(row)))
    for theme, groups in THEMES.items():
        for g, cls in groups:
            e = sum(pc * (c * chance(cls, "chest", g) + b * chance(cls, "barrel", g)) / SCALE
                    for (th, c, b, pc) in STRUCTURES.values() if th == theme)
            print("  por cidade: %-15s %.2f" % (g, e))


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
    print("gravados: %d tabelas, %d modificadores" % (len(tables), len(mods)))


if __name__ == "__main__":
    check_ids()
    preview()
    if "--write" in sys.argv:
        write()
