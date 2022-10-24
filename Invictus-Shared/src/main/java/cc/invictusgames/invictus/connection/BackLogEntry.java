package cc.invictusgames.invictus.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;

/**
 * @author langgezockt (langgezockt@gmail.com)
 * 02.03.2021 / 18:08
 * Invictus / cc.invictusgames.invictus.spigot.connection
 */

@RequiredArgsConstructor
public abstract class BackLogEntry {

    @Getter
    private final Request.Builder builder;

    public abstract void onSend(RequestResponse response);

}
