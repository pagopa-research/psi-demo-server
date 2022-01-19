package it.lockless.psidemoserver.model;

import java.util.Map;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiDatasetMapDTO that = (PsiDatasetMapDTO) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return "DatasetMapDTO{" +
                "content=" + content +
                '}';
    }
}
