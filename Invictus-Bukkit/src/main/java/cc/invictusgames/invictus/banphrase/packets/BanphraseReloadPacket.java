package cc.invictusgames.invictus.banphrase.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 21:12
 * Invictus / cc.invictusgames.invictus.banphrase.packets
 */

public class BanphraseReloadPacket implements Packet {

    @Override
    public void receive() {
        InvictusBukkit.getBukkitInstance().getBanphraseService().loadBanphrases(() -> { });
    }
}
