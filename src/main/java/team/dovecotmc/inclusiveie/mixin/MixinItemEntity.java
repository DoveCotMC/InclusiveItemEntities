package team.dovecotmc.inclusiveie.mixin;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
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

    public MixinItemEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "mergeWithNeighbours", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB redirect$mergeWithNeighbours(AABB instance, double pX, double pY, double pZ) {
        final double horizontal = getItem().getCount() >= 64 ? 1.5 : .5;
        return instance.inflate(horizontal, .0, horizontal);
    }

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

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void inject$playerTouch(Player pEntity, CallbackInfo ci) {
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
                pEntity.getInventory().add(itemstack);
                i = copy.getCount() - itemstack.getCount();
                if (!shouldContinue && i == 0) return;
                copy.setCount(i);
                ForgeEventFactory.firePlayerItemPickupEvent(pEntity, (ItemEntity) (Object) this, copy);
                pEntity.take(this, i);
                if (itemstack.isEmpty()) {
                    this.discard();
                    itemstack.setCount(i);
                }
                pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                pEntity.onItemPickup((ItemEntity) (Object) this);
            }
        }
    }
}
