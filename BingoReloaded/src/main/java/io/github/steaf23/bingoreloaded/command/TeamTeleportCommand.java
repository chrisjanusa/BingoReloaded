package io.github.steaf23.bingoreloaded.command;

import io.github.steaf23.bingoreloaded.BingoReloaded;
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
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TeamTeleportCommand implements TabExecutor
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
            BingoSession session = gameManager.getSession(BingoReloaded.getWorldNameOfDimension(p.getWorld()));
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

            if (args.length != 2 || (!args[0].equals("to") && !args[0].equals("save"))) {
                sendUsage(p);
                return true;
            }

            if (args[0].equals("save")) {
                if (args[1].isBlank()) {
                    sendUsage(p);
                } else {
                    team.saveLocation(args[1], p.getLocation());
                    new TranslatedMessage(BingoTranslation.TP_SAVED_LOCATION)
                            .arg(args[1])
                            .color(ChatColor.BOLD)
                            .send(p);
                }
                return true;
            }

            if (args[0].equals("to")) {
                Player teammate = Bukkit.getPlayer(args[1]);
                if (teammate == null) {
                    Location savedLoc = team.getSavedLocation(args[1]);
                    if (savedLoc == null) {
                        new TranslatedMessage(BingoTranslation.TP_NOT_PLAYER)
                                .color(ChatColor.RED)
                                .arg(args[0])
                                .send(p);
                    } else {
                        p.teleport(savedLoc);
                    }
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
        }
        return false;
    }

    private void sendUsage(Player p) {
        new TranslatedMessage(BingoTranslation.TP_USAGE)
                .arg("/btp to <teammate>")
                .color(ChatColor.RED)
                .arg("/btp to <location_name>")
                .color(ChatColor.RED)
                .arg("/btp back")
                .color(ChatColor.RED)
                .arg("/btp save <location_name>")
                .color(ChatColor.RED)
                .send(p);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player p) {
            BingoSession session = gameManager.getSession(BingoReloaded.getWorldNameOfDimension(p.getWorld()));
            if (session == null)
                return null;

            TeamManager teamManager = session.teamManager;

            BingoParticipant player = teamManager.getBingoParticipant(p);
            if (player == null) {
                return null;
            }
            BingoTeam team = player.getTeam();
            if (team == null) {
                return null;
            }
            if (args.length == 1) {
                ArrayList<String> commandOptions = new ArrayList<>();
                commandOptions.add("to");
                commandOptions.add("save");
                String backCommand = "back";
                if (teleportBackEnabled && player.preTeleportLocation() != null) {
                    commandOptions.add(backCommand);
                }
                return commandOptions.stream().filter(arg -> arg.startsWith(args[0])).toList();
            }
            if (args[0].equals("to") && args.length == 2) {
                List<String> possibleArgs = new LinkedList<>(team
                        .getMembers()
                        .stream()
                        .map(BingoParticipant::getDisplayName).toList()
                );
                possibleArgs.remove(p.getDisplayName());
                possibleArgs.addAll(team.getSavedLocationNames());
                return possibleArgs.stream().filter(arg -> arg.startsWith(args[1])).toList();
            }
            if (args[0].equals("save") && args.length == 2) {
                return List.of("<location_name>");
            }
            return null;
        }
        return null;
    }
}
