package fuel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class RealSuspendHttpLoaderTest {

    private lateinit var realHttpLoader: RealSuspendHttpLoader
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        realHttpLoader = RealSuspendHttpLoader(OkHttpClient())

        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test(expected = HttpException::class)
    fun `unsuccessful 404 Error`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Hello World"))

        val unsuccessfulRequest = Request.Builder().data(mockWebServer.url("get")).build()

        val string = withContext(Dispatchers.IO) {
            realHttpLoader.get(unsuccessfulRequest).body!!.string()
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `get test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder().data(mockWebServer.url("get")).build()

        val string = withContext(Dispatchers.IO) {
            realHttpLoader.get(request).body!!.string()
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `post test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Hi?".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("post"))
            .requestBody(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            realHttpLoader.post(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("POST", request1.method)
        val utf8 = withContext(Dispatchers.IO) {
            request1.body.readUtf8()
        }
        assertEquals("Hi?", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for post`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("post")).build()

        withContext(Dispatchers.IO) {
            realHttpLoader.post(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("POST", request1.method)
    }

    @Test
    fun `put test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Put There".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("put"))
            .requestBody(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            realHttpLoader.put(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("PUT", request1.method)
        val utf8 = withContext(Dispatchers.IO) {
            request1.body.readUtf8()
        }
        assertEquals("Put There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for put`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("put")).build()

        withContext(Dispatchers.IO) {
            realHttpLoader.put(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("PUT", request1.method)
    }

    @Test
    fun `patch test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Hello There".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("patch"))
            .requestBody(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            realHttpLoader.patch(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("PATCH", request1.method)
        val utf8 = withContext(Dispatchers.IO) {
            request1.body.readUtf8()
        }
        assertEquals("Hello There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for patch`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("patch")).build()

        withContext(Dispatchers.IO) {
            realHttpLoader.patch(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("PATCH", request1.method)
    }

    @Test
    fun `delete test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder()
            .data(mockWebServer.url("delete"))
            .requestBody(null)
            .build()

        val string = withContext(Dispatchers.IO) {
            realHttpLoader.delete(request).body!!.string()
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("DELETE", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `head test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("head")).build()

        withContext(Dispatchers.IO) {
            realHttpLoader.head(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("HEAD", request1.method)
    }

    @Test
    fun `connect test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .data(mockWebServer.url("connect"))
            .method("CONNECT")
            .requestBody(null)
            .build()

        withContext(Dispatchers.IO) {
            realHttpLoader.method(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("CONNECT", request1.method)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty method for CONNECT`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val request = Request.Builder().data(mockWebServer.url("connect")).build()

        withContext(Dispatchers.IO) {
            realHttpLoader.method(request)
        }

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("CONNECT", request1.method)
    }
}