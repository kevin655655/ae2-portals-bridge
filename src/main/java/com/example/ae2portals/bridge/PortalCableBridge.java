package com.example.ae2portals.bridge;

import com.example.ae2portals.Ae2PortalsBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates the visual + functional bridge between AE2 cables and Immersive
 * Portals portals. This is the single entry point for everything that needs
 * to happen on either side of a portal:
 *
 * <ul>
 *   <li>Discover portals that have a cable adjacent on either side.</li>
 *   <li>For each such portal, re-emit the cable geometry on the far side
 *       using AE2's own {@code CableBuilder} so texture, channel glow, and
 *       connection directions all match the real cable.</li>
 *   <li>For the functional layer, hand the cable pair to the grid-adjacency
 *       hook so AE2 treats them as a single network.</li>
 * </ul>
 *
 * The two halves of this — visual and functional — are independent and can
 * be enabled/disabled separately in {@code fabric.mod.json} if needed.
 */
public final class PortalCableBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortalCableBridge.class);

    private PortalCableBridge() {}

    /**
     * Called from {@link Ae2PortalsBridge#onInitialize()}. Looks up the
     * Immersive Portals registration entry point and registers our render
     * callback. If Immersive Portals isn't loaded this is a no-op (the mod
     * shouldn't be installed without it, but the guard is here for dev).
     */
    public static void init() {
        // TODO Phase 1: register PortalRenderCallback with Immersive Portals
        //   forEachPortal(portal -> emitCableGeometry(portal))
        //
        // TODO Phase 2: Mixin into CablePart.updateConnections() to detect
        //   portal-adjacent cables and add remote-cable grid nodes.
        //
        // TODO Phase 3: handle cross-dimension portals via AE2's P2P tunnel
        //   mechanism.
        LOGGER.debug("[{}] PortalCableBridge.init() (no-op until Phase 1)", Ae2PortalsBridge.MOD_ID);
    }
}
