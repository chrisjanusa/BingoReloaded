package io.github.steaf23.bingoreloaded.data.recoverydata;

import io.github.steaf23.bingoreloaded.cards.BingoCard;
import io.github.steaf23.bingoreloaded.settings.BingoSettings;
import io.github.steaf23.bingoreloaded.tasks.statistics.StatisticTracker;
import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import org.bukkit.Location;

import java.util.Map;

public class RecoveryData {
    private BingoCard bingoCard;
    private GameTimer timer;
    private BingoSettings settings;
    private StatisticTracker statisticTracker;
    private Map<String, Map<String, Location>> teamToSavedLocations;

    public RecoveryData(BingoCard bingoCard, GameTimer timer, BingoSettings settings, StatisticTracker statisticTracker, Map<String, Map<String, Location>> teamToSavedLocations) {
        this.bingoCard = bingoCard;
        this.timer = timer;
        this.settings = settings;
        this.statisticTracker = statisticTracker;
        this.teamToSavedLocations = teamToSavedLocations;
    }

    public BingoCard getBingoCard() {
        return bingoCard;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public BingoSettings getSettings() {
        return settings;
    }

    public StatisticTracker getStatisticTracker() {
        return statisticTracker;
    }

    public Map<String, Map<String, Location>> getTeamToSavedLocations() {
        return teamToSavedLocations;
    }

    public boolean hasNull() {
        return bingoCard == null || timer == null || settings == null;
    }
}
