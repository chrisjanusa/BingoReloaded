package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.ItemTask")
public record ItemTask(Material material, int count) implements CountableTask
{
    public ItemTask(Material material)
    {
        this(material, 1);
    }

    public ItemTask(Material material, int count)
    {
        this.material = material;
        this.count = Math.min(64, Math.max(1, count));
    }

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText text = new ItemText();
        text.addText(Integer.toString(count) + "x ");
        text.addItemName(material);
        return text;
    }

    @Override
    public ItemText[] getItemDescription()
    {
        Set<ChatColor> modifiers = new HashSet<>(){{
            add(ChatColor.DARK_AQUA);
        }};
        return BingoTranslation.LORE_ITEM.asItemText(modifiers, new ItemText(Integer.toString(count)));
    }

    @Override
    public BaseComponent getDescriptionsTitle() {
        return null;
    }

    @Override
    public List<BaseComponent> getDescriptions()
    {
        return List.of(ItemText.combine(getItemDescription()).asComponent());
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("item", material.name());
            put("count", count);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTask itemTask = (ItemTask) o;
        return material == itemTask.material;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(material);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof ItemTask itemTask))
            return false;

        return material.equals(itemTask.material);
    }

    public static ItemTask deserialize(Map<String, Object> data)
    {
        return new ItemTask(
                Material.valueOf((String) data.get("item")),
                (int) data.get("count"));
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public CountableTask updateTask(int newCount)
    {
        return new ItemTask(material, newCount);
    }
}
