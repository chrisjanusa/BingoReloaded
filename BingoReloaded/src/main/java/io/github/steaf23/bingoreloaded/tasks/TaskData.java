package io.github.steaf23.bingoreloaded.tasks;

import io.github.steaf23.bingoreloaded.item.ItemText;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public interface TaskData extends ConfigurationSerializable, Serializable
{
    ItemText getItemDisplayName();
    ItemText[] getItemDescription();
    BaseComponent getDescriptionsTitle();
    List<BaseComponent> getDescriptions();
    default int getStackSize()
    {
        return 1;
    }
    boolean isTaskEqual(TaskData other);
}
