package net.ryu.ultimateraces.listener;

import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.utils.item.ItemCreator;
import net.ryu.ultimateraces.utils.message.Txt;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoulGemListener implements Listener {
    private UltimateRacesPlugin plugin = UltimateRacesPlugin.getPlugin(UltimateRacesPlugin.class);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player player = event.getPlayer();
        ItemStack hand = getItemInMainHand(player);
        if (hand != null) {
            ItemStack itemStack = ItemCreator.fromConfig(plugin, "soul-gem").toItemStack();
            if (itemStack.isSimilar(hand)) {
                event.setCancelled(true);
                plugin.soulGem.add(player.getUniqueId());
                if (hand.getAmount() > 1) {
                    hand.setAmount(hand.getAmount() - 1);
                    player.updateInventory();
                } else {
                    setItemInMainHand(player, new ItemStack(Material.AIR));
                    player.updateInventory();
                }
                plugin.getSelection().open(player);
            }
        }
    }

    private void setItemInMainHand(Player player, ItemStack itemStack) {
        if (Txt.isNewerVersion())
            player.getInventory().setItemInMainHand(itemStack);
        else
            player.setItemInHand(itemStack);
    }

    private ItemStack getItemInMainHand(Player player) {
        if (Txt.isNewerVersion())
            return player.getInventory().getItemInMainHand();
        else
            return player.getItemInHand();
    }
}
