package it.lockless.psidemoserver.repository;

import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SerializedBloomFilterRepositoryTest {

    @Autowired
    SerializedBloomFilterRepository serializedBloomFilterRepository;

    private SerializedBloomFilter serializedBloomFilter1;

    @BeforeEach
    public void init(){
        String s1 = "String1";
        String s2 = "String2";
        serializedBloomFilter1 = new SerializedBloomFilter();
        serializedBloomFilter1.setSerializedValue(s1.getBytes(StandardCharsets.UTF_8));
        serializedBloomFilter1.setBloomFilterCreationDate(Instant.now());

        SerializedBloomFilter serializedBloomFilter2 = new SerializedBloomFilter();
        serializedBloomFilter2.setSerializedValue(s2.getBytes(StandardCharsets.UTF_8));
        serializedBloomFilter2.setBloomFilterCreationDate(Instant.now().minusSeconds(100));

        this.serializedBloomFilter1 = serializedBloomFilterRepository.save(serializedBloomFilter1);
        serializedBloomFilterRepository.save(serializedBloomFilter2);
    }

    @Test
    public void findLastByBloomFilterCreationDateTest(){
        Optional<SerializedBloomFilter> serializedBloomFilterOptional = serializedBloomFilterRepository.findFirstByOrderByBloomFilterCreationDateDesc();
        assertTrue(serializedBloomFilterOptional.isPresent());
        assertEquals(serializedBloomFilter1.getId(), serializedBloomFilterOptional.get().getId());
    }
}
