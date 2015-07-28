package ratpack.rest

import ratpack.rest.store.EntityStore

interface RestEntity {

    String getName()
    EntityStore getStore()
    Class getType()

}

