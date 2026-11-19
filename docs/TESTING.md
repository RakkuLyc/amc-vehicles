# AMC Vehicles — tests

## Test server
- `run/` (outside git): `server.properties` with RCON on port 25576 and password `teste`, server on 25598. Stage 1: flat
  world (`level-type=minecraft:flat`); since stage 2: normal world, seed `20261027` (the same as AMC's test battery).
  `run/mods` with `Immersive Vehicles-1.21.1-24.0.0.jar`, `MTS Official Pack-1.21.1-V29.jar` and (since stage 2)
  `abandonedmoderncity-0.1.0.jar`. Since 0.9.2 the server listens only on 127.0.0.1 and the tools read the password from
  `AMCV_RCON_PASSWORD` (default `teste`).
- Start: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runServer -Dorg.gradle.java.installations.paths=$JAVA_HOME`.
- Commands: `python3 -I tools/rcon.py "command 1" "command 2"`.
- The lines `MTSERROR: Welcome to MTS...`, `Could not do model-based legacy compats on mtsofficialpack:engineamci4` and
  `For input string: "roller2"` in the log are messages of IV itself while it loads the pack, before this mod does
  anything (I did not test the server without this mod).

## Stage 1 — the Mercedes (2026-11-16, version 0.1.0)
Run on the test server, from the console, at (0, -60, 0) and around, with the flat ground at y -61:

| Command | Reply |
|---|---|
| `execute positioned 0 -60 0 rotated 0 0 run amcv mercedes white true false` | `_white`, nose to the south, hood open, trunk closed, 1500/15000 mB (lava), 1 engine taken out, parts `9:mts:invisible_standing`, `11:mtsofficialpack:gloveboxscout` |
| `... positioned 10 -60 0 rotated 90 0 run amcv mercedes police false true` | `_police`, west, hood closed, trunk open, the same |
| `... positioned 20 -60 0 rotated 180 0 run amcv mercedes` | `_beige` (drawn), north, hood open, trunk closed (drawn), the same |
| `... positioned -10 -60 0 rotated 270 0 run amcv mercedes salmon true true` | `_salmon`, east, hood and trunk open, the same |

Checked in the saved data (`save-all flush` and `data get entity`), **and again after restarting the server**:
- only the parts `part_9` (`mts:invisible_standing`) and `part_11` (`gloveboxscout`): no engine, wheels or seats;
- `fuelTank`: `currentFluid: "lava"`, `fluidLevel: 1500.0`;
- `instrument0_systemName: "instrument_car_fuelqty"`, `instrument1_systemName: "instrument_car_spedometer_kph"`;
- `door_hood: 1.0` on the white one (hood open); absent on the police one (closed = 0, IV does not save zero);
  `door_trunk: 1.0` on the police one; `door_l` and `door_r` 1.0 on both (doors open, the pack's default);
- `anglesy: -90.0` on the police one (nose to the west, Minecraft angle 90 with the sign flipped).

**The user's test in the game (0.1.0):** `Mercedes_salmon`, nose to the south (the announced side), hood closed and trunk
open as in the command's reply; side door open (the pack's default in 0.1.0).

**The user's test in the game (0.1.1):** "everything is working" (2026-11-16); stage 1 approved.

## Version 0.1.1 — drawn doors and the gauge to the right (2026-11-16)
Test server, nose to the south:

| Command | Reply |
|---|---|
| `amcv mercedes red false false true false` | left door open, right closed |
| `amcv mercedes` (3 times) | `_brown` both closed; `_silver` and `_black` only the right one open |

Saved data of the red one: `door_l: 1.0`, no `door_r` (closed = 0), `instrument1_systemName:
"instrument_car_spedometer_kph"` (middle) and `instrument2_systemName: "instrument_car_fuelqty"` (right), no instrument in
0.

## Stage 2 — bays of the wide building and `/amcv fill` (2026-11-16, version 0.2.0)

### Detection (`tools/find_spots.py`)
`python3 -I tools/find_spots.py <abandoned_wide_building.nbt> 0`: 36 bays between the `white_concrete` stripes of the floor
(layer 0), 34 free: north row x 11..77 (17 bays of 3 × 8, nose N), south row x 3..77 (19 of 3 × 9, nose S), of which
x 43..45 and 47..49 (z 21..27) BLOCKED by the stairwell. Image: `docs/spots/abandoned_wide_building.png`.

### Unit tests (`PlacementTest`, `./gradlew test`, against AMC's code)
- `everyModelOfSpotsExists`: every model of `spots.txt` exists in AMC.
- `stripesOnBothSidesInEveryRotation`: for each bay and each rotation 0..3, the cells right outside the two long sides of
  the bay, turned by `Placement.turn`, are `white_concrete` in AMC's `Def.code` (the blocks it places, with the
  overgrowth) in 60%+ (passed).
- `carInsideItsBayAndAlongIt`: the car's body (the columns it covers) lies inside the turned bay, the nose along the long
  side and the height on top of the floor.
- `turnedDirectionMatchesTurnedPoints` and `yawOfDirections`: the turned direction matches the turned points; the angle of
  each direction.
- **The test catches errors**: swapping on purpose the turn of 1 quarter for that of 3, `stripesOnBothSides...` failed
  ("rot 1: 0 of 16 stripe cells") and so did `turnedDirection...`; the code was put back and the 5 passed.

### In the world (seed 20261027)
- `/amcs locate structure building_large abandoned_wide_building` (from 0, 100, 0): box 321,1154 .. 351,1237 (turned:
  31 × 84), corner 321 1154, rotation 1, city ground y 67 (garage floor y 59).
- `forceload add 300 1130 380 1260` and `execute positioned 336 60 1195 run amcv fill`: **34 bays, 34 cars, 0 skipped**.
- `python3 -I tools/check_bays.py 336 1195 60`: the 34 cars at y 59.99 (on the floor), **10 of 10 white stripe cells** in
  the two columns 2 blocks to each side of the middle of the body, along the 5 blocks of the body, on all of them; the row
  at x 342.57 with the nose to the east and the one at x 329.93 to the west: both to the edge of the box (x 351 and 321),
  that is, facing the wall.

### Blocked bay and repeated `fill` (0.2.0)
- A second `/amcv fill` with the 34 cars there: **0 cars, 34 skipped** ("occupied by a vehicle at 342 59 1166"...): it
  does not stack.
- `kill` of the cars, `setblock 343 60 1170 minecraft:stone` (inside bay 2) and `fill`: **33 cars, 1 skipped** ("bay 2:
  Stone at 343 60 1170"); the multi-line reply shows whole in the console.

### Direction of the nose (`NoseYawTest`, with IV's jar on the tests' classpath)
`RotationMatrix.setToAngles(0, -yaw, 0)` applied to the model's +Z (the front of the merc230): yaw 0 → +Z (south), 90 → -X
(west), 180 → -Z (north), 270 → +X (east); and `Placement.yaw` agrees with it in the 4 directions. So the "nose to the
east/west" of `check_bays.py` (which reads `anglesy` with the same rule) does not depend only on the assumption of the
sign.

**Not seen in the game:** the look in the garage (the whole car inside the bay, without going through walls or pillars)
and the garage in other rotations in the world (only rotation 1 was tested in the world; the 4 in the unit tests).

## Stage 3 — generation in the world (2026-11-16, version 0.3.0)

### Unit tests (`DrawTest`)
- `sameInputSameCar`: the draw is deterministic.
- `chancesAndColours` (2,000,000 bays): 2.99% with a car; hood 50.1%, trunk 50.0%, left door 49.8%, right 50.1%; the 15
  civilian colours between 3,870 and 4,088 (1/15 ± 0.5%); no police one.
- `policeOnlyPoliceColours`: at the police station only `_police` and `_police2`.
- `projectionPerCity`: AMC's real planner (flat terrain, seeds 20261027, 3478477863671517581, -3297019724594858492, 1 and
  42; the cities of 12 × 12 cells of each), with the draw by the position of the bay: 492 cities, 172 with a wide building
  (35.0%), 5,848 bays (11.9 per city), 159 cars (0.32 per city; 2.72% of the bays, within chance for 3%: the expected
  would be 175 ± 13); cities with 0 cars 380 (77.2%), 1 74 (15.0%), 2 30 (6.1%), 3 7 (1.4%), 4 1 (0.2%); 0.92 car per wide
  building.

### In the world (seed 20261027)
A first round with the draw by the order of the bay and a queue in memory; the reviewer pointed at two paths the normal
game reaches and the tests do not (a chunk that drops below full and comes back without a new load event; a chunk with
two cars where one waits, which repeated the log every 10 ticks), and that the draw by order would change if the bays of a
model were reordered. Fixed (the mod goes through `Pending`; resolved cars are not tried again; the draw uses the position
of the bay; "occupied" only by `mts:builder_existing`) and **everything run again, in a new world**:

- **Exact count**: 12 wide buildings (`/amcs locate` from a grid of 7 × 7 points, 736 apart),
  `tools/check_generation.py --load` (forceload of each box, which generates it): in all of them, cars in the box = drawn =
  "occupied by a vehicle" in `/amcv draw`; **11 cars, 11 drawn, 11 in place**; 11 `(generated)` lines in the log, no bay
  skipped.
- **Many cars per chunk (chance switched to 100% only for this test, then put back to 3%: the diff of `Draw.java` does not
  have the line, and `javap -constants` of the jar shows `CHANCE_PER_BAY = 0.03d`)**: new world; `forceload` only of chunk
  162,207 (near the box 2593,3265): bays 8 to 17 (in the full chunks 162..164 of row 205) placed, 10 lines in the log, the
  others waiting for their chunks to be born; `forceload` of the whole box: **33 cars and 1 bay skipped** ("bay 25 skipped
  (generated): Chest at 2643 64 3272", a chest drawn by AMC inside the bay), **34 lines, one per bay, none repeated** (the
  same 10 s later), `pending chunks 0`.
- **Stripes, 4 rotations in the world** (`check_bays.py`, in the first round; the turn and the position did not change with
  the fixes): the generated cars of the boxes -1300,-1976 (3) and 385,1857 (3) and -2559,-1240 (2), and `/amcv fill` in the
  boxes 1000,-2410 (rotation 2) and 2593,-1087 (rotation 0): **all with 10/10 stripe cells**, 17 noses N and 17 S in the
  boxes of rotation 0 and 2, E/W in those of rotation 1 and 3.
- **Restart** (first round): 79 cars before and 79 after; no new `[AMCV] Mercedes` line; the 10 boxes generated again
  11/11/11; `pending chunks 0`.
- **Edge (a full chunk that does not tick)** (first round): `forceload add` only of chunk 166,204: the Mercedes whose body
  lies wholly in chunk 164,204 (2 from the forced one: full, no tick) was placed ("(generated)"); saved data the same as
  a normal car's (only invisible_standing and gloveboxscout, 1,500 of lava, speedometer in 1, fuel in 2, doors and trunk
  drawn).
- **A pending chunk survives a restart** (first round): owner in chunk -243,-304 (2 from the forced -245,-304: full) and
  body also in -242,-304 (3 away: not full): `pending chunks 1`, no car; server stopped and restarted: still `pending
  chunks 1`, no car; `forceload add` of -242,-304: the car appeared, `pending chunks 0`.
- **A pending chunk survives unloading** (second round): the silver Mercedes of bay 13 of the box 4648,-415 (owner 291,-25,
  body also in 292,-25): `forceload` of 289,-25 → `pending chunks 1`, no car; `forceload remove` and 20 s → still 1, no car;
  `forceload add` again → 1, no car; `forceload add` of 292,-25 → **the car appeared**, `pending chunks 0`.

### Tools fixed along the way (they were not defects of the mod)
- `check_bays.py` paired the list of positions with that of `anglesy`, but IV does not save `anglesy` when it is 0 (nose
  to the south): the lists went out of step and the noses came out wrong. Now it asks the angle of each car (absent = 0).
- RCON splits replies above 4096 bytes; the client read only the first piece (`tools/rconlib.py` now reads all of them).
  Sending the command and a marker stuck together broke the connection: the game's `RconClient` reads up to 1,460 bytes,
  handles one packet per read and closes when a read brings less than 10 bytes (checked in Minecraft 1.21.1's code); now
  one packet goes at a time.
- AMC's first command after the server starts plans the cities (~10 s, "Planning cities"; the server stands still
  meanwhile).

**Not seen in the game:** a car generated by itself, with a player walking (the tests used forceload; the edge of the
player's view is the "full without tick" case, tested above).

## Stage 4 — the other structures (2026-11-16, version 0.4.0)

### Survey and proposal
- A sweep of all 30 models, every layer, every block with 8+ cells in the layer as a possible stripe (`find_spots.py` per
  block): apart from the wide building, only false positives (the floor planks of the houses and of the ramp building, the
  white concrete grid of the tower's lobby, the floor of the bluish building and of the apocalyptic one) and, at the gas
  station, the islands of the pumps themselves (edged with red terracotta). Maps of covered/open floor to find the garages.
- Proposal (`docs/spots/etapa4_propostas.png`), approved by the user; he added the large pharmacy and chose the drawn nose
  at the gas stations. Adjustments before the proposal, from the check of `find_spots.py --bays`: medium workshop (a column
  at x 6: bays at x 8..10 and 13..15), modern house (blocks at z 9: bays from z 10), large pharmacy (the hedge of the front
  at z 8..9: bays up to z 7).

### Unit tests
- `PlacementTest.floorAndRoomInEveryRotationAndVariant` (new): every bay, in the 4 rotations, in **every colour variant**
  of the model, has a floor in every cell and 2 free blocks (air, structure void, plants, carpets, vines) on top, in AMC's
  `Def.code` (with the overgrowth): 10,628 cells checked. **The test catches an error**: with the medium workshop's bay
  back at x 6..8, it failed ("workshop_medium_blue_black ... cell 6,4: minecraft:polished_blackstone 1 over the floor");
  the file was put back.
- `stripesOnBothSidesInEveryRotation`: now only for the models of painted stripes (the wide building).
- `carInsideItsBayAndAlongIt`: both ways of the bays with a drawn nose.
- `DrawTest.projectionPerCity` (the 57 bays): 16,567 bays in 492 cities (33.7 per city), 497 cars (1.01 per city,
  3.00%); cities with 0 cars 196 (39.8%), 1 166 (33.7%), 2 80 (16.3%), 3 31 (6.3%), 4 18 (3.7%), 6 1 (0.2%).

### In the world (seed 20261027, new world)
- One copy of each new structure (`/amcs locate structure ...` from 0, 100, 0). **Generation, exact count**
  (`check_generation.py --load` in the 9 boxes): 8 without a drawn car and the police station with 1 (`Mercedes_police2`,
  bay 1): **1 car, 1 drawn, 1 in place**.
- **`/amcv fill`** in each: medium workshop 2/2 (rotation 2), small workshop 2/2 (rotation 2), gas station 4/4 (rotation 3),
  small gas station 2/2 (rotation 0), large pharmacy 4/4 (rotation 0), police station 4/4 (rotation 2), garage house 1/1,
  suburban house 2/2 (rotation 2); modern house **0/2 in the first round: "Moss Carpet at 242 70 184"**: the moss carpet of
  AMC's overgrowth has a collision of 1/16 and the check treated it as an obstacle; fixed (`AmcWorld.THIN`: on the first
  block over the floor, up to 1/4 of height does not block) and again: **2/2**. Total 23 cars.
- **Police station**: the 4 cars only of police colours (2 `_police`, 2 `_police2`); `check_bays.py ... white_concrete`:
  6, 7, 8 and 7 of 10 stripe cells (the lines are worn in the model), nose to the building.
- **Large pharmacy**: `check_bays.py` gives 2 to 4/10, because it looks for the stripe 2 blocks from the middle and these
  bays are 4 wide (the stripe is 3 away on one side); checked by the sum: bay x 2..5, z 1..7 of the copy with corner
  -317,172, rotation 0, middle at x -313.0, z 176.5; the car is at x -313.00, z 175.075 (= 176.5 - 1.425, nose to the
  south). Exact.
- **Gas stations**: mixed noses as asked (gas station: 3 N and 1 S; small gas station: 1 E and 1 W).

- **The second car of the `kill`** (2 removed, 1 in the 9 boxes): `Mercedes_blue in gas_station_small_yellow bay 2 at 309
  70 338 (generated)`, a neighbouring small gas station, in a chunk at the edge of the loaded area.
- **Generation in every new bay (chance switched to 100% only for this test, then put back to 3%: `git diff` empty and
  `javap -constants` of the jar with `CHANCE_PER_BAY = 0.03d`, build with `--rerun-tasks`)**: new world, the same 9 boxes:
  **23 cars, 23 drawn, 23 in place**, no bay skipped; 28 `(generated)` lines (the 23 and 5 of neighbouring structures in
  the chunks of the edge), none repeated; noses of the gas station 2 N and 2 S, of the small gas station 1 E and 1 W (the
  path of the drawn nose in the generation); `pending chunks 0`.
- **Cars on the moss carpet**: the 2 of the modern house, seconds later, at y 69.991 (the floor of the ground layer is at
  y 69 and the car stands on top of it, like the others: ~0.01 below the top of the block); the carpets are still there
  (`execute if block 242 70 184 minecraft:moss_carpet` passed).

**Not seen in the game:** the look in these structures (the car inside the garage without going through a wall, beside
the island without going through the pump, before the pharmacy's hedge); the other colour variants only in the unit tests
(in the world, one variant of each).

## Version 0.4.1 — garages of the abandoned house and of the small building (2026-11-16)
- Pointed at by the user. Abandoned house: the front room on the left has no front wall (z 13, air up to 4 high), stone
  floor; small building: ground floor on pillars open at the front, layers 1 to 5 free. Proposal
  (`docs/spots/garagens_propostas.png`) checked with `find_spots.py --bays`: bay 3 of the small building hit a spruce
  stair bench at x 15..16, z 12 and moved to x 12..14. Approved by the user ("Approve the 6").
- Unit tests: all pass; `floorAndRoom` 11,204 cells; projection 19,905 bays in 492 cities (40.5 per city), 599 cars (1.22
  per city, 3.01%), 162 cities without a car (32.9%); abandoned house 0.07 per copy, small building 0.11.
- In the world (seed 20261027, new world): 2 copies of each (`/amcs locate` from 0,0, 1500,1500 and -1500,1500).
  Generation: no car drawn in the 4 (3%), 0 in the world, the same. `/amcv fill`: abandoned house 2/2 (rotations 0 and 2),
  small building 4/4 (two copies of rotation 1), no bay skipped. The path of the generation is the same as the other
  bays' (only the data changed); the 100% round was not redone.

## Version 0.4.2 — cars resting on the support blocks, not buried (2026-11-16)
- The user tested with a 100% test jar (not versioned) and saw the cars half buried (screenshot: the sill and the wheel
  boxes below the floor, a dark block sunk under the front wheel box).
- Cause, measured in the pack: `merc230.obj` has the objects `cube_fl/fr/rl/rr` (support blocks at the positions of the
  wheels, visible when the wheel is missing: `part_present_N` = 0 and `terrain_distance` 0..0.7 in `merc230.json`) with
  the lowest point at y -0.46875 (the lowest of the model; the body -0.1875); the lowest collision boxes also go down to
  -0.47. The origin was put on the floor, so everything sank 0.47. The highest point of the model: 1.625 (the light bar of
  the police colours), body 1.5625.
- Fix: origin 0.46875 over the floor (`Placement.CAR_LIFT`) and 3 free blocks required (`Placement.HEADROOM`, top of the
  car at 2.09). Unit tests: all pass with 3 blocks (`floorAndRoom`, 11,204 cells: the 63 bays fit).
- On the server (new world): `/amcv fill` at the police station 4/4 and in the wide building 34/34 (3 free blocks in the
  world); position right after and 30 s later, in chunks that tick: y 69.460175 (floor at y 69) and 57.460175 (floor at
  57): still, resting on the blocks (their base 0.0086 inside the floor, the same settling as before).
- 100% test jar rebuilt with the fix (`amcvehicles-0.4.2-TESTE-100pct.jar`, only local; `javap -constants`:
  `CHANCE_PER_BAY = 1.0d`, `CAR_LIFT = 0.46875d`); the normal jar has `CHANCE_PER_BAY = 0.03d`.

**Not seen in the game:** the fixed look (it needs the user's client).

## Stage 5 — 8 models (2026-11-16, version 0.5.0)

### Proof of the pump (for stage 6)
- `/amcv pump` on the server: `execute positioned -320 69 330 rotated 0 0 run amcv pump 2` and `... -325 69 330 rotated
  90 0 run amcv pump 0`: both placed; `data get block`: `id: "mts:builder_fluidtank"`, `packID: "mtsofficialpack"`,
  `systemName: "fuelpump"`, `anglesy` -180 and 90, `tank` with `currentFluid: "lava"`, `currentFluidMod: "minecraft"`,
  `fluidLevel: 2000.0` on the first and empty on the second. After restarting the server: the same. The reflection on the
  two `protected` members of IV worked.

### Unit tests (15, all pass)
- `CarsTest.tableMatchesThePacks`: the table of `Cars` against the jars (read as zip): each colour exists, the police ones
  are all those of the pack, the variables of the doors and of the trunk exist (the Mustang has no `door_trunk`), the
  hood exists, `lift` = base of the support blocks and `top` = highest point of the `.obj` (up to 1e-4), and they fit in 3
  blocks.
- `DrawTest.chancesModelsAndColours` (4,000,000 bays): 3.00% with a car; the 8 models between 14,838 and 15,152 (1/8 ±
  0.6%); each civilian colour of each model near 1/n of the model; no police one; hood 50.1%, trunk 50.1% (only on the
  models with a trunk), doors 50.1%.
- `DrawTest.policeOnlyPoliceModelsAndColours` (300,000): `cagouille_police` 100,105, `mustang_police` 99,956,
  `mercedes_police` 49,779, `mercedes_police2` 50,160 (1/3, 1/3, 1/6, 1/6).
- `PlacementTest.carInsideItsBayAndAlongIt`: the **8 models** inside all 63 bays, in the 4 rotations and in both ways of
  the drawn bays (the Mustang, 2.97 wide, and the Cagouille, 6.38 long, the tightest).
- The projection per city does not change (index 0, "the bay has a car", is the same): 599 cars, 1.22 per city.

### In the world (seed 20261027)
- `/amcv lineup` (from -335, 69, 305, nose to the south): **12 cars**. Replies: only the engine taken out on each (on the
  Escargot also the spare wheel `wheellarge`); fuel 1,500 (Mercedes, Cagouille, Escargot, Station Wagon, Scout), 1,000
  (Camaro, Highwayman), 800 (Mustang), lava; gauges: Mercedes and Cagouille 1 and 2, Camaro and Highwayman 0 and 1,
  Escargot 2 and 1, Station Wagon 0 and 2, Mustang and Scout 2 and 3; doors per model (the Station Wagon with 4, the
  Mustang without a trunk, the Scout with `door_tailgate`).
- Saved data (`save-all`, `data get entity`): on all of them, **only** `instrument_car_spedometer_kph` and
  `instrument_car_fuelqty` in the gauges above (no tachometer, clock or temperature); parts: the permanent ones, plus the
  Scout's pickup bed and sticker and the Mustang's mirror ornament. Height 30 s later, floor at y 69: Mercedes, Escargot
  69.460; Cagouille, Highwayman 69.429; Camaro 69.491; Mustang 69.4375; Station Wagon and Scout 69.46875 (chunk without
  tick): each on its own `lift`.
- Generation (`check_generation.py`, in a world where some areas had already been generated by 0.4.2): the 3 boxes
  generated now match (wide buildings -1300,-1976: `mustang_whitestripe`; 385,1857: `highwayman_blue`, `camaro_brown`,
  `scout_tan`; gas station 2593,-351: none); the 2 generated before (wide building 1000,-2410 and police station) have the
  cars of the old `fill` and do not count.
- `/amcv fill` at the police station: `[mustang_police, cagouille_police, mercedes_police, cagouille_police]`, only police
  cars.

**Not seen in the game:** the look of the 7 new models (height, doors, dashboard), which only the user's client shows.

## Stage 6 — fuel pumps and balance (2026-11-16, version 0.6.0)

### Unit tests (22, all pass)
- `PumpTest.floorAndRoomInEveryRotationAndVariant`: the 10 pumps, in the 5 colours and 4 rotations (200 positions): floor
  under, the cell and the one above free in AMC's `Def.code`. **It catches an error**: with a pump on purpose at x 22, z 11
  (a pillar of the canopy) it failed ("minecraft:chiseled_polished_blackstone at +0"); the file was put back.
- `pumpsOutOfTheBays`, `facingTurnsWithTheStructure`, `pumpKeysNeverBayKeys`, `pumpsOfTheGasStations` (8 and 2).
- `thirtyPercentWithOneBucket` (1,000,000): 30.00% with 1 bucket. `pumpsOfAStationIndependent` (200,000 gas stations):
  mean 2.400 pumps with lava per gas station and variance 1.679 (binomial of 8 with 0.3: 2.4 and 1.68), histogram
  [11542, 39476, 59288, 50937, 27201, 9326, 1951, 263, 16]: 6 or more in 1.1% of the gas stations.

### In the world (seed 20261027, new world)
- **Balance**: in the log at start, `[AMCV] Lava as a fuel set to 0.35 (server starting; was: diesel 1.0, avgas 1.0,
  gasoline 1.0); IV's config file is not changed`. The first round had also changed `furnace` and `brewing_stand` (they
  are not engine ones): fixed. `config/mtsconfig.json` with `"lava": 1.0` in the 5 types before and after two runs of the
  server.
