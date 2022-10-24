package cc.invictusgames.invictus.punishment;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.02.2020 / 14:59
 * Invictus / cc.invictusgames.invictus.spigot.punishment
 */

@RequiredArgsConstructor
public class PunishmentService {

    private static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(PunishmentService.class);
    private final Invictus invictus;

    public List<Punishment> getPunishments(UUID uuid) {
        RequestResponse response = RequestHandler.get("punishment/profile/%s", uuid.toString());
        if (!response.wasSuccessful()) {
            LOG.warning(String.format("Could not load punishments of %s: %s (%d)",
                    uuid.toString(), response.getErrorMessage(), response.getCode()));
            return new ArrayList<>();
        }

        List<Punishment> punishments = new ArrayList<>();
        response.asArray().forEach(element -> punishments.add(new Punishment(invictus, element.getAsJsonObject())));
        return punishments;
    }
}
