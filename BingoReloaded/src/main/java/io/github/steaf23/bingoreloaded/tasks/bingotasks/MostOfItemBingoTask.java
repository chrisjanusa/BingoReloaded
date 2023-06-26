package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.MostOfItemTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.*;

public class MostOfItemBingoTask extends BingoTask<MostOfItemTask> {
    public Map<String, Integer> teamCount = new HashMap<>();

    public MostOfItemBingoTask(MostOfItemTask mostOfItemTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.DARK_GREEN;
        this.material = mostOfItemTask.materials().get(0);
        this.glowing = true;
        this.data = mostOfItemTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    public MostOfItemBingoTask(MostOfItemTask mostOfItemTask, ChildHavingBingoTask<?> parent, Map<String, Integer> teamCount) {
        this(mostOfItemTask, parent);
        this.teamCount = teamCount;
    }

    @Override
    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);
        String teamName = team.getName();
        base.addExtra("\n - ");
        base.addExtra("Your team currently has " + teamCount.getOrDefault(teamName, 0));

        return base;
    }

    public synchronized void increaseCount(BingoParticipant player, int amount, long gameTime) {
        String teamName = player.getTeam().getName();
        teamCount.put(teamName, teamCount.getOrDefault(teamName, 0) + amount);

        if (completedBy.isPresent() && Objects.equals(completedBy.get().getTeam().getName(), teamName)) {
            return;
        }
        int mostCount = -1;
        String mostTeam = "";
        boolean hasTie = false;
        for (Map.Entry<String, Integer> entry : teamCount.entrySet()) {
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
        MostOfItemBingoTask task = new MostOfItemBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "MostOfItemBingoTask{" +
                "data=" + data +
                '}';
    }
}
