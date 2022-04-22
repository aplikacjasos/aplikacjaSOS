package com.app.aplikacjasos.utils

import java.io.Serializable

class Message : Serializable {
    private var phoneNumber: String = ""
    private var fullName: String = ""
    private var contentDisplay: String = ""
    private var contentMessage: String = ""
    private var streetName: String = ""
    private var lat: Double = 0.0;
    private var lon: Double = 0.0;

    constructor(fullName: String, number: String, streetName: String, lat: Double, lon: Double) {
        this.fullName = fullName
        this.phoneNumber = number
        this.streetName = streetName
        this.lat = lat
        this.lon = lon

        this.contentDisplay = "Przydarzył się niespodziewany wypadek, proszę o natychmiastową pomoc:" +
                System.getProperty("line.separator") +
                System.getProperty("line.separator") +
                "Znajduję się przy: $streetName" +
                System.getProperty("line.separator") +
                "Współrzędne:" +
                System.getProperty("line.separator") +
                "Lat: $lat" +
                System.getProperty("line.separator") +
                "Lon: $lon" +
                System.getProperty("line.separator") +
                System.getProperty("line.separator") +
                "Dokładne miejsce zdarzenia: " +
                System.getProperty("line.separator") +
                "https://www.google.com/maps/search/?api=1&query=$lat,$lon"

        this.contentMessage = "Przydarzył się niespodziewany wypadek, proszę o natychmiastową pomoc:" +
                "\n\n" +
                "Znajduję się przy: $streetName" +
                "\n" +
                "Współrzędne:" +
                "\n" +
                "Lat: $lat" +
                "\n" +
                "Lon: $lon" +
                "\n\n" +
                "Dokładne miejsce zdarzenia: " +
                "\n" +
                "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    }

    fun getFullName(): String {
        return this.fullName
    }

    fun getPhoneNumber(): String {
        return this.phoneNumber
    }

    fun getMessageContentDisplay(): String {
        return this.contentDisplay
    }

    fun getMessageContentMessage(): String {
        return this.contentMessage
    }

    fun getStreetName(): String {
        return this.streetName
    }

    fun getLatitude(): Double {
        return this.lat
    }

    fun getLongitude(): Double {
        return this.lon;
    }
}