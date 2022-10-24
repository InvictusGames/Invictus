package cc.invictusgames.invictus.tag.command.parameter;

import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.tag.Tag;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TagParameter implements ParameterType<Tag> {

    private static final Invictus invictus = Invictus.getInstance();

    @Override
    public Tag parse(CommandSender sender, String source) {
        Tag tag = invictus.getTagService().getTag(source);

        if (tag == null) {
            sender.sendMessage(CC.RED + "Tag " + CC.YELLOW + source + CC.RED + " not found.");
            return null;
        }

        return tag;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> flags) {
        List<String> completions = new ArrayList<>();
        for (Tag tag : invictus.getTagService().getTagList()) {
            completions.add(tag.getName());
        }
        return completions;
    }
}
