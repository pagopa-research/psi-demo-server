package it.lockless.psidemoserver.mapper;

import it.lockless.psidemoserver.model.enumeration.AlgorithmDTO;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;

public class AlgorithmMapper {

    public static AlgorithmDTO toDTO(Algorithm algorithm){
        switch (algorithm) {
            case DH:
                return AlgorithmDTO.DH;
            case RSA:
                return AlgorithmDTO.RSA;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }

    public static Algorithm toEntity(AlgorithmDTO algorithmDTO){
        switch (algorithmDTO) {
            case DH:
                return Algorithm.DH;
            case RSA:
                return Algorithm.RSA;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }
}
