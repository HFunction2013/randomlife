package com.randomlife;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Random Life - Forge 1.20.1
 *
 * Every living creature gets a random max health (1-20000) and shows its
 * current/total health floating above its head. Players re-roll their health
 * on every respawn.
 */
@Mod(RandomLifeMod.MODID)
public class RandomLifeMod {

    public static final String MODID = "randomlife";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RandomLifeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new RandomLifeEvents());
        MinecraftForge.EVENT_BUS.register(new SetLifeCommand());

        LOGGER.info("Random Life mod initialized.");
    }
}
