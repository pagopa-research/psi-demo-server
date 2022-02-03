package it.lockless.psidemoserver.model;

import psi.model.PsiClientSession;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;

public class PsiClientSessionDTO {

    @NotNull
    private Long sessionId;

    @NotNull
    private Instant expiration;

    @NotNull
    private PsiClientSession psiClientSession;

    private BloomFilterDTO bloomFilterDTO;

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

    public BloomFilterDTO getBloomFilterDTO() {
        return bloomFilterDTO;
    }

    public void setBloomFilterDTO(BloomFilterDTO bloomFilterDTO) {
        this.bloomFilterDTO = bloomFilterDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiClientSessionDTO that = (PsiClientSessionDTO) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(expiration, that.expiration) && Objects.equals(psiClientSession, that.psiClientSession) && Objects.equals(bloomFilterDTO, that.bloomFilterDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, expiration, psiClientSession, bloomFilterDTO);
    }

    @Override
    public String toString() {
        return "PsiClientSessionDTO{" +
                "sessionId=" + sessionId +
                ", expiration=" + expiration +
                ", psiClientSession=" + psiClientSession +
                ", bloomFilterDTO=" + bloomFilterDTO +
                '}';
    }
}
