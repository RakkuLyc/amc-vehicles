package com.amcvehicles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * /amcv (op 2), test commands.
 * <ul>
 *   <li>{@code /amcv mercedes [color] [hood] [trunk]}: one abandoned Mercedes in front of you (rear axle 3 blocks ahead), its nose
 *   pointing the way you look (snapped to north, south, east or west); what is not given is drawn.</li>
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
                .then(Commands.literal("mercedes")
                        .executes(c -> mercedes(c, null, null, null))
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(colors, b))
                                .executes(c -> mercedes(c, StringArgumentType.getString(c, "color"), null, null))
                                .then(Commands.argument("hood", BoolArgumentType.bool())
                                        .then(Commands.argument("trunk", BoolArgumentType.bool())
                                                .executes(c -> mercedes(c, StringArgumentType.getString(c, "color"),
                                                        BoolArgumentType.getBool(c, "hood"),
                                                        BoolArgumentType.getBool(c, "trunk"))))))));
    }

    private static int mercedes(CommandContext<CommandSourceStack> c, String color, Boolean hood, Boolean trunk) {
        CommandSourceStack src = c.getSource();
        // the source's position and look: a player, or "execute positioned ... rotated ... run" from the console
        RandomSource r = src.getLevel().getRandom();
        String sub = color == null ? Mercedes.drawColor(r, false) : "_" + color;
        boolean h = hood != null ? hood : r.nextDouble() < Mercedes.HOOD_OPEN_CHANCE;
        boolean t = trunk != null ? trunk : r.nextDouble() < Mercedes.TRUNK_OPEN_CHANCE;
        // the look snapped to a quarter turn: 0 south, 90 west, 180 north, 270 east
        int quarter = Mth.floor(Mth.wrapDegrees(src.getRotation().y) / 90.0 + 0.5) & 3;
        float yaw = quarter * 90f;
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double x = Math.floor(src.getPosition().x) + 0.5 + dx * AHEAD;
        double z = Math.floor(src.getPosition().z) + 0.5 + dz * AHEAD;
        double y = Math.floor(src.getPosition().y);
        try {
            Mercedes.Result res = Mercedes.spawn(src.getLevel(), x, y, z, yaw, sub, h, t);
            String facing = switch (quarter) {
                case 0 -> "south";
                case 1 -> "west";
                case 2 -> "north";
                default -> "east";
            };
            src.sendSuccess(() -> Component.literal(String.format(
                    "Mercedes%s at %.1f %.1f %.1f, nose to the %s; hood %s, trunk %s; fuel %.0f/%.0f mB (%s); "
                            + "engines removed %d; parts %s",
                    res.color(), x, y, z, facing, res.hoodOpen() ? "open" : "closed",
                    res.trunkOpen() ? "open" : "closed", res.fuel(), res.fuelCapacity(),
                    res.fluid().isEmpty() ? "no fluid" : res.fluid(), res.enginesRemoved(), res.parts())), true);
            return 1;
        } catch (RuntimeException e) {
            AmcVehicles.LOG.error("Could not spawn the Mercedes", e);
            src.sendFailure(Component.literal("Could not spawn the Mercedes: " + e));
            return 0;
        }
    }
}
