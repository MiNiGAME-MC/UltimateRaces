package net.ryu.ultimateraces.utils.message;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Messages {
    HELP("messages.help-information"),
    NO_PERMS("messages.insufficient-permissions"),
    CONFIGURATION_RELOAD("messages.configuration-reload"),
    CHAT_ENABLE("messages.chat-enabled"),
    CHAT_DISABLE("messages.chat-disabled"),
    SPAWN("messages.spawn"),
    SET_SPAWN("messages.set-spawn"),
    FRIENDLY_FIRE_ENABLE("messages.friendly-fire-enable"),
    FRIENDLY_FIRE_DISABLE("messages.friendly-fire-disable"),
    SENDER_GIVE("messages.sender-give"),
    RECEIVER_GIVE("messages.receiver-give"),
    SET_RACE("messages.set-race"),
    RACE_CHANGED("messages.race-changed"),
    RETURNED_SOUL_GEM("messages.returned-soulgem"),
    ITEM_DROPPED("messages.item-dropped"),
    INVALID_RACE("messages.invalid-race"),
    HARM_OWN_RACE("messages.harm-own-race"),
    INVENTORY_FULL("messages.inventory-full");

    private String path;
    private YamlConfiguration file;
    {
        UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);
        file = plugin.getMessages().getConfiguration();
    }

    Messages(String path) {
        this.path = path;
    }

    public void send(CommandSender sender, String... replace) {
        if (file.get(path) != null) {
            if (!(file.getStringList(path).isEmpty())) {
                String message = "";
                for (String string : file.getStringList(path)) {
                    for (int i = 0; i < replace.length; i += 2) {
                        String toReplace = replace[i];
                        String replaceWith = replace[i + 1];
                        if (string.contains(toReplace)) {
                            message = Txt.parse(string.replace(toReplace, replaceWith));
                        }
                    }
                    sender.sendMessage(message);
                }
            } else {
                String message = file.getString(path);
                if (message != null) {
                    for (int i = 0; i < replace.length; i += 2) {
                        String toReplace = replace[i];
                        String replaceWith = replace[i + 1];
                        message = Txt.parse(message.replace(toReplace, replaceWith));
                    }
                    sender.sendMessage(message);
                }
            }
        }
    }

    public void send(CommandSender sender) {
        if (file.get(path) != null) {
            if (!(file.getStringList(path).isEmpty())) {
                for (String string : file.getStringList(path)) {
                    sender.sendMessage(Txt.parse(string));
                }
            } else {
                String message = file.getString(path);
                if (message != null) sender.sendMessage(Txt.parse(message));
            }
        }
    }
}
