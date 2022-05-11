package com.app.aplikacjasos.utils

class Phone {
    companion object {
        fun parse(phoneNumber: String) : String {
            val chunked = phoneNumber.chunked(3)
            return chunked.joinToString(separator = "-")
        }
    }
}
