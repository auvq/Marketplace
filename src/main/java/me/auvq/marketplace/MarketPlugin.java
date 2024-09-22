package me.auvq.marketplace;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import me.auvq.marketplace.command.*;
import me.auvq.marketplace.manager.DatabaseManager;
import me.auvq.marketplace.manager.MarketManager;
import me.auvq.marketplace.utils.CC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class MarketPlugin extends JavaPlugin {

    @Getter
    private static MarketPlugin instance;

    @Getter
    private Economy economy = null;

    private MarketManager marketManager;
    private DatabaseManager databaseManager;

    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.databaseManager = new DatabaseManager();
        this.marketManager = new MarketManager();

        liteCommands = LiteBukkitFactory.builder()
                .commands(
                        new MarketCommand(),
                        new AdminCommand(),
                        new BlackmarketCommand(),
                        new SellCommand(),
                        new TransactionsCommand()
                )
                .build();
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getConsoleSender().sendMessage(CC.color("&aThe config setup correctly!"));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }
}
