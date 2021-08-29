package com.example.twiliotesting

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val accountSID = "AC41847eef5c541ba476941edcbfd2e51a"
        val authToken = "665a9dd0b68a21f2d6c3358eb9ce0a38"

        val logger = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor(accountSID,authToken))
            .addInterceptor(logger)
            .build()

        val retrofitBuilder = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.twilio.com/2010-04-01/Accounts/")
            .addConverterFactory(MoshiConverterFactory.create())
        val retrofit = retrofitBuilder.build()
        val twilioClient = retrofit.create(TwilioApi::class.java)
        val twilioCall = twilioClient.getMessages()
        twilioCall.enqueue(object: Callback<Twiliox> {
            override fun onResponse(call: Call<Twiliox>, response: Response<Twiliox>) {
              for(message in response.body()?.messages!!){
                  Log.e("MESSAGE:","${message?.body}")
              }
                Log.e("POSITION OF MOST RECENT","${response.body()?.messages?.get(1)!!}")
            }
            override fun onFailure(call: Call<Twiliox>, t: Throwable) {
                Log.e("ON FAILURE","$t")
            }
        })
    }
}

