package it.lockless.psidemoserver.model;

import java.util.Objects;
import java.util.Set;

/**
 * DTO used to transfer a page of the server dataset.
 */

public class PsiServerDatasetPageDTO {

    private Integer page;

    private Integer size;

    private Integer entries;

    private Boolean last;

    private Integer totalPages;

    private Long totalEntries;

    private Set<String> content;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getEntries() {
        return entries;
    }

    public void setEntries(Integer entries) {
        this.entries = entries;
    }

    public Boolean isLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(Long totalEntries) {
        this.totalEntries = totalEntries;
    }

    public Set<String> getContent() {
        return content;
    }

    public void setContent(Set<String> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiServerDatasetPageDTO that = (PsiServerDatasetPageDTO) o;
        return Objects.equals(page, that.page) &&
                Objects.equals(size, that.size) &&
                Objects.equals(entries, that.entries) &&
                Objects.equals(last, that.last) &&
                Objects.equals(totalPages, that.totalPages) &&
                Objects.equals(totalEntries, that.totalEntries) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, entries, last, totalPages, totalEntries, content);
    }

    @Override
    public String toString() {
        return "ServerDatasetPageDTO{" +
                "page=" + page +
                ", size=" + size +
                ", entries=" + entries +
                ", last=" + last +
                ", totalPages=" + totalPages +
                ", totalEntries=" + totalEntries +
                ", content=" + content +
                '}';
    }
}
