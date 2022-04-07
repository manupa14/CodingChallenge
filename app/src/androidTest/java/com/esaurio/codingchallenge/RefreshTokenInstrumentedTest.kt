package com.esaurio.codingchallenge

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.esaurio.codingchallenge.data.Prefs
import com.esaurio.codingchallenge.data.api.CodingChallengeAPI
import com.esaurio.codingchallenge.data.model.LoginResultTO

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RefreshTokenInstrumentedTest {

    private val latch: CountDownLatch = CountDownLatch(1)

    @Test
    fun refreshToken() {
        CodingChallengeAPI.SHARED_INSTANCE.login("matias@yopmail.com","123456", object : CodingChallengeAPI.DataListener<LoginResultTO>{
            override fun onResponse(data: LoginResultTO) {
                if (data.isResultOK){
                    //invalidamos token
                    Prefs.sharedInstance.saveAuthTokens(
                            Prefs.sharedInstance.authorizationToken?.replace("a","B")?.replace("A","b"),
                            Prefs.sharedInstance.refreshToken
                    )

                    callApiWithExpiredToken()
                }else{
                    fail("Login credentials invalid. ${data.resultMessage ?: ""}")
                }
            }

            override fun onError(code: Int, message: String?) {
                fail("Login Service not reachable. Code: $code. Message: ${message ?: ""}")
            }
        })

        latch.await()

    }

    private fun callApiWithExpiredToken(){
//        CodingChallengeAPI.SHARED_INSTANCE.getPatients("",0,object : CodingChallengeAPI.DataListener<List<Patient>>{
//            override fun onResponse(data: List<Patient>) {
//                latch.countDown()
//            }
//
//            override fun onError(code: Int, message: String?) {
//                if (code == 401)
//                    fail("Token not refreshed")
//                else
//                    fail(" Service not reachable. Code: $code. Message: ${message ?: ""}")
//            }
//        })
    }
}