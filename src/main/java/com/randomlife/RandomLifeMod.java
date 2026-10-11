package com.randomlife;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Random Life - Forge 1.20.1
 *
 * Every living creature gets a random max health (1-20000) and shows its
 * current/total health floating above its head. Players re-roll their health
 * on every respawn.
 *
 * <p>Vanilla's {@code generic.max_health} attribute is hard-capped at 1024,
 * which silently clamps every roll above that (that is why everything used to
 * read "1024/1024"). This mod registers its own syncable
 * {@code randomlife:max_health} attribute with a 20000 cap; a small mixin makes
 * {@code LivingEntity#getMaxHealth()} return it whenever it is present, and the
 * value is synced to clients like any other attribute.
 */
@Mod(RandomLifeMod.MODID)
public class RandomLifeMod {

    public static final String MODID = "randomlife";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);

    /** Our own max-health attribute: same default as vanilla, but capped at 20000. */
    public static final RegistryObject<Attribute> MAX_HEALTH =
            ATTRIBUTES.register("max_health",
                    () -> new RangedAttribute("attribute.name.randomlife.max_health",
                            20.0D, RandomLifeEvents.MIN_HEALTH, RandomLifeEvents.MAX_HEALTH)
                            .setSyncable(true));

    public RandomLifeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ATTRIBUTES.register(modEventBus);

        // Add our custom max-health attribute to every living entity type so that
        // AttributeMap.getInstance(...) never returns null. Without this, entities
        // whose default attribute supplier does not include randomlife:max_health
        // crash with NPE in applyHealth on join.
        modEventBus.addListener(this::onEntityAttributeModification);

        MinecraftForge.EVENT_BUS.register(new RandomLifeEvents());
        MinecraftForge.EVENT_BUS.register(new SetLifeCommand());

        LOGGER.info("Random Life mod initialized.");
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, MAX_HEALTH.get());
        }
    }
}
