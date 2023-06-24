package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("Bingo.EveryoneTask")
public record EveryoneTask(TaskData task) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText text = new ItemText("Every player must obtain ");
        return ItemText.combine(text, task.getItemDisplayName());
    }

    @Override
    public ItemText[] getItemDescription()
    {
        return task.getItemDescription();
    }

    @Override
    public BaseComponent getDescription()
    {
        return new ItemText("Players remaining").asComponent();
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
        EveryoneTask itemTask = (EveryoneTask) o;
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
        if (!(other instanceof EveryoneTask itemTask))
            return false;

        return task.equals(itemTask.task);
    }

    public static EveryoneTask deserialize(Map<String, Object> data)
    {
        return new EveryoneTask(
                (TaskData)data.getOrDefault("task", null)
        );
    }
}
