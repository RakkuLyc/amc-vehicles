# Findings for the next phases (noted while reading the code in phase 2, 2026-11-19)

Nothing of this was fixed in phase 2, because that phase changes no behaviour. Each item was a **hypothesis** (seen by
reading the code) until it was tested in the phase shown.

## Phase 3 (security and robustness)
- **[CONFIRMED and FIXED in 0.9.2]** **An IV error inside the generation's tick**: `Generation.place` called
  `Vehicles.spawn` without a `try`. If IV throws an error (a pack item that no longer exists, for example after updating
  or removing OAmP, or a renamed colour), the exception leaves `Generation.onLevelTick` (event `LevelTickEvent.Post`).
  Hypothesis: it stops the server, or repeats every 10 ticks, because the chunk stays in `Pending`. The commands
  (`AmcvCommands`) already caught the error; the generation did not.
- **A failure of the reflection in `FuelPumps`** (a future IV): the `catch` returns the reason, `Generation.placePump` logs
  and returns it, and `resolve` counts the pump as resolved. By reading, the chunk **leaves** `Pending` (it does not get
  stuck). To be confirmed by a test.
- **Command replies with `e.toString()`** (`car`, `lineup`, `fill`): they show the exception's class name to the operator.
  Low severity (op 2 only); the log already has the stack trace.
- **[REFUTED in 0.9.2]** **`FuelBalance.ORIGINAL`**: `FuelBalanceTest` shows that, in both orders (server or client first),
  IV's values come back; the second application already finds 0.05 and saves nothing as original.

## Phase 4 (defects)
- **[CONFIRMED and FIXED in 0.9.3]** **A duplicate car right after the server starts**: in the test world, `/amcv draw` at
  the police station right after starting the server said "placed" for a car that was already there; afterwards there
  were **2** vehicles in the bay (`execute if entity ...` gave `count: 2`). Likely cause (hypothesis, confirmed in 0.9.3):
  the entities of a chunk load after the chunk, and `AmcWorld.blocked` looked for the vehicle before that. The same held
  for `Generation` in a pending chunk after a crash.

## Phase 5 (optimization; measure first)
- `Generation.resolve` recomputes every 10 ticks, for each loaded pending chunk, the draw and the position of all the cars
  and pumps of the chunk's structures (`carsOf`/`pumpsOf`), even when nothing changed.
- `FuelPumps.place` looks up by reflection `BuilderBlock.blockMap` and `BuilderTileEntity.setTileEntity` at every pump.
- `AmcWorld.blocked` does an entity search in the car's box before looking at the blocks.
- (Measured in phase 5: the mod takes ~0.03% of the server's time; none of these was changed.)
