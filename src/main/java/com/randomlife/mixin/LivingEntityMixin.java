package com.randomlife.mixin;

import com.randomlife.RandomLifeMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@code LivingEntity#getMaxHealth()} return the mod's own
 * {@code randomlife:max_health} attribute when it is present, instead of the
 * vanilla {@code generic.max_health} attribute (which is hard-capped at 1024).
 *
 * <p>The attribute is added on entity join (server side) and synced to clients,
 * so this reads the real rolled value on both sides. Entities without the
 * attribute keep vanilla behaviour.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void randomlife$getMaxHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        AttributeMap attributes = self.getAttributes();
        if (attributes != null && attributes.hasAttribute(RandomLifeMod.MAX_HEALTH.get())) {
            cir.setReturnValue((float) self.getAttributeValue(RandomLifeMod.MAX_HEALTH.get()));
        }
    }
}
