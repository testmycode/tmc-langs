package fi.helsinki.cs.tmc.langs.domain;

import java.util.ArrayList;
import java.util.List;

public class MetaSyntaxGenerator {
    
    public static List<MetaSyntax> listSyntaxes(String fileType) {
        List<MetaSyntax> list = new ArrayList<>();
        if (fileType.matches("java|c|cpp|h|hpp|js")) {
            list.add(new MetaSyntax("\\/\\/", ""));          //
            list.add(new MetaSyntax("\\/\\*+", "\\*+\\/"));  /* */
        }
        if (fileType.matches("xml|http|html")) {
            list.add(new MetaSyntax("<!--", "-->"));
        }
        return list;
    }
}
