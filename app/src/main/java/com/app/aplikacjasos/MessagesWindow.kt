package com.app.aplikacjasos

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.aplikacjasos.utils.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

//, OnMapReadyCallback
class MessagesWindow : AppCompatActivity(), OnMapReadyCallback {

    var mMediaPlayer: MediaPlayer? = null
    private lateinit var stopAlarm: Button
    private lateinit var timeNow: LocalDateTime
    private lateinit var time: TextView
    private lateinit var date: TextView
    var am: AudioManager? = null

    //do mapki
    var currentMarker: Marker? = null

    private lateinit var mMap: GoogleMap

    lateinit var msgBlock: Message

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages_window)

        msgBlock = intent.extras!!.get("message_object") as Message

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        //Audio ustaw na maxa
        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am!!.setStreamVolume(AudioManager.STREAM_MUSIC, am!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

        timeNow = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val formatted = timeNow.format(formatter).split(" ")

        stopAlarm = findViewById(R.id.stopAlarm)

        time = findViewById(R.id.timeText)
        time.text = formatted[1]
        date = findViewById(R.id.dateText)
        date.text = formatted[0]

        val actionBar = supportActionBar
        actionBar?.title = "Aplikacja SOS"
        actionBar?.setDisplayUseLogoEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val receiver = findViewById<TextView> (R.id.sentTo)
        val content = findViewById<TextView> (R.id.messageContent)

        val receiverMSG = "Wysłano SMS do: $msgBlock"
        receiver.text = receiverMSG
        content.text = msgBlock.getMessageContentDisplay()

        playSound()

        stopAlarm.setOnClickListener {
            stopAlarm.isEnabled = false
            stopSound()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        stopSound()
        finish()
        return true
    }

    fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.alarm)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latLng = LatLng(msgBlock.getLatitude(), msgBlock.getLongitude())
        moveMarket(latLng)

        googleMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {

                if (currentMarker != null) {
                    currentMarker?.remove()
                }
                val newlatLng = LatLng(marker.position.latitude, marker.position.longitude)
                moveMarket(newlatLng)
            }

            override fun onMarkerDrag(marker: Marker) {}
        })
    }


    private fun moveMarket(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng).title("Miejsce Wypadku")
            .snippet(msgBlock.getStreetName()).draggable(true)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        currentMarker = mMap.addMarker(markerOptions)
        currentMarker?.showInfoWindow()
    }

}