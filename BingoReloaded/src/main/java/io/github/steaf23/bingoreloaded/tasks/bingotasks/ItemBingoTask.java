package io.github.steaf23.bingoreloaded.tasks.bingotasks;


import io.github.steaf23.bingoreloaded.tasks.ItemTask;
import net.md_5.bungee.api.ChatColor;

import java.util.Optional;

public class ItemBingoTask extends BingoTask<ItemTask>
{

    public ItemBingoTask(ItemTask itemTask) {
        this(itemTask, null);
    }

    public ItemBingoTask(ItemTask itemTask, ChildHavingBingoTask<?> parent) {
        this.nameColor = ChatColor.YELLOW;
        this.material = itemTask.material();
        this.glowing = false;
        this.data = itemTask;
        this.parentTask = parent;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        ItemBingoTask task = new ItemBingoTask(data, this.parentTask);
        if (completedBy.isPresent()) {
            task.completedBy = completedBy;
            task.completedAt = completedAt;
        }
        task.setVoided(voided);
        return task;
    }

    @Override
    public String toString() {
        return "ItemBingoTask{" +
                "data=" + data +
                '}';
    }
}
