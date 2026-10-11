package com.example.ae2portals;

import com.example.ae2portals.bridge.PortalCableBridge;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the AE2 Portals Bridge mod.
 *
 * <p>Phase plan:
 * <ol>
 *   <li><b>Visual layer</b> — render AE2 cables through Immersive Portals by hooking
 *       the portal render pass and submitting cable geometry with the portal's
 *       transformation matrix applied.</li>
 *   <li><b>Functional layer (same-dimension)</b> — Mixin into
 *       {@code appeng.parts.networking.CablePart} to detect portal-adjacent
 *       cables and add the cable on the other side as a grid connection.</li>
 *   <li><b>Functional layer (cross-dimension)</b> — when the portal leads to a
 *       different Level, use AE2's existing P2P tunnel mechanism internally
 *       to bridge the grids across dimensions.</li>
 * </ol>
 */
public class Ae2PortalsBridge implements ModInitializer {
    public static final String MOD_ID = "ae2_portals_bridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] Initializing AE2 Portals Bridge (v{})", MOD_ID, /* version */ "0.1.0-dev");
        PortalCableBridge.init();
        LOGGER.info("[{}] AE2 Portals Bridge ready", MOD_ID);
    }
}
