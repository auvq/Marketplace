package me.auvq.marketplace.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.manager.MarketManager;
import me.auvq.marketplace.utils.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfirmationGUI extends ChestGui {

    private final Player player;
    private final ItemStack item;
    private final double price;
    private final String itemId;
    private final MarketManager marketManager;
    private final boolean isBlackMarket;

    public ConfirmationGUI(@NotNull final Player player, @NotNull final ItemStack item, double price, @NotNull final String itemId, boolean isBlackMarket) {
        super(3, "");

        this.setTitle(Objects.requireNonNull(
                CC.color(MarketPlugin.getInstance().getConfig().getString("gui-titles.confirmation-gui-title"))));

        this.setOnGlobalClick(event -> event.setCancelled(true));

        this.player = player;
        this.item = item;
        this.price = price;
        this.itemId = itemId;
        this.marketManager = MarketPlugin.getInstance().getMarketManager();
        this.isBlackMarket = isBlackMarket;

        this.fill();
    }

    private void fill() {
        final StaticPane pane = new StaticPane(9, 3);

        pane.addItem(new GuiItem(item), 4, 1);

        ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmButtonMeta = confirmButton.getItemMeta();
        confirmButtonMeta.setDisplayName(CC.color("&aConfirm"));
        confirmButton.setItemMeta(confirmButtonMeta);

        pane.addItem(new GuiItem(confirmButton, event -> {
            if(isBlackMarket) {
                if (marketManager.hasEnoughMoney(player, price)) {
                    marketManager.purchaseBlackmarketItem(player, itemId, price);
                } else {
                    player.sendMessage(CC.color("&cYou do not have enough money to purchase this item."));
                }
                player.closeInventory();
                return;
            }

            if (marketManager.hasEnoughMoney(player, price)) {
                marketManager.purchaseItem(player, itemId, price);
            } else {
                player.sendMessage(CC.color("&cYou do not have enough money to purchase this item."));
            }
            player.closeInventory();
            return;
        }), 3, 2);

        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelButtonMeta = cancelButton.getItemMeta();
        cancelButtonMeta.setDisplayName(CC.color("&cCancel"));
        cancelButton.setItemMeta(cancelButtonMeta);

        pane.addItem(new GuiItem(cancelButton, event -> player.closeInventory()), 5, 2);

        this.addPane(pane);
    }
}