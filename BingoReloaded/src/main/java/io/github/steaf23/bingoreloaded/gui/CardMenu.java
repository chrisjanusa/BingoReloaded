package io.github.steaf23.bingoreloaded.gui;

import io.github.steaf23.bingoreloaded.cards.CardSize;
import io.github.steaf23.bingoreloaded.gui.base.MenuItem;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.util.Message;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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

    public CardMenu(MenuManager menuManager, CardSize cardSize, String title)
    {
        super(menuManager, title, cardSize.size);
        this.size = cardSize;
        setMaxStackSizeOverride(64);
        slotToTask = new HashMap<>();
    }

    @Override
    public boolean onClick(InventoryClickEvent event, HumanEntity player, MenuItem clickedItem, ClickType clickType) {
        if (!size.taskSlots.contains(event.getRawSlot()))
            return true;

        BingoTask<?> task = slotToTask.get(clickedItem);
        if (task == null)
            return true;

        BaseComponent base = new TextComponent("\n");
        BaseComponent name = task.data.getItemDisplayName().asComponent();
        name.setBold(true);
        name.setColor(task.nameColor);

        base.addExtra(name);
        BaseComponent descriptionTitle = task.data.getDescriptionsTitle();
        if (descriptionTitle != null) {
            base.addExtra("\n");
            descriptionTitle.setColor(task.nameColor);
            base.addExtra(descriptionTitle);
        }
        for(BaseComponent description : task.data.getDescriptions()) {
            base.addExtra("\n - ");
            base.addExtra(description);
        }

        Message.sendDebugNoPrefix(base, (Player) player);

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
