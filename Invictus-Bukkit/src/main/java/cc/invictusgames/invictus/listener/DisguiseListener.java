package cc.invictusgames.invictus.listener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisguiseListener /*implements TinyProtocol*/ {

   /* private static final Field B_FIELD = ReflectionUtil.getField(PacketPlayOutNamedEntitySpawn.class, "b");

    private final InvictusBukkit invictus;

    @Override
    public Packet handleIncomingPacket(Player player, Packet packet, io.netty.channel.ChannelHandlerContext channelHandlerContext) {
        return packet;
    }

    @Override
    public Packet handleOutgoingPacket(Player player, Packet packet, io.netty.channel.ChannelHandlerContext channelHandlerContext) {
        if (packet instanceof PacketPlayOutNamedEntitySpawn) {
            //Debugger.debug("a");
            PacketPlayOutNamedEntitySpawn entitySpawn = (PacketPlayOutNamedEntitySpawn) packet;

            GameProfile gameProfile = ReflectionUtil.getFieldValue(B_FIELD, entitySpawn);
            GameProfile copy = fakePlayerInfo(gameProfile, true);
            if (copy == null)
                return packet;
            //Debugger.debug("b");

            ReflectionUtil.setFieldValue(B_FIELD, entitySpawn, copy);
            //Debugger.debug(copy.getId() + ":" + copy.getName());
            return packet;
        }

        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo playerInfo = (PacketPlayOutPlayerInfo) packet;
            GameProfile gameProfile = playerInfo.getAction()
            GameProfile copy = fakePlayerInfo(gameProfile, false);
            if (copy == null)
                return packet;

            playerInfo.player = copy;
            return packet;
        }

        return packet;
    }*/

 /*   private GameProfile fakePlayerInfo(GameProfile gameProfile, boolean debug) {
        debug = false;
        if (gameProfile == null || gameProfile.getId() == null)
            return null;

        Profile profile = invictus.getProfileService().getProfile(gameProfile.getId());
        if (debug)
            Debugger.debug(gameProfile.getId() + "");
        if (profile == null || !profile.isDisguised()) {
            if (debug) {
                Debugger.debug("profile==null? " + (profile == null));
                if (profile != null)
                    Debugger.debug("disguised? " + (profile.isDisguised()));
            }
            return null;
        }

        GameProfile copy = new GameProfile(UUID.fromString("1403a220-1552-47a3-bd9a-3c38ee4869e0"),
                profile.getDisguiseData().getDisguiseName());

        copy.getProperties().putAll(gameProfile.getProperties());
        copy.getProperties().removeAll("textures");

        Property texture = new Property("textures",
                profile.getDisguiseData().getTexture(),
                profile.getDisguiseData().getSignature());

        copy.getProperties().put("textures", texture);
        return copy;
    }
*/
}
