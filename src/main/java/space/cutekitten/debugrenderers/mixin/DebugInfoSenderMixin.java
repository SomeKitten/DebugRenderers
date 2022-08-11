package space.cutekitten.debugrenderers.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(DebugInfoSender.class)
public abstract class DebugInfoSenderMixin {
    @Shadow
    private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {}

    @Inject(method = "sendPathfindingData", at = @At("RETURN"))
    private static void sendPathfindingData(World world, MobEntity mob, Path path, float nodeReachProximity, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld) || path == null) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(mob.getId());
        buf.writeFloat(nodeReachProximity);
        path.toBuffer(buf);
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_PATH);
    }
    @Inject(method = "sendNeighborUpdate", at = @At("RETURN"))
    private static void sendNeighborUpdate(World world, BlockPos pos, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarLong(world.getTime());
        buf.writeBlockPos(pos);
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_NEIGHBORS_UPDATE);
    }
    @Inject(method = "sendStructureStart", at = @At("RETURN"))
    private static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart, CallbackInfo ci) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(world.toServerWorld().getDimensionKey().getValue());
        writeBox(buf, structureStart.getBoundingBox());
        buf.writeInt(structureStart.getChildren().size());
        for (StructurePiece child : structureStart.getChildren()) {
            writeBox(buf, child.getBoundingBox());
            buf.writeBoolean(true); // not sure what this value is supposed to be
        }
        sendToAll(world.toServerWorld(), buf, CustomPayloadS2CPacket.DEBUG_STRUCTURES);
    }
    @Inject(method = "sendPoi", at = @At("RETURN"))
    private static void sendPoi(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
//        TODO: use ifPresent() instead of just .get()
        RegistryEntry<PointOfInterestType> entry = PointOfInterestTypes.getTypeForState(world.getBlockState(pos)).get();
        PointOfInterestType type = entry.value();
        buf.writeString(world.getBlockState(pos).getBlock().getName().getString());
        buf.writeInt(type.ticketCount());
        sendToAll(world, buf, CustomPayloadS2CPacket.DEBUG_POI_ADDED);
    }
    @Inject(method = "sendPoiRemoval", at = @At("RETURN"), cancellable = true)
    private static void sendPoiRemoval(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        sendToAll(world, buf, CustomPayloadS2CPacket.DEBUG_POI_REMOVED);

        ci.cancel();
    }
    @Inject(method = "sendGoalSelector", at = @At("RETURN"))
    private static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(mob.getBlockPos());
        buf.writeInt(mob.getId());
        buf.writeInt(goalSelector.getGoals().size());
        int i = 0;
        for (PrioritizedGoal goal : goalSelector.getGoals()) {
            buf.writeInt(i);
            buf.writeBoolean(goal.isRunning());
            buf.writeString(goal.getGoal().toString());
            i++;
        }
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_GOAL_SELECTOR);
    }
    @Inject(method = "sendRaids", at = @At("RETURN"))
    private static void sendRaids(ServerWorld server, Collection<Raid> raids, CallbackInfo ci) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(raids.size());
        for (Raid raid : raids) {
            buf.writeBlockPos(raid.getCenter());
        }
        sendToAll(server, buf, CustomPayloadS2CPacket.DEBUG_RAIDS);
    }
    @Inject(method = "sendBrainDebugData", at = @At("RETURN"))
    private static void sendBrainDebugData(LivingEntity living, CallbackInfo ci) {
        if (!(living.world instanceof ServerWorld world) || !(living instanceof MobEntity mob)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(living.getPos().x);
        buf.writeDouble(living.getPos().y);
        buf.writeDouble(living.getPos().z);
        buf.writeUuid(mob.getUuid());
        buf.writeInt(mob.getId());
        buf.writeString(NameGenerator.name(mob.getUuid()));
        buf.writeString(mob instanceof VillagerEntity villager ? villager.getVillagerData().getProfession().toString() : "none");
        buf.writeInt(mob instanceof VillagerEntity villager ? villager.getVillagerData().getLevel() : 0);
        buf.writeFloat(mob.getHealth());
        buf.writeFloat(mob.getMaxHealth());
        DebugInfoSenderAccessor.invokeWriteBrain(mob, buf);
        sendToAll(world, buf, CustomPayloadS2CPacket.DEBUG_BRAIN);
    }
    @Inject(method = "sendBeeDebugData", at = @At("RETURN"))
    private static void sendBeeDebugData(BeeEntity bee, CallbackInfo ci) {
        if (!(bee.world instanceof ServerWorld world)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(bee.getPos().x);
        buf.writeDouble(bee.getPos().y);
        buf.writeDouble(bee.getPos().z);
        buf.writeUuid(bee.getUuid());
        buf.writeInt(bee.getId());
        if (bee.getHivePos() != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(bee.getHivePos());
        } else {
            buf.writeBoolean(false);
        }
        if (bee.getFlowerPos() != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(bee.getFlowerPos());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(0); // not sure what this is supposed to be
        Path path = bee.getNavigation().getCurrentPath();
        if (path != null) {
            buf.writeBoolean(true);
            path.toBuffer(buf);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(0); // labels???
        buf.writeInt(0); // blacklist???
        sendToAll(world, buf, CustomPayloadS2CPacket.DEBUG_BEE);
    }
    @Inject(method = "sendBeehiveDebugData", at = @At("RETURN"))
    private static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeString(blockEntity.getClass().getSimpleName());
        buf.writeInt(blockEntity.getBeeCount());
        buf.writeInt(BeehiveBlockEntity.getHoneyLevel(state));
        buf.writeBoolean(blockEntity.isSmoked());
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_HIVE);
    }
    @Inject(method = "sendGameEvent", at = @At("RETURN"))
    private static void sendGameEvent(World world, GameEvent event, Vec3d pos, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(Registry.GAME_EVENT.getId(event).toString());
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_GAME_EVENT);
    }
    @Inject(method = "sendGameEventListener", at = @At("RETURN"))
    private static void sendGameEventListener(World world, GameEventListener eventListener, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        PositionSource positionSource = eventListener.getPositionSource();
        PositionSourceType<PositionSource> positionSourceType = (PositionSourceType<PositionSource>) positionSource.getType();
        buf.writeIdentifier(Registry.POSITION_SOURCE_TYPE.getId(positionSource.getType()));
        positionSourceType.writeToBuf(buf, positionSource);
        buf.writeVarInt(eventListener.getRange());
        sendToAll(serverWorld, buf, CustomPayloadS2CPacket.DEBUG_GAME_EVENT_LISTENERS);
    }
    private static void writeBox(PacketByteBuf buf, BlockBox box) {
        buf.writeInt(box.getMinX());
        buf.writeInt(box.getMinY());
        buf.writeInt(box.getMinZ());
        buf.writeInt(box.getMaxX());
        buf.writeInt(box.getMaxY());
        buf.writeInt(box.getMaxZ());
    }
}
