package net.luxxy.auction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionMod implements ClientModInitializer {

    /**
     * Chat-Muster für eingehende Zahlungen. Gruppe 1 = Name, 2 = Zahl, 3 = k/m/b (optional).
     * Passe sie an das genaue Format deines Servers an (z.B. HugoSMP)!
     */
    private static final List<Pattern> PAY_PATTERNS = List.of(
            Pattern.compile("(\\w{3,16}) (?:paid you|has paid you|sent you|hat dir) \\$?([\\d.,]+)\\s?([kKmMbB]?)"),
            Pattern.compile("(\\w{3,16}) hat dir \\$?([\\d.,]+)\\s?([kKmMbB]?) (?:gezahlt|gesendet|überwiesen)")
    );

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("auctionmod", "main"));

    private static KeyBinding setItemKey;
    private static KeyBinding menuKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        setItemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auctionmod.set_item", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auctionmod.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auctionmod.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (setItemKey.wasPressed()) {
                if (client.player == null) continue;
                ItemStack held = client.player.getMainHandStack();
                if (held.isEmpty()) {
                    client.player.sendMessage(Text.literal("§cDu hast kein Item in der Hand!"), true);
                } else {
                    AuctionState.item = held.copy();
                    client.player.sendMessage(
                            Text.literal("§aAuktions-Item: ").append(held.getName()), true);
                }
            }
            while (menuKey.wasPressed()) {
                client.setScreen(new AuctionScreen());
            }
            while (toggleKey.wasPressed()) {
                AuctionState.hudVisible = !AuctionState.hudVisible;
            }
        });

        // Zahlungen aus dem Chat lesen
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            parsePayment(message.getString());
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, ts) ->
                parsePayment(message.getString()));

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of("auctionmod", "auction_hud"), AuctionHud::render);
    }

    private static void parsePayment(String text) {
        for (Pattern p : PAY_PATTERNS) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                try {
                    double value = Double.parseDouble(m.group(2).replace(",", ""));
                    switch (m.group(3).toLowerCase()) {
                        case "k" -> value *= 1_000;
                        case "m" -> value *= 1_000_000;
                        case "b" -> value *= 1_000_000_000;
                        default -> {}
                    }
                    AuctionState.addPayment(m.group(1), value);
                } catch (NumberFormatException ignored) {}
                return;
            }
        }
    }
}
