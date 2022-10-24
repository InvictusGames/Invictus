package cc.invictusgames.invictus.prime.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.prime.menu.PrimeMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PrimeCommand {

    private final InvictusBukkit invictus;

    @Command(names = "prime",
             playerOnly = true)
    public boolean primeCommand(Player sender) {
        new PrimeMenu(invictus).openMenu(sender);
        return true;
    }
}