- **`/amcv draw` before generating** (gas station 2593,-351 and small gas station 385,172): it lists the pumps with
  position, chunk and drawn lava, "chunks around not ready", without forcing a load.
- **Waiting and AMC's repair** (small gas station, pumps in chunk 24,11): `forceload` of chunk 22,11 (24 full, the
  neighbour 25 not): `pending chunks 1`, no pump. A pump placed by hand (`/amcv pump 1`) at 397, 70, 181 (a cell of air in
  the `.nbt`). `forceload` of 26,11 (generates 25): the generated pump 1 appeared (0 bucket, "generated"), `pending chunks
  0`, and the hand-placed one **stayed there** (1,000 mB): AMC's repair did not go over the cell; the rule of the 8
  neighbours stays as a margin (see `CLAUDE.md`).
- **Large gas station**: `forceload` of the box: the 8 pumps placed ("generated"), lava 1, 0, 0, 1, 1, 1, 1, 1 bucket as
  the draw of `draw`; `data get block`: `systemName "fuelpump"`, `fluidLevel 1000` on those with lava, no level on the
  empty ones; `anglesy` 90 on the 8 (the copy is in rotation 3).
- **Restart**: the pumps stay (the same lava); `/amcv fill` at the small gas station: "already there" on the 2.

### Review before delivery (0.6.0)
- **Consumption**: in IV's `PartEngine` the consumption is `getTotalFuelConsumption() * fuelUsageFactor / fuelFactor`:
  the potency divides, so 0.35 makes each bucket yield ~1/3 (the estimate of ~0.45 city per bucket holds, from the user's
  measure).
