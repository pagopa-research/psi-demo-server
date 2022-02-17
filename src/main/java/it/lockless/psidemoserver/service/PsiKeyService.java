package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.PsiKey;
import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import psi.PsiServerKeyDescription;
import psi.PsiServerKeyDescriptionFactory;
import psi.model.PsiAlgorithmParameter;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Provides functionalities to manage the key descriptions
 */

@Service
public class PsiKeyService {

    private static final Logger log = LoggerFactory.getLogger(PsiKeyService.class);

    private final StoredAlgorithmKey storedAlgorithmKey;

    public PsiKeyService(StoredAlgorithmKey storedAlgorithmKey) {
        this.storedAlgorithmKey = storedAlgorithmKey;
    }

    /**
     * Builds PsiServerKeyDescription from a PsiKey
     * @param psiKey the database object containing the key information
     * @return a PsiServerKeyDescription representing the key
     * */
    PsiServerKeyDescription buildPsiServerKeyDescription(PsiKey psiKey){
        log.trace("Calling buildPsiServerKeyDescription with psiKey = {}", psiKey);
        switch (psiKey.getAlgorithm()) {
            case BS:
                return PsiServerKeyDescriptionFactory.createBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getModulus());
            case DH:
                return PsiServerKeyDescriptionFactory.createDhServerKeyDescription(psiKey.getPrivateKey(), psiKey.getModulus(), psiKey.getGenerator());
            case ECBS:
                return PsiServerKeyDescriptionFactory.createEcBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey());
            case ECDH:
                return PsiServerKeyDescriptionFactory.createEcDhServerKeyDescription(psiKey.getPrivateKey());
            default:
                throw new CustomRuntimeException("The algorithm "+psiKey.getAlgorithm()+" is not supported");
        }
    }

    /**
     * Builds and stores a PsiKey from a PsiServerKeyDescription
     * @param psiAlgorithmParameter     the algorithm parameters associated to the key to be stored
     * @param psiServerKeyDescription   the sdk representation of a server key
     * @return the keyId associated to the new key
     * */
    Long storePsiServerKeyDescription(PsiAlgorithmParameter psiAlgorithmParameter, PsiServerKeyDescription psiServerKeyDescription){
        log.debug("Calling storePsiServerKeyDescription with psiAlgorithmParameter = {}, psiServerKeyDescription = {}", psiAlgorithmParameter, psiServerKeyDescription);
        PsiKey psiKey = new PsiKey();
        psiKey.setAlgorithm(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()));
        psiKey.setKeySize(psiAlgorithmParameter.getKeySize());
        psiKey.setKeyId((new SecureRandom()).nextLong());
        switch (psiAlgorithmParameter.getAlgorithm()){
            case BS:
                psiKey.setPublicKey(psiServerKeyDescription.getPublicExponent());
            case DH:
                psiKey.setModulus(psiServerKeyDescription.getModulus());
                psiKey.setPrivateKey(psiServerKeyDescription.getPrivateExponent());
                psiKey.setGenerator(psiServerKeyDescription.getGenerator());
                break;
            case ECBS:
                psiKey.setPublicKey(psiServerKeyDescription.getEcPublicQ());
            case ECDH:
                psiKey.setPrivateKey(psiServerKeyDescription.getEcPrivateD());
                break;
            default:
                throw new CustomRuntimeException("The algorithm "+psiAlgorithmParameter.getAlgorithm()+" is not supported");
        }
        storedAlgorithmKey.storeKey(psiKey);
        return psiKey.getKeyId();
    }

    /**
     * Retrieves the PsiServerKeyDescription corresponding to the keyId.
     * @param keyId the id of the key to be retrieved.
     * @return a PsiServerKeyDescription of the key associated to the keyId.
     */
    PsiServerKeyDescription findAndBuildByKeyId(Long keyId){
        log.trace("Calling findByKeyId with keyId = {}", keyId);
        return storedAlgorithmKey.findByKeyId(keyId).map(this::buildPsiServerKeyDescription)
                .orElseThrow(KeyNotAvailableException::new);
    }

    /**
     * If available, retrieves the PsiServerKeyDescription corresponding to the psiAlgorithm and keySize.
     * @param psiAlgorithmParameter the algorithm parameters of the key to be retrieved.
     * @return if available, an Optional containing the PsiServerKeyDescription of the retrieved key,
     * an empty Optional otherwise .
     */
    Optional<PsiKey> findByPsiAlgorithmParameter(PsiAlgorithmParameter psiAlgorithmParameter){
        log.trace("Calling findByPsiAlgorithmAndKeySize with psiAlgorithmParameter = {}", psiAlgorithmParameter);
        return storedAlgorithmKey.findByAlgorithmAndKeySize(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()), psiAlgorithmParameter.getKeySize());
    }
}
