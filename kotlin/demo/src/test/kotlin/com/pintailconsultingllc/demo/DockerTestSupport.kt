package com.pintailconsultingllc.demo

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.DockerClientFactory

abstract class DockerTestSupport {

    companion object {
        
        @BeforeAll
        @JvmStatic
        internal fun doBeforeTestSuite() {
            Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable,
                "Docker is not available for integration testing!"
            )
        }
    }
}