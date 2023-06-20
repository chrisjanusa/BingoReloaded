package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.tasks.StatisticTask;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import net.md_5.bungee.api.ChatColor;

import java.util.Optional;

public class StatisticBingoTask extends BingoTask<StatisticTask>
{
    public StatisticBingoTask(StatisticTask statisticTask) {
        this(statisticTask, null);
    }

    public StatisticBingoTask(StatisticTask statisticTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.LIGHT_PURPLE;
        this.material = BingoStatistic.getMaterial(statisticTask.statistic());
        this.glowing = true;
        this.data = statisticTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        StatisticBingoTask task = new StatisticBingoTask(data, parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }
}
