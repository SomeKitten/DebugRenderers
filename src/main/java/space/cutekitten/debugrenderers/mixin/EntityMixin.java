package space.cutekitten.debugrenderers.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow private int id;

    @Shadow public World world;

    @Inject(method = "setRemoved", at = @At("HEAD"))
    public void setRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(id);
            sendToAll(serverWorld, buf, ClientDB.DEBUG_GOAL_SELECTOR_REMOVE);
        }
    }

    private static void sendToAll(ServerWorld world, PacketByteBuf buf, Identifier channel) {
        Packet<?> packet = new CustomPayloadS2CPacket(channel, buf);

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(packet);
        }

    }
}
