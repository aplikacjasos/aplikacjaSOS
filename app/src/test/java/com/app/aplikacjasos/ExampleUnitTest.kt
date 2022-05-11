package com.app.aplikacjasos

import org.junit.Test
import com.app.aplikacjasos.utils.Phone
import com.app.aplikacjasos.utils.SMS
import org.junit.Assert.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class PhoneUnitTest {
    @Test
    fun parsing_isCorrect() {
        assertEquals("123-456-789", Phone.parse("123456789"))
    }
}

class SMSUnitTest {
    @Test
    fun toSMS_invalidNumber() {
        assertEquals("Nieprawidłowy numer telefonu", SMS.toSMS("abc def"))
    }

    @Test
    fun toSMS_correctNumber() {
        assertEquals("123456789", SMS.toSMS("123-456-789"))
    }

    @Test
    fun toSMS_isPhoneNumber() {
        assertTrue(SMS.isPhoneNumber("123456789"))
    }

    @Test
    fun toSMS_isNotPhoneNumber() {
        assertFalse(SMS.isPhoneNumber("abcdef"))
    }
}
