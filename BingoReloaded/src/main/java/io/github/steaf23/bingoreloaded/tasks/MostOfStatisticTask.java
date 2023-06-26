package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Statistic;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.MostOfStatisticTask")
public record MostOfStatisticTask(BingoStatistic statistic) implements TaskData
{
    public MostOfStatisticTask(BingoStatistic statistic)
    {
        this.statistic = statistic;
    }

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText amount = new ItemText("the most");

        ItemText text = new ItemText("*", ChatColor.ITALIC);

        switch (statistic.getCategory())
        {
            case ROOT_STATISTIC -> {
                if (statistic.stat() == Statistic.ENTITY_KILLED_BY || statistic.stat() == Statistic.KILL_ENTITY)
                {
                    ItemText entityName = new ItemText().addEntityName(statistic.entityType());
                    ItemText[] inPlaceArguments =
                            switch (statistic.stat())
                                    {
                                        case KILL_ENTITY -> new ItemText[]{amount, entityName};
                                        case ENTITY_KILLED_BY -> new ItemText[]{entityName, amount};
                                        default -> new ItemText[]{};
                                    };
                    text.addStatistic(statistic.stat(), inPlaceArguments);
                }
                else
                {
                    text.add(amount);
                    text.addText(" ");
                    text.addStatistic(statistic.stat());
                    text.addText(" ");
                    text.addItemName(statistic.materialType());
                }
            }
            case TRAVEL -> {
                text.add(amount);
                text.addText(" Blocks ");
                text.addStatistic(statistic.stat());
            }
            default -> {
                text.add(amount);
                text.addText(" ");
                text.addStatistic(statistic.stat());
            }
        }
        text.addText("*");
        return text;
    }

    @Override
    public ItemText[] getItemDescription()
    {
        Set<ChatColor> modifiers = new HashSet<>(){{
            add(ChatColor.DARK_AQUA);
        }};
        return BingoTranslation.LORE_STATISTIC.asItemText(modifiers);
    }

    public ItemText[] getLastToItemDescription()
    {
        Set<ChatColor> modifiers = new HashSet<>(){{
            add(ChatColor.DARK_AQUA);
        }};
        return BingoTranslation.LAST_TO_LORE_STATISTIC.asItemText(modifiers);
    }

    @Override
    public BaseComponent getDescription()
    {
        return ItemText.combine(getItemDescription()).asComponent();
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof MostOfStatisticTask statisticTask))
            return false;

        return statistic.equals(statisticTask.statistic);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MostOfStatisticTask that = (MostOfStatisticTask) o;
        return Objects.equals(statistic, that.statistic);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(statistic);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize()
    {
        return new HashMap<>(){{
            put("statistic", statistic);
        }};
    }

    public static MostOfStatisticTask deserialize(Map<String, Object> data)
    {
        return new MostOfStatisticTask(
                (BingoStatistic) data.get("statistic")
        );
    }
}
