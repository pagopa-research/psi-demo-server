package it.lockless.psidemoserver.model;

import java.util.Map;

public class PsiDatasetMapDTO {

    private Map<Long, String> content;

    public PsiDatasetMapDTO() {

    }

    public PsiDatasetMapDTO(Map<Long, String> content) {
        this.content = content;
    }

    public Map<Long, String> getContent() {
        return content;
    }

    public void setContent(Map<Long, String> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "DatasetMapDTO{" +
                "content=" + content +
                '}';
    }
}
