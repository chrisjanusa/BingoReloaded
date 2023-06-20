package io.github.steaf23.bingoreloaded.gui;

import io.github.steaf23.bingoreloaded.cards.CardSize;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.gui.base.MenuInventory;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.util.Message;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.List;

public class CardMenu extends MenuInventory
{
    private final CardSize size;
    private final HashMap<Integer, BingoTask> slotToTask;

    public CardMenu(CardSize cardSize, String title)
    {
        super(9 * cardSize.size, title, null);
        this.size = cardSize;
        setMaxStackSizeOverride(64);
        slotToTask = new HashMap<>();
    }

    @Override
    public void onItemClicked(InventoryClickEvent event, int slotClicked, Player player, ClickType clickType)
    {
        if (!size.taskSlots.contains(slotClicked))
            return;

        BingoTask<?> task = slotToTask.get(slotClicked);
        if (task == null)
            return;

        BaseComponent base = new TextComponent("\n");
        BaseComponent name = task.data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(task.nameColor);

        base.addExtra(name);
        for(BaseComponent description : task.data.getDescriptions()) {
            base.addExtra("\n - ");
            base.addExtra(description);
        }

        Message.sendDebugNoPrefix(base, player);
    }

    public void show(Player player, List<BingoTask<?>> tasks)
    {
        for (int i = 0; i < tasks.size(); i++)
        {
            BingoTask<?> task = tasks.get(i);
            addItem(task.asStack().copyToSlot(size.getCardInventorySlot(i)));
            slotToTask.put(i, task);
        }
        open(player);
    }

    public void setInfo(String name, String... description)
    {
        MenuItem info = new MenuItem(0, Material.MAP, name, description);
        addItem(info);
    }
}
