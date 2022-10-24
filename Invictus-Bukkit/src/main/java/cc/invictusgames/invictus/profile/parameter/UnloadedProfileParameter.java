package cc.invictusgames.invictus.profile.parameter;

import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.UnloadedProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 04.06.2020 / 11:16
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class UnloadedProfileParameter implements ParameterType<UnloadedProfile> {

    private static Invictus invictus = Invictus.getInstance();

    @Override
    public UnloadedProfile parse(CommandSender sender, String source) {
        if ((source.equals("@self")) && (sender instanceof Player)) {
            Player player = (Player) sender;
            return new UnloadedProfile(invictus, player.getUniqueId(), player.getName());
        }

        if (Bukkit.getPlayer(source) != null) {
            Player player = Bukkit.getPlayer(source);
            return new UnloadedProfile(invictus, player.getUniqueId(), player.getName());
        }

        UUID uuid = UUIDCache.getUuid(source);
        if (uuid != null) {
            return new UnloadedProfile(invictus, uuid, source);
        }

        sender.sendMessage(CC.YELLOW + source + CC.RED + " has never joined the server.");
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> flags) {
        List<String> completions = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!(sender instanceof Player)) {
                completions.add(player.getName());
            } else {
                if (((Player) sender).canSee(player)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
