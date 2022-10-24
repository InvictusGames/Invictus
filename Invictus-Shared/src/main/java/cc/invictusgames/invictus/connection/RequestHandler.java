package cc.invictusgames.invictus.connection;

import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonElement;
import lombok.Getter;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.02.2021 / 12:49
 * Invictus / cc.invictusgames.invictus.spigot.connection
 */

public class RequestHandler {

    private static final Invictus invictus = Invictus.getInstance();
    private static final Logger LOG = invictus.getLogFactory().newLogger(RequestHandler.class);

    @Getter
    private static boolean apiDown = false;

    @Getter
    private static long lastError = -1;

    @Getter
    private static long lastRequest = -1;

    @Getter
    private static long lastLatency = 0;
    private static long averageLatency = 0;

    @Getter
    private static long totalRequests = 0;

    @Getter
    private static long averageLatencyTicks = 0;

    public static double getAverageLatency() {
        if (averageLatencyTicks == 0)
            return -1;

        return (averageLatency + 0.0D) / (averageLatencyTicks + 0.0D);
    }

    public static int getBackLogSize() {
        return backLog.size();
    }

    private static final List<BackLogEntry> backLog = new ArrayList<>();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5L))
            .writeTimeout(Duration.ofSeconds(5L))
            .readTimeout(Duration.ofSeconds(5L))
            .build();

    public static void startBackLogTask() {
        Tasks.runTimerAsync(RequestHandler::sendBackLog, 6000L, 6000L);
    }

    public static RequestResponse get(String endpoint, Object... args) {
        Request.Builder builder = newBuilder(endpoint, args);
        builder.get();

        return call(builder, false);
    }

    public static RequestResponse post(String endpoint, JsonElement body, Object... args) {
        Request.Builder builder = newBuilder(endpoint, args);
        //builder.post(RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8")));
        builder.post(RequestBody.create(body.toString(), MediaType.parse("application/json")));

        return call(builder, false);
    }

    public static RequestResponse put(String endpoint, JsonElement body, Object... args) {
        Request.Builder builder = newBuilder(endpoint, args);
        //builder.put(RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8")));
        builder.put(RequestBody.create(body.toString(), MediaType.parse("application/json")));

        return call(builder, false);
    }

    public static RequestResponse delete(String endpoint, Object... args) {
        Request.Builder builder = newBuilder(endpoint, args);
        builder.delete();

        return call(builder, false);
    }

    private static RequestResponse call(Request.Builder builder, boolean fromBackLog) {
        Timings timings = new Timings("api-request").startTimings();
        lastRequest = System.currentTimeMillis();
        totalRequests++;

        boolean newDown = false;
        try {
            Response response = CLIENT.newCall(builder.build()).execute();
            RequestResponse requestResponse = RequestResponse.ofResponse(response, builder);
            newDown = requestResponse.couldNotConnect();

            if (newDown)
                lastError = System.currentTimeMillis();
            else {
                lastLatency = timings.stopTimings().calculateDifference();
                averageLatency += timings.calculateDifference();
                averageLatencyTicks++;
            }
            return requestResponse;
        } catch (IOException e) {
            newDown = true;
            lastError = System.currentTimeMillis();
            e.printStackTrace();
            return RequestResponse.ofError(e, builder);
        } finally {
            if (!newDown && apiDown && !fromBackLog)
                sendBackLog();

            apiDown = newDown;
        }
    }

    private static Request.Builder newBuilder(String endpoint, Object... args) {
        return new Request.Builder()
                .url(invictus.getMainConfig().getBackendHost() + String.format(endpoint, args))
                .addHeader("Authorization", invictus.getMainConfig().getBackendKey());
    }

    public static void addToBackLog(BackLogEntry entry) {
        if (!apiDown)
            throw new UnsupportedOperationException("Cannot add requests to backlog while api is not down");

        backLog.add(entry);
    }

    public static void sendBackLog() {
        if (backLog.isEmpty())
            return;

        LOG.config("Attempting to send request backlog...");

        Iterator<BackLogEntry> iterator = backLog.iterator();
        int sent = 0;
        while (iterator.hasNext()) {
            BackLogEntry next = iterator.next();
            RequestResponse response = call(next.getBuilder(), true);
            if (!response.couldNotConnect()) {
                next.onSend(response);
                iterator.remove();
                sent++;
            }
        }

        if (sent == 0)
            LOG.warning("Could not send request backlog, API is still down");
        else LOG.info(String.format("Sent %d requests from backlog - %d Failed", sent, backLog.size()));
    }

}
