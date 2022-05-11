package com.app.aplikacjasos.model;

import com.orm.SugarRecord;

import java.io.Serializable;

public class Contact extends SugarRecord implements Serializable {

    private String contactName;
    private String phoneNumber;
    private boolean checked = false;

    public Contact() {

    }

    public Contact(String contactName, String phoneNumber) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "contactName='" + contactName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", checked=" + checked +
                '}';
    }
}
