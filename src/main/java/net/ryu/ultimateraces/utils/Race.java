package net.ryu.ultimateraces.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

@Getter @Setter
public class Race {

    private Map<PotionEffectType, Integer> effects;
    private List<String> commands;
    private String tag;
    private String spawn;
}
