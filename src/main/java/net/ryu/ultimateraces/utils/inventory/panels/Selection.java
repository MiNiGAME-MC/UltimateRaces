package net.ryu.ultimateraces.utils.inventory.panels;

import com.gmail.nossr50.api.ExperienceAPI;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.GSound;
import net.ryu.ultimateraces.utils.inventory.Inventories;
import net.ryu.ultimateraces.utils.item.ItemCreator;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Selection extends Inventories {
    private Map<String, Integer> races;
    private UltimateRacesPlugin plugin;
    private FileConfiguration configuration;

    public Selection(UltimateRacesPlugin plugin) {
        super(Txt.roundUpToNine(plugin.getConfigs().getConfiguration().getInt("races-panels.selection.size")), Txt.parse(plugin.getConfigs().getConfiguration().getString("races-panels.selection.title")));
        this.plugin = plugin;
        configuration = plugin.getConfigs().getConfiguration();
        races = new HashMap<>();
        configuration.getConfigurationSection("races-panels.selection.items").getKeys(false).forEach(section -> {
            int slot = (configuration.get("races-panels.selection.items." + section + ".slot") != null ? configuration.getInt("races-panels.selection.items." + section + ".slot") : getInventory().firstEmpty());
            setItem(slot, ItemCreator.fromConfig(plugin, "races-panels.selection.items." + section + ".item").toItemStack(), e -> {
                ItemStack itemStack = e.getCurrentItem();
                Player player = (Player) e.getWhoClicked();
                if (get(slot) == null) return;
                if (configuration.getBoolean("races-settings.use-confirmation-pane")) {
                    plugin.getConfirmation().setItem(plugin.getConfirmation().getSlot(), itemStack);
                    plugin.getConfirmation().getRace(get(slot));
                    plugin.closeInventory.add(e.getWhoClicked().getUniqueId());
                    plugin.getConfirmation().open(player);
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.closeInventory.remove(e.getWhoClicked().getUniqueId()));
                } else {
                    plugin.getUserRegistry().getUser(player.getUniqueId(), data -> {
                        String previousRace = (plugin.getUserRegistry().getUser(player.getUniqueId()).getRace());
                        if (previousRace != null)
                            plugin.getRaceManager().getRace(previousRace).getEffects().keySet().stream().map(effect -> PotionEffectType.getByName(effect.getName())).forEach(player::removePotionEffect);
                        data.setRace(get(slot));
                        plugin.soulGem.remove(player.getUniqueId());
                        plugin.getRaceManager().getRace(get(slot)).getCommands().forEach(commands -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commands.replace("%player%", player.getName())));
                        plugin.getServer().getOnlinePlayers().forEach(players -> Txt.send(plugin.getRaces().getConfiguration(), "races." + get(slot) + ".broadcast-message", players));
                        Location location = Txt.getLocation(plugin.getRaceManager().getRace(get(slot)).getSpawn().split(";"));
                        if (location != null)
                            player.teleport(location);
                        if (plugin.getConfigs().getConfiguration().getBoolean("races-settings.fireworks"))
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                                FireworkMeta meta = firework.getFireworkMeta();
                                meta.addEffect(Txt.getRandomEffect());
                                firework.setFireworkMeta(meta);
                            }, 20L);
                        if (plugin.mcMMOEnabled()) {
                            if (plugin.getRaces().getConfiguration().get("races." + get(slot) + ".mcMMO-boost") != null) {
                                plugin.getRaces().getConfiguration().getStringList("races." + get(slot) + ".mcMMO-boost").stream().map(boost -> boost.split(":")).forEach(split -> {
                                    String skill = split[0];
                                    int levels = Integer.parseInt(split[1]);
                                    ExperienceAPI.addLevel(player, skill.toUpperCase(), levels);
                                });
                            }
                        }
                        if (plugin.getConfigs().getConfiguration().get("races.settings.sounds.select") != null)
                            player.playSound(player.getLocation(), GSound.valueOf(plugin.getConfigs().getConfiguration().getString("races.settings.sounds.select.effect")).parseSound(),
                                    plugin.getConfigs().getConfiguration().getInt("races.settings.sounds.select.volume"),
                                    plugin.getConfigs().getConfiguration().getInt("races.settings.sounds.select.pitch"));
                        plugin.getRaceManager().getRace(get(slot)).getEffects().entrySet().forEach(effect -> player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getKey().getName()), Integer.MAX_VALUE, effect.getValue()), false));
                    });
                    plugin.getUserRegistry().saveUser(player.getUniqueId());
                    player.closeInventory();
                }
            });
        });
    }

    public void loadPanelSelection() {
        races.clear();
        Objects.requireNonNull(configuration.getConfigurationSection("races-panels.selection.items")).getKeys(false).stream().filter(section -> configuration.get("races-panels.selection.items." + section + ".race") != null).forEach(section -> races.put(configuration.getString("races-panels.selection.items." + section + ".race"), configuration.getInt("races-panels.selection.items." + section + ".slot")));
    }

    private String get(int slot) {
        for (Map.Entry<String, Integer> entry : races.entrySet()) {
            if (entry.getValue().equals(slot)) return entry.getKey();
        }
        return null;
    }
}