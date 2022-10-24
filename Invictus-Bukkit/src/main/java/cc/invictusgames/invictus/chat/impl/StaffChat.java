package cc.invictusgames.invictus.chat.impl;

import cc.invictusgames.ilib.chat.ChatChannel;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class StaffChat extends ChatChannel {

    @Getter
    private static final StaffChat instance = new StaffChat(InvictusBukkit.getBukkitInstance());

    private final InvictusBukkit invictus;

    public StaffChat(InvictusBukkit invictus) {
        super("staff",
                CC.BLUE + "Staff",
                "invictus.command.staffchat",
                Collections.singletonList("sc"),
                '?',
                10);
        this.invictus = invictus;
    }

    @Override
    public String getFormat(Player player, CommandSender commandSender) {
        return null;
    }

    @Override
    public boolean onChat(Player player, String s) {
        return false;
    }

    @Override
    public void chat(Player player, String message) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (!InvictusSettings.STAFF_MESSAGES.get(player))
            player.sendMessage(CC.YELLOW + "Your message has been sent.");

        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                invictus.getMessageService().formatMessage(
                        "staff.chat",
                        invictus.getServerName(),
                        profile.getRealDisplayName(),
                        CC.strip(message)
                ),
                "invictus.staff",
                true
        ));
    }
}
