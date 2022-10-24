package cc.invictusgames.invictus.punishment.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PunishmentCreatePacket implements Packet {

    private static final InvictusBukkit INVICTUS = InvictusBukkit.getBukkitInstance();

    private UUID uuid;
    private Punishment.PunishmentType type;
    private String executor;
    private String reason;
    private String serverType;
    private String server;
    private long duration;
    Set<String> flags;

    @Override
    public void receive() {
        INVICTUS.getProfileService().loadProfile(uuid, profile -> {
            ChatMessage message = new ChatMessage(CC.format(
                    "%s&a was%s&a %s by %s&a.",
                    profile.getRealDisplayName(),
                    flags.contains("silent") ? " " + CC.YELLOW + "silently" : "",
                    type.getContext().toLowerCase(),
                    executor
            ));

            message.hoverText(
                    (type == Punishment.PunishmentType.BAN || type == Punishment.PunishmentType.MUTE ?
                            CC.format("&eDuration: &c%s\n", TimeUtils.formatTimeShort(duration)) : "") +
                            CC.format("&eReason: &c%s\n", reason) +
                            CC.format("&eAdded On: &c%s", serverType
                                    + (server.isEmpty() ? "" : CC.YELLOW + " : " + CC.RED + server))
            );

            for (Player current : Bukkit.getOnlinePlayers()) {
                if (current.hasPermission("invictus.staff") && InvictusSettings.STAFF_MESSAGES.get(current))
                    message.send(current);
                else if (!flags.contains("silent")) current.sendMessage(message.build().toLegacyText());
            }
            
            message.send(Bukkit.getConsoleSender());

            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                return;

            Tasks.run(() -> {
                if (type == Punishment.PunishmentType.MUTE) {
                    if (duration == -1)
                        player.sendMessage(CC.format("&cYou have been muted permanently due to &e%s&c.", reason));
                    else player.sendMessage(CC.format(
                            "&cYou have been muted for &e%s &cdue to &e%s&c. This punishment expires in &e%s&c.",
                            TimeUtils.formatDetailed(duration),
                            reason,
                            TimeUtils.formatDetailed(duration)
                    ));
                }

                if (type == Punishment.PunishmentType.BAN) {
                    if (flags.contains("clear")) {
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                    }

                    if (duration == -1)
                        player.kickPlayer(CC.format(
                                "&cYour account has been suspended from the %s Network.\n\n"
                                        + "&cIf you feel this punishment is unfair, you may appeal at &e%s&c.",
                                INVICTUS.getMessageService().formatMessage("network-name"),
                                INVICTUS.getMessageService().formatMessage("discord-link")
                        ));
                    else player.kickPlayer(CC.format(
                            "&cYour account has been suspended from the %s Network.\n" +
                                    "&cThis punishment expires in &e%s&c.\n\n" +
                                    "&cIf you feel this punishment is unfair, you may appeal at &e%s&c.",
                            INVICTUS.getMessageService().formatMessage("network-name"),
                            TimeUtils.formatDetailed(duration),
                            INVICTUS.getMessageService().formatMessage("discord-link")
                    ));
                }

                if (type == Punishment.PunishmentType.BLACKLIST) {
                    player.kickPlayer(CC.format(
                            "&cYour account has been blacklisted from the %s Network.\n\n" +
                                    "&cThis type of punishment cannot be appealed.",
                            INVICTUS.getMessageService().formatMessage("network-name")
                    ));
                }

                if (type == Punishment.PunishmentType.WARN) {
                    if (flags.contains("kick"))
                        player.kickPlayer(CC.format("&4&lYou have been warned: &e&l%s", reason));
                    else player.sendMessage(new String[]{
                            " ",
                            " ",
                            CC.format("&4&lYou have been warned: &e&l%s", reason),
                            " ",
                            " "
                    });
                }

                if (type == Punishment.PunishmentType.KICK) {
                    player.kickPlayer(CC.format(
                            "&cYou have been kicked.\n\n"
                                    + "&cReason: &e%s",
                            reason
                    ));
                }
            });

        }, true);
    }
}
