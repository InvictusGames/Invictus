package cc.invictusgames.invictus.tag.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.utils.Tasks;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 29.12.2020 / 13:42
 * Invictus / cc.invictusgames.invictus.spigot.tag.packet
 */

public class TagReloadPacket implements Packet {

    @Override
    public void receive() {
        Tasks.runAsync(() -> InvictusBukkit.getBukkitInstance().getTagService().loadTags());
    }

}
