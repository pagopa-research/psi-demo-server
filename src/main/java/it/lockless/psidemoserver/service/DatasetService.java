package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * It offers functionalities used to initialize the database content.
 * Note: this calss is intended to be used only for testing purpose. Remove it in a business environment.
 */

@Service
public class DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    private final PsiElementRepository psiElementRepository;

    private final BloomFilterService bloomFilterService;

    @Value("${bloomfilter.enabled}")
    private boolean bloomFilterEnabled;

    public DatasetService(PsiElementRepository psiElementRepository, BloomFilterService bloomFilterService) {
        this.psiElementRepository = psiElementRepository;
        this.bloomFilterService = bloomFilterService;
    }

    /**
     * Fill the database with a dummy set described by the input map.
     * Note: This function is provided only for testing purposes
     */
    public void initServerDataset(Map<String, Integer> datasetStructure){
        log.debug("Calling initServerDataset with datasetStructure = {}", datasetStructure);
        psiElementRepository.deleteAll();
        for(Map.Entry<String, Integer> entry : datasetStructure.entrySet()){
            for (int i = 0; i < entry.getValue(); i++) {
                PsiElement psiElement = new PsiElement();
                psiElement.setValue(entry.getKey()+"-"+i);
                psiElementRepository.save(psiElement);
            }
        }

        // If enabled, we also update the Bloom Filter after setting the server dataset
        if(bloomFilterEnabled)
            bloomFilterService.computeAndSaveSerializedBloomFilter();
    }
}
