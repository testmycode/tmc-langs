package fi.helsinki.cs.tmc.langs.domain;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaSyntaxGenerator {

    private static final Map<String, List<MetaSyntax>> cache = Maps.newHashMap();

    public static List<MetaSyntax> listSyntaxes(String fileType) {
        if (cache.containsKey(fileType)) {
            return cache.get(fileType);
        }
        List<MetaSyntax> list = new ArrayList<>();
        if (fileType.matches("java|c|cpp|h|hpp|js|css|rs|qml|cs")) {
            list.add(new MetaSyntax("\\/\\/", "")); //
            list.add(new MetaSyntax("\\/\\*+", "\\*+\\/")); /* */
        } else if (fileType.matches("xml|http|html|qrc")) {
            list.add(new MetaSyntax("<!--", "-->"));
        } else if (fileType.matches("properties|py|R|pro")) {
            list.add(new MetaSyntax("#", ""));
        }
        cache.put(fileType, list);
        return list;
    }
}
