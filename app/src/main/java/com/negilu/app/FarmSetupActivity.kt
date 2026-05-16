package com.negilu.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FarmSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var farmerNameInput: EditText
    private lateinit var villageInput: EditText
    private lateinit var surveyNumberInput: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farm_setup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        farmerNameInput = findViewById(R.id.farmerNameInput)
        villageInput = findViewById(R.id.villageInput)
        surveyNumberInput = findViewById(R.id.surveyNumberInput)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            saveFarmProfile()
        }
    }

    private fun saveFarmProfile() {
        val farmerName = farmerNameInput.text.toString().trim()
        val village = villageInput.text.toString().trim()
        val surveyNumber = surveyNumberInput.text.toString().trim()

        if (farmerName.isEmpty()) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (village.isEmpty()) {
            Toast.makeText(this, "Enter your village name", Toast.LENGTH_SHORT).show()
            return
        }

        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        val userId = auth.currentUser?.uid ?: return

        val farmData = hashMapOf(
            "farmerName" to farmerName,
            "village" to village,
            "surveyNumber" to surveyNumber,
            "phone" to (auth.currentUser?.phoneNumber ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("farmers")
            .document(userId)
            .set(farmData)
            .addOnSuccessListener {
                Toast.makeText(this, "Farm profile saved!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
                saveButton.text = "Save & Continue"
            }
    }
}