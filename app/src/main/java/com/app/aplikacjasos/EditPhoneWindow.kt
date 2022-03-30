package com.app.aplikacjasos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener


class EditPhoneWindow : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_phone_window)

        val editPhoneButton = findViewById<Button> (R.id.confirmPhoneButton)
        val inputPhone = findViewById<EditText>(R.id.phoneInput)

        editPhoneButton.isEnabled = inputPhone.length() == 9

        inputPhone.addTextChangedListener {
            if (!inputPhone.hasFocus()) {
                return@addTextChangedListener
            }

            editPhoneButton.isEnabled = inputPhone.text.length == 9
        }

        editPhoneButton.setOnClickListener {
            val intent = Intent(this, SOSWindow::class.java)
            startActivity(intent)
            finish()
        }

    }

}