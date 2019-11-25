package net.ryu.ultimateraces.listener;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
        String newMessage = Txt.parse(plugin.getRaces().getConfiguration().getString("races." + race + ".chat-format")
                .replace("%player%", player.getName())
                .replace("%player_display%", player.getDisplayName())
                .replace("%message%", event.getMessage()));
        if (plugin.getUserRegistry().getUser(player.getUniqueId()).isChat()) {
            event.setCancelled(true);
            plugin.getServer().getOnlinePlayers().stream().filter(players -> plugin.getUserRegistry().getUser(players.getUniqueId()).getRace().equals(race)).forEach(players -> players.sendMessage(newMessage));
        }
        String format = event.getFormat();
        event.setFormat(format.replace("{race_tag}", Txt.parse(plugin.getRaceManager().getRace(race).getTag())));
    }
}
