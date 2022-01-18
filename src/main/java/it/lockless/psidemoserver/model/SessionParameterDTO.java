package it.lockless.psidemoserver.model;

import it.lockless.psidemoserver.model.enumeration.AlgorithmDTO;

import javax.validation.constraints.NotNull;

public class SessionParameterDTO {

    @NotNull
    private AlgorithmDTO algorithm;

    @NotNull
    private Integer keySize;

    public SessionParameterDTO() {}

    public SessionParameterDTO(@NotNull AlgorithmDTO algorithm, @NotNull Integer keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    public AlgorithmDTO getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmDTO algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }
}