- **Config file**: IV's `saveToDisk` is also called by `ControlSystem` and by `GUIConfig` (IV's controls and config
  screen, during the game). Added: the values come back when the server stops and when a client leaves a server; on the
  test server, the log says `[AMCV] Lava as a fuel put back to IV's values (server stopped): {diesel=1.0, avgas=1.0,
  gasoline=1.0}` and the file keeps 1.0 in the 5 types. A save made with a world open still takes 0.35 to the file
  (documented).
- **The cars' tank**: `EntityFluidTank.fill` only joins the same fluid of the same mod (or `wildcard`): with the mod `""`
  a bucket of lava (`minecraft`) would be refused. Now the tank is saved with `currentFluidMod: "minecraft"` (checked in
  `data get entity` of a car of `/amcv car`). Pouring the bucket was not tested (it needs a player).
- **Cells of the pumps**: in the 10 variants, all air in the `.nbt`; the game's rule now also takes plants, vines,
  cobwebs and thin blocks, like `PumpTest`.

**Not seen in the game:** the look and the front of the pumps, filling a car at the pump and with a bucket, the new
consumption of lava (it needs driving) and the mod loading on the client (`ClientHooks` is new).

## Version 0.6.1 — no cars at the gas stations (2026-11-16)
- The user's request after testing 0.6.0: the cars at the gas stations were in the way of filling up a built car. The 6
  bays of the gas stations left `spots.txt`; the pumps stay. The draw of the other bays does not change (it is by the
  position of the bay).
