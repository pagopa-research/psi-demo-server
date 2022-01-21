package it.lockless.psidemoserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

// This configuration is used to evict the stack trace from the error responses.
// In this way the error response contains (es.):
//      "type":"https://www.to.be.defined,com/problem/entity-not-found"
//      "title":"entityNotFound"
//      "status":404,
//      "detail":"the object with the provided id does not exist"
@Configuration
public class ProblemConfiguration {


    /*
     * Module for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    ProblemModule problemModule() {
        return new ProblemModule();
    }

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }
}
