package io.github.steaf23.bingoreloaded.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GetCommand implements CommandExecutor
{

    public GetCommand() {}

    @Override
    public boolean onCommand(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, String[] args) {
        if (commandSender instanceof Player p) {
            if (args.length == 0) return false;
            if (args.length == 2 && !args[0].equals("silk")) return false;
            if (args.length > 2) return false;
            switch (args[0]) {
                case "rockets" -> {
                    ItemStack rockets = new ItemStack(Material.FIREWORK_ROCKET, 64);
                    FireworkMeta metaData = (FireworkMeta) rockets.getItemMeta();
                    if (metaData != null) metaData.setPower(3);
                    rockets.setItemMeta(metaData);
                    p.getInventory().addItem(rockets);
                }
                case "axe" -> {
                    ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
                    axe.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                    axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                    axe.addEnchantment(Enchantment.DIG_SPEED, 5);
                    p.getInventory().addItem(axe);
                }
                case "pick" -> {
                    ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
                    pick.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                    pick.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
                    p.getInventory().addItem(pick);
                }
                case "shovel" -> {
                    ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
                    shovel.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                    shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
                    p.getInventory().addItem(shovel);
                }
                case "silk" -> {
                    if (args.length == 1) {
                        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
                        pick.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
                        pick.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
                        p.getInventory().addItem(pick);
                    } else {
                        switch (args[1]) {
                            case "axe" -> {
                                ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
                                axe.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                                axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                                axe.addEnchantment(Enchantment.DIG_SPEED, 5);
                                p.getInventory().addItem(axe);
                            }
                            case "pick" -> {
                                ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
                                pick.addEnchantment(Enchantment.SILK_TOUCH, 1);
                                pick.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
                                p.getInventory().addItem(pick);
                            }
                            case "shovel" -> {
                                ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
                                shovel.addEnchantment(Enchantment.SILK_TOUCH, 1);
                                shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
                                p.getInventory().addItem(shovel);
                            }
                        }
                    }
                }
                case "elytra" -> {
                    ItemStack elytra = new ItemStack(Material.ELYTRA);
                    elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
                    p.getInventory().addItem(elytra);
                }
                case "apple" -> p.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64));
                case "sword" -> {
                    ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
                    sword.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                    sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
                    p.getInventory().addItem(sword);
                }
                default -> {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