- Unit tests: all pass; projection 17,507 bays in 492 cities (35.6 per city), 527 cars (1.07 per city, 3.01%), 187 cities
  without a car (38.0%).
- Server: `/amcv fill` at the small gas station and at the gas station: "0 bays, 0 cars", pumps "already there"; `/amcv
  draw` at the gas station: 0 bays, the 8 pumps listed.

## Version 0.6.2 — lava of the pumps: 50% empty, 50% with 1 to 3 buckets (2026-11-16)
- The user's request (first "back to the original, 0 to 3", then "50% chance of having 0, 50% chance of having 1, 2 or
  3"; read as 1/6 for each of 1, 2 and 3).
- `PumpTest.halfEmptyHalfOneToThree` (1,200,000): 0 bucket 599,997 (50.0%), 1 199,731, 2 199,912, 3 200,360 (16.7% each).
  `pumpsOfAStationIndependent` (200,000 gas stations): mean 7.998 buckets per large gas station (expected 8) and variance
  10.636 (expected 32/3 = 10.67).
- New world: large gas station and small gas station generated; the 10 pumps with the drawn lava (gas station: 0, 1, 1,
  0, 0, 0, 0, 0 buckets; small gas station: 2 and 0), `fluidLevel` 1000 and 2000 where there is lava, no level on the empty
  ones.

## Version 0.7.0 — loot of workshops and gas stations (2026-11-16)

### Unit tests (`LootDataTest`, 25 in all, all pass)
- `everyItemExistsAndNothingExcluded`: the 57 item entries exist in the jars (base IV and the two packs) and none is a
  police, `_legacy`, `huge` or harness one. **It catches an error**: with `blowtorchh` it failed ("no item
  mts:mtsofficialpack.blowtorchh"); the file was put back and regenerated.
- `referencedTablesExist`, `everyPoolAddsUpToTenThousand` (each draw = weight + empty = 10,000),
  `modifiersAreRegisteredAndPointAtTheAmcTables` (8 modifiers, the global list matches, each one points at the right AMC
  table).

### On the server (new world, AMC + AMC TACZ Loot + TACZ + LesRaisins + First Aid + AMC Vehicles together)
- No loot loading error in the log. `tools/sample_loot.py` generates AMC's table N times in a chest (Luck 0) and compares
  each item with the expected (deviation in standard deviations of the binomial). Results:
  - `workshop/chest` (16,000): seat 9.38% (expected 9.20), wheel 9.34, engine 2.04 (2.00), blowtorch 4.29 (4.40), repair
    kit 2.10, jumper pack 1.98; all between -0.7 and +0.9 deviation.
  - `workshop/barrel` (16,000): seat 4.74 (4.60), wheel 4.49, engine 1.03 (1.00), blowtorch 2.24 (2.20), repair kit 1.19
    (1.00; +2.4 deviations, the only one above 2), jumper pack 1.04.
  - `gas_station/chest` (8,000): empty jerry can 4.55 (4.40), full 4.58; `gas_station/barrel` (16,000): empty 2.11
    (2.20), full 2.24; in all full jerry cans the fluid was `lava` (366 in the chest of 8,000 and 359 in the barrel of
    16,000).
  - The 4 "hard" tables (4,000 each): all within 2 deviations.
  - No item beyond 3 deviations. With samples of 8,000, two items (wheel in the chest, jumper pack in the barrel) had come
    out near -2.4 deviations and came back to the expected with 16,000: noise (with 36 comparisons, 1 to 2 above 1.96
    deviation are expected by chance alone).
- **It adds, it does not replace**: with my loot on, AMC TACZ Loot's items (`tacz:`, `lrtactical:`, `firstaid:`) still come
  out in ~57% of the chests and ~34% of the barrels of workshop and gas station. I did not compare with a round without my
  mod (the test server loads AMC Vehicles from the source code, not from the `mods` folder); the argument is that AMC TACZ
  Loot's tables and modifiers were not touched.
- **Pots** (`workshop/pot`, `gas_station/pot` and the hard versions, 6,000 pots): 0 items of AMC Vehicles.
- **Excluded variants**: in the samples of the 4 "hard" tables, no id with `police`, `legacy`, `huge` or `harness`.

### Defects of the sampler found and fixed along the way (they were not the tables')
- It cut the text of the items expecting `count` first: the full jerry can (which has `components`) was attributed to the
  neighbouring item. Now it splits by the `{...}` keys of the list.
- The key `id` was looked for as `id: "...`, which also matches the end of `jerrycanFluid: "lava"`: the full jerry can
  became the item "lava". Now it only matches the whole `id`.
- Effect: in the first rounds the full jerry can came out at 0% and some items "out" of the interval; after the fixes,
  everything within the expected.

**Read in IV's code** (`ItemPartInteractable.doEntityInteraction`, not run): a jerry can with `jerrycanFluid` pours into
the car with `fuelTank.fill(fluid, "wildcard", 1000)`: the `wildcard` passes the tank's mod check (so the tank of AMC
Vehicles' cars, marked `minecraft`, does not get in the way); it needs **an installed engine** (without an engine IV warns
"no engine" and does not pour), a fluid compatible with the engine (lava is) and room for 1,000 mB in the tank. The empty
jerry can is filled at a pump (`TileEntityFuelPump.interact`: a pump with 1,000 mB or more and a jerry can without
`jerrycanFluid`).

**Not seen in the game:** using a full jerry can from the loot on a car with an engine (confirmed only by reading the code
above); Luck (the console does not apply it; `quality` = 25% of the weight as in AMC TACZ Loot) and opening the containers
in a really generated world; and the list of ids was checked in the jars, not in the game's item registry (which the test
server used in the samples).

## Version 0.7.1 — generic seat in the loot and only offroad and vintage wheels (2026-11-16)
- The user's request: include the generic seat `seat` (10 colours, ids `mts:mtsofficialpack.seat<colour>`, without `_`) and
  keep only the offroad and vintage wheels. `tools/gen_loot.py --write`; 53 item entries (they were 57), all in the jars;
  25 unit tests pass.
- Server (`workshop/chest`, 12,000 containers): seat 9.46% (expected 9.20; +1.0 deviation), wheel 8.97% (-0.9), engine
  2.09, blowtorch 4.32, repair kit 2.26 (+2.0), jumper pack 1.89; none beyond 3 deviations. Seats per model: generic 258,
  sport 280, vintage 293, car 304 (of 1,135): the 4 with the same chance; 34 distinct colours seen (7 + 7 + 10 + 10).
  Wheels seen: only `wheeloffroad_default` and `wheelvintage_lightblue`, `_mint`, `_sinty`. The 8 tables and the rest did
  not change (only the choice inside seat and wheel).

## Performance of the parked vehicles (2026-11-17, version 0.7.1)
The user's question: what changes in the long run with many loaded chunks and generated cars; do the cars stay far from
the player?

**Read in IV's code** (`BuilderEntityExisting`, `EntityManager`, `AEntityVehicleD_Moving`; decompiled, not run):
- The car is a normal Minecraft entity (`mts:builder_existing`) saved in the chunk's NBT (`saveWithoutId`) and restored
  when the chunk loads (`loadFromSavedNBT`); no despawn distance. It is only discarded if the vehicle becomes invalid,
  falls below the world or leaves the world border.
- Unloaded chunk: the car leaves memory (`onRemovedFromLevel` calls `remove()`) and stays only on disk.
- There is no "sleeping" mode for a parked car: `EntityManager.tickAll` runs the `update()` of every registered vehicle
  every tick, with the full physics (`getForcesAndMotions`, `moveVehicle`, collision boxes), except with the setting
  `noclipVehicles`.

**Measured** (test server, new world, 4 areas of wide building with `forceload`, everything entity-ticking, 20 s after
each `/amcv fill`, `tick query` 3 times): mean MSPT with 4 cars 1.4 ms; 36 cars 3.0; 67 cars 4.5; 98 cars 6.0; 131 cars
7.8 (P99 from 5 to 12 ms). **Linear** growth, ~0.05 ms per car (+1.5 ms every ~31 cars), no sign of a quadratic cost up to
131. The budget of a tick is 50 ms: ~300 ticking cars = ~15 ms.
Disk (`run/world/entities`, after `save-all flush`): 81,175 bytes before and 258,550 after 127 new cars: ~1.4 KB per car.

