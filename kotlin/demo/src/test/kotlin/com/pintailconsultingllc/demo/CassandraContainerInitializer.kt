package com.pintailconsultingllc.demo

import com.datastax.driver.core.KeyspaceMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.utility.DockerImageName
import java.util.stream.Collectors

/**
 * ApplicationContextInitializer implementation for managing the Cassandra Testcontainers container.
 */
class CassandraContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        const val EXPOSED_PORT = 9042
        const val KEYSPACE_NAME = "demo"
        const val DATA_CENTER = "datacenter1"
        const val SCHEMA_ACTION = "create_if_not_exists"
        val cassandraContainer = CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))
            .withExposedPorts(EXPOSED_PORT) as CassandraContainer<*>
        val log: Logger = LoggerFactory.getLogger(CassandraContainerInitializer::class.java)
        val CREATE_KEYSPACE_QUERY = String.format(
            "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};",
            KEYSPACE_NAME
        )

        init {
            log.info("Starting the Cassandra container...")
            cassandraContainer.start()
        }
    }

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        log.info("Initializing Cassandra container...")
        log.info(String.format("Docker image name: %s", cassandraContainer.dockerImageName))
        log.info(String.format("Docker container ID: %s", cassandraContainer.containerId))
        log.info(String.format("Docker container name: %s", cassandraContainer.containerName))
        TestPropertyValues.of(
            String.format("spring.data.cassandra.keyspace-name=%s", KEYSPACE_NAME),
            String.format("spring.data.cassandra.contact-points=%s", cassandraContainer.containerIpAddress),
            String.format("spring.data.cassandra.port=%s", cassandraContainer.getMappedPort(EXPOSED_PORT)),
            String.format("spring.data.cassandra.local-datacenter=%s", DATA_CENTER),
            String.format("spring.data.cassandra.schema-action=%s", SCHEMA_ACTION)
        ).applyTo(configurableApplicationContext.environment)
        val cluster = cassandraContainer.cluster
        cluster.connect().use { session ->
            session.execute(CREATE_KEYSPACE_QUERY)
            val keyspaces =
                session.cluster.metadata.keyspaces
            val filteredKeyspaces = keyspaces
                .stream()
                .filter { km: KeyspaceMetadata -> km.name == KEYSPACE_NAME }
                .collect(Collectors.toList())
            log.info(String.format("Number of %s keyspaces: %d", KEYSPACE_NAME, filteredKeyspaces.size))
        }
    }

}