package it.lockless.psidemoserver.service;

import it.lockless.psidemoserver.entity.PsiElement;
import it.lockless.psidemoserver.repository.PsiElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);


    private final PsiElementRepository psiElementRepository;

    public DatasetService(PsiElementRepository psiElementRepository) {
        this.psiElementRepository = psiElementRepository;
    }

    public void intiServerDataset(Map<String, Integer> datasetStructure){
        log.debug("Calling intiServerDataset with datasetStructure = {}", datasetStructure);

        for(Map.Entry<String, Integer> entry : datasetStructure.entrySet()){
            for (int i = 0; i < entry.getValue(); i++) {
                PsiElement psiElement = new PsiElement();
                psiElement.setValue(entry.getKey()+"-"+entry.getValue());
                psiElementRepository.save(psiElement);
            }
        }
    }
}
