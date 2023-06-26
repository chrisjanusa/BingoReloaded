package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.MostOfItemTask")
public record MostOfItemTask(List<Material> materials, String name) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText text = new ItemText();
        text.addText("Have the most " + name);
        return text;
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
        return new ItemText("Have the most total of the following").asComponent();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("items", materials.stream().map(Enum::name).toList());
            put("name", name);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MostOfItemTask itemTask = (MostOfItemTask) o;
        return materials == itemTask.materials;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(materials);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof MostOfItemTask itemTask))
            return false;

        return materials.equals(itemTask.materials);
    }

    public static MostOfItemTask deserialize(Map<String, Object> data)
    {
        return new MostOfItemTask(
                ((List<String>) data.get("items")).stream().map(materialName -> Material.valueOf(materialName.toUpperCase())).toList(),
                (String) data.get("name")
        );
    }

    @Override
    public String toString() {
        return "MostOfItemTask{" +
                "material=" + materials +
                '}';
    }
}
