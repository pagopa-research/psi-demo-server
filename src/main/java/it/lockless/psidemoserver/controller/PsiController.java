package it.lockless.psidemoserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.lockless.psidemoserver.controller.errors.EntityNotFoundProblem;
import it.lockless.psidemoserver.controller.errors.RequestTimeoutProblem;
import it.lockless.psidemoserver.model.PsiAlgorithmParameterListDTO;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.service.DatasetService;
import it.lockless.psidemoserver.service.EncryptionService;
import it.lockless.psidemoserver.service.PsiSessionService;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import psi.dto.PsiAlgorithmParameterDTO;

import javax.validation.Valid;
import java.util.Map;


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

	@Operation(description = "Get a description of the PSI sessions supported by the server",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiAlgorithmParameterListDTO> getParameters() {
		log.debug("Calling getParameters");
		return ResponseEntity.ok(new PsiAlgorithmParameterListDTO(encryptionService.getAvailableSessionParameterDTO()));
	}

	@Operation(description = "Create a new PSI session",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiSessionWrapperDTO> initSession(@Valid @RequestBody PsiAlgorithmParameterDTO sessionParameterDTO) {
		log.debug("Calling initSession with sessionParameterDTO = {}", sessionParameterDTO);
		return ResponseEntity.ok(psiSessionService.initSession(sessionParameterDTO));
	}

	@Operation(description = "Retrieve the server encryption of the client dataset passed in input.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "404", description = "session not found"),
			@ApiResponse(responseCode = "408", description = "session expired"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "/{sessionId}/clientSet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiDatasetMapDTO> encryptClientDataset(@PathVariable("sessionId") Long sessionId, @Valid @RequestBody PsiDatasetMapDTO psiDatasetMapDTO) {
		log.debug("Calling encryptClientDataset with sessionId = {}, psiDatasetMapDTO.size() = {}", sessionId, psiDatasetMapDTO.getContent().size());
        try {
            return ResponseEntity.ok(encryptionService.encryptClientSet(sessionId, psiDatasetMapDTO));
        } catch (SessionNotFoundException e) {
            throw new EntityNotFoundProblem("sessionNotFound","Session identified by "+sessionId+" not found");
        } catch (SessionExpiredException e) {
			throw new RequestTimeoutProblem("sessionExpired", "Session identified by "+sessionId+" is expired");
		}
	}

	@Operation(description = "Get the encrypted dataset of the server.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "404", description = "session not found"),
			@ApiResponse(responseCode = "408", description = "session expired"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/{sessionId}/serverSet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiServerDatasetPageDTO> getEncryptedServerServerDataset(
			@PathVariable("sessionId") Long sessionId,
			@RequestParam(value="page", defaultValue = "0") Integer page,
			@RequestParam(value="size", defaultValue = "1000") Integer size) {
		log.debug("Calling PsiServerDatasetPageDTO with sessionId = {}, page = {}, size = {}", sessionId, page, size);
        try {
            return ResponseEntity.ok(encryptionService.getEncryptedServerDataset(sessionId, page, size));
        } catch (SessionNotFoundException e) {
            throw new EntityNotFoundProblem("sessionNotFound","Session identified by "+sessionId+" not found");
        } catch (SessionExpiredException e) {
			throw new RequestTimeoutProblem("sessionExpired", "Session identified by "+sessionId+" is expired");
		}
	}

	@Operation(description = "Get the status of a PSI session.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "404", description = "session not found"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiSessionWrapperDTO> getSession(
			@PathVariable("sessionId") Long sessionId) {
		log.debug("Calling getSession with sessionId = {}", sessionId);
        try {
            return ResponseEntity.ok(psiSessionService.getPsiSessionWrapperDTO(sessionId));
        } catch (SessionNotFoundException e) {
            throw new EntityNotFoundProblem("sessionNotFound","Session identified by "+sessionId+" not found");
        }
	}

	@Operation(description = "Populate the server dataset depending on the input content, in the shape 'KEY-VALUE'",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiDatasetMapDTO> initServerDataset(@RequestBody Map<String, Integer> datasetStructure) {
		log.debug("Calling initServerDataset with intiServerDataset = {}", datasetStructure);
		datasetService.intiServerDataset(datasetStructure);
		return ResponseEntity.ok().build();
	}
}