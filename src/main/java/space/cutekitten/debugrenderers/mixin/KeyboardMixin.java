package space.cutekitten.debugrenderers.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.cutekitten.debugrenderers.client.ClientDB;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            MinecraftClient client = ClientDB.client;
            switch (key) {
                case GLFW.GLFW_KEY_RIGHT -> {
                    ClientDB.debugRenderer++;
                    if (ClientDB.debugRenderer > ClientDB.debugRenderers.length - 1) {
                        ClientDB.debugRenderer = -1;
                    }
                    client.inGameHud.setOverlayMessage(Text.literal("Debug renderer: " +
                            (ClientDB.debugRenderer == -1 ? "none" : ClientDB.debugRenderers[ClientDB.debugRenderer])
                    ), false);
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    ClientDB.debugRenderer--;
                    if (ClientDB.debugRenderer < -1) {
                        ClientDB.debugRenderer = ClientDB.debugRenderers.length - 1;
                    }
                    client.inGameHud.setOverlayMessage(Text.literal("Debug renderer: " +
                            (ClientDB.debugRenderer == -1 ? "none" : ClientDB.debugRenderers[ClientDB.debugRenderer])
                    ), false);
                }
            }

            if (ClientDB.debugRenderer < -1) {
                ClientDB.debugRenderer = ClientDB.debugRenderers.length - 1;
            }
            if (ClientDB.debugRenderer > ClientDB.debugRenderers.length - 1) {
                ClientDB.debugRenderer = -1;
            }
        }
    }
}
