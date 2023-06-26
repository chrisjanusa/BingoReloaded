package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.ItemBingoTask;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.StatisticBingoTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.LastToTask")
public record LastToTask(TaskData task) implements ChildHavingTask
{

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText text = new ItemText("Last to obtain ");
        return ItemText.combine(text, task.getItemDisplayName());
    }

    @Override
    public ItemText[] getItemDescription()
    {
        if (task instanceof StatisticTask statisticTask) {
            return statisticTask.getLastToItemDescription();
        }
        if (task instanceof ItemTask itemTask) {
            return itemTask.getLastToItemDescription();
        }
        return task.getItemDescription();
    }

    @Override
    public BaseComponent getDescription()
    {
        return new ItemText("Be the last to complete the following").asComponent();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("task", task());
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastToTask itemTask = (LastToTask) o;
        return task == itemTask.task;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(task);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof LastToTask itemTask))
            return false;

        return task.equals(itemTask.task);
    }

    public static LastToTask deserialize(Map<String, Object> data)
    {
        return new LastToTask(
                (TaskData)data.getOrDefault("task", null)
        );
    }

    @Override
    public List<TaskData> getChildren() {
        return List.of(task);
    }
}
