package io.github.steaf23.bingoreloaded.tasks.statistics;

import io.github.steaf23.bingoreloaded.event.BingoMostOfStatisticProgressEvent;
import io.github.steaf23.bingoreloaded.event.BingoStatisticCompletedEvent;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MostOfStatisticProgress extends StatisticProgress
{

    public MostOfStatisticProgress(BingoStatistic statistic, BingoPlayer player)
    {
        super(statistic, player, 1);
    }

    public MostOfStatisticProgress(BingoStatistic statistic, BingoPlayer player, int progressLeft, int previousGlobalProgress)
    {
        super(statistic, player, progressLeft, previousGlobalProgress);
    }

    @Override
    public boolean done()
    {
        return false;
    }

    public void setProgress(int newProgress)
    {
        double progressDelta = newProgress - previousGlobalProgress;
        previousGlobalProgress = newProgress;
        if (statistic.getCategory() == BingoStatistic.StatisticCategory.TRAVEL)
        {
            progressDelta /= 100;
        }
        if (progressDelta > 0)
        {
            var event = new BingoMostOfStatisticProgressEvent(statistic, player, progressDelta);
            Bukkit.getPluginManager().callEvent(event);
        }
    }
}
