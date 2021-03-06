package com.example.qr_rangers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;

/**
 * Activity that is shown after a valid scan
 * Enables the user to add location info and a photo to the QR Code before adding it
 */
public class ScanResultActivity extends AppCompatActivity {

    private static final int pic_id = 123;

    private TextView totalScore;
    private ImageButton cameraButton;
    private SwitchMaterial attachLocation;

    Bitmap photo = null;
    private Location location = null;
    private GpsTracker gpsTracker;
    private User user;
    private QRCode qr;
    private ScannedCode code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        attachLocation = findViewById(R.id.locationswitch);
        gpsTracker = new GpsTracker(ScanResultActivity.this);
        if(!gpsTracker.canGetLocation())
            gpsTracker.showSettingsAlert();
        Location loc = null;
        if (attachLocation.isChecked()) {
            if (gpsTracker.canGetLocation()) {
                double longitude = gpsTracker.getLongitude();
                double latitude = gpsTracker.getLatitude();
                gpsTracker.stopUsingGPS();
                loc = new Location(longitude, latitude);
            }
        }
        QRCode dbqr = new QRCode(content, loc,true);
        qr = Database.QrCodes.getByName(dbqr.getCodeInfo());
        if (qr == null) {
            qr = Database.QrCodes.add(dbqr);
        }
        user = loadUser();
        code = new ScannedCode(qr, user, loc, null, null);
        if (user.HasQR(code)){
            Toast.makeText(getBaseContext(), "You already scanned this one!", Toast.LENGTH_SHORT).show();
            finish();
        }

        int score = qr.getScore();
        TextView scoreTextView = findViewById(R.id.textViewScore);
        scoreTextView.setText(String.valueOf(score).concat(" pts."));
        totalScore = findViewById(R.id.newtotalscore);
        totalScore.setText(String.valueOf(intent.getStringExtra("totalScore")));
        TextView codeUserCount = findViewById(R.id.codeusercount);
        codeUserCount.setText("" + qr.getScannedCount());
        EditText commentBox = findViewById(R.id.commentbox);


        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent camera_intent
                        = new Intent(MediaStore
                        .ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, pic_id);
            }
        });


        //Saving the QR code to server happens here
        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Disable the camera button when saving qr code to db
                cameraButton.setClickable(false);
                //process location data if provided by the user
                if (attachLocation.isChecked()){
                    if(gpsTracker.canGetLocation()){
                        double longitude = gpsTracker.getLongitude();
                        double latitude = gpsTracker.getLatitude();
                        gpsTracker.stopUsingGPS();
                        location = new Location(longitude,latitude);
                    }
                }
                //Process image to JPEG if provided by user
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String imageEncoded = null;
                if (photo != null) {
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                }
                //generate code for saving to db from collected data
                ScannedCode codeToSave = new ScannedCode(qr,user,location,commentBox.getText().toString(),imageEncoded);

                try {
                    boolean qrChanged = false;
                    if (qr.getLocation() == null && location != null) {
                        qr.setLocation(location);
                        qrChanged = true;
                    }
                    if (qr.getPhoto() == null && photo != null) {
                        qr.setPhoto(imageEncoded);
                        qrChanged = true;
                    }
                    if (qrChanged) {
                        Database.QrCodes.update(qr);
                    }
                    user.AddQR(codeToSave);
                    Database.Users.update(user);
                }
                catch (Exception e) {
                    System.out.println(e.toString());
                    Toast.makeText(ScanResultActivity.this,"exception encountered".concat(e.toString()),Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

    }

    /**
     * gets the image from the camera API
     *
     * @param requestCode
     *      An integer representing the image itself
     * @param resultCode
     *      An integer identifying if the function return properly
     * @param data
     *      The intent from which the data is coming
     */
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == Activity.RESULT_OK) {
             photo = (Bitmap) data.getExtras().get("data");
             cameraButton.setImageBitmap(photo);
        }
    }

    /**
     * Fetches user from database
     * @return user from db
     */
    private User loadUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String id = sharedPreferences.getString("ID", null);
        User localUser = Database.Users.getById(id);
        return localUser;
    }
}