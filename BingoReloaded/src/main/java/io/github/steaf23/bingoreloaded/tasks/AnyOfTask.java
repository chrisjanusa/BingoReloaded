package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.AnyOfTask")
public record AnyOfTask(String name, Material icon, List<TaskData> possibleTasks) implements TaskData
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
    public BaseComponent getDescriptionsTitle() {
        return new ItemText("Complete any of the following").asComponent();
    }

    @Override
    public List<BaseComponent> getDescriptions()
    {
        ArrayList<BaseComponent> descriptions = new ArrayList<>();
        for (TaskData possibleTask : possibleTasks) {
            descriptions.add(possibleTask.getItemDisplayName().asComponent());
        }
        return descriptions;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("name", name);
            put("icon", icon.name());
            put("possible_tasks", possibleTasks);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyOfTask itemTask = (AnyOfTask) o;
        return possibleTasks == itemTask.possibleTasks;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof AnyOfTask itemTask))
            return false;

        return name.equals(itemTask.name);
    }

    public static AnyOfTask deserialize(Map<String, Object> data)
    {
        return new AnyOfTask(
                (String) data.get("name"),
                Material.valueOf((String) data.get("icon")),
                (List<TaskData>)data.getOrDefault("possible_tasks", null)
        );
    }
}
