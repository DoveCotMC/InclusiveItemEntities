package team.dovecotmc.inclusiveie.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    @Shadow
    private int pickupDelay;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    @Nullable
    private UUID owner;

    public MixinItemEntity(EntityType<?> pEntityType, World pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "mergeWithNeighbours", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;inflate(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    private AxisAlignedBB redirect$mergeWithNeighbours(AxisAlignedBB instance, double pX, double pY, double pZ) {
        final double horizontal = getItem().getCount() >= 64 ? 1.5 : .5;
        return instance.inflate(horizontal, .0, horizontal);
    }

    @Redirect(method = "isMergable", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxStackSize()I"))
    private int redirect$isMergable(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "areMergable", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxStackSize()I"))
    private static int redirect$areMergable(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @Redirect(
            method = "merge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxStackSize()I"))
    private static int redirect$merge$0(ItemStack instance) {
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "merge(Lnet/minecraft/entity/item/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V",
            constant = @Constant(intValue = 64))
    private static int redirect$merge$1(int constant) {
        return Integer.MAX_VALUE;
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void inject$playerTouch(PlayerEntity pEntity, CallbackInfo ci) {
        ci.cancel();
        if (!this.level.isClientSide) {
            if (this.pickupDelay > 0) return;
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();
            int hook = ForgeEventFactory.onItemPickup((ItemEntity) (Object) this, pEntity);
            if (hook < 0) return;
            ItemStack copy = itemstack.copy();
            boolean shouldContinue = i <= 0 || hook == 1;
            if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(pEntity.getUUID()))) {
                pEntity.inventory.add(itemstack);
                i = copy.getCount() - itemstack.getCount();
                if (!shouldContinue && i == 0) return;
                copy.setCount(i);
                BasicEventHooks.firePlayerItemPickupEvent(pEntity, (ItemEntity) (Object) this, copy);
                pEntity.take(this, i);
                if (itemstack.isEmpty()) {
                    this.remove();
                    itemstack.setCount(i);
                }
                pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                pEntity.onItemPickup((ItemEntity) (Object) this);
            }
        }
    }
}
