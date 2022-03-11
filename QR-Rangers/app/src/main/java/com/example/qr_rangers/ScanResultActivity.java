package com.example.qr_rangers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;

public class ScanResultActivity extends AppCompatActivity {

    private TextView totalScore;
    private SwitchMaterial attachLocation;
    //private ImageView imgPreview;
    Bitmap photo = null;
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
        TextView scoreTextView = findViewById(R.id.textViewScore);
        scoreTextView.setText(String.valueOf(score).concat(" pts."));
        totalScore = findViewById(R.id.newtotalscore);
        totalScore.setText(String.valueOf(intent.getStringExtra("totalScore")));
        TextView codeUserCount = findViewById(R.id.codeusercount);
        //TODO: Set codeuser count from database
        //codeUserCount.setText();
        EditText commentBox = findViewById(R.id.commentbox);
        //imgPreview = findViewById(R.id.imageView);
        gpsTracker = new GpsTracker(ScanResultActivity.this);
        if(!gpsTracker.canGetLocation())
            gpsTracker.showSettingsAlert();
        ImageButton cameraButton = findViewById(R.id.cameraButton);
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
        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachLocation.isChecked()){
                    if(gpsTracker.canGetLocation()){
                        double longitude = gpsTracker.getLongitude();
                        double latitude = gpsTracker.getLatitude();
                        gpsTracker.stopUsingGPS();
                        location = new Location(longitude,latitude);
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
                    /*
                    if (imageEncoded != null)
                            imgPreview.setImageBitmap(decodeFromFirebaseBase64(imageEncoded));
                */
                }
                catch (Exception e) {
                    System.out.println(e.toString());
                    Toast.makeText(ScanResultActivity.this,"exception encountered".concat(e.toString()),Toast.LENGTH_SHORT).show();
                }
             finish();
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
        User localUser = Database.Users.getById(id, new User("","",""));
        return localUser;
    }

    public static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    /*Attempts to add qr code to db if user didn't click add data */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        user.AddQR(qr);

    }
}