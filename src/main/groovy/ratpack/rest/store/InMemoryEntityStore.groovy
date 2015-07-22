package ratpack.rest.store

import com.google.common.collect.ImmutableList

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

    String create(Map data = [:]) {
        def instance = entityType.newInstance()
        String id = UUID.randomUUID().toString()
        instance.id = id

        data.each { key, value ->
            instance."$key" = value
        }

        store << instance
        id
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
