package com.example.ephraimkunz.plumbingscheduler;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.URI;

public class MainActivity extends AppCompatActivity {
    public final static String PREFERENCES_FILE = "PREFERENCES_FILE";
    public final static String NUMBER_PREF = "NUMBER_PREF";
    public final static String TAG= "Main_Activity";

    private Button createEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] NECESSARY_PERMISSIONS = new String[] {Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS };
        for (String permission : NECESSARY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    permission) != PackageManager.PERMISSION_GRANTED) {

                //ask for permission
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        NECESSARY_PERMISSIONS, 123);
            }
        }

        createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if currently in a call.
                SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
                String number = prefs.getString(NUMBER_PREF, "");
                Log.e(MainActivity.TAG, "numberForPref: " + number);

                if (number == null || number.isEmpty()) {
                    Log.e(MainActivity.TAG, "getting recents");
                    number = getMostRecentInboundNumberFromCallHistory(getApplicationContext());
                }

                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setData(CalendarContract.Events.CONTENT_URI);

                if (number != null && !number.isEmpty()) {
                    calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "Phone number: " + number);

                    Contact contact = getContactMatchingNumber(getApplicationContext(), number);
                    if (contact.name != null) {
                        calIntent.putExtra(CalendarContract.Events.TITLE, contact.name);
                    }

                    if (contact.address != null) {
                        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, contact.address);
                    }
                }

                startActivity(calIntent);
            }
        });
    }

    private static Contact getContactMatchingNumber(Context context, String phoneNumber) {
        String[] projection = new String[] {
                ContactsContract.PhoneLookup.CONTACT_ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME};
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor lookupCursor = context.getContentResolver().query(uri, projection, null,null, null);

        int nameIndex = lookupCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
        int contactIdIndex = lookupCursor.getColumnIndex(ContactsContract.PhoneLookup.CONTACT_ID);
        Contact contact = new Contact();
        while (lookupCursor.moveToNext()) {
            String name = lookupCursor.getString(nameIndex);
            String contactId = lookupCursor.getString(contactIdIndex);
            if (!name.isEmpty()) {
                contact.name = name;

                // Get address for this contact if there is one.
                Uri postal_uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
                Cursor postal_cursor  = context.getContentResolver().query(postal_uri,null,ContactsContract.Data.CONTACT_ID + "="+contactId,null,null);
                while(postal_cursor.moveToNext()) {
                    String formatted = postal_cursor.getString(postal_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                    if (!formatted.isEmpty()) {
                        contact.address = formatted;
                    }
                }
                postal_cursor.close();
            }
        }
        lookupCursor.close();

        return contact;
    }

    private static String getMostRecentInboundNumberFromCallHistory(Context context) {
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        String phNumber = null;
        while (cursor.moveToNext()) {
            String callType = cursor.getString(type);
            int dircode = Integer.parseInt(callType);
            if (dircode != CallLog.Calls.INCOMING_TYPE) {
                continue;
            }

            phNumber = cursor.getString(number);
            break;
        }
        cursor.close();
        return phNumber;
    }
}
