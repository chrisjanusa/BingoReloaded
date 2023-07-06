package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.LevelTask")
public record LevelTask(int level) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        return new ItemText("Reach level " + level);
    }

    @Override
    public ItemText[] getItemDescription()
    {
        return new ItemText[]{ new ItemText("Gain enough Exp to reach level " + level, ChatColor.DARK_AQUA) };
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
            put("level", level);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelTask itemTask = (LevelTask) o;
        return level == itemTask.level;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(level);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof LevelTask itemTask))
            return false;

        return level == itemTask.level;
    }

    public static LevelTask deserialize(Map<String, Object> data)
    {
        return new LevelTask((Integer) data.get("level"));
    }

    @Override
    public String toString() {
        return "levelTask{" +
                "level='" + level + '\'' +
                '}';
    }
}
