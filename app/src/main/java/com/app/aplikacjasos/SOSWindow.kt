package com.app.aplikacjasos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.aplikacjasos.utils.Message
import com.app.aplikacjasos.utils.SMS
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.math.sqrt


class SOSWindow : AppCompatActivity(), SensorEventListener {

    lateinit var preferences: SharedPreferences
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var accText: TextView
    private lateinit var sosButton: ImageButton
    private lateinit var countDownTimer2: CountDownTimer

    private var sensorAccFlag = false
    private var startAcc = 0.0
    private var emergencyFlag = false

    private var phoneNumber: String? = ""
    private var fullName: String? = ""

    val FINE_LOCATION_RQ = 101
    val PERMISSION_SEND_SMS = 123;
    var currentLocation: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val multiplePermissionContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
        // permissionStatusMap is of type <String, Boolean>
        // if all permissions accepted
        if (!permissionsStatusMap.containsValue(false)) {
            Toast.makeText(this, "Zatwierdzono wszystkie uprawnienia", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nie zaakceptowano wszystkich pozwoleń, aplikacja nie będzie działać prawidłowo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos_window)

        multiplePermissionContract.launch(
            arrayOf(
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        //checkForPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, "location", FINE_LOCATION_RQ)
        //checkForPermissions(android.Manifest.permission.SEND_SMS, "sendSMS", PERMISSION_SEND_SMS)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        accText = findViewById(R.id.testAcc)

        sosButton = findViewById(R.id.SOSButton)
        preferences = getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)

        phoneNumber = preferences.getString("PhoneNumber", "")
        fullName = preferences.getString("PhoneOwner", "")

        val current = this
        var flag = false

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                flag = false
                Toast.makeText(
                    current,
                    "Wiadomośc zostanie wysłana za " + (millisUntilFinished + 1000) / 1000 + " sekund",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onFinish() {
                flag = true
                fetchLocationAndSendSMSAndRetiretToMessages()
            }
        }

        countDownTimer2 = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                1000,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(1000)
                    }
                }

                Toast.makeText(current, "Wiadomośc zostanie wysłana za " + (millisUntilFinished+1000) / 1000 + " sekund",   Toast.LENGTH_LONG).show()
            }

            override fun onFinish() {
                fetchLocationAndSendSMSAndRetiretToMessages()
            }

        }

            sosButton.setOnTouchListener { _, event ->
                if(!emergencyFlag) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        countDownTimer.start()
                        sosButton.imageAlpha = 200
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        countDownTimer.cancel()
                        if (!flag) {
                            Toast.makeText(
                                current,
                                "Anulowano wysyłanie wiadomości SMS",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        sosButton.imageAlpha = 255
                    }
                } else {
                    if(event.action == MotionEvent.ACTION_DOWN) {
                        sosButton.imageAlpha = 255
                        countDownTimer2.cancel()
                        sensorManager.registerListener(current, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                        emergencyFlag = false
                    }
                }

                true
            }

    }

    private fun getTheAddress(latitude: Double, longitude: Double): String? {
        var retVal = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            retVal = addresses[0].getAddressLine(0)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return retVal
    }

    private fun fetchLocationAndSendSMSAndRetiretToMessages() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_RQ)
            return
        }

        val task = fusedLocationProviderClient!!.lastLocation

        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location

                var address: String? = null;

                try {
                    address = getTheAddress(currentLocation!!.latitude, currentLocation!!.longitude)
                } catch(e: Exception) {
                    address = "Nie znaleziono adresu"
                }

                val msgBlock = Message(fullName!!, phoneNumber!!, address!!, currentLocation!!.latitude, currentLocation!!.longitude)

                val intent = Intent(this, MessagesWindow::class.java)
                intent.putExtra("message_object", msgBlock as Serializable)

                try {
                    SMS.sendSMSMultipart(msgBlock)

                    if(emergencyFlag) {
                        emergencyFlag = false
                        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                    }
                    sosButton.imageAlpha = 255

                    startActivity(intent)
                    /*
                    MaterialAlertDialogBuilder(current)
                        .setMessage("Wysłano wiadomość SMS")
                        .setPositiveButton("OK") { _, _ ->
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
                     */
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage("Nie udało się wysłać wiadomości: " + e.message)
                        .setPositiveButton("OK") { _, _ ->
                        }
                        .setCancelable(false)
                        .show()
                }




            }
        }
    }

    override fun onResume() {
        super.onResume()
        emergencyFlag = false
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editactivity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.editNumber) {
            val intent = Intent(this, EditPhoneWindow::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when {
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED ->{
                    Toast.makeText(applicationContext, "$name permission granted",Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)

            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"$name permission refused",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }
        when(requestCode){
            FINE_LOCATION_RQ -> innerCheck("location")
        }

    }

    private fun showDialog(permission: String,name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to acces your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") {dialog, which ->
                ActivityCompat.requestPermissions(this@SOSWindow, arrayOf(permission),requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
    */


    @SuppressLint("ServiceCast")
    override fun onSensorChanged(p0: SensorEvent?) {
        val x = p0!!.values[0].toDouble()
        val y = p0!!.values[1].toDouble()
        val z = p0!!.values[2].toDouble()

        val total = sqrt(x * x + y * y + z * z)

        if (!sensorAccFlag) {
            startAcc = total
            sensorAccFlag = true
        }

        val measure = "Init: $startAcc - Mes: $total"
        accText.text = measure

        if (total < 2) {
            /*
            MaterialAlertDialogBuilder(this)
                .setMessage("Wykryto spadanie")
                .setPositiveButton("OK") {_, _ ->
                }
                .setCancelable(false)
                .show()
             */
            emergencyFlag = true
        } else if (total > 2.5 * startAcc) {
            /*
            MaterialAlertDialogBuilder(this)
                .setMessage("Wykryto przyspieszenie")
                .setPositiveButton("OK") {_, _ ->
                }
                .setCancelable(false)
                .show()
             */
            emergencyFlag = true
        }

        if (emergencyFlag) {
            sosButton.imageAlpha = 100
            sensorManager.unregisterListener(this)
            countDownTimer2.start()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }
}