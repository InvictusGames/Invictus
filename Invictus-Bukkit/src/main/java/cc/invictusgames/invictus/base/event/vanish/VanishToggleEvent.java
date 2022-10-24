package cc.invictusgames.invictus.base.event.vanish;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 02.07.2020 / 17:47
 * Invictus / cc.invictusgames.invictus.spigot.base.staffmode
 */

public class VanishToggleEvent extends PlayerEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();

    @Getter
    private final boolean enabled;
    private boolean cancelled = false;

    public VanishToggleEvent(Player who, boolean enabled) {
        super(who);
        this.enabled = enabled;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
