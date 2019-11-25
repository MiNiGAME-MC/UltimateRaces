package net.ryu.ultimateraces.utils.inventory.panels;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.inventory.Inventories;
import org.bukkit.configuration.file.FileConfiguration;

public class Creator extends Inventories {
    private UltimateRacesPlugin plugin;
    private FileConfiguration configuration;

    public Creator(UltimateRacesPlugin plugin, String race) {
        super(9*4, "Race: " + race);
        this.plugin = plugin;
        configuration = plugin.getConfigs().getConfiguration();
        Integer[] glass1 = {0,4,8,27,31,35};
        Integer[] glass2 = {1,2,3,5,6,7,20,24,28,29,30,32,33,34};
        Integer[] glass3 = {9,11,13,15,16,17,18,19,21,23,25,26};
    }
}
