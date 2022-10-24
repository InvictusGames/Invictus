package cc.invictusgames.invictus.chatfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.03.2020 / 05:45
 * Invictus / cc.invictusgames.invictus.spigot.chatfilter
 */

public class ChatFilter {

    private final List<Pattern> patterns = new ArrayList<>();

    public void addDefaultFilters() {
        //addFilter("n+ *[i1l]+ *(g+ *|g) *[e3]+ *r+");
        //addFilter("k+ *i+ *l+ *y+ *o *u+ *r+ *s+ *e+ *l+ *f+");
        //addFilter("k+ *y+ *s+");
        //addFilter("f+ *[a4]+ *(g|g +)+ *[o0]+ *t+");
        //addFilter("c+ *u+ *n+ *t+");
        //addFilter("l+ *a+ *g+");
        //addFilter("s+ *c+ *a+ *m+");

        addFilter("(^|\\s)(n+ *([i1l|] *)+ *(g *)+ *([e34a] *)+ *r+)");
        addFilter("(^|\\s)(n+ *([i1l|] *)+ *(g *)+ *([4a] *)+)");
        addFilter("(^|\\s)(k+ *(i *)+ *(l *)+ *(y *)+ *(o *) *(u *)+ *(r *)+ *(s *)+ *(e *)+ *(l *)+ *f+)");
        addFilter("(^|\\s)(k+ *(y *)+ *s+)");
        addFilter("(^|\\s)(n+ *([a4] *)+ *(g *)+ *([o0] *)+ *t+)");
        addFilter("(^|\\s)(c+ *(u *)+ *(n *)+ *t+)");
        addFilter("(^|\\s)(l+ *([a4] *)+ *g+)");
        addFilter("(^|\\s)(s+ *(c *)+ *([a4] *)+ *m+)");
        addFilter("[0-9]{1,3} +[0-9]{1,3} +[0-9]{1,3} +[0-9]{1,3}");
        addFilter("t(elegram)? +me *\\/ *Summered");
    }

    public void addFilter(String pattern) {
        pattern = pattern.replace(" ", "[-+*._ ]");
        patterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
    }

    public boolean isFiltered(String pattern) {
        return patterns.stream().anyMatch(filter -> filter.matcher(pattern).find());
    }

}
