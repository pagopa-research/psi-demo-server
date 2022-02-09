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
 * It contains functionalities to compute the Bloom Filter.
 * */

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

    public void computeAndSaveSerializedBloomFilter(){
        SerializedBloomFilter serializedBloomFilter = this.computeSerializedBloomFilter();
        this.saveSerializedBloomFilter(serializedBloomFilter);
    }


    private void saveSerializedBloomFilter(SerializedBloomFilter serializedBloomFilter){
        this.serializedBloomFilterRepository.save(serializedBloomFilter);
    }

    Optional<SerializedBloomFilter> getLastSerializedBloomFilter(){
        return serializedBloomFilterRepository.findFirstByOrderByBloomFilterCreationDateDesc();
    }
}