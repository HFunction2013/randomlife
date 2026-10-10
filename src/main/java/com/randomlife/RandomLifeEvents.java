package com.randomlife;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Handles rolling random health for every creature and keeping the floating
 * health text above heads up to date.
 */
public class RandomLifeEvents {

    public static final String TAG_INITIALIZED = "randomlife.initialized";
    public static final String TAG_ORIGINAL_NAME = "randomlife.original_name";

    public static final int MIN_HEALTH = 1;
    public static final int MAX_HEALTH = 20000;

    private static final Random RANDOM = new Random();

    /** Rolls a random max health in the inclusive range [1, 20000]. */
    public static int rollHealth() {
        return MIN_HEALTH + RANDOM.nextInt(MAX_HEALTH - MIN_HEALTH + 1);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return; // The server is authoritative for health values.
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.getAttribute(Attributes.MAX_HEALTH) == null) {
            return; // A few living entities have no health attribute at all.
        }

        CompoundTag data = living.getPersistentData();
        if (data.getBoolean(TAG_INITIALIZED)) {
            // Already rolled (e.g. player logging back in): just refresh the text.
            updateHealthText(living);
            return;
        }

        // Remember the original name so the tag can read e.g. "Steve 15000/20000".
        if (living instanceof ServerPlayer player) {
            data.putString(TAG_ORIGINAL_NAME, player.getGameProfile().getName());
        } else if (living.hasCustomName()) {
            data.putString(TAG_ORIGINAL_NAME, living.getCustomName().getString());
        }

        data.putBoolean(TAG_INITIALIZED, true);
        applyHealth(living, rollHealth());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        // Every respawn gives the player a brand new random health pool.
        applyHealth(player, rollHealth());
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        updateHealthText(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        updateHealthText(event.getEntity());
    }

    /** Applies a new max health and heals the entity back to full. */
    public static void applyHealth(LivingEntity living, int maxHealth) {
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        living.setHealth(maxHealth);
        updateHealthText(living);
    }

    /** Re-renders the nametag as "current/max", prefixed with the original name when available. */
    public static void updateHealthText(LivingEntity living) {
        int current = (int) Math.ceil(living.getHealth());
        int max = (int) Math.ceil(living.getMaxHealth());

        String text = current + "/" + max;
        String original = living.getPersistentData().getString(TAG_ORIGINAL_NAME);
        if (!original.isEmpty()) {
            text = original + " " + text;
        }

        living.setCustomName(Component.literal(text));
        living.setCustomNameVisible(true);
    }
}
