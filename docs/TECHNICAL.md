# AMC Vehicles — technical overview

Integration mod for NeoForge 1.21.1 between **Abandoned Modern City** (AMC) and **Immersive Vehicles** (IV): abandoned
cars in the AMC structures. Version 1.0.0 (2026-11-19; the release after the six phases of the 1.0.0 work). Stage 6 content:
8 car models and fuel pumps at the gas stations. The cars **appear by themselves** when the world is generated (9% per
bay) in the 59 parking bays of 11 AMC structures (list below; since 0.6.1 no car at the gas stations, so they do not get
in the way of a player who comes to fill up), and the pumps at every gas station. Only areas generated after the mod is
installed get cars and pumps. Recent versions: 0.9.3 keeps the mod's lava out of `mtsconfig.json` and no longer places a
car twice right after the server starts; 0.9.2 no longer stops the server when IV refuses to build a car during the
generation, and accepts only IV 24.x.

## The abandoned cars
- **Models** (the same chance each): from the Official Pack, **Mercedes 230**, **Mustang 69** and **Scout**; from the
  Official Automobile Pack, **Cagouille**, **Camaro**, **Escargot**, **Highwayman** and **Station Wagon**. Then the
  colour, with the same chance among the model's civilian colours.
- **Police cars only at the police station** (and only them there): Mercedes (`_police`/`_police2`), Cagouille and
  Mustang, 1/3 each.
- Always: **flat battery** (0.7.3; a jumper pack is needed), **no engine, no wheels** (no spare wheel either), **no
  seats**, **15% of fuel** of each model's tank (10% until 0.7.3) and, on the dashboard, **only a speedometer (km/h) and
  a fuel gauge**. They rest on the support blocks the model itself shows when the wheels are missing.
- Drawn: hood, trunk (the Scout's tailgate; the Mustang has none) and each side door (the Station Wagon has 4), each
  open or closed with 50%, separately.
- Rarity: **9% per bay**, drawn once per copy of the structure (the same in any loading order). On average ~3.4 cars per
  city (8% of the cities with none; with 3% it was ~1.1; without the glass house, 3.2). A bay with something in the way
  (for example a chest AMC drew there) stays without its car.
- The fuel is what IV would put in a new car with the model's engine (type `gasoline`): without a gasoline mod, lava.

## Fuel pumps and fuel
- `mts:fuelpump` pumps (IV's own) always at the gas stations: 8 at the gas station (2 at each island) and 2 at the small
  gas station (`docs/spots/bombas_propostas.png`). **50% empty; the others with 1, 2, 3 or 4 buckets of lava** (the same
  chance each, 12.5%; ~1.25 bucket per pump, ~9 per city; the most was 3 until 0.7.12).
- **Lava yields 0.05** as an engine fuel (gasoline, diesel and avgas) since 0.7.12: ~0.07 city per bucket (in plain IV,
  ~1.3), so the consumption is very fast (the user's choice after testing it). The mod sets this when a server starts (or
  when a client joins a server), only in memory, and puts IV's value back when it stops; the file `config/mtsconfig.json`
  is not changed by the mod. Since 0.9.3, when IV saves its config with a world open (its controls and config screens, a
  player's first join), the mod puts IV's values back only during the save: the file keeps IV's values. **A dirty file
  from older versions**: if your `mtsconfig.json` already has `"lava": 0.05` (or 0.35, 0.45, 0.6) under `gasoline`,
  `diesel` or `avgas`, set it to `1.0` with the game closed; the mod cannot know the original value. (History: 0.35 in
  0.6.0 to 0.7.4, removed in 0.7.5, 0.60 in 0.7.9, 0.45 in 0.7.10, 0.35 in 0.7.11.)

## Loot of workshops and gas stations
Adds to AMC TACZ Loot (it adds to AMC's tables, replaces nothing; pots get nothing). Chance per container (a barrel has
half): Common (Comum) 9.2% chest / 4.6% barrel; Uncommon (Incomum) 4.4% / 2.2%; Rare (Raro) 2.0% / 1.0%.
- **Workshops**: seats, wheels and wrench (Common), blowtorch (Uncommon), engine, repair kit and jumper pack (Rare).
  Seats: Car Seat, Sport Seat, Vintage Seat and the generic seat (34 colours); wheels: only offroad and vintage; engines:
  the 5 of the 8 cars; nothing of the police.
- **Gas stations**: only IV's jerry can, **always full** (1 bucket of lava), Uncommon (chest 4.4% / barrel 2.2%).
- The repair kit is 10% more common than the engine and the jumper pack (chest 2.2% / barrel 1.1% against 2.0% / 1.0%).
- On average, per city: ~4.4 seats, ~4.4 wheels, ~4.4 wrenches, ~2 blowtorches, ~1 engine, ~1.1 repair kit, ~1 jumper
  pack and ~0.6 jerry can (always full).

## Parking bays
| Structure | Bays | Where |
|---|---|---|
| Wide building | 34 | underground garage, between the white stripes, nose to the wall |
| Police station | 4 | front parking lot (worn white lines), nose to the building, **police cars only** |
| Large pharmacy (5 colours) | 4 | front parking lot (worn yellow stripes), nose to the shop |
| Medium workshop (3 colours) | 2 | inside, nose to the back |
| Small workshop (5 colours) | 2 | inside, one in each box |
| Small building | 4 | ground floor on pillars (2 at the entrance, 2 at the back) |
| Modern garage house | 2 | inside the garage |
| Suburban house | 2 | inside the garage (door on the west side) |
| Abandoned house | 2 | front garage room |
| Garage house | 1 | inside the garage |
| Glass house | 2 | north-east corner room (open to the west), side by side, nose to the back wall |

Images: `docs/spots/abandoned_wide_building.png`, `docs/spots/etapa4_vagas.png`, `docs/spots/garagens_propostas.png`
and `docs/spots/vidro_rampa_propostas.png` (the proposal of 0.8.0; the ramp building's bays left in 0.8.1); their captions
are explained in `docs/spots/README.md`. The car stands centred in the bay.

## Commands (op 2)
- `/amcv car <model> [color] [hood trunk]`: a car in front of you (e.g. `/amcv car mustang police`). What is not given
  is drawn.
- `/amcv lineup`: the 8 models and the 4 police cars side by side in front of you.
- `/amcv fill`: a car in every bay of the AMC structure you stand in (to check the bays). It does not work on structures
  placed with `/amcs spawn` (they are not part of the city plan).
- `/amcv draw`: the real draw (the generation's) in the structure you stand in: it says which bays have a car and places
  the missing ones (useful in areas generated before the mod and to test).
- `/amcv pump [buckets]`: a fuel pump in front of you with that many buckets of lava.
- `/amcv fill` and `/amcv draw` also place the pumps of the gas station you stand in.

## Requirements
- Immersive Vehicles 24.x (`mts`, `[24.0.0,25)` since 0.9.2), MTS Official Pack V29+ (`mtsofficialpack`) and
  **Official Automobile Pack V3+ (`oamp`)**.
- Abandoned Modern City (0.1.0, build of 2026-11-16 or newer).

## Build
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME` →
`build/libs/amcvehicles-1.0.0.jar` (it also runs the tests). The folder `libs/` (outside git) needs the jars of
Immersive Vehicles, of the two packs and of AMC (their SHA-256 are in `docs/TESTING.md`, version 0.9.2).
Technical details, decisions and tests: `CLAUDE.md` and `docs/TESTING.md`.
