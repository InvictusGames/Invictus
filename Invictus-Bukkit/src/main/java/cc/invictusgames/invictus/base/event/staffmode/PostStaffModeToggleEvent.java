package cc.invictusgames.invictus.base.event.staffmode;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 02.07.2020 / 17:49
 * Invictus / cc.invictusgames.invictus.spigot.base.staffmode
 */

public class PostStaffModeToggleEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final boolean enabled;

    public PostStaffModeToggleEvent(Player who, boolean enabled) {
        super(who);
        this.enabled = enabled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
