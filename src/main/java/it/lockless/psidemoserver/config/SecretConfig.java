package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.util.exception.AlgorithmInvalidKeyException;
import it.lockless.psidemoserver.util.exception.AlgorithmNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import psi.utils.Base64EncoderHelper;
import psi.utils.CustomTypeConverter;

import javax.crypto.spec.DHPrivateKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

// Important: this is only a mock of a real keyStore and it is not intended to be used in a real environment.

@Component
public class SecretConfig {

    private static final Logger log = LoggerFactory.getLogger(SecretConfig.class);

    private static final String KEY_STORE_FILENAME = "key.store";

    @Bean
    public StoredAlgorithmKey createStoredAlgorithmKey() {
        log.info("Calling createStoredAlgorithmKey");
        Set<PsiKey> psiKeySet = loadKeySetFromFile();
        int originalPsiKeySet = psiKeySet.size();
        log.info("Found {} items into the keyStore", originalPsiKeySet);

        for (Algorithm algorithm : Algorithm.values()) {
            for(Integer keySize : algorithm.getSupportedKeySize()){
                PsiKey psiKey = psiKeySet.stream()
                        .filter(k -> (k.getAlgorithm().equals(algorithm) && k.getKeySize().equals(keySize)))
                        .findAny().orElse(null);
                if (psiKey == null)
                    psiKeySet.add(generateKey(algorithm, keySize));
            }
        }

        if(originalPsiKeySet != psiKeySet.size()) {
            log.info("Generate {} new keys", (psiKeySet.size() - originalPsiKeySet));
            writeKeySetFromFile(psiKeySet);
        }
        return new StoredAlgorithmKey(psiKeySet);
    }

    private PsiKey generateKey(Algorithm algorithm, int keySize) {
        log.info("Calling generateKey with algorithm = {}, keySize = {}", algorithm, keySize);
        PsiKey psiKey = new PsiKey();
        // Search the key in the DB

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
                    break;
                default:
                    throw new AlgorithmNotSupportedException("The algorithm "+algorithm+" is not supported");
            }
        } catch (InvalidKeySpecException e) {
            throw new AlgorithmInvalidKeyException("KeySpec is invalid. Verify whether both the input algorithm and key size are correct and compatible.");
        }

        psiKey.setKeyId((long)(Math.random() * (Long.MAX_VALUE)));

        psiKey.setAlgorithm(algorithm);
        psiKey.setKeySize(keySize);

        return psiKey;
    }

    private Set<PsiKey> loadKeySetFromFile(){
        log.info("Calling loadKeySetFromFile");
        Set<PsiKey> psiKeySet = new HashSet<>();
        File keyStoreFile = new File(KEY_STORE_FILENAME);

        if(keyStoreFile.exists()) {
            try {
                try (Stream<String> stream = Files.lines(keyStoreFile.toPath())) {
                    stream.forEach(x -> psiKeySet.add(Base64EncoderHelper.base64ToObject(x, PsiKey.class)));
                }
            } catch (IOException e) {
                log.error("Unable to open file "+KEY_STORE_FILENAME);
            }

        }
        return psiKeySet;
    }

    private boolean writeKeySetFromFile(Set<PsiKey> psiKeySet){
        log.info("Calling writeKeySetFromFile with psiKeySet.size() = {}", psiKeySet.size());
        File keyStoreFile = new File(KEY_STORE_FILENAME);
        try {
            if (!keyStoreFile.exists())
                keyStoreFile.createNewFile();
            FileWriter fileWriter = new FileWriter(keyStoreFile, false);
            for (PsiKey psiKey : psiKeySet)
                fileWriter.write(Base64EncoderHelper.objectToBase64(psiKey)+"\n");
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
