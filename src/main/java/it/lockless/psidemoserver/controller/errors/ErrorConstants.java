package it.lockless.psidemoserver.controller.errors;

import java.net.URI;

public final class ErrorConstants {

    // General
    public static final String PROBLEM_BASE_URL = "https://www.to.be.defined,com/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final String ERR_VALIDATION = "error.validation";

    // BadRequestProblem
    public static final URI TYPE_BAD_REQUEST = URI.create(PROBLEM_BASE_URL + "/bad-request");

    // ConflictProblem
    public static final URI TYPE_CONFLICT = URI.create(PROBLEM_BASE_URL + "/conflict");

    // EntityNotFoundProblem
    public static final URI TYPE_ENTITY_NOT_FOUND = URI.create(PROBLEM_BASE_URL + "/entity-not-found");

    // RequestTimeoutProblem
    public static final URI TYPE_REQUEST_TIMEOUT = URI.create(PROBLEM_BASE_URL + "/request-timeout");

    // FailedDependencyProblem
    public static final URI TYPE_FAILED_DEPENDENCY = URI.create(PROBLEM_BASE_URL + "/failed-dependency");
    public static final String TYPE_FAILED_ERROR_STRING = "The connection to the data source failed due to an invalid or missing authentication of the server to the data source";

    // PreconditionFailed
    public static final URI TYPE_PRECONDITION_FAILED = URI.create(PROBLEM_BASE_URL + "/precondition-failed");

    private ErrorConstants() {
    }
}
