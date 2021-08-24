package net.kunmc.lab.invisibleblockworld.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.kunmc.lab.invisibleblockworld.InvisibleBlockWorld;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {
    @Inject(method = "renderFluid", at = @At("HEAD"), cancellable = true)
    public void renderFluid(BlockPos posIn, IBlockDisplayReader lightReaderIn, IVertexBuilder vertexBuilderIn, FluidState fluidStateIn, CallbackInfoReturnable<Boolean> info) {
        if (!InvisibleBlockWorld.revealedBlockPosSet.contains(posIn)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    @Inject(method = "renderModel(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderModel(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, boolean checkSides, Random rand, net.minecraftforge.client.model.data.IModelData modelData, CallbackInfoReturnable<Boolean> info) {
        if (!InvisibleBlockWorld.revealedBlockPosSet.contains(posIn)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
