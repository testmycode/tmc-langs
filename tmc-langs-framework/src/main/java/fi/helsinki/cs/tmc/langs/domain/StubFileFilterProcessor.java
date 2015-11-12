package fi.helsinki.cs.tmc.langs.domain;

import java.util.List;

final class StubFileFilterProcessor extends Filer {

    @Override
    public List<String> prepareFile(List<String> data) {
        for (CommentStyleFileFilter filter : getCommentStyleFileFilters()) {
            data = filter.filterForStub(data);
        }

        return data;
    }
}
