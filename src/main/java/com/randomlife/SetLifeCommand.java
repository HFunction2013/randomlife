package com.randomlife;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Implements /randomlife:set_life <targets> <life>
 *
 *   life = <num>     : sets the max health of the targets to num (1..20000)
 *   life = random    : re-rolls a random max health for the targets
 *
 * In both cases the current health points are kept and simply capped at the
 * new total, so an entity above its new max is clamped back to full.
 */
public class SetLifeCommand {

    private static final SimpleCommandExceptionType INVALID_LIFE =
            new SimpleCommandExceptionType(Component.literal(
                    "Life must be a whole number between 1 and 20000, or the word 'random'."));

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("randomlife:set_life")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("life", StringArgumentType.word())
                                        .executes(context -> setLife(
                                                context,
                                                EntityArgument.getEntities(context, "targets"),
                                                StringArgumentType.getString(context, "life"))))));
    }

    private int setLife(CommandContext<CommandSourceStack> context,
                        List<? extends Entity> targets,
                        String lifeArgument) throws CommandSyntaxException {

        int newMax;
        if (lifeArgument.equalsIgnoreCase("random")) {
            newMax = RandomLifeEvents.rollHealth();
        } else {
            try {
                newMax = Integer.parseInt(lifeArgument);
                if (newMax < RandomLifeEvents.MIN_HEALTH || newMax > RandomLifeEvents.MAX_HEALTH) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw INVALID_LIFE.create();
            }
        }

        int count = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity living)
                    || living.getAttribute(Attributes.MAX_HEALTH) == null) {
                continue;
            }
            int current = (int) Math.ceil(living.getHealth());
            living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMax);
            // Keep the current health points; clamp to the new total.
            living.setHealth(Math.min(current, newMax));
            RandomLifeEvents.updateHealthText(living);
            count++;
        }

        final int affected = count;
        context.getSource().sendSuccess(() -> Component.literal(
                "Set max health to " + newMax + " for " + affected + " entit"
                        + (affected == 1 ? "y" : "ies") + "."), true);
        return affected;
    }
}
