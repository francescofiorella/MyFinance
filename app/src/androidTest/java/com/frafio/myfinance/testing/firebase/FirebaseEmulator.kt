package com.frafio.myfinance.testing.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * The Firebase Emulator Suite on this PC, reached from the device through `adb reverse` (the
 * `firebaseEmulatorReverse` Gradle task). [connect] must run before anything touches Firebase:
 * `useEmulator` throws once an instance is in use, so a forgotten call fails instead of reaching
 * the real project.
 */
object FirebaseEmulator {

    private const val HOST = "127.0.0.1"
    private const val FIRESTORE_PORT = 8080
    private const val AUTH_PORT = 9099

    private var connected = false

    private val projectId: String
        get() = checkNotNull(FirebaseApp.getInstance().options.projectId)

    @Synchronized
    fun connect() {
        if (connected) return
        listOf(FIRESTORE_PORT, AUTH_PORT).forEach { port ->
            check(isReachable(port)) {
                "Firebase emulator not reachable on $HOST:$port — run `firebase emulators:start --only auth,firestore` " +
                    "in the repo root (see docs/testing.md)"
            }
        }
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings { setLocalCacheSettings(memoryCacheSettings {}) }
            useEmulator(HOST, FIRESTORE_PORT)
        }
        FirebaseAuth.getInstance().useEmulator(HOST, AUTH_PORT)
        connected = true
    }

    fun clearFirestore() {
        request("DELETE", FIRESTORE_PORT, "/emulator/v1/projects/$projectId/databases/(default)/documents")
    }

    fun clearAuth() {
        request("DELETE", AUTH_PORT, "/emulator/v1/projects/$projectId/accounts")
    }

    /** The mails the Auth emulator would have sent, as `email to requestType` (`PASSWORD_RESET`, `VERIFY_EMAIL`). */
    fun oobCodes(): List<Pair<String, String>> {
        val codes = JSONObject(request("GET", AUTH_PORT, "/emulator/v1/projects/$projectId/oobCodes"))
            .optJSONArray("oobCodes") ?: return emptyList()
        return (0 until codes.length()).map { index ->
            codes.getJSONObject(index).let { it.getString("email") to it.getString("requestType") }
        }
    }

    fun randomEmail(): String = "test-${UUID.randomUUID()}@example.com"

    // An HTTP round trip, not a socket connect: `adb reverse` accepts the connection on the device
    // even when nothing listens on the PC.
    private fun isReachable(port: Int): Boolean {
        val connection = URL("http://$HOST:$port/").openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 2_000
            connection.readTimeout = 2_000
            connection.responseCode
            true
        } catch (e: IOException) {
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun request(method: String, port: Int, path: String): String {
        val connection = URL("http://$HOST:$port$path").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            check(connection.responseCode in 200..299) { "$method $path answered ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
