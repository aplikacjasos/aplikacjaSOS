@file:Suppress("DEPRECATION")

package com.app.aplikacjasos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener

class MainActivity : AppCompatActivity() {

    private val requestPhoneNumber = 1
    private lateinit var contactUri: Uri

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectNumberBtn = findViewById<Button>(R.id.selectNumberBtn)
        selectNumberBtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent,requestPhoneNumber)
            }
        }

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
            intent.putExtra("no",inputPhone.text.toString());
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == requestPhoneNumber && resultCode == Activity.RESULT_OK){
            contactUri = data!!.data!!
            getPhoneNumber()
        }
    }

    @SuppressLint("Range")
    private fun getPhoneNumber() {
        val displayPhoneNumber = findViewById<TextView>(R.id.phoneInput)
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = contentResolver.query(contactUri, projection, null, null, null)
        if(cursor!!.moveToFirst()){
            val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val selected = getString(R.string.selectedNumber, phoneNumber)
            displayPhoneNumber.text = selected
        }
        cursor.close()
    }
}