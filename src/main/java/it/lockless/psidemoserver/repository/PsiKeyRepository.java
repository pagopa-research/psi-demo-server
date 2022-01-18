package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.PsiKey;
import it.lockless.psidemoserver.entity.enumeration.Algorithm;
import it.lockless.psidemoserver.model.enumeration.AlgorithmDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsiKeyRepository extends JpaRepository<PsiKey, Long> {

    Optional<PsiKey> findByKeyId(Long keyId);

    Optional<PsiKey> findByAlgorithmAndKeySize(Algorithm algorithm, int keySize);

}
