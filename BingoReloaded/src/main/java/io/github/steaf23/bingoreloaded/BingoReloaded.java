package io.github.steaf23.bingoreloaded;

import io.github.steaf23.bingoreloaded.command.*;
import io.github.steaf23.bingoreloaded.data.*;
import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.data.helper.SerializablePlayer;
import io.github.steaf23.bingoreloaded.data.recoverydata.SerializableRecoveryData;
import io.github.steaf23.bingoreloaded.data.recoverydata.SerializableStatisticProgress;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.*;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.bingotasks.*;
import io.github.steaf23.bingoreloaded.data.recoverydata.timer.SerializableCountdownTimer;
import io.github.steaf23.bingoreloaded.data.recoverydata.timer.SerializableCounterTimer;
import io.github.steaf23.bingoreloaded.data.helper.YmlDataManager;
import io.github.steaf23.bingoreloaded.gameloop.BingoGameManager;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.gameloop.multiple.MultiAutoBingoCommand;
import io.github.steaf23.bingoreloaded.gameloop.multiple.MultiGameManager;
import io.github.steaf23.bingoreloaded.gameloop.singular.SimpleAutoBingoCommand;
import io.github.steaf23.bingoreloaded.gameloop.singular.SingularGameManager;
import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.hologram.HologramManager;
import io.github.steaf23.bingoreloaded.hologram.HologramPlacer;
import io.github.steaf23.bingoreloaded.settings.CustomKit;
import io.github.steaf23.bingoreloaded.settings.BingoSettings;
import io.github.steaf23.bingoreloaded.tasks.*;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import io.github.steaf23.bingoreloaded.util.Message;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class BingoReloaded extends JavaPlugin
{
    public static final String CARD_1_18 = "lists_1_18.yml";
    public static final String CARD_1_19 = "lists_1_19.yml";
    public static final String CARD_1_20 = "lists.yml";

    // Amount of ticks per second.
    public static final int ONE_SECOND = 20;
    public static boolean usesPlaceholderAPI = false;

    private static BingoReloaded instance;

    private ConfigData config;
    private HologramManager hologramManager;
    private HologramPlacer hologramPlacer;
    private BingoGameManager gameManager;

    public BingoReloaded() {
        reloadConfig();
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        // Kinda ugly, but we can assume there will only be one instance of this class anyway.
        instance = this;
        usesPlaceholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        BasicMenu.pluginTitlePrefix = Message.PREFIX_STRING_SHORT + " " + ChatColor.DARK_RED;

        ConfigurationSerialization.registerClass(BingoSettings.class);
        ConfigurationSerialization.registerClass(ItemTask.class);
        ConfigurationSerialization.registerClass(MostOfItemTask.class);
        ConfigurationSerialization.registerClass(LevelTask.class);
        ConfigurationSerialization.registerClass(MostOfStatisticTask.class);
        ConfigurationSerialization.registerClass(RandomOneOfTask.class);
        ConfigurationSerialization.registerClass(AnyOfTask.class);
        ConfigurationSerialization.registerClass(AnyAdvancementsTask.class);
        ConfigurationSerialization.registerClass(BreedTask.class);
        ConfigurationSerialization.registerClass(EveryoneTask.class);
        ConfigurationSerialization.registerClass(DeathMessageTask.class);
        ConfigurationSerialization.registerClass(LastToTask.class);
        ConfigurationSerialization.registerClass(AllOfTask.class);
        ConfigurationSerialization.registerClass(AdvancementTask.class);
        ConfigurationSerialization.registerClass(StatisticTask.class);
        ConfigurationSerialization.registerClass(BingoStatistic.class);
        ConfigurationSerialization.registerClass(CustomKit.class);
        ConfigurationSerialization.registerClass(MenuItem.class);
        ConfigurationSerialization.registerClass(SerializablePlayer.class);
        ConfigurationSerialization.registerClass(TeamData.TeamTemplate.class);
        ConfigurationSerialization.registerClass(SerializableBasicBingoCard.class);
        ConfigurationSerialization.registerClass(SerializableDeathMessageBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableItemBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableAdvancementBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableStatisticBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableMostOfItemBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableMostOfStatisticBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableAnyOfBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableAnyAdvancementsBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableEveryoneBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableAllOfBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableLastToBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableBreedBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableLevelBingoTask.class);
        ConfigurationSerialization.registerClass(SerializableCardSize.class);
        ConfigurationSerialization.registerClass(SerializableCompleteBingoCard.class);
        ConfigurationSerialization.registerClass(SerializableLockoutBingoCard.class);
        ConfigurationSerialization.registerClass(SerializableCounterTimer.class);
        ConfigurationSerialization.registerClass(SerializableCountdownTimer.class);
        ConfigurationSerialization.registerClass(SerializableRecoveryData.class);
        ConfigurationSerialization.registerClass(SerializableStatisticProgress.class);

        this.config = new ConfigData(getConfig());

        BingoTranslation.setLanguage(createYmlDataManager(config.language).getConfig(), createYmlDataManager("languages/en_us.yml").getConfig());
        Message.log("" + ChatColor.GREEN + BingoTranslation.CHANGED_LANGUAGE.translate());

        this.hologramManager = new HologramManager();
        this.hologramPlacer = new HologramPlacer(hologramManager);

        TabExecutor autoBingoCommand;

        if (config.configuration == ConfigData.PluginConfiguration.SINGULAR) {
            this.gameManager = new SingularGameManager(this);
            autoBingoCommand = new SimpleAutoBingoCommand((SingularGameManager) gameManager);
        } else {
            this.gameManager = new MultiGameManager(this);
            autoBingoCommand = new MultiAutoBingoCommand((MultiGameManager) gameManager);
        }

        registerCommand("bingo", new BingoCommand(config, gameManager));
        registerCommand("get", new GetCommand(gameManager));

        if (config.teleportToTeammates) {
            registerCommand("btp", new TeamTeleportCommand(gameManager, config.teleportBack));
        }

        if (config.flightCap > 0) {
            registerCommand("flight", new TakeFlightCommand(gameManager, config.flightCap));
        }
        registerCommand("autobingo", autoBingoCommand);
        if (config.enableTeamChat) {
            TeamChatCommand command = new TeamChatCommand(player -> gameManager.getSession(BingoReloaded.getWorldNameOfDimension(player.getWorld())));
            registerCommand("btc", command);
            Bukkit.getPluginManager().registerEvents(command, this);
        }

        Message.log(ChatColor.GREEN + "Enabled " + getName());

//        if (RecoveryCardData.loadCards(game))
//        {
//            game.resume();
//        }
    }

    public void registerTeamChatCommand(String commandName, Function<Player, BingoSession> bingoSessionResolver) {
        registerCommand(commandName, new TeamChatCommand(bingoSessionResolver));
    }

    public void registerCommand(String commandName, TabExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    public static String getWorldNameOfDimension(World dimension) {
        return dimension.getName()
                .replace("_nether", "")
                .replace("_the_end", "");
    }

    public static YmlDataManager createYmlDataManager(String filepath) {
        return new YmlDataManager(instance, filepath);
    }

    public void onDisable() {
        if (gameManager != null) {
            gameManager.onDisable();
        }
    }

    public ConfigData config() {
        return config;
    }

    public HologramManager holograms() {
        return hologramManager;
    }

    public static void incrementPlayerStat(Player player, BingoStatType stat) {
        boolean savePlayerStatistics = instance.config.savePlayerStatistics;
        if (savePlayerStatistics) {
            BingoStatData statsData = new BingoStatData();
            statsData.incrementPlayerStat(player, stat);
        }
    }

    public static boolean areAdvancementsDisabled() {
        return !Bukkit.advancementIterator().hasNext() || Bukkit.advancementIterator().next() == null;
    }

    public static BingoReloaded getInstance() {
        return instance;
    }

    public static void scheduleTask(@NotNull Consumer<BukkitTask> task) {
        BingoReloaded.scheduleTask(task, 0);
    }

    public static void scheduleTask(@NotNull Consumer<BukkitTask> task, long delay) {
        if (delay <= 0)
            Bukkit.getScheduler().runTask(instance, task);
        else
            Bukkit.getScheduler().runTaskLater(instance, task, delay);
    }

    public static String getDefaultTasksVersion() {
        String version = Bukkit.getVersion();
        if (version.contains("(MC: 1.18")) {
            return CARD_1_18;
        }
        else if (version.contains("(MC: 1.19")) {
            return CARD_1_19;
        }
        else if (version.contains("(MC: 1.20")) {
            return CARD_1_20;
        }
        return CARD_1_18;
    }
}
