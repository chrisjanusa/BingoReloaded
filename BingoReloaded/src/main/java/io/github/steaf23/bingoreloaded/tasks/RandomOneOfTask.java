package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.RandomOneOfTask")
public record RandomOneOfTask(List<TaskData> possibleTasks) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        // Should never be displayed as it will immediately be replaced by one of the possible tasks
        return new ItemText("RandomOneOfTask");
    }

    @Override
    public ItemText[] getItemDescription()
    {
        // Should never be displayed as it will immediately be replaced by one of the possible tasks
        return new ItemText[]{new ItemText("RandomOneOfTask")};
    }

    @Override
    public BaseComponent getDescription()
    {
        return ItemText.combine(getItemDescription()).asComponent();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("possible_tasks", possibleTasks);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomOneOfTask itemTask = (RandomOneOfTask) o;
        return possibleTasks == itemTask.possibleTasks;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(possibleTasks);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof RandomOneOfTask itemTask))
            return false;

        return possibleTasks.equals(itemTask.possibleTasks);
    }

    public static RandomOneOfTask deserialize(Map<String, Object> data)
    {
        return new RandomOneOfTask(
                (List<TaskData>)data.getOrDefault("possible_tasks", null)
        );
    }
}
