package it.lockless.psidemoserver.model;

import psi.model.PsiClientSession;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

public class PsiSessionWrapperDTO {

    private Long sessionId;

    @NotNull
    private Instant expiration;

    private PsiClientSession psiClientSession;

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

    public PsiClientSession getPsiClientSession() {
        return psiClientSession;
    }

    public void setPsiClientSession(PsiClientSession psiClientSession) {
        this.psiClientSession = psiClientSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiSessionWrapperDTO that = (PsiSessionWrapperDTO) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(expiration, that.expiration) &&
                Objects.equals(psiClientSession, that.psiClientSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, expiration, psiClientSession);
    }

    @Override
    public String toString() {
        return "PsiSessionWrapperDTO{" +
                "sessionId=" + sessionId +
                ", expiration=" + expiration +
                ", psiSessionDTO=" + psiClientSession +
                '}';
    }
}
