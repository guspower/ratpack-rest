package ratpack.rest

import groovy.transform.ToString
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore

@ToString(includePackage = false, includeNames = true)
class RestEntity {

    EntityStore store = new InMemoryEntityStore<?>()
    String name

}