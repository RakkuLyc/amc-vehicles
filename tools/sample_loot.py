#!/usr/bin/env python3
"""Amostra uma tabela do AMC no servidor de teste (RCON, Sorte 0) e compara cada item do AMC Vehicles com o esperado.

Uso: python3 -I tools/sample_loot.py <tabela, ex.: workshop/barrel> [N]
Gera a tabela `abandonedmoderncity:chests/<tabela>` N vezes num baú (os modificadores de loot de todos os mods se
aplicam) e conta, por container: bancos, rodas, motores, blowtorch, repair kit, jumper pack, galão vazio e cheio (o
cheio é o galão com `jerrycanFluid` no dado do item) e, para provar que o AMC TACZ Loot continua somando, os itens de
`tacz:`/`lrtactical:`/`firstaid:`. O esperado vem de gen_loot.py (CHANCE por classe, baú ou barril; _hard = baú/barril).
Mostra o desvio (em desvios-padrão da binomial): até 1,96 = ok; até 3 = limite (esperado em 5% dos itens por acaso);
acima de 3 = defeito.
"""
import collections, math, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from rconlib import Rcon
import importlib.util

spec = importlib.util.spec_from_file_location("gen", os.path.join(os.path.dirname(os.path.abspath(__file__)), "gen_loot.py"))
gen = importlib.util.module_from_spec(spec)
src = open(spec.origin).read().replace('\nif __name__ == "__main__":', '\nif False:')
exec(compile(src, "gen_loot", "exec"), gen.__dict__)


# a chave `id` inteira (não o fim de `jerrycanFluid: "lava"`, que também termina em `id: "`)
ID_KEY = re.compile(r'(?:^|[\s,{])id: "([a-z_0-9:.]+)"')


def group(chunk):
    m = ID_KEY.search(chunk)
    iid = m.group(1) if m else ""
    if iid.startswith("mts:"):
        if "seat" in iid: return "seat"
        if "wheel" in iid: return "wheel"
        if ".engine" in iid: return "engine"
        if "blowtorch" in iid: return "blowtorch"
        if "repairkit" in iid: return "repairkit"
        if "jumperpack" in iid: return "jumperpack"
        if "jerrycan" in iid: return "jerrycan_full" if "jerrycanFluid" in chunk else "jerrycan_empty"
        return "mts_other"
    if iid.split(":")[0] in ("tacz", "lrtactical", "firstaid"):
        return "addon"
    return "other"


def split_items(text):
    """Os itens de `data get block ... Items`: cada compound {...} do nível da lista (a ordem das chaves varia)."""
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
    print("tabela %s: %d containers (tipo %s)" % (table, n, kind))
    ok = True
    for g, cls in gen.THEMES[theme]:
        p = gen.chance(cls, kind, g) / gen.SCALE
        obs = have[g] / n
        sd = math.sqrt(p * (1 - p) / n)
        z = (obs - p) / sd
        inside = abs(z) <= 3
        ok &= inside
        print("  %-15s esperado %5.2f%%  observado %5.2f%% (%d de %d)  desvio %+.1f  %s" % (
            g, 100 * p, 100 * obs, have[g], n, z, "ok" if abs(z) <= 1.96 else ("limite (95%)" if inside else "FORA (3 desvios)")))
    stray = sorted(g for g in have if g in ("mts_other",) or (g in gen_names(theme) is False))
    print("  itens do AMC TACZ Loot (tacz/lrtactical/firstaid) em %.1f%% dos containers" % (100 * have["addon"] / n))
    print("  ids vistos:", dict(items_seen))
    if fluids:
        print("  galão cheio: fluido", dict(fluids))
    print("RESULTADO:", "nenhum item além de 3 desvios" if ok else "HÁ ITEM FORA (mais de 3 desvios)")


def gen_names(theme):
    return [g for g, _ in gen.THEMES[theme]]


main()
