package cc.invictusgames.invictus.banphrase.procedure;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.packets.BanphraseReloadPacket;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 20:50
 * Invictus / cc.invictusgames.invictus.banphrase.procedure
 */

@RequiredArgsConstructor
@Data
public class BanphraseAddProcedure {

    private final Profile profile;
    private String name;
    private Banphrase.BanphraseOperator operator;
    private String phrase;
    private Banphrase.MuteMode muteMode;
    private long duration;

    public void finish() {
        Tasks.runAsync(() -> {
            JsonBuilder body = new JsonBuilder();
            body.add("id", UUID.randomUUID());
            body.add("name", name);
            body.add("phrase", phrase);
            body.add("operator", operator.name());
            body.add("muteMode", muteMode.name());
            body.add("duration", duration);

            RequestResponse response = RequestHandler.post("banphrase", body.build());
            if (!response.wasSuccessful()) {
                profile.player().sendMessage(CC.format("&cCould not add banphrase: %s (%d)",
                        response.getErrorMessage(), response.getCode()));
                return;
            }

            profile.player().sendMessage(CC.format("&eAdded a &c%s &ebanphrase for &c%s &e(&c%s&e).",
                    operator.getDisplay().toLowerCase(),
                    name,
                    phrase
            ));

            Invictus.getInstance().getRedisService().publish(new BanphraseReloadPacket());
        });
    }

}
