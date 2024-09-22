package me.auvq.marketplace.manager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import me.auvq.marketplace.MarketPlugin;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

public class DatabaseManager {

    private final MarketPlugin plugin = MarketPlugin.getInstance();

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    @Getter
    private final MongoCollection<Document> itemListingsCollection;

    @Getter
    private final MongoCollection<Document> transactionHistoryCollection;

    public DatabaseManager() {
        try {
            String connectionString = plugin.getConfig().getString("mongo.connectionString");

            this.mongoClient = MongoClients.create(connectionString);
            this.database = mongoClient.getDatabase(plugin.getConfig().getString("mongo.databaseName"));

            this.itemListingsCollection = database.getCollection("itemListings");
            this.transactionHistoryCollection = database.getCollection("transactionHistory");
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new RuntimeException("Failed to connect to MongoDB");
        }
    }

    public void close() {
        mongoClient.close();
    }

    public void addTransaction(String playerId, String itemBase64, double price, String type) {
        Document transaction = new Document(type.equals("list") ? "sellerId" : "buyerId", playerId)
                .append("item", itemBase64)
                .append("price", price)
                .append("type", type);
        transactionHistoryCollection.insertOne(transaction);
    }

    public String itemStackToBase64(ItemStack itemStack, boolean asOne) {
        ItemStack local = itemStack;
        if (asOne) {
            local.setAmount(1);
        }
        String encodedObj;

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(local);
            os.flush();

            byte[] serializedObj = io.toByteArray();
            encodedObj = Base64.getEncoder().encodeToString(serializedObj);

            // Log the Base64 string for debugging
            plugin.getLogger().info("Serialized Base64: " + encodedObj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return encodedObj;
    }

    public ItemStack base64ToItemStack(String base64) {
        try {
            byte[] serializedObj = Base64.getDecoder().decode(base64);
            ByteArrayInputStream in = new ByteArrayInputStream(serializedObj);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);

            ItemStack itemStack = (ItemStack) is.readObject();

            // Log the deserialized ItemStack for debugging
            plugin.getLogger().info("Deserialized ItemStack: " + itemStack);
            return itemStack;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}