package net.ryu.ultimateraces;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.ryu.ultimateraces.api.UltimateRacesAPI;
import net.ryu.ultimateraces.commands.CmdRace;
import net.ryu.ultimateraces.listener.ChatListener;
import net.ryu.ultimateraces.listener.ConnectionListener;
import net.ryu.ultimateraces.listener.PlayerListener;
import net.ryu.ultimateraces.listener.SoulGemListener;
import net.ryu.ultimateraces.managers.FileManager;
import net.ryu.ultimateraces.managers.PlaceholderAPI;
import net.ryu.ultimateraces.managers.RaceManager;
import net.ryu.ultimateraces.managers.UserRegistry;
import net.ryu.ultimateraces.tasks.EffectTask;
import net.ryu.ultimateraces.utils.inventory.InventoryManager;
import net.ryu.ultimateraces.utils.inventory.panels.Confirmation;
import net.ryu.ultimateraces.utils.inventory.panels.Selection;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
public class UltimateRacesPlugin extends JavaPlugin {
    private FileManager configs, messages, races;
    private UserRegistry userRegistry;
    private RaceManager raceManager;
    private Selection selection;
    private Confirmation confirmation;
    public List<UUID> closeInventory, soulGem;

    @Override
    public void onEnable() {
        registerFiles();
        registerListeners(new ConnectionListener(), new PlayerListener(), new ChatListener(), new SoulGemListener());
        registerCommand();
        registerPlaceholderAPI();
        InventoryManager.register(this);
        refresh();
        selection = new Selection(this);
        selection.loadPanelSelection();
        confirmation.loadButtons();
        raceManager = new RaceManager();
        raceManager.loadRaces(this);
        getServer().getServicesManager().register(UltimateRacesAPI.class,
                this.userRegistry,
                this,
                ServicePriority.Highest
        );
        closeInventory = new ArrayList<>();
        soulGem = new ArrayList<>();
        new EffectTask().runTaskTimer(this, 60L, 120L);
    }

    public void refresh() {
        selection = new Selection(this);
        confirmation = new Confirmation(this);
    }

    private void registerPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPI placeholderAPI = new PlaceholderAPI();
            placeholderAPI.register();
        }
    }

    public boolean mcMMOEnabled() {
        return (Bukkit.getPluginManager().getPlugin("mcMMO") != null);
    }

    private void registerCommand() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CmdRace());
        commandManager.getCommandCompletions().registerCompletion("races", race -> races.getConfiguration().getConfigurationSection("races").getKeys(false));
        commandManager.getCommandCompletions().registerCompletion("amount", amount -> Arrays.asList("1", "2", "4", "8", "16", "32", "64"));
    }

    private void registerListeners(Listener... listeners) {
        Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerFiles() {
        configs = new FileManager(this, "config", getDataFolder().getAbsolutePath());
        messages = new FileManager(this, "message_en", getDataFolder().getAbsolutePath());
        races = new FileManager(this, "races", getDataFolder().getAbsolutePath());
        userRegistry = new UserRegistry(this);
        userRegistry.init();
    }

    @Override
    public void onDisable() {
        this.userRegistry.getUsers().forEach((uuid, user) -> this.userRegistry.saveUser(uuid));
        closeInventory.clear();
        soulGem.clear();
    }
}
