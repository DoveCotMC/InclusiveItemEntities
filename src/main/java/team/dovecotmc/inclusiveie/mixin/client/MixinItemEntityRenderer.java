package team.dovecotmc.inclusiveie.mixin.client;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {
    @Inject(method = "getRenderAmount", at = @At("HEAD"), cancellable = true)
    private void inject$getRenderAmount(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        int i = 1;
        final int count = pStack.getCount();
        if (count > 512) {
            i = 5;
        } else if (count > 128) {
            i = 4;
        } else if (count > 32) {
            i = 3;
        } else if (count > 1) {
            i = 2;
        }
        cir.setReturnValue(i);
    }
}
