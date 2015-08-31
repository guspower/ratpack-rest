package ratpack.rest

import groovy.transform.ToString
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore

@ToString(includePackage = false, includeNames = true)
class DefaultRestEntity implements RestEntity {

    static final DEFAULT_TYPE = HashMap.class

    final EntityStore store = new InMemoryEntityStore<?>()
    final String name
    final Class type

    DefaultRestEntity(Class type) {
        this.type = type
        this.name = type.simpleName.toLowerCase()
        this.store = new InMemoryEntityStore(type)
    }

    DefaultRestEntity(Class type, EntityStore store) {
        this.type = type
        this.name = type.simpleName.toLowerCase()
        this.store = store
    }

    DefaultRestEntity(String name) {
        this.name = name
        this.type = DEFAULT_TYPE
        this.store = new InMemoryEntityStore(type)
    }

    DefaultRestEntity(String name, EntityStore store) {
        this.name = name
        this.type = DEFAULT_TYPE
        this.store = store
    }

    DefaultRestEntity(String name, Class type, EntityStore store) {
        this.name = name
        this.type = type
        this.store = store
    }

}