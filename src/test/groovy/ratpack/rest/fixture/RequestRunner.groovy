package ratpack.rest.fixture

import groovy.transform.ToString
import groovyx.gpars.GParsPool

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@ToString(includePackage = false, includeNames = true)
class RequestRunner {

    final Benchmark benchmark

    AtomicBoolean run = new AtomicBoolean(true)
    AtomicInteger success = new AtomicInteger(0)
    AtomicInteger failure = new AtomicInteger(0)

    String name
    int threadCount = 25
    int duration

    RequestRunner(String name, int duration) {
        this.name     = name
        this.duration = duration
        this.benchmark = new Benchmark(name)
    }

    Closure request = { AtomicBoolean keepGoing, HttpFixture httpFixture ->
        while(keepGoing.get()) {
            if(httpFixture.getNext()) {
                success.incrementAndGet()
            } else {
                failure.incrementAndGet()
            }
        }
    }

    void run(HttpFixture httpFixture) {
        start()

        Thread.start {
            GParsPool.withPool threadCount, {
                threadCount.times { int index ->
                    GParsPool.executeAsync(
                        request.curry(run, httpFixture.clone())
                    )
                }
            }
        }

        Thread control = Thread.start {
            Thread.sleep duration
            stop()
        }

        control.join()
    }

    private void start() {
        run.set true
        benchmark.start duration, threadCount
    }

    private void stop() {
        run.set false
        benchmark.stop success.get(), failure.get()
    }

}


