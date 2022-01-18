package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.BAD_REQUEST;

/**
 * BadRequestProblem should be thrown for general bad requests that do not require a specific problem
 * as they are only used once.
 **/
public class BadRequestProblem extends AbstractThrowableProblem {
    public BadRequestProblem(String title, String detail) {
        super(ErrorConstants.TYPE_BAD_REQUEST, title, BAD_REQUEST, detail);
    }
}
