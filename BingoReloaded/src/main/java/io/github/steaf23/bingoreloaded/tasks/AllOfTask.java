package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.AllOfTask")
public record AllOfTask(String name, Material icon, List<TaskData> tasks) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        return new ItemText(name);
    }

    @Override
    public ItemText[] getItemDescription()
    {
        Set<ChatColor> modifiers = new HashSet<>(){{
            add(ChatColor.DARK_AQUA);
        }};
        return BingoTranslation.LORE_ADVANCEMENT.asItemText(modifiers);
    }

    @Override
    public BaseComponent getDescription()
    {
        return new ItemText("Complete all of the following").asComponent();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("name", name);
            put("icon", icon.name());
            put("tasks", tasks);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllOfTask itemTask = (AllOfTask) o;
        return tasks == itemTask.tasks;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof AllOfTask itemTask))
            return false;

        return name.equals(itemTask.name);
    }

    public static AllOfTask deserialize(Map<String, Object> data)
    {
        return new AllOfTask(
                (String) data.get("name"),
                Material.valueOf((String) data.get("icon")),
                (List<TaskData>)data.getOrDefault("tasks", null)
        );
    }
}
