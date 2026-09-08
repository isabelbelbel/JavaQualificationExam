package com.qualification.exam.exception;

import java.util.Set;

public class CircularDependencyException
        extends InvalidProjectPlanException {

    private final Set<String> affectedTaskKeys;

    public CircularDependencyException(
            Set<String> affectedTaskKeys
    ) {
        super(
                "Circular dependency detected involving tasks: "
                        + affectedTaskKeys
        );

        this.affectedTaskKeys = Set.copyOf(
                affectedTaskKeys
        );
    }

    public Set<String> getAffectedTaskKeys() {
        return affectedTaskKeys;
    }
}