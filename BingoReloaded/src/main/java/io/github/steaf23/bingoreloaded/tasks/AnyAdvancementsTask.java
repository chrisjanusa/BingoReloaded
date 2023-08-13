package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.AnyAdvancementsTask")
public record AnyAdvancementsTask(int count) implements TaskData
{
    public AnyAdvancementsTask(int count)
    {
        this.count = count;
    }

    @Override
    public ItemText getItemDisplayName()
    {
        if (count > 0) {
            return new ItemText("Obtain " + count + " unique advancements");
        } else {
            return new ItemText("Obtain the most unique advancements");
        }
    }

    @Override
    public ItemText[] getItemDescription()
    {
        return new ItemText[]{ new ItemText("Click to see which are already completed", ChatColor.DARK_AQUA) };
    }

    // This method exists because advancement descriptions can contain newlines,
    // which makes it impossible to use as item names or descriptions without getting a missing character.
    @Override
    public BaseComponent getDescription()
    {
        BaseComponent comp = new ItemText("Click to see which are already completed", ChatColor.DARK_AQUA).asComponent();
        comp.setColor(ChatColor.DARK_AQUA);
        return comp;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyAdvancementsTask that = (AnyAdvancementsTask) o;
        return Objects.equals(count, that.count);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(count);
    }

    @Override
    public String toString() {
        return "AnyAdvancementsTask{" +
                "count=" + count +
                '}';
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        return this.equals(other);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("count", count);
        }};
    }

    public static AnyAdvancementsTask deserialize(Map<String, Object> data)
    {
        return new AnyAdvancementsTask(
                (Integer) data.getOrDefault("count",-1)
        );
    }
}
