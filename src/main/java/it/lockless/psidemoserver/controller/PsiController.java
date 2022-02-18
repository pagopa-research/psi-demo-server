package it.lockless.psidemoserver.controller;

import it.lockless.psidemoserver.model.PsiAlgorithmParameterDTO;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterListDTO;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.service.DatasetService;
import it.lockless.psidemoserver.service.EncryptionService;
import it.lockless.psidemoserver.service.PsiSessionService;
import it.lockless.psidemoserver.util.exception.AlgorithmInvalidKeyException;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Exposes to clients the endpoints used to start a new session and carry on a PSI computation.
 */

@RestController
@RequestMapping("/psi")
public class PsiController {

	private static final Logger log = LoggerFactory.getLogger(PsiController.class);

	private final PsiSessionService psiSessionService;

	private final EncryptionService encryptionService;

	private final DatasetService datasetService;

	public PsiController(PsiSessionService psiSessionService, EncryptionService encryptionService, DatasetService datasetService) {
		this.psiSessionService = psiSessionService;
		this.encryptionService = encryptionService;
		this.datasetService = datasetService;
	}

	/**
	 * Retrieves a description of the PSI algorithm parameters supported by the server.
	 * @return 	200, a PsiAlgorithmParameterListDTO containing list of PSI algorithms and keys supported by the server
	 * 			500, internal server error
	 */
	@GetMapping(value = "/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiAlgorithmParameterListDTO> getParameters() {
		log.debug("Calling getParameters");
		return ResponseEntity.ok(encryptionService.getAvailablePsiAlgorithmParameter());
	}

	/**
	 * Creates a new PSI session for the requesting client.
	 * @param 	psiAlgorithmParameterDTO the psiAlgorithmParameter selected by the client
	 * @return 	200, a PsiClientSessionDTO containing the information required to setup the client side session
	 * 			400, wrong or missing input
	 * 			500, internal server error
	 */
	@PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity initSession(@Valid @RequestBody PsiAlgorithmParameterDTO psiAlgorithmParameterDTO) {
		log.debug("Calling initSession with psiAlgorithmParameterDTO = {}", psiAlgorithmParameterDTO);
		try {
			return ResponseEntity.ok(psiSessionService.initSession(psiAlgorithmParameterDTO));
		} catch (AlgorithmInvalidKeyException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	/**
	 * Retrieves the server encryption of the client dataset passed in input.
	 * @param sessionId 		the id identifying the session associated to the client
	 * @param psiDatasetMapDTO 	the client set to be encrypted by the server
	 * @return	200, a PsiDatasetMapDTO containing the client encrypted dataset
	 * 			400, wrong or missing input
	 * 			404, session not found
	 * 			408, session expired
	 * 			500, internal server error
	 */
	@PostMapping(value = "/{sessionId}/clientSet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity encryptClientDataset(@PathVariable("sessionId") Long sessionId, @Valid @RequestBody PsiDatasetMapDTO psiDatasetMapDTO) {
		log.debug("Calling encryptClientDataset with sessionId = {}, psiDatasetMapDTO.size() = {}", sessionId, psiDatasetMapDTO.getContent().size());
        try {
            return ResponseEntity.ok(encryptionService.encryptClientSet(sessionId, psiDatasetMapDTO));
        } catch (SessionNotFoundException e) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session identified by "+sessionId+" not found");
        } catch (SessionExpiredException e) {
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Session identified by "+sessionId+" is expired");
		}
	}

	/**
	 * Retrieves the encrypted dataset of the server.
	 * @param sessionId the id identifying the session associated to the client
	 * @param page		the page to be retrieved by the server set
	 * @param size		the size of the page to be retrieved
	 * @return 	200, a PsiServerDatasetPageDTO containing the server encrypted dataset page
	 * 			400, wrong or missing input
	 * 			404, session not found
	 * 			408, session expired
	 * 			500, internal server error
	 */
	@GetMapping(value = "/{sessionId}/serverSet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getEncryptedServerServerDataset(
			@PathVariable("sessionId") Long sessionId,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "size", defaultValue = "1000") Integer size) {
		log.debug("Called PsiServerDatasetPageDTO with sessionId = {}, page = {}, size = {}", sessionId, page, size);
        try {
            return ResponseEntity.ok(encryptionService.getEncryptedServerDataset(sessionId, page, size));
        } catch (SessionNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session identified by "+sessionId+" not found");
		} catch (SessionExpiredException e) {
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Session identified by "+sessionId+" is expired");
		}
	}

	/**
	 * Retrieves the description of the session identified by the sessionId
	 * @param sessionId the id identifying the session associated to the client
	 * @return 	200, a PsiClientSessionDTO containing the information about the session
	 * 			404, session not found
	 * 			500, internal server error
	 */
	@GetMapping(value = "/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getSession(@PathVariable("sessionId") Long sessionId) {
		log.debug("Called getSession with sessionId = {}", sessionId);
        try {
            return ResponseEntity.ok(psiSessionService.getPsiClientSessionDTO(sessionId));
        } catch (SessionNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session identified by "+sessionId+" not found");
		}
	}

	/**
	 * Populates the server dataset by adding new entries based on the entries of the map passed in the body.
	 * For each entry of the map, a number of entries equal to the number expressed by the value is created,
	 * with the radix of the entry being the key and the last portion of the string being an increasing counter in
	 * the format KEY-COUNTER. This endpoint is only offered for testing purposes and should be excluded by
	 * any production environment.
	 *
	 * @param datasetStructure map describing the name and number of element to be created
	 * @return	200, in case the server dataset has been populated correctly
	 * 			404, session not found
	 * 			500, internal server error
	 */
	@PostMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity initServerDataset(@RequestBody Map<String, Integer> datasetStructure) {
		log.debug("Called initServerDataset with datasetStructure = {}", datasetStructure);
		datasetService.initServerDataset(datasetStructure);
		return ResponseEntity.ok().build();
	}
}