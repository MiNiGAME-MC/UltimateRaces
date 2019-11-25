package net.ryu.ultimateraces.listener;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ConnectionListener implements Listener {
    private UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getUserRegistry().getUser(event.getPlayer().getUniqueId(), data -> {
            if (data.getRace() == null) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getSelection().open(event.getPlayer()), 20L);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = event.getPlayer().getUniqueId();
            plugin.getUserRegistry().saveUser(uuid);
        });
        Txt.returnSoulGem(event.getPlayer());
        plugin.soulGem.remove(event.getPlayer().getUniqueId());
    }
}
