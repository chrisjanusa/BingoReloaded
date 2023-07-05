package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.tasks.DeathMessageTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public class DeathMessageBingoTask extends BingoTask<DeathMessageTask>
{

    public DeathMessageBingoTask(DeathMessageTask deathMessageTask) {
        this(deathMessageTask, null);
    }

    public DeathMessageBingoTask(DeathMessageTask deathMessageTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.DARK_RED;
        this.material = Material.SKELETON_SKULL;
        this.glowing = true;
        this.data = deathMessageTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        DeathMessageBingoTask task = new DeathMessageBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "DeathMessageBingoTask{" +
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
