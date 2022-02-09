package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * It stores the information about a single key as part of the keyStore service
 */

public class PsiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long keyId;

    @NotNull
    private Algorithm algorithm;

    @NotNull
    private Integer keySize;

    private String modulus;

    private String generator;

    @NotNull
    private String privateKey;

    @NotNull
    private String publicKey;

    private String ecSpecName;

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

    public String getEcSpecName() {
        return ecSpecName;
    }

    public void setEcSpecName(String ecSpecName) {
        this.ecSpecName = ecSpecName;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    @Override
    public String toString() {
        return "PsiKey{" +
                "keyId=" + keyId +
                ", algorithm=" + algorithm +
                ", keySize=" + keySize +
                ", modulus='" + modulus + '\'' +
                ", generator='" + generator + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", ecSpecName='" + ecSpecName + '\'' +
                '}';
    }
}
