package me.auvq.marketplace.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import me.auvq.marketplace.MarketPlugin;
import me.auvq.marketplace.gui.MarketplaceGUI;
import org.bukkit.entity.Player;

@Command(name = "marketplace")
public class MarketCommand {

    private final MarketPlugin plugin = MarketPlugin.getInstance();

    @Execute
    public void openMarket(@Context Player player) {
        new MarketplaceGUI(player).show(player);
    }
}
