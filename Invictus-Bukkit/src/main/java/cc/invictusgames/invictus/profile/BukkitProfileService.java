package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.GrantBackLogEntry;
import cc.invictusgames.invictus.note.Note;
import cc.invictusgames.invictus.note.NoteBackLogEntry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * @author langgezockt (langgezockt@gmail.com)
 * 03.03.2021 / 04:11
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@RequiredArgsConstructor
public class BukkitProfileService {

    private final InvictusBukkit invictus;

    public RequestResponse addGrant(Profile target, Grant grant) {
        JsonBuilder builder = new JsonBuilder();
        builder.add("id", grant.getId());
        builder.add("rank", grant.getRank().getUuid());
        builder.add("grantedBy", grant.getGrantedBy());
        builder.add("grantedAt", grant.getGrantedAt());
        builder.add("grantedReason", grant.getGrantedReason());
        builder.add("scopes", StringUtils.join(grant.getScopes(), ","));
        builder.add("duration", grant.getDuration());
        builder.add("end", grant.getEnd());

        RequestResponse response = RequestHandler.post("profile/%s/grants", builder.build(),
                target.getUuid().toString());
        if (response.couldNotConnect())
            RequestHandler.addToBackLog(new GrantBackLogEntry(grant, target.getUuid(),
                    response.getRequestBuilder()));
        return response;
    }

    public RequestResponse removeGrant(Profile target, Grant grant) {
        JsonBuilder builder = new JsonBuilder();
        builder.add("removedBy", grant.getRemovedBy());
        builder.add("removedAt", grant.getRemovedAt());
        builder.add("removedReason", grant.getRemovedReason());
        builder.add("removed", grant.isRemoved());

        RequestResponse response = RequestHandler.put("profile/%s/grants/%s", builder.build(),
                target.getUuid().toString(), grant.getId().toString());
        if (response.couldNotConnect())
            RequestHandler.addToBackLog(new GrantBackLogEntry(grant, target.getUuid(),
                    response.getRequestBuilder()));
        else if (!response.wasSuccessful()) {
            grant.setRemovedBy("N/A");
            grant.setRemovedAt(-1);
            grant.setRemovedReason("N/A");
            grant.setRemoved(false);
        }
        return response;
    }

    public RequestResponse addNote(Profile target, Note note) {
        RequestResponse response = RequestHandler.post("profile/%s/notes", note.toJson(), target.getUuid());
        if (response.couldNotConnect())
            RequestHandler.addToBackLog(new NoteBackLogEntry(note, target.getUuid(), response.getRequestBuilder()));
        return response;
    }

    public RequestResponse removeNote(Profile target, Note note) {
        return RequestHandler.delete("profile/%s/notes/%s", target.getUuid().toString(), note.getId().toString());
    }

}
