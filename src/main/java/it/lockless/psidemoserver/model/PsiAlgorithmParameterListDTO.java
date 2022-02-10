package it.lockless.psidemoserver.model;

import psi.model.PsiAlgorithmParameter;

import java.util.List;
import java.util.Objects;

/**
 * DTO encapsulating the list of supported PsiAlgorithmParameterDTO.
 */

public class PsiAlgorithmParameterListDTO {

    private List<PsiAlgorithmParameter> content;

    public PsiAlgorithmParameterListDTO(List<PsiAlgorithmParameter> content) {
        this.content = content;
    }

    public List<PsiAlgorithmParameter> getContent() {
        return content;
    }

    public void setContent(List<PsiAlgorithmParameter> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiAlgorithmParameterListDTO that = (PsiAlgorithmParameterListDTO) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return "PsiAlgorithmParameterListDTO{" +
                "content=" + content +
                '}';
    }
}
