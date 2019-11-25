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
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Confirmation extends Inventories {
    private Map<Integer, String> button;
    private UltimateRacesPlugin plugin;
    private FileConfiguration configuration;
    private int s;
    private String race;

    public Confirmation(UltimateRacesPlugin plugin) {
        super(Txt.roundUpToNine(plugin.getConfigs().getConfiguration().getInt("races-panels.confirmation.size")), Txt.parse(plugin.getConfigs().getConfiguration().getString("races-panels.confirmation.title")));
        this.plugin = plugin;
        configuration = plugin.getConfigs().getConfiguration();
        button = new HashMap<>();
        Objects.requireNonNull(configuration.getConfigurationSection("races-panels.confirmation.items")).getKeys(false).forEach(section -> {
            int slot = (configuration.get("races-panels.confirmation.items." + section + ".slot") != null ? configuration.getInt("races-panels.confirmation.items." + section + ".slot") : getInventory().firstEmpty());
            if (configuration.get("races-panels.confirmation.items." + section + ".button") != null && Objects.requireNonNull(configuration.getString("races-panels.confirmation.items." + section + ".button")).equalsIgnoreCase("race")) {
                s = slot;
            } else {
                setItem(slot, ItemCreator.fromConfig(plugin, "races-panels.confirmation.items." + section + ".item").toItemStack(), e -> {
                    if (get(slot) == null) return;
                    Player player = (Player) e.getWhoClicked();
                    if (get(slot).equalsIgnoreCase("accept")) {
                        plugin.getUserRegistry().getUser(player.getUniqueId(), data -> {
                            String previousRace = (plugin.getUserRegistry().getUser(player.getUniqueId()).getRace());
                            if (previousRace != null)
                                plugin.getRaceManager().getRace(previousRace).getEffects().keySet().stream().map(effect -> PotionEffectType.getByName(effect.getName())).forEach(player::removePotionEffect);
                            data.setRace(race);
                            plugin.soulGem.remove(e.getWhoClicked().getUniqueId());
                            plugin.getRaceManager().getRace(race).getCommands().forEach(commands -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commands.replace("%player%", player.getName())));
                            plugin.getServer().getOnlinePlayers().forEach(players -> Txt.send(plugin.getRaces().getConfiguration(), "races." + race + ".broadcast-message", players, "%player%", player.getName()));
                            Location location = Txt.getLocation(plugin.getRaceManager().getRace(race).getSpawn().split(";"));
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
                                if (plugin.getRaces().getConfiguration().get("races." + race + ".mcMMO-boost") != null) {
                                    plugin.getRaces().getConfiguration().getStringList("races." + race + ".mcMMO-boost").stream().map(boost -> boost.split(":")).forEach(split -> {
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
                            plugin.getRaceManager().getRace(race).getEffects().entrySet().forEach(effect -> player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getKey().getName()), Integer.MAX_VALUE, effect.getValue()), false));
                        });
                        plugin.getUserRegistry().saveUser(e.getWhoClicked().getUniqueId());
                        e.getWhoClicked().closeInventory();
                    }
                    if (get(slot).equalsIgnoreCase("deny")) {
                        plugin.closeInventory.add(e.getWhoClicked().getUniqueId());
                        plugin.getSelection().open((Player) e.getWhoClicked());
                        Bukkit.getScheduler().runTask(plugin, () -> plugin.closeInventory.remove(e.getWhoClicked().getUniqueId()));
                    }
                });
            }
        });
    }

    public void loadButtons() {
        button.clear();
        Objects.requireNonNull(configuration.getConfigurationSection("races-panels.confirmation.items")).getKeys(false).stream().filter(section -> configuration.get("races-panels.confirmation.items." + section + ".button") != null).forEach(section -> button.put(configuration.getInt("races-panels.confirmation.items." + section + ".slot"), configuration.getString("races-panels.confirmation.items." + section + ".button")));
    }

    public int getSlot() {
        return s;
    }

    public void getRace(String race) {
        this.race = race;
    }

    private String get(int slot) {
        for (Map.Entry<Integer, String> entry : button.entrySet()) {
            if (entry.getKey().equals(slot)) return entry.getValue();
        }
        return null;
    }
}