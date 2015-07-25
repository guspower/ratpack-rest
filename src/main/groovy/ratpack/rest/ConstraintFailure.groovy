package ratpack.rest

import groovy.transform.ToString

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@ToString(includePackage = false, includeNames = true)
class ConstraintFailure {

    static List<ConstraintFailure> build(ConstraintViolationException exception) {
        exception.constraintViolations.collect { ConstraintViolation violation ->
            new ConstraintFailure(violation)
        }
    }

    ConstraintFailure(ConstraintViolation violation) {
        this.field = violation.propertyPath
        this.value = violation.invalidValue
        this.type  = violation.rootBeanClass.simpleName
    }

    String field
    String value
    String type

}
