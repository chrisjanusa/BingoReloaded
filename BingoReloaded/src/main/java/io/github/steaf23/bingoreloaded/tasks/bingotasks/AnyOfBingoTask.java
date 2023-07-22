package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AnyOfTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class AnyOfBingoTask extends ChildHavingBingoTask<AnyOfTask> {

    public Map<String, List<BingoTask<?>>> childrenPerTeam;

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
            childrenPerTeam.put(team.getIdentifier(), new ArrayList<>());
        }
        for (TaskData task : anyOfTask.tasks()) {
            for (BingoTeam team : activeTeams) {
                List<BingoTask<?>> children = childrenPerTeam.get(team.getIdentifier());
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
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        HashMap<String, List<BingoTask<?>>> childrenPerTeamCopy = new HashMap<>();
        AnyOfBingoTask taskCopy = new AnyOfBingoTask(data, parentTask, childrenPerTeamCopy);
        for (String teamName : childrenPerTeam.keySet()) {
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
        var slotEvent = new ChildHavingTaskCompleteEvent(participant, this);
        Bukkit.getPluginManager().callEvent(slotEvent);
        complete(participant, gameTime);
    }

    @Override
    public Message[] onChildCompleteMessage(BingoTask<?> child, BingoParticipant completedBy, String completedAt) {
        return new Message[]{};
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
        for (TaskData possibleTask : data.tasks()) {
            base.addExtra("\n - ");
            base.addExtra(possibleTask.getItemDisplayName().asComponent());
        }
        return base;
    }

    @Override
    public List<BingoTask<?>> getChildTasksForPlayer(BingoParticipant participant) {
        return childrenPerTeam.get(participant.getTeam().getIdentifier());
    }
}
