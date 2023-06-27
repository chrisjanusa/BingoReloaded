package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@SerializableAs("Bingo.BreedTask")
public record BreedTask(@Nullable EntityType animal, int count) implements TaskData
{

    @Override
    public ItemText getItemDisplayName()
    {
        String countString;
        if (count > 0) {
            countString = Integer.toString(count);
        } else {
            countString = "the most";
        }
        String animalName = cleanUpAnimalName(animal.name());

        return new ItemText("Breed " + animalName + " " + countString + " times");
    }

    private String cleanUpAnimalName(String name) {
        boolean capitalNext = true;
        StringBuilder cleanedUp = new StringBuilder();
        for (char letter : name.toCharArray()) {
            if (letter == '_') {
                capitalNext = true;
                cleanedUp.append(' ');
                break;
            }
            if (capitalNext) {
                capitalNext = false;
                cleanedUp.append(letter);
            } else {
                cleanedUp.append(Character.toLowerCase(letter));
            }
        }
        return cleanedUp.toString();
    }

    @Override
    public ItemText[] getItemDescription()
    {
        List<String> givenList = Arrays.asList(
                "Baby Making Time",
                "Making the decision to have a child is momentous",
                "Lets make a baby!",
                "Hasta la vista baby!",
                "Maka da baby",
                "You might want to dim lights",
                "Might help if you throw on “Cbat” by Hudson Mohawke"
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

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof BreedTask statisticTask))
            return false;

        return animal.equals(statisticTask.animal);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BreedTask that = (BreedTask) o;
        return Objects.equals(animal, that.animal);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(animal);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("animal", animal == null ? "" : animal.name());
        }};
    }

    public static BreedTask deserialize(Map<String, Object> data)
    {
        String entityStr = (String) data.getOrDefault("animal", null);
        EntityType entity = null;
        if (entityStr != null && !entityStr.isEmpty())
            entity = EntityType.valueOf(entityStr.toUpperCase());
        return new BreedTask(entity, (Integer) data.getOrDefault("count", 1));
    }
}
