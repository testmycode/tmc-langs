package fi.helsinki.cs.tmc.langs.domain;

import java.util.List;

final class SolutionFileFilterProcessor extends Filer {

    @Override
    List<String> prepareFile(List<String> data) {

        for (CommentStyleFileFilter filter : getCommentStyleFileFilters()) {
            data = filter.filterForSolution(data);
        }

        return data;
    }
}
