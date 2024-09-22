package me.auvq.marketplace;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class MarketItem {

    private final String itemId;
    private final ItemStack itemStack;
    private final double price;
    private final String sellerId;

    public MarketItem(String itemId, ItemStack itemStack, double price, String sellerId) {
        this.itemId = itemId;
        this.itemStack = itemStack;
        this.price = price;
        this.sellerId = sellerId;
    }
}