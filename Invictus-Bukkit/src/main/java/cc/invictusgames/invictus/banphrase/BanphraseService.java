package cc.invictusgames.invictus.banphrase;

import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.callback.Callable;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 19:30
 * Invictus / cc.invictusgames.invictus.banphrase
 */

@RequiredArgsConstructor
public class BanphraseService {

    public static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(BanphraseService.class);

    @Getter
    private final List<Banphrase> banphrases = new ArrayList<>();

    public void loadBanphrases(Callable callable) {
        Tasks.runAsync(() -> {
            LOG.config("Loading banphrases...");
            Timings timings = new Timings("banphrase-loading").startTimings();
            banphrases.clear();

            RequestResponse response = RequestHandler.get("banphrases");
            if (!response.wasSuccessful()) {
                LOG.warning(String.format("Could not load banphrases: %s (%d)",
                        response.getErrorMessage(), response.getCode()));
                return;
            }

            response.asArray().forEach(banphrase -> banphrases.add(new Banphrase(banphrase.getAsJsonObject())));
            LOG.info(String.format("Loaded %d banphrases in %dms",
                    banphrases.size(), timings.stopTimings().calculateDifference()));
            callable.callback();
        });
    }

    public Banphrase checkBanned(String message) {
        Banphrase max = null;
        for (Banphrase banphrase : banphrases) {
            if (banphrase.isEnabled() && banphrase.matches(message)) {
                if (max == null) {
                    max = banphrase;
                    continue;
                }

                if (banphrase.compareTo(max) > 0)
                    max = banphrase;
            }
        }

        return max;
    }


}
