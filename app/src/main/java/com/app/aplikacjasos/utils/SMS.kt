package com.app.aplikacjasos.utils

import android.telephony.SmsManager

class SMS {
    companion object {
        fun toSMS(contact: String?): String {
            var parsed = contact?.replace(" ", "")?.replace("[^0-9]".toRegex(), "")
            if(parsed!!.length < 9) {
                return "Nieprawidłowy numer telefonu"
            }
            return parsed.reversed().substring(0, 9).reversed()
        }

        fun sendSMSMultipart(message: Message){
            val number = "+48" + message.getPhoneNumber()

            val smsManager: SmsManager = SmsManager.getDefault()
            val parts: ArrayList<String> = smsManager.divideMessage(message.getMessageContentMessage())

            smsManager.sendMultipartTextMessage(number, null, parts, null, null)
        }

        fun sendSMS(message: Message){
            val number = "+48" + message.getPhoneNumber()

            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message.getMessageContentMessage(), null, null)
        }

        fun isPhoneNumber(number: String?): Boolean {
            return number?.length == 9
        }
    }
}