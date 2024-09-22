package me.auvq.marketplace.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CC {

    private static final Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

    public static String color(String message) {
        return toColorHex(message);
    }

    public static List<String> color(List<String> message) {
        return message.stream().map(CC::color).collect(Collectors.toList());
    }

    public static List<String> translate(List<String> strings) {
        return strings.stream().map(CC::translate).collect(Collectors.toList());
    }

    public static String translate(String string) {
        Matcher match = pattern.matcher(string);
        while (match.find()) {
            String color = string.substring(match.start() + 1, match.end());
            string = string.replace("&" + color, net.md_5.bungee.api.ChatColor.of(color) + "");
            match = pattern.matcher(string);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String toColorHex(String message) {
        Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.valueOf(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = message.substring(0, matcher.start());
            final String after = message.substring(matcher.end());
            message = before + hexColor + after;
            matcher = hexPattern.matcher(message);
        }
        return CC.translate(message);
    }
}
