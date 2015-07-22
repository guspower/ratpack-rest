package ratpack.rest.store

import javax.validation.ConstraintViolationException

interface EntityStore<T> {

    String create(Object data) throws ConstraintViolationException

    List<T> getAll()

    T get(String id)

    Class getType()

}
