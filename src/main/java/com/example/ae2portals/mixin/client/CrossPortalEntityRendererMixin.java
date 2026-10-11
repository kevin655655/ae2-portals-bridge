package com.example.ae2portals.mixin.client;

import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusRenderState;
import com.example.ae2portals.Ae2PortalsBridge;
import com.example.ae2portals.mixin.client.CableBusBakedModelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.CrossPortalEntityRenderer;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Mixes into IP's per-portal render completion hook so that AE2 cable quads
 * adjacent to the active portal get re-submitted with the portal's
 * transformation applied.
 *
 * <p>All vanilla Minecraft client types are accessed via reflection because
 * Fabric Loom 1.8.13 + Yarn 1.21.1+build.3 isn't remapping the
 * {@code net.minecraft.client.*}, {@code com.mojang.*}, and
 * {@code net.neoforged.*} packages in this dev environment. The Yarn
 * mapping file has the entries but the remap step isn't picking them up.
 * Once that's fixed, this file can be rewritten with direct imports.
 */
@Mixin(CrossPortalEntityRenderer.class)
public class CrossPortalEntityRendererMixin {

    // ---- cached class lookups (resolved once when the class loads) ----
    private static final Class<?> MC;
    private static final Class<?> MODEL_DATA;
    private static final Class<?> BLOCK_STATE;
    private static final Class<?> BLOCK_RENDERER;
    private static final Class<?> LEVEL_CHUNK;
    private static final Class<?> BAKED_QUAD;
    private static final Class<?> BUFFER_SOURCE;
    private static final Class<?> CABLE_BUS_RENDER_STATE;

    // ---- cached method/field handles ----
    private static final Method MC_GET_INSTANCE;
    private static final Method MC_LEVEL;
    private static final Method MC_RENDER_BUFFERS;
    private static final Method MC_BLOCK_RENDERER;
    private static final Method MC_OPTIONS;
    private static final Method CLIENT_LEVEL_HAS_CHUNK;
    private static final Method CLIENT_LEVEL_CHUNK_SOURCE;
    private static final Method LEVEL_GET_SPAWN_POS;
    private static final Method CHUNK_SOURCE_GET_CHUNK;
    private static final Method CHUNK_GET_BLOCK_ENTITIES;
    private static final Method BLOCK_ENTITY_GET_BLOCK_STATE;
    private static final Method BLOCK_ENTITY_GET_MODEL_DATA;
    private static final Method MODEL_DATA_GET;
    private static final Method BLOCK_RENDERER_GET_BLOCK_MODEL;
    private static final Method POSE_STACK_PUSH;
    private static final Method POSE_STACK_POP;
    private static final Method POSE_STACK_TRANSLATE;
    private static final Method POSE_STACK_SCALE;
    private static final Method POSE_STACK_MUL_POSE;
    private static final Method BUFFER_SOURCE_END_BATCH;
    private static final Method BUFFER_SOURCE_PUT_BULK_DATA;
    private static final Method BAKED_QUAD_GET_POSE;
    private static final Field CABLE_BUS_RENDER_STATE_PROPERTY;

