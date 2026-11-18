package com.amcvehicles;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

/** AMC Vehicles: abandoned Immersive Vehicles cars in the structures of Abandoned Modern City. */
@Mod(AmcVehicles.MODID)
public final class AmcVehicles {
    public static final String MODID = "amcvehicles";
    public static final Logger LOG = LogUtils.getLogger();

    public AmcVehicles(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> AmcvCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(Generation::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(Generation::onLevelTick);
        NeoForge.EVENT_BUS.addListener(Generation::onServerStopped);
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent e) -> FuelBalance.apply("server starting"));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> FuelBalance.restore("server stopped"));
        if (FMLEnvironment.dist.isClient()) {
            ClientHooks.register();
        }
    }
}
