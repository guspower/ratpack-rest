package ratpack.rest.store

import com.google.common.collect.ImmutableList

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

class InMemoryEntityStore<T> implements EntityStore<T> {

    private final static Class DEFAULT_TYPE = HashMap.class

    private final List<T> store = []
    private final Class entityType

    InMemoryEntityStore() {
        this.entityType = DEFAULT_TYPE
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
        apply instance, data

        String id = UUID.randomUUID().toString()
        instance.id = id

        validate instance

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
    boolean exists(String id) {
        get id
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

    @Override
    boolean update(String id, Object update) throws ConstraintViolationException {
        def existing = get(id)
        def updated
        if(update) {
            if(withoutId(update)) {
                updated = apply(existing.clone(), update)
                updated.id = id

                validate updated

                store.remove existing
                store.add updated
            }
        }
        updated
    }

    private void validate(Object instance) {
        if(!instance.getClass().isAssignableFrom(DEFAULT_TYPE)) {
            Validator validator = Validation.buildDefaultValidatorFactory().validator
            Set<ConstraintViolation> violations = validator.validate(instance)
            if (violations) {
                throw new ConstraintViolationException(violations)
            }
        }
    }

    private Map withoutId(Object data) {
        Map result

        if(data) {
            if(data instanceof Map) {
                result = data
            } else {
                result = data.properties
            }
            result.remove('id')
        }

        result
    }

    private apply(Object instance, Object data) {
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
        instance
    }

}
