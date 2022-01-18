package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.PRECONDITION_FAILED;

public class PreconditionFailedProblem extends AbstractThrowableProblem {

    public PreconditionFailedProblem(String title, String message) {
        super(ErrorConstants.TYPE_PRECONDITION_FAILED,
                title,
                PRECONDITION_FAILED,
                message);
    }
}
