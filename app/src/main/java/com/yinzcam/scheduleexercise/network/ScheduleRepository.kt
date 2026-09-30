package com.yinzcam.scheduleexercise.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yinzcam.scheduleexercise.model.ScheduleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class ScheduleRepository(
    private val scheduleUrl: String = DEFAULT_SCHEDULE_URL,
    private val gson: Gson = Gson()
) {

    companion object {
        const val DEFAULT_SCHEDULE_URL =
            "http://files.yinzcam.com.s3.amazonaws.com/iOS/interviews/ScheduleExercise/schedule.json"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 15_000
    }

    suspend fun fetchSchedule(): Result<ScheduleResponse> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(scheduleUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doInput = true
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    IOException("Unexpected response code $responseCode from schedule feed")
                )
            }

            val body = BufferedReader(
                InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)
            ).use { it.readText() }

            val parsed = gson.fromJson(body, ScheduleResponse::class.java)
                ?: return@withContext Result.failure(IOException("Empty schedule response"))

            Result.success(parsed)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
