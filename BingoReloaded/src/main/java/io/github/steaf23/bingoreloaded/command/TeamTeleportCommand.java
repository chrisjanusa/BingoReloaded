package io.github.steaf23.bingoreloaded.command;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.gameloop.BingoGameManager;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TeamTeleportCommand implements CommandExecutor
{
    private final BingoGameManager gameManager;
    private final Boolean teleportBackEnabled;

    public TeamTeleportCommand(BingoGameManager gameManager, boolean teleportBack)
    {
        this.gameManager = gameManager;
        teleportBackEnabled = teleportBack;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, String[] args)
    {
        if (commandSender instanceof Player p)
        {
            BingoSession session = gameManager.getSession(p);
            if (session == null)
                return false;

            TeamManager teamManager = session.teamManager;

            BingoParticipant player = teamManager.getBingoParticipant(p);
            if (player == null) {
                new TranslatedMessage(BingoTranslation.TP_NO_TEAM)
                        .color(ChatColor.RED)
                        .send(p);
                return true;
            }

            BingoTeam team = player.getTeam();
            if (team == null) {
                new TranslatedMessage(BingoTranslation.TP_NO_TEAM)
                        .color(ChatColor.RED)
                        .send(p);
                return true;
            }

            if (teleportBackEnabled && args.length == 1 && args[0].equals("back")) {
                Location preTeleportLocation = player.preTeleportLocation();
                if (player.preTeleportLocation() != null) {
                    p.teleport(preTeleportLocation);
                    if (player instanceof BingoPlayer) {
                        ((BingoPlayer) player).removePreTeleportLocation();
                    }
                }
                return true;
            }

            if (args.length != 2 || !args[0].equals("to")) {
                new TranslatedMessage(BingoTranslation.TP_USAGE)
                        .arg("/btp to <teammate>")
                        .color(ChatColor.RED)
                        .arg("/btp back")
                        .color(ChatColor.RED)
                        .send(p);
                return true;
            }

            Player teammate = Bukkit.getPlayer(args[1]);
            if (teammate == null) {
                new TranslatedMessage(BingoTranslation.TP_NOT_PLAYER)
                        .color(ChatColor.RED)
                        .arg(args[0])
                        .send(p);
                return true;
            }
            BingoParticipant bingoTeammate = teamManager.getBingoParticipant(teammate);
            if (!team.getMembers().contains(bingoTeammate)) {
                new TranslatedMessage(BingoTranslation.TP_NOT_TEAMMATE)
                        .color(ChatColor.RED)
                        .arg(args[0])
                        .send(p);
                return true;
            }
            if (player instanceof BingoPlayer) {
                ((BingoPlayer) player).setPreTeleportLocation(p.getLocation());
            }
            p.teleport(teammate);
        }
        return false;
    }
}
