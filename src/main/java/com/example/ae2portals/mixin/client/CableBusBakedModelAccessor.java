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
 * <p>Uses raw {@code LoadingCache} (without the value type parameter)
 * to avoid hard-importing {@code BakedQuad}, which Loom hasn't been able
 * to resolve in this dev environment. The cast happens at the call site.
 */
@Mixin(CableBusBakedModel.class)
public abstract class CableBusBakedModelAccessor {

    @Shadow
    private LoadingCache cableModelCache;

    /**
     * Returns the cached cable quad list for the given render state.
     */
    public List<?> ae2portals$getCachedCableQuads(CableBusRenderState renderState) {
        return (List<?>) cableModelCache.getUnchecked(renderState);
    }
}
