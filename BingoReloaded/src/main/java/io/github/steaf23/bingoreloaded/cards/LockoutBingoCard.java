package io.github.steaf23.bingoreloaded.cards;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.BingoCardTaskCompleteEvent;
import io.github.steaf23.bingoreloaded.gui.CardMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuManager;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.tasks.bingotasks.BingoTask;
import io.github.steaf23.bingoreloaded.util.Message;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;

import java.util.List;

public class LockoutBingoCard extends BingoCard
{
    public int teamCount;
    public int currentMaxTasks;

    private final TeamManager teamManager;

    public LockoutBingoCard(MenuManager menuManager, CardSize size, int teamCount, TeamManager teamManager) {
        super(menuManager, size, teamManager);
        this.currentMaxTasks = size.fullCardSize;
        this.teamCount = teamCount;
        this.teamManager = teamManager;
        for (CardMenu menu : menuPerTeam.values()) {
            menu.setInfo(BingoTranslation.INFO_LOCKOUT_NAME.translate(),
                    BingoTranslation.INFO_LOCKOUT_DESC.translate().split("\\n"));
        }
    }

    public LockoutBingoCard(MenuManager menuManager, CardSize size, List<BingoTask<?>> tasks, TeamManager teamManager, int currentMaxTasks) {
        super(menuManager, size, tasks, teamManager);
        this.teamManager = teamManager;
        this.teamCount = teamManager.getActiveTeams().size();
        this.currentMaxTasks = currentMaxTasks;
        for (CardMenu menu : menuPerTeam.values()) {
            menu.setInfo(BingoTranslation.INFO_LOCKOUT_NAME.translate(),
                    BingoTranslation.INFO_LOCKOUT_DESC.translate().split("\\n"));
        }
    }

    // Lockout cards cannot be copied since it should be the same instance for every player.
    @Override
    public LockoutBingoCard copy() {
        return this;
    }

    @Override
    public boolean hasBingo(BingoTeam team) {
        // get the completeCount of the team with the most items.
        BingoTeam leadingTeam = teamManager.getLeadingTeam();
        BingoTeam losingTeam = teamManager.getLosingTeam();

        int itemsLeft = size.fullCardSize - getTotalCompleteCount();

        // if amount on items cannot get up to amount of items of the team with the most items, this team cannot win anymore.
        if (itemsLeft + getCompleteCount(losingTeam) < getCompleteCount(leadingTeam)) {
            dropTeam(losingTeam, teamManager.getSession());
        }

        if (teamCount < 2) {
            return true;
        }

        if (teamCount > 2) {
            return false;
        }

        // Only pick a bingo winner when there are only 2 teams remaining
        int completeCount = getCompleteCount(team);
        return completeCount > (currentMaxTasks / 2);
    }

    public void dropTeam(BingoTeam team, BingoSession session) {
        if (team.outOfTheGame) {
            return;
        }
        new TranslatedMessage(BingoTranslation.DROPPED)
                .arg(team.getColoredName().asLegacyString())
                .sendAll(session);
        team.outOfTheGame = true;
        for (BingoTask<?> task : tasks) {
            if (task.isCompleted() && session.teamManager.getParticipantsOfTeam(team).contains(task.completedBy.get())) {
                task.setVoided(true);
                currentMaxTasks--;
            }
        }
        teamCount--;
    }

    public int getTotalCompleteCount() {
        int total = 0;
        for (BingoTeam t : teamManager.getActiveTeams()) {
            total += getCompleteCount(t);
        }
        return total;
    }
}
