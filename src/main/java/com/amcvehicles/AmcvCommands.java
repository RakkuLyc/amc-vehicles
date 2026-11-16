package com.amcvehicles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * /amcv (op 2), test commands.
 * <ul>
 *   <li>{@code /amcv mercedes [color] [hood trunk [doorLeft doorRight]]}: one abandoned Mercedes in front of you
 *   (rear axle 3 blocks ahead), its nose pointing the way you look (snapped to north, south, east or west); what is not given is drawn.</li>
 *   <li>{@code /amcv fill}: a Mercedes in every bay of the AMC structure you stand in (the debug of the bays); colour,
 *   hood, trunk and doors drawn, the police colours in the police station; a bay where the car does not fit in the world
 *   is skipped and listed.</li>
 *   <li>{@code /amcv draw}: the real draw (3% per bay, {@link Draw}) applied to the AMC structure you stand in: the
 *   cars the generation gives that copy, placed now unless the bay is blocked or occupied (in a chunk generated with
 *   this mod they are already there). For structures of areas generated before the mod, and to test.</li>
 * </ul>
 */
final class AmcvCommands {
    /** Distance from the player to the rear axle of the car, along the look. */
    static final int AHEAD = 3;

    private AmcvCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        List<String> colors = new ArrayList<>();
        for (String c : Mercedes.CIVILIAN) {
            colors.add(c.substring(1));
        }
        for (String c : Mercedes.POLICE) {
            colors.add(c.substring(1));
        }
        d.register(Commands.literal("amcv").requires(s -> s.hasPermission(2))
                .then(Commands.literal("fill").executes(AmcvCommands::fill))
                .then(Commands.literal("draw").executes(AmcvCommands::draw))
                .then(Commands.literal("mercedes")
                        .executes(c -> mercedes(c, null, null, null, null, null))
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(colors, b))
                                .executes(c -> mercedes(c, StringArgumentType.getString(c, "color"), null, null, null,
                                        null))
                                .then(Commands.argument("hood", BoolArgumentType.bool())
                                        .then(Commands.argument("trunk", BoolArgumentType.bool())
                                                .executes(c -> mercedes(c, StringArgumentType.getString(c, "color"),
                                                        BoolArgumentType.getBool(c, "hood"),
                                                        BoolArgumentType.getBool(c, "trunk"), null, null))
                                                .then(Commands.argument("doorLeft", BoolArgumentType.bool())
                                                        .then(Commands.argument("doorRight", BoolArgumentType.bool())
                                                                .executes(c -> mercedes(c,
                                                                        StringArgumentType.getString(c, "color"),
                                                                        BoolArgumentType.getBool(c, "hood"),
                                                                        BoolArgumentType.getBool(c, "trunk"),
                                                                        BoolArgumentType.getBool(c, "doorLeft"),
                                                                        BoolArgumentType.getBool(c, "doorRight"))))))))));
    }

    private static int mercedes(CommandContext<CommandSourceStack> c, String color, Boolean hood, Boolean trunk,
                                Boolean doorLeft, Boolean doorRight) {
        CommandSourceStack src = c.getSource();
        // the source's position and look: a player, or "execute positioned ... rotated ... run" from the console
        RandomSource r = src.getLevel().getRandom();
        String sub = color == null ? Mercedes.drawColor(r, false) : "_" + color;
        boolean h = hood != null ? hood : r.nextDouble() < Mercedes.HOOD_OPEN_CHANCE;
        boolean t = trunk != null ? trunk : r.nextDouble() < Mercedes.TRUNK_OPEN_CHANCE;
        boolean dl = doorLeft != null ? doorLeft : r.nextDouble() < Mercedes.DOOR_OPEN_CHANCE;
        boolean dr = doorRight != null ? doorRight : r.nextDouble() < Mercedes.DOOR_OPEN_CHANCE;
        // the look snapped to a quarter turn: 0 south, 90 west, 180 north, 270 east
        int quarter = Mth.floor(Mth.wrapDegrees(src.getRotation().y) / 90.0 + 0.5) & 3;
        float yaw = quarter * 90f;
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double x = Math.floor(src.getPosition().x) + 0.5 + dx * AHEAD;
        double z = Math.floor(src.getPosition().z) + 0.5 + dz * AHEAD;
        double y = Math.floor(src.getPosition().y);
        try {
            Mercedes.Result res = Mercedes.spawn(src.getLevel(), x, y, z, yaw, sub, h, t, dl, dr);
            String facing = switch (quarter) {
                case 0 -> "south";
                case 1 -> "west";
                case 2 -> "north";
                default -> "east";
            };
            src.sendSuccess(() -> Component.literal(String.format(
                    "Mercedes%s at %.1f %.1f %.1f, nose to the %s; hood %s, trunk %s, left door %s, right door %s; fuel %.0f/%.0f mB (%s); "
                            + "engines removed %d; parts %s",
                    res.color(), x, y, z, facing, res.hoodOpen() ? "open" : "closed",
                    res.trunkOpen() ? "open" : "closed", res.doorLeftOpen() ? "open" : "closed",
                    res.doorRightOpen() ? "open" : "closed", res.fuel(), res.fuelCapacity(),
                    res.fluid().isEmpty() ? "no fluid" : res.fluid(), res.enginesRemoved(), res.parts())), true);
            return 1;
        } catch (RuntimeException e) {
            AmcVehicles.LOG.error("Could not spawn the Mercedes", e);
            src.sendFailure(Component.literal("Could not spawn the Mercedes: " + e));
            return 0;
        }
    }

    private static int fill(CommandContext<CommandSourceStack> c) {
        CommandSourceStack src = c.getSource();
        ServerLevel level = src.getLevel();
        BlockPos at = BlockPos.containing(src.getPosition());
        AmcWorld.Placed s = AmcWorld.at(level, at.getX(), at.getZ());
        if (s == null) {
            src.sendFailure(Component.literal("Not in an AMC structure (stand inside its lot)."));
            return 0;
        }
        String name = s.def().name();
        List<Spots.Bay> bays = Spots.of(s.def().model());
        if (bays.isEmpty()) {
            src.sendFailure(Component.literal(name + " has no bays yet."));
            return 0;
        }
        RandomSource r = level.getRandom();
        int spawned = 0;
        List<String> skipped = new ArrayList<>();
        for (int k = 0; k < bays.size(); k++) {
            Placement.Car car = s.car(bays.get(k));
            String why = AmcWorld.blocked(level, car);
            if (why != null) {
                skipped.add("bay " + (k + 1) + ": " + why);
                continue;
            }
            try {
                Mercedes.spawn(level, car.x(), car.y(), car.z(), car.noseYaw(), Mercedes.drawColor(r, s.police()),
                        r.nextDouble() < Mercedes.HOOD_OPEN_CHANCE, r.nextDouble() < Mercedes.TRUNK_OPEN_CHANCE,
                        r.nextDouble() < Mercedes.DOOR_OPEN_CHANCE, r.nextDouble() < Mercedes.DOOR_OPEN_CHANCE);
                spawned++;
            } catch (RuntimeException e) {
                AmcVehicles.LOG.error("Could not spawn the Mercedes of bay {} of {}", k + 1, name, e);
                skipped.add("bay " + (k + 1) + ": " + e);
            }
        }
        int n = spawned;
        src.sendSuccess(() -> Component.literal(String.format("%s (box corner %d %d, rotation %d, ground y %d): %d bays, "
                + "%d cars, %d skipped%s", name, s.x0(), s.z0(), s.rot(), s.base(), bays.size(), n, skipped.size(),
                skipped.isEmpty() ? "" : "\n" + String.join("\n", skipped))), true);
        for (String k : skipped) {
            AmcVehicles.LOG.info("[amcv fill] {} {}", name, k);
        }
        return n;
    }

    private static int draw(CommandContext<CommandSourceStack> c) {
        CommandSourceStack src = c.getSource();
        ServerLevel level = src.getLevel();
        BlockPos at = BlockPos.containing(src.getPosition());
        AmcWorld.Placed s = AmcWorld.at(level, at.getX(), at.getZ());
        if (s == null) {
            src.sendFailure(Component.literal("Not in an AMC structure (stand inside its lot)."));
            return 0;
        }
        String name = s.def().name();
        int bays = Spots.of(s.def().model()).size();
        if (bays == 0) {
            src.sendFailure(Component.literal(name + " has no bays yet."));
            return 0;
        }
        List<String> lines = new ArrayList<>();
        int placed = 0;
        List<Generation.Planned> drawn = Generation.drawn(s, null, null);
        for (Generation.Planned p : drawn) {
            String r = Generation.place(level, p, "/amcv draw");
            placed += r.equals("placed") ? 1 : 0;
            // the chunks under the body (the car waits until all of them are loaded full chunks)
            java.util.Set<String> chunks = new java.util.TreeSet<>();
            for (int[] cell : p.car().cells()) {
                chunks.add((cell[0] >> 4) + "," + (cell[1] >> 4));
            }
            lines.add(String.format("bay %d: Mercedes%s at %.2f %d %.2f (chunks %s), %s", p.bay() + 1,
                    p.drawn().color(), p.car().x(), (int) Math.floor(p.car().y()), p.car().z(),
                    String.join(" ", chunks), r));
        }
        int n = placed;
        src.sendSuccess(() -> Component.literal(String.format("%s (seed %d): %d bays, %d drawn (%.0f%% each), %d "
                + "placed now; pending chunks %d, queued %d%s", name, s.seed(), bays, drawn.size(),
                Draw.CHANCE_PER_BAY * 100, n, Pending.of(level).size(), Generation.queued(),
                lines.isEmpty() ? "" : "\n" + String.join("\n", lines))), true);
        return n;
    }
}
