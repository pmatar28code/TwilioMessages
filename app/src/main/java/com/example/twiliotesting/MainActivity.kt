package com.example.twiliotesting

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() {
    private var authToken =""
    private val accountSID = "ACc27112fb4b4e922d6c19495ae01fa60b"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore
        db.collection("Twilio")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.e(
                        "TEST ON SUCCESS FIRESTORE", "${document.id} => ${document.data}"
                    )
                    authToken = ""
                    val tempAuthToken = document.data
                    for(char in tempAuthToken.toString()){
                        if(char != '{' && char !='}' && char !='='){
                            authToken+=char
                        }
                    }
                    authToken = authToken.removePrefix("token")
                    Log.e("TOKEN WITHOUT OTHER STUFF", authToken)
                    makeTwilioCall(accountSID,authToken)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TEST ON FAILURE", "Error getting documents.", exception)
            }
    }
    private fun makeTwilioCall(accountSID:String, token:String){
        val callBack = object: Callback<Twiliox> {
            override fun onResponse(call: Call<Twiliox>, response: Response<Twiliox>) {
                for(message in response.body()?.messages!!){
                    Log.e("MESSAGE:","${message?.body}")
                }
                Log.e("POSITION OF MOST RECENT","${response.body()?.messages?.get(1)!!}")
            }
            override fun onFailure(call: Call<Twiliox>, t: Throwable) {
                Log.e("ON FAILURE","$t")
            }
        }
        val logger = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor(accountSID,token))
            .addInterceptor(logger)
            .build()
        val retrofitBuilder = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.twilio.com/2010-04-01/Accounts/")
            .addConverterFactory(MoshiConverterFactory.create())
        val retrofit = retrofitBuilder.build()
        val twilioClient = retrofit.create(TwilioApi::class.java)
        val twilioCall = twilioClient.getMessages()
        twilioCall.enqueue(callBack)
    }
}

