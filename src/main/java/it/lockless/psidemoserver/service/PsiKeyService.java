package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.config.PsiKey;
import it.lockless.psidemoserver.config.StoredAlgorithmKey;
import it.lockless.psidemoserver.mapper.AlgorithmMapper;
import it.lockless.psidemoserver.util.exception.CustomRuntimeException;
import it.lockless.psidemoserver.util.exception.KeyNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import psi.model.PsiAlgorithmParameter;
import psi.server.PsiServerKeyDescription;
import psi.server.PsiServerKeyDescriptionFactory;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Functionalities related to dataset encryptions
 * */

@Service
public class PsiKeyService {

    private static final Logger log = LoggerFactory.getLogger(PsiKeyService.class);

    private final StoredAlgorithmKey storedAlgorithmKey;

    public PsiKeyService(StoredAlgorithmKey storedAlgorithmKey) {
        this.storedAlgorithmKey = storedAlgorithmKey;
    }

    // Build PsiServerKeyDescription from a PsiKey
    PsiServerKeyDescription buildPsiServerKeyDescription(PsiKey psiKey){
        log.trace("Calling buildPsiServerKeyDescription with psiKey = {}", psiKey);
        switch (psiKey.getAlgorithm()) {
            case BS:
                return PsiServerKeyDescriptionFactory.createBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getModulus());
            case DH:
                return PsiServerKeyDescriptionFactory.createDhServerKeyDescription(psiKey.getPrivateKey(), psiKey.getModulus(), psiKey.getGenerator());
            case ECBS:
                return PsiServerKeyDescriptionFactory.createEcBsServerKeyDescription(psiKey.getPrivateKey(), psiKey.getPublicKey(), psiKey.getEcSpecName());
            case ECDH:
                return PsiServerKeyDescriptionFactory.createEcDhServerKeyDescription(psiKey.getPrivateKey(), psiKey.getEcSpecName());
            default:
                throw new CustomRuntimeException("The algorithm "+psiKey.getAlgorithm()+" is not supported");
        }
    }

    // Build and store a PsiKey from a PsiServerKeyDescription
    Long storePsiServerKeyDescription(PsiAlgorithmParameter psiAlgorithmParameter, PsiServerKeyDescription psiServerKeyDescription){
        log.debug("Calling storePsiServerKeyDescription with psiAlgorithmParameter = {}, psiServerKeyDescription = {}", psiAlgorithmParameter, psiServerKeyDescription);
        PsiKey psiKey = new PsiKey();
        psiKey.setAlgorithm(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()));
        psiKey.setKeySize(psiAlgorithmParameter.getKeySize());
        psiKey.setKeyId((new SecureRandom()).nextLong());
        switch (psiAlgorithmParameter.getAlgorithm()){
            case BS:
                psiKey.setPublicKey(psiServerKeyDescription.getPublicKey());
            case DH:
                psiKey.setModulus(psiServerKeyDescription.getModulus());
                psiKey.setPrivateKey(psiServerKeyDescription.getPrivateKey());
                psiKey.setGenerator(psiServerKeyDescription.getGenerator());
                break;
            case ECBS:
                psiKey.setPublicKey(psiServerKeyDescription.getEcPublicKey());
            case ECDH:
                psiKey.setEcSpecName(psiServerKeyDescription.getEcSpecName());
                psiKey.setPrivateKey(psiServerKeyDescription.getEcPrivateKey());
                break;
            default:
                throw new CustomRuntimeException("The algorithm "+psiAlgorithmParameter.getAlgorithm()+" is not supported");
        }
        storedAlgorithmKey.storeKey(psiKey);
        return psiKey.getKeyId();
    }

    // Retrieve the PsiServerKeyDescription corresponding to the keyId
    PsiServerKeyDescription findAndBuildByKeyId(Long keyId){
        log.trace("Calling findByKeyId with keyId = {}", keyId);
        return storedAlgorithmKey.findByKeyId(keyId).map(this::buildPsiServerKeyDescription)
                .orElseThrow(KeyNotAvailableException::new);
    }

    // If available, retrieve the PsiServerKeyDescription corresponding to the psiAlgorithm and keySize
    Optional<PsiKey> findByPsiAlgorithmParameter(PsiAlgorithmParameter psiAlgorithmParameter){
        log.trace("Calling findByPsiAlgorithmAndKeySize with psiAlgorithmParameter = {}", psiAlgorithmParameter);
        return storedAlgorithmKey.findByAlgorithmAndKeySize(AlgorithmMapper.toEntity(psiAlgorithmParameter.getAlgorithm()), psiAlgorithmParameter.getKeySize());
    }
}
