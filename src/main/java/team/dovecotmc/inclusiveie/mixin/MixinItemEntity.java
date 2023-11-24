package team.dovecotmc.inclusiveie.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {
    @Redirect(method = "isMergable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private int redirect$isMergable(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "areMergable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private static int redirect$areMergable(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @Redirect(
            method = "merge(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private static int redirect$merge$0(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            constant = @Constant(intValue = 64))
    private static int redirect$merge$1(int constant) {
        return Integer.MAX_VALUE;
    }
}
