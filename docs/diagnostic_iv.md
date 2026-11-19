# Diagnosis: Immersive Vehicles problems (2026-11-16)

The user's request: car sounds in conflict with vanilla sounds, a wrong fuel gauge, a car that does not fill up. Sources:
the `latest.log`, the `debug.log` and the 21 previous logs of the instance "MAZ 2.0.0 (TESTE)"; the code of IV 24.0.0
(decompiled), of Minecraft 1.21.1 (NeoForge 21.1.219's sources) and of Superb Warfare 0.8.9.2; and an isolated test with
the game's audio library (LWJGL 3.3.3, OpenAL Soft 1.23.1), on a virtual device (no sound). **Nothing was changed.**
(Left pending by the user: it waits for his tests and his answers; translated in 0.9.5, not updated.)

Key: **Confirmed** = seen in the code, in the log or in a test. **Hypothesis** = likely, without proof. **Test** = what
decides.

## 1. Sounds (754 errors `Allocate new source: Invalid parameter parameter value.` in the `latest.log`)

### Confirmed
- **Frequency**: the 754 errors are only in the last session (2026-11-16, 23:07). In the 21 previous sessions there were 0
  or 1 error per session, including with AMC Vehicles 0.5.0 and 0.6.0.
- **Trigger in the session**: 23:23:18 and 23:23:23, `System default audio device has changed!`: the default device went
  to `sink-sunshine-stereo` (the virtual device of Sunshine, a streaming server) and back to the card's audio. Both times
  Minecraft restarted its sound engine. The first burst of errors starts at 23:24:09 (the only earlier error is a `Stop:
  Invalid name parameter.` at 23:23:18, during the switch itself).
- **IV keeps the buffers of the sounds** (the loaded audio data) in a list (`InterfaceSound.dataSourceBuffers`) and only
  clears it when it notices the restart, by testing a car sound **that is playing**. Its message for that ("Had an
  invalid sound name. Was the sound system reset?") **does not appear** in the log: the list was not cleared.
- **Isolated test**: a buffer ID of the old device, used after reopening the device, gives `AL_INVALID_VALUE` (the exact
  text of the log) when the ID no longer exists, and, when the ID was reused, **binds Minecraft's buffer without an
  error** (a car sound would play a vanilla sound).
- **IV leaves the error pending**: when it plays a sound it clears the errors, binds the buffer
  (`alSourcei(AL_BUFFER, id)`) and does not read the error afterwards.
- **Isolated test**: an error left by one thread is read by the check of another (IV updates its sounds on the "Render
  thread"; Minecraft creates its own on the "Sound engine").
- **Minecraft** (`Channel.create`) creates the channel, reads any pending error and, if there is one, **drops the vanilla
  sound**; the created channel is not deleted (it is lost).
- **A second path to the same error, without a device switch**: when IV cannot get a channel, it deletes the buffer
  (`alDeleteBuffers`) but **does not take it out of the list**; later it uses a deleted buffer.
- **Withdrawn** (I had assumed it): running out of channels. The isolated test shows it gives another error
  (`AL_OUT_OF_MEMORY`, "Unable to allocate memory", which does not appear in the log); and Superb Warfare already raises
  OpenAL's limit to 4,096 (in the test: 4,095 channels with its setting). Also withdrawn: pitch 0 on the starting sound
  (OpenAL takes pitch 0; a negative pitch, a negative volume or NaN is what gives the error).
- **Superb Warfare**: `SoundLimit` (sets `sources = 4096` in OpenAL) and `LibraryMixin` (raises Minecraft's channel limits
  to 4,096 and 64). Neither of the two makes calls that cause this error.

### Hypothesis
- In your session: a switch of audio device (Sunshine) with no car sound playing, IV's list kept invalid buffers, each new
  car sound left a pending `INVALID_VALUE`, and Minecraft read it and dropped vanilla sounds (and, where the ID was reused,
  cars played vanilla sounds).
- The game uses the OpenAL that comes with LWJGL (1.23.1), the same as the test: the instance's setting does not ask for
  the system's OpenAL (1.25.2), but I did not check with the game open.

### Test (changing nothing)
1. Open the game, stay **without a running car nearby** and switch the default audio device (or connect and disconnect
   Sunshine); then start a car and drive among mobs. Expected by the hypothesis: the errors come back and sounds get
   strange.
2. Repeat **with a running car** during the switch. Expected: IV notices ("Had an invalid sound name" in the log) and
   there are no errors.
3. Leave and enter the game after the switch: expected that everything goes back to normal.

## 2. Fuel

### Confirmed (IV's code)
- **The pump fills the vehicle nearest to it** (the vehicle's origin, up to 16 blocks), not the one the player wants
  (`ATileEntityFuelPump.interact`). If the nearest one has no engine: "no engine" (`NOENGINE`); if it has another fluid:
  "does not contain the same fluid" (`MISMATCH`).
- **"There is no fuel in this vehicle"** is decided on the **client**, by the tank of the client's copy
  (`PartEngine.autoStartEngine`: level 0 on the client).
- **The client's copy** only changes through "add/remove X" packets (`PacketFluidTankChange`), which find the tank by its
  **identifier** (UUID) in IV's entity map (`EntityManager.trackedEntityMap`).
- **A vehicle's tank is never removed from that map**: the vehicle registers it (`world.addEntity(fuelTank)`) and no IV
  class calls `remove()` on a vehicle's tank (only on those of parts, pumps and fluid loaders).
- **When a tank is created with an identifier that already exists in the map**, IV gives it a random identifier
  (`AEntityA_Base`).
- **The pump, when it fills an empty tank, saves the mod `wildcard`** in it (`fill(fluid, "wildcard")`); a bucket afterwards
  (mod `minecraft`) is refused (`fill` only joins the same fluid with the same mod, or an incoming `wildcard`).

### Hypothesis (a wrong gauge and "no fuel" with fuel)
- The car leaves the client's range (death, distance, change of dimension) and comes back; the client's new tank gets a
  random identifier (the old one stays in the map); the server's packets go to the old tank; the gauge and the start check
  look at the new one, which does not move. In the session: death at 23:21:33 (followed by "unknown entity" warnings); at
  23:25 "Connected and filling" and, 7 s later, "There is no fuel in this vehicle".

### Test (changing nothing)
1. Fill up a car and look at the gauge. Go far away (beyond the view distance, or `/tp` far and back) or die and come
   back. Fill up or drive again. Expected by the hypothesis: the gauge stops changing and "no fuel" may appear with fuel.
2. Leave to the menu and enter again: expected that the gauge shows the right value again.

### Effects of AMC Vehicles itself (our suspects)
- **Cars at the gas stations generated by 0.6.0** (before 0.6.1) stay in the world and "steal" the pump (the nearest one).
- **Cars generated up to the first 0.6.0** have a tank with mod `""`: they refuse a bucket.
- **Lava at 0.35**: the consumption with lava is ~3× larger than IV's normal (the gauge drops faster).
- To tell them apart: a round **without `amcvehicles`** in the `mods` folder, with a car from IV's item and a pump placed
  by hand, only in survival.

## 3. Questions to the user
- What was put in the pump before 23:26:59 ("This pump does not contain the same fluid... lava is in the vehicle")?
- Which car were you trying to start at 23:25 and at 23:26 (an abandoned one of the mod or one from the item, with an
  engine) and which one was nearest to the pump?
- The sound "conflict": do vanilla sounds vanish, come out wrong, or do cars play strange sounds? Does it happen in other
  sessions, and did those coincide with Sunshine or with a change of audio output?
- The gauge: what exactly does it show (still, zeroed, full) and does it go back to normal when leaving and entering the
  world?
