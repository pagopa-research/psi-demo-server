package it.lockless.psidemoserver.config;

import it.lockless.psidemoserver.entity.enumeration.Algorithm;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StoredAlgorithmKey {

    private ConcurrentHashMap<Long, PsiKey> keyIdMap;

    StoredAlgorithmKey(Set<PsiKey> psiKeySet) {
        this.keyIdMap = new ConcurrentHashMap<>();
        psiKeySet.forEach(key ->
                keyIdMap.put(key.getKeyId(), key));
    }

    public void storeKey(PsiKey psiKey){
        keyIdMap.put(psiKey.getKeyId(), psiKey);

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
