package ratpack.rest.store

interface Relation {

    final static String DEFAULT_NAME = 'default'

    String getId()
    String getName()
    String getFrom()
    String getTo()

}