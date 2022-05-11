package com.app.aplikacjasos.service;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import com.app.aplikacjasos.model.Contact;
import com.app.aplikacjasos.utils.SMS;
import java.util.ArrayList;
import java.util.List;

public class ContactService {

    public List<Contact> getAllContactsInDatabase() {
        List<Contact> contacts = Contact.findWithQuery(Contact.class, "SELECT * FROM CONTACT ORDER BY CHECKED DESC, CONTACT_NAME ASC", null);
        return contacts;
    }

    public long countCheckedElements(List<Contact> contacts) {

        String[] whereArgs = {
                "1"
        };

        long count = Contact.count(Contact.class, "CHECKED = ?", whereArgs);

        return count;
    }

    public List<Contact> getCheckedContacts() {
        List<Contact> checkedContacts = Contact.findWithQuery(Contact.class, "SELECT * FROM CONTACT WHERE CHECKED = 1 ORDER BY CONTACT_NAME", null);
        return checkedContacts;
    }

    public List<Contact> getAllDeviceContacts(Context context) {

        if(context == null) return null;

        ContentResolver contentResolver = context.getContentResolver();
        if(contentResolver == null) return null;

        List<Contact> contacts = new ArrayList<Contact>();

        String[] filedProjection = {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };

        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, filedProjection, null, null, sort);

        if(phones != null && phones.getCount() > 0) {
            while(phones.moveToNext()) {

                @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                @SuppressLint("Range") String fullName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                phoneNumber = SMS.Companion.toSMS(phoneNumber);

                if(SMS.Companion.isPhoneNumber(phoneNumber)) {
                    Contact contact = new Contact(fullName, phoneNumber);
                    contacts.add(contact);
                }

            }
        }

        return contacts;
    }

    public void updateDeviceDatabaseContacts(List<Contact> device) {
        for(Contact deviceContact : device) {
            String[] whereArgs = {
                    deviceContact.getContactName(),
                    deviceContact.getPhoneNumber()
            };

            long count = Contact.count(Contact.class, "CONTACT_NAME = ? AND PHONE_NUMBER = ?", whereArgs);

            if(count == 0) {
                deviceContact.save();
            }

        }
    }

    public void removeEveryhingFromDatabase() {
        Contact.deleteAll(Contact.class);
    }

    public void saveAllContactsToDatabase(List<Contact> contactsList) {
        for(Contact contact : contactsList) {
            contact.save();
        }
    }

}
