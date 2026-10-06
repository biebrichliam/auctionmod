package net.luxxy.auction;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionState {
    /** Titel oben im Overlay – hier anpassen. */
    public static final String TITLE = "199k's Auction";

    public static ItemStack item = ItemStack.EMPTY;
    /** Spielername -> Summe aller /pay an dich */
    public static final Map<String, Double> payments = new HashMap<>();

    public static long durationSec = 60;
    public static long endMillis = 0;
    public static boolean running = false;
    public static boolean hudVisible = true;

    public static void start() {
        endMillis = System.currentTimeMillis() + durationSec * 1000L;
        running = true;
    }

    public static void stop() {
        running = false;
    }

    /** Zeit zurücksetzen + Zahlungen löschen. */
    public static void reset() {
        running = false;
        payments.clear();
        endMillis = 0;
    }

    public static void setDuration(long sec) {
        durationSec = Math.max(1, sec);
        if (running) endMillis = System.currentTimeMillis() + durationSec * 1000L;
    }

    public static long remainingSec() {
        if (!running) return durationSec;
        long ms = endMillis - System.currentTimeMillis();
        if (ms <= 0) {
            running = false;
            return 0;
        }
        return (ms + 999) / 1000;
    }

    public static void addPayment(String player, double amount) {
        if (!running) return; // nur während die Auktion läuft
        payments.merge(player, amount, Double::sum);
    }

    public static double total() {
        return payments.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public static List<Map.Entry<String, Double>> sorted() {
        List<Map.Entry<String, Double>> l = new ArrayList<>(payments.entrySet());
        l.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return l;
    }

    public static String formatTime(long sec) {
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    public static String formatMoney(double v) {
        if (v >= 1_000_000_000) return trim(v / 1_000_000_000) + "B";
        if (v >= 1_000_000) return trim(v / 1_000_000) + "M";
        if (v >= 1_000) return trim(v / 1_000) + "K";
        return trim(v);
    }

    private static String trim(double d) {
        String s = String.format(java.util.Locale.US, "%.2f", d);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}
