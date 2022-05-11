package com.app.aplikacjasos

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.aplikacjasos.model.Contact
import com.app.aplikacjasos.service.ContactService
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

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var accText: TextView
    private lateinit var sosButton: ImageButton
    private lateinit var countDownTimer2: CountDownTimer

    private var contactService: ContactService = ContactService()

    private lateinit var checkedContacts: List<Contact>

    private var sensorAccFlag = false
    private var startAcc = 0.0
    private var emergencyFlag = false

    private val FINE_LOCATION_RQ = 101
    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val current = this

    private val multiplePermissionContract = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
        // permissionStatusMap is of type <String, Boolean>
        // if all permissions accepted
        if (!permissionsStatusMap.containsValue(false)) {
            Toast.makeText(this, "Zatwierdzono wszystkie uprawnienia", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nie zaakceptowano wszystkich pozwoleń, aplikacja nie będzie działać prawidłowo", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos_window)

        multiplePermissionContract.launch(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SOSapp", "SOSapp", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        accText = findViewById(R.id.testAcc)
        sosButton = findViewById(R.id.SOSButton)

        var flag = false

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

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

                checkedContacts = contactService.checkedContacts
                fetchLocationAndSendSMSAndRedirectToMessages()
            }
        }

        countDownTimer2 = object : CountDownTimer(10000, 1000) {
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
                val builder = NotificationCompat.Builder(current, "SOSapp")
                    .setSmallIcon(R.drawable.img_sercokoperta)
                    .setContentTitle("Wysłano alert SOS")
                    .setContentText("Alert SOS został wysłany do wybranych osób.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                val uniqueId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                with(NotificationManagerCompat.from(current)) {
                    notify(uniqueId, builder.build())
                }

                emergencyFlag = false
                sensorManager.registerListener(current, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

                checkedContacts = contactService.checkedContacts
                fetchLocationAndSendSMSAndRedirectToMessages()
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

    private fun fetchLocationAndSendSMSAndRedirectToMessages() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_RQ)
            return
        }

        val task = fusedLocationProviderClient!!.lastLocation

        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location

                var address: String? = null

                try {
                    address = getTheAddress(currentLocation!!.latitude, currentLocation!!.longitude)
                } catch(e: Exception) {
                    address = "Nie znaleziono adresu"
                }

                val msgBlock = Message(checkedContacts, address!!, currentLocation!!.latitude, currentLocation!!.longitude)

                val intent = Intent(this, MessagesWindow::class.java)
                intent.putExtra("message_object", msgBlock as Serializable)

                try {

                    SMS.sendSMSMultipartForEach(msgBlock)

                    if(emergencyFlag) {
                        emergencyFlag = false
                        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                    }
                    sosButton.imageAlpha = 255

                    startActivity(intent)

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
        //emergencyFlag = false
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        //sensorManager.unregisterListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editactivity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.editNumber) {
            val intent = Intent(this, ContactListView::class.java)
            startActivity(intent)
        } /*else if(item.itemId == R.id.backgroundProcess) {
            val intent = Intent(this, BackgroundService::class.java)
            startService(intent)
            finish()
        }*/
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ServiceCast")
    override fun onSensorChanged(p0: SensorEvent?) {
        val x = p0!!.values[0].toDouble()
        val y = p0.values[1].toDouble()
        val z = p0.values[2].toDouble()

        val total = sqrt(x * x + y * y + z * z)

        if (!sensorAccFlag) {
            startAcc = total
            sensorAccFlag = true
        }

        val measure = "Init: $startAcc - Mes: $total"
        accText.text = measure

        if (total < 2) {
            emergencyFlag = true
        } else if (total > 2.5 * startAcc) {
            emergencyFlag = true
        }

        if (emergencyFlag) {
            sosButton.imageAlpha = 100
            sensorManager.unregisterListener(this)

            //to fg
            bringActivityToForeground()

            countDownTimer2.start()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    private fun bringActivityToForeground() {
        val intent = Intent(this, javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        applicationContext.startActivity(intent)
    }

}