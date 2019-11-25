package net.ryu.ultimateraces.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PlaceholderAPI extends PlaceholderExpansion {
    private UltimateRacesPlugin plugin;
    { plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class); }

    @Override
    public String getIdentifier() {
        return "race";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public List<String> getPlaceholders() {
        return Arrays.asList(new String[] { "%race%", "%tag%" });
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) throw new IllegalArgumentException("Sorry, but it seems as though the player is unknown!");
        switch (identifier.toLowerCase()) {
            case "race":
                return (Txt.getRace(player.getUniqueId()) != null ? Txt.getRace(player.getUniqueId()) : " ");
            case "tag":
                return (plugin.getRaceManager().getRace(Txt.getRace(player.getUniqueId())).getTag() != null ? plugin.getRaceManager().getRace(Txt.getRace(player.getUniqueId())).getTag() : " ");
        }
        return null;
    }
}
