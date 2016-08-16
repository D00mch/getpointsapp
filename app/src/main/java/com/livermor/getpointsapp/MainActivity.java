package com.livermor.getpointsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int IMAGE = R.drawable.wardrobe_1_2; // set image here


    private ImageView mImageView;
    private RelativeLayout mRelativeLayoutContainer;

    Scene mScene;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        populateMainImage();
        RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.mRelativeLayout);
        mScene = new Scene(getApplicationContext(), mRelativeLayout);
    }

    private void populateMainImage() {
        ImageView mImageView = (ImageView) findViewById(R.id.mImageView);
        Glide.with(this)
                .load(IMAGE)
                .into(mImageView);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScene.printCoords();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mScene.changeSniper(this);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

}
