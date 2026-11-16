#!/usr/bin/env python3
"""The numbers of each car for src/main/java/com/amcvehicles/Cars.java, read from the pack jars (no extraction).

usage: python3 -I tools/car_info.py <pack.jar> <pack id> <system name> [<system name> ...]
  e.g. python3 -I tools/car_info.py "libs/OAmP-1.21.1-V3.jar" oamp cagouille camaro escargot highwayman stationmerc
For each vehicle: the variants (subNames; police ones marked), the door variables (variables named door_* in the json),
the default parts, the instrument slots (hudX and default), and from the .obj model: the body extents (rear and front
of the origin, half width), the lift (bottom of the support blocks cube_*, shown when a wheel is missing) and the top.
The json of the packs is lenient (comments, trailing commas): it is read with regular expressions.
"""
import re, sys, zipfile


def strip(s):
    return re.sub(r'//[^\n]*', '', s)


def array(s, key):
    i = s.find('"%s"' % key)
    if i < 0:
        return ''
    j = s.index('[', i)
    depth = 0
    for k in range(j, len(s)):
        depth += {'[': 1, ']': -1}.get(s[k], 0)
        if depth == 0:
            return s[j:k + 1]
    return ''


def objects(arr):
    out, depth, start = [], 0, None
    for k, ch in enumerate(arr):
        if ch in '[{':
            if ch == '{' and depth == 1:
                start = k
            depth += 1
        elif ch in ']}':
            depth -= 1
            if ch == '}' and depth == 1:
                out.append(arr[start:k + 1])
    return out


def main():
    jar, pack, names = sys.argv[1], sys.argv[2], sys.argv[3:]
    z = zipfile.ZipFile(jar)
    for name in names:
        s = strip(z.read('assets/%s/jsondefs/vehicles/%s.json' % (pack, name)).decode('utf-8', 'replace'))
        subs = re.findall(r'"subName"\s*:\s*"([^"]*)"', s)
        police = [v for v in subs if 'police' in v]
        doors = sorted(set(re.findall(r'"!?(door_[a-z_]+)"', s)))
        fuel = re.search(r'"fuelCapacity"\s*:\s*([\d.]+)', s)
        print('== %s:%s  fuelCapacity %s' % (pack, name, fuel.group(1) if fuel else '?'))
        print('  civilian (%d): %s' % (len(subs) - len(police), [v for v in subs if v not in police]))
        print('  police: %s' % police)
        print('  door variables: %s' % doors)
        for i, p in enumerate(objects(array(s, 'parts'))):
            t = re.search(r'"types"\s*:\s*\[([^\]]*)\]', p)
            d = re.search(r'"defaultPart"\s*:\s*"([^"]+)"', p)
            if d:
                print('  part %d %s default %s%s' % (i, re.findall(r'"([^"]+)"', t.group(1)) if t else '', d.group(1),
                                                    ' (permanent)' if re.search(r'"isPermanent"\s*:\s*true', p) else ''))
        for i, ins in enumerate(objects(array(s, 'instruments'))):
            hx = re.search(r'"hudX"\s*:\s*(-?\d+)', ins)
            di = re.search(r'"defaultInstrument"\s*:\s*"([^"]+)"', ins)
            print('  instrument slot %d hudX %s default %s' % (i, hx.group(1) if hx else '?', di.group(1) if di else '-'))
        xs, ys, zs, cube = [], [], [], []
        cur = ''
        for line in z.read('assets/%s/objmodels/vehicles/%s.obj' % (pack, name)).decode('utf-8', 'replace').splitlines():
            if line[:2] in ('o ', 'g '):
                cur = line.split()[1]
            elif line.startswith('v '):
                x, y, zz = map(float, line.split()[1:4])
                xs.append(x); ys.append(y); zs.append(zz)
                if cur.lower().startswith('cube'):
                    cube.append(y)
        print('  model: rear %.5f front %.5f halfWidth %.5f lift %.5f top %.5f' % (
            -min(zs), max(zs), max(abs(min(xs)), abs(max(xs))), -min(cube) if cube else float('nan'), max(ys)))


main()
