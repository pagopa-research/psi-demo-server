package it.lockless.psidemoserver.model;

import psi.model.PsiAlgorithmParameter;

public class PsiAlgorithmParameterDTO {

    private PsiAlgorithmParameter content;

    public PsiAlgorithmParameterDTO() {}

    public PsiAlgorithmParameterDTO(PsiAlgorithmParameter content) {
        this.content = content;
    }

    public PsiAlgorithmParameter getContent() {
        return content;
    }

    public void setContent(PsiAlgorithmParameter content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PsiAlgorithmParameterDTO{" +
                "content=" + content +
                '}';
    }
}
