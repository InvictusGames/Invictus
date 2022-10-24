package cc.invictusgames.invictus.disguise.event;

import cc.invictusgames.invictus.rank.Rank;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Getter
public class ProfileDisguiseEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    private final String name;
    private final Rank rank;


    public ProfileDisguiseEvent(Player who, String name, Rank rank) {
        super(who);
        this.name = name;
        this.rank = rank;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
