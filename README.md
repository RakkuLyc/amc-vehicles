# AMC Vehicles

A NeoForge 1.21.1 mod that puts abandoned **Immersive Vehicles** cars in the structures of **Abandoned Modern City**:
in the underground garage of the wide building, the parking lots, the garages of the houses and inside the workshops,
plus fuel pumps at the gas stations and car parts in the workshops' loot. Version 0.9.5 (2026-11-19), MIT license.

## Requirements
- Minecraft 1.21.1 with NeoForge 21.1.219 or newer.
- Immersive Vehicles **24.x** (`mts`; version 25 or newer is refused on purpose: the mod reaches two of its internal
  members to place the fuel pumps).
- MTS Official Pack V29+ (`mtsofficialpack`) and Official Automobile Pack V3+ (`oamp`).
- Abandoned Modern City 0.1.0 (build of 2026-11-16 or newer).

## Installation
Put `amcvehicles-0.9.5.jar` in the `mods` folder next to the four mods above. Only **areas generated after** the mod is
installed get cars and fuel pumps; for an area generated before, an operator can run `/amcv draw` inside a structure.

## What it does
- **Cars**: 9% chance in each of the 59 parking bays of 11 AMC structures (on average ~3.4 cars per city). 8 models
  with the same chance (Mercedes 230, Mustang 69, Scout, Cagouille, Camaro, Escargot, Highwayman, Station Wagon), then a
  colour; police cars only at the police station (and only police cars there). Every car is abandoned: no engine, no
  wheels, no seats, a flat battery (a jumper pack is needed), 15% of its tank, only a speedometer and a fuel gauge;
  hood, trunk and each door open or closed at random.
- **Fuel pumps** at every gas station (8, or 2 at the small one): half are empty, the others hold 1 to 4 buckets of lava.
- **Lava is a weak fuel**: it goes 5% as far as normal in Immersive Vehicles (the mod sets this in memory when a world
  starts and puts it back when it stops).
- **Loot** (added to AMC's tables, nothing replaced): seats, wheels, wrenches, blowtorches, engines, repair kits and
  jumper packs in the workshops; a full jerry can at the gas stations.

## Commands (operators, level 2)
- `/amcv car <model> [color] [hood trunk]`: one abandoned car in front of you (e.g. `/amcv car mustang police`).
- `/amcv lineup`: the 8 models and the 4 police cars side by side.
- `/amcv fill`: a car in every bay of the AMC structure you stand in (to check the bays).
- `/amcv draw`: the real draw of the structure you stand in; places what is missing.
- `/amcv pump [buckets]`: a fuel pump in front of you with 0 to 15 buckets of lava.

## Important: Immersive Vehicles' config file
Since 0.9.3 the mod keeps its lava value out of `config/mtsconfig.json`. **Versions before 0.9.3 could write it there**
(when Immersive Vehicles saved its config with a world open), and then lava stays weak even without the mod. If your
file has `"lava": 0.05` (or 0.35, 0.45, 0.6) under `gasoline`, `diesel` or `avgas`, close the game and set it to `1.0`.

## Known issues
- Areas generated before the mod was installed have no cars (use `/amcv draw` there).
- If the server crashes after a new chunk is saved and before the world saves the mod's list of pending chunks, the cars of
  that chunk are lost (never duplicated).
- `/amcv fill` does not see structures placed with `/amcs spawn` (they are not part of the city plan).

## More
Technical documentation: `docs/TECHNICAL.md`; tests and their results: `docs/TESTING.md`; the work rules and the user's
decisions: `CLAUDE.md`.
