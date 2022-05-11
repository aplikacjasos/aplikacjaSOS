package com.app.aplikacjasos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.aplikacjasos.adapter.CustomAdapter
import com.app.aplikacjasos.model.Contact
import com.app.aplikacjasos.service.ContactService


class ContactListView : AppCompatActivity() {

    private lateinit var listView: ListView
    private val contactService: ContactService = ContactService()

    private val contacts = contactService.allContactsInDatabase

    private lateinit var saveContacts: MenuItem

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list_view)

        val actionBar = supportActionBar
        actionBar?.title = "Kontakty"
        actionBar?.setDisplayUseLogoEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        listView = findViewById<View>(R.id.contactsList) as ListView

        val adapter = CustomAdapter(contacts, this)

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->

            val contact: Contact = contacts!![position] as Contact
            contact.isChecked = !contact.isChecked
            adapter.notifyDataSetChanged()

            enableDisableSaveIcon(contacts)

        }

    }

    private fun checkIfMoreThanOneIsChecked(contacts: List<Contact>): Boolean {
        contacts.forEach{
            if(it.isChecked) {
                return true
            }
        }
        return false
    }

    private fun enableDisableSaveIcon(contacts: List<Contact>) {
        val oneCheck = checkIfMoreThanOneIsChecked(contacts)
        saveContacts.isEnabled = oneCheck
        saveContacts.isVisible = oneCheck
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.savecontacts, menu)

        saveContacts = menu!!.findItem(R.id.saveContacts)
        enableDisableSaveIcon(contacts)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.saveContacts) {

            contactService.removeEveryhingFromDatabase()
            contactService.saveAllContactsToDatabase(contacts)

            finish()

            Toast.makeText(this, "Zaktualizowano kontakty", Toast.LENGTH_SHORT).show()


        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}