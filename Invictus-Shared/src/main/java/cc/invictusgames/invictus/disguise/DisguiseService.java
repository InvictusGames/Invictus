package cc.invictusgames.invictus.disguise;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.11.2020 / 14:30
 * Invictus / cc.invictusgames.invictus.spigot.disguise
 */

public class DisguiseService {

    public static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(DisguiseService.class);

    private final Invictus invictus;

    public DisguiseService(Invictus invictus) {
        this.invictus = invictus;
    }

    public DisguiseData getDisguiseData(UUID uuid) {
        RequestResponse response = RequestHandler.get("disguise/%s", uuid.toString());
        if (response.wasSuccessful())
            return new DisguiseData(invictus, response.asObject());

        if (response.getCode() == 404) {
            return new DisguiseData(invictus, uuid);
        }

        LOG.warning(String.format(
                "Could not load disguise data of %s: %s (%d)",
                uuid.toString(),
                response.getErrorMessage(),
                response.getCode()
        ));
        return null;
    }

}
