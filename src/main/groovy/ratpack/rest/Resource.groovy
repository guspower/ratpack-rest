package ratpack.rest

import ratpack.rest.store.EntityStore

interface Resource {

    String getName()
    EntityStore getStore()
    Class getType()

}

