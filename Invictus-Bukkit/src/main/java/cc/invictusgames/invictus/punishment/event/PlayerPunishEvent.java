package cc.invictusgames.invictus.punishment.event;

import cc.invictusgames.invictus.punishment.Punishment;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 02.07.2020 / 18:07
 * Invictus / cc.invictusgames.invictus.spigot.punishment.event
 */

public class PlayerPunishEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final Punishment punishment;

    public PlayerPunishEvent(Player who, Punishment punishment) {
        super(who);
        this.punishment = punishment;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
