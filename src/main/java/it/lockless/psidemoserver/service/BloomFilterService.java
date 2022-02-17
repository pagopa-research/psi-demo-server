package it.lockless.psidemoserver.service;

import com.google.common.hash.BloomFilter;
import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.entity.SerializedBloomFilter;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import it.lockless.psidemoserver.repository.SerializedBloomFilterRepository;
import it.lockless.psidemoserver.util.BloomFilterHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Offers the functionalities to compute the Bloom Filter.
 */

@SuppressWarnings("UnstableApiUsage")
@Service
public class BloomFilterService {

    private final PsiElementRepository psiElementRepository;

    private final SerializedBloomFilterRepository serializedBloomFilterRepository;

    @Value("${bloomfilter.fpp}")
    private double bloomFilterFpp;

    public BloomFilterService(PsiElementRepository psiElementRepository, SerializedBloomFilterRepository serializedBloomFilterRepository) {
        this.psiElementRepository = psiElementRepository;
        this.serializedBloomFilterRepository = serializedBloomFilterRepository;
    }

    /**
     * Computes the Bloom Filter based on the actual element set.
     * @return an object containing a byte array representation of the Bloom Filter
     */
    private SerializedBloomFilter computeSerializedBloomFilter(){
        List<PsiElement> psiElementList = psiElementRepository.findAll();
        Set<String> dataSet = new HashSet<>();
        for(PsiElement psiElement : psiElementList)
            dataSet.add(psiElement.getValue());
        BloomFilter<CharSequence> bloomFilter = BloomFilterHelper.createBloomFilterByteArrayFromSet(dataSet, bloomFilterFpp);
        SerializedBloomFilter serializedBloomFilter = new SerializedBloomFilter();
        serializedBloomFilter.setSerializedValue(BloomFilterHelper.convertBloomFilterToByteArray(bloomFilter));
        serializedBloomFilter.setBloomFilterCreationDate(Instant.now());
        return serializedBloomFilter;
    }

    /**
     * Computes and stores the Bloom Filter based on the actual element set.
     */
    public void computeAndSaveSerializedBloomFilter(){
        SerializedBloomFilter serializedBloomFilter = computeSerializedBloomFilter();
        this.serializedBloomFilterRepository.save(serializedBloomFilter);
    }

    /**
     * Retrieves the last computed Bloom Filter.
     */
    Optional<SerializedBloomFilter> getLastSerializedBloomFilter(){
        return serializedBloomFilterRepository.findFirstByOrderByBloomFilterCreationDateDesc();
    }
}