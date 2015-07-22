package ratpack.rest

import groovy.transform.ToString
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore

@ToString(includePackage = false, includeNames = true)
class RestEntity {

    static final DEFAULT_TYPE = HashMap.class

    final EntityStore store = new InMemoryEntityStore<?>()
    final String name
    final Class type

    RestEntity(Class type) {
        this.type = type
        this.name = type.simpleName.toLowerCase()
        this.store = new InMemoryEntityStore(type)
    }

    RestEntity(Class type, EntityStore store) {
        this.type = type
        this.name = type.simpleName.toLowerCase()
        this.store = store
    }

    RestEntity(String name) {
        this.name = name
        this.type = DEFAULT_TYPE
        this.store = new InMemoryEntityStore(type)
    }

    RestEntity(String name, EntityStore store) {
        this.name = name
        this.type = DEFAULT_TYPE
        this.store = store
    }

}