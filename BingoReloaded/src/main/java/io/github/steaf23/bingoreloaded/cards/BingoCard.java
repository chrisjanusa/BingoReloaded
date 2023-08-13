package io.github.steaf23.bingoreloaded.cards;


import io.github.steaf23.bingoreloaded.event.BingoMostOfStatisticProgressEvent;
import io.github.steaf23.bingoreloaded.event.ChildHavingTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.gameloop.BingoGame;
import io.github.steaf23.bingoreloaded.BingoReloaded;
import io.github.steaf23.bingoreloaded.data.BingoCardData;
import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.data.TaskListData;
import io.github.steaf23.bingoreloaded.event.BingoCardTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.event.BingoStatisticCompletedEvent;
import io.github.steaf23.bingoreloaded.gui.CardMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.tasks.*;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.*;
import io.github.steaf23.bingoreloaded.tasks.statistics.BingoStatistic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BingoCard
{
    public final CardSize size;
    public List<BingoTask<?>> tasks;

    protected final HashMap<String, CardMenu> menuPerTeam;
    protected final TeamManager teamManager;

    private static final TaskData DEFAULT_TASK = new ItemTask(Material.DIRT, 1);

    public BingoCard(MenuManager menuManager, CardSize size, TeamManager teamManager)
    {
        this.size = size;
        this.tasks = new ArrayList<>();
        this.teamManager = teamManager;
        menuPerTeam = new HashMap<>();
        for (BingoTeam team : teamManager.getActiveTeams()) {
            CardMenu menu = new CardMenu(menuManager, size, BingoTranslation.CARD_TITLE.translate(), team);
            menu.setInfo(BingoTranslation.INFO_REGULAR_NAME.translate(),
                    BingoTranslation.INFO_REGULAR_DESC.translate().split("\\n"));
            menuPerTeam.put(team.getIdentifier(), menu);
        }
    }

    public BingoCard(MenuManager menuManager, CardSize size, List<BingoTask<?>> tasks, TeamManager teamManager) {
        this.size = size;
        this.tasks = tasks;
        this.teamManager = teamManager;
        menuPerTeam = new HashMap<>();
        for (BingoTeam team : teamManager.getActiveTeams()) {
            CardMenu menu = new CardMenu(menuManager, size, BingoTranslation.CARD_TITLE.translate(), team);
            menu.setInfo(BingoTranslation.INFO_REGULAR_NAME.translate(),
                    BingoTranslation.INFO_REGULAR_DESC.translate().split("\\n"));
            menuPerTeam.put(team.getIdentifier(), menu);
        }
    }

    /**
     * Generating a bingo card has a few steps:
     *  - Create task shuffler
     *  - Create a ticketlist. This list contains a list name for each task on the card,
     *      based on how often an item from that list should appear on the card.
     *  - Using the ticketlist, pick a random task from each ticketlist entry to put on the card.
     *  - Finally shuffle the tasks and add them to the card.
     *      If the final task count is lower than the amount of spaces available on the card, it will be filled up using default tasks.
     * @param cardName
     * @param seed
     * @param activeTeams
     */
    public void generateCard(String cardName, int seed, boolean withAdvancements, boolean withStatistics, Set<BingoTeam> activeTeams)
    {
        BingoCardData cardsData = new BingoCardData();
        TaskListData listsData = cardsData.lists();
        // Create shuffler
        Random shuffler;
        if (seed == 0)
        {
            shuffler = new Random();
        }
        else
        {
            shuffler = new Random(seed);
        }

        // Create ticketList
        List<String> ticketList = new ArrayList<>();
        for (String listName : cardsData.getListsSortedByMin(cardName))
        {
            if (listsData.getTasks(listName, withStatistics, withAdvancements).size() == 0) // Skip empty task lists.
            {
                continue;
            }

            int proportionalMin = Math.max(1, (int)((float)cardsData.getListMin(cardName, listName) * size.fullCardSize / CardSize.X5.fullCardSize));
            for (int i = 0; i < proportionalMin; i++)
            {
                ticketList.add(listName);
            }
        }
        List<String> overflowList = new ArrayList<>();
        for (String listName : cardsData.getListNames(cardName))
        {
            int proportionalMin = Math.max(1, (int)((float)cardsData.getListMin(cardName, listName) * size.fullCardSize / CardSize.X5.fullCardSize));
            int proportionalMax = (int)((float)cardsData.getListMax(cardName, listName) * size.fullCardSize / CardSize.X5.fullCardSize);

            for (int i = 0; i < proportionalMax - proportionalMin; i++)
            {
                overflowList.add(listName);
            }
        }
        Collections.shuffle(ticketList, shuffler);
        Collections.shuffle(overflowList, shuffler);
        ticketList.addAll(overflowList);
        if (ticketList.size() > size.fullCardSize)
            ticketList = ticketList.subList(0, size.fullCardSize);

        // Pick random tasks
        List<TaskData> newTasks = new ArrayList<>();
        Map<String, List<TaskData>> allTasks = new HashMap<>();
        for (String listName : ticketList)
        {
            if (!allTasks.containsKey(listName))
            {
                List<TaskData> listTasks = new ArrayList<>(listsData.getTasks(listName, withStatistics, withAdvancements));
                if (listTasks.size() == 0) // Skip empty task lists.
                {
                    continue;
                }
                Collections.shuffle(listTasks, shuffler);
                allTasks.put(listName, listTasks);
            }
            newTasks.add(allTasks.get(listName).remove(allTasks.get(listName).size() - 1));
        }
        while (newTasks.size() < size.fullCardSize)
        {
            newTasks.add(DEFAULT_TASK);
        }
        newTasks = newTasks.subList(0, size.fullCardSize);

        // Shuffle and add tasks to the card.
        Collections.shuffle(newTasks, shuffler);
        newTasks.forEach(item ->
                tasks.add(BingoTask.getBingoTask(item, null, activeTeams))
        );
    }

    public void showInventory(Player player)
    {
        menuPerTeam.get(teamManager.getBingoParticipant(player).getTeam().getIdentifier()).show(player, tasks);
    }

    public boolean hasBingo(BingoTeam team)
    {
        //check for rows and columns
        for (int y = 0; y < size.size; y++)
        {
            boolean completedRow = true;
            boolean completedCol = true;
            for (int x = 0; x < size.size; x++)
            {
                int indexRow = size.size * y + x;
                Optional<BingoParticipant> completedBy = tasks.get(indexRow).completedBy;
                if (completedBy.isEmpty() || !team.getMembers().contains(completedBy.get()))
                {
                    completedRow = false;
                }

                int indexCol = size.size * x + y;
                completedBy = tasks.get(indexCol).completedBy;
                if (completedBy.isEmpty() || !team.getMembers().contains(completedBy.get()))
                {
                    completedCol = false;
                }
            }

            if (completedRow || completedCol)
            {
                return true;
            }
        }

        // check for diagonals
        boolean completedDiagonal1 = true;
        for (int idx = 0; idx < size.fullCardSize; idx += size.size + 1) {
            Optional<BingoParticipant> completedBy = tasks.get(idx).completedBy;
            if (completedBy.isEmpty() || !team.getMembers().contains(completedBy.get()))
            {
                completedDiagonal1 = false;
                break;
            }
        }

        boolean completedDiagonal2 = true;
        for (int idx = 0; idx < size.fullCardSize; idx += size.size - 1)
        {
            if (idx != 0 && idx != size.fullCardSize - 1)
            {
                Optional<BingoParticipant> completedBy = tasks.get(idx).completedBy;
                if (completedBy.isEmpty() || !team.getMembers().contains(completedBy.get()))
                {
                    completedDiagonal2 = false;
                    break;
                }
            }
        }
        return completedDiagonal1 || completedDiagonal2;
    }

    /**
     * @param team The team.
     * @return The amount of completed items for the given team.
     */
    public int getCompleteCount(BingoTeam team)
    {
        int count = 0;
        for (var task : tasks)
        {
            if (task.completedBy.isPresent() && team.getMembers().contains(task.completedBy.get()))
                count++;
        }

        return count;
    }

    public BingoCard copy()
    {
        BingoCard card = new BingoCard(menuPerTeam.values().stream().findFirst().get().getMenuManager(), this.size, teamManager);
        List<BingoTask<?>> newTasks = new ArrayList<>();
        for (BingoTask<?> slot : tasks)
        {
            newTasks.add(slot.copy());
        }
        card.tasks = newTasks;
        return card;
    }

    public void onInventoryClick(final InventoryClickEvent event, final BingoPlayer player, final BingoGame game)
    {
        Player p = player.sessionPlayer().get();

        if (event.getSlotType() == InventoryType.SlotType.RESULT && event.getClick() != ClickType.SHIFT_LEFT)
        {
            BingoReloaded.scheduleTask(task -> {
                ItemStack resultStack = p.getItemOnCursor();
                completeItemSlot(resultStack, player, game);
            });
            return;
        }

        BingoReloaded.scheduleTask(task -> {
            for (ItemStack stack : p.getInventory().getContents())
            {
                if (stack != null)
                {
                    stack = completeItemSlot(stack, player, game);
                }
            }

            ItemStack stack = p.getItemOnCursor();
            stack = completeItemSlot(stack, player, game);
        });
    }

    public void onPlayerCollectItem(final EntityPickupItemEvent event, final BingoPlayer player, final BingoGame game)
    {
        ItemStack stack = event.getItem().getItemStack();
        int amount = stack.getAmount();
        stack = completeItemSlot(stack, player, game);
        if (amount != stack.getAmount())
        {
            event.setCancelled(true);
            ItemStack resultStack = stack.clone();

            BingoReloaded.scheduleTask(task -> {
                player.sessionPlayer().get().getWorld().dropItem(event.getItem().getLocation(), resultStack);
                event.getItem().remove();
            });
        }
    }

    public void onAnimalBreed(final EntityBreedEvent event, final BingoPlayer player, final BingoGame game)
    {
        checkTasksForBreedProgress(tasks, event.getEntityType(), player, game);
    }

    private void checkTasksForBreedProgress(List<BingoTask<?>> tasks, EntityType breedAnimalType, BingoPlayer player, BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof BreedBingoTask breedBingoTask) {
                BreedTask data = breedBingoTask.data;
                if (breedAnimalType == data.animal()) {
                    breedBingoTask.increaseBreedCount(player, game.getGameTime());
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForBreedProgress(childHavingBingoTask.getChildTasksForPlayer(player), breedAnimalType, player, game);
            }
        }
    }

    public void onPlayerDeath(final PlayerDeathEvent event, final BingoPlayer player, final BingoGame game)
    {
        checkTasksForPlayerDeath(tasks, event.getDeathMessage(), player, game);
    }

    private void checkTasksForPlayerDeath(List<BingoTask<?>> tasks, String deathMessage, BingoPlayer player, BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof DeathMessageBingoTask deathMessageBingoTask) {
                DeathMessageTask data = deathMessageBingoTask.data;
                if (deathMessage.contains(data.deathMessage())) {
                    if (deathMessageBingoTask.complete(player, game.getGameTime())) {
                        var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                        Bukkit.getPluginManager().callEvent(slotEvent);
                    }
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForPlayerDeath(childHavingBingoTask.getChildTasksForPlayer(player), deathMessage, player, game);
            }
        }
    }

    public void onPlayerLevelUp(final PlayerLevelChangeEvent event, final BingoPlayer player, final BingoGame game)
    {
        checkTasksForPlayerLevel(tasks, event.getNewLevel(), player, game);
    }

    private void checkTasksForPlayerLevel(List<BingoTask<?>> tasks, int newLevel, BingoPlayer player, BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof LevelBingoTask levelBingoTask) {
                LevelTask data = levelBingoTask.data;
                if (data.level() <= newLevel) {
                    if (levelBingoTask.complete(player, game.getGameTime())) {
                        var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                        Bukkit.getPluginManager().callEvent(slotEvent);
                    }
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForPlayerLevel(childHavingBingoTask.getChildTasksForPlayer(player), newLevel, player, game);
            }
        }
    }

    public void onPlayerDroppedItem(final PlayerDropItemEvent event, final BingoPlayer player, final BingoGame game)
    {
        BingoReloaded.scheduleTask(task -> {
            ItemStack stack = event.getItemDrop().getItemStack();
            stack = completeItemSlot(stack, player, game);
        });
    }

    ItemStack completeItemSlot(ItemStack item, BingoPlayer player, BingoGame game)
    {
        if (player.sessionPlayer().isEmpty()) {
            return item;
        }

        if (player.getTeam().outOfTheGame) {
            return item;
        }

        BingoTask deathMatchTask = game.getDeathMatchTask();
        if (deathMatchTask != null)
        {
            if (item.getType().equals(deathMatchTask.material))
            {
                var slotEvent = new BingoCardTaskCompleteEvent(deathMatchTask, player, true);
                Bukkit.getPluginManager().callEvent(slotEvent);
            }
            return item;
        }

        checkTasksForItemCompletion(tasks, item, player, game);

        return item;
    }

    private void checkTasksForItemCompletion(List<BingoTask<?>> tasks, ItemStack item, BingoPlayer player, BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof ItemBingoTask itemBingoTask) {
                ItemTask data = itemBingoTask.data;
                if (data.material().equals(item.getType()) && data.count() <= item.getAmount()) {
                    if (!task.complete(player, game.getGameTime())) {
                        continue;
                    }
                    if (game.getConfig().removeTaskItems) {
                        item.setAmount(item.getAmount() - data.getCount());
                        player.sessionPlayer().get().updateInventory();
                    }
                    var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                    Bukkit.getPluginManager().callEvent(slotEvent);
                    break;
                }
            } else if (task instanceof MostOfItemBingoTask mostOfItemBingoTask) {
                MostOfItemTask data = mostOfItemBingoTask.data;
                if (data.materials().stream().anyMatch(material -> material.equals(item.getType()))) {
                    mostOfItemBingoTask.increaseCount(player, item.getAmount(), game.getGameTime());
                    item.setAmount(0);
                    player.sessionPlayer().get().updateInventory();
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForItemCompletion(childHavingBingoTask.getChildTasksForPlayer(player), item, player, game);
            }
        }
    }

    public void onPlayerAdvancementDone(final PlayerAdvancementDoneEvent event, final BingoPlayer player, final BingoGame game)
    {
        if (player.getTeam().outOfTheGame)
            return;

        if (game.getDeathMatchTask() != null)
            return;
        checkTasksForAdvancement(tasks, event, player, game);
    }

    private void checkTasksForAdvancement(List<BingoTask<?>> tasks, final PlayerAdvancementDoneEvent event, final BingoPlayer player, final BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof AdvancementBingoTask advancementBingoTask) {
                AdvancementTask data = advancementBingoTask.data;

                if (data.advancement().equals(event.getAdvancement())) {
                    if (!task.complete(player, game.getGameTime()))
                        continue;

                    var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                    Bukkit.getPluginManager().callEvent(slotEvent);
                }
            } else if (task instanceof AnyAdvancementsBingoTask anyAdvancementsBingoTask) {
                anyAdvancementsBingoTask.advancementCompleted(player, event.getAdvancement(), game.getGameTime());
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForAdvancement(childHavingBingoTask.getChildTasksForPlayer(player), event, player, game);
            }
        }
    }

    public void onPlayerStatIncrement(final PlayerStatisticIncrementEvent event, final BingoPlayer player, final BingoGame game) {

        if (player.getTeam().outOfTheGame)
            return;

        if (game.getDeathMatchTask() != null)
            return;

        checkTasksForStatistic(tasks, event, player, game);
    }

    private void checkTasksForStatistic(List<BingoTask<?>> tasks, final PlayerStatisticIncrementEvent event, final BingoPlayer player, final BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof StatisticBingoTask statisticBingoTask) {
                StatisticTask data = statisticBingoTask.data;
                if (data.statistic().equals(new BingoStatistic(event.getStatistic(), event.getEntityType(), event.getMaterial())) &&
                        data.count() == event.getNewValue()) {
                    if (!task.complete(player, game.getGameTime()))
                        continue;

                    var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                    Bukkit.getPluginManager().callEvent(slotEvent);
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForStatistic(childHavingBingoTask.getChildTasksForPlayer(player), event, player, game);
            }
        }
    }

    public void onPlayerStatisticCompleted(final BingoStatisticCompletedEvent event, final BingoPlayer player, final BingoGame game) {
        if (player.getTeam().outOfTheGame)
            return;

        if (game.getDeathMatchTask() != null)
            return;

        checkTasksForStatistic(tasks, event, player, game);
    }

    public void onPlayerStatisticProgressed(final BingoMostOfStatisticProgressEvent event, final BingoPlayer player, final BingoGame game) {
        if (player.getTeam().outOfTheGame)
            return;

        if (game.getDeathMatchTask() != null)
            return;

        checkTasksForMostOfStatistic(tasks, event, player, game);
    }

    private void checkTasksForMostOfStatistic(List<BingoTask<?>> tasks, final BingoMostOfStatisticProgressEvent event, final BingoPlayer player, final BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof MostOfStatisticBingoTask mostOfStatisticBingoTask) {

                MostOfStatisticTask data = mostOfStatisticBingoTask.data;
                if (data.statistic().equals(event.stat)) {
                    mostOfStatisticBingoTask.increaseCount(player, event.progress, game.getGameTime());
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForMostOfStatistic(childHavingBingoTask.getChildTasksForPlayer(player), event, player, game);
            }
        }
    }

    private void checkTasksForStatistic(List<BingoTask<?>> tasks, final BingoStatisticCompletedEvent event, final BingoPlayer player, final BingoGame game) {
        for (BingoTask<?> task : tasks) {
            if (task instanceof StatisticBingoTask statisticBingoTask) {

                StatisticTask data = statisticBingoTask.data;
                if (data.statistic().equals(event.stat)) {
                    if (!task.complete(player, game.getGameTime()))
                        continue;

                    var slotEvent = new BingoCardTaskCompleteEvent(task, player, hasBingo(player.getTeam()));
                    Bukkit.getPluginManager().callEvent(slotEvent);
                    break;
                }
            } else if (task instanceof ChildHavingBingoTask<?> childHavingBingoTask) {
                checkTasksForStatistic(childHavingBingoTask.getChildTasksForPlayer(player), event, player, game);
            }
        }
    }

    public void onChildHavingTaskComplete(ChildHavingTaskCompleteEvent event) {
        var slotEvent = new BingoCardTaskCompleteEvent(event.getTask(), event.getParticipant(), hasBingo(event.getParticipant().getTeam()));
        Bukkit.getPluginManager().callEvent(slotEvent);
    }
}
