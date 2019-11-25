package net.ryu.ultimateraces.managers;

import lombok.Getter;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.Race;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RaceManager {
    private Map<String, Race> races;

    public RaceManager() {
        races = new HashMap<>();
    }

    public void loadRaces(UltimateRacesPlugin plugin) {
        FileConfiguration file = plugin.getRaces().getConfiguration();
        for (String input : file.getConfigurationSection("races").getKeys(false)) {
            Race race = new Race();
            if (!(file.getStringList("races." + input + ".effects").isEmpty())) {
                Map<PotionEffectType, Integer> effects = new HashMap();
                file.getStringList("races." + input + ".effects").forEach(effect -> {
                    String[] split = effect.split(":");
                    PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
                    if (type == null)
                        throw new IllegalArgumentException("Sorry, but the effect " + effect + " doesn't exist!");
                    effects.put(type, Integer.parseInt(split[1]));
                });
                race.setEffects(effects);
            }
            if (!(file.getStringList("races." + input + ".commands").isEmpty())) race.setCommands(file.getStringList("races." + input + ".commands"));
            if (file.getString("races." + input + ".tag") != null) race.setTag(file.getString("races." + input + ".tag"));
            if (file.getString("races." + input + ".spawn-point") != null) race.setSpawn(file.getString("races." + input + ".spawn-point"));
            races.put(input, race);
        }
    }

    public Race getRace(String race) {
        return races.get(race);
    }
}
