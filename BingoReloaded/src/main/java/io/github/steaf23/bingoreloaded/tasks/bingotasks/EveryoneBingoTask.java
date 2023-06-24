package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.EveryoneTask;
import io.github.steaf23.bingoreloaded.tasks.LastToTask;
import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

public class EveryoneBingoTask extends ChildHavingBingoTask<EveryoneTask> {

    public Map<String, Map<String, BingoTask<?>>> childrenPerTeam;

    public EveryoneBingoTask(EveryoneTask everyoneTask, ChildHavingBingoTask<?> parentTask, Set<BingoTeam> activeTeams) {
        this.nameColor = ChatColor.GOLD;
        this.material = BingoTask.getBingoTask(everyoneTask.task()).material;
        this.glowing = true;
        this.data = everyoneTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = new HashMap<>();
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
        for (BingoTeam team : activeTeams) {
            childrenPerTeam.put(team.getName(), new HashMap<>());
        }
        for (BingoTeam team : activeTeams) {
            Map<String, BingoTask<?>> children = childrenPerTeam.get(team.getName());
            for (BingoParticipant player : team.getMembers()) {
                children.put(player.getDisplayName(), BingoTask.getBingoTask(everyoneTask.task(), this, activeTeams));
            }
        }
    }

    public EveryoneBingoTask(EveryoneTask everyoneTask, ChildHavingBingoTask<?> parentTask,  Map<String, Map<String, BingoTask<?>>> childrenPerTeam) {
        this.nameColor = ChatColor.RED;
        this.material = Material.BARRIER;
        this.glowing = true;
        this.data = everyoneTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = childrenPerTeam;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        HashMap<String, Map<String, BingoTask<?>>> childrenPerTeamCopy = new HashMap<>();
        EveryoneBingoTask taskCopy = new EveryoneBingoTask(data, parentTask, childrenPerTeamCopy);
        for (String teamName : childrenPerTeam.keySet()) {
            childrenPerTeamCopy.put(teamName, new HashMap<>());
            for (String playerName : childrenPerTeam.get(teamName).keySet()) {
                childrenPerTeamCopy.get(teamName).put(playerName, childrenPerTeam.get(teamName).get(playerName).copy(taskCopy));
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
    public List<BingoTask<?>> getChildTasksForPlayer(BingoParticipant participant) {
        return List.of(childrenPerTeam.get(participant.getTeam().getName()).get(participant.getDisplayName()));
    }

    @Override
    void onChildComplete(BingoParticipant participant, long gameTime) {
        Map<String, BingoTask<?>> teamTasks = childrenPerTeam.get(participant.getTeam().getName());
        for (BingoParticipant teammate : participant.getTeam().getMembers()) {
            if (!teamTasks.get(teammate.getDisplayName()).isCompleted()) {
                return;
            }
        }
        var slotEvent = new ChildHavingTaskCompleteEvent(participant, this);
        Bukkit.getPluginManager().callEvent(slotEvent);
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
        Map<String, BingoTask<?>> teamTasks = childrenPerTeam.get(team.getName());
        for (BingoParticipant teammate : team.getMembers()) {
            if (!teamTasks.get(teammate.getDisplayName()).isCompleted()) {
                base.addExtra("\n - ");
                base.addExtra(new ItemText(teammate.getDisplayName()).asComponent());
            }
        }

        return base;
    }
}
