package it.lockless.psidemoserver.util;

import com.google.common.hash.BloomFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@SuppressWarnings("UnstableApiUsage")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BloomFilterHelperTest {

    Set<String> filterDataSet;
    Set<String> comparisonDataSet;

    @BeforeEach
    public void init(){
        this.filterDataSet = new HashSet<>();
        this.comparisonDataSet = new HashSet<>();
        for(int i = 0; i < 100; i++){
            filterDataSet.add("SERVER-"+i);
        }

        for(int i = 0; i < 50; i++){
            comparisonDataSet.add("SERVER-"+i);
        }
        for(int i = 0; i < 50; i++){
            comparisonDataSet.add("OTHER-"+i);
        }
    }

    @Test
    public void bloomFilterBasicTest(){
        BloomFilter<CharSequence> bloomFilter = BloomFilterHelper.createBloomFilterByteArrayFromSet(filterDataSet, 0.01);
        Set<String> filteredSet = BloomFilterHelper.filterSet(comparisonDataSet, bloomFilter);
        assertTrue(filteredSet.size() > 50);
    }

    @Test
    public void serializeDeserializeTest(){
        BloomFilter<CharSequence> bloomFilter = BloomFilterHelper.createBloomFilterByteArrayFromSet(filterDataSet, 0.01);
        byte[] serializedBloomFilter = BloomFilterHelper.convertBloomFilterToByteArray(bloomFilter);
        BloomFilter<CharSequence> deserializedBloomFilter = BloomFilterHelper.getBloomFilterFromByteArray(serializedBloomFilter);
        Set<String> filteredSet = BloomFilterHelper.filterSet(comparisonDataSet, deserializedBloomFilter);
        assertTrue(filteredSet.size() > 50);
    }
}
