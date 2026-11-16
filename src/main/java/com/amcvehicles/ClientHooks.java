package com.amcvehicles;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client only (loaded only on a client): the lava potency of {@link FuelBalance} when the player joins a server. */
final class ClientHooks {
    private ClientHooks() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn e) -> FuelBalance.apply("joining a server"));
    }
}
