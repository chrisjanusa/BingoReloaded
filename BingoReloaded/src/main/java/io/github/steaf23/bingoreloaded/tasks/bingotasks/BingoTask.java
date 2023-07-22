package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.BingoCardTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.*;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public abstract class BingoTask<T extends TaskData>
{

    public Optional<BingoParticipant> completedBy;
    public long completedAt;
    boolean voided;

    public T data;
    public ChatColor nameColor;
    public Material material;
    public boolean glowing;
    public ChildHavingBingoTask parentTask;

    public static BingoTask<?> getBingoTask(TaskData data, ChildHavingBingoTask<?> parentTask, Set<BingoTeam> activeTeams) {
        if (data instanceof ItemTask itemTask) {
            return new ItemBingoTask(itemTask, parentTask);
        }
        if (data instanceof AdvancementTask advancementTask) {
            return new AdvancementBingoTask(advancementTask, parentTask);
        }
        if (data instanceof StatisticTask statisticTask) {
            return new StatisticBingoTask(statisticTask, parentTask);
        }
        if (data instanceof AnyOfTask anyOfTask) {
            return new AnyOfBingoTask(anyOfTask, parentTask, activeTeams);
        }
        if (data instanceof AllOfTask allOfTask) {
            return new AllOfBingoTask(allOfTask, parentTask, activeTeams);
        }
        if (data instanceof LastToTask lastToTask) {
            return new LastToBingoTask(lastToTask, parentTask, activeTeams);
        }
        if (data instanceof EveryoneTask everyoneTask) {
            return new EveryoneBingoTask(everyoneTask, parentTask, activeTeams);
        }
        if (data instanceof MostOfItemTask mostOfItemTask) {
            return new MostOfItemBingoTask(mostOfItemTask, parentTask);
        }
        if (data instanceof MostOfStatisticTask mostOfStatisticTask) {
            return new MostOfStatisticBingoTask(mostOfStatisticTask, parentTask);
        }
        if (data instanceof BreedTask breedTask) {
            return new BreedBingoTask(breedTask, parentTask);
        }
        if (data instanceof DeathMessageTask deathMessageTask) {
            return new DeathMessageBingoTask(deathMessageTask, parentTask);
        }
        if (data instanceof LevelTask levelTask) {
            return new LevelBingoTask(levelTask, parentTask);
        }
        Message.log("This Type of data is not supported by BingoTask: '" + data + "'!");
        return new ItemBingoTask(new ItemTask(Material.BEDROCK));
    }

    public static BingoTask<?> getBingoTask(TaskData data) {
        return getBingoTask(data, null, Set.of());
    }

    public void setVoided(boolean value)
    {
        if (isCompleted())
            return;

        voided = value;
    }

    public boolean isVoided()
    {
        return voided;
    }

    public boolean isCompleted()
    {
        return completedBy.isPresent();
    }

    public MenuItem asStack(BingoTeam team)
    {
        ItemStack item;
        MenuItem finalItem = new MenuItem(new ItemStack(Material.BEDROCK));
        // Step 1: create the item and put the new name, description and material on it.
        if (isVoided()) // VOIDED TASK
        {
            ItemText addedDesc = new ItemText(BingoTranslation.VOIDED.translate(
                    completedBy.get().getTeam().getColoredName().asLegacyString()), ChatColor.DARK_GRAY);

            ItemText itemName = new ItemText(ChatColor.DARK_GRAY, ChatColor.STRIKETHROUGH);
            itemName.addText("A", ChatColor.MAGIC);
            itemName.add(data.getItemDisplayName());
            itemName.addText("A", ChatColor.MAGIC);

            item = new ItemStack(Material.BEDROCK);
            ItemText.buildItemText(item, itemName, addedDesc);
            finalItem = new MenuItem(finalItem);
            ItemMeta meta = finalItem.getItemMeta();

            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            finalItem.setItemMeta(meta);
        }
        else if (isCompleted()) // COMPLETED TASK
        {
            String timeString = GameTimer.getTimeAsString(completedAt);

            ItemText itemName = new ItemText(ChatColor.GRAY, ChatColor.STRIKETHROUGH);
            itemName.add(data.getItemDisplayName());

            Set<ChatColor> modifiers = new HashSet<>(){{
                add(ChatColor.DARK_PURPLE);
                add(ChatColor.ITALIC);
            }};
            ItemText[] desc = BingoTranslation.COMPLETED_LORE.asItemText(modifiers,
                    new ItemText(completedBy.get().getDisplayName(),
                            completedBy.get().getTeam().getColor(), ChatColor.BOLD),
                    new ItemText(timeString, ChatColor.GOLD));
            ChatColor completedColor = completedBy.get().getTeam().getColor();
            finalItem = MenuItem.createColoredLeather(completedColor, Material.LEATHER_CHESTPLATE);
            ItemText.buildItemText(finalItem,
                    itemName,
                    desc);
            if (finalItem.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
                armorMeta.setColor(org.bukkit.Color.fromRGB(completedColor.getColor().getRed(), completedColor.getColor().getGreen(), completedColor.getColor().getBlue()));
                armorMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
                finalItem.setItemMeta(armorMeta);
            }
        }
        else // DEFAULT TASK
        {
            ItemText itemName = new ItemText(nameColor);
            itemName.add(data.getItemDisplayName());

            item = new ItemStack(material);
            ItemText.buildItemText(item,
                    itemName,
                    data.getItemDescription());

            item.setAmount(data.getStackSize());

            finalItem = new MenuItem(item);
            ItemMeta meta = finalItem.getItemMeta();

            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            finalItem.setItemMeta(meta);

            if (glowing && completedBy.isEmpty())
            {
                finalItem.setGlowing(true);
            }
        }

        return finalItem;
    }

    public static BingoTask<?> fromStack(ItemStack in)
    {
        return BingoTask.getBingoTask(null);
    }

    public boolean complete(BingoParticipant participant, long gameTime)
    {
        if (parentTask != null && parentTask.isCompleted()) {
            return false;
        }
        if (completedBy.isPresent())
            return false;

        completedBy = Optional.of(participant);
        completedAt = gameTime;
        if (parentTask != null) {
            parentTask.onChildComplete(participant, gameTime);
        }
        return true;
    }

    public Message[] onCompleteMessage(BingoParticipant completedBy, String timeString) {
        Message message = new TranslatedMessage(BingoTranslation.COMPLETED).color(ChatColor.AQUA)
                .component(data.getItemDisplayName().asComponent()).color(nameColor)
                .arg(new ItemText(completedBy.getDisplayName(), completedBy.getTeam().getColor(), ChatColor.BOLD).asLegacyString())
                .arg(timeString).color(ChatColor.WHITE);
        return new Message[]{message};
    }

    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);

        base.addExtra("\n - ");
        base.addExtra(data.getDescription());

        return base;
    }

    abstract public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask);
    public BingoTask<?> copy() {
        return this.copy(null);
    }

    public boolean hasParent() {
        return parentTask != null;
    }

    @Override
    public String toString() {
        return "BingoTask{" +
                "completedBy=" + completedBy +
                ", completedAt=" + completedAt +
                ", voided=" + voided +
                ", data=" + data +
                ", nameColor=" + nameColor +
                ", material=" + material +
                ", glowing=" + glowing +
                ", parentTask=" + parentTask +
                '}';
    }
}
