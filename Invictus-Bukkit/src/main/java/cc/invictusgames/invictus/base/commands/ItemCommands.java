package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.EnchantmentWrapper;
import cc.invictusgames.ilib.utils.ItemUtils;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 04.06.2020 / 12:13
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class ItemCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"item", "i", "get"},
             permission = "invictus.command.item",
             description = "Give an item to yourself")
    public boolean item(Player sender,
                        @Param(name = "item") ItemStack item,
                        @Param(name = "amount", defaultValue = "1") int amount) {
        if (amount < 1) {
            sender.sendMessage(CC.RED + "The amount must be greater than 0.");
            return false;
        }

        item.setAmount(amount);
        sender.getInventory().addItem(item);
        sender.sendMessage(CC.GOLD + "Giving " + CC.WHITE + amount + CC.GOLD + " of " + CC.WHITE + ItemUtils.getName(item) + CC.GOLD + ".");
        return true;
    }

    @Command(names = {"give"},
             permission = "invictus.command.give",
             description = "Give an item to a player")
    public boolean give(CommandSender sender,
                        @Param(name = "player") Player target,
                        @Param(name = "item") ItemStack item,
                        @Param(name = "amount", defaultValue = "1") int amount) {
        invictus.getProfileService().loadProfile(target.getUniqueId(), targetProfile -> {
            if (amount < 1) {
                sender.sendMessage(CC.RED + "The amount must be greater than 0.");
                return;
            }

            item.setAmount(amount);
            target.getInventory().addItem(item);
            if (!sender.equals(target)) {
                sender.sendMessage(CC.GOLD + "Giving " + targetProfile.getDisplayName(sender) + " " + CC.WHITE + amount + CC.GOLD + " of " + CC.WHITE + ItemUtils.getName(item) + CC.GOLD + ".");
                return;
            }
            target.sendMessage(CC.GOLD + "Giving " + CC.WHITE + amount + CC.GOLD + " of " + CC.WHITE + ItemUtils.getName(item) + CC.GOLD + ".");
        }, true);
        return true;
    }

    @Command(names = {"more"},
             permission = "invictus.command.more",
             playerOnly = true,
             description = "Give yourself more of the item in your hand")
    public boolean more(Player sender, @Param(name = "amount", defaultValue = "-1") int amount) {
        if (sender.getItemInHand() == null) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        if (amount == -1) {
            sender.getItemInHand().setAmount(64);
        } else {
            if (amount < 1) {
                sender.sendMessage(CC.RED + "The amount must be greater than 0.");
                return false;
            }
            sender.getItemInHand().setAmount(Math.min(64, sender.getItemInHand().getAmount() + amount));
        }

        sender.sendMessage(CC.GOLD + "Giving " + CC.WHITE + (amount == -1 ? 64 : amount) + CC.GOLD + " of "
                + CC.WHITE + ItemUtils.getName(sender.getItemInHand()) + CC.GOLD + ".");
        return true;
    }

    @Command(names = {"head", "skull"},
             permission = "invictus.command.head",
             playerOnly = true,
             description = "Give yourself a players head")
    public boolean head(Player sender, @Param(name = "player") String player) {
        sender.getInventory().addItem(new ItemBuilder(Material.SKULL_ITEM, 3).setSkullOwner(player).build());
        sender.sendMessage(CC.GOLD + "You were given " + CC.WHITE + player + CC.GOLD + "'s head.");
        return true;
    }

    @Command(names = {"rename"},
             permission = "invictus.command.rename",
             playerOnly = true,
             description = "Rename your held item")
    public boolean rename(Player sender, @Param(name = "name", wildcard = true) String name) {
        if ((sender.getItemInHand() == null) || (sender.getItemInHand().getType().equals(Material.AIR))) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        sender.sendMessage(CC.GOLD + "Renamed your " + CC.WHITE + ItemUtils.getName(sender.getItemInHand()) +
                CC.GOLD + " to " + CC.WHITE + CC.translate(name) + CC.GOLD + ".");
        sender.setItemInHand(new ItemBuilder(sender.getItemInHand()).setDisplayName(CC.translate(name)).build());
        return true;
    }

    @Command(names = {"enchant", "ench"},
             permission = "invictus.command.enchant",
             playerOnly = true,
             description = "Enchant an item")
    public boolean enchant(
            Player sender,
            @Param(name = "enchantment") Enchantment enchantment,
            @Param(name = "level", defaultValue = "1") int level,
            @Flag(names = {"h", "-hotbar"}, description = "Enchant your entire hotbar") boolean hotbar,
            @Flag(names = {"i", "-inventory"}, description = "Enchant your entire inventory") boolean inventory,
            @Flag(names = {"a", "-armor"}, description = "Enchant your armor") boolean armor) {

        EnchantmentWrapper wrapper = EnchantmentWrapper.fromString(enchantment.getName());
        if (level < 0) {
            sender.sendMessage(CC.RED + "The level must be 0 or greater.");
            return false;
        }

        if ((enchantment.getMaxLevel() > level) && (!sender.hasPermission("invictus.command.enchant.force"))) {
            sender.sendMessage(CC.RED + "The maximum level for " + CC.YELLOW + wrapper.getFancyName() + CC.RED
                    + " is " + CC.YELLOW + enchantment.getMaxLevel() + CC.RED + ".");
            return false;
        }

        if ((hotbar) && (!inventory)) {
            int enchanted = 0;
            for (int i = 0; i < 8; i++) {
                ItemStack item = sender.getInventory().getItem(i);
                if ((item != null) && (enchantment.canEnchantItem(item))) {
                    if (level == 0)
                        item.removeEnchantment(enchantment);
                    else item.addUnsafeEnchantment(enchantment, level);
                    enchanted++;
                }
            }

            if (enchanted == 0) {
                sender.sendMessage(CC.RED + "No items in your hotbar can be enchanted with "
                        + CC.YELLOW + wrapper.getFancyName() + CC.RED + ".");
                return false;
            }

            broadcastCommandMessage(sender, CC.GOLD + "Enchanted " + CC.WHITE + enchanted + CC.GOLD +
                    " item" + (enchanted == 1 ? "" : "s") + " in your hotbar with " + CC.WHITE + wrapper.getFancyName()
                    + CC.GOLD + " level " + CC.WHITE + level + CC.GOLD + ".");
        } else if (inventory) {
            int enchanted = 0;
            for (ItemStack item : sender.getInventory().getContents()) {
                if ((item != null) && (enchantment.canEnchantItem(item))) {
                    if (level == 0)
                        item.removeEnchantment(enchantment);
                    else item.addUnsafeEnchantment(enchantment, level);
                    enchanted++;
                }
            }

            if (enchanted == 0) {
                sender.sendMessage(CC.RED + "No items in your inventory can be enchanted with "
                        + CC.YELLOW + wrapper.getFancyName() + CC.RED + ".");
                return false;
            }

            broadcastCommandMessage(sender, CC.GOLD + "Enchanted " + CC.WHITE + enchanted + CC.GOLD +
                    " item" + (enchanted == 1 ? "" : "s") + " in your inventory with " + CC.WHITE
                    + wrapper.getFancyName() + CC.GOLD + " level " + CC.WHITE + level + CC.GOLD + ".");
        }

        if (armor) {
            int enchanted = 0;
            for (ItemStack item : sender.getInventory().getArmorContents()) {
                if ((item != null) && (enchantment.canEnchantItem(item))) {
                    if (level == 0)
                        item.removeEnchantment(enchantment);
                    else item.addUnsafeEnchantment(enchantment, level);
                    enchanted++;
                }
            }

            if (enchanted == 0) {
                sender.sendMessage(CC.RED + "Your armor can't be enchanted with "
                        + CC.YELLOW + wrapper.getFancyName() + CC.RED + ".");
                return false;
            }

            broadcastCommandMessage(sender, CC.GOLD + "Enchanted " + CC.WHITE + enchanted + CC.GOLD
                    + " item" + (enchanted == 1 ? "" : "s") + " of your armor with " + CC.WHITE + wrapper.getFancyName()
                    + CC.GOLD + " level " + CC.WHITE + level + CC.GOLD + ".");
        }

        if ((!hotbar) && (!inventory) && (!armor)) {
            ItemStack item = sender.getItemInHand();
            if ((item == null) || (item.getType().equals(Material.AIR))) {
                sender.sendMessage(CC.RED + "You must be holding an item.");
                return false;
            }

            if (!enchantment.canEnchantItem(item) && !sender.hasPermission("invictus.command.enchant.force")) {
                sender.sendMessage(CC.RED + "Your " + CC.YELLOW + ItemUtils.getName(item) + CC.RED +
                        " can't be enchanted with " + CC.YELLOW + wrapper.getFancyName() + CC.RED + ".");
                return false;
            }

            if (level == 0)
                item.removeEnchantment(enchantment);
            else item.addUnsafeEnchantment(enchantment, level);
            broadcastCommandMessage(sender,
                    CC.GOLD + "Enchanted your " + CC.WHITE + ItemUtils.getName(item) + CC.GOLD + " with " +
                            CC.WHITE + wrapper.getFancyName() + CC.GOLD + " level " + CC.WHITE + level + CC.GOLD + ".");
        }

        return true;
    }

    @Command(names = {"lore get", "lore list"},
             permission = "invictus.command.lore",
             description = "Show the lore of the item you are holding",
             playerOnly = true)
    public boolean loreGet(Player sender) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        if (!sender.getItemInHand().hasItemMeta() || !sender.getItemInHand().getItemMeta().hasLore()) {
            sender.sendMessage(CC.RED + "No lore found.");
            return false;
        }

        sender.getItemInHand().getItemMeta().getLore().forEach(sender::sendMessage);
        return true;
    }

    @Command(names = {"lore add"},
             permission = "invictus.command.lore",
             description = "Add a line to the lore of your item",
             playerOnly = true)
    public boolean loreAdd(Player sender, @Param(name = "line", wildcard = true) String line) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        ItemBuilder builder = new ItemBuilder(sender.getItemInHand());
        builder.addToLore(CC.translate(line));
        sender.setItemInHand(builder.build());

        sender.sendMessage(CC.format("&6Inserted %s &6at position &f%d&6.",
                CC.translate(line), sender.getItemInHand().getItemMeta().getLore().size()));
        return true;
    }

    @Command(names = {"lore insert"},
             permission = "invictus.command.lore",
             description = "Insert a line to the lore of your item",
             playerOnly = true)
    public boolean loreInsert(Player sender, @Param(name = "position") int position,
                              @Param(name = "line", wildcard = true) String line) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        List<String> lore = new ArrayList<>();
        if (sender.getItemInHand().hasItemMeta() && sender.getItemInHand().getItemMeta().hasLore())
            lore = sender.getItemInHand().getItemMeta().getLore();

        if (position > lore.size()) {
            sender.sendMessage(CC.format("&cLore only has &e%d &clines.", lore.size()));
            return false;
        }

        lore.add(position - 1, CC.translate(line));

        ItemBuilder builder = new ItemBuilder(sender.getItemInHand());
        builder.setLore(lore);
        sender.setItemInHand(builder.build());

        sender.sendMessage(CC.format("&6Inserted %s &6at position &f%d&6.",
                CC.translate(line), position));
        return true;
    }

    @Command(names = {"lore remove"},
             permission = "invictus.command.lore",
             description = "Remove a line from the lore of your item",
             playerOnly = true)
    public boolean loreRemove(Player sender, @Param(name = "position") int position) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        List<String> lore = new ArrayList<>();
        if (sender.getItemInHand().hasItemMeta() && sender.getItemInHand().getItemMeta().hasLore())
            lore = sender.getItemInHand().getItemMeta().getLore();

        if (position > lore.size()) {
            sender.sendMessage(CC.format("&cLore only has &e%d &clines.", lore.size()));
            return false;
        }

        lore.remove(position - 1);

        ItemBuilder builder = new ItemBuilder(sender.getItemInHand());
        builder.setLore(lore);
        sender.setItemInHand(builder.build());

        sender.sendMessage(CC.format("&6Removed entry at position &f%d&6.", position));
        return true;
    }

    @Command(names = {"lore clear"},
             permission = "invictus.command.lore",
             description = "Clear the lore of the item your holding",
             playerOnly = true)
    public boolean loreClear(Player sender) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        if (!sender.getItemInHand().hasItemMeta() || !sender.getItemInHand().getItemMeta().hasLore()) {
            sender.sendMessage(CC.RED + "No lore found.");
            return false;
        }

        ItemBuilder builder = new ItemBuilder(sender.getItemInHand());
        builder.setLore(Collections.emptyList());
        sender.setItemInHand(builder.build());

        sender.sendMessage(CC.GOLD + "Lore cleared.");
        return true;
    }

    @Command(names = {"potmod add"},
             permission = "invictus.command.potmod",
             description = "Add a custom potion effect to your item",
             playerOnly = true)
    public boolean potmodAdd(Player sender,
                             @Param(name = "effect") PotionEffectType effect,
                             @Param(name = "duration") Duration duration,
                             @Param(name = "amplifier") int amplifier) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() != Material.POTION) {
            sender.sendMessage(CC.RED + "You must be holding a potion.");
            return false;
        }

        if (duration.isPermanent()) {
            sender.sendMessage(CC.RED + "Cannot add permanent potion effect.");
            return false;
        }

        if (amplifier < 0) {
            sender.sendMessage(CC.RED + "Cannot add negative potion effect.");
            return false;
        }

        PotionMeta potionMeta = (PotionMeta) sender.getItemInHand().getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(
                effect,
                (int) (TimeUnit.MILLISECONDS.toSeconds(duration.getDuration()) * 20),
                amplifier
        ), true);
        sender.getItemInHand().setItemMeta(potionMeta);
        sender.updateInventory();
        sender.sendMessage(CC.format(
                "&6Added potion effect &f%s&6: Level &f%d &6for &f%s&6.",
                effect.getName(),
                amplifier,
                TimeUtils.formatTimeShort(duration.getDuration())
        ));
        return true;
    }

    @Command(names = {"setitemdurability", "setitemdura"},
             permission = "invictus.command.setitemdurability",
             description = "Set the durability of your item",
             playerOnly = true)
    public boolean setItemDurability(Player sender, @Param(name = "durability") short durability) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        if (!Enchantment.DURABILITY.canEnchantItem(sender.getItemInHand())) {
            sender.sendMessage(CC.RED + "This item cannot be damaged.");
            return false;
        }

        sender.getItemInHand().setDurability((short) ((sender.getItemInHand().getType().getMaxDurability() - durability) + 1));
        sender.sendMessage(CC.format(
                "&6Set durability of your &f%s &6to &f%d&6.",
                ItemUtils.getName(sender.getItemInHand()),
                sender.getItemInHand().getDurability()
        ));
        return true;
    }

    @Command(names = {"unbreakable"},
             permission = "invictus.command.unbreakable",
             description = "Make your item unbreakable",
             playerOnly = true)
    public boolean unbreakable(Player sender) {
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            sender.sendMessage(CC.RED + "You must be holding an item.");
            return false;
        }

        boolean unbreakable = sender.getItemInHand().hasItemMeta()
                && sender.getItemInHand().getItemMeta().spigot().isUnbreakable();
        ItemBuilder builder = new ItemBuilder(sender.getItemInHand());
        builder.setUnbreakable(!unbreakable);
        sender.setItemInHand(builder.build());
        sender.sendMessage(CC.format(
                "&6Set unbreakable of your &f%s &6to %s&6.",
                ItemUtils.getName(sender.getItemInHand()),
                CC.colorBoolean(!unbreakable, "yes", "no")
        ));
        return true;
    }

    private static void broadcastCommandMessage(CommandSender sender, String message) {
        org.bukkit.command.Command.broadcastCommandMessage(sender, message);
    }

}
