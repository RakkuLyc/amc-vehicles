# AMC Vehicles — work rules (mandatory)

The rules of the Abandoned Modern City project apply (the user's choice, 2026-11-16):
1. **Ask along the way**: design decisions, numbers and style belong to the user; when there is more than one solution,
   present the options with a recommendation.
2. **Build the `.jar` at every small/medium change** and say what changed in it.
3. **GitHub** (`gh`, repository `RakkuLyc/amc-vehicles`, public since 2026-11-19): frequent, clear commits; update the repository at
   every testable jar.
4. **Iterative**: small, testable stages; at the end of each one, a summary + the recommended next step.
5. **Do not move to the next stage before the user tests and approves the current one.**
6. **Documentation always up to date** (this file, `README.md`, `docs/TECHNICAL.md`, `docs/TESTING.md`) in the same
   commit as the change; the values come from the code and the tests, never from memory.
7. **AMC does not reference Immersive Vehicles** (a promise of AMC's README): all the integration lives in this mod. A
   change in AMC (for example a neutral access to data) only with the user's OK and with AMC's documentation.

## What it is
Integration mod (NeoForge 1.21.1, Java) between **Abandoned Modern City** (AMC, `abandonedmoderncity`) and **Immersive
Vehicles** (IV, mod id `mts`) with the **Official Content Pack** (`mtsofficialpack`). Mod id `amcvehicles`, package
`com.amcvehicles`, folder `Projetos Claude/AMCVehicles`. Created on 2026-11-16.

Goal: abandoned cars appearing at random and rarely in the parking lots and garages of the AMC structures (respecting
the bays, the underground ones too), inside the workshops (never outside) and at the gas stations (beside the pumps:
there is no painted bay there). Until 0.4.2 only the Official Pack's **Mercedes 230**; since 0.5.0 (stage 5) **8
models**: Mercedes 230, Mustang 69 and Scout of the Official Pack (`mtsofficialpack`) and Cagouille, Camaro, Escargot,
Highwayman and Station Wagon of the **Official Automobile Pack** (OAmP, mod id `oamp`, a required dependency).

## The user's decisions (2026-11-16)
- **State of the Mercedes, always**: no engine, no wheels, no seats, **15% of gasoline** (10% until 0.7.3; raised at
  the user's request on 2026-11-17), **speedometer (km/h) and fuel gauge** on the dashboard.
- **Drawn**: hood open and trunk open (each separately; 50% each, my value, to be confirmed) and **each side door open
  or closed, 50% the left and 50% the right, separately** (the user's request, 2026-11-16).
- **Colours**: the 15 civilian ones drawn with the same chance; `_police` and `_police2` **only at the police station**
  (and only them there).
- **Rarity**: **9% per bay** since 0.7.2 (2026-11-17; it was 3% until 0.7.1; each bay draws alone). With it the
  underground garage of the wide building (many bays) has ~3 cars on average and the structures with few bays, 0 or 1.
  Projection with the 57 bays: **3.20 cars per city**, 9.3% of the cities without a car (with 3% it was 1.07 and 38%);
  with the 80 bays of 0.8.0: 3.63 per city, 7.9% without a car; **with the 59 bays of 0.8.1 (glass house, without the
  ramp building): 3.42 per city**, 8.3% without a car.
- **Debug command** that puts a car in **every bay of the structure the player stands in** (to check the detection of
  the bays).
- Name AMC Vehicles, private repository, documentation in Portuguese (in English since 0.9.5, 2026-11-19: phase 6 of the
  1.0.0 work, the user's choice).
- **Only new chunks** get cars (2026-11-16, stage 3): the areas generated before the mod was installed (or before this
  version) stay without cars; for them, and to test, `/amcv draw` applies the real draw to the structure one stands in.
- **Stage 5 (2026-11-16)**: the 8 models with **the same chance**, then the colour with the same chance among the
  model's civilian ones; **the police variants are exclusive to the police station** (reminded by the user), and there
  the draw is **by model** (Mercedes, Cagouille and Mustang, 1/3 each; on the Mercedes `_police` or `_police2` at 50%);
  the Mercedes' state on all of them (no engine, wheels or seats, 15% of fuel since 0.7.4, hood, trunk and each door at
  50%); dashboard **only speedometer km/h + fuel gauge** on all (his choice, between keeping the pack's gauges or not).
- **Stage 6 (2026-11-16)**: fuel pumps at the gas stations, 2 per island, always there (positions approved:
  `docs/spots/bombas_propostas.png`). Model: the user asked for `mts:fuelpump` (base IV's) instead of the
  `mtsofficialpack:fuelpump` he had chosen before. Content: first "0 to 3 buckets of lava" (confirmed by him); after the
  balance below, 70% empty and 30% with 1 bucket (0.6.0); in 0.6.2 the user changed it to **50% empty and 50% with 1, 2
  or 3 buckets** (the same chance each: 1/6), ~1 bucket per pump on average, ~7 per city; **in 0.7.13 (2026-11-18, the
  user's request: "from 0 to 3 to 0 to 4") the most rose to 4: 50% empty and 1, 2, 3 or 4 buckets with 12.5% each,
  1.25 bucket per pump, ~9 per city** (the ~7.3 pumps per city: 0.61 gas station x 8 + 1.21 small gas station x 2).
- **Fuel balance (2026-11-16)**: the user measured that 5 buckets of lava take a car across 6 to 7 cities (~1.3 city per
  bucket) and asked for a balance. Sum: with 0 to 3 buckets per pump there would be ~11 buckets per city (~7 pumps per
  city: 0.61 gas station × 8 + 1.21 small gas station × 2), and lava is endless in the lakes. His choices: **lava yields
  0.35** as an engine fuel (about 0.45 city per bucket; 5 buckets ≈ 2 cities; **removed in 0.7.5**, see "Balance"), set
  by the mod at start; **pumps 70% empty, 30% with 1 bucket** (~2 buckets per city); **the cars' tank stays at 10%**.

## Stages
1. **(approved by the user, 2026-11-16)** Command `/amcv mercedes`: a Mercedes in the asked state in front of whoever
   runs it. After the first test (0.1.0), requests of 0.1.1: drawn doors and the fuel gauge to the right of the
   speedometer. The user tested 0.1.1 ("everything is working") and confirmed hood and trunk 50% and the fuel lava.
2. **(approved by the user, 2026-11-16)** The bays of the underground garage of the wide building
   (`abandoned_wide_building`) and the debug command `/amcv fill`, which fills every bay of the structure the player
   stands in.
3. **(approved by the user, 2026-11-16)** Generation in the world: 3% per bay (9% since 0.7.2), drawn colours, only new
   chunks, `/amcv draw`.
4. **(done in 0.4.0, waiting for the user's test)** The other structures: image of the proposed bays
   (`docs/spots/etapa4_propostas.png`) approved by the user ("Approve all"), with the nose **drawn** at the gas stations
   (his choice) and the **large pharmacy** added by him (the front bays). See "Bays" below. In 0.4.1 the user pointed
   at the garages of the **abandoned house** and of the **small building** (proposal `docs/spots/garagens_propostas.png`,
   "Approve the 6"). In 0.4.2 the cars came to rest on the model's support blocks (the user saw them half buried with a
   100% test jar).
5. **(done in 0.5.0, waiting for the user's test)** The 8 models (see "Models" below); `/amcv car` and `/amcv lineup`
   instead of `/amcv mercedes`.
6. **(done in 0.6.0, waiting for the user's test)** The fuel pumps of the gas stations (`mts:fuelpump`, 10 positions) and
   the lava balance (see "Pumps" and "Balance" below). After testing, the user asked (0.6.1) to **take the cars out of
   the gas stations**: the cars parked beside the pumps were in the way of filling up a built car. The 6 bays of the gas
   stations left `spots.txt` (they stay in a comment); the pumps stay.
7. **(done in 0.7.0, waiting for the user's test)** Loot of workshops and gas stations (see "Loot of workshops and gas
   stations").

**The 1.0.0 work (since 2026-11-18; the plan is kept outside the repository):** six phases, each approved by the user
before the next. Phase 0 (safety net: `GoldenDrawTest` and `docs/baseline/`) **approved on 2026-11-19**. Phase 1 (dead
code) **done in 0.9.0, approved on 2026-11-19**: the drawn nose left (see "Bays"); `Spots.all`, `Spots.allPumps` and
`Spots.Bay.alongZ` stay (only the tests use them, but those tests check the real bays and pumps: the user's choice);
old text fixed. I push from then on (the user granted the permission). Phase 2 (cleaner code, same behaviour) **done in
0.9.1, approved on 2026-11-19**: no automatic formatter, only `.editorconfig` (the user's choice: 4 spaces, UTF-8, LF,
120 columns); long methods split (`Vehicles.spawn`, `Generation.onLevelTick`, `FuelPumps.place`, the commands), repeated
code in one place (`AmcWorld.Placed.of`, `AmcvCommands.structureHere`/`spawn`/`forward`), named numbers; the classes,
except `AmcVehicles`, visible only in the package. Findings for phases 3 to 5: `docs/baseline/FINDINGS.md`.
From then on the user asked to go through phases 3 to 6 without stopping, with the decisions given at once
(`docs/baseline/DECISIONS_1.0.md`). Phase 3 (security) **done in 0.9.2**: an IV error while building a car in the
generation stopped the server (and again at every start, with the chunk pending): now it goes to the log and the car is
skipped; `mts` accepted only in `[24.0.0,25)`; `LICENSE` (MIT, RakkuLyc, 2026); `distributionSha256Sum` in Gradle's
wrapper; a bad line of `spots.txt` names its line number; the test RCON password from `AMCV_RCON_PASSWORD`; the test
server only on 127.0.0.1; new commits with GitHub's noreply e-mail. Tests: 31 (new `FuelBalanceTest` and `SpotsTest`;
for them `build.gradle` puts `com.mojang:logging:1.2.7` and `slf4j-api:2.0.9` only on the tests' runtime).
Phase 4 (defects) **done in 0.9.3**: (1) IV saves `config/mtsconfig.json` with a world open in three places, all through
`ConfigSystem.saveToDisk` (`GUIConfig`, `ControlSystem` and `WrapperWorld.onIVWorldTickPre`, when a player joins for the
first time: `joinedPlayers`); `ConfigSystemMixin` (`com.amcvehicles.mixin`, `amcvehicles.mixins.json`, `[[mixins]]` in
`neoforge.mods.toml`) puts IV's values in memory before the save and the mod's lava after it
(`FuelBalance.beforeIvSave`/`afterIvSave`): the file stays clean and the game keeps 0.05; a file already dirty stays
dirty (edit it by hand). (2) Duplicate car: the entities of a chunk load after it; `Generation.ready` waits for
`ServerLevel.areEntitiesLoaded` of the chunks under the car and `AmcWorld.blocked` answers "entities of chunk x,z not
loaded yet" (the commands do not place the car). Left for the user to decide: closing the window "chunk saved before
`Pending`" (see `docs/TESTING.md`, 0.9.3).
Phase 5 (optimization) **measured, no change**: the mod takes ~0.03% of the server's time while a whole city is generated
(14 µs per new chunk, 10 µs per tick on average); nothing to optimize (`docs/TESTING.md`, phase 5).
Phase 6 (documents in English) **done in 0.9.5**: a short `README.md` for players, `docs/TECHNICAL.md`, this file,
`docs/TESTING.md`, `docs/diagnostic_iv.md` (was `diagnostico_iv.md`), the notes of `docs/baseline/` and the Python tools'
messages in English; terms in `docs/GLOSSARY.md`; the captions drawn inside the images of `docs/spots/` stay in
Portuguese, explained in `docs/spots/README.md`.
Phases 3 to 6 **approved on 2026-11-19**, with my recommendations applied (no code change): the window "chunk saved
before `Pending`" stays as it is (option c), the images keep their Portuguese captions, `ConfigSystemMixin` stays required.
**1.0.0 released on 2026-11-19** (the user's OK): `neoforge.mods.toml` with `displayURL`, `issueTrackerURL` and `credits`
(and the icon, see below), `CHANGELOG.md`, tag `v1.0.0` and a GitHub release with the jar. The user's instance
"MAZ 2.0.0 (TESTE)" had `"lava": 0.05` in `gasoline`, `diesel` and `avgas` (read on 2026-11-19, saved 09:03; not changed:
it needs his OK and the game closed).
**Icon (2026-11-19, the user's request and OK):** `src/main/resources/icon.png` (his screenshot, 512×512) and `logoFile="icon.png"` in `neoforge.mods.toml`. The user asked to keep the version at 1.0.0 (nobody had downloaded it): tag `v1.0.0` moved to the commit with the icon and the release's jar replaced. Same day, the AMC dependency went from `[0.1.0,)` to `[1.0.0,)` (0.1.0 was never public); `libs/abandonedmoderncity-0.1.0.jar` (compile and tests) was NOT swapped for AMC's 1.0.0 jar (their classes and some `.nbt` differ; swapping needs the golden test checked).
**Public repository (2026-11-19, the user's request, like AbandonedModernCity and AMC TACZ Loot):** the e-mail of the 44
old commits rewritten to GitHub's noreply address in the whole history (`git filter-branch --env-filter`; the tree of
HEAD the same, `3110972...`), backup in `~/backups/amcv-repo-2026-11-19` (mirror, the old history), `push --force` of
`main` and `v1.0.0` (approved by the user), the release recreated with the same jar, then public. Left over (the same in
AbandonedModernCity): GitHub still serves the old commits by their direct hash (checked: an old commit's `.patch` shows
the e-mail) until GitHub Support removes them.
**Pending (2026-11-16):** the diagnosis of Immersive Vehicles' problems (sound and fuel, `docs/diagnostic_iv.md`) was
left pending by the user (it waits for his tests and his 4 answers; no fix proposed). Lava at 0.35 was removed in 0.7.5
(2026-11-17), but the `mtsconfig.json` of his instance kept 0.35 written by IV: see "Balance".

## Models (`Cars`, `Vehicles`; stage 5)
- **Table** (`Cars.java`, no Minecraft classes): for each model the key of the commands, the pack and the name, the
  civilian and the police colours, the measures of the `.obj` model (body behind and ahead of the origin, half width,
  height of the support blocks = `lift`, highest point = `top`) and the variables of the doors and of the trunk. Taken
  from the jars by `python3 -I tools/car_info.py <pack.jar> <pack id> <names...>` and checked against the jars by
  `CarsTest`.

  | Key | Pack:name | Civilian | Police | Length | Width | lift | top | Doors | Trunk |
  |---|---|---|---|---|---|---|---|---|---|
  | mercedes | mtsofficialpack:merc230 | 15 | _police, _police2 | 5.77 | 2.50 | 0.469 | 1.625 | door_l, door_r | door_trunk |
  | cagouille | oamp:cagouille | 18 | _police | 6.38 | 2.38 | 0.438 | 1.688 | door_l, door_r | door_trunk |
  | camaro | oamp:camaro | 14 | - | 6.32 | 2.45 | 0.500 | 1.193 | door_l, door_r | door_trunk |
  | escargot | oamp:escargot | 16 | - | 6.35 | 2.44 | 0.469 | 1.506 | door_l, door_r | door_trunk |
  | highwayman | oamp:highwayman | 9 | - | 5.56 | 2.25 | 0.438 | 1.250 | door_left, door_right | door_trunk |
  | station_wagon | oamp:stationmerc | 14 | - | 5.82 | 2.50 | 0.469 | 1.406 | door_fl, door_fr, door_rl, door_rr | door_trunk |
  | mustang | mtsofficialpack:fordmustang69 | 12 | _police | 6.21 | 2.97 | 0.438 | 1.813 | door_l, door_r | (none) |
  | scout | mtsofficialpack:scout | 14 | - | 5.00 | 2.64 | 0.469 | 1.647 | door_l, door_r | door_tailgate (the pickup bed's tailgate) |

  All have the support blocks `cube_fl/fr/rl/rr`. The tallest on its support blocks is the Mustang (2.25):
  `Cars.HEADROOM` = 3 free blocks. The 8 fit in every bay (59 since 0.8.1) in the 4 rotations
  (`PlacementTest.carInsideItsBayAndAlongIt`).
- **Assembly** (`Vehicles.spawn`, the old `Mercedes.spawn` for any model): a new car (data `null`), hood, trunk and each
  door at the drawn value; **dashboard**: speedometer `instrument_car_spedometer_kph` in place of the pack's speedometer
  (a default gauge with "spedometer" in its name), else in the middle one (by the `hudX` position); gauge
  `instrument_car_fuelqty` in place of the pack's ("fuelqty"), else the first to the right of the speedometer (the
  smallest `hudX` above), else the nearest to the left; the pack's other gauges go (`removeIntrument`, IV's method, with
  its misspelling). On the Mercedes this gives the same as before (middle and right). Parts: the pack's defaults, then
  out go the engines, **all the wheels** (the Escargot's spare too) and the seats that are not permanent; the glovebox,
  the invisible seat of the trunk, the Scout's pickup bed and sticker and the Mustang's mirror ornament stay. Fuel: 15%
  of the model's `fuelCapacity` (0.7.4; Camaro and Highwayman 1,500, Mustang 1,200, the others 2,250; they were 10%:
  1,000, 800 and 1,500), the fluid of the type of its default engine (gasoline: lava, without a gasoline mod), saved with
  the fluid's real mod (`minecraft` for lava; 0.6.0): with `""`, as IV does on new cars, a bucket poured into the car
  (mod `minecraft`) would be refused by `EntityFluidTank.fill`, which only joins the same fluid of the same mod or
  `wildcard` (what the pump sends).
- **Draw** (`Draw`): new indices, without changing 0 (the bay has a car): 7 the model, 8 and 9 the rear doors of the
  Station Wagon; 1 (colour) now draws among the model's colours.

- **Battery** (0.7.3, 2026-11-17, the user's request): the cars spawn with a **flat battery** (0%;
  `Vehicles.BATTERY_FRACTION`, which multiplies the model's capacity and goes to `electricPower`, the field IV saves). IV
  puts 85.7% in a new car; measured on 2026-11-17 on a car generated by the mod: `electricPower` 12.0001 of capacity 14.
  With it the jumper pack (Rare (Raro) in the workshops' loot) becomes necessary to start a car. **Not tested**: the user
  said to skip the tests of this change. (Checked on the test server in 0.9.3: the field `electricPower` no longer
  appears on a generated car, and IV does not save zero.)

## How the Mercedes is built (`Mercedes.spawn`, until 0.4.2; since 0.5.0 `Vehicles.spawn`, see "Models")
**Confirmed** (IV 24.0.0's code decompiled + data saved on the test server, before and after a restart): the parts that
stay, the fuel, the saved instruments, the values of `door_hood`/`door_trunk`, the saved angle, and that the car spawns
**unlocked** (no `locked` variable; IV does not keep an owner).
**Seen by the user in the game:** in 0.1.0, the nose to the announced side (south) and the trunk open; in 0.1.1,
"everything is working" (2026-11-16). **Unit test** (`NoseYawTest`, with IV's own rotation maths): the nose points south,
west, north and east with the angles 0, 90, 180 and 270. If a ghost engine ever shows on a client, the alternative is to
add only the permanent parts instead of adding everything and taking the engine out.

- `new EntityVehicleF_Physics(world, null, item, null)`: data `null` = a new car, as from a fresh item. The pack sets
  `door_l`, `door_r` and `door_hood` to 1 (open) through its `initialVariables`; the mod then sets `door_hood`,
  `door_trunk`, `door_l` (the door on the model's +x side, the left of whoever sits) and `door_r` to the drawn value.
- Instruments (before the car is in the world, so they go in the first data): the car has 3 places on the dashboard (0
  left, 1 middle and largest, 2 right); speedometer `instrument_car_spedometer_kph` in 1 (middle), gauge
  `instrument_car_fuelqty` in 2 (to the right of the speedometer, the user's request); 0 stays empty.
- `world.spawnEntity` and `addPartsPostAddition(null, null)`: adds the pack's default parts: the engine
  `enginemercedesm102` and the permanent ones (glovebox `gloveboxscout`, slot 11, and the invisible standing "seat" of
  the trunk `mts:invisible_standing`, slot 9, which the pack uses to carry things in the trunk). The merc230 **has no
  default wheels or seats**. The mod takes the engine out (`removePart`, telling the clients).
- Fuel: `fuelCapacity` 15,000 mB → **1,500 mB**. The fluid is the one IV itself would choose for the pack's engine: the
  most potent of type `gasoline` in IV's fuel config that exists in the game (IV's default: lava 1.0, gasoline 1.0,
  ethanol 0.85). **Without a gasoline mod, it is lava** (as on any new IV car).
- Rotation: IV's angle is Minecraft's with the sign flipped (`WrapperEntity.getYaw`); `Vehicles.spawn` (before,
  `Mercedes.spawn`) takes the Minecraft angle the **nose** points to (0 south, 90 west, 180 north, 270 east). The model's
  origin is the rear axle: the body goes from ~1.3 blocks behind it to ~4.15 ahead (wheels at z 0 and 3.19 of
  `merc230.json`).
- **Height (0.4.2, the user saw the cars half buried)**: without wheels, the merc230 rests on the model's 4 support
  blocks (`cube_fl/fr/rl/rr`, which the pack shows when the wheel of that corner is missing: `part_present_N` = 0 and
  near the ground), with their base at y -0.46875 of the model (the lowest collision boxes too: y -0.37, height 0.2).
  With the origin on the floor the car sank 0.47; now the origin is **0.46875 over the floor** (it was
  `Placement.CAR_LIFT`; since 0.5.0 each model's height, `Cars.Model.lift`, used by `Placement.car` and by `/amcv car`).
  The model's highest point is 1.625 (the light bar of the police colours; the body 1.5625), so the car reaches 2.09 over
  the floor and needs **3 free blocks** (`Placement.HEADROOM`), not 2.

## Bays (`Spots`, `Placement`, `AmcWorld`)
- **List**: `src/main/resources/amcvehicles/spots.txt`, one line per bay:
  `<model> bay <x0> <y> <z0> <x1> <z1> <nose>`, in coordinates of the model's `.nbt` (the structure block's). The model
  is the `model` column of AMC's `index.txt`, else the file name: the colour variants share the bays. x0..x1, z0..z1 =
  the cells of the bay; y = the layer of the floor; nose = where the nose points in the `.nbt` (N = -Z, S = +Z, E = +X,
  W = -X). The car stands in the middle of the bay (the body centred: the rear axle 1.425 behind the middle).
- **Detection** (`tools/find_spots.py <file.nbt> <floor y> [--png] [--out] [--model]`, Python, outside the mod): bay =
  the floor between two parallel painted stripes (straight lines of the stripe block, 4+ long) with 2 to 5 cells between
  them and 6+ of overlap; it needs a floor and 3 free blocks in every cell (else it comes out as BLOCKED and does not go
  in the list). The nose goes to the closed end (the car goes in nose first): the other end, with floor and free height
  beyond it, is the aisle. The image (`--png`) shows the floor, the stripes, the bays (green; red = blocked), the car
  (blue) and the nose (red dot). Approved images in `docs/spots/`.
- **Drawn nose: removed in 0.9.0** (2026-11-19, approved by the user in phase 1 of the 1.0.0 work). Until 0.8.1 the nose
  could be `X` or `Z` in `spots.txt` (along that axis, the way drawn per car: `Draw.Car.flip`, 50%, index 6 of the draw;
  `Spots.Bay.oriented`); only the bays of the gas stations used it, and they left in 0.6.1. Today the parser only takes
  N, S, E, W. Index 6 stays free (the others do not change: the golden test gave the same result). To bring the gas
  stations' bays back (commented in `spots.txt`): a fixed nose for each, or 0.8.1's code from git history.
- **Structures with bays** (the list of 0.4.1, 63 bays in 12 models; **today 59 in 11**: without the gas stations since
  0.6.1 and with the glass house since 0.8.0): wide building 34 (painted stripes, detected); and, written by hand and
  checked (`find_spots.py --bays`): medium workshop 2 and small workshop 2 (inside, one per box, nose to the back), gas
  station 4 and small gas station 2 (beside the islands of the pumps, along them, drawn nose), large pharmacy 4 (the front
  bays between the worn yellow stripes, nose to the shop, up to the hedge of the front), police station 4 (the front bays
  between the worn white lines, nose to the building, only police colours), garage house 1, modern garage house 2 and
  suburban house 2 (inside the garage), abandoned house 2 (the front garage room, without a front wall) and small
  building 4 (the ground floor on pillars: 2 at the entrance, 2 at the back). The coordinates and the reason for each are
  in the comments of `spots.txt`.
  Image of the applied bays, generated from `spots.txt`: `docs/spots/etapa4_vagas.png` (the approved proposal, without
  the large pharmacy and with the gas stations' noses still fixed: `docs/spots/etapa4_propostas.png`). Looked at and
  without bays: the tower (the white concrete grid is the floor of the lobby), the bluish building and the apocalyptic one
  (drawings of the floor), the ramp building (rooms on the ground floor; it got bays in 0.8.0 and lost them in 0.8.1, see
  below). Maps of covered/open floor of the ground layer of the other 9 houses, of the small pharmacy and of the police
  station's basement (`docs/spots/levantamento.png`): no evident garage or parking lot (the police station's basement is
  small rooms); in the houses with a raised floor (mansion, pool, tower, victorian, large) the ground layer comes out
  blocked and the map is not conclusive. The detector only sees whole painted stripes: it does not find bays of worn
  paint (the large pharmacy and the police station were seen by eye) nor garages without paint. The user added the large
  pharmacy and, in 0.4.1, the abandoned house and the small building (which the survey had not seen as garages); other
  structures with bays come in when he points at them.
- **Ramp building REMOVED in 0.8.1 (2026-11-18)**: the user tested 0.8.0 and asked to keep the glass house's bays and to
  take out the ramp building's. The 21 lines stay commented in `spots.txt` (to come back). Today: **59 bays in 11
  models**, 3.42 cars per city, 8.3% without a car. The text below is the history of 0.8.0.
- **Glass house and ramp building (0.8.0, 2026-11-18, the user's request; proposal
  `docs/spots/vidro_rampa_propostas.png`)**: **glass house** (`abandoned_glass_house`) 2 bays in the north-east corner
  room (the one of the photo he sent: open to the west between two pillars at x 32, with an anvil, a chest and barrels on
  the north wall): `35 0 12 41 14 E` and `35 0 16 41 18 E`, nose to the east wall, aisle at z 15; a third does not fit
  (a pillar and a barrel at z 20). My first proposal (the front porch, 3 bays) was in the wrong place: he sent the photo to
  say where it was. **Ramp building** (`abandoned_ramp_building`, structure `special`, ~0.11 copy per city) 21 bays of 3
  × 7 in the long hall (x 8..92, z 49..57), nose to the north wall (N), proposed every 4 cells from x 10 and kept only
  those the detector gave as free (the others have rubble): **10 on floor y=10** (in the open, white marks and a yellow
  line) and **11 on the ground floor y=0** (plain); floor y=5 has none that fits. His choice: y=10 and y=0, 21 bays.
  With 9% that is ~1.9 car per ramp building and ~0.2 per glass house. Total: **80 bays in 12 models** (they were 57 in
  10).
- **Wide building** (`abandoned_wide_building`, a garage with its floor on layer 0 of the `.nbt`, 8 layers buried, 7 of
  free height; stripes of `white_concrete`, yellow lines in the aisle): 36 bays found, **34 in the list**; the 2 of the
  south row at x 43..45 and 47..49 stay out (the stairwell stands in them). Bays of 3 × 8 (north row, nose N) and 3 × 9
  (south row, nose S). Image: `docs/spots/abandoned_wide_building.png`.
- **From the `.nbt` to the world** (`Placement.car`): AMC's own turn (`Structures.Def.template`, read backwards): the box
  of the solid blocks starts at ox, oy, oz of the `.nbt` and measures w × h × d; it turns `(rot of the lot + turn of the
  model) & 3` quarter turns clockwise; it goes to the corner of the lot (x0, z0); the floor of layer y is at
  `base + y - oy - ground`, `base` being the height of the city ground in that column (`ChunkPlan.h2 / 2 - 1`, as AMC's
  `ChunkBuilder`) and the car stands on top of it.
- **In the world** (`AmcWorld`): the structure of a column comes from AMC's plan
  (`Plans.get(level).chunk(cx, cz).lot[i]`). Before placing a car, every column under the body (5.45 × 2) needs a floor
  (a block with collision) and 3 blocks without collision over it (`Placement.HEADROOM`; 2 until 0.4.1) (air, grass,
  vines; on the first one, a thin block up to 1/4 high, like the moss carpet of AMC's overgrowth, `AmcWorld.THIN`) and no
  IV vehicle there (a car of an earlier `fill`: it does not stack); else the bay is skipped and the reason goes in the
  command's reply and in the log. The structures placed with `/amcs spawn` are not in the plan: `/amcv fill` does not
  see them.

## Generation in the world (`Draw`, `Generation`, `Pending`; stage 3)
- **Draw** (`Draw.car`): a pure function of the lot's seed (the copy of the structure; AMC takes it from the world seed)
  and of the **position of the bay** in the `.nbt` (`Spots.Bay.key`: x0, y, z0; not the order in `spots.txt`, so that
  adding or reordering bays of a model does not change the draw of the others), with its own salt (SplitMix64): the same
  bay of the same world always gives the same result, in any loading order. **9% per bay** (`CHANCE_PER_BAY`; 3% until
  0.7.1); then, each separately: colour (the 15 civilian ones with the same chance; at the police station, `type`
  `police_station`, only the 2 police ones), hood 50%, trunk 50%, left door 50%, right door 50%. The chances live in
  `Draw` and the colours in `Cars` (no Minecraft classes, for the tests).
- **Owner**: a car belongs to the chunk of its origin (the rear axle).
- **When**: on the `ChunkEvent.Load` of a **freshly generated** chunk (`isNewChunk`; the event runs on the main thread,
  after the chunk became a `LevelChunk` with status FULL, checked in NeoForge 21.1.219's code, `ChunkStatusTasks`), the
  mod draws that chunk's cars (the structures with bays that have a column in it, through AMC's `ChunkPlan.lot`); if
  there is any, the chunk goes to `Pending` (saved with the world, `data/amcvehicles_pending.dat`). **Never inside AMC's
  generation.**
- **Placing** (`Generation.onLevelTick`, every 10 ticks): the mod goes through its own `Pending` (not a queue fed by the
  loads: so it also catches a chunk that drops below full and comes back at the edge of a player's view without a new
  load event, and the pending ones after a restart); for each pending chunk that is a loaded full chunk, each car of it
  waits until every chunk under the body is a loaded full chunk with its entities loaded (`getChunkNow`, which never
  forces a load, and `areEntitiesLoaded`, since 0.9.3); then `Generation.place` checks the world (`AmcWorld.blocked`:
  floor, 3 of free height (`Placement.HEADROOM`), no IV vehicle, `mts:builder_existing`, there) and places the car, or
  drops it for good if it is blocked (for example a chest drawn by AMC inside the bay) or if IV refuses to build it
  (since 0.9.2: logged, the car skipped). When no car of the chunk waits any more, it leaves `Pending`. In a chunk with a
  car waiting, the others already resolved are not tried again in the same session (no repeated log). The check for "a
  vehicle there" prevents a duplicate car if the server crashes between placing the car and saving.
- **Known limit**: `Pending` is saved with the world; if the new chunk is saved (when unloaded) and the server crashes
  before the world saves `Pending`, the cars of that chunk are lost (it is not "new" again).
- **Log**: each car placed (`[AMCV] <model><colour> in <structure> bay <n> at x y z (generated)`) and each bay dropped,
  with the reason.
- **Projection** (`DrawTest.projectionPerCity`: AMC's real planner on flat terrain, 5 seeds, 492 cities, with the real
  draw; 0.4.1, the 63 bays): 40.5 bays per city, **1.22 car per city**; 32.9% of the cities without a car. Per copy: wide
  building 0.92, police station 0.15, gas station 0.13, large pharmacy 0.12, small building 0.11, abandoned house, modern
  house and suburban house 0.07, small gas station and small workshop 0.06, garage house 0.04, medium workshop 0.03.
  (0.4.0: 1.01 per city; until 0.3.0, only the wide building: 0.32.) **0.6.1, without the 6 bays of the gas stations (57
  bays in 10 models)**: 35.6 bays per city, **1.07 car per city**, 38.0% of the cities without a car.

## Pumps (`FuelPumps`, `Generation`; stage 6)
- **Model**: `mts:fuelpump`, of base IV (`decor.type` `fuel_pump`, a tank of 15,000 mB; the Official Pack's is the same
  inside). Positions in `spots.txt` (lines `<model> pump <x> <y> <z> <front>`, coordinates of the `.nbt`, y = the layer
  over the floor of the island): gas station 8 (2 at each island: x 15 and 17 / 27 and 29, z 9 and 16, front S), small
  gas station 2 (x 8 and 12, z 9, front N). Checked by `find_spots.py --bays` and by `PumpTest` (floor under, the cell and
  the one above free, in every colour and rotation; outside the bays).
- **Lava**: `Draw.buckets`, a pure function of the copy's seed and of the pump's position (`Spots.Pump.key`, with a mark
  that never matches a bay's key): 50% empty (`Draw.PUMP_EMPTY_CHANCE`), the others 1 to `MAX_BUCKETS` (4 since 0.7.13; 3
  before) buckets with the same chance (index 0 decides whether it has lava, 1 how many); independent between the pumps of
  a gas station (tested).
- **Generation**: like the cars. A new chunk with a pump goes to `Pending`. The pump is only placed when its chunk **and
  its 8 neighbours** are loaded full chunks (`Generation.pumpReady`), because AMC's `NeighbourRepair` puts the planned
  blocks back (the pump's cell is air in the `.nbt`) in an already built chunk when a neighbour is generated later. In the
  test, a pump placed by hand in a chunk with a neighbour still to generate **survived** the neighbour's generation
  (hypothesis: for a chunk to become full its neighbours have already gone through the step where AMC repairs, and do not
  repeat it); the rule stays as a safety margin (cost: the pump appears when the neighbours are ready). `placePump` also
  places nothing if the chunks around are not ready (`/amcv draw` in an area not loaded does not force a load), nor where
  there already is an IV block ("already there"), nor where the cell is not air, a plant, a vine, a cobweb or a thin block
  (up to 1/4, like a carpet; the same rule as `PumpTest`), nor without a floor. In the 10 colour variants, the pumps'
  cells are air in the `.nbt` (checked).
- **How the pump is placed**: IV's `WrapperWorld.setBlock` without a player only sets the block, without the part that
  keeps its state (the tile entity). `FuelPumps.place` does what IV does for a player: it sets IV's block (the item's
  `BuilderBlock`, found in the map `BuilderBlock.blockMap`), creates the tile entity with data that carry the angle
  (`angles`, read by the constructor before it sizes the pump's box) and the tank (`currentFluid` `lava`,
  `currentFluidMod` `minecraft`, what IV saves when a player pours a bucket, and `fluidLevel`), and links it to the block
  (`BuilderTileEntity.setTileEntity`). Two members of IV are `protected` and are used by reflection; it worked on NeoForge
  (tested). Alternative, if it ever fails: set the block and load into the block entity an NBT that IV rebuilds by itself
  (`lastLoadedNBT`).

## Balance (`FuelBalance`: 0.35 in 0.6.0 to 0.7.4; removed in 0.7.5; 0.60 in 0.7.9; 0.45 in 0.7.10; 0.35 in 0.7.11; **0.05 since 0.7.12**)
- **0.9.3 (2026-11-19)**: `ConfigSystemMixin` keeps the mod's value out of `mtsconfig.json` (see "The 1.0.0 work"); the
  "Warning" and "Limit" below about IV saving 0.05 to the file are history from then on.
- **0.7.12 (2026-11-17)**: the user tested a jar with lava at 0.05 ("the consumption is very fast, working normally. I
  liked it this way, I want it to be the default"): only the constant `LAVA` (0.35 to 0.05). Each bucket yields 5% of
  IV's normal: ~0.07 city per bucket (by his measure of ~1.3 at 1.0), ~14 buckets per city of travel; the pumps give ~7
  buckets per city and the jerry cans ~0.6, so fuel is very scarce (a note to the user: maybe more lava in the pumps). The
  15% tank of a car (2,250 mB) yields ~0.15 city. The text below holds with 0.05.
- **0.7.11 (2026-11-17, the user's request: "lower it again to 0.35")**: only the constant `LAVA` (0.45 to 0.35): each
  bucket yields 35% of IV's normal, ~0.45 city per bucket (by his measure of ~1.3 at 1.0; 5 buckets ≈ 2.3 cities). The
  text below holds with 0.35.
- **0.7.10 (2026-11-17, the user's request: "reduce it to 0.45")**: only the constant `LAVA` changed (0.60 to 0.45): each
  bucket yields 45% of IV's normal, ~0.6 city per bucket (by his measure of ~1.3 at 1.0; 5 buckets ≈ 2.9 cities). The
  text below holds with 0.45. In his instance the `mtsconfig.json` had 0.6 written by IV (2026-11-17, 21:33); with the
  mod the effective value is the mod's (0.45) and the file only changes if IV rewrites it with a world open.
- **0.7.9 (2026-11-17, the user's request: "change the FuelBalance rule again, this time to 0.60")**: the classes
  `FuelBalance` and `ClientHooks` came back from git history (commit `36560dd`) with `LAVA = 0.60` and the hooks in
  `AmcVehicles`; the rest of the text below holds with 0.60 instead of 0.35. Each bucket yields 60% of IV's normal: ~0.8
  city per bucket (by his measure of ~1.3 at 1.0; 5 buckets ≈ 3.9 cities). The same behaviour: only in memory when the
  server starts and when a client joins a server, IV's values back on stop/leave, `gasoline`, `diesel` and `avgas`.
  **Warning**: if IV saves `mtsconfig.json` with a world open (its controls or config screen), the 0.60 goes to the file
  and stays after the mod is removed (it happened with 0.35 on 2026-11-17 and was fixed by hand).
- (History below: the removal of 0.7.5 and the original text.)
- **Removed at the user's request** ("remove the setting that makes the fuel drain faster"): the classes `FuelBalance`
  and `ClientHooks` and the hooks in `AmcVehicles` deleted; the mod no longer touches the consumption (lava back to 1.0
  in IV's fuel types, ~1.3 city per bucket by his measure). **But the file stayed dirty**: on 2026-11-17, 01:09, the
  `config/mtsconfig.json` of the instance "MAZ 2.0.0 (TESTE)" had `gasoline`, `diesel` and `avgas` at 0.35 (IV saved the
  config with the mod's value in memory, the limit described below). Removing the mod does not undo it: the file must go
  back to `"lava": 1.0` in those three types (`furnace` and `brewing_stand` are already at 1.0).
- **File fixed** on 2026-11-17 (with the game closed, at the user's request): `config/mtsconfig.json` of the instance
  "MAZ 2.0.0 (TESTE)", the 3 lines `"lava": 0.35` (`gasoline`, `diesel`, `avgas`) went back to `1.0`; backup in
  `~/backups/mtsconfig-2026-11-17/mtsconfig.json`; the only difference from the backup is those 3 lines.
- How it was: (original text below)
- When a server starts (`ServerAboutToStartEvent`, the single player world too) and, on a client, when it joins a server
  (`ClientHooks`, only loaded on the client), the mod sets lava's potency to **0.35** in IV's engine fuel types that take
  it (`gasoline`, `diesel`, `avgas` by default; `furnace` and `brewing_stand`, which are not engine ones, stay as they
  are: the first run of 0.6.0 had changed those too). Only in memory: IV writes `config/mtsconfig.json` at the end of the
  game's loading (on the client, `FMLLoadCompleteEvent`, which runs in parallel with the other mods), so the value is set
  after that; when the server stops (`ServerStoppedEvent`) and when a client leaves a server IV's values come back
  (`FuelBalance.restore`). IV's engine divides the consumption by the potency (`PartEngine`:
  `getTotalFuelConsumption() * fuelUsageFactor / fuelFactor`, read at every use), so 0.35 makes each bucket yield ~1/3 and
  applies at once. The log says `[AMCV] Lava as a fuel set to 0.35 (...; was: ...)` and, on stop,
  `... put back to IV's values`. **Limit**: IV also saves the file during a game when the player changes its controls or
  its config screen (`ControlSystem`, `GUIConfig` call `saveToDisk`); such a save, with a world open, takes 0.35 to
  `mtsconfig.json` (to undo it: edit the file). The user approved lava at 0.35 quoting `fuels.gasoline.lava`; the mod
  applies it to `diesel` and `avgas` too (otherwise a diesel engine would be a loophole; to be confirmed with him).

## Loot of workshops and gas stations (`tools/gen_loot.py`; stage 7, 0.7.0, 2026-11-16)
- **Request**: loot for the workshop (car parts, blowtorch, repair kit, jumper pack) and for the gas station (empty and
  full jerry can), **adding to** AMC TACZ Loot, replacing nothing. The user suggested the items and the rarities of the
  blowtorch (Uncommon (Incomum)), repair kit and jumper pack (Rare (Raro)) and of the jerry cans (Epic (Épico) and
  Uncommon, then **both Uncommon**: "1 bucket does not make much difference"); those of seats, wheels (Common (Comum))
  and engine (Rare) are proposals of mine that he approved, along with "raise the chances overall": **all x2** (approved
  on 2026-11-16).
- **Mechanism**: one loot modifier `neoforge:add_table` per AMC table (`abandonedmoderncity:chests/<theme>/<tier>`), the
  same as AMC TACZ Loot; several mods add to the same table and each mod's `global_loot_modifiers.json` list is read.
  Themes `workshop` (both workshops, every colour) and `gas_station` (both gas stations); tiers `chest`, `chest_hard`,
  `barrel`, `barrel_hard` (the hard one uses the same table as the common one: as in AMC TACZ Loot, the item chance does
  not rise in the hard one); **pots get nothing**. 12 tables in `data/amcvehicles/loot_table` (4 of loot and 8 of choice
  in `pick/`) and 8 modifiers in `data/amcvehicles/loot_modifiers`. The mod replaces no table.
- **Format (AMC TACZ Loot's)**: each item has its own, independent draw: a weight in 10,000 and an "empty" with the rest;
  `quality` = 25% of the weight (Luck works as there; the console's `/loot` does not apply Luck).
- **Chances per container** (AMC TACZ Loot's classes x2; barrel = half of the chest): Common 9.2% chest / 4.6% barrel;
  Uncommon 4.4% / 2.2%; Rare 2.0% / 1.0%. Everything at the top of `gen_loot.py` (`BASE_CHEST`, `FATOR`, `BARREL_MULT`).

  | Item | Class | Where | Detail |
  |---|---|---|---|
  | Seat | Common | workshops | 4 models with the same chance, then the colour (34 ids): `carseat` (7 colours), `sportseat` (7), `vintageseat` (10) and the generic `seat` (10 colours, ids without `_`: `mts:mtsofficialpack.seatblack`...; included at the user's request on 2026-11-16); not the `seat_harness` (aircraft) |
  | Wheel | Common | workshops | **only offroad and vintage** (the user's request, 2026-11-16), with the same chance, then the variant (`wheeloffroad_default`; `wheelvintage_lightblue`, `_mint`, `_sinty`: 4 ids); 1 at a time; no `wheelvintage_police` (police is exclusive to the police station); diameter 1.0, taken by the 8 cars (0.4 to 1.0); the small, medium and large wheels do not come in |
  | Engine | Rare | workshops | the 5 engines of the 8 cars (M102, FE428, AMC I4, Escargot I6, Camaro V8) |
  | Wrench | Common | workshops | `mts:mts.wrench`, of base IV (0.7.7, the user's request on 2026-11-17; in the workshops, not at the gas stations) |
  | Blowtorch | Uncommon | workshops | `mtsofficialpack:blowtorch` (repairs 50) |
  | Repair kit | Rare, **x1.1** | workshops | `mtsofficialpack:repairkit` (repairs a destroyed car); the chance is the Rare one plus 10% (**chest 2.2% / barrel 1.1%**, `ITEM_MULT` in `gen_loot.py`; the user's request on 2026-11-17, 0.7.6) so it is a little more common than the engine and the jumper pack. The draws are independent: one item does not "compete" with another, this only orders them |
  | Jumper pack | Rare | workshops | `mts:jumperpack` |
  | ~~Empty jerry can~~ | removed | - | left the loot in 0.7.8 (2026-11-17, the user's request); the player crafts one (7 iron ingots and a bucket) and fills it at a pump |
  | Full jerry can | Uncommon | gas stations | `mts:jerrycan` with `{jerrycanFluid:"lava"}` in `minecraft:custom_data` (what IV saves when it is filled at the pump: 1,000 mB, 1 bucket) |
- **Expected per structure** (containers measured in AMC TACZ Loot's `docs/containers.json`; a hard barrel counts as a
  barrel): medium workshop (1 chest, 61 barrels) seat, wheel and wrench 2.9, blowtorch 1.4, engine and jumper 0.63,
  repair kit 0.69; small workshop (2 chests, 39 barrels) 2.0, 0.95, 0.43; gas station (4 chests, 26 barrels) 0.75 full
  jerry can; small gas station (1 chest, 3 barrels) 0.11. **Per city** (copies per city of `DrawTest`): 4.4 seats, 4.4
  wheels and 4.4 wrenches, 2.1 blowtorches, 0.97 of engine, of repair kit and of jumper pack, 0.59 full jerry can (0.7.8:
  only the full one; before there were 0.59 empty + 0.59 full).
- **Ids**: `mts:<pack>.<name><variant>` (e.g. `mts:mtsofficialpack.carseat_grey`, `mts:mts.jerrycan`); the generator
  checks each id against the item model in the jars of `libs/` and `LootDataTest` does too.
- **Use**: `python3 -I tools/gen_loot.py` (preview), `--write` rewrites `src/main/resources/data/...`.
  `python3 -I tools/sample_loot.py <theme>/<tier> [N]` samples on the test server and compares with the expected.

## Architecture
```
AmcVehicles    the @Mod: registers the commands and the events (generation, FuelBalance when the server starts and
               stops, ClientHooks on the client)
Cars           the 8 models: colours, measures, doors, trunk (pure; checked against the jars)
Vehicles       builds an abandoned car of any model (state, dashboard, parts, fuel)
FuelPumps      places an IV fuel pump (without a player) with lava
Spots          the bays of spots.txt, by model (no Minecraft classes)
Placement      bay of the .nbt -> position, nose and columns of the car in the world (no Minecraft classes; tested)
AmcWorld       reads AMC's plan (the structure of a column) and checks whether the car fits in the world
Draw           the draw of the cars (9% per bay, model, colour, hood, trunk, doors), pure
Generation     the generation: draws when a chunk is generated, places on the tick when the chunks under the car (or
               around the pump) are ready
Pending        the chunks with a drawn car not yet placed (saved with the world; gone through every 10 ticks)
FuelBalance    lava at 0.05 as an IV engine fuel, only in memory (set at start, put back at stop)
ClientHooks    the same as FuelBalance on the client, when joining and leaving a server (only loaded on the client)
mixin/ConfigSystemMixin  IV's values in memory while IV saves its config file (0.9.3)
AmcvCommands   /amcv (op 2): test commands
```

## Commands (op 2)
- `/amcv car <model> [colour] [hood trunk]` (0.5.0; until 0.4.2 `/amcv mercedes`): a car with its rear axle 3 blocks in
  front of whoever runs it, on its support blocks, the nose where they look (rounded to N, S, E or W). Model by key
  (`mercedes`, `cagouille`, `camaro`, `escargot`, `highwayman`, `station_wagon`, `mustang`, `scout`); colour without
  the `_` (the police ones too); hood and trunk `true`/`false`; what is missing is drawn (the doors always). From the
  console: `execute positioned X Y Z rotated YAW 0 run amcv car ...`. It answers with the position, the model and the
  colour, the hood, the trunk, each door, the fuel, the fluid, the gauges used, the parts taken out and those that stayed.
- `/amcv lineup` (0.5.0): the 8 models side by side (4 blocks between them, to the right of whoever looks), a civilian
  colour drawn, then the 4 police cars (`mercedes_police`, `mercedes_police2`, `cagouille_police`, `mustang_police`):
  to look at the height, doors and dashboard of all of them. It answers one line per car.
- `/amcv pump [buckets]`: a fuel pump (`mts:fuelpump` since 0.6.0) 3 blocks ahead, its front to whoever runs it, with that
  many buckets of lava (0 to 15).
- `/amcv fill` and `/amcv draw` also place the structure's pumps (`fill` draws the lava now, with the generation's
  chance; `draw` uses the copy's drawn lava and lists each pump with its chunk).
- `/amcv fill`: a car in **every bay** of the AMC structure whoever runs it stands in (inside its lot, at any height; in
  the garage too); model, colour, hood, trunk and doors drawn (at the police station, the police cars). It answers with
  the structure, the corner of the box, the rotation, the ground height, how many bays, how many cars and the skipped
  bays with the reason. To take the cars away: `/kill @e[type=mts:builder_existing,distance=..80]`.
- `/amcv draw`: the **real draw** (the generation's) in the structure whoever runs it stands in: it places now the drawn
  cars that are not there yet (in a chunk generated with the mod they already are: the reply says "occupied by a
  vehicle"). It answers with the copy's seed, the bays, how many drawn, how many placed now, the pending chunks and, for
  each drawn bay, the colour, the origin (rear axle), the chunks under the body and what happened.

## Build and test
- Build (Java 21; the system's default Java is 27, which Gradle does not accept):
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME`
  → `build/libs/amcvehicles-<version>.jar`.
- `libs/` (not versioned) needs `Immersive Vehicles-1.21.1-24.0.0.jar`, `MTS Official Pack-1.21.1-V29.jar`,
  `OAmP-1.21.1-V3.jar` (copies in `AbandonedModernCity/libs/`; the two packs for `CarsTest`) and
  `abandonedmoderncity-0.1.0.jar` (AMC's jar, from `AbandonedModernCity/build/libs/`; the one used in 0.2.0 is that of
  2026-11-16, 02:40). The build compiles against them and the tests use AMC's pure code (structures and `Def.code`); none
  goes inside this mod's jar. Their SHA-256: `docs/TESTING.md`, version 0.9.2.
- Unit tests: `./gradlew test` (the same `JAVA_HOME`; ~15 s, AMC loads its 52 structures). 31 tests since 0.9.2;
  `GoldenDrawTest` compares the draw and the position of each car and pump with
  `src/test/resources/golden/draw_golden.txt` (safety net of the 1.0.0 work; the snapshot of 0.8.1 is in
  `docs/baseline/README.md`). If it fails, the draw or the position changed: the reference is only rewritten (delete the
  file and run again) when the change was asked for by the user.
- Test server: `./gradlew runServer` (the same `JAVA_HOME`), with the jars of IV, of the two packs and of AMC in
  `run/mods`; a normal world on seed 20261027, RCON on port 25576 (password `teste`, or the variable `AMCV_RCON_PASSWORD`;
  the server listens only on 127.0.0.1 since 0.9.2), `python3 -I tools/rcon.py "command" ...`;
  `python3 -I tools/check_bays.py <x> <z> <radius>` checks that each car stands between two stripes;
  `python3 -I tools/check_generation.py [--load] "x0,z0 .. x1,z1" ...` compares the cars of each box (the ones
  `/amcs locate` shows) with the draw. The scripts use `tools/rconlib.py` (it reads long replies whole: RCON splits them in
  packets of 4096; and it sends one packet at a time: the game's `RconClient` reads up to 1,460 bytes, handles one packet
  per read and closes the connection when a read brings less than 10 bytes, checked in the code). Details and results in
  `docs/TESTING.md`.
