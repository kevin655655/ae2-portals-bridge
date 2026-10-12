package com.example.ae2portals.mixin.client;

import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusRenderState;
import com.google.common.cache.LoadingCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * Exposes AE2's private {@code cableModelCache} so the IP render hook can
 * submit cached cable quads through a portal. The cache is populated by
 * AE2's own chunk bake, so calling {@link LoadingCache#getUnchecked} is
 * cheap after the first bake.
 *
 * <p>{@code remap = false} tells the Mixin processor to skip the Yarn
 * obfuscation lookup and use the field name as-is. The AE2 jar we depend
 * on is built with MCP mappings (not Yarn), so there is no Yarn
 * intermediary entry to look up. The field name in the class file is
 * literally {@code cableModelCache} (verified with {@code strings} on
 * the extracted class), so the Mixin applies cleanly.
 */
@Mixin(CableBusBakedModel.class)
public abstract class CableBusBakedModelAccessor {

    @Shadow(remap = false)
    private LoadingCache cableModelCache;

    /**
     * Returns the cached cable quad list for the given render state.
     */
    @SuppressWarnings("unchecked")
    public List<?> ae2portals$getCachedCableQuads(CableBusRenderState renderState) {
        return (List<?>) cableModelCache.getUnchecked(renderState);
    }
}
