package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AllOfTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class AllOfBingoTask extends ChildHavingBingoTask<AllOfTask>
{

    public Map<String, List<BingoTask<?>>> childrenPerTeam;

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
            childrenPerTeam.put(team.getIdentifier(), new ArrayList<>());
        }
        for (TaskData task: allOfTask.tasks()) {
            for (BingoTeam team : activeTeams) {
                List<BingoTask<?>> children = childrenPerTeam.get(team.getIdentifier());
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
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
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
        if (getNumberOfTasksRemaining(participant) == 0) {
            complete(participant, gameTime);
        }
    }

    private int getNumberOfTasksRemaining(BingoParticipant participant) {
        int remaining = 0;
        for (BingoTask<?> child : childrenPerTeam.get(participant.getTeam().getIdentifier())) {
            if (!child.isCompleted()) {
                remaining++;
            }
        }
        return remaining;
    }

    @Override
    public Message[] onChildCompleteMessage(BingoTask<?> child, BingoParticipant completedBy, String completedAt) {
        Message completedMessage = new TranslatedMessage(BingoTranslation.COMPLETED).color(ChatColor.AQUA)
                .component(child.data.getItemDisplayName().asComponent()).color(child.nameColor)
                .arg(new ItemText(completedBy.getDisplayName(), completedBy.getTeam().getColor(), ChatColor.BOLD).asLegacyString())
                .arg(completedAt).color(ChatColor.WHITE);
        int remainingTasks = getNumberOfTasksRemaining(completedBy);
        if (remainingTasks != 0) {
            return new Message[]{completedMessage, new Message(remainingTasks + " tasks remaining for ").component(data.getItemDisplayName().asComponent()).color(nameColor)};
        } else {
            return new Message[]{completedMessage};
        }
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
        for (BingoTask<?> task : childrenPerTeam.get(team.getIdentifier())) {
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

    @Override
    public List<BingoTask<?>> getChildTasksForPlayer(BingoParticipant participant) {
        return childrenPerTeam.get(participant.getTeam().getIdentifier());
    }
}
