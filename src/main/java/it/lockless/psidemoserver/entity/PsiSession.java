package it.lockless.psidemoserver.entity;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "psi_session")
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


}
