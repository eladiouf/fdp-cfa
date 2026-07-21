package com.citeconomy.client.hud;

import com.citeconomy.CiteconomyMod;
import com.citeconomy.client.ClientState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = CiteconomyMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class BalanceOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        int balance = ClientState.getBalance();

        String text = "\u25C6 " + balance + " Cr";
        var graphics = event.getGuiGraphics();
        var font = mc.font;

        int width = mc.getWindow().getGuiScaledWidth();
        int textWidth = font.width(text);

        int bgX = width - textWidth - 14;
        int bgY = 4;
        int bgW = textWidth + 16;
        int bgH = 14;

        graphics.fill(bgX, bgY, bgX + bgW, bgY + bgH, 0x80202020);
        graphics.drawString(font, text, bgX + 6, bgY + 3, 0xFFD700, false);
    }
}
