package it.lockless.psidemoserver.mapper;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import psi.dto.PsiAlgorithmDTO;

public class AlgorithmMapper {

    public static PsiAlgorithmDTO toDTO(Algorithm algorithm){
        switch (algorithm) {
            case DH:
                return PsiAlgorithmDTO.DH;
            case BS:
                return PsiAlgorithmDTO.BS;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }

    public static Algorithm toEntity(PsiAlgorithmDTO algorithmDTO){
        switch (algorithmDTO) {
            case DH:
                return Algorithm.DH;
            case BS:
                return Algorithm.BS;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }
}
