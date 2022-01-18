package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.REQUEST_TIMEOUT;

/**
 * ConflictProblem should be thrown for requests taht could not be completed due to a conflict with the current state
 * of the target resource.
 **/
public class RequestTimeoutProblem extends AbstractThrowableProblem {
    public RequestTimeoutProblem(String title, String detail) {
        super(ErrorConstants.TYPE_REQUEST_TIMEOUT, title, REQUEST_TIMEOUT, detail);
    }
}
