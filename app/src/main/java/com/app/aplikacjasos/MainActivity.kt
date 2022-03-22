package com.app.aplikacjasos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    val REQUEST_PHONE_NUMBER = 1
    lateinit var contactUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectNumberBtn = findViewById<Button>(R.id.selectNumberBtn)
        selectNumberBtn.setOnClickListener{

            val intent = Intent(Intent.ACTION_PICK)

            intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE

            if(intent.resolveActivity(packageManager) != null){
                startActivityForResult(intent,REQUEST_PHONE_NUMBER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PHONE_NUMBER && resultCode == Activity.RESULT_OK){
            contactUri = data!!.data!!
            getPhoneNumber()
        }
    }

    @SuppressLint("Range")
    private fun getPhoneNumber() {
        val display_phone_number = findViewById<TextView>(R.id.displaySelectedTextView)
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = contentResolver.query(contactUri, projection, null, null, null)
        if(cursor!!.moveToFirst()){
            val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            display_phone_number.text = "Selected phone number: \n $phoneNumber"
        }
    }
}