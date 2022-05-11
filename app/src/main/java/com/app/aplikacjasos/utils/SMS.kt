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

        fun sendSMSMultipartForEach(message: Message){
            var checkedContacts = message.getContacts()

            checkedContacts.forEach{

                val number = "+48" + it.phoneNumber

                val smsManager: SmsManager = SmsManager.getDefault()
                val parts: ArrayList<String> = smsManager.divideMessage(message.getMessageContentMessage())

                smsManager.sendMultipartTextMessage(number, null, parts, null, null)

            }



        }

        fun sendSMSForEach(message: Message){
            var checkedContacts = message.getContacts()

            checkedContacts.forEach{
                val number = "+48" + it.phoneNumber

                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(number, null, message.getMessageContentMessage(), null, null)
            }

        }

        fun isPhoneNumber(number: String?): Boolean {
            return number?.length == 9
        }
    }
}