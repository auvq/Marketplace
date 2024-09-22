package me.auvq.marketplace.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.auvq.marketplace.MarketItem;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.manager.MarketManager;
import me.auvq.marketplace.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlackMarketGUI extends ChestGui {

    private final MarketPlugin plugin = MarketPlugin.getInstance();
    private final Player player;
    private final MarketManager marketManager;

    public BlackMarketGUI(@NotNull final Player player) {
        super(6, " ");

        this.setTitle(Objects.requireNonNull(
                CC.color(plugin.getConfig().getString("gui-titles.blackmarket-gui-title"))));

        this.setOnGlobalClick(event -> event.setCancelled(true));

        this.player = player;
        this.marketManager = plugin.getMarketManager();

        this.fill();
    }

    private void fill() {
        final StaticPane borderPane = new StaticPane(9, 6);

        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        for (int x = 0; x < 9; x++) {
            borderPane.addItem(new GuiItem(borderItem), x, 0);
            borderPane.addItem(new GuiItem(borderItem), x, 5);
        }

        for (int y = 1; y < 5; y++) {
            borderPane.addItem(new GuiItem(borderItem), 0, y);
            borderPane.addItem(new GuiItem(borderItem), 8, y);
        }

        final StaticPane closePane = new StaticPane(4, 5, 1, 1);

        ItemStack closeButton = new ItemStack(Material.BARRIER);

        closePane.addItem(new GuiItem(closeButton, event -> player.closeInventory()), 0, 0);

        final PaginatedPane itemsPane = new PaginatedPane(1, 1, 7, 4);

        List<MarketItem> items = marketManager.getMarketItems();
        List<MarketItem> randomItems = getRandomItems(items, 28);
        int page = 0;
        int slot = 0;

        StaticPane pagePane = new StaticPane(0, 0, 7, 4);

        for (MarketItem marketItem : randomItems) {
            if (slot >= 28) {
                itemsPane.addPane(page, pagePane);
                page++;
                slot = 0;
                pagePane = new StaticPane(0, 0, 7, 4);
            }
            ItemStack item = marketItem.getItemStack();
            double originalPrice = marketItem.getPrice();
            double discountedPrice = originalPrice / 2;
            String itemId = marketItem.getItemId();
            String seller = Bukkit.getOfflinePlayer(UUID.fromString(marketItem.getSellerId())).getName();

            ItemStack itemWithLore = addLore(item, originalPrice, discountedPrice, seller);
            pagePane.addItem(new GuiItem(itemWithLore, event -> {
                if (marketItem.getSellerId().equals(player.getUniqueId().toString())) {
                    player.sendMessage(CC.color("&cYou cannot purchase your own item."));
                    return;
                }
                if (marketManager.hasEnoughMoney(player, discountedPrice)) {
                    new ConfirmationGUI(player, item, discountedPrice, itemId, true).show(player);
                } else {
                    player.sendMessage(CC.color("&cYou do not have enough money."));
                    player.closeInventory();
                }
            }), slot % 7, slot / 7);
            slot++;
        }

        itemsPane.addPane(page, pagePane);

        final StaticPane navigationPane = new StaticPane(0, 5, 9, 1);

        ItemStack previousPageItem = new ItemStack(Material.ARROW);
        ItemMeta previousPageMeta = previousPageItem.getItemMeta();
        if (previousPageMeta != null) {
            previousPageMeta.setDisplayName(CC.color("&ePrevious Page"));
            previousPageItem.setItemMeta(previousPageMeta);
        }

        ItemStack nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPageItem.getItemMeta();
        if (nextPageMeta != null) {
            nextPageMeta.setDisplayName(CC.color("&eNext Page"));
            nextPageItem.setItemMeta(nextPageMeta);
        }

        navigationPane.addItem(new GuiItem(previousPageItem, event -> {
            if (itemsPane.getPage() > 0) {
                itemsPane.setPage(itemsPane.getPage() - 1);
                update();
            }
        }), 2, 0);

        navigationPane.addItem(new GuiItem(nextPageItem, event -> {
            if (itemsPane.getPage() < itemsPane.getPages() - 1) {
                itemsPane.setPage(itemsPane.getPage() + 1);
                update();
            }
        }), 6, 0);

        this.addPane(borderPane);
        this.addPane(itemsPane);
        this.addPane(navigationPane);
        this.addPane(closePane);
    }

    private List<MarketItem> getRandomItems(List<MarketItem> items, int count) {
        List<MarketItem> randomItems = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count && !items.isEmpty(); i++) {
            randomItems.add(items.remove(random.nextInt(items.size())));
        }
        return randomItems;
    }

    private ItemStack addLore(ItemStack item, double originalPrice, double discountedPrice, String seller) {
        ItemStack itemWithLore = item.clone();
        ItemMeta meta = itemWithLore.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            FileConfiguration config = plugin.getConfig();
            List<String> configLore = config.getStringList("blackmarket-item-lore");
            for (String line : configLore) {
                lore.add(CC.color(line.replace("%original_price%", String.valueOf(originalPrice))
                                .replace("%discounted_price%", String.valueOf(discountedPrice)))
                        .replace("%seller%", seller)
                );
            }
            meta.setLore(lore);
            itemWithLore.setItemMeta(meta);
        }
        return itemWithLore;
    }
}