package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SerializedBloomFilterRepository extends JpaRepository<SerializedBloomFilter, Long> {

    Optional<SerializedBloomFilter> findFirstByOrderByBloomFilterCreationDateDesc();
}
