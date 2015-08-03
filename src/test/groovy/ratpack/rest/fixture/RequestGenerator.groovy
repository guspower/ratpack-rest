package ratpack.rest.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.transform.ToString
import groovyx.gpars.GParsPool
import jodd.http.HttpRequest

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@ToString(includePackage = false, includeNames = true)
class RequestGenerator {

    final String REPORT_DIR_KEY = 'test.report.dir'

    long start
    long end
    AtomicBoolean run = new AtomicBoolean(true)
    AtomicInteger success = new AtomicInteger(0)
    AtomicInteger failure = new AtomicInteger(0)

    String name
    int threadCount = 25
    int duration

    Map<String, String> before, after

    RequestGenerator(String name, int duration) {
        this.name     = name
        this.duration = duration
    }

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

    Thread run(String url) {
        start()

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

    private void start() {
        start = System.currentTimeMillis()
        before = systemInfo
    }

    private void stop() {
        run.set false
        end = System.currentTimeMillis()
        after = systemInfo
    }

    void report() {
        def mapper = new ObjectMapper()
        mapper.enable SerializationFeature.INDENT_OUTPUT

        String data = mapper.writeValueAsString (results() + sys())
        String reportDirPath = System.properties.getProperty(REPORT_DIR_KEY)
        if(reportDirPath) {
            def reportDir = new File(reportDirPath)
            reportDir.mkdirs()
            def report = new File(reportDir, "$name-${System.currentTimeMillis()}.json")
            if(report.createNewFile()) { report.text = data }
        } else {
            println data
        }
    }

    private Map results() {
        [
            test: [
                config: config(),
                time: [
                    start:    start,
                    end:      end,
                    duration: (end - start)
                ],
                requests: [
                    success:  success.get(),
                    failure:  failure.get(),
                    total:    (success.get() + failure.get()),
                    average:  Math.round(((success.get() + failure.get()) / ((end - start) / 1000)))
                ]
            ]
        ]
    }

    private Map config() {
        [
            duration: duration,
            threads:  threadCount
        ]
    }

    private Map sys() {
        def system = System.properties

        [
            host: [
                memory: [
                    total: Runtime.runtime.totalMemory(),
                    free:  Runtime.runtime.freeMemory(),
                    max:   Runtime.runtime.maxMemory()
                ],
                name: InetAddress.localHost.hostName,
                os: [
                    name:    system.'os.name',
                    version: system.'os.version'
                ],
                stat: [
                    before: before,
                    after: after
                ]
            ],
            vm: [
                name:    system.'java.vm.name',
                spec:    system.'java.vm.specification.version',
                version: system.'java.runtime.version',
                vendor:  system.'java.vm.vendor'
            ]
        ]
    }

    private static Map getSystemInfo() {
        [
            disk:   getSystemInfo('/proc/diskstats'),
            system: getSystemInfo('/proc/stat'),
            memory: getSystemInfo('/proc/meminfo'),
            vm:     getSystemInfo('/proc/vmstat')
        ]
    }

    private static String getSystemInfo(String name) {
        def file = new File(name)
        (file.exists() && file.canRead()) ? file.text : ''
    }

}