**Not measured:** the client (rendering, sound, the client's copy of the `EntityManager`); cars in loaded chunks but
**outside the simulation distance** (the test server without a player only has forced chunks, which tick). Hypothesis from
the code: IV's `EntityManager` only registers the car on the entity's first tick (`baseTick` restores IV from the NBT), so
a car never ticked by Minecraft costs no physics, but one that was ticked and then left the simulation would stay
registered (and ticking) until the chunk unloads. Deciding test: in the game, with a 100% test jar near a full garage,
`/tick query` standing beside it and then at a distance between the simulation and the view (`simulation-distance`
smaller than `view-distance`).

### Cost of the mod itself when it creates cars (2026-11-17, 0.7.1)
`/amcv fill` in a garage of the wide building (33-34 cars, time of the reply through RCON, it includes the draw, the check
of the world and the creation of the car in IV): 32 ms the first time (warm-up), then 21, 16 and 15 ms = **~0.5 ms per
car**. `/amcv draw` (only checking 34 bays): 3 ms. This is a **one-off cost per car**, when generating; the ongoing cost is
IV's (~0.05 ms per car per tick, measured above).

## Version 0.7.2 — 9% per bay (2026-11-17)
- The user's request: the chance of the parked cars rises from 3% to 9% (`Draw.CHANCE_PER_BAY`). The draw is the same (the
  bay has a car if the number of index 0 < the chance): who had a car with 3% still has it, and more come in.
- Unit tests: 25 pass. `DrawTest.chancesModelsAndColours` (4,000,000 bays): 9.02% with a car, the 8 models between 44,872
  and 45,491, hood, trunk and doors 50%. `projectionPerCity` (492 cities, the 57 bays): 17,507 bays, 1,575 cars = **3.20
  per city** (9.00% of the bays); cities without a car 46 (9.3%), with 1: 15.2%, 2: 19.5%, 3: 18.5%, 4: 11.2%, 5: 10.2%,
  6: 6.9%, 7: 3.9%, 8 to 11: 5.2% together.
- Server (new world, seed 20261027, 6 wide buildings generated): cars in the boxes 6, 4, 4, 3, 3 and 3 = 23, drawn 23, in
  place 23, no bay skipped (25 `(generated)` lines in the log: 2 of neighbouring structures in the chunks of the edge).
- The 100% test jar still exists (`CHANCE_PER_BAY = 1.0d`, only local).

## Version 0.7.3 — cars with a flat battery (2026-11-17)
- The user's request. Before: `electricPower` 12.0001 of capacity 14 (85.7%, IV's default), measured on 3 generated cars
  (Mercedes, Camaro, Mustang). Now `Vehicles.spawn` sets `electricPower = batteryCapacity * 0` (0%).
- **NOT TESTED** (the user asked to skip the tests of this change): neither the unit tests nor the server. Only compiled
  (`./gradlew build -x test`). I do not know whether IV reloads the field on the car's first update nor whether the
  dashboard or the jumper pack behave as expected. To check: `/amcv car mercedes` and
  `execute as @e[type=mts:builder_existing,limit=1,sort=nearest] run data get entity @s electricPower` (expected 0.0).
  (0.9.3: on a generated car the field no longer appears, and IV does not save zero.)

## Version 0.7.4 — the cars' tank at 15% (2026-11-17)
- The user's request (along with the battery's, and with the same instruction to skip the tests):
  `Vehicles.FUEL_FRACTION` from 0.10 to 0.15. Amounts: Camaro and Highwayman 1,500 mB (they were 1,000), Mustang 1,200
  (800), Mercedes, Cagouille, Escargot, Station Wagon and Scout 2,250 (1,500).
- **NOT TESTED** (at the user's request): only compiled (`./gradlew build -x test`); the constant inside the jar was read
  with `javap`. 0.7.3 (battery 0%) also stays untested. (0.9.3: a generated Escargot has `fluidLevel 2250`.)

## Version 0.7.5 — lava back to 1.0 (2026-11-17)
- The user's request: remove the setting that makes the fuel drain faster (`FuelBalance`'s lava at 0.35). `FuelBalance`
  and `ClientHooks` and the hooks in `AmcVehicles` deleted.
- Tests: 25 unit tests pass. Server (new world): 0 "Lava as a fuel" lines in the log (before there was one at start and
  another at stop), 0 errors of the mod, `/amcv car mercedes` answers `fuel 2250/15000 mB (lava)` (15%), `mtsconfig.json`
  of the test server with `"lava": 1.0` in the 5 types.
- **Finding**: the `config/mtsconfig.json` of the instance "MAZ 2.0.0 (TESTE)" (saved on 2026-11-17, 01:09) has
  `gasoline`, `diesel` and `avgas` at **0.35**: IV saved its own config with the mod's value in memory (the limit
  documented in 0.6.0). Removing the mod does not undo it. Pending: set those three back to 1.0 (it needs the user's OK and
  the game closed).
- Not tested in the game: the consumption with lava at 1.0 (it depends on the file above).

## Version 0.7.6 — repair kit 10% more common (2026-11-17)
- The user's request: repair kit from 1.0% to 1.1% (barrel; chest 2.0% to 2.2%), "to avoid competing with the engine and
  the jumper pack". The draws are independent (each item has its own pool): there is no competition; the change only
  ranks the repair kit above the other two. `ITEM_MULT = {"repairkit": 1.1}` in `tools/gen_loot.py`; only
  `workshop/chest.json` and `workshop/barrel.json` changed (weights 220 and 110).
- Tests: 25 unit tests pass. Server (16,000 containers of each): barrel repair kit 1.22% (expected 1.10; +1.4 deviation),
  engine 0.92 (1.00), jumper pack 0.89 (1.00); chest repair kit 2.27% (2.20; +0.6), engine 2.12, jumper pack 1.93. No item
  beyond 3 deviations; 0 loot errors. The 10% difference is smaller than the noise of a small sample: it shows only in the
  mean of many containers (expected per medium workshop: repair kit 0.69, engine and jumper pack 0.63).

## Version 0.7.7 — wrench in the workshops' loot; `mtsconfig.json` fixed (2026-11-17)
- **`mtsconfig.json`** (the user's request, game closed: `ps` without a game process): backup
  `~/backups/mtsconfig-2026-11-17/`, `"lava": 0.35` replaced by `"lava": 1.0` (3 lines: `gasoline`, `diesel`, `avgas`),
  checked with `json.load` (lava 1.0 in the 5 types) and `diff` against the backup (only those 3 lines). To revert: `cp -p`
  of the backup over the file.
- **Wrench** (`mts:mts.wrench`, base IV) Common in the workshops (chest 9.2% / barrel 4.6%), like seats and wheels.
  Preview: 2.90 per medium workshop, 1.98 per small one, 4.44 per city. 25 unit tests pass (55 item entries, all in the
  jars).
- Server (new world, 0 loot errors): `workshop/barrel` (16,000) wrench 4.92% (expected 4.60; +1.9 deviation);
  `workshop/chest` (16,000) 8.74% (9.20; -2.0); `workshop/barrel_hard` (4,000) 4.58%; the other items unchanged; none
  beyond 3 deviations. `gas_station/barrel` (3,000): no wrench.

## Version 0.7.8 — jerry can always full in the gas stations' loot (2026-11-17)
- The user's request: remove the empty jerry can; the jerry can now always comes out full, keeping the rarity (Uncommon:
  chest 4.4% / barrel 2.2%). Consequence: from ~1.2 to ~0.6 jerry can per city. `GAS = [("jerrycan_full", "Incomum")]` in
  `tools/gen_loot.py`; only `gas_station/chest.json` and `gas_station/barrel.json` changed (one draw instead of two).
- Unit test: `LootDataTest.everyPoolAddsUpToTenThousand` required 2 draws per table and now requires 1 (the test was out of
  date, not the data); with a gas station table without any draw it fails (checked). 25 tests pass.
- `tools/sample_loot.py` now flags any AMC Vehicles item that should not come out in the sampled table.
- Server (new world, 0 loot errors): `gas_station/chest` (12,000) full jerry can 4.22% (expected 4.40; -1.0 deviation),
  `barrel` (16,000) 2.34% (2.20; +1.2), `chest_hard` (4,000) 4.20%, `barrel_hard` (4,000) 2.38%; the 1,143 jerry cans seen
  had `jerrycanFluid: "lava"`; no item that should not be there (neither the empty jerry can nor the wrench) and
  `workshop/barrel` (3,000) without such an item.
- Note on method: a first round passed "table N" as a single argument (zsh does not split `$t`) and the sampler printed
  nothing; it was redone with separate arguments.

## Version 0.7.9 — `FuelBalance` back, with lava at 0.60 (2026-11-17)
- The user's request: "change the FuelBalance rule again, this time to 0.60". The rule had been removed in 0.7.5; the
  classes `FuelBalance` and `ClientHooks` were restored from commit `36560dd` (`git checkout 36560dd -- ...`) and the hooks
  of `AmcVehicles` wired again; `LAVA = 0.60`.
- Tests: 25 unit tests pass; the constant inside the jar (`javap -constants`) is `LAVA = 0.6d`.
- Server (new world): log at start `[AMCV] Lava as a fuel set to 0.6 (server starting; was: diesel 1.0, avgas 1.0,
  gasoline 1.0); IV's config file is not changed`; at stop `[AMCV] Lava as a fuel put back to IV's values (server
  stopped)`; `/amcv car mercedes` answers `fuel 2250/15000 mB (lava)`; `mtsconfig.json` of the test server with
  `"lava": 1.0` in the 5 types after stopping.
