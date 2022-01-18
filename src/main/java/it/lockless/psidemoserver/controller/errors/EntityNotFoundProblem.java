package it.lockless.psidemoserver.controller.errors;

import org.zalando.problem.AbstractThrowableProblem;

import static org.zalando.problem.Status.NOT_FOUND;

public class EntityNotFoundProblem extends AbstractThrowableProblem {

    public EntityNotFoundProblem(String title, String message) {
        super(ErrorConstants.TYPE_ENTITY_NOT_FOUND,
            title,
            NOT_FOUND,
            message);
    }
}
