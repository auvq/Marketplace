package me.auvq.marketplace.manager;

import me.auvq.marketplace.MarketItem;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.utils.CC;
import org.bson.types.ObjectId;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketManager {

    private final MarketPlugin plugin = MarketPlugin.getInstance();
    private final DatabaseManager databaseManager;

    public MarketManager() {
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void listItem(Player player, ItemStack itemStack, double price) {
        String itemBase64 = databaseManager.itemStackToBase64(itemStack, true);
        String itemId = UUID.randomUUID().toString();

        Document document = new Document("itemId", itemId)
                .append("sellerId", player.getUniqueId().toString())
                .append("item", itemBase64)
                .append("price", price);

        databaseManager.getItemListingsCollection().insertOne(document);
        player.sendMessage(CC.color("&aItem listed successfully!"));
        player.getInventory().remove(itemStack);
    }

    public void purchaseItem(Player player, String itemId, double price) {
        Document query = new Document("itemId", itemId);
        Document storedItem = databaseManager.getItemListingsCollection().find(query).first();

        if (storedItem != null) {
            String storedItemBase64 = storedItem.getString("item");
            ItemStack purchasedItem = databaseManager.base64ToItemStack(storedItemBase64);

            if (purchasedItem != null) {
                player.getInventory().addItem(purchasedItem);
            }

            String sellerId = storedItem.getString("sellerId");
            OfflinePlayer seller = plugin.getServer().getOfflinePlayer(UUID.fromString(sellerId));

            plugin.getEconomy().depositPlayer(seller, price);

            if(seller.isOnline()) {
                seller.getPlayer().sendMessage(CC.color("&aYour item has been purchased!"));
            }

            databaseManager.getItemListingsCollection().deleteOne(query);
            databaseManager.addTransaction(player.getUniqueId().toString(), storedItemBase64, price, "purchase");

            player.sendMessage(CC.color("&aItem purchased successfully!"));
            plugin.getEconomy().withdrawPlayer(player, price);
        }
    }

    public void purchaseBlackmarketItem(Player player, String itemId, double price) {
        Document query = new Document("itemId", itemId);
        Document storedItem = databaseManager.getItemListingsCollection().find(query).first();

        if (storedItem != null) {
            String storedItemBase64 = storedItem.getString("item");
            ItemStack purchasedItem = databaseManager.base64ToItemStack(storedItemBase64);

            if (purchasedItem != null) {
                player.getInventory().addItem(purchasedItem);
            }

            String sellerId = storedItem.getString("sellerId");
            OfflinePlayer seller = plugin.getServer().getOfflinePlayer(UUID.fromString(sellerId));

            plugin.getEconomy().depositPlayer(seller, price * 2);
            if(seller.isOnline()) {
                seller.getPlayer().sendMessage(CC.color("&aYour item has been purchased through the black market!"));
            }

            databaseManager.getItemListingsCollection().deleteOne(query);
            databaseManager.addTransaction(player.getUniqueId().toString(), storedItemBase64, price, "purchase");

            player.sendMessage(CC.color("&aItem purchased through the black market successfully!"));
            plugin.getEconomy().withdrawPlayer(player, price);
        }
    }

    public double getItemPrice(ItemStack itemStack) {
        String itemBase64 = databaseManager.itemStackToBase64(itemStack, true);
        Document query = new Document("item", itemBase64);

        Document item = databaseManager.getItemListingsCollection().find(query).first();
        if (item == null) {
            return -1;
        }

        return item.getDouble("price");
    }

    public List<Document> getTransactionHistory(Player player) {
        Document query = new Document("$or", List.of(
                new Document("sellerId", player.getUniqueId().toString()),
                new Document("buyerId", player.getUniqueId().toString())
        ));

        return databaseManager.getTransactionHistoryCollection().find(query).into(new ArrayList<>());
    }

    public List<MarketItem> getMarketItems() {
        List<MarketItem> marketItems = new ArrayList<>();
        List<Document> documents = databaseManager.getItemListingsCollection().find().into(new ArrayList<>());

        for (Document document : documents) {
            String itemBase64 = document.getString("item");
            ItemStack itemStack = databaseManager.base64ToItemStack(itemBase64);
            if (itemStack != null) {
                String itemId = document.getString("itemId");
                double price = document.getDouble("price");
                String sellerId = document.getString("sellerId");
                marketItems.add(new MarketItem(itemId, itemStack, price, sellerId));
            }
        }

        return marketItems;
    }

    public void removeAllListings() {
        databaseManager.getItemListingsCollection().deleteMany(new Document());
        plugin.getLogger().info("All marketplace listings have been removed.");
    }

    public void removeAllTransactionHistory() {
        databaseManager.getTransactionHistoryCollection().deleteMany(new Document());
        plugin.getLogger().info("All transaction history has been removed.");
    }

    public boolean hasEnoughMoney(Player player, double price) {
        return plugin.getEconomy().getBalance(player) >= price;
    }
}