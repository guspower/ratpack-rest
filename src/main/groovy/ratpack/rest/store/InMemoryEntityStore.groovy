package ratpack.rest.store

import com.google.common.collect.ImmutableList

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

class InMemoryEntityStore<T> implements EntityStore<T> {

    private final List<T> store = []
    private final Class entityType

    InMemoryEntityStore() {
        this.entityType = HashMap.class
    }

    InMemoryEntityStore(Class entityType) {
        this.entityType = entityType
    }

    InMemoryEntityStore(Class entityType, List data) {
        this.entityType = entityType
        this.store = data
    }

    Class getType() { entityType }

    String create(Object data) throws ConstraintViolationException {
        def instance = entityType.newInstance()

        if(data) {
            if(data instanceof Map) {
                data.each { key, value ->
                    instance."$key" = value
                }
            } else {
                data.properties.findAll { key, value -> key != 'class' }.each { key, value ->
                    instance."$key" = value
                }
            }
        }

        String id = UUID.randomUUID().toString()
        instance.id = id

        Validator validator = Validation.buildDefaultValidatorFactory().validator
        Set<ConstraintViolation> violations = validator.validate(instance)
        if(violations) {
            throw new ConstraintViolationException(violations)
        }

        store << instance
        id
    }

    @Override
    boolean delete(String id) {
        def entity = store.find { it.id == id }
        if(entity) {
            store.remove entity
        }
        entity
    }

    @Override
    boolean deleteAll() {
        store.clear()
        true
    }

    @Override
    List<T> getAll() {
        ImmutableList.copyOf(store)
    }

    @Override
    T get(String id) {
        store.find {
            it.id == id
        }
    }

}
