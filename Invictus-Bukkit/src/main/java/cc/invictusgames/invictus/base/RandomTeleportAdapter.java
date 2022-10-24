package cc.invictusgames.invictus.base;

import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 17.10.2020 / 23:12
 * Invictus / cc.invictusgames.invictus.spigot.base
 */

public interface RandomTeleportAdapter {

    RandomTeleportAdapter DEFAULT = (player, checkFor) -> true;

    boolean isValidTarget(Player player, Player checkFor);

}
