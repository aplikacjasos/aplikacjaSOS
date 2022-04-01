package com.app.aplikacjasos

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SOSWindow : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos_window)

        val sosButton = findViewById<ImageButton> (R.id.SOSButton)

        val current = this
        var flag = false

        val countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                flag = false
                Toast.makeText(current, "Wiadomośc zostanie wysłana za " + (millisUntilFinished+1000) / 1000 + " sekund",   Toast.LENGTH_LONG).show()
            }

            override fun onFinish() {
                flag = true

                val intent = Intent(current, MessagesWindow::class.java)

                MaterialAlertDialogBuilder(current)
                    .setMessage("Wysłano wiadomość SMS")
                    .setPositiveButton("OK") {_, _ ->
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .show()

            }
        }

        sosButton.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                countDownTimer.start()
                sosButton.imageAlpha = 200
                sendSms()
            } else if (event.action == MotionEvent.ACTION_UP) {
                countDownTimer.cancel()
                if(!flag){
                    Toast.makeText(current, "Odpuszczono przycisk, nie udało się wysłać SMS",   Toast.LENGTH_LONG).show()
                }
                sosButton.imageAlpha = 255
            }
            true
        }


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

    fun sendSms(){
        val bundle = intent.extras
        val value = bundle!!.get("no")
        val numer = "+48" + value.toString()
        val wiadomosc = "Siema, idziemy do biedry?"

        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(numer, null, wiadomosc, null, null)
    }
}