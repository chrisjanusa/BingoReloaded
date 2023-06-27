package io.github.steaf23.bingoreloaded.data.recoverydata;

import io.github.steaf23.bingoreloaded.cards.BingoCard;
import io.github.steaf23.bingoreloaded.cards.CompleteBingoCard;
import io.github.steaf23.bingoreloaded.cards.LockoutBingoCard;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.SerializableBasicBingoCard;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.SerializableBingoCard;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.SerializableCompleteBingoCard;
import io.github.steaf23.bingoreloaded.data.recoverydata.bingocard.SerializableLockoutBingoCard;
import io.github.steaf23.bingoreloaded.data.recoverydata.timer.SerializableCountdownTimer;
import io.github.steaf23.bingoreloaded.data.recoverydata.timer.SerializableCounterTimer;
import io.github.steaf23.bingoreloaded.data.recoverydata.timer.SerializableGameTimer;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.settings.BingoSettings;
import io.github.steaf23.bingoreloaded.tasks.statistics.StatisticProgress;
import io.github.steaf23.bingoreloaded.tasks.statistics.StatisticTracker;
import io.github.steaf23.bingoreloaded.util.timer.CountdownTimer;
import io.github.steaf23.bingoreloaded.util.timer.CounterTimer;
import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


@SerializableAs("RecoveryData")
public record SerializableRecoveryData(
        SerializableBingoCard bingoCard,
        SerializableGameTimer timer,
        BingoSettings settings,
        List<SerializableStatisticProgress> statisticProgresses,
        Map<String, Map<String, Location>> teamToSavedLocations
) implements ConfigurationSerializable {
    private static final String BINGO_CARD_ID = "bingo_card";
    private static final String TIMER_ID = "game_timer";
    private static final String SETTINGS_ID = "settings";
    private static final String STATS_ID = "statistic_progresses";
    private static final String SAVED_LOCATIONS_ID = "saved_locations";

    public SerializableRecoveryData(BingoCard bingoCard, GameTimer timer, BingoSettings settings, StatisticTracker statisticTracker, Set<BingoTeam> teams) {
        this(
                getSerializableBingoCard(bingoCard),
                getSerializableGameTimer(timer),
                settings,
                getSerializableStatisticProgressList(statisticTracker),
                getSavedLocationMap(teams)
        );
    }

    private static Map<String, Map<String, Location>> getSavedLocationMap(Set<BingoTeam> teams) {
        Map<String, Map<String, Location>> savedLocationMap = new HashMap<>();
        for (BingoTeam team : teams) {
            savedLocationMap.put(team.getIdentifier(), team.savedLocations);
        }
        return savedLocationMap;
    }

    private static SerializableGameTimer getSerializableGameTimer(GameTimer timer) {
        SerializableGameTimer serializableGameTimer = null;
        if (timer instanceof CountdownTimer) {
            serializableGameTimer = new SerializableCountdownTimer((CountdownTimer) timer);
        } else if (timer instanceof CounterTimer) {
            serializableGameTimer = new SerializableCounterTimer((CounterTimer) timer);
        }
        return serializableGameTimer;
    }

    private static SerializableBingoCard getSerializableBingoCard(BingoCard bingoCard) {
        SerializableBingoCard serializableBingoCard = null;
        if (bingoCard instanceof LockoutBingoCard) {
            serializableBingoCard = new SerializableLockoutBingoCard((LockoutBingoCard) bingoCard);
        } else if (bingoCard instanceof CompleteBingoCard) {
            serializableBingoCard = new SerializableCompleteBingoCard((CompleteBingoCard) bingoCard);
        } else {
            serializableBingoCard = new SerializableBasicBingoCard(bingoCard);
        }
        return serializableBingoCard;
    }

    private static List<SerializableStatisticProgress> getSerializableStatisticProgressList(StatisticTracker statisticTracker) {
        List<SerializableStatisticProgress> serializableStatisticProgressList = new ArrayList<>();
        if (statisticTracker != null) {
            serializableStatisticProgressList = statisticTracker.getStatistics()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(SerializableStatisticProgress::new)
                    .toList();
        }
        return serializableStatisticProgressList;
    }

    public static SerializableRecoveryData deserialize(Map<String, Object> data)
    {
        return new SerializableRecoveryData(
                (SerializableBingoCard) data.getOrDefault(BINGO_CARD_ID, null),
                (SerializableGameTimer) data.getOrDefault(TIMER_ID, null),
                (BingoSettings) data.getOrDefault(SETTINGS_ID, null),
                (List<SerializableStatisticProgress>) data.getOrDefault(STATS_ID, null),
                (Map<String, Map<String, Location>>) data.getOrDefault(SAVED_LOCATIONS_ID, null)
        );
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        data.put(BINGO_CARD_ID, bingoCard);
        data.put(TIMER_ID, timer);
        data.put(SETTINGS_ID, settings);
        data.put(STATS_ID, statisticProgresses);
        data.put(SAVED_LOCATIONS_ID, teamToSavedLocations);

        return data;
    }

    public RecoveryData toRecoveryData(BingoSession session) {
        StatisticTracker tracker = null;
        if (statisticProgresses != null) {
            ArrayList<StatisticProgress> stats = statisticProgresses
                    .stream()
                    .map(statisticProgress -> statisticProgress.toStatisticProgress(session))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            tracker = new StatisticTracker(session.worldName, stats);
        }
        return new RecoveryData(
                bingoCard.toBingoCard(session),
                timer.toGameTimer(session),
                settings,
                tracker,
                teamToSavedLocations
        );
    }
}
