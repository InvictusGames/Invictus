package cc.invictusgames.invictus.vote;

import cc.invictusgames.ilib.command.CommandService;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import cc.invictusgames.invictus.vote.command.VoteCommand;
import cc.invictusgames.invictus.vote.handler.VoteHandler;
import cc.invictusgames.invictus.vote.handler.impl.DiscordSyncHandler;
import cc.invictusgames.invictus.vote.handler.impl.NameMCHandler;
import cc.invictusgames.invictus.vote.handler.impl.VoteWebsiteHandler;
import cc.invictusgames.invictus.vote.listener.VoteListener;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Logger;

public class VoteService {

    public static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(VoteService.class);

    private final Map<String, VoteHandler> handlerMap = new HashMap<>();

    public VoteService(Invictus invictus) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            LOG.info("Votifier not found, canceling initialization.");
            return;
        }

        registerHandler(new VoteWebsiteHandler(invictus, "MinecraftServers.org", "https://bit.ly/3jZ8sP0", 1));
        registerHandler(new VoteWebsiteHandler(invictus, "MCSL", "https://bit.ly/3k35cT0", 2));
        registerHandler(new NameMCHandler());
        registerHandler(new DiscordSyncHandler());

        CommandService.register(InvictusBukkitPlugin.getInstance(), new VoteCommand(this));
        Bukkit.getPluginManager().registerEvents(new VoteListener(this), InvictusBukkitPlugin.getInstance());
    }

    public void registerHandler(VoteHandler voteHandler) {
        handlerMap.put(voteHandler.getServiceName(), voteHandler);
    }

    public VoteHandler getHandler(String serviceName) {
        return handlerMap.get(serviceName);
    }

    public List<VoteHandler> getHandler() {
        List<VoteHandler> list = new ArrayList<>(handlerMap.values());
        list.sort(Comparator.comparingInt(VoteHandler::getPriority));
        return list;
    }

}