- Not tested in the game: the real consumption with 0.60, and `ClientHooks` on a client (the part of joining and leaving a
  server). The `mtsconfig.json` of the user's instance stays at 1.0 (fixed in 0.7.7), unless IV saves it again.

## Version 0.7.10 — lava at 0.45 (2026-11-17)
- The user's request: lower it from 0.60 to 0.45 (`FuelBalance.LAVA`). Nothing else changed.
- Tests: 25 unit tests pass; `javap -constants` on the jar: `LAVA = 0.45d`. Server (new world): `[AMCV] Lava as a fuel set
  to 0.45 (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)`; at stop `... put back to IV's values (server
  stopped)`; `mtsconfig.json` of the test server with `"lava": 1.0` in the 5 types after stopping.
- The user's instance (read, not changed): `mtsconfig.json` with `gasoline`, `diesel` and `avgas` at 0.6, saved by IV on
  2026-11-17, 21:33 (0.7.9). With the mod, the value in memory (0.45) rules; the file only changes if IV rewrites it. With
  0.6 in the file, the mod keeps 0.6 as "IV's value" and puts it back at stop (the putting back does not restore 1.0).
- Not tested in the game: the real consumption with 0.45.

## Version 0.7.11 — lava at 0.35 again (2026-11-17)
- The user's request: from 0.45 to 0.35 (`FuelBalance.LAVA`); nothing else changed.
- Tests: 25 unit tests pass; `javap -constants`: `LAVA = 0.35d`. Server (new world): `[AMCV] Lava as a fuel set to 0.35
  (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)`; at stop `... put back to IV's values`; `mtsconfig.json` of
  the test server with `"lava": 1.0` in the 5 types after stopping.
- Not tested in the game: the real consumption with 0.35. The `mtsconfig.json` of the user's instance was not changed in
  this version.

## Version 0.7.12 — lava at 0.05 as the default (2026-11-17)
- The user tested a test jar with lava at 0.05 (`amcvehicles-0.7.11-TESTE-100pct-lava0.05.jar`, not versioned) in his
  game: "the consumption is very fast, working normally" (confirmation of the consumption in the game, made by him). He
  asked for it to be the default: `FuelBalance.LAVA` from 0.35 to 0.05.
- Tests: 25 unit tests pass; `javap -constants`: `LAVA = 0.05d`. Server (new world): `[AMCV] Lava as a fuel set to 0.05
  (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)` and the putting back at stop; `mtsconfig.json` of the test
  server at 1.0 after stopping.

## Version 0.7.13 — pumps with 0 to 4 buckets (2026-11-18)
- The user's request: "go from 0 to 3 to 0 to 4 of lava in the pumps". Read as: still 50% empty and the most rises from 3
  to 4 (`Draw.MAX_BUCKETS`): 1, 2, 3 or 4 buckets with 12.5% each, mean 1.25 bucket per pump (it was 1). If he meant 0 to 4
  with the same chance (20% each), it is another rule: asked to him at delivery.
- Tests: 25 unit tests pass. `PumpTest.halfEmptyHalfOneToThree` (1,200,000; renamed `halfEmptyOthersOneToMaxBuckets` in
  0.9.1): 0 bucket 599,997 (50.0%) and 1, 2, 3, 4 buckets 149,791, 150,063, 149,697, 150,452 (12.5% each).
  `pumpsOfAStationIndependent` (200,000 gas stations of 8 pumps): mean 9.995 buckets (expected 10) and variance 17.431
  (expected 17.5; before they were 8 and 10.67); the test was updated to these values.
- Server (new world, 8 gas station areas generated one by one): 34 pumps, **the tank of each one matches the drawn one (0
  differences)**; drawn buckets: 0 in 20, 1 in 8, 3 in 5, 4 in 1 (a pump of 4 buckets, 4,000 mB, appeared). 4 "Can't keep
  up" warnings (2 to 4 s behind) while it generated several cities; no exception. A first try with 21 gas stations at once
  brought the test server down (overload from generating everything together; the log was deleted by mistake, so the exact
  cause was not read).
- Only for new chunks: the pumps already generated in the user's worlds do not change.

## Version 0.8.0 — bays at the glass house and at the ramp building (2026-11-18)
- The user's request: "add parking bays at the abandoned_glass_house and at the ramp building". Proposal in
  `docs/spots/vidro_rampa_propostas.png` (generated with `tools/find_spots.py --bays`). His answers: floors y=10 and y=0 of
  the ramp building (21 bays), a good amount, and, at the glass house, a photo that showed the place is the north-east
  corner room (not the front porch I had proposed); the 2 bays of the room applied after his confirmation.
- Detector: glass house, the 2 bays free; ramp building, 20 proposals per floor: y=10 10 free, y=0 11 free, y=5 0 free
  (hence out). Only the free ones go in.
- Tests: 25 unit tests pass (including `PlacementTest`, which puts the 8 models in every bay in the 4 rotations, now with
  the 23 new ones). Projection with AMC's real planner (5 seeds, 492 cities): 19,704 bays (40.0 per city), **1,788 cars
  (3.63 per city, 9.07% of the bays)**; cities with 0 cars 39 (7.9%); glass house 521 copies, 108 cars (0.21 per copy);
  ramp building 55 copies (0.11 per city), 105 cars (1.91 per copy). Before: 3.20 per city and 9.3% without a car.
