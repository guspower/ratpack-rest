package ratpack.rest.store

interface EntityStore<T> {

    List<T> getAll()

    T get(String id)

}
