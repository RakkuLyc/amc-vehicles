package com.amcvehicles;

import minecrafttransportsimulator.jsondefs.JSONConfigSettings;
import minecrafttransportsimulator.systems.ConfigSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FuelBalance} with IV's fuel config as plain Java objects: in a single player game the integrated server
 * (server starting / stopped) and the client (joining / leaving) both apply and restore, in either order.
 */
class FuelBalanceTest {
    /** IV's config with lava at {@code lava} in the three engine fuels and in the furnace. */
    static void config(double lava) {
        JSONConfigSettings s = new JSONConfigSettings();
        s.fuel = new JSONConfigSettings.ConfigFuel();
        s.fuel.fuels = new HashMap<>();
        for (String type : new String[]{"gasoline", "diesel", "avgas", "furnace"}) {
            Map<String, Double> m = new HashMap<>();
            m.put("lava", lava);
            s.fuel.fuels.put(type, m);
        }
        ConfigSystem.settings = s;
    }

    static double lava(String type) {
        return ConfigSystem.settings.fuel.fuels.get(type).get("lava");
    }

    @AfterEach
    void forgetOriginals() {
        FuelBalance.restore("test end");
        ConfigSystem.settings = null;
    }

    @Test
    void serverThenClientPutsIvValuesBack() {
        config(1.0);
        FuelBalance.apply("server starting");
        FuelBalance.apply("joining a server");
        assertEquals(FuelBalance.LAVA, lava("gasoline"));
        assertEquals(1.0, lava("furnace"), "the furnace is not an engine fuel");
        FuelBalance.restore("leaving a server");
        FuelBalance.restore("server stopped");
        for (String type : new String[]{"gasoline", "diesel", "avgas", "furnace"}) {
            assertEquals(1.0, lava(type), type);
        }
    }

    @Test
    void clientThenServerPutsIvValuesBack() {
        config(1.0);
        FuelBalance.apply("joining a server");
        FuelBalance.apply("server starting");
        FuelBalance.restore("server stopped");
        FuelBalance.restore("leaving a server");
        for (String type : new String[]{"gasoline", "diesel", "avgas"}) {
            assertEquals(1.0, lava(type), type);
        }
    }

    /**
     * A config that IV already saved with the mod's value (the dirty mtsconfig.json): there is nothing to put back,
     * so the file keeps 0.05 after the mod is gone. Documents the known limit (stage 4 of the 1.0 work looks at it).
     */
    @Test
    void dirtyConfigStaysDirty() {
        config(FuelBalance.LAVA);
        FuelBalance.apply("server starting");
        FuelBalance.restore("server stopped");
        assertEquals(FuelBalance.LAVA, lava("gasoline"));
    }
}
