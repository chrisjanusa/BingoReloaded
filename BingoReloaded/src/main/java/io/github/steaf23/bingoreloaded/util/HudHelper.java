package io.github.steaf23.bingoreloaded.util;


import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import org.bukkit.entity.Player;

public class HudHelper {
    public static String getHudMessage(String hudFormat, GameTimer timer, Player player) {
        if (hudFormat.contains("{game_time}")) {
            hudFormat = hudFormat.replace("{game_time}", timer.getTimeString());
        }
        if (hudFormat.contains("{x}")) {
            hudFormat = hudFormat.replace("{x}", Integer.toString(player.getLocation().getBlockX()));
        }
        if (hudFormat.contains("{y}")) {
            hudFormat = hudFormat.replace("{y}", Integer.toString(player.getLocation().getBlockY()));
        }
        if (hudFormat.contains("{z}")) {
            hudFormat = hudFormat.replace("{z}", Integer.toString(player.getLocation().getBlockZ()));
        }
        if (hudFormat.contains("{biome}")) {
            hudFormat = hudFormat.replace("{biome}", getBiome(player));
        }
        if (hudFormat.contains("{direction}")) {
            hudFormat = hudFormat.replace("{direction}", getCardinalDirection(player));
        }
        if (hudFormat.contains("{world_time}")) {
            hudFormat = hudFormat.replace("{world_time}", getWorldTime(player));
        }
        return hudFormat;
    }

    private static String getWorldTime(Player player) {
        long tickCount = player.getWorld().getTime();
        long hours = (6 + tickCount/1000) % 25;
        if (hours > 12) {
            return (hours - 12) + " pm";
        } else {
            return hours + " am";
        }
    }

    private static String getBiome(Player player) {
        String biomeName = player.getLocation().getBlock().getBiome().name();
        StringBuilder prettyBiomeName = new StringBuilder();
        boolean firstLetterInWord = true;
        for (char letter : biomeName.toCharArray()) {
            if (letter == '_') {
                prettyBiomeName.append(' ');
                firstLetterInWord = true;
            } else if (firstLetterInWord) {
                prettyBiomeName.append(letter);
                firstLetterInWord = false;
            } else {
                prettyBiomeName.append(Character.toLowerCase(letter));
            }
        }
        return prettyBiomeName.toString();
    }

    public static String getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "N";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        } else {
            return "N";
        }
    }

}
