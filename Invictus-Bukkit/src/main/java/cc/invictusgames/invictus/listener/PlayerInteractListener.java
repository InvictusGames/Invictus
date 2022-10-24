package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.RequiredArgsConstructor;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 17.04.2020 / 17:19
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class PlayerInteractListener implements Listener {

    private final InvictusBukkit invictus;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof Skull) {
                Skull skull = (Skull) block.getState();
                if (skull.getSkullType() != SkullType.PLAYER)
                    return;

                if (skull.getOwner() == null) {
                    player.sendMessage(CC.GOLD + "This is the head of: " + CC.WHITE + "Steve");
                    return;
                }

                invictus.getProfileService().loadProfile(UUIDCache.getUuid(skull.getOwner()), target -> {
                    if (target == null) {
                        player.sendMessage(CC.GOLD + "This is the head of: " + CC.WHITE + skull.getOwner());
                        return;
                    }

                    player.sendMessage(CC.GOLD + "This is the head of: " + target.getRealDisplayName());
                }, true);
            }
        }
    }

}
