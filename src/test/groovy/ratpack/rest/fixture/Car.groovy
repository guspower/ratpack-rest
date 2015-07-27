package ratpack.rest.fixture

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.validation.constraints.NotNull

@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Car {

    String id

    @NotNull
    String manufacturer

    String colour

}
