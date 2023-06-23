package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SerializableAs("Bingo.StatisticTask")
public record StatisticTask(BingoStatistic statistic, int count) implements CountableTask
{
    public StatisticTask(BingoStatistic statistic)
    {
        this(statistic, 1);
    }

    public StatisticTask(BingoStatistic statistic, int count)
    {
        this.statistic = statistic;
        this.count = Math.min(64, Math.max(1, count));
    }

    @Override
    public ItemText getItemDisplayName()
    {
        ItemText amount = new ItemText(Integer.toString(count));

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
                text.add(new ItemText(Integer.toString(count * 10)));
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

    @Override
    public BaseComponent getDescription()
    {
        return ItemText.combine(getItemDescription()).asComponent();
    }

    @Override
    public boolean isTaskEqual(TaskData other)
    {
        if (!(other instanceof StatisticTask statisticTask))
            return false;

        return statistic.equals(statisticTask.statistic);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticTask that = (StatisticTask) o;
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
            put("count", count);
        }};
    }

    public static StatisticTask deserialize(Map<String, Object> data)
    {
        return new StatisticTask(
                (BingoStatistic) data.get("statistic"),
                (int)data.get("count")
        );
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public CountableTask updateTask(int newCount)
    {
        return new StatisticTask(statistic, newCount);
    }
}
