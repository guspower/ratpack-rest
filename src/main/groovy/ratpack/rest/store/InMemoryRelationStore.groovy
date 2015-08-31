package ratpack.rest.store

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import ratpack.rest.CreateRelation

import javax.validation.ConstraintViolationException

class InMemoryRelationStore implements RelationStore {

    private Set<Relation> relations = []

    @Override
    String create(CreateRelation create) throws ConstraintViolationException {
        def relation = new DefaultRelation(
            id: UUID.randomUUID().toString(),
            from: create.from,
            to: create.to,
            name: create.name
        )
        relations << relation
        relation.id
    }

    @Override
    Relation get(String id) {
        relations.find { it.id == id }
    }

    @EqualsAndHashCode
    @ToString(includePackage = false, includeNames = true)
    final static class DefaultRelation implements Relation {
        String id
        String name
        String from
        String to
    }

}
