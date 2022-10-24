package cc.invictusgames.invictus.vote.listener;

import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.vote.VoteService;
import cc.invictusgames.invictus.vote.handler.VoteHandler;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

@RequiredArgsConstructor
public class VoteListener implements Listener {

    private final VoteService voteService;

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        VoteHandler handler = voteService.getHandler(vote.getServiceName());
        if (handler == null) {
            VoteService.LOG.warning("Received vote from " + vote.getServiceName() + " but was unable to locate handler.");
            return;
        }

        UUID uuid = UUIDCache.getUuid(vote.getUsername());
        if (uuid == null) {
            VoteService.LOG.warning("Received vote for " + vote.getUsername() + " but was unable to locate uuid.");
            return;
        }

        /*
         * Vote has a timeStamp, but for some reason the people that made votifier thought it would be a good idea
         * to make the time stamp a FORMATTED DATE STRING that varies depending on what site they vote on,
         * so our only option is to set it to the milliseconds when we received the vote.
         */
        handler.setVotedAt(uuid, System.currentTimeMillis());
    }

}
