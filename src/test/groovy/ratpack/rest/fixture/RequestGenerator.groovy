package ratpack.rest.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.gpars.GParsPool
import jodd.http.HttpRequest

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class RequestGenerator {

    long start
    long end
    AtomicBoolean run = new AtomicBoolean(true)
    AtomicInteger success = new AtomicInteger(0)
    AtomicInteger failure = new AtomicInteger(0)
    int threadCount = 25

    Closure getUrl = { AtomicBoolean keepGoing, String url, int index ->
        while(keepGoing.get()) {
            int statusCode = HttpRequest.get(url).send().statusCode()
            if((statusCode > 100) && (statusCode < 400)) {
                success.incrementAndGet()
            } else {
                failure.incrementAndGet()
            }
        }
    }

    Thread run(String url, int duration) {
        start = System.currentTimeMillis()
        Thread.start {
            GParsPool.withPool threadCount, {
                threadCount.times { int index ->
                    GParsPool.executeAsync(getUrl.curry(run, url, index))
                }
            }
        }
        Thread.start {
            Thread.sleep duration
            stop()
        }
    }

    void stop() {
        run.set false
        end = System.currentTimeMillis()
    }

    String report() {
        new ObjectMapper().writeValueAsString ([
            start:   start,
            end:     end,
            success: success.get(),
            failure: failure.get(),
            total:   (success.get() + failure.get()),
            average: ((success.get() + failure.get()) / ((end - start) / 1000)),
            host:    InetAddress.localHost.hostName
        ])
    }

}


