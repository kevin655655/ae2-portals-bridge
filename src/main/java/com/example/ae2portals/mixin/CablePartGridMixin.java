package com.example.ae2portals.mixin;

import appeng.parts.networking.CablePart;
import com.example.ae2portals.Ae2PortalsBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Phase 2 functional layer.
 *
 * <p>Mixes into {@code appeng.parts.networking.CablePart} so that, after
 * AE2 has scanned the six in-world neighbors for connections, we add
 * any portal-mediated connections. If a neighbor in direction D is
 * an Immersive Portals portal frame, we look up the block on the far
 * side via IP's API; if that block is also a cable bus, we add the
 * remote cable's grid node to this cable's in-world connections. AE2
 * then treats the two cables as directly adjacent and unifies their
 * grids.
 *
 * <p>For Phase 0 (this commit) the class is a no-op stub. The actual
 * {@code @Mixin(CablePart.class)} annotation and {@code @Inject} on
 * {@code updateConnections} will be added in Phase 2, once the AE2
 * Yarn mappings on the 1.21 branch are confirmed in the local
 * build.
 */
public abstract class CablePartGridMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CablePartGridMixin.class);

    /**
     * Placeholder method — will become a Mixin entry point in Phase 2.
     * Uses Object for the Direction parameter to avoid hard-importing
     * vanilla Minecraft client classes (Loom remap is broken for those
     * in this dev environment).
     */
    @SuppressWarnings("unused")
    private void ae2portals$injectPortalConnections(Object direction) {
        LOGGER.trace("[{}] CablePartGridMixin stub called (no-op until Phase 2)", Ae2PortalsBridge.MOD_ID);
    }
}
