package com.swago.eightball.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import com.swago.eightball.BuildConfig
import com.swago.eightball.R
import com.swago.eightball.model.Message

import kotlinx.android.synthetic.main.activity_main.*;
import java.io.IOException
import java.lang.String.format

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eightball.text = "";
        messageButton.setOnClickListener { v ->
            messageButton.isEnabled = false
            getRandomMessage() }
    }

    fun getRandomMessage() {
        val client = OkHttpClient()
        Log.d("API CALL", "Making Call")
        client.newCall(Request.Builder()
            .url(format("http://%s/messages/random", BuildConfig.EightballApiUrl))
            .get()
            .header("API_KEY", BuildConfig.EightballApiKey)
            .build()).enqueue( object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                Log.e("API CALL", "Error contacting eightballapi: " + e.toString())
                runOnUiThread { messageButton.isEnabled = true }
            }
            override fun onResponse(response: Response?) {
                Log.d("API CALL", "Response: " + response?.code())
                val message = jacksonObjectMapper()
                    .readValue<Message>(response?.body()?.string() ?: "").name
                runOnUiThread {
                    eightball.text = message
                    messageButton.isEnabled = true
                }
            }
        })
    }
}
