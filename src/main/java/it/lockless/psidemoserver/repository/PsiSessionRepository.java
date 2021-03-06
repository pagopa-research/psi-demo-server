package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.PsiSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository of PsiSession.
 */

public interface PsiSessionRepository extends JpaRepository<PsiSession, Long> {

    Optional<PsiSession> findBySessionId(Long sessionId);

}
