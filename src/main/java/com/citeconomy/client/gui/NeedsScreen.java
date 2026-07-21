package com.citeconomy.client.gui;

import com.citeconomy.client.ClientNeedsData;
import com.citeconomy.data.WeeklyQuest;
import com.citeconomy.menu.NeedsMenu;
import com.citeconomy.network.QuestCompletePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class NeedsScreen extends AbstractContainerScreen<NeedsMenu> {
    private static final int WINDOW_W = 230;
    private static final int WINDOW_H = 176;

    public NeedsScreen(NeedsMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = WINDOW_W;
        this.imageHeight = WINDOW_H;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float delta, int mx, int my) {
        int x = (this.width - WINDOW_W) / 2;
        int y = (this.height - WINDOW_H) / 2;
        gui.fill(x, y, x + WINDOW_W, y + WINDOW_H, 0xFFC6C6C6);
        gui.fill(x, y, x + WINDOW_W, y + 18, 0xFF555555);
        gui.drawString(this.font, Component.translatable("gui.citeconomy.needs.title"), x + 5, y + 5, 0xFFFFFF, false);
        gui.fill(x + 5, y + 22, x + WINDOW_W - 5, y + 120, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mx, int my) {
        List<WeeklyQuest> quests = ClientNeedsData.quests;
        if (quests == null || quests.isEmpty()) {
            gui.drawString(this.font, Component.translatable("gui.citeconomy.needs.none"), 10, 30, 0xAAAAAA);
            return;
        }

        int y = 28;
        for (WeeklyQuest q : quests) {
            String status;
            int color;
            if (q.isClaimed()) {
                status = Component.translatable("gui.citeconomy.needs.claimed").getString();
                color = 0x555555;
            } else if (q.isComplete()) {
                status = Component.translatable("gui.citeconomy.needs.claim").getString();
                color = 0x55FF55;
            } else {
                status = q.getProgress() + "/" + q.getTargetAmount();
                color = 0xFFFFFF;
            }

            gui.drawString(this.font, Component.translatable("gui.citeconomy.needs.line", q.getItemId().replace("minecraft:", ""), q.getTargetAmount(), status, q.getRewardCredits()), 10, y, color);
            y += 20;
        }
    }

    @Override
    public void render(GuiGraphics gui, int mx, int my, float delta) {
        renderBackground(gui, mx, my, delta);
        super.render(gui, mx, my, delta);
        renderTooltip(gui, mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int listX = ((this.width - WINDOW_W) / 2) + 10;
        int listY = ((this.height - WINDOW_H) / 2) + 28;
        List<WeeklyQuest> quests = ClientNeedsData.quests;

        if (quests != null) {
            for (int i = 0; i < quests.size(); i++) {
                WeeklyQuest q = quests.get(i);
                if (q.isComplete() && !q.isClaimed()) {
                    int y = listY + i * 20;
                    if (mx >= listX && mx <= listX + 200 && my >= y && my <= y + 16) {
                        PacketDistributor.sendToServer(new QuestCompletePayload(q.getId()));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
