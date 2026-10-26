package me.alpha432.oyvey.debug;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.render.ChamsModule;
import me.alpha432.oyvey.util.render.ChamsRenderType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ChamsDebugHarness {
    private static int tick;

    private ChamsDebugHarness() {
    }

    public static void init() {
        if (System.getProperty("oyvey.chamsDebug") == null) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.level == null) {
                tick = 0;
                return;
            }
            ++tick;
            switch (tick) {
                case 40 -> {
                    hold(mc, Items.ENDER_PEARL);
                    enable();
                    ChamsRenderType.setDebugVariant(0);
                }
                case 60 -> shoot(mc, "chams_0_pearl.png");
                case 80 -> ChamsRenderType.setDebugVariant(1);
                case 100 -> shoot(mc, "chams_1_pearl.png");
                case 120 -> ChamsRenderType.setDebugVariant(2);
                case 140 -> shoot(mc, "chams_2_pearl.png");
                case 160 -> ChamsRenderType.setDebugVariant(3);
                case 180 -> shoot(mc, "chams_3_pearl.png");
                case 200 -> ChamsRenderType.setDebugVariant(0);
                case 220 -> shoot(mc, "chams_0_pearl_again.png");
                case 240 -> hold(mc, Items.TOTEM_OF_UNDYING);
                case 260 -> shoot(mc, "chams_0_totem.png");
                case 280 -> hold(mc, Items.OBSIDIAN);
                case 300 -> shoot(mc, "chams_0_obsidian.png");
                case 320 -> hold(mc, Items.TRIDENT);
                case 340 -> shoot(mc, "chams_0_trident.png");
                case 360 -> {
                    ChamsModule module = module();
                    if (module != null && module.isEnabled()) {
                        module.toggle();
                    }
                    hold(mc, Items.ENDER_PEARL);
                }
                case 380 -> shoot(mc, "chams_off_pearl.png");
                case 460 -> mc.stop();
                default -> {
                }
            }
        });
    }

    private static void hold(Minecraft mc, Item item) {
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
    }

    private static ChamsModule module() {
        return OyVey.moduleManager == null ? null : OyVey.moduleManager.getModuleByClass(ChamsModule.class);
    }

    private static void enable() {
        ChamsModule module = module();
        if (module != null && !module.isEnabled()) {
            module.toggle();
        }
    }

    private static void shoot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name, mc.gameRenderer.mainRenderTarget(), 1, message -> {
        });
        OyVey.LOGGER.info("[ChamsDebug] screenshot {}", name);
    }
}
