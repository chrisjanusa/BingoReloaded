package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.AnyAdvancementsTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;

import java.util.*;

public class AnyAdvancementsBingoTask extends BingoTask<AnyAdvancementsTask>
{
    public Map<String, Set<String>> teamCount;

    public AnyAdvancementsBingoTask(AnyAdvancementsTask anyAdvancementsTask, ChildHavingBingoTask<?> parent) {
        this(anyAdvancementsTask, parent, new HashMap<>());
    }

    public AnyAdvancementsBingoTask(AnyAdvancementsTask anyAdvancementsTask, ChildHavingBingoTask<?> parent, Map<String, Set<String>> teamCount) {
        this.nameColor = ChatColor.GREEN;
        this.material = Material.FILLED_MAP;
        this.glowing = true;
        this.data = anyAdvancementsTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
        this.teamCount = teamCount;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        AnyAdvancementsBingoTask task = new AnyAdvancementsBingoTask(data, parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);
        String teamName = team.getIdentifier();
        Set<String> completedAdvancements = teamCount.getOrDefault(teamName, new HashSet<>());
        base.addExtra("\nYour team currently has " + completedAdvancements.size() + " unique advancements");
        for (String advancement : completedAdvancements) {
            base.addExtra("\n - ");
            base.addExtra(advancement);
        }

        return base;
    }

    public synchronized void advancementCompleted(BingoParticipant player, Advancement advancement, long gameTime) {
        if (advancement.getDisplay() == null) {
            return;
        }
        if (data.count() == -1) {
            String teamName = player.getTeam().getIdentifier();
            Set<String> completedAdvancements = teamCount.getOrDefault(teamName, new HashSet<>());
            completedAdvancements.add(advancement.getDisplay().getTitle());
            teamCount.put(teamName, completedAdvancements);
            if (completedBy.isPresent() && Objects.equals(completedBy.get().getTeam().getIdentifier(), teamName)) {
                return;
            }
            int mostCount = -1;
            String mostTeam = "";
            boolean hasTie = false;
            for (Map.Entry<String, Set<String>> entry : teamCount.entrySet()) {
                if (entry.getValue().size() == mostCount) {
                    hasTie = true;
                } else if (entry.getValue().size() > mostCount) {
                    mostCount = entry.getValue().size();
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
        } else {
            if (completedBy.isPresent()) {
                return;
            }
            String teamName = player.getTeam().getIdentifier();
            Set<String> completedAdvancements = teamCount.getOrDefault(teamName, new HashSet<>());
            completedAdvancements.add(advancement.getDisplay().getTitle());
            teamCount.put(teamName, completedAdvancements);
            if (completedAdvancements.size() >= data.count()) {
                completedBy = Optional.of(player);
                completedAt = gameTime;
                if (parentTask != null) {
                    parentTask.onChildComplete(player, gameTime);
                }
                var slotEvent = new ChildHavingTaskCompleteEvent(player, this);
                Bukkit.getPluginManager().callEvent(slotEvent);
            }
        }
    }
}
