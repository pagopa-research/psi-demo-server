package it.lockless.psidemoserver.entity;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * It is intended to keep the information on the single client session during interactions.
 * Since a session is considered expired after a fixed amount of time,
 * this table should be periodically cleaned up by removing old sessions.
 */

@Entity
@Table(name = "psi_session",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id"}, name = "unique_session_id")
        })
public class PsiSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "psi_session_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="psi_session_id_seq",sequenceName="psi_session_id_seq", allocationSize = 1)
    @Column(name = "id")
    private long id;

    @Column(name = "algorithm", nullable = false)
    private Algorithm algorithm;

    @Column(name = "key_size", nullable = false)
    private Integer keySize;

    @Column(name = "cache_enabled", nullable = false)
    private Boolean cacheEnabled;

    @Column(name = "key_id", nullable = false)
    private Long keyId;

    @Column(name = "expiration", nullable = false)
    private Instant expiration;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PsiSession that = (PsiSession) o;
        return id == that.id &&
                algorithm == that.algorithm &&
                Objects.equals(keySize, that.keySize) &&
                Objects.equals(cacheEnabled, that.cacheEnabled) &&
                Objects.equals(keyId, that.keyId) &&
                Objects.equals(expiration, that.expiration) &&
                Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, algorithm, keySize, cacheEnabled, keyId, expiration, sessionId);
    }

    @Override
    public String toString() {
        return "PsiSession{" +
                "id=" + id +
                ", algorithm=" + algorithm +
                ", keySize=" + keySize +
                ", cacheEnabled=" + cacheEnabled +
                ", keyId=" + keyId +
                ", expiration=" + expiration +
                ", sessionId=" + sessionId +
                '}';
    }
}
