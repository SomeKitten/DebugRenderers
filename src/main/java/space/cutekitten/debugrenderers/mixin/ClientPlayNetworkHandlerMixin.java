package space.cutekitten.debugrenderers.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;getData()Lnet/minecraft/network/PacketByteBuf;", shift = At.Shift.AFTER), cancellable = true)
    public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (ClientDB.DEBUG_GOAL_SELECTOR_REMOVE.equals(packet.getChannel())) {
            ClientDB.client.debugRenderer.goalSelectorDebugRenderer.removeGoalSelectorList(packet.getData().readInt());
            ci.cancel();
        }
    }
}