    static {
        try {
            MC = Class.forName("net.minecraft.client.Minecraft");
            MODEL_DATA = Class.forName("net.neoforged.neoforge.client.model.data.ModelData");
            BLOCK_STATE = Class.forName("net.minecraft.world.level.block.state.BlockState");
            BLOCK_RENDERER = Class.forName("net.minecraft.client.renderer.block.BlockRenderer");
            LEVEL_CHUNK = Class.forName("net.minecraft.world.level.chunk.LevelChunk");
            BAKED_QUAD = Class.forName("net.minecraft.client.renderer.block.model.BakedQuad");
            BUFFER_SOURCE = Class.forName("net.minecraft.client.renderer.MultiBufferSource$BufferSource");
            CABLE_BUS_RENDER_STATE = Class.forName("appeng.client.render.cablebus.CableBusRenderState");

            MC_GET_INSTANCE = MC.getMethod("getInstance");
            MC_LEVEL = MC.getMethod("level");
            MC_RENDER_BUFFERS = MC.getMethod("renderBuffers");
            MC_BLOCK_RENDERER = MC.getMethod("getBlockRenderer");
            MC_OPTIONS = MC.getMethod("options");

            Class<?> clientLevel = Class.forName("net.minecraft.client.multiplayer.ClientLevel");
            CLIENT_LEVEL_HAS_CHUNK = clientLevel.getMethod("hasChunk", int.class, int.class);
            CLIENT_LEVEL_CHUNK_SOURCE = clientLevel.getMethod("getChunkSource");
            LEVEL_GET_SPAWN_POS = Class.forName("net.minecraft.world.level.Level").getMethod("getSpawnPos");

            Class<?> chunkSource = Class.forName("net.minecraft.world.level.chunk.ChunkSource");
            CHUNK_SOURCE_GET_CHUNK = chunkSource.getMethod("getChunk", int.class, int.class, boolean.class);
            CHUNK_GET_BLOCK_ENTITIES = LEVEL_CHUNK.getMethod("getBlockEntities");

            Class<?> blockEntity = Class.forName("net.minecraft.world.level.block.entity.BlockEntity");
            BLOCK_ENTITY_GET_BLOCK_STATE = blockEntity.getMethod("getBlockState");
            BLOCK_ENTITY_GET_MODEL_DATA = blockEntity.getMethod("getModelData");

            Class<?> modelProperty = Class.forName("net.neoforged.neoforge.client.model.data.ModelProperty");
            MODEL_DATA_GET = MODEL_DATA.getMethod("get", modelProperty);

            BLOCK_RENDERER_GET_BLOCK_MODEL = BLOCK_RENDERER.getMethod("getBlockModel", BLOCK_STATE);

            Class<?> poseStack = Class.forName("com.mojang.blaze3d.vertex.PoseStack");
            POSE_STACK_PUSH = poseStack.getMethod("pushPose");
            POSE_STACK_POP = poseStack.getMethod("popPose");
            POSE_STACK_TRANSLATE = poseStack.getMethod("translate", double.class, double.class, double.class);
            POSE_STACK_SCALE = poseStack.getMethod("scale", float.class, float.class, float.class);
            POSE_STACK_MUL_POSE = poseStack.getMethod("mulPose", Class.forName("com.mojang.blaze3d.vertex.Quaternion"));

            BUFFER_SOURCE_END_BATCH = BUFFER_SOURCE.getMethod("endBatch");
            BUFFER_SOURCE_PUT_BULK_DATA = BUFFER_SOURCE.getMethod("putBulkData", Class.forName("com.mojang.blaze3d.vertex.PoseStack$Pose"), BAKED_QUAD);

            BAKED_QUAD_GET_POSE = BAKED_QUAD.getMethod("getPose");

            // PROPERTY is a Field on CableBusRenderState, not a Method
            CABLE_BUS_RENDER_STATE_PROPERTY = CABLE_BUS_RENDER_STATE.getField("PROPERTY");
        } catch (Exception e) {
            throw new RuntimeException("AE2 Portals Bridge: failed to resolve reflected classes/methods", e);
        }
    }

