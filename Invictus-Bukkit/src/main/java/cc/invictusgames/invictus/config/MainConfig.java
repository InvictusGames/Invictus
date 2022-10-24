package cc.invictusgames.invictus.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 14.02.2020 / 11:57
 * Invictus / cc.invictusgames.invictus.spigot.config
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class MainConfig extends IMainConfig {

    private long slowChatDelay = -1;

    private boolean chatMuted = false;
    private boolean staffModeOnJoin = true;
    private boolean staffVisible = true;
    private boolean antiVPN = true;

    private boolean queueEnabled = false;
    private boolean queuePaused = false;
    private int queueRate = 2;

}
