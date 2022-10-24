package cc.invictusgames.invictus.permission.adapter;

import cc.invictusgames.ilib.command.CommandService;
import cc.invictusgames.ilib.command.permission.PermissionAdapter;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 05.03.2021 / 09:31
 * Invictus / cc.invictusgames.invictus.permission.adapter
 */

public class OwnerOnlyPermission extends PermissionAdapter {

    private final Invictus invictus;

    public OwnerOnlyPermission(Invictus invictus) {
        super("owner");
        this.invictus = invictus;
    }

    @Override
    public boolean test(CommandSender sender) {
        boolean b = testSilent(sender);
        if (!b) {
            if (sender.isOp())
                sender.sendMessage(CC.RED + "This command can only be used as owner.");
            else sender.sendMessage(CommandService.NO_PERMISSION_MESSAGE);
        }
        return b;
    }

    @Override
    public boolean testSilent(CommandSender sender) {
        if (!(sender instanceof Player))
            return true;

        Profile profile = invictus.getProfileService().getProfile((Player) sender);
        return profile.getRealCurrentGrant().getRank().getWeight() >= invictus.getMainConfig().getOwnerWeight();
    }
}
