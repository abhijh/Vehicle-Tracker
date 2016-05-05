package com.thesecurenode.abhinav.devicetracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static ChatManager chatManager;
    private static String location;
    private String lastUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Allowed");
        } else {
            Log.d("Permission", "Not Allowed");
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            5);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location lastKnownLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (lastKnownLocation == null || l.getAccuracy() < lastKnownLocation.getAccuracy()) {
                lastKnownLocation = l;
            }
        }
        Toast.makeText(MainActivity.this, String.valueOf(lastKnownLocation), Toast.LENGTH_SHORT).show();
        location = lastKnownLocation.getLatitude()+", "+lastKnownLocation.getLongitude();
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(final Location location) {
                Chat chat = chatManager.createChat(lastUser);
                MainActivity.location = location.getLatitude()+", "+location.getLongitude();
                try {
                    chat.sendMessage(MainActivity.location);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }



    @Override
    public void onStart() {
        super.onStart();
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                .setServiceName("fstine.com")
                .setHost("fstine.com")
                .setPort(5222)
                .setResource("tracker")
                .build();

        final AbstractXMPPConnection conn = new XMPPTCPConnection(config);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String username = "device";
                    String password = "toor";
                    conn.connect();
                    conn.login(username, password);
                    Presence setStatus = new Presence(Presence.Type.available);
                    conn.sendStanza(setStatus);
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        chatManager = ChatManager.getInstanceFor(conn);
        chatManager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        if (!createdLocally)
                            chat.addMessageListener(new ChatMessageListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void processMessage(Chat chat, Message message) {
                                    if (message.getBody().equals("get")) {
                                        lastUser = message.getFrom();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "Sending location", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        try {
                                            chat.sendMessage(location);
                                        } catch (SmackException.NotConnectedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                    }
                });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
