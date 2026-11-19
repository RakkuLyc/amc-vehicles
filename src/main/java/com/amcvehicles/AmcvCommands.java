package com.amcvehicles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * /amcv (op 2), test commands.
 * <ul>
 *   <li>{@code /amcv car <model> [color] [hood trunk]}: one abandoned car of {@link Cars} in front of you (its origin,
 *   the rear axle, 3 blocks ahead), its nose pointing the way you look (snapped to north, south, east or west); what is
 *   not given is drawn (the colour among the civilian ones; the doors always).</li>
 *   <li>{@code /amcv lineup}: every model side by side in front of you, a civilian colour drawn, then every police
 *   colour (to look at heights, doors and dashboards).</li>
 *   <li>{@code /amcv fill}: a car in every bay of the AMC structure you stand in (the debug of the bays); model, colour,
 *   hood, trunk and doors drawn, the police cars in the police station; a bay where the car does not fit in the world
 *   is skipped and listed.</li>
 *   <li>{@code /amcv draw}: the real draw (9% per bay, {@link Draw}) applied to the AMC structure you stand in: the
 *   cars the generation gives that copy, placed now unless the bay is blocked or occupied (in a chunk generated with
 *   this mod they are already there). For structures of areas generated before the mod, and to test.</li>
 *   <li>{@code /amcv pump [buckets]}: a fuel pump 3 blocks ahead, its front to you, with that many buckets of lava.</li>
 *   <li>{@code fill} and {@code draw} also place the fuel pumps of the structure (fill: the lava drawn now; draw: the
 *   lava of the generation).</li>
 * </ul>
 */
final class AmcvCommands {
    /** Distance from the player to the rear axle of the car, along the look. */
    static final int AHEAD = 3;
    /** Distance between the cars of {@code /amcv lineup}. */
    static final int LINEUP_GAP = 4;

