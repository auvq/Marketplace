package me.auvq.marketplace.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.utils.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "transactions")

public class TransactionsCommand {

    private final MarketPlugin plugin = MarketPlugin.getInstance();

    @Execute
    public void getTransactions(@Context Player player) {
        player.sendMessage(CC.color("&eTransaction History:"));
        plugin.getMarketManager().getTransactionHistory(player).forEach(document -> {
            String sellerId = document.getString("sellerId");
            String buyerId = document.getString("buyerId");
            String itemJson = document.getString("item");
            double price = document.getDouble("price");
            String type = document.getString("type");

            if (type.equals("list")) {
                player.sendMessage(CC.color("§7" + sellerId + " listed an item for " + price + "$."));
            } else {
                player.sendMessage(CC.color("§7" + buyerId + " purchased an item for " + price + "$."));
            }
        });
    }
}
