package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import java.util.Map;
import java.util.Optional;

public class StoredAlgorithmKey {

    Map<Algorithm, Map<Integer, PsiKey>> keyMap;

    public Optional<PsiKey> get(Algorithm algorithm, int keySize) {
        Map<Integer, PsiKey> privatePublicKeyMap = keyMap.get(algorithm);
        if (privatePublicKeyMap != null) {
            PsiKey privatePublicKey = privatePublicKeyMap.get(keySize);
            if (privatePublicKey != null)
                return Optional.of(privatePublicKey);
        }
        return Optional.empty();
    }

}
