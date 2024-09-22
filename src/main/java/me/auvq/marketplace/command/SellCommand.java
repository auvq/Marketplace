package me.auvq.marketplace.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.auvq.marketplace.MarketPlugin;
import org.bukkit.entity.Player;

@Command(name = "sell")
public class SellCommand {

    private final MarketPlugin plugin = MarketPlugin.getInstance();

    @Execute
    public void sellItem(@Context Player player, @Arg double price) {
        plugin.getMarketManager().listItem(player, player.getItemInHand(), price);
    }
}
