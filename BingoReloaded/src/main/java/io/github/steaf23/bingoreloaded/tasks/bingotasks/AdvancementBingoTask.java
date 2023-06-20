package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.tasks.AdvancementTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public class AdvancementBingoTask extends BingoTask<AdvancementTask>
{

    public AdvancementBingoTask(AdvancementTask advancementTask) {
        this(advancementTask, null);
    }

    public AdvancementBingoTask(AdvancementTask advancementTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.GREEN;
        this.material = Material.FILLED_MAP;
        this.glowing = true;
        this.data = advancementTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        AdvancementBingoTask task = new AdvancementBingoTask(data, parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }
}
