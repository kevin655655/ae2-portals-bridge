package com.example.ae2portals;

import java.lang.reflect.Method;

/**
 * Tiny helper to get the Minecraft singleton via reflection. Lives in the
 * main (non-mixin) package so {@link CrossPortalEntityRendererMixin} can
 * use it without circular dep on the mixin package.
 */
public final class MinecraftReflectionHelpers {
    private MinecraftReflectionHelpers() {}

    private static final Method MC_GET_INSTANCE;

    static {
        try {
            MC_GET_INSTANCE = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance");
        } catch (Exception e) {
            throw new RuntimeException("AE2 Portals Bridge: Minecraft.getInstance not resolvable", e);
        }
    }

    public static Object getMinecraft() {
        try {
            return MC_GET_INSTANCE.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("AE2 Portals Bridge: Minecraft.getInstance() failed", e);
        }
    }
}
