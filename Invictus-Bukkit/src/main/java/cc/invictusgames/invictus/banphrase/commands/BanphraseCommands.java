package cc.invictusgames.invictus.banphrase.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.banphrase.menu.BanphraseMainMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 20:29
 * Invictus / cc.invictusgames.invictus.banphrase
 */

@RequiredArgsConstructor
public class BanphraseCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"banphrases"},
             permission = "invictus.command.banphrases",
             description = "Manage banphrases",
             playerOnly = true)
    public boolean banphrases(Player sender) {
        new BanphraseMainMenu(invictus).openMenu(sender);
        return true;
    }

}
