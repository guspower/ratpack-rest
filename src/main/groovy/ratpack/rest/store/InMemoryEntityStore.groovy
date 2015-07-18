package ratpack.rest.store

import com.google.common.collect.ImmutableList

class InMemoryEntityStore<T> implements EntityStore<T> {

    private List<T> store = []

    InMemoryEntityStore() {}

    InMemoryEntityStore(List<T> data) {
        this.store = data
    }

    @Override
    List<T> getAll() {
        ImmutableList.copyOf(store)
    }

    @Override
    T get(String id) {
        store.find {
            it.id == id
        }
    }

}
