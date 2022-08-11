package space.cutekitten.debugrenderers.mixin;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Path.class)
public interface PathAccessor {
    @Accessor("debugNodes")
    void setDebugNodes(PathNode[] debugNodes);
    @Accessor("debugSecondNodes")
    void setDebugSecondNodes(PathNode[] debugSecondNodes);
    @Accessor("debugTargetNodes")
    void setDebugTargetNodes(Set<TargetPathNode> debugTargetNodes);
}
