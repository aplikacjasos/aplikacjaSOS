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
import android.widget.TextView

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
        val displayPhoneNumber = findViewById<TextView>(R.id.displaySelectedTextView)
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