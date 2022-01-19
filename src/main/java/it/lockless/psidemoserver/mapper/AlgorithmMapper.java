package it.lockless.psidemoserver.mapper;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psi.dto.PsiAlgorithmDTO;

public class AlgorithmMapper {

    private static final Logger log = LoggerFactory.getLogger(AlgorithmMapper.class);

    public static PsiAlgorithmDTO toDTO(Algorithm algorithm){
        log.trace("Calling toDTO with algorithm = {}", algorithm);
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
        log.trace("Calling toEntity with algorithmDTO = {}", algorithmDTO);
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
