package ratpack.rest.fixture

import ratpack.registry.Registry
import ratpack.rest.Main
import ratpack.test.MainClassApplicationUnderTest

class RestApplicationUnderTest extends MainClassApplicationUnderTest {

    private Registry registry

    RestApplicationUnderTest() {
        super(Main)
    }

    @Override
    protected Registry createOverrides(Registry registry) {
        this.registry = registry
    }

}
