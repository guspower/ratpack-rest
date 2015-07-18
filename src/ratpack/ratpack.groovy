import static ratpack.groovy.Groovy.ratpack

ratpack {
    bindings {

    }
    handlers {
        get {
            render "Hello World!"
        }
    }
}
