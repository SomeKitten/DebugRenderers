package space.cutekitten.debugrenderers.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.debug.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public ClientWorld world;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final public DebugRenderer debugRenderer;

    @Inject(at = @At("RETURN"), method = "tick")
    private void onEndTick(CallbackInfo info) {
        loadDebugInfo();
    }

    private void loadDebugInfo() {
        Object[] paths = ClientDB.newPaths.toArray();
        for (Object pathParams : paths) {
            ImmutableTriple<Integer, Path, Float> path = (ImmutableTriple<Integer, Path, Float>) pathParams;
//            sometimes its null???
            if (path == null) {
                continue;
            }
            debugRenderer.pathfindingDebugRenderer.addPath(path.getLeft(), path.getMiddle(), path.getRight());
            ClientDB.newPaths.remove(path);
        }


        Object[] neighborUpdates = ClientDB.newNeighborUpdates.toArray();
        for (Object neighborUpdate : neighborUpdates) {
            Pair<Long, BlockPos> update = (Pair<Long, BlockPos>) neighborUpdate;
//            sometimes its null???
            if (update == null) {
                continue;
            }
            ((NeighborUpdateDebugRenderer)(debugRenderer.neighborUpdateDebugRenderer)).addNeighborUpdate(update.getLeft(), update.getRight());
            ClientDB.newNeighborUpdates.remove(update);
        }


        Object[] structures = ClientDB.newStructures.toArray();
        for (Object structure : structures) {
            Pair< StructureWorldAccess, StructureStart> structureStartPair = (Pair< StructureWorldAccess, StructureStart>) structure;
//            sometimes its null???
            if (structureStartPair == null) {
                continue;
            }
            StructureStart structureStart = structureStartPair.getRight();
            List<BlockBox> childrenBoxes = new ArrayList<>();
            List<Boolean> someColorBools = new ArrayList<>();
            for (StructurePiece child : structureStart.getChildren()) {
                childrenBoxes.add(child.getBoundingBox());
                someColorBools.add(true);
            }
            debugRenderer.structureDebugRenderer.addStructure(structureStart.getBoundingBox(), childrenBoxes, someColorBools, structureStartPair.getLeft().getDimension());
            ClientDB.newStructures.remove(structure);
        }


        Object[] pois = ClientDB.newPOIs.toArray();
        for (Object poi : pois) {
            Pair<ServerWorld, BlockPos> poiPair = (Pair<ServerWorld, BlockPos>) poi;
//            sometimes its null???
            if (poiPair == null) {
                continue;
            }
            ServerWorld world = poiPair.getLeft();
            BlockPos pos = poiPair.getRight();
            BlockState state = world.getBlockState(pos);
            AtomicInteger freeTickets = new AtomicInteger();
            PointOfInterestTypes.getTypeForState(state).ifPresent(registryEntry -> {
                freeTickets.set(registryEntry.value().ticketCount());
            });

            debugRenderer.villageDebugRenderer.addPointOfInterest(new VillageDebugRenderer.PointOfInterest(
                    pos,
                    state.getBlock().getClass().getSimpleName(),
                    freeTickets.get()
            ));
            ClientDB.newPOIs.remove(poi);
        }


        Object[] bees = ClientDB.newBees.toArray();
        for (Object b : bees) {
            BeeEntity bee = (BeeEntity) b;
//            sometimes its null???
            if (bee == null) {
                continue;
            }
            debugRenderer.beeDebugRenderer.addBee(new BeeDebugRenderer.Bee(bee.getUuid(), bee.getId(), bee.getPos(), bee.getNavigation().getCurrentPath(), bee.getHivePos(), bee.getFlowerPos(), 0));
            ClientDB.newBees.remove(bee);
        }

        Object[] beehives = ClientDB.newBeehives.toArray();
        for (Object b : beehives) {
            BeehiveBlockEntity beehive = (BeehiveBlockEntity) b;
//            sometimes its null???
            if (beehive == null || world == null) {
                continue;
            }
            debugRenderer.beeDebugRenderer.addHive(new BeeDebugRenderer.Hive(beehive.getPos(), beehive.getClass().getSimpleName(), beehive.getBeeCount(), BeehiveBlockEntity.getHoneyLevel(beehive.getCachedState()), beehive.isSmoked(), world.getTime()));
            ClientDB.newBeehives.remove(beehive);
        }

//        used https://github.com/Tom-The-Geek/DebugRenderers as reference
        Object[] goalSelectors = ClientDB.newGoalSelectors.toArray();
        for (Object g : goalSelectors) {
            Pair<MobEntity, GoalSelector> goalPair = (Pair<MobEntity, GoalSelector>) g;
//            sometimes its null???
            if (goalPair == null) {
                continue;
            }

            MobEntity mob = goalPair.getLeft();
            if (mob.isAlive()) {
                GoalSelector goalSelector = goalPair.getRight();

                Set<PrioritizedGoal> goalList = goalSelector.getGoals();
                List<GoalSelectorDebugRenderer.GoalSelector> goalListOut = new ArrayList<>();
                int i = 0;
                for (PrioritizedGoal goal : goalList) {
                    goalListOut.add(new GoalSelectorDebugRenderer.GoalSelector(mob.getBlockPos(), i, goal.getGoal().toString(), goal.isRunning()));
                    i++;
                }

                debugRenderer.goalSelectorDebugRenderer.setGoalSelectorList(mob.getId(), goalListOut);
            } else {
                debugRenderer.goalSelectorDebugRenderer.removeGoalSelectorList(mob.getId());
            }

            ClientDB.newGoalSelectors.remove(goalPair);
        }

        Object[] event = ClientDB.newGameEvents.toArray();
        for (Object e : event) {
            Pair<GameEvent, Vec3d> gameEventPair = (Pair<GameEvent, Vec3d>) e;
//            sometimes its null???
            if (gameEventPair == null) {
                continue;
            }
            debugRenderer.gameEventDebugRenderer.addEvent(gameEventPair.getLeft(), gameEventPair.getRight());
            ClientDB.newGameEvents.remove(gameEventPair);
        }

        Object[] eventListener = ClientDB.newGameEventListeners.toArray();
        for (Object e : eventListener) {
            GameEventListener gameEventListener = (GameEventListener) e;
//            sometimes its null???
            if (gameEventListener == null) {
                continue;
            }
            debugRenderer.gameEventDebugRenderer.addListener(gameEventListener.getPositionSource(), gameEventListener.getRange());
            ClientDB.newGameEventListeners.remove(e);
        }

        Object[] livingBrains = ClientDB.newBrains.toArray();
        for (Object l : livingBrains) {
            LivingEntity livingBrain = (LivingEntity) l;
//            sometimes its null???
            if (livingBrain == null) {
                continue;
            }
            if (!(livingBrain instanceof VillagerEntity villager)) {
                continue;
            }

            debugRenderer.villageDebugRenderer.addBrain(new VillageDebugRenderer.Brain(
                    villager.getUuid(),
                    villager.getId(),
                    NameGenerator.name(villager),
                    villager.getVillagerData().getProfession().toString(),
                    villager.getVillagerData().getLevel(),
                    villager.getHealth(),
                    villager.getMaxHealth(),
                    villager.getPos(),
                    villager.getInventory().toString(),
                    villager.getNavigation().getCurrentPath(),
                    villager.canSummonGolem(world.getTime()),
                    villager.getReputation(player)
            ));
            ClientDB.newBrains.remove(villager);
        }

        Object[] raids = ClientDB.newRaids.toArray();
        Collection<BlockPos> raidPos = new ArrayList<>();
        for (Object r : raids) {
            Collection<Raid> raid = (Collection<Raid>) r;
//            sometimes its null???
            if (raid == null || raid.isEmpty()) {
                continue;
            }

            for (Raid rd : raid) {
                raidPos.add(rd.getCenter());
            }
        }

        if (!raidPos.isEmpty()) {
            debugRenderer.raidCenterDebugRenderer.setRaidCenters(raidPos);
        }

//        Object[] chunks = ClientDB.newChunkChanges.toArray();
//        for (Object c : chunks) {
//            Pair<World, ChunkPos> chunkPair = (Pair<World, ChunkPos>) c;
////            sometimes its null???
//            if (chunkPair == null) {
//                continue;
//            }
//
//            World world = chunkPair.getLeft();
//            ChunkPos chunkPos = chunkPair.getRight();
//
//            ((WorldGenAttemptDebugRenderer) debugRenderer.worldGenAttemptDebugRenderer).addBox();
//        }
    }
}
