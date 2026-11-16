package com.amcvehicles;

import minecrafttransportsimulator.systems.ConfigSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lava as a fuel is weaker (the user's choice, 16 Nov 2026): the user measured that 5 buckets of lava took a car
 * across 6 or 7 AMC cities (~1.3 cities per bucket), and lava is endless in the world (any lava lake). The potency of
 * lava in IV's fuel config ({@code fuel.fuels.<type>.lava}, 1.0 by default for gasoline, diesel and avgas) is set to
 * {@link #LAVA}: each bucket goes about a third as far (~0.45 city). A real gasoline of another mod keeps its potency.
 * <p>
 * Only in memory, never in IV's {@code config/mtsconfig.json}: IV writes that file while the game loads (on the client,
 * in {@code FMLLoadCompleteEvent}), so the value is set later, when a server starts (the single player world included:
 * the integrated server shares IV's settings) and, on a client, when it joins a server. Taking the mod out restores
 * IV's value. IV's engines read the map every time they burn fuel ({@code PartEngine}), so the change applies at once.
 */
public final class FuelBalance {
    /** Potency of lava as an engine fuel. */
    public static final double LAVA = 0.35;
    /**
     * IV's fuel types that are not engine fuels and keep lava as it is: its furnace and brewing stand parts (lava 1.0
     * there too; seen in the log of the first run of 0.6.0, which changed them as well).
     */
    static final java.util.Set<String> NOT_ENGINES = java.util.Set.of("furnace", "brewing_stand");

    private FuelBalance() {
    }

    /** Sets lava to {@link #LAVA} in every engine fuel type of IV's config that takes it; logs what changed. */
    static void apply(String when) {
        if (ConfigSystem.settings == null || ConfigSystem.settings.fuel == null
                || ConfigSystem.settings.fuel.fuels == null) {
            AmcVehicles.LOG.warn("[AMCV] IV's fuel config is not loaded ({}): lava keeps its potency", when);
            return;
        }
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> e : ConfigSystem.settings.fuel.fuels.entrySet()) {
            if (NOT_ENGINES.contains(e.getKey())) {
                continue;
            }
            Double was = e.getValue().get("lava");
            if (was != null && was != LAVA) {
                e.getValue().put("lava", LAVA);
                changed.add(e.getKey() + " " + was);
            }
        }
        if (!changed.isEmpty()) {
            AmcVehicles.LOG.info("[AMCV] Lava as a fuel set to {} ({}; was: {}); IV's config file is not changed", LAVA,
                    when, String.join(", ", changed));
        }
    }
}
