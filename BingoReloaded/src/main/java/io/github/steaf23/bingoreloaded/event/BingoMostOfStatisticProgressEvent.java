package io.github.steaf23.bingoreloaded.event;

import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;

public class BingoMostOfStatisticProgressEvent extends BingoEvent
{
    public final BingoStatistic stat;
    public final BingoPlayer player;
    public final double progress;

    public BingoMostOfStatisticProgressEvent(BingoStatistic stat, BingoPlayer player, double progress)
    {
        super(player.getSession());
        this.stat = stat;
        this.player = player;
        this.progress = progress;
    }
}
