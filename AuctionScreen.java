package net.luxxy.auction;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AuctionScreen extends Screen {
    private TextFieldWidget timeField;

    public AuctionScreen() {
        super(Text.literal("Auktions-Menü"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 40;

        timeField = new TextFieldWidget(textRenderer, cx - 60, y, 70, 20, Text.literal("Zeit"));
        timeField.setText(AuctionState.formatTime(AuctionState.durationSec));
        addDrawableChild(timeField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Setzen"), b -> applyTime())
                .dimensions(cx + 14, y, 46, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Start"), b -> {
            applyTime();
            AuctionState.start();
        }).dimensions(cx - 60, y + 28, 58, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Beenden"), b -> AuctionState.stop())
                .dimensions(cx + 2, y + 28, 58, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Zurücksetzen"), b -> {
            AuctionState.reset();
            timeField.setText(AuctionState.formatTime(AuctionState.durationSec));
        }).dimensions(cx - 60, y + 52, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), b -> close())
                .dimensions(cx - 60, y + 76, 120, 20).build());
    }

    /** Akzeptiert "90" (Sekunden) oder "1:30" (mm:ss). */
    private void applyTime() {
        String t = timeField.getText().trim();
        try {
            long sec;
            if (t.contains(":")) {
                String[] p = t.split(":");
                sec = Long.parseLong(p[0].trim()) * 60 + Long.parseLong(p[1].trim());
            } else {
                sec = Long.parseLong(t);
            }
            AuctionState.setDuration(sec);
            timeField.setText(AuctionState.formatTime(AuctionState.durationSec));
        } catch (Exception e) {
            timeField.setText(AuctionState.formatTime(AuctionState.durationSec));
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, AuctionState.TITLE, width / 2, height / 2 - 70, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Zeit (mm:ss)", width / 2 - 60, height / 2 - 52, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
