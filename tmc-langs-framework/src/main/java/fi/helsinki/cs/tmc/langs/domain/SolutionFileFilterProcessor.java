package fi.helsinki.cs.tmc.langs.domain;

import java.util.List;

final class SolutionFileFilterProcessor extends Filer {

    @Override
    public List<String> prepareFile(List<String> data) {

        for (CommentStyleFileFilter filter : getCommentStyleFileFilters()) {
            data = filter.filterForSolution(data);
        }
        return data;
    }
}
