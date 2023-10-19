package io.github.steaf23.bingoreloaded.event;

import io.github.steaf23.bingoreloaded.gameloop.BingoGame;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.cards.BingoCard;
import io.github.steaf23.bingoreloaded.cards.LockoutBingoCard;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class CardEventManager {
    private final List<BingoCard> cards;
    private String worldName;

    public CardEventManager(String worldName) {
        this.cards = new ArrayList<>();
        this.worldName = worldName;
    }

    public void setCards(List<BingoCard> newCards) {
        this.cards.clear();
        this.cards.addAll(newCards);
    }

    public void handlePlayerAdvancementCompleted(final PlayerAdvancementDoneEvent event, final BingoSession session) {
        if (event.getAdvancement().getDisplay() != null) {
            Bukkit.getLogger().log(Level.WARNING, "Advancement completed (manager): " + event.getAdvancement().getDisplay().getTitle());
        }
        BingoParticipant participant = session.teamManager.getBingoParticipant(event.getPlayer());
        if (participant == null || !(participant instanceof BingoPlayer player) || !session.isRunning())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        if (!(session.phase() instanceof BingoGame runningGame))
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerAdvancementDone(event, player, runningGame);
        }
    }

    public void handlePlayerDroppedItem(final PlayerDropItemEvent event, final BingoGame game) {
        BingoParticipant participant = game.getTeamManager().getBingoParticipant(event.getPlayer());
        if (participant == null || !(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerDroppedItem(event, player, game);
        }
    }

    public void handlePlayerPickupItem(final EntityPickupItemEvent event, final BingoGame game) {
        if (!(event.getEntity() instanceof Player p))
            return;

        BingoParticipant participant = game.getTeamManager().getBingoParticipant(p);
        if (participant == null || !(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerCollectItem(event, player, game);
        }
    }

    public void handleEntityBreed(final EntityBreedEvent event, final BingoGame game) {
        if (event.getBreeder() instanceof Player p) {
            BingoParticipant participant = game.getTeamManager().getBingoParticipant(p);
            if (participant == null || !(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
                return;

            BingoTeam team = player.getTeam();
            if (team == null)
                return;

            for (BingoCard card : cards) {
                if (team.card.equals(card))
                    card.onAnimalBreed(event, player, game);
            }
        }
    }

    public void handleInventoryClicked(final InventoryClickEvent event, final BingoGame game) {
        if (!(event.getWhoClicked() instanceof Player p))
            return;

        BingoParticipant participant = game.getTeamManager().getBingoParticipant(p);
        if (participant == null || !(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onInventoryClick(event, player, game);
        }
    }

    public void handleStatisticCompleted(final BingoStatisticCompletedEvent event, final BingoGame game) {
        if (!event.player.sessionPlayer().isPresent())
            return;

        BingoTeam team = event.player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerStatisticCompleted(event, event.player, game);
        }
    }

    public void handleStatisticCompleted(final BingoMostOfStatisticProgressEvent event, final BingoGame game) {
        if (!event.player.sessionPlayer().isPresent())
            return;

        BingoTeam team = event.player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerStatisticProgressed(event, event.player, game);
        }
    }

    public void handleChildHavingTaskComplete(ChildHavingTaskCompleteEvent event) {
        for (BingoCard card : cards) {
            card.onChildHavingTaskComplete(event);
        }
    }

    public void handlePlayerDeathEvent(PlayerDeathEvent event, BingoGame game) {
        Player p = event.getEntity().getPlayer();
        BingoParticipant participant = game.getTeamManager().getBingoParticipant(p);
        if (!(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerDeath(event, player, game);
        }
    }

    public void handlePlayerLevelChangeEvent(PlayerLevelChangeEvent event, BingoGame game) {
        Player p = event.getPlayer();
        BingoParticipant participant = game.getTeamManager().getBingoParticipant(p);
        if (!(participant instanceof BingoPlayer player) || !player.sessionPlayer().isPresent())
            return;

        BingoTeam team = player.getTeam();
        if (team == null)
            return;

        for (BingoCard card : cards) {
            if (team.card.equals(card))
                card.onPlayerLevelUp(event, player, game);
        }
    }
}
