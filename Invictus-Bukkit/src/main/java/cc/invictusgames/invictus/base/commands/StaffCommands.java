package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.chat.ChatChannel;
import cc.invictusgames.ilib.chat.ChatService;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.chat.impl.StaffChat;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 23.06.2020 / 17:46
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class StaffCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"staffchat", "sc"},
             permission = "invictus.command.staffchat",
             description = "Send a message in staff chat.",
             playerOnly = true)
    public boolean staffchat(Player sender,
                             @Param(name = "message", defaultValue = "@toggle", wildcard = true) String message) {
        if (message.equals("@toggle")) {
            ChatChannel targetChannel = StaffChat.getInstance();
            ChatChannel chatChannel = ChatService.fromPlayer(sender);
            if (chatChannel == targetChannel)
                targetChannel = ChatService.getDefaultChannel();

            ChatService.setChatChannel(sender, targetChannel, false);
            return true;
        }

        StaffChat.getInstance().chat(sender, message);
        return true;
    }

    @Command(names = {"togglestaffmessages", "tsm", "togglenotify", "tnotify", "notify", "stfu"},
             permission = "invictus.command.togglestaffmessages",
             description = "Toggle all staff messages",
             playerOnly = true)
    public void toggleStaffMessages(Player sender) {
        InvictusSettings.STAFF_MESSAGES.set(sender, !InvictusSettings.STAFF_MESSAGES.get(sender));
        sender.sendMessage(CC.format("&6You %s &6your staff messages",
                CC.colorBoolean(InvictusSettings.STAFF_MESSAGES.get(sender))));
    }

}
