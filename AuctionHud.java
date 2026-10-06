package net.luxxy.auction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;
import java.util.Map;

public class AuctionHud {
    private static final int MAX_ROWS = 5;

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        if (!AuctionState.hudVisible) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;
        TextRenderer tr = mc.textRenderer;

        int w = 190;
        int h = 18 + 26 + 12 + MAX_ROWS * 11 + 6;
        int x = ctx.getScaledWindowWidth() / 2 - w / 2;
        int y = 4;

        // Hintergrund + Rahmen
        ctx.fill(x, y, x + w, y + h, 0xCC0B1A33);
        ctx.fill(x, y, x + w, y + 1, 0xFF3C7DD9);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xFF3C7DD9);
        ctx.fill(x, y, x + 1, y + h, 0xFF3C7DD9);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xFF3C7DD9);

        // 1) Titel oben
        ctx.drawText(tr, AuctionState.TITLE, x + 6, y + 6, 0xFFFFFFFF, true);

        // Timer rechts
        long rem = AuctionState.remainingSec();
        int color = !AuctionState.running ? 0xFFAAAAAA : (rem <= 10 ? 0xFFFF5555 : 0xFF55FF55);
        String time = AuctionState.formatTime(rem);
        ctx.drawText(tr, time, x + w - 6 - tr.getWidth(time), y + 6, color, true);

        // Item
        int iy = y + 20;
        if (!AuctionState.item.isEmpty()) {
            ctx.drawItem(AuctionState.item, x + 6, iy);
            ctx.drawText(tr, AuctionState.item.getName().getString(), x + 28, iy + 4, 0xFFFFFFFF, true);
        } else {
            ctx.drawText(tr, "Kein Item (Taste G)", x + 6, iy + 4, 0xFF888888, true);
        }
        String einnahmen = "Einnahmen: $" + AuctionState.formatMoney(AuctionState.total());
        ctx.drawText(tr, einnahmen, x + w - 6 - tr.getWidth(einnahmen), iy + 4, 0xFFFFD36B, true);

        // 2) Tabelle: Spieler | Summe
        int ty = y + 18 + 26;
        ctx.fill(x + 4, ty - 1, x + w - 4, ty + 10, 0x55000000);
        ctx.drawText(tr, "Spieler", x + 8, ty, 0xFF8FB8FF, false);
        String head = "Summe";
        ctx.drawText(tr, head, x + w - 8 - tr.getWidth(head), ty, 0xFF8FB8FF, false);

        List<Map.Entry<String, Double>> list = AuctionState.sorted();
        if (list.isEmpty()) {
            ctx.drawText(tr, "Noch keine Zahlungen", x + 8, ty + 14, 0xFF888888, false);
        }
        for (int i = 0; i < Math.min(MAX_ROWS, list.size()); i++) {
            Map.Entry<String, Double> e = list.get(i);
            int ry = ty + 12 + i * 11;
            ctx.drawText(tr, (i + 1) + ". " + e.getKey(), x + 8, ry, i == 0 ? 0xFFFFD700 : 0xFFFFFFFF, true);
            String amt = "$" + AuctionState.formatMoney(e.getValue());
            ctx.drawText(tr, amt, x + w - 8 - tr.getWidth(amt), ry, i == 0 ? 0xFFFFD700 : 0xFF55FF55, true);
        }
    }
}
