package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.CONFLICT;

/**
 * ConflictProblem should be thrown for requests taht could not be completed due to a conflict with the current state
 * of the target resource.
 **/
public class ConflictProblem extends AbstractThrowableProblem {
    public ConflictProblem(String title, String detail) {
        super(ErrorConstants.TYPE_CONFLICT, title, CONFLICT, detail);
    }
}
