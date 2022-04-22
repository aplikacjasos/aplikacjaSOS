package com.app.aplikacjasos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.aplikacjasos.utils.SMS

class EditPhoneWindow : AppCompatActivity() {

    private val requestPhoneNumber = 1
    private lateinit var contactUri: Uri
    lateinit var preferences: SharedPreferences
    lateinit var phoneNumberShared: String
    lateinit var fullNameShared: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_phone_window)

        val actionBar = supportActionBar
        actionBar?.title = "Aplikacja SOS"
        actionBar?.setDisplayUseLogoEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val editPhoneButton = findViewById<Button> (R.id.confirmPhoneButton)
        val phoneLabel = findViewById<TextView> (R.id.phoneLabel)

        editPhoneButton.isEnabled = false

        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        val phoneNumber = preferences.getString("PhoneNumber", "")
        val fullName = preferences.getString("PhoneOwner", "")

        val selectNumberBtn = findViewById<Button>(R.id.selectNumberButton)
        selectNumberBtn.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent,requestPhoneNumber)
            }
        }

        val initContact = fullName + System.getProperty("line.separator") + phoneNumber
        phoneLabel.setText(initContact)

        editPhoneButton.setOnClickListener {

            val editor = preferences.edit()
            editor.putString("PhoneNumber", phoneNumberShared)
            editor.putString("PhoneOwner", fullNameShared)
            editor.apply()

            val intent = Intent(this, SOSWindow::class.java)
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
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val confirmPhoneButton = findViewById<Button> (R.id.confirmPhoneButton)

        val cursor = contentResolver.query(contactUri, projection, null, null, null)
        if(cursor!!.moveToFirst()){
            val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val fullName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

            val phoneLabel = findViewById<TextView> (R.id.phoneLabel)
            val selected = getString(R.string.selectedNumber, phoneNumber)

            val parsedPhone = SMS.toSMS(selected)

            if(!SMS.isPhoneNumber(parsedPhone)) {
                phoneLabel.setTextColor(Color.parseColor("#FF0000"))
                phoneLabel.text = parsedPhone
            } else {
                phoneLabel.setTextColor(ContextCompat.getColor(this, androidx.activity.R.color.secondary_text_default_material_light))
                phoneNumberShared = parsedPhone
                fullNameShared = fullName
                val editContact = fullName + System.getProperty("line.separator") + parsedPhone
                phoneLabel.text = editContact
            }

            confirmPhoneButton.isEnabled = parsedPhone.length == 9

        }
        cursor.close()
    }

}