package com.app.aplikacjasos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener


class MainWindow : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_window)

        val confirmPhoneButton = findViewById<Button> (R.id.confirmPhoneButton)
        val inputPhone = findViewById<EditText>(R.id.phoneInput)

        confirmPhoneButton.isEnabled = inputPhone.length() == 9

        inputPhone.addTextChangedListener {
            if (!inputPhone.hasFocus()) {
                return@addTextChangedListener
            }

            confirmPhoneButton.isEnabled = inputPhone.text.length == 9
        }

        //Ustawienie kontaktu
        confirmPhoneButton.setOnClickListener {
            val intent = Intent(this, SOSWindow::class.java)
            startActivity(intent)
            finish()
        }

    }
}