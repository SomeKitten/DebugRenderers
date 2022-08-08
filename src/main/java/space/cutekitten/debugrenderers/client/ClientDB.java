package space.cutekitten.debugrenderers.client;

import io.netty.channel.unix.IovArray;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;

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

    public static List<ImmutableTriple<Integer, Path, Float>> newPaths = new ArrayList<>();
    public static List<Pair<Long, BlockPos>> newNeighborUpdates = new ArrayList<>();
    public static List<Pair<StructureWorldAccess, StructureStart>> newStructures = new ArrayList<>();
    public static List<Pair<ServerWorld, BlockPos>> newPOIs = new ArrayList<>();
    public static List<BeeEntity> newBees = new ArrayList<>();
    public static List<BeehiveBlockEntity> newBeehives = new ArrayList<>();
    public static List<Pair<GameEvent, Vec3d>> newGameEvents = new ArrayList<>();
    public static List<GameEventListener> newGameEventListeners = new ArrayList<>();
    public static List<Pair<MobEntity, GoalSelector>> newGoalSelectors = new ArrayList<>();
    public static List<LivingEntity> newBrains = new ArrayList<>();
}
