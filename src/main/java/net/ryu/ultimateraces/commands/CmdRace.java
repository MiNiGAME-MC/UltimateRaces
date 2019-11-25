package net.ryu.ultimateraces.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.inventory.panels.Selection;
import net.ryu.ultimateraces.utils.item.ItemCreator;
import net.ryu.ultimateraces.utils.message.Messages;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("race|races")
public class CmdRace extends BaseCommand {
    private UltimateRacesPlugin plugin;
    { plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class); }

    @HelpCommand
    public void init(CommandSender sender) {
        if (!(sender.hasPermission("races.chat")) && !(sender.hasPermission("races.*")) && !(sender.getName().equalsIgnoreCase("RyujiX"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        Messages.HELP.send(sender);
        if (sender.getName().equalsIgnoreCase("RyujiX"))
            sender.sendMessage(Txt.parse(" \n" +
                    "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&l【 &e&lUltimate Races &8&l】&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬" + "\n" +
                "  &7▪ &fResource Version&e: " + plugin.getDescription().getVersion() + "\n" +
                "  &7▪ &fResource Author&e: RyujiX" + "\n" +
                "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
            ));
    }

    @Subcommand("set")
    @CommandCompletion("@players @races")
    public void setRace(CommandSender sender, OnlinePlayer player, String race) {
        if (!(sender.hasPermission("races.setrace")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        Player p = player.getPlayer();
        String previousRace = plugin.getUserRegistry().getUser(player.getPlayer().getUniqueId()).getRace();
        if (plugin.getRaces().getConfiguration().getConfigurationSection("races").getKeys(false).contains(race)) {
            Messages.SET_RACE.send(sender, "%player%", p.getName(), "%previous-race%", previousRace, "%new-race%", race);
            Messages.RACE_CHANGED.send(p, "%sender%", sender.getName(), "%previous-race%", previousRace, "%new-race%", race);
            plugin.getUserRegistry().getUser(player.getPlayer().getUniqueId(), data -> {
                plugin.getRaceManager().getRace(previousRace).getEffects().keySet().stream().map(effect -> PotionEffectType.getByName(effect.getName())).forEach(p::removePotionEffect);
                data.setRace(race);
                plugin.getRaceManager().getRace(race).getEffects().entrySet().forEach(effect -> p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect.getKey().getName()), Integer.MAX_VALUE, effect.getValue()), false));
            });
            plugin.getUserRegistry().saveUser(player.getPlayer().getUniqueId());
        } else {
            Messages.INVALID_RACE.send(sender);
        }
    }

    @Subcommand("chat")
    public void chat(CommandSender sender) {
        if (!(sender.hasPermission("races.chat")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        Player player = (Player) sender;
        String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
        if (race != null) {
            boolean chat = plugin.getUserRegistry().getUser(player.getUniqueId()).isChat();
            plugin.getUserRegistry().getUser(player.getUniqueId()).setChat(!(chat));
            if (!(chat)) Messages.CHAT_ENABLE.send(player, "%race%", race);
            else Messages.CHAT_DISABLE.send(player, "%race%", race);
        } else {
            player.sendMessage(Txt.parse("&cSorry, please reconnect to the server and select a race!"));
        }
    }

    @Subcommand("setspawn")
    @CommandCompletion("@races")
    public void setSpawn(CommandSender sender, String race) {
        if (!(sender.hasPermission("races.setspawn")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        if (plugin.getRaces().getConfiguration().getConfigurationSection("races").getKeys(false).contains(race)) {
            plugin.getRaces().getConfiguration().getConfigurationSection("races").getKeys(false).stream().filter(races -> races.equalsIgnoreCase(race)).forEach(races -> {
                plugin.getRaces().getConfiguration().set("races." + races + ".spawn-point", Txt.serializeLocation(((Player) sender).getLocation()));
                plugin.getRaceManager().getRace(races).setSpawn(Txt.serializeLocation(((Player) sender).getLocation()));
                plugin.getRaces().save();
                Messages.SET_SPAWN.send(sender, "%race%", races);
            });
        } else {
            Messages.INVALID_RACE.send(sender);
        }
    }

    @Subcommand("spawn")
    public void spawn(CommandSender sender) {
        if (!(sender.hasPermission("races.spawn")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        Player player = (Player) sender;
        String race = plugin.getUserRegistry().getUser(player.getUniqueId()).getRace();
        if (race != null) {
            Location location = Txt.getLocation(plugin.getRaceManager().getRace(race).getSpawn().split(";"));
            if (location != null) {
                Messages.SPAWN.send(player, "%race%", race);
                player.teleport(location);
            }
        } else {
            player.sendMessage(Txt.parse("&cSorry, please reconnect to the server and select a race!"));
        }
    }

    @Subcommand("friendlyfire")
    public void friendlyFire(CommandSender sender) {
        if (!(sender.hasPermission("races.friendlyfire")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        boolean friendlyFire = plugin.getConfigs().getConfiguration().getBoolean("races-settings.friendly-fire");
        plugin.getConfigs().getConfiguration().set("races-settings.friendly-fire", !(friendlyFire));
        plugin.getConfigs().save();
        if (friendlyFire) Messages.FRIENDLY_FIRE_ENABLE.send(sender);
        else Messages.FRIENDLY_FIRE_DISABLE.send(sender);
    }

    @Subcommand("givegem")
    @CommandCompletion("@players @amount")
    public void giveGem(CommandSender sender, OnlinePlayer player, @Default("1") Integer amount) {
        if (!(sender.hasPermission("races.givegem")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        ItemStack itemStack = ItemCreator.fromConfig(plugin, "soul-gem").toItemStack();
        itemStack.setAmount(amount);
        Messages.SENDER_GIVE.send(sender, "%player%", player.getPlayer().getName(), "%amount%", Txt.fixInteger(amount));
        Messages.RECEIVER_GIVE.send(player.getPlayer(), "%sender%", sender.getName(), "%amount%", Txt.fixInteger(amount));
        if (ItemCreator.isInventoryFull(itemStack, itemStack.getAmount(), player.getPlayer())) {
            Messages.INVENTORY_FULL.send(player.getPlayer());
            Messages.ITEM_DROPPED.send(player.getPlayer());
            player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), itemStack);
            return;
        }
        player.getPlayer().getInventory().addItem(itemStack);
    }

//    @Subcommand("create")
//    public void create(CommandSender sender, String race) {
//
//    }
//
//    @Subcommand("edit")
//    @CommandCompletion("@races")
//    public void edit(CommandSender sender, String race) {
//
//    }

    @Subcommand("reload")
    public void reload(CommandSender sender) {
        if (!(sender.hasPermission("races.reload")) && !(sender.hasPermission("races.*"))) {
            Messages.NO_PERMS.send(sender);
            return;
        }
        plugin.getRaces().reload();
        plugin.getConfigs().reload(); plugin.getMessages().reload();
        plugin.refresh();
        plugin.getSelection().loadPanelSelection(); plugin.getConfirmation().loadButtons();
        plugin.getRaceManager().loadRaces(plugin);
        Messages.CONFIGURATION_RELOAD.send(sender);
    }
}
