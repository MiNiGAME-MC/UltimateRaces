package net.ryu.ultimateraces.listener;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.inventory.Inventories;
import net.ryu.ultimateraces.utils.message.Messages;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    private UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        boolean friendlyFire = plugin.getConfigs().getConfiguration().getBoolean("races-settings.friendly-fire");
        if (!(friendlyFire)) {
            if (event.getDamager() instanceof Player) {
                if (event.getEntity() instanceof Player) {
                    Player attacker = (Player) event.getDamager();
                    Player victim = (Player) event.getEntity();
                    if (Txt.getRace(attacker.getUniqueId()).equals(Txt.getRace(victim.getUniqueId()))) {
                        event.setCancelled(true);
                        Messages.HARM_OWN_RACE.send(attacker);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
        if (race != null) {
            Location location = Txt.getLocation(plugin.getRaces().getConfiguration().getString("races." + race + ".spawn-point").split(";"));
            event.setRespawnLocation(location);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        if (!(plugin.getConfigs().getConfiguration().getBoolean("races-settings.override-spawn-command"))) return;
        Player player = event.getPlayer();
        String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
        if (race != null) {
            if (event.getMessage().equalsIgnoreCase("/spawn")) {
                event.setCancelled(true);
                Location location = Txt.getLocation(plugin.getRaceManager().getRace(race).getSpawn().split(";"));
                Messages.SPAWN.send(player, "%race%", race);
                player.teleport(location);
            }
        }
    }

    int i = 0;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof Inventories)) return;
        Player player = (Player) event.getPlayer();
        if (plugin.closeInventory.contains(player.getUniqueId())) return;
        if (plugin.soulGem.contains(player.getUniqueId())) {
            Txt.returnSoulGem(player);
            plugin.soulGem.remove(player.getUniqueId());
            return;
        }
        if (plugin.getConfigs().getConfiguration().getBoolean("races-settings.force-selection")) {
            String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
            if (race == null) {
                if (!(player.isOp())) {
                    Inventories inv = (Inventories) event.getInventory().getHolder();
                    Bukkit.getScheduler().runTask(plugin, () -> inv.open(player));
                }
            }
        }
    }
}
