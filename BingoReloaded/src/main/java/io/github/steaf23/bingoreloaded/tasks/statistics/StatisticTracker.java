package io.github.steaf23.bingoreloaded.tasks.statistics;

import io.github.steaf23.bingoreloaded.gameloop.BingoGame;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AnyOfTask;
import io.github.steaf23.bingoreloaded.tasks.StatisticTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.StatisticBingoTask;
import org.bukkit.Statistic;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StatisticTracker
{
    private final ArrayList<StatisticProgress> statistics;
    private final String worldName;

    public StatisticTracker(String worldName)
    {
        this.statistics = new ArrayList<>();
        this.worldName = worldName;
    }

    public StatisticTracker(String worldName, ArrayList<StatisticProgress> statistics)
    {
        this.statistics = statistics;
        this.worldName = worldName;
    }

    public void start(Set<BingoTeam> teams)
    {
        for (BingoTeam team : teams)
        {
            createStatisticProgressFromTasks((List<TaskData>) team.card.tasks.stream().map(task -> task.data).toList(), team);
        }
    }

    private void createStatisticProgressFromTasks(List<TaskData> tasks, BingoTeam team) {
        for (TaskData task : tasks)
        {
            if (task instanceof StatisticTask statTask) {

                for (BingoParticipant player : team.getMembers()) {
                    if (statistics.stream().anyMatch(progress ->
                            progress.player.equals(player) && progress.statistic.equals(statTask.statistic())))
                        continue;

                    if (player instanceof BingoPlayer bingoPlayer)
                        statistics.add(new StatisticProgress(statTask.statistic(), bingoPlayer, statTask.count()));
                }
            } else if (task instanceof AnyOfTask anyOfTask) {
                createStatisticProgressFromTasks(anyOfTask.possibleTasks(), team);
            }
        }
    }

    public double getProgressLeft(BingoPlayer player, BingoStatistic statistic)
    {
        List<StatisticProgress> statProgress = statistics.stream().filter(progress ->
                progress.player.equals(player) && progress.statistic.equals(statistic)).toList();

        if (statProgress.size() != 1)
            return Double.MAX_VALUE;

        return statProgress.get(0).progressLeft;
    }

    public void updateProgress()
    {
        statistics.forEach(StatisticProgress::updatePeriodicProgress);
        statistics.removeIf(progress -> progress.progressLeft <= 0);
    }

    public void reset()
    {
        statistics.clear();
    }

    public void handleStatisticIncrement(final PlayerStatisticIncrementEvent event, final BingoGame game)
    {
        if (game == null)
            return;

        BingoParticipant player = game.getTeamManager().getBingoParticipant(event.getPlayer());
        if (player == null || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        BingoStatistic stat = new BingoStatistic(event.getStatistic(), event.getEntityType(), event.getMaterial());

        List<StatisticProgress> matchingStatistic = statistics.stream().filter(progress ->
                progress.player.equals(player) && progress.statistic.equals(stat)).toList();
        if (matchingStatistic.size() == 1)
        {
            matchingStatistic.get(0).setProgress(event.getNewValue());
        }

        statistics.removeIf(progress -> progress.progressLeft <= 0);
    }

    public List<StatisticProgress> getStatistics() {
        return statistics;
    }
}