    @Inject(method = "onEndRenderingEntitiesAndBlockEntities", at = @At("TAIL"))
    private static void ae2portals$renderCableProjections(Object matrixStackObj, CallbackInfo ci) {
        if (!PortalRendering.isRendering()) {
            return;
        }
        Portal portal = PortalRendering.getRenderingPortal();
        if (portal == null) {
            return;
        }

        try {
            Object mc = MC_GET_INSTANCE.invoke(null);
            Object world = MC_LEVEL.invoke(mc);
            if (world == null) {
                return;
            }

            Object renderBuffers = MC_RENDER_BUFFERS.invoke(mc);
            Object consumers = renderBuffers.getClass().getMethod("bufferSource").invoke(renderBuffers);

            // Mirror the logic in CrossPortalEntityRenderer.setupEntityProjectionRenderingTransformation
            POSE_STACK_PUSH.invoke(matrixStackObj);
            try {
                Object origin = portal.getOriginPos();
                double ox = (double) origin.getClass().getField("x").get(origin);
                double oy = (double) origin.getClass().getField("y").get(origin);
                double oz = (double) origin.getClass().getField("z").get(origin);
                POSE_STACK_TRANSLATE.invoke(matrixStackObj, ox, oy, oz);
                double scale = portal.getScaling();
                if (scale != 1.0) {
                    POSE_STACK_SCALE.invoke(matrixStackObj, (float) scale, (float) scale, (float) scale);
                }
                Object rotation = portal.getRotation();
                if (rotation != null) {
                    Object mcQuat = rotation.getClass().getMethod("toMcQuaternion").invoke(rotation);
                    POSE_STACK_MUL_POSE.invoke(matrixStackObj, mcQuat);
                }
                POSE_STACK_TRANSLATE.invoke(matrixStackObj, -ox, -oy, -oz);

                // Get player position via reflection to avoid Nameable/BlockPos type imports
                Object player = MC.getMethod("player").invoke(mc);
                Object playerPos;
                if (player == null) {
                    playerPos = LEVEL_GET_SPAWN_POS.invoke(world);
                } else {
                    playerPos = Class.forName("net.minecraft.world.entity.Entity")
                        .getMethod("blockPosition").invoke(player);
                }
                int pX = (int) playerPos.getClass().getField("x").get(playerPos);
                int pZ = (int) playerPos.getClass().getField("z").get(playerPos);
                int renderDist = (int) MC_OPTIONS.invoke(mc)
                    .getClass().getMethod("getEffectiveRenderDistance")
                    .invoke(MC_OPTIONS.invoke(mc)) * 16;

                int minX = (pX - renderDist) >> 4;
                int maxX = (pX + renderDist) >> 4;
                int minZ = (pZ - renderDist) >> 4;
                int maxZ = (pZ + renderDist) >> 4;

                Object chunkSource = CLIENT_LEVEL_CHUNK_SOURCE.invoke(world);
                for (int cx = minX; cx <= maxX; cx++) {
                    for (int cz = minZ; cz <= maxZ; cz++) {
                        if (!(boolean) CLIENT_LEVEL_HAS_CHUNK.invoke(world, cx, cz)) continue;
                        Object chunk = CHUNK_SOURCE_GET_CHUNK.invoke(chunkSource, cx, cz, false);
                        if (chunk == null) continue;
                        Map<Object, Object> blockEntities = (Map<Object, Object>) CHUNK_GET_BLOCK_ENTITIES.invoke(chunk);
                        for (Object be : blockEntities.values()) {
                            if (!(be instanceof CableBusBlockEntity cableBE)) continue;
                            renderCableBE(cableBE, matrixStackObj, consumers);
                        }
                    }
                }

                BUFFER_SOURCE_END_BATCH.invoke(consumers);
            } finally {
                POSE_STACK_POP.invoke(matrixStackObj);
            }
        } catch (Exception e) {
            Ae2PortalsBridge.LOGGER.error("AE2 Portals Bridge: cable projection render failed", e);
        }
    }

    private static void renderCableBE(CableBusBlockEntity cableBE, Object poseStack, Object consumersObj) {
        try {
            Object blockState = BLOCK_ENTITY_GET_BLOCK_STATE.invoke(cableBE);
            Object blockRenderer = MC_BLOCK_RENDERER.invoke(MC_GET_INSTANCE.invoke(null));
            Object model = BLOCK_RENDERER_GET_BLOCK_MODEL.invoke(blockRenderer, blockState);
            if (!(model instanceof CableBusBakedModel)) return;
            if (!(model instanceof CableBusBakedModelAccessor accessor)) return;

            Object modelData = BLOCK_ENTITY_GET_MODEL_DATA.invoke(cableBE);
            if (modelData == null) return;
            // CABLE_BUS_RENDER_STATE_PROPERTY is a Field, so use .get(null) not .invoke()
            Object renderState = MODEL_DATA_GET.invoke(modelData, CABLE_BUS_RENDER_STATE_PROPERTY.get(null));
            if (renderState == null) return;

            POSE_STACK_PUSH.invoke(poseStack);
            try {
                java.util.List<?> quads = accessor.ae2portals$getCachedCableQuads((CableBusRenderState) renderState);
                for (Object q : quads) {
                    BUFFER_SOURCE_PUT_BULK_DATA.invoke(consumersObj, BAKED_QUAD_GET_POSE.invoke(q), q);
                }
            } finally {
                POSE_STACK_POP.invoke(poseStack);
            }
        } catch (Exception e) {
            Ae2PortalsBridge.LOGGER.error("AE2 Portals Bridge: renderCableBE failed", e);
        }
    }
}
