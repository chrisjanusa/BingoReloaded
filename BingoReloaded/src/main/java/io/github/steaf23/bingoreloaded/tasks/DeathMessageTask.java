package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.DeathTask")
public record DeathMessageTask(String deathMessage) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        return new ItemText("Obtain a death message containing " + deathMessage);
    }

    @Override
    public ItemText[] getItemDescription()
    {
        List<String> givenList = Arrays.asList(
                "Do not fear death",
                "YOLO",
                "Let's put the fun in funeral",
                "I won't kill you. But I don't have to save you.",
                "Hope you set your respawn point"
        );
        Random rand = new Random();
        String randomDescription = givenList.get(rand.nextInt(givenList.size()));
        return new ItemText[]{ new ItemText(randomDescription, ChatColor.DARK_AQUA) };
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
            put("deathMessage", deathMessage);
        }};
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathMessageTask itemTask = (DeathMessageTask) o;
        return deathMessage == itemTask.deathMessage;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(deathMessage);
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof DeathMessageTask itemTask))
            return false;

        return deathMessage.equals(itemTask.deathMessage);
    }

    public static DeathMessageTask deserialize(Map<String, Object> data)
    {
        return new DeathMessageTask((String) data.get("deathMessage"));
    }

    @Override
    public String toString() {
        return "DeathMessageTask{" +
                "deathMessage='" + deathMessage + '\'' +
                '}';
    }
}
