package space.cutekitten.debugrenderers.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

import java.util.Collection;

@Mixin(DebugInfoSender.class)
public class DebugInfoSenderMixin {
    @Inject(method = "sendPathfindingData", at = @At("RETURN"))
    private static void sendPathfindingData(World world, MobEntity mob, Path path, float nodeReachProximity, CallbackInfo ci) {
        if (path != null) {
            ClientDB.newPaths.add(new ImmutableTriple<>(mob.getId(), path, nodeReachProximity));
        }
    }
    @Inject(method = "sendNeighborUpdate", at = @At("RETURN"))
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo ci) {
        if (pos != null) {
            ClientDB.newNeighborUpdates.add(new Pair<>(world.getTime(), pos));
        }
    }
    @Inject(method = "sendStructureStart", at = @At("RETURN"))
    private static void sendNeighborUpdate(StructureWorldAccess world, StructureStart structureStart, CallbackInfo ci) {
        if (structureStart != null) {
            ClientDB.newStructures.add(new Pair<>(world, structureStart));
        }
    }
    @Inject(method = "sendPoi", at = @At("RETURN"))
    private static void sendPoi(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (pos != null) {
            ClientDB.newPOIs.add(new Pair<>(world, pos));
        }
    }
    @Inject(method = "sendBeeDebugData", at = @At("RETURN"))
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        if (bee != null) {
            ClientDB.newBees.add(bee);
        }
    }
    @Inject(method = "sendBeehiveDebugData", at = @At("RETURN"))
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity != null) {
            ClientDB.newBeehives.add(blockEntity);
        }
    }

    @Inject(method = "sendGameEvent", at = @At("RETURN"))
    private static void sendGameEvent(World world, GameEvent event, Vec3d pos, CallbackInfo ci) {
        if (event != null) {
            ClientDB.newGameEvents.add(new Pair<>(event, pos));
        }
    }

    @Inject(method = "sendGameEventListener", at = @At("RETURN"))
    private static void sendGameEventListener(World world, GameEventListener eventListener, CallbackInfo ci) {
        if (eventListener != null) {
            ClientDB.newGameEventListeners.add(eventListener);
        }
    }

    @Inject(method = "sendGoalSelector", at = @At("RETURN"))
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (mob != null) {
            ClientDB.newGoalSelectors.add(new Pair<>(mob, goalSelector));
        }
    }

    @Inject(method = "sendBrainDebugData", at = @At("RETURN"))
    private static void sendBrainDebugData(LivingEntity living, CallbackInfo ci) {
        if (living != null) {
            ClientDB.newBrains.add(living);
        }
    }

    @Inject(method = "sendRaids", at = @At("RETURN"))
    private static void sendRaids(ServerWorld server, Collection<Raid> raids, CallbackInfo ci) {
        ClientDB.newRaids.add(raids);
    }



//    @Inject(method = "sendChunkWatchingChange", at = @At("RETURN"))
//    private static void sendChunkWatchingChange(ServerWorld world, ChunkPos pos, CallbackInfo ci) {
//        if (pos != null) {
//            ClientDB.newChunkChanges.add(new Pair<>(world, pos));
//        }
//    }
}
