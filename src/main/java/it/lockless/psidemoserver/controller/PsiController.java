package it.lockless.psidemoserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.lockless.psidemoserver.controller.errors.EntityNotFoundProblem;
import it.lockless.psidemoserver.controller.errors.RequestTimeoutProblem;
import it.lockless.psidemoserver.model.PsiDatasetMapDTO;
import it.lockless.psidemoserver.model.PsiServerDatasetPageDTO;
import it.lockless.psidemoserver.model.PsiSessionWrapperDTO;
import it.lockless.psidemoserver.model.SessionParameterDTO;
import it.lockless.psidemoserver.service.PsiSessionService;
import it.lockless.psidemoserver.util.exception.SessionExpiredException;
import it.lockless.psidemoserver.util.exception.SessionNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/psi")
public class PsiController {
	private final PsiSessionService psiSessionService;

	public PsiController(PsiSessionService psiSessionService) {
		this.psiSessionService = psiSessionService;
	}

	@Operation(description = "Get a description of the PSI sessions supported by the server",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@GetMapping(value = "/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SessionParameterDTO>> getParameters() {
		return ResponseEntity.ok(psiSessionService.getAvailableSessionParameterDTO());
	}

	@Operation(description = "Create a new PSI session",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiSessionWrapperDTO> initSession(@RequestBody SessionParameterDTO sessionParameterDTO) {
		return ResponseEntity.ok(psiSessionService.initSession(sessionParameterDTO));
	}

	@Operation(description = "Retrieve the server encryption of the client dataset passed in input.",  responses = {
			@ApiResponse(responseCode = "200", description = "successful operation"),
			@ApiResponse(responseCode = "400", description = "wrong or missing input"),
			@ApiResponse(responseCode = "404", description = "session not found"),
			@ApiResponse(responseCode = "408", description = "session expired"),
			@ApiResponse(responseCode = "500", description = "internal server error") })
	@PostMapping(value = "/{sessionId}/clientSet", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PsiDatasetMapDTO> encryptClientDataset(@PathVariable("sessionId") Long sessionId, @RequestBody PsiDatasetMapDTO psiDatasetMapDTO) {
        try {
            return ResponseEntity.ok(psiSessionService.encryptClientSet(sessionId, psiDatasetMapDTO));
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
        try {
            return ResponseEntity.ok(psiSessionService.getEncryptedServerDataset(sessionId, page, size));
        } catch (SessionNotFoundException e) {
            throw new EntityNotFoundProblem("sessionNotFound","Session identified by "+sessionId+" not found");
        } catch (SessionExpiredException e) {
			throw new RequestTimeoutProblem("sessionExpired", "Session identified by "+sessionId+" is expired");
		}
	}



}