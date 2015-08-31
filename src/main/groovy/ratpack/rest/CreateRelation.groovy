package ratpack.rest

import groovy.transform.ToString
import ratpack.rest.store.Relation

@ToString(includePackage = false, includeNames = true)
class CreateRelation {

    String from
    String to
    String name = Relation.DEFAULT_NAME

}
