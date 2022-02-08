package it.lockless.psidemoserver.mapper;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psi.model.PsiAlgorithm;

/*
This mapper is used to translate the PsiAlgorithm as represented by the sdk, with the Algorithm as stored into the DB
 */

public class AlgorithmMapper {

    private AlgorithmMapper() {}

    private static final Logger log = LoggerFactory.getLogger(AlgorithmMapper.class);

    public static PsiAlgorithm toPsiAlgorithm(Algorithm algorithm){
        log.trace("Calling toDTO with algorithm = {}", algorithm);
        switch (algorithm) {
            case DH:
                return PsiAlgorithm.DH;
            case BS:
                return PsiAlgorithm.BS;
            case ECDH:
                return PsiAlgorithm.ECDH;
            case ECBS:
                return PsiAlgorithm.ECBS;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }

    public static Algorithm toEntity(PsiAlgorithm psiAlgorithm){
        log.trace("Calling toEntity with algorithmDTO = {}", psiAlgorithm);
        switch (psiAlgorithm) {
            case DH:
                return Algorithm.DH;
            case BS:
                return Algorithm.BS;
            case ECDH:
                return Algorithm.ECDH;
            case ECBS:
                return Algorithm.ECBS;
            default:
                throw new CustomRuntimeException("Algorithm not supported");
        }
    }
}
