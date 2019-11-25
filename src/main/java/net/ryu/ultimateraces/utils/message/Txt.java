package net.ryu.ultimateraces.utils.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.item.ItemCreator;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Txt {
    private static UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    /**
     * Alternate the colour of the string you enter.
     * @param input = the string you wish to colour
     * @return
     */
    public static String parse(String input) {
        return input != null ? ChatColor.translateAlternateColorCodes('&', input) : null;
    }

    /**
     *
     * @param input
     * @return
     */
    public static List<String> parse(List<String> input) {
        return input.isEmpty() ? input : input.stream().map(Txt::parse).collect(Collectors.toList());
    }

    /**
     *
     * @param value
     * @return
     */
    public static String fixDoubleValue(double value) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setRoundingMode(RoundingMode.FLOOR);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(0);
        return numberFormat.format(value);
    }

    public static void msgAll(String... messages) {
        Bukkit.getOnlinePlayers().stream().forEach((o) -> {
            Arrays.stream(messages).forEach((s) -> {
                o.sendMessage(parse(s));
            });
        });
    }

    /**
     *
     * @param value
     * @return
     */
    public static String fixInteger(int value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(value);
    }

    /**
     *
     * @param value
     * @return
     */
    public static boolean isInteger(String value) {
        try {
            int i = Integer.parseInt(value);
        } catch (NumberFormatException exception) { return false; }
        return true;
    }

    /**
     *
     * @param slot
     * @return
     */
    public static int roundUpToNine(int slot) {
        if (slot < 9) return 9;
        if (slot < 18) return 18;
        if (slot < 27) return 27;
        if (slot < 36) return 36;
        if (slot < 45) return 45;
        return 54;
    }

    public static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     *
     * @param location
     * @return
     */
    public static String serializeLocation(Location location) {
        return location != null ? location.getWorld().getName() + ";" + fixDoubleValue(location.getX()) + ";"
                + fixDoubleValue(location.getY()) + ";" + fixDoubleValue(location.getZ()) +
                ";" + fixDoubleValue(location.getYaw()) + ";" + fixDoubleValue(location.getPitch()) : "";
    }

    /**
     *
     * @param strings
     * @return
     */
    public static Location getLocation(String[] strings) {
        Location location = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]), Integer.parseInt(strings[4]), Integer.parseInt(strings[5]));
        return location;
    }

    /**
     *
     * @param uuid
     * @return
     */
    public static String getRace(UUID uuid) {
        return plugin.getUserRegistry().getUser(uuid).getRace();
    }

    public static void send(FileConfiguration file, String path, CommandSender sender, String... replace) {
        if (file.get(path) != null) {
            if (!(file.getStringList(path).isEmpty())) {
                String message = "";
                for (String string : file.getStringList(path)) {
                    for (int i = 0; i < replace.length; i += 2) {
                        String toReplace = replace[i];
                        String replaceWith = replace[i + 1];
                        if (string.contains(toReplace)) {
                            message = Txt.parse(string.replace(toReplace, replaceWith));
                        }
                    }
                    sender.sendMessage(message);
                }
            } else {
                String message = file.getString(path);
                if (message != null) {
                    for (int i = 0; i < replace.length; i += 2) {
                        String toReplace = replace[i];
                        String replaceWith = replace[i + 1];
                        message = Txt.parse(message.replace(toReplace, replaceWith));
                    }
                    sender.sendMessage(message);
                }
            }
        }
    }

    public static FireworkEffect getRandomEffect() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int number = random.nextInt(4) + 1;
        List<String> types = Arrays.asList("BALL", "BALL_LARGE", "BURST", "CREEPER", "STAR");
        FireworkEffect.Type type = FireworkEffect.Type.valueOf(types.get(number));
        FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean())
                .withColor(getColor(random.nextInt(17) + 1)).withFade(getColor(random.nextInt(17) + 1))
                .with(type).trail(random.nextBoolean()).build();
        return effect;
    }

    private static String _versionString;

    public static boolean isNewerVersion() {
        return (getVersion() != null) && getVersion().contains("v1_9")
                || getVersion().contains("v1_10") || getVersion().contains("v1_11")
                || getVersion().contains("v1_12") || getVersion().contains("v1_13")
                || getVersion().contains("v1_14");
    }

    private static String getVersion() {
        if (_versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            _versionString = name.substring(name.lastIndexOf(46) + 1) + ".";
        }
        return _versionString;
    }

    public static void returnSoulGem(Player player) {
        if (plugin.soulGem.contains(player.getUniqueId())) {
            ItemStack itemStack = ItemCreator.fromConfig(plugin, "soul-gem").toItemStack();
            Messages.RETURNED_SOUL_GEM.send(player);
            if (ItemCreator.isInventoryFull(itemStack, itemStack.getAmount(), player)) {
                Messages.INVENTORY_FULL.send(player);
                Messages.ITEM_DROPPED.send(player);
                player.getPlayer().getWorld().dropItemNaturally(player.getLocation(), itemStack);
                return;
            }
            player.getInventory().addItem(itemStack);
        }
    }

    private static Color getColor(int i) {
        Color c = null;
        if (i == 1) {
            c = Color.AQUA;
        }
        if (i == 2) {
            c = Color.BLACK;
        }
        if (i == 3) {
            c = Color.BLUE;
        }
        if (i == 4) {
            c = Color.FUCHSIA;
        }
        if (i == 5) {
            c = Color.GRAY;
        }
        if (i == 6) {
            c = Color.GREEN;
        }
        if (i == 7) {
            c = Color.LIME;
        }
        if (i == 8) {
            c = Color.MAROON;
        }
        if (i == 9) {
            c = Color.NAVY;
        }
        if (i == 10) {
            c = Color.OLIVE;
        }
        if (i == 11) {
            c = Color.ORANGE;
        }
        if (i == 12) {
            c = Color.PURPLE;
        }
        if (i == 13) {
            c = Color.RED;
        }
        if (i == 14) {
            c = Color.SILVER;
        }
        if (i == 15) {
            c = Color.TEAL;
        }
        if (i == 16) {
            c = Color.WHITE;
        }
        if (i == 17) {
            c = Color.YELLOW;
        }
        return c;
    }
}
