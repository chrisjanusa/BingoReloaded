package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.tasks.LevelTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public class LevelBingoTask extends BingoTask<LevelTask>
{

    public LevelBingoTask(LevelTask levelTask) {
        this(levelTask, null);
    }

    public LevelBingoTask(LevelTask levelTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.DARK_GREEN;
        this.material = Material.EXPERIENCE_BOTTLE;
        this.glowing = true;
        this.data = levelTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        LevelBingoTask task = new LevelBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "LevelBingoTask{" +
                "completedBy=" + completedBy +
                ", completedAt=" + completedAt +
                ", voided=" + voided +
                ", data=" + data +
                ", nameColor=" + nameColor +
                ", material=" + material +
                ", glowing=" + glowing +
                ", parentTask=" + parentTask +
                '}';
    }
}
