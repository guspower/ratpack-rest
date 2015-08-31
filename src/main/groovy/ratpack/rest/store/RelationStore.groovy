package ratpack.rest.store

import ratpack.rest.CreateRelation

import javax.validation.ConstraintViolationException

interface RelationStore {

    String create(CreateRelation create) throws ConstraintViolationException

    Relation get(String id)

}