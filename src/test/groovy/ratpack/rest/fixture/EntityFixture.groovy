package ratpack.rest.fixture

import com.fasterxml.jackson.databind.ObjectMapper

class EntityFixture<T> {

    private final Closure config
    private final Class<T> type
    private final int size
    private final Random random

    final List<T> data = []

    EntityFixture(int size, Class<T> type = HashMap, Closure config = { it }, Random random = new Random()) {
        this.type = type
        this.size = size
        this.config = config
        this.random = random

        data.addAll generate(size)
    }

    T lookup(String id) {
        data.find { it.id == id }
    }

    T hydrate(String json) {
        new ObjectMapper().readValue(json, type)
    }

    T instance(String id) {
        T result = type.newInstance()
        result.id = id

        config result

        result
    }

    T pick() {
        data.get(random.nextInt(data.size()))
    }

    private T next() {
        instance(nextId())
    }

    private String nextId() {
        UUID.randomUUID().toString()
    }

    private List<T> generate(int count) {
        (0..count).collect { next() }
    }

}
