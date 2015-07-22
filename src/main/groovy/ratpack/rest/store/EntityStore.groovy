package ratpack.rest.store

interface EntityStore<T> {

    String create()

    List<T> getAll()

    T get(String id)

    Class getType()

}
