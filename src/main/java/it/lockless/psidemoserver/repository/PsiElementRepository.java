package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.PsiElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PsiElementRepository  extends JpaRepository<PsiElement, Long> {

}
