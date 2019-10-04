package com.example.insta_android.data

import android.accounts.AccountManager
import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.insta_android.data.model.LoggedInUser
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.charset.Charset
@Entity
class User {
    fun User(){}
    @PrimaryKey
    var id: Int = 0
    var email: String = ""
    var authentication_token: String = ""
}
/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource(var context: Context) {
    var client = OkHttpClient()
    private var moshi = Moshi.Builder().build()
    private var userJsonAdapter = moshi.adapter(User::class.java)
    fun login(username: String, password: String): Result<LoggedInUser> {
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var edit = preferences.edit()
        var token = preferences.getString("auth_token","")
        print(">>>>>>>> Trying to LOGIN\n")
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
            var response_text = response.body!!.source().readString(Charset.defaultCharset())
            System.out.printf("response: %s\n", response_text)
            response.use {
                if(!it!!.isSuccessful){
                    throw IOException("Unexpected code " + response)
                }
            }
            System.out.printf(">>> RESPONSE: %s\n", response)


            var jsonData = userJsonAdapter.fromJson(response_text)

            edit.putString("auth_token", jsonData!!.authentication_token)
            edit.commit()

            val user = LoggedInUser(java.util.UUID.randomUUID().toString(), username)
            return Result.Success(user)
    }

    fun logout() {
        // TODO: revoke authentication
        var preferences = context.getSharedPreferences("insta", Context.MODE_PRIVATE)
        var token = preferences.getString("auth_token","no-token")
        System.out.printf("Token: %s \n", token)
        System.out.printf("Token: %s \n", token)
        var edit = preferences.edit()
        edit.remove("auth_token")
        edit.apply()
        println("\n\nLOGGED OUT\n\n")
    }
}

