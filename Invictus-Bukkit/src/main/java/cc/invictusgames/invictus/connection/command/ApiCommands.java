package cc.invictusgames.invictus.connection.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author langgezockt (langgezockt@gmail.com)
 * 03.03.2021 / 05:24
 * Invictus / cc.invictusgames.invictus.spigot.connection.command
 */

@RequiredArgsConstructor
public class ApiCommands {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final Invictus invictus;

    @Command(names = {"api test"},
             permission = "op",
             description = "Test if the api is working",
             async = true)
    public boolean test(CommandSender sender) {
        sender.sendMessage(CC.YELLOW + "Sending test request...");
        Timings timings = new Timings("api-test").startTimings();
        RequestResponse response = RequestHandler.get("istheapiworking");
        timings.stopTimings();

        if (!(sender instanceof Player))
            sender.sendMessage(CC.format("&eRequest to &c%s &egave following result:",
                    response.getRequestBuilder().build().url().toString()));
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.format(" &eCode: &c%d", response.getCode()));
        sender.sendMessage(CC.format(" &eSuccessful: %s",
                CC.colorBoolean(response.wasSuccessful(), "true", "false")));
        sender.sendMessage(CC.format(" &eCould Not Connect: %s",
                CC.colorBoolean(response.couldNotConnect(), "true", "false")));
        sender.sendMessage(CC.format(" &eBody: &c%s",
                response.getResponse() == null ? null : response.getResponse().toString()));
        sender.sendMessage(CC.format(" &eError: &c%s", response.getErrorMessage()));
        sender.sendMessage(CC.format(" &eLatency: &c%dms", timings.calculateDifference()));
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"api cache"},
             permission = "op",
             description = "View cache stats of the api",
             async = true)
    public boolean cache(CommandSender sender) {
        RequestResponse response = RequestHandler.get("stats/cache");
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&c%s (%d)", response.getErrorMessage(), response.getCode()));
            return false;
        }

        JsonObject object = response.asObject();
        AtomicBoolean first = new AtomicBoolean(true);

        sender.sendMessage(CC.SMALL_CHAT_BAR);
        object.entrySet().forEach(parent -> {
            if (!first.get())
                sender.sendMessage(" ");
            else first.set(false);

            sender.sendMessage(CC.RED + CC.BOLD + parent.getKey());
            parent.getValue().getAsJsonObject().entrySet().forEach(entry ->
                    sender.sendMessage(CC.format(" &e%s: &c%s", entry.getKey(), entry.getValue().getAsString())));
        });

        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"api info"},
             permission = "op",
             description = "View local api info",
             async = true)
    public boolean info(CommandSender sender) {
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.RED + CC.BOLD + "Api Info");
        sender.sendMessage(CC.format(" &eStatus: %s",
                CC.colorBoolean(!RequestHandler.isApiDown(), "Online", "Offline")));

        sender.sendMessage(CC.format(" &eLast Request: &c%s", RequestHandler.getLastRequest() == -1
                ? "Never"
                : TimeUtils.formatTimeShort(System.currentTimeMillis() - RequestHandler.getLastRequest())));

        sender.sendMessage(CC.format(" &eLast Error: &c%s", RequestHandler.getLastError() == -1
                ? "Never"
                : TimeUtils.formatTimeShort(System.currentTimeMillis() - RequestHandler.getLastError()) + " ago"));

        sender.sendMessage(CC.format(" &eTotal Requests: &c%d", RequestHandler.getTotalRequests()));
        sender.sendMessage(CC.format(" &eRequests Successful: &c%d (%.2f%%)",
                RequestHandler.getAverageLatencyTicks(),
                (float) ((RequestHandler.getAverageLatencyTicks() * 100) / RequestHandler.getTotalRequests())));

        sender.sendMessage(CC.format(" &eLast Latency: &c%dms", RequestHandler.getLastLatency()));
        sender.sendMessage(CC.format(" &eAverage Latency: &c%sms",
                decimalFormat.format(RequestHandler.getAverageLatency())));
        sender.sendMessage(CC.format(" &eRequests in BackLog: &c%d", RequestHandler.getBackLogSize()));
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"api sethost"},
             permission = "owner",
             description = "Set the host of the api",
             async = true)
    public boolean setHost(CommandSender sender, @Param(name = "host") String host) {
        invictus.getMainConfig().setBackendHost(host);
        invictus.saveMainConfig();
        sender.sendMessage(CC.format("&aSet api host to &e%s&a.", host));

        test(sender);
        return true;
    }

    @Command(names = {"api setkey"},
             permission = "owner",
             description = "Set the secret key of the api",
             async = true)
    public boolean setKey(CommandSender sender, @Param(name = "key") String key) {
        invictus.getMainConfig().setBackendKey(key);
        invictus.saveMainConfig();
        sender.sendMessage(CC.format("&aSet api secret key to &e%s&a.", key));

        test(sender);
        return true;
    }

}
