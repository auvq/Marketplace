package me.auvq.marketplace.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.manager.MarketManager;
import me.auvq.marketplace.utils.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "marketplace admin")
@Permission("marketplace.admin")
public class AdminCommand {

    private final MarketManager marketManager = MarketPlugin.getInstance().getMarketManager();

    @Execute(name = "removeall")
    public void removeAllListingsAndTransactions(@Context Player sender) {
        marketManager.removeAllListings();
        marketManager.removeAllTransactionHistory();
        sender.sendMessage(CC.color("&aSuccessfully removed all listings and transaction history."));
    }
}