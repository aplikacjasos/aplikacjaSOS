package com.app.aplikacjasos.utils

import com.app.aplikacjasos.model.Contact
import java.io.Serializable

class Message : Serializable {

    private var contacts: List<Contact>

    private var contentDisplay: String
    private var contentMessage: String

    private var lat: Double
    private var lon: Double
    private var streetName: String

    constructor(contacts: List<Contact>, streetName: String, lat: Double, lon: Double) {
        this.contacts = contacts
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

    fun getContacts(): List<Contact> {
        return this.contacts
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

    override fun toString(): String {

        var lista: ArrayList<String> = ArrayList()

        this.contacts.forEach{
            val oneContact = it.contactName + " (" + it.phoneNumber + ")"
            //val oneContact = it.contactName + " (XXX-XXX-XXX)"
            lista.add(oneContact)
        }

        return lista.joinToString(separator = ", ")
    }
}