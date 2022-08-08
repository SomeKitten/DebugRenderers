package space.cutekitten.debugrenderers.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

import java.util.List;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Shadow @Final public DebugRenderer.Renderer skyLightDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer waterDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer chunkBorderDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer heightmapDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer collisionDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer neighborUpdateDebugRenderer;

    @Shadow @Final public StructureDebugRenderer structureDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer worldGenAttemptDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer blockOutlineDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer chunkLoadingDebugRenderer;

    @Shadow @Final public VillageDebugRenderer villageDebugRenderer;

    @Shadow @Final public VillageSectionsDebugRenderer villageSectionsDebugRenderer;

    @Shadow @Final public BeeDebugRenderer beeDebugRenderer;

    @Shadow @Final public RaidCenterDebugRenderer raidCenterDebugRenderer;

    @Shadow @Final public GoalSelectorDebugRenderer goalSelectorDebugRenderer;

    @Shadow @Final public GameTestDebugRenderer gameTestDebugRenderer;

    @Shadow @Final public GameEventDebugRenderer gameEventDebugRenderer;

    @Shadow @Final public PathfindingDebugRenderer pathfindingDebugRenderer;
    private List<DebugRenderer.Renderer> renderers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        renderers = List.of(
                pathfindingDebugRenderer,
                waterDebugRenderer,
                chunkBorderDebugRenderer,
                heightmapDebugRenderer,
                collisionDebugRenderer,
                neighborUpdateDebugRenderer,
                structureDebugRenderer,
                skyLightDebugRenderer,
                worldGenAttemptDebugRenderer,
                blockOutlineDebugRenderer,
                chunkLoadingDebugRenderer,
                villageDebugRenderer,
                villageSectionsDebugRenderer,
                beeDebugRenderer,
                raidCenterDebugRenderer,
                goalSelectorDebugRenderer,
                gameTestDebugRenderer,
                gameEventDebugRenderer
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderDebug(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (ClientDB.debugRenderer == -1) {
            return;
        }

        DebugRenderer.Renderer renderer = renderers.get(ClientDB.debugRenderer);

        renderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }
}
