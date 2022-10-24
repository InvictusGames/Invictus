package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.UnloadedProfile;
import cc.invictusgames.invictus.utils.ProfileInventory;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 04.06.2020 / 11:38
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class InventoryCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"copyfrom", "cpfrom"},
             permission = "invictus.command.copyfrom",
             description = "Copy the inventory of a player",
             playerOnly = true)
    public boolean copyfrom(Player sender, @Param(name = "player") UnloadedProfile unloadedTarget) {
        unloadedTarget.load(target -> {
            unloadedTarget.loadPlayer(player -> {
                if (target == null || player == null) {
                    return;
                }

                if (player.isOnline()) {
                    sender.getInventory().setContents(target.player().getInventory().getContents());
                    sender.getInventory().setArmorContents(target.player().getInventory().getArmorContents());
                    sender.sendMessage(target.getDisplayName(sender) + CC.GOLD + "'s inventory has been applied to " +
                            "you.");
                    return;
                }

                sender.getInventory().setContents(player.getInventory().getContents());
                sender.getInventory().setArmorContents(player.getInventory().getArmorContents());
                sender.sendMessage(CC.GOLD + "Offline player " + target.getDisplayName(sender) +
                        CC.GOLD + "'s inventory has been applied to you.");
            }, true);
        }, true);
        return true;
    }

    @Command(names = {"copyto", "cpto"},
             permission = "invictus.command.copyto",
             description = "Apply your inventory to a player",
             playerOnly = true)
    public boolean copyto(Player sender, @Param(name = "player") UnloadedProfile unloadedTarget, @Flag(names = {"o",
            "-offline"}, description = "Apply your inventory to an offline player") boolean offline) {
        unloadedTarget.load(target -> {
            unloadedTarget.loadPlayer(player -> {
                if (target == null || player == null) {
                    return;
                }

                if (player.isOnline()) {
                    target.player().getInventory().setContents(sender.getInventory().getContents());
                    target.player().getInventory().setArmorContents(sender.getInventory().getArmorContents());
                    sender.sendMessage(CC.GOLD + "Your inventory has been applied to " + target.getDisplayName(sender) + CC.GOLD + ".");
                    return;
                }

                if (!offline) {
                    sender.sendMessage(CC.format("&cPlayer &e%s &cis not online.", target.getName()));
                    return;
                }

                player.getInventory().setContents(sender.getInventory().getContents());
                player.getInventory().setArmorContents(sender.getInventory().getArmorContents());
                player.saveData();
                sender.sendMessage(CC.GOLD + "Your inventory has been applied to offline player " + target.getDisplayName(sender) + CC.GOLD + ".");
            }, true);
        }, true);
        return true;
    }

    @Command(names = {"invsee"},
             permission = "invictus.command.invsee",
             description = "Edit the inventory of a player",
             playerOnly = true)
    public boolean invsee(Player sender, @Param(name = "player") UnloadedProfile unloadedTarget) {
        unloadedTarget.load(target -> {
            unloadedTarget.loadPlayer((player -> {
                if (target == null || player == null) {
                    return;
                }

                sender.openInventory(ProfileInventory.getInventory(target, player).getBukkitInventory());
                ProfileInventory.getOpen().add(sender.getUniqueId());
                sender.sendMessage(CC.GOLD + "Opening inventory of " +
                        (player.isOnline() ? "" : "offline player ") + target.getDisplayName(sender) + CC.GOLD + ".");
            }), true);
        }, true);
        return true;
    }

    @Command(names = {"clear", "clearinventory", "clearinv", "ci"},
             permission = "invictus.command.clear",
             description = "Clear the inventory of a player")
    public boolean clear(CommandSender sender,
                         @Param(name = "player", defaultValue = "@self") UnloadedProfile unloadedTarget) {
        unloadedTarget.load(target -> {
            if (target == null) {
                return;
            }

            if ((!sender.equals(target.player())) && (!sender.hasPermission("invictus.command.clear.other"))) {
                sender.sendMessage(CC.RED + "You are not allowed to clear the inventory of other players");
                return;
            }

            target.player().getInventory().clear();
            target.player().getInventory().setArmorContents(null);
            if (!sender.equals(target.player())) {
                org.bukkit.command.Command.broadcastCommandMessage(sender, target.getDisplayName(sender) + CC.GOLD +
                        "'s inventory has been cleared.");
            }
            target.player().sendMessage(CC.GOLD + "Your inventory has been cleared.");
        }, true);
        return true;
    }

}
