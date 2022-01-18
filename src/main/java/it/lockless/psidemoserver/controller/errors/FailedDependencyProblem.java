package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.FAILED_DEPENDENCY;

public class FailedDependencyProblem extends AbstractThrowableProblem {

    public FailedDependencyProblem(String title, String detail) {
        super(ErrorConstants.TYPE_FAILED_DEPENDENCY, title, FAILED_DEPENDENCY, detail);
    }
}
