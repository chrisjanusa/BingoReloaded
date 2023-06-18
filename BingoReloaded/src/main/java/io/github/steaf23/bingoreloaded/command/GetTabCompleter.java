package io.github.steaf23.bingoreloaded.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class GetTabCompleter implements TabCompleter {

    public GetTabCompleter() {}

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("rockets", "axe", "pick", "shovel", "silk", "elytra", "apple", "sword")
                    .filter(arg -> arg.startsWith(args[0]))
                    .toList();
        }
        if (args.length == 2 && args[0].equals("silk")) {
            return Stream.of("axe", "pick", "shovel")
                    .filter(arg -> arg.startsWith(args[1]))
                    .toList();
        }
        return null;
    }
}
