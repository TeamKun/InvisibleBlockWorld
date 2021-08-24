package net.kunmc.lab.invisibleblockworld.packet;

import net.kunmc.lab.invisibleblockworld.InvisibleBlockWorld;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RevealedBlockPosMessage {
    public Set<BlockPos> revealedBlockPos;

    public RevealedBlockPosMessage() {
        this.revealedBlockPos = new HashSet<>();
    }

    public RevealedBlockPosMessage(Set<BlockPos> revealedBlockPos) {
        this.revealedBlockPos = revealedBlockPos;
    }

    public static void encodeMessage(RevealedBlockPosMessage message, PacketBuffer buffer) {
        message.revealedBlockPos.forEach(buffer::writeBlockPos);
    }

    public static RevealedBlockPosMessage decodeMessage(PacketBuffer buffer) {
        RevealedBlockPosMessage message = new RevealedBlockPosMessage();
        try {
            while (true) {
                message.revealedBlockPos.add(buffer.readBlockPos());
            }
        } catch (IndexOutOfBoundsException ignore) {
        }

        return message;
    }

    public static void receiveMessage(RevealedBlockPosMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        InvisibleBlockWorld.revealedBlockPosSet = message.revealedBlockPos;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        World w = Minecraft.getInstance().player.getEntityWorld();
        InvisibleBlockWorld.revealedBlockPosSet.forEach(x -> {
            BlockState blockState = w.getBlockState(x);
            w.notifyBlockUpdate(x, blockState, blockState, 16);
        });
    }
}
