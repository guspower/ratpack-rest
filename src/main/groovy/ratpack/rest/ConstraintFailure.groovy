package ratpack.rest

import com.fasterxml.jackson.databind.JsonMappingException
import groovy.transform.ToString

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@ToString(includePackage = false, includeNames = true)
class ConstraintFailure {

    static List<ConstraintFailure> constraintViolation(ConstraintViolationException exception) {
        exception.constraintViolations.collect { ConstraintViolation violation ->
            new ConstraintFailure(violation)
        }
    }

    static List<ConstraintFailure> jsonMapping(JsonMappingException exception, Resource entity) {
        [new ConstraintFailure(
            field:   exception.path[-1].fieldName,
            type:    entity.name,
            message: exception.message.substring(0, exception.message.indexOf('(')).trim()
        )]
    }

    static List<ConstraintFailure> clientSuppliedId(String name, def id) {
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
