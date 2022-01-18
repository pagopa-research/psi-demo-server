package it.lockless.psidemoserver.mapper;

import it.lockless.psidemoserver.entity.PsiSession;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.model.SessionParameterDTO;

public class PsiSessionMapper {

    public PsiSessionWrapperDTO toDTO(PsiSession psiSession){
        PsiSessionWrapperDTO sessionDTO = new PsiSessionWrapperDTO();
        SessionParameterDTO sessionParameterDTO = new SessionParameterDTO();

        sessionParameterDTO.setAlgorithm(psiSession.getAlgorithm());
        sessionParameterDTO.setKeySize(psiSession.getKeySize());
        sessionDTO.setSessionParameterDTO(sessionParameterDTO);

        sessionDTO.setExpiration(psiSession.getExpiration());

        return sessionDTO;
    }
}
