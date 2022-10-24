package cc.invictusgames.invictus.bossbar;

import cc.invictusgames.ilib.ILib;
import cc.invictusgames.ilib.bossbar.BossBarService;
import cc.invictusgames.ilib.stringanimation.StringAnimation;
import cc.invictusgames.ilib.stringanimation.impl.BlinkAnimation;
import cc.invictusgames.ilib.stringanimation.impl.FadeAnimation;
import cc.invictusgames.ilib.stringanimation.impl.StaticAnimation;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.bossbar.config.BossBarConfig;
import cc.invictusgames.invictus.bossbar.config.BossBarEntry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.function.Predicate;

public class BossBarProvider {

    @Setter
    @Getter
    private static Predicate<Player> shouldShow = player -> true;

    public BossBarProvider(InvictusBukkit invictus) {
        BossBarConfig config = invictus.getConfigurationService().loadConfiguration(
                BossBarConfig.class,
                new File(ILib.getInstance().getMainConfig().getMessagesPath(), "bossbar.json")
        );

        StringAnimation animation = new StringAnimation();

        for (BossBarEntry entry : config.getAnimations()) {
            animation.add(new StaticAnimation(
                    entry.getFadeFrom() + entry.getText(),
                    10
            ));

            animation.add(new FadeAnimation(
                    entry.getText(),
                    entry.getFadeFrom(),
                    entry.getFadeTo(),
                    false
            ));

            animation.add(new BlinkAnimation(
                    entry.getText(),
                    entry.getFadeFrom(),
                    entry.getFadeTo(),
                    3,
                    2
            ));

            animation.add(new StaticAnimation(
                    entry.getFadeTo() + entry.getText(),
                    10
            ));

            animation.add(new FadeAnimation(
                    entry.getText(),
                    entry.getFadeFrom(),
                    entry.getFadeTo(),
                    true
            ));

            animation.add(new BlinkAnimation(
                    entry.getText(),
                    entry.getFadeTo(),
                    entry.getFadeFrom(),
                    3,
                    2
            ));
        }

        animation.whenTicked(s -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!shouldShow.test(player))
                    continue;

                BossBarService.setBossBar(player, s, 1F);
            }
        });
        animation.start(4L);
    }

}
