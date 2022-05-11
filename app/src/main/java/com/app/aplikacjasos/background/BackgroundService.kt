package com.app.aplikacjasos.background

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
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
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.aplikacjasos.R
import com.app.aplikacjasos.model.Contact
import com.app.aplikacjasos.service.ContactService
import com.app.aplikacjasos.utils.Message
import com.app.aplikacjasos.utils.SMS
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.*
import kotlin.math.sqrt

class BackgroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private lateinit var countDownTimer: CountDownTimer

    private val contactService = ContactService()
    private lateinit var checkedContacts: List<Contact>
    private val current = this
    private var sensorAccFlag = false

    private var startAcc = 0.0

    private var emergencyFlag = false

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        countDownTimer = object : CountDownTimer(5000, 1000) {
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

                checkedContacts = contactService.checkedContacts
                fetchLocationAndSendSMSAndRedirectToMessages()
            }

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

                SMS.sendSMSMultipartForEach(msgBlock)
            }
        }
    }

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

        if (total < 2) {
            emergencyFlag = true
        } else if (total > 2.5 * startAcc) {
            emergencyFlag = true
        }

        if(emergencyFlag) {
            sensorManager.unregisterListener(this)
            countDownTimer.start()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

}