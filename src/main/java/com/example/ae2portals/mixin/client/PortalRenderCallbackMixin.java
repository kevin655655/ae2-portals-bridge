package com.example.ae2portals.mixin.client;

import com.example.ae2portals.Ae2PortalsBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phase 1 hook point into Immersive Portals' portal rendering.
 *
 * <p>Immersive Portals renders the "other side" of a portal as a clipped
 * sub-scene; while doing so, it gives mod authors a chance to render
 * custom geometry that should appear on the far side. When Phase 1 is
 * implemented, this class becomes a real Mixin targeting IP's portal
 * renderer; the {@code @Mixin} annotation will be added at that point
 * once we confirm the exact entry point on the IP {@code 1.21} branch.
 *
 * <p>For Phase 0 (this commit) the class is a no-op stub so the project
 * still builds.
 */
public abstract class PortalRenderCallbackMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortalRenderCallbackMixin.class);

    /**
     * Placeholder method — will become a Mixin entry point in Phase 1.
     * Will be invoked once per portal per frame by the IP render callback.
     */
    @SuppressWarnings("unused")
    private static void ae2portals$renderCablesOnFarSide() {
        LOGGER.trace("[{}] PortalRenderCallbackMixin stub called (no-op until Phase 1)", Ae2PortalsBridge.MOD_ID);
    }
}
