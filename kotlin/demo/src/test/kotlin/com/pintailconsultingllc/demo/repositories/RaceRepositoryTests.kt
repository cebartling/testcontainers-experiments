package com.pintailconsultingllc.demo.repositories

import com.datastax.driver.core.utils.UUIDs
import com.pintailconsultingllc.demo.CassandraContainerInitializer
import com.pintailconsultingllc.demo.DockerTestSupport
import com.pintailconsultingllc.demo.TestSupport
import com.pintailconsultingllc.demo.entities.Race
import org.junit.jupiter.api.*
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@DataCassandraTest
@ContextConfiguration(initializers = [CassandraContainerInitializer::class])
@Tag(TestSupport.INTEGRATION_TEST)
@DisplayName("RaceRepository integration test suite")
class RaceRepositoryTests : DockerTestSupport() {

    @Autowired
    var raceRepository: RaceRepository? = null

    @Autowired
    var reactiveCassandraTemplate: ReactiveCassandraTemplate? = null


    val expectedUuid = UUIDs.timeBased()
    val expectedName = "Fat Bike Birkie"
    val expectedDescription = "Some description"
    var newRace: Race? = null
    var createdRace: Race? = null

    @Nested
    @DisplayName("creating a new race")
    inner class SaveNewRaceTests {
        val date = Date()

        @BeforeEach
        fun doBeforeEachTest() {
            val truncateMono: Mono<Void> = reactiveCassandraTemplate!!.truncate(Race::class.java)
            StepVerifier.create(truncateMono).verifyComplete()
            newRace = Race(id = expectedUuid, name = expectedName, description = expectedDescription, date = date)
            val saveMono: Mono<Race> = raceRepository!!.save(newRace!!)
            StepVerifier.create(saveMono).consumeNextWith { race: Race ->
                createdRace = race
            }.verifyComplete()
        }

        @DisplayName("should save a new race to the database")
        @Test
        fun verifyCreate() {
            Assertions.assertAll(
                Executable {
                    Assertions.assertEquals(
                        newRace!!.id,
                        createdRace!!.id
                    )
                },
                Executable {
                    Assertions.assertEquals(
                        newRace!!.name,
                        createdRace!!.name
                    )
                },
                Executable {
                    Assertions.assertEquals(
                        newRace!!.description,
                        createdRace!!.description
                    )
                },
                Executable {
                    Assertions.assertEquals(
                        newRace!!.date.time,
                        createdRace!!.date.time
                    )
                },
            )
        }
    }

}