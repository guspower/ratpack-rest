package ratpack.rest.store

import javax.validation.ConstraintViolationException

interface EntityStore<T> {

    String create(Object data) throws ConstraintViolationException

    boolean delete(String id)

    boolean deleteAll()

    List<T> getAll()

    T get(String id)

    Class getType()

    boolean update(String id, Object data) throws ConstraintViolationException

}
