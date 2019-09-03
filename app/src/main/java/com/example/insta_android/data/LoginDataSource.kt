package com.example.insta_android.data

import android.accounts.AccountManager
import android.content.Context
import com.example.insta_android.data.model.LoggedInUser
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.charset.Charset

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(var context: Context) {
    var client = OkHttpClient()
    private var moshi = Moshi.Builder().build()
    private var gistJsonAdapter = moshi.adapter(Gist.class)
    fun login(username: String, password: String): Result<LoggedInUser> {
        print(">>>>>>>> Trying to LOGIN\n")
        var a: AccountManager = AccountManager.get()
            // TODO: handle loggedInUser authentication
            print("body\n")
            var requestBody = FormBody.Builder()
                .add("user[email]", username)
                .add("user[password]", password)
                .build()

            print("request\n")
            var request = Request.Builder()
                .header("ContentType","application/json")
                .header("Accept", "application/json")
                .url("http://kek.arslogi.ca:3001/users/sign_in")
                .post(requestBody)
                .build()

            print("start\n")
            var response = client.newCall(request).execute()
            print("called\n")
            System.out.printf("response: %s\n", response.body!!.source().readString(Charset.defaultCharset()))
            response.use {
                if(!it!!.isSuccessful){
                    throw IOException("Unexpected code " + response)
                }
            }
            System.out.printf(">>> RESPONSE: %s\n", response)

            var preferences = context.getSharedPreferences()
            var edit = preferences.edit()

            edit.putString("auth_token", "hello")

            val user = LoggedInUser(java.util.UUID.randomUUID().toString(), username)
            return Result.Success(user)
    }

    fun logout() {
        // TODO: revoke authentication

    }
}

