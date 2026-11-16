#!/usr/bin/env python3
"""Finds the painted parking bays of an AMC structure (.nbt) and writes them for the mod.

A bay = the floor between two parallel painted stripes (straight runs of the stripe block, MIN_RUN+ long) that are
GAP_MIN to GAP_MAX cells apart and overlap by BAY_MIN_LEN+ cells. The car parks nose-in: its nose points to the end
of the bay that is closed (the other end, open, is the aisle).

usage: python3 -I tools/find_spots.py <file.nbt> <floor y of the template> [--stripe white_concrete]
                                       [--png out.png] [--out spots.txt] [--model name]
Output lines (template coordinates of the .nbt, as the structure block shows them), the format of
src/main/resources/amcvehicles/spots.txt:
  <model> bay <x0> <y> <z0> <x1> <z1> <facing>
x0..x1, z0..z1 = the cells of the bay (inclusive), y = the floor layer, facing = where the nose points in the
template: N (-Z), S (+Z), E (+X), W (-X).
"""
import argparse, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nbt import load

MIN_RUN = 4        # shortest stripe
GAP_MIN, GAP_MAX = 2, 5   # width of a bay between two stripes
BAY_MIN_LEN = 6    # the car is ~5.45 long
HEADROOM = 2       # free cells over the floor needed in the bay
CAR_W, CAR_REAR, CAR_FRONT = 2.0, 1.3, 4.15   # merc230: width, rear and front of the body from the rear axle

AIRS = {'air', 'cave_air', 'void_air'}
SOFT = ('vine', 'short_grass', 'tall_grass', 'fern', 'moss_carpet', 'cobweb', 'glow_lichen', 'dead_bush',
        'large_fern', '_carpet', 'torch', 'button', 'pressure_plate', 'snow[')


def base(s):
    return s.split('[')[0]


def free(blocks, x, y, z):
    s = blocks.get((x, y, z))
    return s is None or base(s) in AIRS or any(t in s for t in SOFT)


def runs(cells, axis):
    """Maximal straight runs of cells: axis 'z' = runs along z at fixed x -> (x, z0, z1)."""
    out = []
    for c in sorted(cells, key=lambda p: (p[0], p[1]) if axis == 'z' else (p[1], p[0])):
        fixed, var = (c[0], c[1]) if axis == 'z' else (c[1], c[0])
        if out and out[-1][0] == fixed and out[-1][2] == var - 1:
            out[-1][2] = var
        else:
            out.append([fixed, var, var])
    return [r for r in out if r[2] - r[1] + 1 >= MIN_RUN]


def bays(blocks, y, stripe):
    cells = {(x, z) for (x, yy, z), s in blocks.items() if yy == y and base(s) in stripe}
    found = []
    for axis in ('z', 'x'):
        rs = runs(cells, axis)
        for a in rs:
            # nearest stripe after a (on the fixed axis) that overlaps it
            best = None
            for b in rs:
                if b[0] <= a[0]:
                    continue
                lo, hi = max(a[1], b[1]), min(a[2], b[2])
                if hi - lo + 1 < BAY_MIN_LEN:
                    continue
                if best is None or b[0] < best[0][0]:
                    best = (b, lo, hi)
            if best is None:
                continue
            b, lo, hi = best
            gap = b[0] - a[0] - 1
            if not GAP_MIN <= gap <= GAP_MAX:
                continue
            if axis == 'z':
                found.append((a[0] + 1, lo, b[0] - 1, hi, 'z'))
            else:
                found.append((lo, a[0] + 1, hi, b[0] - 1, 'x'))
    return found


def open_beyond(blocks, y, cells):
    """How many of these cells (just past an end of the bay) have a floor and headroom: the aisle side."""
    n = 0
    for x, z in cells:
        if not free(blocks, x, y, z) and all(free(blocks, x, y + k, z) for k in range(1, HEADROOM + 1)):
            n += 1
    return n


def facing(blocks, y, bay):
    x0, z0, x1, z1, axis = bay
    if axis == 'z':
        before = open_beyond(blocks, y, [(x, z0 - k) for x in range(x0, x1 + 1) for k in (1, 2)])
        after = open_beyond(blocks, y, [(x, z1 + k) for x in range(x0, x1 + 1) for k in (1, 2)])
        return 'N' if after > before else 'S'
    before = open_beyond(blocks, y, [(x0 - k, z) for z in range(z0, z1 + 1) for k in (1, 2)])
    after = open_beyond(blocks, y, [(x1 + k, z) for z in range(z0, z1 + 1) for k in (1, 2)])
    return 'W' if after > before else 'E'


