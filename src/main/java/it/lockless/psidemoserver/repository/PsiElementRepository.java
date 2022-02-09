package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.PsiElement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository of PsiElement.
 * */

public interface PsiElementRepository  extends JpaRepository<PsiElement, Long> {

}
