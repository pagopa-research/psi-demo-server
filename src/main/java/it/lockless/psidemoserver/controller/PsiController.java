package it.lockless.psidemoserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.lockless.psidemoserver.model.*;
import it.lockless.psidemoserver.service.BloomFilterService;
import it.lockless.psidemoserver.service.DatasetService;
import it.lockless.psidemoserver.service.EncryptionService;
import it.lockless.psidemoserver.service.PsiSessionService;
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

	@Operation(description = "Get a description of the PSI algorithms supported by the server.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiAlgorithmParameterListDTO> getParameters() {
		log.debug("Calling getParameters");
		return ResponseEntity.ok(encryptionService.getAvailablePsiAlgorithmParameter());
	}

	@Operation(description = "Create a new PSI session.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiClientSessionDTO> initSession(@Valid @RequestBody PsiAlgorithmParameterDTO psiAlgorithmParameterDTO) {
		log.debug("Calling initSession with psiAlgorithmParameterDTO = {}", psiAlgorithmParameterDTO);
		return ResponseEntity.ok(psiSessionService.initSession(psiAlgorithmParameterDTO));
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
        	return new ResponseEntity("Session identified by "+sessionId+" not found", HttpStatus.NOT_FOUND);
        } catch (SessionExpiredException e) {
			return new ResponseEntity("Session identified by "+sessionId+" is expired", HttpStatus.REQUEST_TIMEOUT);
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
		log.debug("Called PsiServerDatasetPageDTO with sessionId = {}, page = {}, size = {}", sessionId, page, size);
        try {
            return ResponseEntity.ok(encryptionService.getEncryptedServerDataset(sessionId, page, size));
        } catch (SessionNotFoundException e) {
			return new ResponseEntity("Session identified by "+sessionId+" not found", HttpStatus.NOT_FOUND);
		} catch (SessionExpiredException e) {
			return new ResponseEntity("Session identified by "+sessionId+" is expired", HttpStatus.REQUEST_TIMEOUT);
		}
	}

	@Operation(description = "Get the status of a PSI session.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "404", description = "session not found"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiClientSessionDTO> getSession(
			@PathVariable("sessionId") Long sessionId) {
		log.debug("Called getSession with sessionId = {}", sessionId);
        try {
            return ResponseEntity.ok(psiSessionService.getPsiClientSessionDTO(sessionId));
        } catch (SessionNotFoundException e) {
			return new ResponseEntity("Session identified by "+sessionId+" not found", HttpStatus.NOT_FOUND);
		}
	}

	@Operation(description = "Populate the server dataset depending on the input content, in the shape 'KEY-VALUE'",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiDatasetMapDTO> initServerDataset(@RequestBody Map<String, Integer> datasetStructure) {
		log.debug("Called initServerDataset with datasetStructure = {}", datasetStructure);
		datasetService.initServerDataset(datasetStructure);
		return ResponseEntity.ok().build();
	}
}