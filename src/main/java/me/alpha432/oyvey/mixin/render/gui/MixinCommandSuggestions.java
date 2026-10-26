package me.alpha432.oyvey.mixin.render.gui;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.manager.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {
    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    boolean keepSuggestions;

    @Shadow
    private boolean allowSuggestions;

    @Shadow
    @Final
    EditBox input;

    @Shadow
    public abstract void showSuggestions(boolean immediateNarration);

    @Inject(method = "updateCommandInfo", at = @At("RETURN"))
    private void oyvey$updateCommandInfo(CallbackInfo ci) {
        String prefix = OyVey.commandManager.getCommandPrefix();
        String value = input.getValue();

        if (prefix.isEmpty() || !value.startsWith(prefix)) {
            return;
        }

        StringReader reader = new StringReader(value);
        reader.setCursor(Math.min(prefix.length(), value.length()));

        ParseResults<CommandManager> parse = OyVey.commandManager.getDispatcher()
                .parse(reader, OyVey.commandManager);

        pendingSuggestions = OyVey.commandManager.getDispatcher()
                .getCompletionSuggestions(parse, Math.max(prefix.length(), input.getCursorPosition()));

        if (!keepSuggestions) {
            suggestions = null;
        }

        if (allowSuggestions && Minecraft.getInstance().options.autoSuggestions().get()) {
            showSuggestions(false);
        }
    }
}
