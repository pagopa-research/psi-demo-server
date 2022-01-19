package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.model.enumeration.AlgorithmDTO;
import it.lockless.psidemoserver.repository.PsiKeyRepository;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.AlgorithmInvalidKeyException;
import it.lockless.psidemoserver.util.exception.AlgorithmNotSupportedException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import psi.utils.CustomTypeConverter;

import javax.crypto.spec.DHPrivateKeySpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SecretConfig {

    private final PsiKeyRepository psiKeyRepository;

    public SecretConfig(PsiKeyRepository psiKeyRepository) {
        this.psiKeyRepository = psiKeyRepository;
    }

    @Bean
    //TODO: Transactional?
    public StoredAlgorithmKey loadSecrets() {
        StoredAlgorithmKey supportedAlgorithms = new StoredAlgorithmKey();

        if (supportedAlgorithms.keyMap == null)
            supportedAlgorithms.keyMap = new HashMap<Algorithm, Map<Integer, PsiKey>>(Algorithm.values().length);

        for (Algorithm algorithm : Algorithm.values()) {
            Map<Integer, PsiKey> psiKeyMap = supportedAlgorithms.keyMap.get(algorithm);
            if (psiKeyMap == null) {
                psiKeyMap = new HashMap<>(algorithm.getSupportedKeySize().size());
                supportedAlgorithms.keyMap.put(algorithm, psiKeyMap);
            }
            for(Integer keySize : algorithm.getSupportedKeySize())
                if(!psiKeyMap.containsKey(keySize))
                    psiKeyMap.put(keySize, generateKey(algorithm, keySize));
        }

        return supportedAlgorithms;
    }

    private PsiKey generateKey(Algorithm algorithm, int keySize) {
        PsiKey psiKey = new PsiKey();
        // Search the key in the DB
        Optional<PsiKey> psiKeyOptional = psiKeyRepository.findByAlgorithmAndKeySize(algorithm,keySize);
        if(psiKeyOptional.isPresent())
            return psiKeyOptional.get();

        // If there is not a key stored in the DB, a new one is generated
        KeyPairGenerator keyGenerator;
        KeyFactory keyFactory;
        try {
            String keyType = algorithm.toString();
            if(keyType.equals("BS")) keyType = "RSA";
            keyGenerator = KeyPairGenerator.getInstance(keyType);
            keyFactory = KeyFactory.getInstance(keyType);
        } catch (NoSuchAlgorithmException e) {
            throw new AlgorithmNotSupportedException(algorithm.toString() + " key generator not available");
        }
        keyGenerator.initialize(keySize);
        KeyPair pair = keyGenerator.genKeyPair();

        try {
            switch (algorithm) {
                case BS:
                    RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);
                    psiKey.setModulus(CustomTypeConverter.convertBigIntegerToString(rsaPrivateKeySpec.getModulus()));
                    psiKey.setPrivateKey(CustomTypeConverter.convertBigIntegerToString(rsaPrivateKeySpec.getPrivateExponent()));
                    RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(pair.getPublic(), RSAPublicKeySpec.class);
                    psiKey.setPublicKey(CustomTypeConverter.convertBigIntegerToString(rsaPublicKeySpec.getPublicExponent()));
                    break;
                case DH:
                    DHPrivateKeySpec dhPrivateKeySpec = keyFactory.getKeySpec(pair.getPrivate(), DHPrivateKeySpec.class);
                    psiKey.setModulus(CustomTypeConverter.convertBigIntegerToString(dhPrivateKeySpec.getP()));
                    psiKey.setPrivateKey(CustomTypeConverter.convertBigIntegerToString(dhPrivateKeySpec.getX()));
            }
        } catch (InvalidKeySpecException e) {
            throw new AlgorithmInvalidKeyException("KeySpec is invalid. Verify whether both the input algorithm and key size are correct and compatible.");
        }

        psiKey.setKeyId((long)(Math.random() * (Long.MAX_VALUE)));

        psiKey.setAlgorithm(algorithm);
        psiKey.setKeySize(keySize);
        psiKeyRepository.save(psiKey);

        return psiKey;
    }
}