def blocked(blocks, y, bay):
    x0, z0, x1, z1, _ = bay
    bad = []
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if free(blocks, x, y, z) or not all(free(blocks, x, y + k, z) for k in range(1, HEADROOM + 1)):
                bad.append((x, z))
    return bad


def render(path, size, blocks, y, result):
    from PIL import Image, ImageDraw
    S = 12
    sx, _, sz = size
    img = Image.new('RGB', (sx * S + 40, sz * S + 40), (40, 40, 40))
    d = ImageDraw.Draw(img)
    for x in range(sx):
        for z in range(sz):
            s = blocks.get((x, y, z))
            if s is None or base(s) in AIRS:
                continue
            b = base(s)
            col = (245, 245, 245) if b == 'white_concrete' else (230, 190, 30) if b.startswith('yellow') else \
                (120, 120, 120) if all(free(blocks, x, y + k, z) for k in (1, 2)) else (70, 55, 45)
            d.rectangle([40 + x * S, 40 + z * S, 40 + x * S + S - 1, 40 + z * S + S - 1], fill=col)
    for x in range(0, sx, 5):
        d.text((40 + x * S, 4), str(x), fill=(255, 255, 255))
    for z in range(0, sz, 5):
        d.text((4, 40 + z * S), str(z), fill=(255, 255, 255))
    for i, (bay, face, bad) in enumerate(result):
        x0, z0, x1, z1, axis = bay
        d.rectangle([40 + x0 * S, 40 + z0 * S, 40 + (x1 + 1) * S - 1, 40 + (z1 + 1) * S - 1],
                    outline=(255, 60, 60) if bad else (60, 220, 60), width=2)
        cx, cz = (x0 + x1 + 1) / 2, (z0 + z1 + 1) / 2
        dx, dz = {'N': (0, -1), 'S': (0, 1), 'E': (1, 0), 'W': (-1, 0)}[face]
        half_len = (CAR_REAR + CAR_FRONT) / 2
        hx, hz = (abs(dx) * half_len + abs(dz) * CAR_W / 2, abs(dz) * half_len + abs(dx) * CAR_W / 2)
        d.rectangle([40 + (cx - hx) * S, 40 + (cz - hz) * S, 40 + (cx + hx) * S, 40 + (cz + hz) * S],
                    outline=(60, 140, 255), width=2)
        nx, nz = cx + dx * (half_len - 0.4), cz + dz * (half_len - 0.4)
        d.ellipse([40 + nx * S - 4, 40 + nz * S - 4, 40 + nx * S + 4, 40 + nz * S + 4], fill=(255, 60, 60))
        d.text((40 + (cx - 0.4) * S, 40 + (cz - 0.5) * S), str(i + 1), fill=(255, 255, 0))
    img.save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('nbt')
    ap.add_argument('y', type=int)
    ap.add_argument('--stripe', default='white_concrete')
    ap.add_argument('--png')
    ap.add_argument('--out')
    ap.add_argument('--model', help='model name (default: the file name)')
    a = ap.parse_args()
    size, blocks = load(a.nbt)
    result = []
    for bay in sorted(bays(blocks, a.y, set(a.stripe.split(','))), key=lambda b: (b[1], b[0])):
        result.append((bay, facing(blocks, a.y, bay), blocked(blocks, a.y, bay)))
    model = a.model or os.path.splitext(os.path.basename(a.nbt))[0]
    lines = []
    for i, (bay, face, bad) in enumerate(result):
        x0, z0, x1, z1, _ = bay
        print('%2d: x %d..%d z %d..%d (%d x %d) nose %s%s' % (i + 1, x0, x1, z0, z1, x1 - x0 + 1, z1 - z0 + 1, face,
                                                             '  BLOCKED %s' % bad if bad else ''))
        if not bad:
            lines.append('%s bay %d %d %d %d %d %s' % (model, x0, a.y, z0, x1, z1, face))
    print('%d bays, %d free' % (len(result), len(lines)))
    if a.png:
        render(a.png, size, blocks, a.y, result)
    if a.out:
        with open(a.out, 'w') as f:
            f.write('\n'.join(lines) + '\n')


main()
