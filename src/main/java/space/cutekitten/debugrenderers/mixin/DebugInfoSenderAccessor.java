package space.cutekitten.debugrenderers.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.DebugInfoSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DebugInfoSender.class)
public interface DebugInfoSenderAccessor {
    @Invoker("writeBrain")
    static void invokeWriteBrain(LivingEntity entity, PacketByteBuf buf) {}
}
