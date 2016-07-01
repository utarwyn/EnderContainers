package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class FloatingTextUtils {

    private static ArrayList<FloatingText> floatingTexts = new ArrayList<>();


    public static ArrayList<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }


    public static FloatingText getFloatingTextFromHandler(Location location, String text){
        return getFloatingTextFromHandler(location, text, null);
    }
    public static FloatingText getFloatingTextFromHandler(Location location, String text, Player p){
        String version = EnderContainers.getServerVersion();
        FloatingText floatingText = null;

        try {
            final Class<?> clazz = Class.forName("fr.utarwyn.endercontainers.handlers." + version + ".FloatingText");

            if (FloatingText.class.isAssignableFrom(clazz)) { // Make sure it actually implements NMS
                floatingText = (FloatingText) clazz.getConstructor(Location.class, String.class, Player.class).newInstance(location, text, p); // Set our handler
            }
        } catch (final Exception e) {
            e.printStackTrace();
            EnderContainers.getInstance().getLogger().severe("Could not find support for this CraftBukkit version.");
            EnderContainers.getInstance().getServer().getPluginManager().disablePlugin(EnderContainers.getInstance());
            return null;
        }

        return floatingText;
    }


    public static FloatingText displayFloatingTextAt(String text, Location loc) {
        FloatingText floatingText = getFloatingTextFromHandler(loc, text);

        floatingTexts.add(floatingText);
        return floatingText;
    }

    public static FloatingText displayFloatingTextAt(String text, World world, float x, float y, float z) {
        return displayFloatingTextAt(text, new Location(world, x, y, z));
    }

    public static FloatingText displayFloatingTextAtFor(String text, Location loc, Player player) {
        FloatingText floatingText = getFloatingTextFromHandler(loc, text, player);

        floatingTexts.add(floatingText);
        return floatingText;
    }


    public static void removeFloatingText(FloatingText floatingText) {
        if (floatingTexts.contains(floatingText)) {
            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
            floatingTexts.remove(floatingText);
        }
    }

    public static void removeFloatingText(String text) {
        for (FloatingText floatingText : floatingTexts) {
            if (!floatingText.getText().equalsIgnoreCase(text)) continue;

            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }

            floatingTexts.remove(floatingText);
        }
    }

    public static void clearFloatingTexts() {
        for (FloatingText floatingText : floatingTexts) {
            try {
                floatingText.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        floatingTexts.clear();
    }


    public interface FloatingText{
        String getText();

        void spawn();
        void spawnFor(Player player);
        void remove();
    }

}