    private AmcvCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        List<String> models = Cars.ALL.stream().map(Cars.Model::key).toList();
        d.register(Commands.literal("amcv").requires(s -> s.hasPermission(2))
                .then(Commands.literal("car")
                        .then(Commands.argument("model", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(models, b))
                                .executes(c -> car(c, null, null, null))
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(colors(c), b))
                                        .executes(c -> car(c, StringArgumentType.getString(c, "color"), null, null))
                                        .then(Commands.argument("hood", BoolArgumentType.bool())
                                                .then(Commands.argument("trunk", BoolArgumentType.bool())
                                                        .executes(c -> car(c, StringArgumentType.getString(c, "color"),
                                                                BoolArgumentType.getBool(c, "hood"),
                                                                BoolArgumentType.getBool(c, "trunk"))))))))
                .then(Commands.literal("lineup").executes(AmcvCommands::lineup))
                .then(Commands.literal("fill").executes(AmcvCommands::fill))
                .then(Commands.literal("draw").executes(AmcvCommands::draw))
                .then(Commands.literal("pump")
                        .executes(c -> pump(c, 0))
                        .then(Commands.argument("buckets", IntegerArgumentType.integer(0, 15))
                                .executes(c -> pump(c, IntegerArgumentType.getInteger(c, "buckets"))))));
    }

    /** The colours of the model typed (without the leading _). */
    private static List<String> colors(CommandContext<CommandSourceStack> c) {
        Cars.Model m = Cars.byKey(StringArgumentType.getString(c, "model"));
        List<String> out = new ArrayList<>();
        if (m != null) {
            m.civilian().forEach(v -> out.add(v.substring(1)));
            m.police().forEach(v -> out.add(v.substring(1)));
        }
        return out;
    }

    /** The look of the source snapped to a quarter turn: 0 south, 90 west, 180 north, 270 east. */
    private static float snappedYaw(CommandSourceStack src) {
        return (Mth.floor(Mth.wrapDegrees(src.getRotation().y) / 90.0 + 0.5) & 3) * 90f;
    }

    private static String facing(float yaw) {
        return switch ((int) yaw) {
            case 0 -> "south";
            case 90 -> "west";
            case 180 -> "north";
            default -> "east";
        };
    }

    private static String describe(Vehicles.Result res) {
        Draw.Car d = res.car();
        Cars.Model m = d.model();
        List<String> doors = new ArrayList<>();
        for (int k = 0; k < m.doors().size(); k++) {
            doors.add(m.doors().get(k) + (d.doorsOpen().get(k) ? " open" : " closed"));
        }
        return String.format("%s%s: hood %s, %s, %s; fuel %.0f/%.0f mB (%s); speedometer slot %d, fuel gauge slot %d; "
                        + "removed %s; parts %s", m.key(), d.color(), d.hoodOpen() ? "open" : "closed",
                m.trunk() == null ? "no trunk" : m.trunk() + (d.trunkOpen() ? " open" : " closed"),
                String.join(", ", doors), res.fuel(), res.fuelCapacity(),
                res.fluid().isEmpty() ? "no fluid" : res.fluid(), res.speedometerSlot(), res.fuelGaugeSlot(),
                res.removed(), res.parts());
    }

    private static int car(CommandContext<CommandSourceStack> c, String color, Boolean hood, Boolean trunk) {
        CommandSourceStack src = c.getSource();
        Cars.Model m = Cars.byKey(StringArgumentType.getString(c, "model"));
        if (m == null) {
            src.sendFailure(Component.literal("Unknown model; one of " + Cars.ALL.stream().map(Cars.Model::key)
                    .toList()));
            return 0;
        }
        Random r = new Random(src.getLevel().getRandom().nextLong());
        Draw.Car d = Draw.car(what -> r.nextDouble(), m, false);
        if (color != null) {
            String sub = "_" + color;
            if (!m.civilian().contains(sub) && !m.police().contains(sub)) {
                src.sendFailure(Component.literal(m.key() + " has no colour " + color));
                return 0;
            }
            d = new Draw.Car(m, sub, d.hoodOpen(), d.trunkOpen(), d.doorsOpen());
        }
        if (hood != null) {
            d = new Draw.Car(m, d.color(), hood, m.trunk() != null && trunk, d.doorsOpen());
        }
        float yaw = snappedYaw(src);
        double x = Math.floor(src.getPosition().x) + 0.5 - Math.sin(Math.toRadians(yaw)) * AHEAD;
        double z = Math.floor(src.getPosition().z) + 0.5 + Math.cos(Math.toRadians(yaw)) * AHEAD;
        // on its support blocks, like the cars of the bays
        double y = Math.floor(src.getPosition().y) + m.lift();
        try {
            Vehicles.Result res = Vehicles.spawn(src.getLevel(), x, y, z, yaw, d);
            src.sendSuccess(() -> Component.literal(String.format("at %.1f %.2f %.1f, nose to the %s; ", x, y, z,
                    facing(yaw)) + describe(res)), true);
            return 1;
        } catch (RuntimeException e) {
            AmcVehicles.LOG.error("Could not spawn the car", e);
            src.sendFailure(Component.literal("Could not spawn the car: " + e));
            return 0;
        }
    }

    private static int lineup(CommandContext<CommandSourceStack> c) {
        CommandSourceStack src = c.getSource();
        Random r = new Random(src.getLevel().getRandom().nextLong());
        List<Draw.Car> cars = new ArrayList<>();
        for (Cars.Model m : Cars.ALL) {
            cars.add(Draw.car(what -> r.nextDouble(), m, false));
        }
        for (Cars.Model m : Cars.POLICE) {
            for (String p : m.police()) {
                Draw.Car d = Draw.car(what -> r.nextDouble(), m, true);
                cars.add(new Draw.Car(m, p, d.hoodOpen(), d.trunkOpen(), d.doorsOpen()));
            }
        }
        float yaw = snappedYaw(src);
        double fx = -Math.sin(Math.toRadians(yaw));
        double fz = Math.cos(Math.toRadians(yaw));
        // the row runs to the right of the look
        double rx = -fz;
        double rz = fx;
        int n = 0;
        List<String> lines = new ArrayList<>();
        for (int k = 0; k < cars.size(); k++) {
            Draw.Car d = cars.get(k);
            double x = Math.floor(src.getPosition().x) + 0.5 + fx * AHEAD + rx * LINEUP_GAP * k;
            double z = Math.floor(src.getPosition().z) + 0.5 + fz * AHEAD + rz * LINEUP_GAP * k;
            double y = Math.floor(src.getPosition().y) + d.model().lift();
            try {
                Vehicles.Result res = Vehicles.spawn(src.getLevel(), x, y, z, yaw, d);
                lines.add(String.format("%d at %.1f %.1f: ", k + 1, x, z) + describe(res));
                n++;
            } catch (RuntimeException e) {
                AmcVehicles.LOG.error("Could not spawn {}{}", d.model().key(), d.color(), e);
                lines.add((k + 1) + ": " + d.model().key() + d.color() + " failed: " + e);
            }
        }
        int placed = n;
        src.sendSuccess(() -> Component.literal(placed + " cars, nose to the " + facing(yaw) + "\n"
                + String.join("\n", lines)), true);
        return placed;
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
        if (bays.isEmpty() && Spots.pumps(s.def().model()).isEmpty()) {
            src.sendFailure(Component.literal(name + " has no bays yet."));
            return 0;
        }
        Random r = new Random(level.getRandom().nextLong());
        int spawned = 0;
        List<String> skipped = new ArrayList<>();
        List<String> models = new ArrayList<>();
        for (int k = 0; k < bays.size(); k++) {
            Draw.Car d = Draw.car(what -> r.nextDouble(), s.police());
            Placement.Car car = s.car(bays.get(k), d.model());
            String why = AmcWorld.blocked(level, car);
            if (why != null) {
                skipped.add("bay " + (k + 1) + " (" + d.model().key() + "): " + why);
                continue;
            }
            try {
                Vehicles.spawn(level, car.x(), car.y(), car.z(), car.noseYaw(), d);
                models.add(d.model().key() + d.color());
                spawned++;
            } catch (RuntimeException e) {
                AmcVehicles.LOG.error("Could not spawn the car of bay {} of {}", k + 1, name, e);
                skipped.add("bay " + (k + 1) + ": " + e);
            }
        }
        // the fuel pumps too (their lava drawn now, with the chance of the generation)
        List<String> pumps = new ArrayList<>();
        for (Generation.PlannedPump p : Generation.pumps(s, null, null)) {
            int buckets = Draw.buckets(r.nextDouble(), r.nextDouble());
            String res = Generation.placePump(level, new Generation.PlannedPump(s, p.pump(), p.at(), buckets),
                    "/amcv fill");
            pumps.add((p.pump() + 1) + ": " + (res.equals("placed") ? buckets + " bucket(s)" : res));
        }
        int n = spawned;
        src.sendSuccess(() -> Component.literal(String.format("%s (box corner %d %d, rotation %d, ground y %d): %d bays, "
                        + "%d cars %s, %d skipped%s%s", name, s.x0(), s.z0(), s.rot(), s.base(), bays.size(), n, models,
                skipped.size(), skipped.isEmpty() ? "" : "\n" + String.join("\n", skipped),
                pumps.isEmpty() ? "" : "\nfuel pumps: " + String.join("; ", pumps))), true);
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
        if (bays == 0 && Spots.pumps(s.def().model()).isEmpty()) {
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
            Set<String> chunks = new TreeSet<>();
            for (int[] cell : p.car().cells()) {
                chunks.add((cell[0] >> 4) + "," + (cell[1] >> 4));
            }
            lines.add(String.format("bay %d: %s%s at %.2f %d %.2f (chunks %s), %s", p.bay() + 1,
                    p.drawn().model().key(), p.drawn().color(), p.car().x(), (int) Math.floor(p.car().y()),
                    p.car().z(), String.join(" ", chunks), r));
        }
        for (Generation.PlannedPump p : Generation.pumps(s, null, null)) {
            String r = Generation.placePump(level, p, "/amcv draw");
            lines.add(String.format("fuel pump %d at %d %d %d (chunk %d,%d), %d bucket(s): %s", p.pump() + 1, p.at().x(),
                    p.at().y(), p.at().z(), p.at().x() >> 4, p.at().z() >> 4, p.buckets(), r));
        }
        int n = placed;
        src.sendSuccess(() -> Component.literal(String.format("%s (seed %d): %d bays, %d drawn (%.0f%% each), %d "
                        + "placed now; pending chunks %d%s", name, s.seed(), bays, drawn.size(),
                Draw.CHANCE_PER_BAY * 100, n, Pending.of(level).size(),
                lines.isEmpty() ? "" : "\n" + String.join("\n", lines))), true);
        return n;
    }

    private static int pump(CommandContext<CommandSourceStack> c, int buckets) {
        CommandSourceStack src = c.getSource();
        float yaw = snappedYaw(src);
        int dx = (int) Math.round(-Math.sin(Math.toRadians(yaw)));
        int dz = (int) Math.round(Math.cos(Math.toRadians(yaw)));
        BlockPos at = BlockPos.containing(src.getPosition()).offset(dx * AHEAD, 0, dz * AHEAD);
        // the front of the pump looks back at you
        String why = FuelPumps.place(src.getLevel(), at, (yaw + 180f) % 360f, buckets * FuelPumps.BUCKET);
        if (why != null) {
            src.sendFailure(Component.literal("Could not place the fuel pump: " + why));
            return 0;
        }
        src.sendSuccess(() -> Component.literal(String.format("Fuel pump at %d %d %d, %d mB of lava", at.getX(),
                at.getY(), at.getZ(), buckets * FuelPumps.BUCKET)), true);
        return 1;
    }
}
