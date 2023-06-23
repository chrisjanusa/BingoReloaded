package io.github.steaf23.bingoreloaded.gui;

import io.github.steaf23.bingoreloaded.cards.CardSize;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.List;

public class CardMenu extends BasicMenu
{
    private final CardSize size;
    private final HashMap<MenuItem, BingoTask<?>> slotToTask;
    public TeamManager teamManager;

    public CardMenu(MenuManager menuManager, CardSize cardSize, String title, TeamManager teamManager)
    {
        super(menuManager, title, cardSize.size);
        this.size = cardSize;
        setMaxStackSizeOverride(64);
        slotToTask = new HashMap<>();
        this.teamManager = teamManager;
    }

    @Override
    public boolean onClick(InventoryClickEvent event, HumanEntity player, MenuItem clickedItem, ClickType clickType) {
        if (!size.taskSlots.contains(event.getRawSlot()))
            return true;

        BingoTask<?> task = slotToTask.get(clickedItem);
        if (task == null)
            return true;
        BingoTeam team = teamManager.getBingoParticipant((Player) player).getTeam();
        Message.sendDebugNoPrefix(task.getOnClickMessage(team), (Player) player);

        return super.onClick(event, player, clickedItem, clickType);
    }

    public void show(Player player, List<BingoTask<?>> tasks)
    {
        for (int i = 0; i < tasks.size(); i++)
        {
            BingoTask<?> task = tasks.get(i);
            MenuItem item = task.asStack();
            addItem(item.copyToSlot(size.getCardInventorySlot(i)));
            slotToTask.put(item, task);
        }
        open(player);
    }

    public void setInfo(String name, String... description)
    {
        MenuItem info = new MenuItem(0, Material.MAP, name, description);
        addItem(info);
    }
}
