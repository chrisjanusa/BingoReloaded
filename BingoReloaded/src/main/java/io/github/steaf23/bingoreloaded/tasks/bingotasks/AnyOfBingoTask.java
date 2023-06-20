package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AnyOfTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public class AnyOfBingoTask extends ChildHavingBingoTask<AnyOfTask>
{

    public AnyOfBingoTask(AnyOfTask anyOfTask, ChildHavingBingoTask<?> parentTask, Set<BingoTeam> activeTeams) {
        this.nameColor = ChatColor.AQUA;
        this.material = anyOfTask.icon();
        this.glowing = true;
        this.data = anyOfTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = new HashMap<>();
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
        for (BingoTeam team : activeTeams) {
            childrenPerTeam.put(team.getName(), new ArrayList<>());
        }
        for (TaskData task: anyOfTask.possibleTasks()) {
            for (BingoTeam team : activeTeams) {
                List<BingoTask<?>> children = childrenPerTeam.get(team.getName());
                children.add(BingoTask.getBingoTask(task, this, activeTeams));
            }
        }
    }

    public AnyOfBingoTask(AnyOfTask anyOfTask, ChildHavingBingoTask<?> parentTask, Map<String, List<BingoTask<?>>> childrenPerTeam) {
        this.nameColor = ChatColor.AQUA;
        this.material = anyOfTask.icon();
        this.glowing = true;
        this.data = anyOfTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = childrenPerTeam;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        HashMap<String, List<BingoTask<?>>> childrenPerTeamCopy = new HashMap<>();
        AnyOfBingoTask taskCopy = new AnyOfBingoTask(data, parentTask, childrenPerTeamCopy);
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
        complete(participant, gameTime);
    }
}
