package cc.invictusgames.invictus.disguise.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Getter
public class ProfileUnDisguiseEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    public ProfileUnDisguiseEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
