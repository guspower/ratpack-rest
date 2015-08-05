package ratpack.rest.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

class Benchmark {

    final String REPORT_DIR_KEY = 'test.report.dir'
    final String name

    long duration, threads
    long start, end
    long success, failure

    Map<String, String> before, after

    Benchmark(String name) {
        this.name = name
    }

    void start(long duration, long threads) {
        this.duration = duration
        this.threads = threads

        start = System.currentTimeMillis()
        before = systemInfo
    }

    void stop(long success, long failure) {
        this.success = success
        this.failure = failure

        end = System.currentTimeMillis()
        after = systemInfo
    }

    void report() {
        String data = asJson()

        File reportDir = findReportDir()
        if(reportDir && reportDir.exists()) {
            def report = new File(reportDir, "$name-${System.currentTimeMillis()}.json")
            if(report.createNewFile()) { report.text = data }
        } else {
            println data
        }
    }

    private String asJson() {
        def mapper = new ObjectMapper()
        mapper.enable SerializationFeature.INDENT_OUTPUT

        mapper.writeValueAsString (results() + sys())
    }

    private File findReportDir() {
        File reportDir
        String reportDirPath = System.properties.getProperty(REPORT_DIR_KEY)
        if(reportDirPath) {
            reportDir = new File(reportDirPath)
            reportDir.mkdirs()
        }

        reportDir
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
                    success:  success,
                    failure:  failure,
                    total:    (success + failure),
                    average:  Math.round(((success + failure) / ((end - start) / 1000)))
                ]
            ]
        ]
    }

    private Map config() {
        [
            duration: duration,
            name:     name,
            threads:  threads
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
