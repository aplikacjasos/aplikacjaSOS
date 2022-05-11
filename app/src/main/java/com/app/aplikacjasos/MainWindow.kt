package com.app.aplikacjasos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.aplikacjasos.service.ContactService
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainWindow : AppCompatActivity() {

    private var contactService: ContactService = ContactService();

    private val contacts = contactService.allContactsInDatabase

    private val multiplePermissionContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
        // permissionStatusMap is of type <String, Boolean>
        // if all permissions accepted
        if (!permissionsStatusMap.containsValue(false)) {
            Toast.makeText(this, "Zatwierdzono uprawnienie", Toast.LENGTH_SHORT).show()

            val listaDevice = contactService.getAllDeviceContacts(this);
            contactService.updateDeviceDatabaseContacts(listaDevice);

        } else {
            Toast.makeText(this, "Nie zaakceptowano wszystkich pozwoleń, aplikacja nie będzie działać prawidłowo", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_window)

        multiplePermissionContract.launch(
            arrayOf(
                android.Manifest.permission.READ_CONTACTS
            )
        )

        if(contactService.countCheckedElements(contacts) > 0) {
            val intent = Intent(this, SOSWindow::class.java)
            startActivity(intent)
            finish()
        }

        if(!isIgnoringBatteryOptimizations()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Informacja")
                .setMessage("Aplikacja nie będzie działać w tle jeśli nie zostanie wyłączona opcja oszczędzania energii." + System.getProperty("line.separator") + "1. Znajdź aplikację SOS" + System.getProperty("line.separator") + "2. Wyłącz oszczędzanie energii." + System.getProperty("line.separator") + "3. Zatwierdź zmiany")
                .setPositiveButton("Zmień ustawienia") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS ))
                }
                .setNegativeButton("Wyjdź") { _, _ ->
                }
                .show()
        }

        val selectNumberBtn = findViewById<Button>(R.id.selectNumberButton)
        selectNumberBtn.setOnClickListener{

            val intent = Intent(this, ContactListView::class.java)
            startActivity(intent)

        }

    }

    override fun onResume() {

        if(contactService.countCheckedElements(contacts) > 0) {
            val intent = Intent(this, SOSWindow::class.java)
            startActivity(intent)
            finish()
        }

        super.onResume()
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm = this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = this.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

}