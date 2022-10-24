package cc.invictusgames.invictus.base;

import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.10.2020 / 17:55
 * Invictus / cc.invictusgames.invictus.spigot.base
 */

public interface PlayTimeGetter {

    PlayTimeGetter DEFAULT = player -> -1;

    long getPlayTime(Player player);

}