- Server (new world, seed 20261027): `amcs locate` found a glass house (x -351..-312, z 434..481, rotation 3) and a ramp
  building (x 172..243, z 1121..1222, rotation 3). `/amcv fill` at the glass house: 2 bays, 2 cars, 0 skipped, at the
  centres expected by the hand sum on the `.nbt` (x -337.5 and -333.5; rear axle at z 445), nose to the north (the
  model's east wall) and the right floor (y 69.4). At the ramp building: 21 bays, 18 cars placed and 3 "occupied by a
  vehicle" (cars of the real 9% draw made in the world's generation, which proves the generation also sees the new bays);
  cars of y=10 at y 78.4 and those of y=0 at y 68.4 (10 above), in z every 4 blocks, all with the nose to the west (the
  model's north turned).
- `tools/check_bays.py` does not work for these bays (it looks for long stripes beside the car; at the ramp building the
  white marks are dots at the edges and at the glass house there is no paint): it showed "0 cars between stripes", without
  meaning.
- Not tested in the game: the look (a car leaning on something, in the open at y=10 of the ramp building), the natural
  generation at the glass house (`fill` tested it; the real draw was only seen at the ramp building) and the size of the
  ramp building (102 × 137 × 72) in performance with cars.

## Version 0.8.1 — ramp building without bays, glass house kept (2026-11-18)
- The user tested 0.8.0: "you can keep the glass house's bays, but you can remove the ramp building's". The 21 lines
  `abandoned_ramp_building bay ...` of `spots.txt` were commented (not deleted), with a note; nothing else changed.
- Tests: 25 unit tests pass. Projection (AMC's real planner, 5 seeds, 492 cities): 18,549 bays (37.7 per city), **1,683
  cars (3.42 per city, 9.07% of the bays)**, cities with 0 cars 41 (8.3%). Jar: `spots.txt` with 59 bays.
- Server (new world): `/amcv fill` at the glass house: 2 bays, 2 cars, 0 skipped; at the ramp building:
  "abandoned_ramp_building has no bays yet". The log has 12 ERROR/Exception lines, all before the world and from IV
  (`MTSERROR` while reading the packs' models, like "roller2" and `engineikarus.obj`, and a Minecraft tag); none mentions
  `amcvehicles`. (A first round counted those 12 and I deleted the log before reading it; I redid it keeping the log.)
- Only for new chunks: a ramp building that the user's world generated with 0.8.0 may already have cars in it (they stay).

## The 1.0.0 work, phase 0 — safety net (2026-11-18, still 0.8.1)
- No change in the mod: only the new golden test (`GoldenDrawTest`, 26 tests in all, all pass) and the snapshot of 0.8.1
  in `docs/baseline/` (hashes of the JSON, jar entries, constants, projection, server). Details and numbers in
  `docs/baseline/README.md`.
- The golden test was checked against a change on purpose (`DOOR_OPEN_CHANCE` 0.5 to 0.51): it failed on line 205; with the
  change undone, it passed.
- Server (new world): 6 boxes generated, 4 cars = 4 drawn = 4 in place, 8 pumps with the drawn lava, `/amcv fill` at the
  glass house with 2 cars; no ERROR or WARN of the mod in the log.

## Version 0.9.0 — phase 1 of 1.0.0: dead code (2026-11-19)
- Survey (changing nothing): `javac -Xlint:all` on main and test (through a Gradle init script; checked with `--debug`
  that `-Xlint:all` reached the compiler): **0 warnings**. No unused import (search by name outside comments). No member
  declared by the mod without use; only `Spots.all`, `Spots.allPumps` and `Spots.Bay.alongZ` are used only by tests (kept,
  the user's choice). The 5 `catch` blocks log the error with the stack trace and return the message; none swallows an
  error. No commented-out code in the Java. The 9 tools of `tools/` are cited by the docs or used by another (`nbt.py` by
  `find_spots.py`; only it lacked the usage line, now it has one). The 7 images of `docs/spots/` are cited. Nothing of
  `run/`, `build/` or `.gradle/` in git.
- Removed (the user's choice): the drawn nose (`X`/`Z` in `spots.txt`, `Spots.Bay.drawnFacing` and `oriented`,
  `Draw.Car.flip`, the index `FLIP` = 6, `PlacementTest`'s `ways`). Index 6 stays free; the others keep their number.
- Text: the pumps' comment in `spots.txt` (it cited `Draw.PUMP_FUEL_CHANCE` and "30% with 1 bucket"), the note of the
  commented bays of the gas stations, the README's duplicate line, the duplicate paragraph of "How the pump is placed" and
  the mentions of `Mercedes.spawn`, `Placement.CAR_LIFT` and "`Mercedes` repeats them" in `CLAUDE.md`, the "63 bays" that
  described the current state.
- Proof: the 69 data lines of `spots.txt` equal to 0.8.1's (`diff` without the comments); 26 tests pass, 0 warnings;
  **golden test identical** (`golden: 4708 lines ... sha256 717de763...e4d8b7`, the reference was not touched); projection
  the same (1,683 cars, 3.42 per city, 8.3% without a car).
- Jars: `amcvehicles-0.9.0.jar` (`javap -constants`: `CHANCE_PER_BAY = 0.09d`; the constants equal to the snapshot of
  0.8.1, except `FLIP = 6`; the 60 jar entries the same) and `amcvehicles-0.9.0-TESTE-100pct.jar` (`CHANCE_PER_BAY =
  1.0d`, made with `./gradlew jar`, without the tests, and the constant put back; only local). The 21 JSON equal to the
  snapshot's.
- Server (new world, the same seed; the world of phase 0 kept in `run/world_phase0`): the same 6 boxes of phase 0 gave
  **the same result** (the same 4 cars in the same bays and positions, the same 8 pumps with the same lava). `/amcv fill`
  at the glass house: 2 bays, 2 cars, 0 skipped; `/amcv car mercedes police true false`: `mercedes_police`, hood open,
  trunk closed; `/amcv lineup`: the 12 cars. Log: the same 12 ERROR and 15 WARN as in phase 0 (IV, TACZ, Minecraft, AMC),
  none of the mod.
- Not checked by a tool: unused parameters and local variables (`-Xlint:all` does not point at them; left for phase 2,
  which reads each class). The docs sweep was targeted (the plan's list and what came up along the way, plus "2 of free
  height" in "Placing" and the "Architecture" without `FuelBalance` and `ClientHooks`); checking every value against the
  code is phase 6's.

## Version 0.9.1 — phase 2 of 1.0.0: cleaner code, same behaviour (2026-11-19)
- The user's decision: no automatic formatter, only `.editorconfig`.
- Extra net before touching anything (the golden test does not cover `Vehicles`, `AmcWorld.blocked`, the generation's
  tick, the pumps nor the commands' replies): in the world of 0.9.0, saved in `run/phase2/` (outside git) the full data
  (`data get`) of the 4 generated cars and of the 8 pumps of the gas station and the `/amcv draw` replies in the 6 boxes of
  phase 0 (`run/phase2/dump.py`), plus the replies of `fill`, `car` (valid, wrong model, wrong colour), `lineup`, `pump`
  and `fill` outside a structure; and the length and nesting of each method (`run/phase2/metrics.py`).
- Changes (form only): `Vehicles.spawn` (79 lines) into `newVehicle`, `setOpenings`, `setDashboard`, `removeParts` and
  `engineFuelType`, in the same order as before (variables and dashboard before the car is in the world, parts after);
  `Generation.onLevelTick` (54) into `resolve`, `placeReadyCars` and `placeReadyPumps` (both always run), the chunk's list
  of cars and pumps computed once, `pumpCellBlocked`, `drawn(s)`/`pumps(s)` without the null `Integer`s; `AmcvCommands`:
  `structureHere`, `spawn` (the `try/catch` that repeated 3 times), `forward`, `chunksUnder`, `lineupCars`, `fillPumps`;
  `AmcWorld.Placed.of` (the same sum repeated in `AmcWorld.at` and `Generation.structuresIn`) and `room`;
  `FuelPumps.pumpData` and `Block.UPDATE_ALL_IMMEDIATE` instead of `11`; `Spots.parseLine`; named constants
  (`PERMISSION`, `MAX_PUMP_BUCKETS`, `COLUMNS`, `PUMP_KEY_SALT`, `SLIVER`); wrong comments fixed (10% of fuel, 0 to 3
  buckets, `CAR_LIFT`) and Java lines of up to 120 columns; classes visible only in the package (except `AmcVehicles`);
  test `halfEmptyHalfOneToThree` renamed `halfEmptyOthersOneToMaxBuckets`. Methods with more than 40 lines: before
  `Vehicles.spawn` 79, `AmcvCommands.fill` 54, `Generation.onLevelTick` 54, `FuelPumps.place` 44, `AmcvCommands.draw` 42;
  after only `AmcvCommands.fill` 41.
- Proof: 26 tests pass, 0 warnings with `-Xlint:all`; **golden test identical** (sha256 `717de763...e4d8b7`); the jar's
  constants equal to the snapshot's, plus the new named ones (with the values of before). Server, new world, the same seed:
  the 6 boxes with the same 4 cars; the full data of the 4 cars and of the 8 pumps and the 6 `/amcv draw` replies
  **identical to those of before** (empty `diff`, only the UUIDs masked); the format of the replies of `fill` (placed and
  occupied), `car`, `lineup`, `pump` and of the 3 error messages the same; log with the same 12 ERROR and 15 WARN (none of
  the mod).
- Jars in `run/jars/`: `amcvehicles-0.9.1.jar` and `amcvehicles-0.9.1-TESTE-100pct.jar` (`CHANCE_PER_BAY = 1.0d`, only
  local).
- Not checked: unused parameters and local variables were looked for by reading each class (not by a tool); the game itself
  (the look) was not tested.

## Version 0.9.2 — phase 3 of 1.0.0: security (2026-11-19)
Threat model: the mod registers no network packet and only reads its embedded `spots.txt`; who can cause harm is an
operator (level 2), a world with corrupt data, another version of IV or of a pack, the build chain and the published
repository.

| Finding | Evidence | Status | Severity | Fix | Test |
|---|---|---|---|---|---|
| An IV error while building a car in the generation stops the server | temporary build with `Vehicles.spawn` always failing, police station generated: crash in 2 s (`crash-2026-11-19_09.50.40`); on restart, crash again in 12 s (`10.12.58`), since the chunk stays pending | confirmed | high (the world becomes unusable with the mod) | `Generation.place` catches the error, logs it (`could not spawn ..., the car is dropped`) and skips the car (the user's choice) | the same world and the same broken build: no crash, the game's clock moves on, `pending chunks 0` |
| `mts` accepted in any future version (the reflection in `FuelPumps` may break) | `versionRange="[24.0.0,)"` | confirmed | medium | `[24.0.0,25)` (the user's choice) | build; NeoForge refuses a 25 on screen (not tested with an IV 25, which does not exist) |
| A failure of the reflection in `FuelPumps` | reading: the `catch (ReflectiveOperationException \| RuntimeException)` returns the reason; `resolve` counts the pump as resolved and the chunk leaves `Pending` | confirmed by reading | low | none | not tested on the server |
| `FuelBalance` would put back the wrong value (client and server in single player) | `FuelBalanceTest`: in both orders IV's values come back | refuted | - | none | 2 tests |
| A `mtsconfig.json` already dirty stays dirty | `FuelBalanceTest.dirtyConfigStaysDirty` | confirmed (known limit) | medium | phase 4 | 1 test |
| Corrupt `Pending` | a file with `chunks` of the wrong type: the world opens, empty list, no error; a random file: `Error loading saved data: amcvehicles_pending` and the world opens | no defect | - | none | 2 server runs |
| `Pending` without a limit / work per pass | a chunk only stays pending while the chunks under one of its cars are not ready; each pass costs one lookup per pending chunk; creating a car costs ~0.5 ms (measured in 0.7.1), a whole garage ~17 ms in a tick of 50 ms | no defect | - | no limit (not needed) | existing measurement |
| A bad line of `spots.txt` | an invalid number gave a `NumberFormatException` without the line | confirmed | low (the file is embedded) | `IllegalStateException("spots.txt line N: ...")` | `SpotsTest` (7 bad lines, 2 good) |
| Commands | `pump 16`: Minecraft refuses ("must not be more than 15"); unknown model and colour refused with a message; `fill`/`draw` limited to the structure's bays (at most 34); level 2 | no defect | - | the numbers became constants in phase 2 | server |
| Command replies with `e.toString()` | `car`, `lineup`, `fill` show the exception's class to the operator | confirmed | low (operator only; no stack trace) | none | - |
| Build chain | wrapper without `distributionSha256Sum`; repositories: NeoForged (releases and mojang-meta), Mojang libraries, Gradle plugin portal | confirmed | medium | hash `72f44c9f...bc3f` (from `services.gradle.org/...sha256` and from the page `gradle.org/release-checksums`) | the wrapper downloaded into an empty folder and accepted the hash (the case of a wrong hash was not tested) |
| Test server exposed on the network | `server-ip=` empty, RCON with password `teste`, `online-mode=false` | confirmed | medium (only while it runs, on the local network) | `server-ip=127.0.0.1` (in `run/`, outside git); `rconlib.py` reads `AMCV_RCON_PASSWORD` (default `teste`) | the tools still connect |
| Python tools | `car_info.py` and `nbt.py` only read; nobody extracts files from a jar to disk; everything runs with `-I` | no defect | - | - | reading |
| Repository | no `LICENSE`; history: 0 personal paths, 0 secrets (only the password `teste` of the local server); personal e-mail in the old commits | confirmed | low | `LICENSE` MIT (RakkuLyc, 2026); new commits with GitHub's noreply e-mail (the user's choice; the history was not rewritten) | `git log -p` |
| Logs | the mod's lines have coordinates, models and reasons; nothing personal | no defect | - | - | the server's log |

- `libs/` (not versioned), SHA-256: `abandonedmoderncity-0.1.0.jar` `392639e5...20ec5e`, `Immersive Vehicles-1.21.1-24.0.0.jar`
  `1d2f7512...78d251`, `MTS Official Pack-1.21.1-V29.jar` `f82e67f6...a0b5d8`, `OAmP-1.21.1-V3.jar` `74311b94...433769`.
- A new finding for phase 4 (in the `Pending` test): a **duplicate** car when `/amcv draw` runs right after the server starts
  (`count: 2` in the police station's bay).
- A folder `logs/` written by `FuelBalanceTest` (Minecraft's logging writes where the test runs) went into a commit by
  mistake: only 4 lines of the mod, nothing personal; taken out of git and ignored (`/logs/` in `.gitignore`).
- Tests: 31 pass; golden test identical.

## Version 0.9.3 — phase 4 of 1.0.0: defects (2026-11-19)
- **Dirty `mtsconfig.json` (fixed)**. Cause, in IV 24.0.0's code (`javap -c`): `ConfigSystem.saveToDisk` writes the whole
  file from memory and is called with a world open by `GUIConfig` and `ControlSystem` (client screens) and by
  `WrapperWorld.onIVWorldTickPre` (the server's tick, when a player joins for the first time: IV gives the starting items,
  puts the player in `general.joinedPlayers` and saves); outside that, while loading (`InterfaceLoader`,
  `PackParser.parsePacks`), before the mod touches anything. Fix (approved by the user beforehand: "apply it directly" if
  the game does not change): `ConfigSystemMixin` around `saveToDisk` (start: IV's values in memory; end: the mod's lava
  again). Test on the server, with a temporary command (already removed) that called `saveToDisk`: reply "lava in memory
  0.05", the file rewritten at 10:24:27 with `"lava": 1.0` in the 5 types; no mixin error in the log. Limit: a file that
  is already dirty (from old versions) stays dirty (`FuelBalanceTest.dirtyConfigStaysDirty`); the README explains how to
  clean it. Not tested: the client's screens (only the server was tested; the path is the same method).
- **Duplicate car (fixed)**. Reproduced in phase 3: `/amcv draw` right after the server started placed a second car in the
  police station's bay (`count: 2`). Cause: the entities of a chunk load after it, and the vehicle check did not see them.
  Fix: `Generation.ready` requires `ServerLevel.areEntitiesLoaded` of the chunks under the car; `AmcWorld.blocked` answers
  "entities of chunk -21,21 not loaded yet". Test: the same world, `/amcv draw` right after starting: "entities of chunk
  -21,21 not loaded yet" (nothing placed); 8 s later: "occupied by a vehicle"; the count stayed at 2 (the old duplicate).
  The generation is the same: wide building and gas station in a new world, 3 cars = 3 drawn = 3 in place, 8 pumps.
- **Stress**: a whole city (the one at -368,368, 4 × 225 = 900 chunks forced at once), the server stopped midway and started
  again: 3 cars and 4 pumps `(generated)`, 3 vehicles in 3 different positions (no duplicate), `Pending` empty; no ERROR or
  WARN of the mod in the two logs; one "Can't keep up" of 6.7 s while generating the 900 chunks (the mod placed 7 items at
  that time, ~0.5 ms per car; the rest is the world's generation, not measured apart). It was not "hundreds of cities": one
  city.
- **Never tested by the user, checked on the server**: tank at 15% (generated Escargot: `fluidLevel 2250` of 15,000, lava);
  battery at 0 (the field `electricPower` does not appear, and IV does not save zero; before 0.7.3 it appeared with 12.0);
  loot (`sample_loot.py`, 400 workshop chests: seat 7.25%, wheel 8.75%, wrench 11.5%, engine 1.5%, blowtorch 4.0%, repair
  kit 1.75%, jumper pack 1.0%, all within 3 deviations; 400 gas station chests: full jerry can 4.0% of 4.4%). Only the user
  can confirm in the game: the flat battery on the car's screen, the jumper pack starting the car, the full jerry can
  pouring lava, the look.
- **The window "chunk saved before `Pending`"** (known limit, not changed): if the server crashes after saving a new chunk
  and before saving `Pending`, the cars of that chunk are lost (never duplicated). To close it: (a) keep the "pending" mark
  in the chunk itself (a NeoForge data attachment, saved along with it; ~40 lines, still reading the old worlds'
  `Pending`); (b) save `Pending` at every new chunk with a car (a disk cost at each one); (c) leave it as it is (the
  autosave is every 5 minutes; at most a rare car is lost). Recommendation: (c) for 1.0; the user's decision.
- **World opened without the mod** (not tested, from reading): Minecraft does not read unknown files of `data/`
  (`amcvehicles_pending.dat` just sits there); the cars and the pumps are IV's and stay; the mixin leaves with the mod.
- Tests: 31 pass; golden test identical.

## Phase 5 of 1.0.0: optimization, measured (2026-11-19; no code change, still 0.9.3)
- Measured with temporary stopwatches (already removed) in `Generation.onChunkLoad` and `Generation.onLevelTick`, in a new
  world, generating the whole city at -368,368 (4 × 225 chunks forced; 7 items placed): **1,205 chunks loaded, 17 ms in
  all (mean 14 µs, worst 2.7 ms)**; **2,982 calls of the tick, 31 ms in all (mean 10 µs, worst 16.8 ms, once: the pass
  that created cars, including the warm-up of IV's first car)**. About 48 ms in ~2.5 minutes of server: ~0.03% of the
  time. One "Can't keep up" in the log, while generating the 900 chunks (the world, not the mod: the mod spent 48 ms in
  all).
- Candidates of the plan and of `docs/baseline/FINDINGS.md` (recomputing the pending chunk's draw every 10 ticks, the
  reflection per pump in `FuelPumps`, the entity search in `AmcWorld.blocked`, `Spots.of`/`Placement` per chunk, reading
  `spots.txt` at start): all of them stay within these numbers; **none would gain perceptibly, so none was changed** (the
  rule: without measuring, no optimizing, and what does not gain is dropped). Jar: 76 KB (`amcvehicles-0.9.3.jar`, 77,653
  bytes).

## Version 0.9.5 — phase 6 of 1.0.0: documents in English (2026-11-19)
- Translated (glossary in `docs/GLOSSARY.md`): a new short `README.md` for players, `docs/TECHNICAL.md` (the old README's
  technical content), `CLAUDE.md`, this file, `docs/diagnostic_iv.md` (renamed from `diagnostico_iv.md`; still pending by the
  user's decision), the notes of `docs/baseline/` (`ACHADOS.md` → `FINDINGS.md`, `DECISOES_1.0.md` → `DECISIONS_1.0.md`),
  the messages and comments of `tools/gen_loot.py`, `tools/sample_loot.py` and `tools/rcon.py`; dates in ISO format also in
  the comments of the Java and of `spots.txt`; lines of up to 120 columns in the tools and in `spots.txt`. The Java comments
  and the game's messages were already in English. The class keys "Comum", "Incomum", "Raro" of `gen_loot.py` stay (they are
  keys of the code).
- Not translated: the captions drawn inside the images of `docs/spots/` (made by scripts that are not in the project; a
  new compositing script could rebuild them, a decision for the user); explained in `docs/spots/README.md`.
- Checks: (a) no accented letter left outside the glossary and the images' page, except "Épico", the AMC TACZ Loot name in
  brackets (the user's choice); no common Portuguese word left; (b) the numbers of the Portuguese (from git) and the English
  versions of `CLAUDE.md`, `TESTING.md` and the diagnosis compared after normalising decimals, thousands and dates: every
  difference is either an artefact of the normalisation (coordinates "x,z", versions like 21.1.219 and 24.0.0, a date split
  across two lines) or a deliberate update (0.9.x notes, 31 tests, 76 KB); (c) 78 paths cited in the documents exist (the only
  other one, `docs/containers.json`, is AMC TACZ Loot's); (d) the values against the code: 9% per bay, 59 bays in 11
  structures, 10 pumps, lava 0.05, tank 15%, battery 0, 1 to 4 buckets, IV `[24.0.0,25)`, 31 tests.
- `python3 -I tools/gen_loot.py --write` followed by `git diff --stat src/main/resources`: empty (the loot did not change);
  `tools/car_info.py` runs; the 69 data lines of `spots.txt` the same.
- Build: 31 tests pass; golden test identical; the compiled constants of 0.9.5 equal to 0.9.3's. Jars in `run/jars/`:
  `amcvehicles-0.9.5.jar` and `amcvehicles-0.9.5-TESTE-100pct.jar` (`CHANCE_PER_BAY = 1.0d`, only local). Not run on the
  server: this version changes no code that runs (comments, documents, tool messages).
