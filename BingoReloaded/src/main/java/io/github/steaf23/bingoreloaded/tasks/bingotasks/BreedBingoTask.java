package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.BreedTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BreedBingoTask extends BingoTask<BreedTask>
{

    public Map<String, Integer> teamCount = new HashMap<>();
    public int max = 0;
    public String maxTeam = "";

    public BreedBingoTask(BreedTask breedTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.DARK_RED;
        if (breedTask.animal() == EntityType.MUSHROOM_COW) {
            this.material = Material.MOOSHROOM_SPAWN_EGG;
        } else {
            this.material = Material.valueOf(breedTask.animal().name() + "_SPAWN_EGG");
        }
        this.glowing = false;
        this.data = breedTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    public BreedBingoTask(BreedTask breedTask, ChildHavingBingoTask<?> parent, Map<String, Integer> teamCount) {
        this(breedTask, parent);
        this.teamCount = teamCount;
    }

    public synchronized void increaseBreedCount(BingoParticipant player, long gameTime) {
        String teamName = player.getTeam().getIdentifier();
        int newCount = teamCount.getOrDefault(teamName, 0) + 1;
        teamCount.put(teamName, newCount);
        if (data.count() < 0) {
            if (!Objects.equals(teamName, maxTeam) && max < newCount) {
                completedBy = Optional.of(player);
                completedAt = gameTime;
                if (parentTask != null) {
                    parentTask.onChildComplete(player, gameTime);
                }
                var slotEvent = new ChildHavingTaskCompleteEvent(player, this);
                Bukkit.getPluginManager().callEvent(slotEvent);
                max = newCount;
                maxTeam = teamName;
            }
        } else if (data.count() <= newCount) {
            complete(player, gameTime);
            var slotEvent = new ChildHavingTaskCompleteEvent(player, this);
            Bukkit.getPluginManager().callEvent(slotEvent);
        }
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
        base.addExtra("Your team has bred " + teamCount.getOrDefault(teamName, 0) + " times");

        return base;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        BreedBingoTask task = new BreedBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "BreedBingoTask{" +
                "data=" + data +
                '}';
    }
}
