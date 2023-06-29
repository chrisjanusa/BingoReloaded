package io.github.steaf23.bingoreloaded.cards;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.gui.CardMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;

import java.util.ArrayList;
import java.util.List;

public class CompleteBingoCard extends BingoCard {
    public CompleteBingoCard(MenuManager menuManager, CardSize size, TeamManager teamManager) {
        super(menuManager, size, teamManager);
        for (CardMenu menu : menuPerTeam.values()) {
            menu.setInfo(BingoTranslation.INFO_COMPLETE_NAME.translate(),
                    BingoTranslation.INFO_COMPLETE_DESC.translate().split("\\n"));
        }
    }

    public CompleteBingoCard(MenuManager menuManager, CardSize size, List<BingoTask<?>> tasks, TeamManager teamManager) {
        super(menuManager, size, tasks, teamManager);
        for (CardMenu menu : menuPerTeam.values()) {
            menu.setInfo(BingoTranslation.INFO_COMPLETE_NAME.translate(),
                    BingoTranslation.INFO_COMPLETE_DESC.translate().split("\\n"));
        }
    }

    @Override
    public boolean hasBingo(BingoTeam team) {
        return getCompleteCount(team) == size.fullCardSize;
    }

    @Override
    public CompleteBingoCard copy() {
        CompleteBingoCard card = new CompleteBingoCard(menuPerTeam.values().stream().findFirst().get().getMenuManager(), this.size, teamManager);
        List<BingoTask<?>> newTasks = new ArrayList<>();
        for (var t : tasks) {
            newTasks.add(t.copy());
        }
        card.tasks = newTasks;
        return card;
    }
}
