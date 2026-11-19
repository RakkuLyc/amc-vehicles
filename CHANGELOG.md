# Changelog

All dates are 2026. Details and test results of each version: `docs/TESTING.md`.

## 1.0.0 — 2026-11-19
First release. The same game content as 0.9.5; `neoforge.mods.toml` gains the project's page, issue tracker, credits and an icon (`icon.png`); it requires Abandoned Modern City 1.0.0 or newer (the dependency said 0.1.0, a development version that was never released).
What the mod does: abandoned Immersive Vehicles cars (8 models, 9% per parking bay, 59 bays in 11 AMC structures; police
cars only at the police station) with no engine, wheels or seats, a flat battery and 15% of fuel; fuel pumps at every gas
station (half empty, the others with 1 to 4 buckets of lava); lava as a weak engine fuel (0.05); car parts in the
workshops' loot and a full jerry can at the gas stations; the `/amcv` test commands.

## 0.9.x — the 1.0.0 work (2026-11-18 to 2026-11-19)
- **0.9.5**: documents in English (a short README, `docs/TECHNICAL.md`, `docs/GLOSSARY.md`); tool messages in English.
- **0.9.3**: Immersive Vehicles' `mtsconfig.json` no longer receives the mod's lava value (a mixin around IV's config
  save); no duplicate car when a structure is checked right after the server starts (the mod waits for the entities to
  load).
- **0.9.2**: an error of Immersive Vehicles while building a car during the world generation no longer stops the server
  (the car is skipped and logged); only Immersive Vehicles 24.x is accepted; `LICENSE` (MIT); the Gradle wrapper checks the
  distribution's hash; a malformed `spots.txt` line names its line number.
- **0.9.1**: cleaner code, same behaviour (long methods split, repeated code merged, named constants).
- **0.9.0**: dead code removed (the unused "drawn nose" of the bays); old comments fixed.

## 0.8.x
- **0.8.1** (2026-11-18): the ramp building's bays removed (kept commented); the glass house's 2 bays stay.
- **0.8.0** (2026-11-18): bays at the glass house (2) and at the ramp building (21).

## 0.7.x (2026-11-16 to 2026-11-18)
- **0.7.13**: pumps hold 1 to 4 buckets when they have lava (was 1 to 3).
- **0.7.12**: lava yields 0.05 as an engine fuel (the default, after the user's test).
- **0.7.9 to 0.7.11**: lava's potency back as a setting (0.60, 0.45, 0.35).
- **0.7.8**: the gas stations' jerry can is always full (the empty one left the loot).
- **0.7.7**: wrench in the workshops' loot.
- **0.7.6**: repair kit 10% more common than the other Rare items.
- **0.7.5**: lava's lower potency removed.
- **0.7.4**: the cars' tank at 15% (was 10%).
- **0.7.3**: the cars spawn with a flat battery.
- **0.7.2**: 9% per bay (was 3%).
- **0.7.1**: generic seat in the loot; only offroad and vintage wheels.
- **0.7.0**: loot of workshops and gas stations (added to AMC TACZ Loot).

## 0.6.x (2026-11-16)
- **0.6.2**: pumps 50% empty, the others with 1 to 3 buckets.
- **0.6.1**: no cars at the gas stations (they were in the way of filling up).
- **0.6.0**: fuel pumps at the gas stations; lava as a weaker engine fuel (0.35).

## 0.5.0 to 0.1.0 (2026-11-16)
- **0.5.0**: 8 car models (`/amcv car`, `/amcv lineup`).
- **0.4.2**: the cars rest on their support blocks (they were half buried).
- **0.4.1**: garages of the abandoned house and of the small building.
- **0.4.0**: bays at the other AMC structures.
- **0.3.0**: cars generated with the world (only new chunks); `/amcv draw`.
- **0.2.0**: the bays of the wide building's underground garage; `/amcv fill`.
- **0.1.1**: drawn doors; the fuel gauge to the right of the speedometer.
- **0.1.0**: `/amcv mercedes`, one abandoned Mercedes 230.
