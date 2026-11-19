# Snapshot of 0.8.1 (phase 0 of the 1.0.0 work, 2026-11-18)

The reference to prove, in the next phases (clean-up, refactoring, security, defects, optimization, translation), that
nothing changed by accident. Taken from the code of 0.8.1 (commit `c1d134a`), before any change.

## Files
- `resources_json_sha256.txt`: SHA-256 of the 21 JSON of `src/main/resources` and of `spots.txt`. To check:
  `(cd src/main/resources && find . -name '*.json' | sort | xargs sha256sum) | diff - <(head -21 docs/baseline/resources_json_sha256.txt)`.
- `jar_entries_0.8.1.txt`: the 60 entries of the jar (23 classes; the jar had 72,015 bytes).
- `constants_0.8.1.txt`: `javap -constants -p` of the `static final` constants of every class (draw, chances, lava 0.05,
  tank 0.15, battery 0.0, `EVERY` 10, `Pending`'s `NAME` etc.).
- `src/test/resources/golden/pending_old_world.dat` and `pending_phase0_world.dat`: samples of `Pending` written by real
  versions (the previous test world, made up to 0.8.1: `chunks` empty, 43 bytes; the world of phase 0: 1 pending chunk,
  ChunkPos 18,71, a car waiting for the chunks under its body). For a future test of "an old world still opens". The saved
  format is only `data.chunks` (a long array of ChunkPos). The keys `Generation.resolvedKey` (which use the order of the bay
  in `spots.txt`) live only in memory (`RESOLVED`, cleared when the server stops): they are not saved.
- The previous test world was kept in `run/world_before_phase0` (outside git).
- The golden test: `src/test/java/com/amcvehicles/GoldenDrawTest.java` and the reference
  `src/test/resources/golden/draw_golden.txt` (4,708 lines, SHA-256 `717de76353b328e4bf37b2dd6c02d50b5d7ad5038f3be386432ed99b25e4d8b7`).
  Part A: the pure draw, 200 copy seeds x every bay (civilian and police station) x every pump: 2,160 cars and 2,000 pump
  lines. Part B: AMC's real planner (world seeds 20261027 and 42, flat terrain at y 70), the world position and angle of
  each drawn car (174) and of each pump (374). If the reference is missing, the test writes it and fails once (so it gets
  looked at); on a difference, the output goes to `build/golden/draw_golden.actual.txt` and the failure shows the first
  different line.

## Numbers
- Build (`./gradlew clean build --rerun-tasks`, Java 21): 25 tests pass in 21 s, no compiler warning. With the golden
  test: 26 tests in 40 s.
- Projection of `DrawTest.projectionPerCity`: 492 cities, 18,549 bays (37.7 per city), 1,683 cars (3.42 per city, 9.07% of
  the bays), 41 cities (8.3%) without a car.
- Compiler: no `warning:`, `Note:`, `deprecat` or `unchecked` line in the build's log.
- Loot: `python3 -I tools/gen_loot.py --write` followed by `git diff --stat`: empty.
- Proof that the golden test catches a change: `DOOR_OPEN_CHANCE` from 0.5 to 0.51 made the test fail on line 205 (a door
  `doors=01` became `11`); with the file back (`git checkout`), it passed.

## Test server (new world, seed 20261027)
- It started in 17.8 s. `check_generation.py --load` in 6 boxes (found by `/amcs locate structure <category> <name>`):
  glass house (-351,434 .. -312,481), police station (-351,314 .. -298,351), blue and black medium workshop
  (-460,385 .. -426,410), abandoned house (-351,172 .. -323,217), wide building (321,1154 .. 351,1237) and yellow gas
  station (-2036,458 .. -2000,502): **4 cars in the boxes, 4 drawn, 4 in place** (`cagouille_police` at the police station;
  `escargot_lightyellow`, `scout_yellow` and `camaro_seagreen` in the wide building); the 8 pumps of the gas station placed
  ("already there" in `/amcv draw`).
- Tanks of the pumps (`data get block`): the one drawn with 3 buckets has `fluidLevel` 3000 of `lava`, the one with 1
  bucket 1000, an empty one `currentFluid ""`.
- `/amcv fill` at the glass house: "2 bays, 2 cars [scout_red, escargot_brown], 0 skipped".
- Log (166 lines; kept outside git in `run/baseline-0.8.1-server.log`, because it has the machine's paths): 12 ERROR and
  15 WARN, **none of the mod**: 11 `MTSERROR` of IV while reading the packs (the same as in 0.8.1), the tag
  `minecraft:placeable`, 2 refmaps of TACZ, 5 ambiguities of Minecraft's `teleport` command, AMC taking 15.3 s in a loading
  task, the offline mode warning of the test server and one "Can't keep up" of 2.7 s while forcing the generation of the 6
  areas. The mod's lines: lava at 0.05 at start, 4 cars and 8 pumps "(generated)".
- For phase 3 (noted, not touched): the test server runs with `online-mode=false`, RCON with password `teste` and
  `server-ip=` empty (it listens on every interface). (Fixed in 0.9.2: `server-ip=127.0.0.1`.)
