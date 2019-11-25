package net.ryu.ultimateraces.utils.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemCreator {
    private final ItemStack item;
    private UltimateRacesPlugin plugin;
    { plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class); }

    public ItemCreator(@NonNull String parsable) {
        if (plugin.getServer().getVersion().contains("1.8") || plugin.getServer().getVersion().contains("1.7") ||
                plugin.getServer().getVersion().contains("1.9") || plugin.getServer().getVersion().contains("1.10") ||
                plugin.getServer().getVersion().contains("1.11") || plugin.getServer().getVersion().contains("1.12")) {
            if (parsable != null) {
                parsable = parsable.toUpperCase().replace(" ", "_");
                if (parsable.contains(":")) {
                    String[] split = parsable.split(":");
                    item = new ItemStack(Material.getMaterial(split[0]));
                    try {
                        item.setDurability(Byte.parseByte(split[1]));
                    } catch (Exception ignored) {}
                } else {
                    item = new ItemStack(XMaterial.fromString(parsable).parseMaterial());
                }
            } else {
                throw new IllegalArgumentException("parsable can't be null");
            }
        } else {
            if (parsable != null) {
                item = new ItemStack(XMaterial.fromString(parsable).parseMaterial());
            } else {
                throw new IllegalArgumentException("parsable can't be null");
            }
        }
    }

    public ItemCreator withDisplayName(String name) {
        return name != null ? this.withMeta(meta -> meta.setDisplayName(Txt.parse(name))) : this;
    }

    public ItemCreator withLore(List<String> lore) {
        return lore != null ? this.withMeta(meta -> meta.setLore(Txt.parse(lore))) : this;
    }

    public ItemCreator withSkull(String owner) {
        return owner == null ? this : this.withMeta(meta -> {
            if (meta instanceof SkullMeta) ((SkullMeta) meta).setOwner(owner);
        });
    }

    public ItemCreator withSpawner(String type) {
        return type == null ? this : this.withMeta(meta -> {
            if (meta instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
                BlockState blockState = blockStateMeta.getBlockState();
                CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;

                creatureSpawner.setSpawnedType(EntityType.valueOf(type));
                blockStateMeta.setBlockState(blockState);
            }
        });
    }

    public ItemCreator withUnbreakable(boolean unbreakable) {
        return unbreakable ? withMeta(meta -> meta.setUnbreakable(true)) : this;
    }

    public ItemCreator withEnchantments(List<String> enchantments) {
        return enchantments != null ? withMeta(meta -> {
            enchantments.forEach(enchants -> {
                if (!(enchants.contains(":")))
                    throw new IllegalArgumentException("The enchantment " + enchants + " is missing the level parameter!");
                String[] split = enchants.split(":");
                meta.addEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1]), true);
            });
        }) : this;
    }

    public ItemCreator withEffects(List<String> effects) {
        return effects != null ? withMeta(meta -> {
            if (meta instanceof PotionMeta) {
                effects.forEach(effect -> {
                    if (!(effect.contains(":")))
                        throw new IllegalArgumentException("The effect " + effect + " is missing either the duration or amplifier, possibly both!");
                    String[] split = effect.split(":");
                    ((PotionMeta)meta).addCustomEffect(new PotionEffect(PotionEffectType.getByName(split[0].toUpperCase()), Integer.parseInt(split[1]), Integer.parseInt(split[2])), true);
                });
            }
        }) : this;
    }

    public ItemCreator withColour(String colour) {
        return colour != null ? withMeta(meta -> {
            if (!(colour.contains(":"))) throw new IllegalArgumentException("It seems as though you're missing a few colour variables!");
            String[] split = colour.split(":");
            if (meta instanceof LeatherArmorMeta) ((LeatherArmorMeta)meta).setColor(Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])));
            if (meta instanceof PotionMeta) ((PotionMeta)meta).setColor(Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])));
        }) : this;
    }

    public ItemCreator withTexture(String url) {
        return url != null ? withMeta(meta -> {
            if (meta instanceof SkullMeta) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", url));
                Field profileField = null;
                try {
                    profileField = meta.getClass().getDeclaredField("profile");
                } catch (NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
                assert profileField != null;
                profileField.setAccessible(true);
                try {
                    profileField.set(meta, profile);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }) : this;
    }

    public ItemCreator withFlags(List<String> flags) {
        return flags != null ? withMeta(meta -> {
            flags.forEach(flag -> {
                if (ItemFlag.valueOf(flag.toUpperCase()) == null)
                    throw new IllegalArgumentException("The flag " + flag.toUpperCase() + " doesn't seem to exist!");
                meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
            });
        }) : this;
    }

    public ItemCreator withAmount(int amount) {
        if (amount != -1) item.setAmount(amount);
        return this;
    }

    public ItemStack toItemStack() {
        return this.item;
    }

    public static ItemCreator fromConfig(ConfigurationSection section) {
        if (!(section.contains("id"))) throw new IllegalArgumentException("Path does not contain a material.");
        return new ItemCreator(section.getString("id"))
                .withDisplayName(section.getString("display"))
                .withLore(section.getStringList("lore"))
                .withAmount(section.getInt("amount", 1))
                .withSkull(section.getString("skull"))
                .withSpawner(section.getString("spawner"))
                .withEnchantments(section.getStringList("enchants"))
                .withEffects(section.getStringList("effects"))
                .withUnbreakable(section.getBoolean("unbreakable"))
                .withColour(section.getString("colour"))
                .withFlags(section.getStringList("flags"))
                .withTexture(section.getString("texture"));
    }

    public static ItemCreator fromConfig(UltimateRacesPlugin plugin, String path) {
        return fromConfig(plugin.getConfigs().getConfiguration().getConfigurationSection(path));
    }

    private ItemCreator withMeta(Consumer<? super ItemMeta> applier) {
        ItemMeta meta = item.getItemMeta();
        applier.accept(meta);
        item.setItemMeta(meta);
        return this;
    }

    public static boolean isInventoryFull(ItemStack itemStack, int amount, Player player) {
        int i = amount;
        try {
            for (ItemStack item : (Txt.isNewerVersion() ? player.getInventory().getStorageContents() : player.getInventory().getContents())) {
                if (itemStack != null) {
                    if (item != null) {
                        if (item.isSimilar(itemStack)) {
                            i = i - (item.getMaxStackSize() - item.getAmount());
                        }
                    } else {
                        i = i - itemStack.getMaxStackSize();
                    }
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return (i > 0);
    }
}