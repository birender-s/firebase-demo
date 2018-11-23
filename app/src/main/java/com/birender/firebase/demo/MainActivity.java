package com.birender.firebase.demo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.birender.firebase.demo.BuildConfig;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener {
    private final String TAG = "FB_FIRSTLOOK";

    // Firebase Remote Config settings
    private final String CONFIG_PROMO_MESSAGE_KEY = "promo_message";
    private final String CONFIG_PROMO_ENABLED_KEY = "promo_enabled";
    private long PROMO_CACHE_DURATION = 1800;

    // Firebase Analytics settings
    private final int MIN_SESSION_DURATION = 5000;

    private FirebaseAnalytics mFBAnalytics;
    private FirebaseRemoteConfig mFBConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve an instance of the Analytics package
        mFBAnalytics = FirebaseAnalytics.getInstance(this);
        // Get the Remote Config instance
        mFBConfig = FirebaseRemoteConfig.getInstance();

        // Enable developer mode to perform more rapid testing.
        // Config fetches are normally limited to 5 per hour. This
        // enables many more requests to facilitate testing.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFBConfig.setConfigSettings(configSettings);

        // Get the default parameter settings from the XML file
        mFBConfig.setDefaults(R.xml.firstlook_config_params);

        // set up button click handlers
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btnAuthActivity).setOnClickListener(this);
        findViewById(R.id.btnPromo).setOnClickListener(this);

        // Check to see if the promo button should be enabled
        checkPromoEnabled();
    }

    private void checkPromoEnabled() {
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFBConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            PROMO_CACHE_DURATION = 0;
        }
        // fetch the values from the Remote Config service
        mFBConfig.fetch(PROMO_CACHE_DURATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Promo check successful");

                            // If the fetch was successful, then "activate" the
                            // values that were retrieved from the server
                            mFBConfig.activateFetched();
                        }
                        else {
                            Log.e(TAG, "Promo check failed");
                        }

                        showPromoButton();
                    }
                });
    }

    private void showPromoButton() {
        // Determine whether the show the promo button and what
        // the promo message should be to give to the user
        boolean showBtn = false;
        String promoMsg = "";

        showBtn = mFBConfig.getBoolean(CONFIG_PROMO_ENABLED_KEY);
        promoMsg = mFBConfig.getString(CONFIG_PROMO_MESSAGE_KEY);

        Button btn = (Button)findViewById(R.id.btnPromo);
        btn.setVisibility(showBtn ? View.VISIBLE : View.INVISIBLE);
        btn.setText(promoMsg);
    }

    @Override
    public void onClick(View v) {
        // Create the Bundle that will hold the data sent to
        // the Analytics package
        Bundle params = new Bundle();
        params.putInt("ButtonID", v.getId());
        String btnName;

        switch (v.getId()) {
            case R.id.btn1:
                btnName = "Button1Click";
                setStatus("btn1 clicked");
                break;
            case R.id.btn2:
                btnName = "Button2Click";
                setStatus("btn2 clicked");
                break;
            case R.id.btnAuthActivity:
                btnName = "ButtonAuthClick";
                setStatus("btnAuthActivity clicked");
                startActivity(new Intent(this, SignInActivity.class));
                break;
            case R.id.btnPromo:
                btnName = "ButtonPromoClick";
                setStatus("btnPromo clicked");
                startActivity(new Intent(this, PromoScreen.class));
                break;
            default:
                btnName = "OtherButton";
                break;
        }
        Log.d(TAG, "Button click logged: " + btnName);
        mFBAnalytics.logEvent(btnName, params);
    }

    private void setStatus(String text) {
        TextView tvStat = (TextView)findViewById(R.id.tvStatus);
        tvStat.setText(text);
    }
}
