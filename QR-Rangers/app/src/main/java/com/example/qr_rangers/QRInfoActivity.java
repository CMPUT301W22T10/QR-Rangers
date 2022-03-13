package com.example.qr_rangers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to that showcases information about a User's scanned QR Code
 * @author Ronan Sandoval
 * @version 1.0
 */
public class QRInfoActivity extends AppCompatActivity {

    QRCode qr;
    User user;

    TextView scannerText;
    TextView scoreText;
    TextView commentText;
    ImageView image;

    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_info);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        qr = (QRCode) getIntent().getSerializableExtra("qr");
        user = (User) getIntent().getSerializableExtra("user");

        scannerText = findViewById(R.id.qr_info_scanner);
        scannerText.setText(user.getUsername());
        scoreText = findViewById(R.id.qr_info_points);
        String scoreString = qr.getScore() + " pts.";
        scoreText.setText(scoreString);
        //TODO: Get comments
        commentText = findViewById(R.id.qr_info_comment);

        image = findViewById(R.id.qr_info_image);
        if (qr.getPhoto() != null) {
            byte[] imageBits = Base64.decode(qr.getPhoto(), Base64.DEFAULT);
            Bitmap bitImage = BitmapFactory.decodeByteArray(imageBits, 0, imageBits.length);
            image.setImageBitmap(bitImage);
        } else {
            image.setImageResource(R.drawable.ic_launcher_background);
        }

        deleteButton = findViewById(R.id.qr_info_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Note", qr == null ? "null" : "notnull");
                user.DeleteQR(qr);
                Database.Users.update(user);
                Intent resultUser = new Intent();
                resultUser.putExtra("user", user);
                QRInfoActivity.this.setResult(Activity.RESULT_OK, resultUser);
                QRInfoActivity.this.finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
