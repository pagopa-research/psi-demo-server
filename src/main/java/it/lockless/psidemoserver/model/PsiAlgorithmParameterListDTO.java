package it.lockless.psidemoserver.model;

import psi.dto.PsiAlgorithmParameterDTO;

import java.util.List;
import java.util.Objects;

public class PsiAlgorithmParameterListDTO {

    List<PsiAlgorithmParameterDTO> content;

    public PsiAlgorithmParameterListDTO(List<PsiAlgorithmParameterDTO> content) {
        this.content = content;
    }

    public List<PsiAlgorithmParameterDTO> getContent() {
        return content;
    }

    public void setContent(List<PsiAlgorithmParameterDTO> content) {
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
