package cc.invictusgames.invictus.tag;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TagService {

    private static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(TagService.class);

    @Getter
    private final List<Tag> tagList = new ArrayList<>();

    public void loadTags() {
        tagList.clear();
        RequestResponse response = RequestHandler.get("tag");
        if (!response.wasSuccessful()) {
            LOG.warning(String.format("Could not load tags: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return;
        }

        response.asArray().forEach(element -> loadTag(element.getAsJsonObject()));
    }

    private void loadTag(JsonObject object) {
        tagList.add(new Tag(object));
    }

    public Tag getTag(String name) {
        for (Tag tag : tagList) {
            if (tag.getName().equalsIgnoreCase(name))
                return tag;
        }

        return null;
    }

    public boolean doesTagExist(String name) {
        return this.getTag(name) != null;
    }

}
