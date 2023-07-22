package io.github.steaf23.bingoreloaded.tasks.bingotasks;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.tasks.LastToTask;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import io.github.steaf23.bingoreloaded.util.timer.GameTimer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

public class LastToBingoTask extends ChildHavingBingoTask<LastToTask> {

    public Map<String, List<BingoTask<?>>> childrenPerTeam;

    public LastToBingoTask(LastToTask lastToTask, ChildHavingBingoTask<?> parentTask, Set<BingoTeam> activeTeams) {
        this.nameColor = ChatColor.RED;
        this.material = Material.BARRIER;
        this.glowing = true;
        this.data = lastToTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = new HashMap<>();
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
        for (BingoTeam team : activeTeams) {
            childrenPerTeam.put(team.getIdentifier(), new ArrayList<>());
        }
        for (BingoTeam team : activeTeams) {
            List<BingoTask<?>> children = childrenPerTeam.get(team.getIdentifier());
            children.add(BingoTask.getBingoTask(lastToTask.task(), this, activeTeams));
        }
    }

    public LastToBingoTask(LastToTask lastToTask, ChildHavingBingoTask<?> parentTask, Map<String, List<BingoTask<?>>> childrenPerTeam) {
        this.nameColor = ChatColor.RED;
        this.material = Material.BARRIER;
        this.glowing = true;
        this.data = lastToTask;
        this.parentTask = parentTask;
        this.childrenPerTeam = childrenPerTeam;
        this.completedBy = Optional.empty();
        this.completedAt = -1L;
    }

    @Override
    public BingoTask<?> copy(ChildHavingBingoTask<?> parentTask) {
        HashMap<String, List<BingoTask<?>>> childrenPerTeamCopy = new HashMap<>();
        LastToBingoTask taskCopy = new LastToBingoTask(data, parentTask, childrenPerTeamCopy);
        for (String teamName : childrenPerTeam.keySet()) {
            childrenPerTeamCopy.put(teamName, new ArrayList<>());
            for (BingoTask<?> child : childrenPerTeam.get(teamName)) {
                childrenPerTeamCopy.get(teamName).add(child.copy(taskCopy));
            }
        }

        if (completedBy.isPresent()) {
            taskCopy.completedBy = completedBy;
            taskCopy.completedAt = completedAt;
        }
        taskCopy.setVoided(voided);
        return taskCopy;
    }

    @Override
    public MenuItem asStack(BingoTeam team) {
        ItemStack item;
        MenuItem finalItem = new MenuItem(new ItemStack(Material.BEDROCK));
        BingoTask<?> teamTask = childrenPerTeam.get(team.getIdentifier()).get(0);

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
        else if (teamTask.isCompleted()) // Unobtainable task
        {
            final ItemText addedDesc;
            addedDesc = teamTask.completedBy
                    .map(
                            bingoParticipant ->
                                    new ItemText(
                                            "This task was failed by " + bingoParticipant.getDisplayName(),
                                            ChatColor.DARK_GRAY
                                    )
                    ).orElseGet(
                            () -> new ItemText("This task was failed", ChatColor.DARK_GRAY)
                    );

            ItemText itemName = new ItemText(ChatColor.DARK_GRAY, ChatColor.STRIKETHROUGH);
            itemName.add(data.getItemDisplayName());

            item = new ItemStack(Material.BEDROCK);
            ItemText.buildItemText(item, itemName, addedDesc);
            finalItem = new MenuItem(finalItem);
            ItemMeta meta = finalItem.getItemMeta();

            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            finalItem.setItemMeta(meta);
        } else // DEFAULT TASK
        {
            ItemText itemName = new ItemText(nameColor);
            itemName.add(data.getItemDisplayName());

            item = new ItemStack(material);
            ItemText.buildItemText(item,
                    itemName,
                    data.getItemDescription());

            item.setAmount(data.getStackSize());

            // STEP 2: Add additional stuff like pdc data and glowing effect.

            finalItem = new MenuItem(item);
            ItemMeta meta = finalItem.getItemMeta();

            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            finalItem.setItemMeta(meta);

            if (glowing && completedBy.isEmpty()) {
                finalItem.setGlowing(true);
            }
        }

        return finalItem;
    }

    @Override
    public List<BingoTask<?>> getChildTasksForPlayer(BingoParticipant participant) {
        return childrenPerTeam.get(participant.getTeam().getIdentifier());
    }

    @Override
    void onChildComplete(BingoParticipant participant, long gameTime) {
        String onlyTeamRemaining = null;
        for (String teamName : childrenPerTeam.keySet()) {
            if (!childrenPerTeam.get(teamName).get(0).isCompleted()) {
                if (onlyTeamRemaining == null) {
                    onlyTeamRemaining = teamName;
                } else {
                    return;
                }
            }
        }
        if (onlyTeamRemaining != null) {
            for (BingoTeam team : participant.getSession().teamManager.getActiveTeams()) {
                if (Objects.equals(team.getIdentifier(), onlyTeamRemaining)) {
                    team.getMembers().stream().findFirst().ifPresent(teamPlayer -> {
                        var slotEvent = new ChildHavingTaskCompleteEvent(teamPlayer, this);
                        Bukkit.getPluginManager().callEvent(slotEvent);
                        complete(teamPlayer, gameTime);
                    });
                }
            }
        }
    }

    @Override
    public Message[] onChildCompleteMessage(BingoTask<?> child, BingoParticipant completedBy, String completedAt) {
        Message completedMessage = new TranslatedMessage(BingoTranslation.FAILED).color(ChatColor.RED)
                .component(child.data.getItemDisplayName().asComponent()).color(child.nameColor)
                .arg(new ItemText(completedBy.getDisplayName(), completedBy.getTeam().getColor(), ChatColor.BOLD).asLegacyString())
                .arg(completedAt).color(ChatColor.WHITE);
        return new Message[] {completedMessage};
    }

    @Override
    public Message[] onCompleteMessage(BingoParticipant completedBy, String timeString) {
        Message completedMessage = new TranslatedMessage(BingoTranslation.LAST_TO_COMPLETED).color(ChatColor.AQUA)
                .arg(completedBy.getTeam().getColoredName().asLegacyString()).color(completedBy.getTeam().getColor())
                .arg(data.getItemDisplayName().asLegacyString()).color(nameColor)
                .arg(timeString).color(ChatColor.WHITE);
        completedMessage.createPrefixedMessage();
        return new Message[] {completedMessage};
    }

    @Override
    public BaseComponent getOnClickMessage(BingoTeam team) {
        BaseComponent base = new TextComponent("\n");
        BaseComponent name = data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(nameColor);

        base.addExtra(name);

        BaseComponent descriptionTitle = data.getDescription();
        base.addExtra("\n");
        descriptionTitle.setColor(nameColor);
        base.addExtra(descriptionTitle);
        base.addExtra("\n - ");
        base.addExtra(data.task().getItemDisplayName().asComponent());

        return base;
    }
}
