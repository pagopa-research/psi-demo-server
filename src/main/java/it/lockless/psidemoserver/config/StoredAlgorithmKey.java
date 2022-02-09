package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import psi.CustomTypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Important note: This service represents the abstraction of a keyStore
 * and is not intended to be used in a business environment.
 * Replace with a proper keyStore service.
 */

@Component
public class StoredAlgorithmKey {

    private static final Logger log = LoggerFactory.getLogger(StoredAlgorithmKey.class);

    private static final String KEY_STORE_FILENAME = "key.store";

    private ConcurrentHashMap<Long, PsiKey> keyIdMap;

    StoredAlgorithmKey() {
        log.info("Calling StoredAlgorithmKey");

        Set<PsiKey> psiKeySet = new HashSet<>();

        File keyStoreFile = new File(KEY_STORE_FILENAME);
        if(keyStoreFile.exists()) {
            try {
                try (Stream<String> stream = Files.lines(keyStoreFile.toPath())) {
                    stream.forEach(x -> psiKeySet.add(CustomTypeConverter.convertStringToObject(x, PsiKey.class)));
                }
            } catch (IOException e) {
                log.error("Unable to open file {}", KEY_STORE_FILENAME);
            }
        }
        log.info("Found {} items into the keyStore", psiKeySet.size());

        this.keyIdMap = new ConcurrentHashMap<>();
        psiKeySet.forEach(key ->
                keyIdMap.put(key.getKeyId(), key));
    }

    public synchronized void storeKey(PsiKey psiKey){
        keyIdMap.put(psiKey.getKeyId(), psiKey);

        File keyStoreFile = new File(KEY_STORE_FILENAME);
        try (FileWriter fileWriter = new FileWriter(keyStoreFile, true)){
            fileWriter.write(CustomTypeConverter.convertObjectToString(psiKey) + "\n");
        } catch (IOException e) {
            log.error("Unable to open file {}", KEY_STORE_FILENAME);
        }
    }

    public Optional<PsiKey> findByAlgorithmAndKeySize(Algorithm algorithm, int keySize) {
        PsiKey psiKey = keyIdMap.values().stream()
                .filter(k -> (k.getAlgorithm().equals(algorithm) && k.getKeySize().equals(keySize)))
                .findAny().orElse(null);
        if (psiKey == null)
            return Optional.empty();
        else
            return Optional.of(psiKey);
    }

    public Optional<PsiKey> findByKeyId(Long keyId) {
        PsiKey psiKey = keyIdMap.get(keyId);
        if (psiKey != null)
            return Optional.of(psiKey);
        return Optional.empty();
    }
}
