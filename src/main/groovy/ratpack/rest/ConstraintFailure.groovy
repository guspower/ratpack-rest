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

    static List<ConstraintFailure> clientSuppliedId(String name, String id) {
        [new ConstraintFailure(
            field:   'id',
            value:   id,
            type:    name,
            message: 'cannot be specified by client'
        )]
    }

    static List<ConstraintFailure> noClientSuppliedId(String name) {
        [new ConstraintFailure(
            field:   'id',
            type:    name,
            message: 'must be specified in url by client'
        )]
    }

    ConstraintFailure(Map data) {
        data.each { key, value ->
            this."$key" = value
        }
    }

    ConstraintFailure(ConstraintViolation violation) {
        this.field   = violation.propertyPath
        this.value   = violation.invalidValue
        this.type    = violation.rootBeanClass.simpleName
        this.message = violation.message
    }

    String field
    String value
    String type
    String message

}
