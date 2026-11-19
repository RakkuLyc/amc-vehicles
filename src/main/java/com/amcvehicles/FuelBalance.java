package com.amcvehicles;

import minecrafttransportsimulator.systems.ConfigSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lava as a fuel is weaker (the user's choice, 2026-11-16): the user measured that 5 buckets of lava took a car
 * across 6 or 7 AMC cities (~1.3 cities per bucket), and lava is endless in the world (any lava lake). The potency of
 * lava in IV's fuel config ({@code fuel.fuels.<type>.lava}, 1.0 by default for gasoline, diesel and avgas) is set to
 * {@link #LAVA}: each bucket goes 5% as far as IV's own (~0.07 city per bucket by the user's measure of ~1.3). A
 * real gasoline of another mod keeps its potency.
 * <p>
 * In memory: IV writes {@code config/mtsconfig.json} while the game loads (on the client, in
 * {@code FMLLoadCompleteEvent}), so the value is set later, when a server starts (the single player world included: the
 * integrated server shares IV's settings) and, on a client, when it joins a server, and put back when the server stops
 * or the client leaves ({@link #restore}). IV also saves that file when a player changes its controls or its config
 * screen during a game: such a save writes 0.05 to the file (to undo it, edit the file). IV's engines read the map
 * every time they burn fuel ({@code PartEngine}), so the change applies at once.
 */
public final class FuelBalance {
    /**
     * Potency of lava as an engine fuel: 0.05 since 0.7.12 (the user's choice after testing it, 2026-11-17). Before:
     * 0.35 in 0.6.0 to 0.7.4, none in 0.7.5 to 0.7.8, 0.60 in 0.7.9, 0.45 in 0.7.10, 0.35 in 0.7.11.
     */
    public static final double LAVA = 0.05;
    /**
     * IV's fuel types that are not engine fuels and keep lava as it is: its furnace and brewing stand parts (lava 1.0
     * there too; seen in the log of the first run of 0.6.0, which changed them as well).
     */
    static final Set<String> NOT_ENGINES = Set.of("furnace", "brewing_stand");

    /** IV's values before {@link #apply}: put back by {@link #restore}. */
    private static final Map<String, Double> ORIGINAL = new HashMap<>();

    private FuelBalance() {
    }

    /**
     * Puts IV's values back (when the server stops and when a client leaves a server): IV also writes its config file
     * while the game runs (its controls and config screens, ControlSystem and GUIConfig, call saveToDisk), and a save
     * made while 0.05 is in memory writes it to the file; putting the values back between sessions keeps that window to
     * the time a world is open.
     */
    static void restore(String when) {
        if (ORIGINAL.isEmpty() || ConfigSystem.settings == null || ConfigSystem.settings.fuel == null) {
            return;
        }
        for (Map.Entry<String, Double> e : ORIGINAL.entrySet()) {
            Map<String, Double> m = ConfigSystem.settings.fuel.fuels.get(e.getKey());
            if (m != null) {
                m.put("lava", e.getValue());
            }
        }
        AmcVehicles.LOG.info("[AMCV] Lava as a fuel put back to IV's values ({}): {}", when, ORIGINAL);
        ORIGINAL.clear();
    }

    /**
     * Before IV writes its config file (ConfigSystemMixin, around {@code ConfigSystem.saveToDisk}): IV's own values
     * back in memory, so the file never gets the mod's potency. {@link #afterIvSave} sets it again.
     */
    public static void beforeIvSave() {
        put(ORIGINAL);
    }

    /** After IV wrote its config file: the mod's potency again in the types it had changed. */
    public static void afterIvSave() {
        Map<String, Double> lava = new HashMap<>();
        ORIGINAL.keySet().forEach(type -> lava.put(type, LAVA));
        put(lava);
    }

    /** Sets lava to the given potency in each fuel type of the map (types missing from IV's config are skipped). */
    private static void put(Map<String, Double> potencyByType) {
        if (potencyByType.isEmpty() || ConfigSystem.settings == null || ConfigSystem.settings.fuel == null
                || ConfigSystem.settings.fuel.fuels == null) {
            return;
        }
        for (Map.Entry<String, Double> e : potencyByType.entrySet()) {
            Map<String, Double> m = ConfigSystem.settings.fuel.fuels.get(e.getKey());
            if (m != null) {
                m.put("lava", e.getValue());
            }
        }
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
                ORIGINAL.putIfAbsent(e.getKey(), was);
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
