package io.github.steaf23.bingoreloaded.cards;

import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.settings.BingoGamemode;

public class CardBuilder
{
    public static BingoCard fromMode(MenuManager menuManager, BingoGamemode mode, CardSize size, TeamManager teamManager)
    {
        return switch (mode)
                {
                    case LOCKOUT -> new LockoutBingoCard(menuManager, size, teamManager);
                    case COMPLETE -> new CompleteBingoCard(menuManager, size, teamManager);
                    default -> new BingoCard(menuManager, size, teamManager);
                };
    }
}
