package io.launcher.quarkus

import io.launcher.quarkus.model.QuarkusProject
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@QuarkusTest
class LauncherQuarkusResourceTest {

    @Inject
    lateinit var projectCreator: QuarkusProjectCreatorMock

    @Test
    @DisplayName("Should return a project with default configuration when there is no parameters")
    fun testNoParams() {
        given()
            .`when`().get("/api/quarkus/download")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
        assertThat(projectCreator.createdProjectRef.get(), equalTo(QuarkusProject()))
    }

    @Test
    @DisplayName("Should fail when a param is specified has empty")
    fun testWithEmptyParam() {
        given()
            .`when`()
            .get("/api/quarkus/download?g=org.acme&a=&pv=1.0.0&c=org.acme.TotoResource&e=io.quarkus:quarkus-resteasy")
            .then()
            .statusCode(400)
    }

    @Test
    @DisplayName("Should return a project with specified configuration when a few parameters are specified")
    fun testWithAFewParams() {
        given()
            .`when`()
            .get("/api/quarkus/download?a=test-app-with-a-few-arg&v=1.0.0&e=io.quarkus:quarkus-smallrye-reactive-messaging&e=io.quarkus:quarkus-kafka-streams")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"")
        assertThat(
            projectCreator.createdProjectRef.get(), equalTo(
                QuarkusProject(
                    artifactId = "test-app-with-a-few-arg",
                    version = "1.0.0",
                    extensions = setOf("io.quarkus:quarkus-kafka-streams", "io.quarkus:quarkus-smallrye-reactive-messaging")
                )
            )
        )
    }

    @Test
    @DisplayName("Should return a project with specified configuration when all parameters are specified")
    fun testWithAllParams() {
        given()
            .`when`()
            .get("/api/quarkus/download?g=com.toto&a=test-app&v=1.0.0&p=/toto&c=org.toto.TotoResource&e=io.quarkus:quarkus-resteasy&e=io.quarkus:quarkus-resteasy-jsonb")
            .then()
            .statusCode(200)
            .contentType("application/zip")
            .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
            projectCreator.createdProjectRef.get(), equalTo(
                QuarkusProject(
                    groupId = "com.toto",
                    artifactId = "test-app",
                    version = "1.0.0",
                    className = "org.toto.TotoResource",
                    path = "/toto",
                    extensions = setOf("io.quarkus:quarkus-resteasy-jsonb", "io.quarkus:quarkus-resteasy")
                )
            )
        )
    }

    @Test
    @DisplayName("Should return the default configuration")
    fun testConfig() {
        given()
            .`when`().get("/api/quarkus/config")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("environment", equalTo("dev"))
            .body("gaTrackingId", nullValue())
            .body("sentryDSN", nullValue())
    }

    @Test
    @DisplayName("Should return the extension list")
    fun testExtensions() {
        given()
            .`when`().get("/api/quarkus/extensions")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$.size()", `is`(53))
    }
}