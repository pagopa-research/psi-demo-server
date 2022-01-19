package it.lockless.psidemoserver.entity;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import javax.persistence.*;
import java.io.Serializable;

//TODO: questa classa dovrebbe essere spostata su uno storage sicuro. Per questo motivo non creiamo una relazione con la session

@Entity
@Table(name = "psi_key")
public class PsiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "psi_key_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="psi_key_id_seq",sequenceName="psi_key_id_seq", allocationSize = 1)
    @Column(name = "id")
    private long id;

    @Column(name = "algorithm", nullable = false)
    private Algorithm algorithm;

    @Column(name = "key_size", nullable = false)
    private Integer keySize;

    @Column(name = "key_id", nullable = false)
    private Long keyId;

    @Column(name = "modulus", columnDefinition="TEXT")
    private String modulus;

    @Column(name = "public_key", columnDefinition="TEXT")
    private String publicKey;

    @Column(name = "private_key", columnDefinition="TEXT")
    private String privateKey;


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

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getModulus() {
        return modulus;
    }

    public void setModulus(String modulus) {
        this.modulus = modulus;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

}
