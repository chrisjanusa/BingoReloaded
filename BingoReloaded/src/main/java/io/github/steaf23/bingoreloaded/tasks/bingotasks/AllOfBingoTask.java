package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AllOfTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.*;

public class AllOfBingoTask extends ChildHavingBingoTask<AllOfTask>
{

    public AllOfBingoTask(AllOfTask allOfTask, ChildHavingBingoTask<?> parentTask, Set<BingoTeam> activeTeams) {
        this.nameColor = ChatColor.BLUE;
        this.material = allOfTask.icon();
        this.glowing = true;
        this.data = allOfTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = new HashMap<>();
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
        for (BingoTeam team : activeTeams) {
            childrenPerTeam.put(team.getName(), new ArrayList<>());
        }
        for (TaskData task: allOfTask.tasks()) {
            for (BingoTeam team : activeTeams) {
                List<BingoTask<?>> children = childrenPerTeam.get(team.getName());
                children.add(BingoTask.getBingoTask(task, this, activeTeams));
            }
        }
    }

    public AllOfBingoTask(AllOfTask allOfTask, ChildHavingBingoTask<?> parentTask, Map<String, List<BingoTask<?>>> childrenPerTeam) {
        this.nameColor = ChatColor.BLUE;
        this.material = allOfTask.icon();
        this.glowing = true;
        this.data = allOfTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = childrenPerTeam;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        HashMap<String, List<BingoTask<?>>> childrenPerTeamCopy = new HashMap<>();
        AllOfBingoTask taskCopy = new AllOfBingoTask(data, parentTask, childrenPerTeamCopy);
        for (String teamName: childrenPerTeam.keySet()) {
            childrenPerTeamCopy.put(teamName, new ArrayList<>());
            for (BingoTask<?> child : childrenPerTeam.get(teamName)) {
                childrenPerTeamCopy.get(teamName).add(child.copy(taskCopy));
            }
        }

        if (completedBy.isPresent()) {
            taskCopy.completedBy = completedBy;
            taskCopy.completedAt = completedAt;
        }
        taskCopy.setVoided(voided);
        return taskCopy;
    }

    @Override
    void onChildComplete(BingoParticipant participant, long gameTime) {
        for (BingoTask<?> child : childrenPerTeam.get(participant.getTeam().getName())) {
            if (!child.isCompleted()) {
                return;
            }
        }
        complete(participant, gameTime);
    }

    @Override
    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);

        BaseComponent descriptionTitle = data.getDescription();
        base.addExtra("\n");
        descriptionTitle.setColor(nameColor);
        base.addExtra(descriptionTitle);
        for (BingoTask<?> task : childrenPerTeam.get(team.getName())) {
            base.addExtra("\n - ");
            BaseComponent childTaskName = task.data.getItemDisplayName().asComponent();
            if (task.isCompleted()) {
                childTaskName.setStrikethrough(true);
            }
            base.addExtra(childTaskName);
        }
        return base;
    }

    @Override
    public boolean complete(BingoParticipant participant, long gameTime) {
        var slotEvent = new ChildHavingTaskCompleteEvent(participant, this);
        Bukkit.getPluginManager().callEvent(slotEvent);
        return super.complete(participant, gameTime);
    }
}
