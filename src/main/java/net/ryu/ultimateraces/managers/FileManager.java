package net.ryu.ultimateraces.managers;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FileManager {
    private File file;
    private String name;
    private UltimateRacesPlugin plugin;
    private YamlConfiguration configuration;
    {
        this.plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);
    }

    public FileManager(JavaPlugin plugin, String name, String directory) {
        this.name = name;
        file = new File(directory, name + ".yml");
        if (!file.exists()) {
            plugin.saveResource(name + ".yml", false);
        }
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            configuration.load(this.file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
    }

    public YamlConfiguration getConfiguration() {
        return configuration;
    }
}
