package io.github.steaf23.bingoreloaded.command;

import io.github.steaf23.bingoreloaded.BingoReloaded;
import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.gameloop.BingoGameManager;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.util.TranslatedMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TakeFlightCommand implements TabExecutor {
    private final BingoGameManager gameManager;
    private final int flightCap;

    public TakeFlightCommand(BingoGameManager gameManager, int flightCap) {
        this.gameManager = gameManager;
        this.flightCap = flightCap;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, String[] args) {
        if (commandSender instanceof Player p) {
            BingoSession session = gameManager.getSession(BingoReloaded.getWorldNameOfDimension(p.getWorld()));
            if (session == null)
                return false;

            TeamManager teamManager = session.teamManager;

            BingoParticipant player = teamManager.getBingoParticipant(p);
            if (player == null) {
                new TranslatedMessage(BingoTranslation.FLIGHT_NO_TEAM)
                        .color(ChatColor.RED)
                        .send(p);
                return true;
            }

            BingoTeam team = player.getTeam();
            if (team == null) {
                new TranslatedMessage(BingoTranslation.FLIGHT_NO_TEAM)
                        .color(ChatColor.RED)
                        .send(p);
                return true;
            }

            if (args.length > 2 || (!args[0].equals("take") && !args[0].equals("give"))) {
                sendUsage(p);
                return true;
            }

            if (args[0].equals("take")) {
                if (team.isInFlight(player)) {
                    new TranslatedMessage(BingoTranslation.FLIGHT_ALREADY_IN_FLIGHT)
                            .color(ChatColor.RED)
                            .send(p);
                }
                if (flightCap > team.getInFlight().size()) {
                    team.addInFlight(player);
                    ItemStack elytra = new ItemStack(Material.ELYTRA);
                    elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                    p.getInventory().setChestplate(elytra);

                    new TranslatedMessage(BingoTranslation.FLIGHT_TOOK_FLIGHT)
                            .arg(p.getDisplayName())
                            .arg("remaining flight slots")
                            .color(ChatColor.BOLD)
                            .send(team);
                    return true;
                } else if (flightCap == 1) {
                    for (BingoParticipant teammate : team.getInFlight()) {
                        new TranslatedMessage(BingoTranslation.FLIGHT_TOOK_FLIGHT)
                                .arg(p.getDisplayName())
                                .arg(teammate.getDisplayName())
                                .color(ChatColor.BOLD)
                                .send(team);
                        team.removeInFlight(teammate);
                        if (teammate.sessionPlayer().isPresent()) {
                            teammate.sessionPlayer().get().getInventory().setChestplate(new ItemStack(Material.AIR));
                        }
                    }
                    team.addInFlight(player);
                    ItemStack elytra = new ItemStack(Material.ELYTRA);
                    elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                    p.getInventory().setChestplate(elytra);


                    return true;
                } else {
                    if (args[1].isBlank()) {
                        new TranslatedMessage(BingoTranslation.FLIGHT_TEAMMATE_REQUIRED)
                                .color(ChatColor.RED)
                                .send(p);
                        return true;
                    }
                    Player teammatePlayer = Bukkit.getPlayer(args[1]);
                    BingoParticipant teammate = null;
                    if (teammatePlayer != null) {
                        teammate = teamManager.getBingoParticipant(teammatePlayer);
                    }
                    if (teammate == null) {
                        new TranslatedMessage(BingoTranslation.FLIGHT_NOT_PLAYER)
                                .color(ChatColor.RED)
                                .arg(args[1])
                                .send(p);
                    } else {
                        team.removeInFlight(teammate);
                        teammatePlayer.getInventory().setChestplate(new ItemStack(Material.AIR));
                        team.addInFlight(player);
                        ItemStack elytra = new ItemStack(Material.ELYTRA);
                        elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                        p.getInventory().setChestplate(elytra);
                        new TranslatedMessage(BingoTranslation.FLIGHT_TOOK_FLIGHT)
                                .arg(p.getDisplayName())
                                .arg(teammate.getDisplayName())
                                .color(ChatColor.BOLD)
                                .send(team);
                        return true;
                    }
                }
                return true;
            }

            if (args.length == 1) {
                sendUsage(p);
                return true;
            }

            if (args[0].equals("give")) {
                if (!team.isInFlight(player)) {
                    new TranslatedMessage(BingoTranslation.FLIGHT_NOT_IN_FLIGHT)
                            .color(ChatColor.RED)
                            .send(p);
                }
                if (args[1].isBlank()) {
                    sendUsage(p);
                    return true;
                }
                Player teammatePlayer = Bukkit.getPlayer(args[1]);
                BingoParticipant teammate = null;
                if (teammatePlayer != null) {
                    teammate = teamManager.getBingoParticipant(teammatePlayer);
                }
                if (teammate == null) {
                    new TranslatedMessage(BingoTranslation.FLIGHT_NOT_TEAMMATE)
                            .color(ChatColor.RED)
                            .arg(args[1])
                            .send(p);
                } else {
                    team.removeInFlight(player);
                    p.getInventory().setChestplate(new ItemStack(Material.AIR));
                    team.addInFlight(teammate);
                    ItemStack elytra = new ItemStack(Material.ELYTRA);
                    elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                    teammatePlayer.getInventory().setChestplate(elytra);

                    new TranslatedMessage(BingoTranslation.FLIGHT_GAVE_FLIGHT)
                            .arg(p.getDisplayName())
                            .arg(teammatePlayer.getDisplayName())
                            .color(ChatColor.BOLD)
                            .send(team);
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    private void sendUsage(Player p) {
        new TranslatedMessage(BingoTranslation.FLIGHT_USAGE)
                .send(p);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String
            alias, @NotNull String[] args) {
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
                if (team.isInFlight(player)) {
                    commandOptions.add("give");
                } else {
                    commandOptions.add("take");
                }
                return commandOptions;
            }
            if (args[0].equals("take") && args.length == 2 && !team.isInFlight(player)) {
                List<String> possibleArgs = new LinkedList<>(team
                        .getInFlight()
                        .stream()
                        .map(BingoParticipant::getDisplayName).toList()
                );
                return possibleArgs.stream().filter(arg -> arg.startsWith(args[1])).toList();
            }
            if (args[0].equals("give") && team.isInFlight(player)) {
                List<String> possibleArgs = new LinkedList<>(team
                        .getMembers()
                        .stream()
                        .filter(teammate -> !team.isInFlight(teammate))
                        .map(BingoParticipant::getDisplayName).toList()
                );
                return possibleArgs.stream().filter(arg -> arg.startsWith(args[1])).toList();
            }
            return null;
        }
        return null;
    }
}
