package cc.invictusgames.invictus.profile.parameter;

import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.command.parameter.defaults.PlayerParameter;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 05.03.2021 / 06:28
 * Invictus / cc.invictusgames.invictus.profile.parameter
 */

@RequiredArgsConstructor
public class ProfileParameter implements ParameterType<Profile> {

    private final Invictus invictus;

    @Override
    public Profile parse(CommandSender sender, String source) {
        if (Bukkit.isPrimaryThread()) {
            sender.sendMessage(CC.RED + "Cannot use ProfileParameter on primary thread. Please inform server" +
                    " administration to mark issued command as async.");
            return null;
        }

        if (source.equals("@self") && sender instanceof Player) {
            return invictus.getProfileService().getProfile((Player) sender);
        }

        if (Bukkit.getPlayer(source) != null)
            return invictus.getProfileService().getProfile(Bukkit.getPlayer(source));

        UUID uuid = UUIDUtils.isUUID(source) ? UUID.fromString(source) : UUIDCache.getUuid(source);
        if (uuid == null) {
            sender.sendMessage(CC.format("&e%s &chas never joined the server.", source));
            return null;
        }

        Profile profile = invictus.getProfileService().getProfile(uuid);
        if (profile != null)
            return profile;

        RequestResponse response = RequestHandler.get("profile/%s", uuid.toString());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not load profile of &e%s&c: %s (%d)",
                    source, response.getErrorMessage(), response.getCode()));
            return null;
        }

        profile = new Profile(invictus, response.asObject());
        invictus.getProfileService().cacheProfile(profile);
        return profile;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> flags) {
        return PlayerParameter.TAB_COMPLETE_FUNCTION.apply(sender, flags);
    }
}
