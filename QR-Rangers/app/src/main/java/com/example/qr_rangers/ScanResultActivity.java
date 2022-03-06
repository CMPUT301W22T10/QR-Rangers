package com.example.qr_rangers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScanResultActivity extends AppCompatActivity {

    private TextView scoreTextView;
    private EditText commentBox;
    private TextView totalScore;
    private TextView codeUserCount;
    private ImageButton cameraButton;
    private SwitchMaterial attachLocation;
    private ImageView imgPreview;
    Bitmap photo = null;
    private Button saveButton;
    private static final int pic_id = 123;
    private Location location = null;
    private GpsTracker gpsTracker;
    private User user;
    private QRCode qr = new QRCode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        Intent intent = getIntent();
        String content = intent.getStringExtra("content");
        qr = new QRCode(content,null,null);
        user = loadUser();
        int score = qr.getScore();
        scoreTextView = findViewById(R.id.textViewScore);
        scoreTextView.setText(String.valueOf(score).concat(" pts."));
        totalScore = findViewById(R.id.newtotalscore);
        totalScore.setText(String.valueOf(intent.getStringExtra("totalScore")));
        codeUserCount = findViewById(R.id.codeusercount);
        //TODO: Set codeuser count from database
        //codeUserCount.setText();
        commentBox = findViewById(R.id.commentbox);
        commentBox.setShowSoftInputOnFocus(true);
        imgPreview = findViewById(R.id.imageView);
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
        attachLocation = findViewById(R.id.locationswitch);
        saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachLocation.isChecked()){
                    gpsTracker = new GpsTracker(ScanResultActivity.this);
                    if(gpsTracker.canGetLocation()){
                        double longitude = gpsTracker.getLongitude();
                        double latitude = gpsTracker.getLatitude();
                        gpsTracker.stopUsingGPS();
                        location = new Location(longitude,latitude);
                    }
                    else{
                        gpsTracker.showSettingsAlert();
                    }
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String imageEncoded = null;
                if (photo != null) {
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                }
                QRCode QrToSave = new QRCode(content,imageEncoded,location);

                try {
                    user.AddQR(QrToSave);
                    Database.Users.update(user);
                    if (imageEncoded != null)
                            imgPreview.setImageBitmap(decodeFromFirebaseBase64(imageEncoded));
                    Toast.makeText(ScanResultActivity.this, String.valueOf(user.getQRNum()).concat(" qrs scanned"), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    System.out.println(e.toString());
                    Toast.makeText(ScanResultActivity.this,"exception encountered".concat(e.toString()),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == Activity.RESULT_OK) {
             photo = (Bitmap) data.getExtras().get("data");
        }
    }
    //Imported it from HomeActivity for now, should refactor
    private User loadUser() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String id = sharedPreferences.getString("ID", null);
        User localUser = Database.Users.getById(id, new User(""));
        return localUser;
    }

    public static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /*Attempts to add qr code to db if user didn't provide extra info*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        user.AddQR(qr);

    }
}