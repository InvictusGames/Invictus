package cc.invictusgames.invictus.prime;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.10.2020 / 17:29
 * Invictus / cc.invictusgames.invictus.spigot.prime
 */

public interface PrimeRewardProvider {

    List<String> getCommands(Player player);

    List<String> getDescription(Player player);

    String getServerName();

}
