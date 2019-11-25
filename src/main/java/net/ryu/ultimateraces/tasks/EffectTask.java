package net.ryu.ultimateraces.tasks;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.Race;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectTask extends BukkitRunnable {
    private UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    @Override
    public void run() {
        for (Player players : plugin.getServer().getOnlinePlayers()) {
            String race = Txt.getRace(players.getUniqueId());
            if (race != null) {
                Race races = plugin.getRaceManager().getRace(race);
                if (!(races.getEffects().isEmpty()))
                    races.getEffects().entrySet().stream().map(effect -> new PotionEffect(PotionEffectType.getByName(effect.getKey().getName()), Integer.MAX_VALUE, effect.getValue(), false)).forEach(players::addPotionEffect);
            }
        }
    }
}
