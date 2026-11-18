package org.projectcontinuum.feature.base.analytics.node

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.projectcontinuum.core.commons.context.ExecutionContext
import org.projectcontinuum.core.commons.protocol.progress.NodeProgressCallback
import org.projectcontinuum.core.commons.utils.NodeInputReader
import org.projectcontinuum.core.commons.utils.NodeOutputWriter
import org.springframework.web.client.RestClient
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.InetSocketAddress
import java.util.Collections
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Template rendering of the REST node: normal FreeMarker features work, Java access from templates does not. */
class RestNodeModelTest {

    private lateinit var server: HttpServer
    private val received = Collections.synchronizedList(mutableListOf<Pair<String, String>>())
    private lateinit var baseUrl: String

    private lateinit var nodeModel: RestNodeModel
    private lateinit var mockOutputWriter: NodeOutputWriter
    private lateinit var mockPortWriter: NodeOutputWriter.OutputPortWriter
    private val context = ExecutionContext(credentialFetcher = { emptyMap() })

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val body = exchange.requestBody.readAllBytes().toString(Charsets.UTF_8)
            received += exchange.requestURI.toString() to body
            val response = "ok".toByteArray()
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"

        nodeModel = RestNodeModel(RestClient.create(), jacksonObjectMapper())
        mockOutputWriter = mock()
        mockPortWriter = mock()
        whenever(mockOutputWriter.createOutputPortWriter("data")).thenReturn(mockPortWriter)
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    private fun run(properties: Map<String, Any>, row: Map<String, Any>): Map<String, Any> {
        val reader: NodeInputReader = mock()
        whenever(reader.read()).thenReturn(row).thenReturn(null)
        whenever(reader.getRowCount()).thenReturn(1L)
        val progress: NodeProgressCallback = mock()
        nodeModel.execute(properties, mapOf("data" to reader), mockOutputWriter, progress, context)
        val captor = argumentCaptor<Map<String, Any>>()
        verify(mockPortWriter, times(1)).write(any(), captor.capture())
        return captor.firstValue
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.response() = this["response"] as Map<String, Any>

    @Test
    fun `documented template features still render`() {
        val row = mapOf<String, Any>("userId" to 1000, "query" to "hello", "name" to "Ann", "age" to 30, "active" to true, "premium" to false)
        val props = mapOf<String, Any>(
            "method" to "POST",
            "url" to "$baseUrl/<#if row.premium>premium<#else>free</#if>/search?user=\${row.userId}&q=\${row.query}",
            "payload" to """{"name": "${'$'}{row.name}", "age": ${'$'}{row.age}, "active": <#if row.active>true<#else>false</#if>}"""
        )

        val out = run(props, row)

        assertEquals(200, out.response()["status"])
        assertEquals(
            listOf("/free/search?user=1000&q=hello" to """{"name": "Ann", "age": 30, "active": true}"""),
            received.toList()
        )
    }

    @Test
    fun `template cannot instantiate Execute with new`() {
        val props = mapOf<String, Any>(
            "method" to "GET",
            "url" to "$baseUrl/<#assign ex=\"freemarker.template.utility.Execute\"?new()>\${ex(\"id\")}",
            "payload" to ""
        )

        val response = run(props, mapOf("id" to 1)).response()

        assertEquals(-1, response["status"])
        assertEquals("validation_error", response["errorType"])
        assertTrue((response["body"] as String).contains("Template rendering failed"))
        assertTrue((response["body"] as String).contains("not allowed"), response["body"] as String)
        assertFalse((response["body"] as String).contains("uid="))
        assertTrue(received.isEmpty(), "no request may be sent")
    }

    /** A non-scalar row value (wrapped as a bean, not a String/Number/Map/List). */
    class Bean(val name: String)

    @Test
    fun `template cannot use api or call Java methods on row values`() {
        val cases = listOf(
            "$baseUrl/\${row?api.getClass().name}" to mapOf<String, Any>("id" to "x"),
            "$baseUrl/\${row.b.getClass().name}" to mapOf<String, Any>("b" to Bean("x"))
        )
        for ((url, row) in cases) {
            received.clear()
            org.mockito.Mockito.clearInvocations(mockPortWriter)
            val response = run(mapOf("method" to "GET", "url" to url, "payload" to ""), row).response()

            assertEquals(-1, response["status"], url)
            assertTrue((response["body"] as String).contains("Template rendering failed"), response["body"] as String)
            assertTrue(received.isEmpty(), "no request may be sent for $url")
        }
    }

    @Test
    fun `bean properties of row values are still readable`() {
        val response = run(
            mapOf("method" to "GET", "url" to "$baseUrl/u/\${row.b.name}", "payload" to ""),
            mapOf("b" to Bean("ann"))
        ).response()

        assertEquals(200, response["status"])
        assertEquals(listOf("/u/ann" to ""), received.toList())
    }
}
