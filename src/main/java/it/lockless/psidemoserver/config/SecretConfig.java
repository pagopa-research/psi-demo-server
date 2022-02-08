package it.lockless.psidemoserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import psi.CustomTypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

// Important: this is only a mock of a real keyStore and it is not intended to be used in a real environment.

@Component
public class SecretConfig {

    private static final Logger log = LoggerFactory.getLogger(SecretConfig.class);

    private static final String KEY_STORE_FILENAME = "key.store";

    @Bean
    public StoredAlgorithmKey loadStoredAlgorithmKey() {
        log.info("Calling loadStoredAlgorithmKey");Set<PsiKey> psiKeySet = new HashSet<>();

        File keyStoreFile = new File(KEY_STORE_FILENAME);
        if(keyStoreFile.exists()) {
            try {
                try (Stream<String> stream = Files.lines(keyStoreFile.toPath())) {
                    stream.forEach(x -> psiKeySet.add(CustomTypeConverter.convertStringToObject(x, PsiKey.class)));
                }
            } catch (IOException e) {
                log.error("Unable to open file "+KEY_STORE_FILENAME);
            }
        }
        log.info("Found {} items into the keyStore", psiKeySet.size());

        return new StoredAlgorithmKey(psiKeySet);
    }

    private boolean writeKeySetFromFile(Set<PsiKey> psiKeySet){
        log.info("Calling writeKeySetFromFile with psiKeySet.size() = {}", psiKeySet.size());
        File keyStoreFile = new File(KEY_STORE_FILENAME);
        try {
            if (!keyStoreFile.exists())
                keyStoreFile.createNewFile();
            FileWriter fileWriter = new FileWriter(keyStoreFile, false);
            for (PsiKey psiKey : psiKeySet)
                fileWriter.write(CustomTypeConverter.convertObjectToString(psiKey)+"\n");
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
