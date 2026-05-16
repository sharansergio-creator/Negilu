package com.negilu.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String = ""

    private lateinit var phoneInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var otpLayout: LinearLayout
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // If already logged in, go to main screen
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        phoneInput = findViewById(R.id.phoneInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        verifyOtpButton = findViewById(R.id.verifyOtpButton)
        otpLayout = findViewById(R.id.otpLayout)
        statusText = findViewById(R.id.statusText)

        sendOtpButton.setOnClickListener {
            val phone = "+91" + phoneInput.text.toString().trim()
            if (phone.length != 13) {
                Toast.makeText(this, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendOtp(phone)
        }

        verifyOtpButton.setOnClickListener {
            val otp = otpInput.text.toString().trim()
            if (otp.length != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(otp)
        }
    }

    private fun sendOtp(phone: String) {
        statusText.text = "Sending OTP..."
        sendOtpButton.isEnabled = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    statusText.text = "Failed: ${e.message}"
                    sendOtpButton.isEnabled = true
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    statusText.text = "OTP sent"
                    otpLayout.visibility = View.VISIBLE
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    statusText.text = "Wrong OTP. Try again."
                }
            }
    }

    private fun goToMain() {
        val userId = auth.currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("farmers")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, FarmSetupActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, FarmSetupActivity::class.java))
                finish()
            }
    }
}