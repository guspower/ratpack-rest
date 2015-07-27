package ratpack.rest.fixture

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Bus {

    String id
    String name
    String colour

}
