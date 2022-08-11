package space.cutekitten.debugrenderers.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class ClientDB {
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static int debugRenderer = -1;
    public static String[] debugRenderers = {
            "pathfindingDebugRenderer",
            "waterDebugRenderer",
            "chunkBorderDebugRenderer",
            "heightmapDebugRenderer",
            "collisionDebugRenderer",
            "neighborUpdateDebugRenderer",
            "structureDebugRenderer",
            "skyLightDebugRenderer",
            "worldGenAttemptDebugRenderer",
            "blockOutlineDebugRenderer",
            "chunkLoadingDebugRenderer",
            "villageDebugRenderer",
            "villageSectionsDebugRenderer",
            "beeDebugRenderer",
            "raidCenterDebugRenderer",
            "goalSelectorDebugRenderer",
            "gameTestDebugRenderer",
            "gameEventDebugRenderer"
    };
    public static Identifier DEBUG_GOAL_SELECTOR_REMOVE = new Identifier("debugrenderers", "debug/goal_selector_remove");
}
