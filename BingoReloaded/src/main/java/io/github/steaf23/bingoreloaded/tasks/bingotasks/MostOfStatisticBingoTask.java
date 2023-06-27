package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.MostOfItemTask;
import io.github.steaf23.bingoreloaded.tasks.MostOfStatisticTask;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MostOfStatisticBingoTask extends BingoTask<MostOfStatisticTask> {
    public Map<String, Double> teamCount = new HashMap<>();

    public MostOfStatisticBingoTask(MostOfStatisticTask mostOfStatisticTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.DARK_GREEN;
        this.material = BingoStatistic.getMaterial(mostOfStatisticTask.statistic());
        this.glowing = true;
        this.data = mostOfStatisticTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    public MostOfStatisticBingoTask(MostOfStatisticTask mostOfStatisticTask, ChildHavingBingoTask<?> parent, Map<String, Double> teamCount) {
        this(mostOfStatisticTask, parent);
        this.teamCount = teamCount;
    }

    @Override
    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);
        String teamName = team.getIdentifier();
        base.addExtra("\n - ");
        base.addExtra("Your team currently has " + getIntegerCount(teamName));

        return base;
    }

    private int getIntegerCount(String teamName) {
        double count = teamCount.getOrDefault(teamName, 0.0);
        return (int) count;
    }

    public synchronized void increaseCount(BingoParticipant player, double amount, long gameTime) {
        String teamName = player.getTeam().getIdentifier();
        teamCount.put(teamName, teamCount.getOrDefault(teamName, 0.0) + amount);
        if (completedBy.isPresent() && Objects.equals(completedBy.get().getTeam().getIdentifier(), teamName)) {
            return;
        }
        double mostCount = -1;
        String mostTeam = "";
        boolean hasTie = false;

        for (Map.Entry<String, Double> entry : teamCount.entrySet()) {
            if (entry.getValue() == mostCount) {
                hasTie = true;
            } else if (entry.getValue() > mostCount) {
                mostCount = entry.getValue();
                mostTeam = entry.getKey();
            }
        }
        if (!hasTie && mostTeam.equals(teamName)) {
            completedBy = Optional.of(player);
            completedAt = gameTime;
            if (parentTask != null) {
                parentTask.onChildComplete(player, gameTime);
            }
            var slotEvent = new ChildHavingTaskCompleteEvent(player, this);
            Bukkit.getPluginManager().callEvent(slotEvent);
        }
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        MostOfStatisticBingoTask task = new MostOfStatisticBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "MostOfStatisticBingoTask{" +
                "data=" + data +
                '}';
    }
}
