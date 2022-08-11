package space.cutekitten.debugrenderers.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(Path.class)
public abstract class PathMixin {
    @Shadow @Final private boolean reachesTarget;

    @Shadow private int currentNodeIndex;

    @Shadow @Nullable private Set<TargetPathNode> debugTargetNodes;

    @Shadow @Final private BlockPos target;

    @Shadow @Final private List<PathNode> nodes;

    @Shadow private PathNode[] debugNodes;

    @Shadow private PathNode[] debugSecondNodes;

//    I really didn't want to do this, but I've looked over the code a lot and can't figure it out :/
    /**
     * @author SomeKitten
     * @reason I need to be able to serialize the path, and couldn't figure out the best way to invoke Path#setDebugInfo
     */
    @Overwrite
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeBoolean(reachesTarget);
        buffer.writeInt(currentNodeIndex);
        if (debugTargetNodes != null) {
            buffer.writeInt(debugTargetNodes.size());
            debugTargetNodes.forEach((targetPathNode) -> targetPathNode.toBuffer(buffer));
        } else {
            buffer.writeInt(0);
        }
        buffer.writeInt(target.getX());
        buffer.writeInt(target.getY());
        buffer.writeInt(target.getZ());
        buffer.writeInt(nodes.size());

        for (PathNode pathNode : nodes) {
            pathNode.toBuffer(buffer);
        }

        buffer.writeInt(debugNodes.length);
        PathNode[] var6 = debugNodes;
        int var7 = var6.length;

        int var4;
        PathNode pathNode2;
        for(var4 = 0; var4 < var7; ++var4) {
            pathNode2 = var6[var4];
            pathNode2.toBuffer(buffer);
        }

        buffer.writeInt(debugSecondNodes.length);
        var6 = debugSecondNodes;
        var7 = var6.length;

        for(var4 = 0; var4 < var7; ++var4) {
            pathNode2 = var6[var4];
            pathNode2.toBuffer(buffer);
        }
    }

    /**
     * @author SomeKitten
     * @reason see PathMixin#toBuffer
     */
    @Overwrite
    public static Path fromBuffer(PacketByteBuf buffer) {
        boolean bl = buffer.readBoolean();
        int i = buffer.readInt();
        int j = buffer.readInt();
        Set<TargetPathNode> set = Sets.newHashSet();

        for(int k = 0; k < j; ++k) {
            set.add(TargetPathNode.fromBuffer(buffer));
        }

        BlockPos blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        List<PathNode> list = Lists.newArrayList();
        int l = buffer.readInt();

        for(int m = 0; m < l; ++m) {
            list.add(PathNode.readBuf(buffer));
        }

        PathNode[] pathNodes = new PathNode[buffer.readInt()];

        for(int n = 0; n < pathNodes.length; ++n) {
            pathNodes[n] = PathNode.readBuf(buffer);
        }

        PathNode[] pathNodes2 = new PathNode[buffer.readInt()];

        for(int o = 0; o < pathNodes2.length; ++o) {
            pathNodes2[o] = PathNode.readBuf(buffer);
        }

        Path path = new Path(list, blockPos, bl);
        ((PathAccessor) path).setDebugNodes(pathNodes);
        ((PathAccessor) path).setDebugSecondNodes(pathNodes2);
        ((PathAccessor) path).setDebugTargetNodes(set);
        path.setCurrentNodeIndex(i);
        return path;
    }
}
