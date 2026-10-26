package me.alpha432.oyvey.util;

import me.alpha432.oyvey.OyVey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class TextUtil {
    private TextUtil() {
        throw new AssertionError("Can't create an instance of utility class");
    }

    public static void init() {
    }

    public static MutableComponent text(String content, Object... obj) {
        String formatted = (obj == null || obj.length == 0) ? content : String.format(content, obj);
        return parse(formatted);
    }

    private static MutableComponent parse(String input) {
        MutableComponent result = Component.empty();
        StringBuilder chunk = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == '{') {
                chunk.append('{');
                i++;
                continue;
            }

            if (c != '{') {
                chunk.append(c);
                continue;
            }

            int end = input.indexOf('}', i + 1);
            if (end == -1) {
                chunk.append(c);
                continue;
            }

            if (!chunk.isEmpty()) {
                result.append(Component.literal(chunk.toString()).withStyle(style));
                chunk.setLength(0);
            }

            style = applyTag(style, input.substring(i + 1, end));
            i = end;
        }

        if (!chunk.isEmpty()) {
            result.append(Component.literal(chunk.toString()).withStyle(style));
        }

        return result;
    }

    private static Style applyTag(Style style, String tag) {
        if (tag.isEmpty() || tag.equalsIgnoreCase("reset")) {
            return Style.EMPTY;
        }

        if (tag.equalsIgnoreCase("global")) {
            return style.withColor(TextColor.fromRgb(OyVey.colorManager.getColorAsInt()));
        }

        if (tag.startsWith("#") && (tag.length() == 7 || tag.length() == 9)) {
            try {
                String hex = tag.substring(1);
                int color = hex.length() == 6
                        ? 0xFF000000 | Integer.parseInt(hex, 16)
                        : (int) Long.parseLong(hex, 16);
                return style.withColor(TextColor.fromRgb(color));
            } catch (NumberFormatException ignored) {
                return style;
            }
        }

        if (tag.length() == 2 && tag.charAt(0) == '+') {
            return switch (tag.charAt(1)) {
                case 'k' -> style.withObfuscated(true);
                case 'l' -> style.withBold(true);
                case 'm' -> style.withStrikethrough(true);
                case 'n' -> style.withUnderlined(true);
                case 'o' -> style.withItalic(true);
                default -> style;
            };
        }

        ChatFormatting formatting = null;
        for (ChatFormatting value : ChatFormatting.values()) {
            if (value.toString().equalsIgnoreCase(tag)) {
                formatting = value;
                break;
            }
        }
        if (formatting == null) {
            return style;
        }

        if (TextColor.fromLegacyFormat(formatting) != null) {
            return style.withColor(formatting);
        }

        return switch (formatting) {
            case OBFUSCATED -> style.withObfuscated(true);
            case BOLD -> style.withBold(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case UNDERLINE -> style.withUnderlined(true);
            case ITALIC -> style.withItalic(true);
            default -> style;
        };
    }
}
