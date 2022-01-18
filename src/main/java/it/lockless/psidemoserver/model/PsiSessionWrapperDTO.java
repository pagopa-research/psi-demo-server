package it.lockless.psidemoserver.model;

import psi.dto.PsiSessionDTO;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class PsiSessionWrapperDTO {

    private Long sessionId;

    @NotNull
    private Instant expiration;

    private PsiSessionDTO psiSessionDTO;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public PsiSessionDTO getPsiSessionDTO() {
        return psiSessionDTO;
    }

    public void setPsiSessionDTO(PsiSessionDTO psiSessionDTO) {
        this.psiSessionDTO = psiSessionDTO;
    }
}
