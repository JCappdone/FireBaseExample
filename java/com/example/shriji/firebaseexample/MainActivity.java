package com.example.shriji.firebaseexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FireBase";
    private static final String SHOW_CONFING_ENABLE_KEY = "promo_enabled";
    private static final String SHOW_CONFING_MESSAGE_KEY = "promo_message";

    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.btnFirst)
    Button btnFirst;
    @BindView(R.id.btnSecond)
    Button btnSecond;
    @BindView(R.id.btnSLogin)
    Button btnSLogin;
    @BindView(R.id.btnSale)
    Button btnSale;

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final long Minimum_session_Duration = 5000;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private long PROMO_CATCH_DURATION = 1800 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        //wait 5 second before counting this as a session
        mFirebaseAnalytics.setMinimumSessionDuration(Minimum_session_Duration);



        //enable developer mode for perform more rapid testing.
        //Config fetches are normally limited 5 per hour.
        //this enable many more requests to facilitate testing.
        FirebaseRemoteConfigSettings configsetting = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        mFirebaseRemoteConfig.setConfigSettings(configsetting);

        mFirebaseRemoteConfig.setDefaults(R.xml.firstlook_config_params);

        //to check if promo button enable or not.
        checkPromoEnable();

    }

    private void checkPromoEnable() {

        //if developer mode catch = 0 means each fetch retrieve value from server.
        //set the catch duration for developer testing
        if(mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            PROMO_CATCH_DURATION = 0;
        }

        //fetch value from remote config setting
        mFirebaseRemoteConfig.fetch(PROMO_CATCH_DURATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: promo check was successful");
                            mFirebaseRemoteConfig.activateFetched();
                        }else {
                            Log.e(TAG, "onComplete: promo checked failed");
                        }

                        showPromoButton();
                    }
                });
    }

    private void showPromoButton() {
        //get promo  setting from remote config
        boolean showBtn = false;
        String promoMsg = "";

        showBtn = mFirebaseRemoteConfig.getBoolean(SHOW_CONFING_ENABLE_KEY);
        promoMsg = mFirebaseRemoteConfig.getString(SHOW_CONFING_MESSAGE_KEY);

        btnSale.setVisibility(showBtn ?  View.VISIBLE : View.INVISIBLE);
        btnSale.setText(promoMsg);
    }



    @OnClick({R.id.btnFirst, R.id.btnSecond, R.id.btnSLogin, R.id.btnSale})
    public void onViewClicked(View view) {

        Bundle params = new Bundle();
        params.putInt("ButttonID", view.getId());
        String btnName;

        switch (view.getId()) {
            case R.id.btnFirst:
                btnName = "Button1Click";
                setStatus("btn1 Clicked");
                break;

            case R.id.btnSecond:
                btnName = "Button2Clicked";
                setStatus("btn2 Clicked");
                break;

            case R.id.btnSLogin:
                btnName = "ButtonAuthClick";
                setStatus("btnAuthactivity clicked");
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
                break;

            case R.id.btnSale:
                btnName = "ButtonPromoclicked";
                setStatus("btnPromoClicked");
                startActivity(new Intent(this, SaleActivity.class));
                break;

            default:
                btnName = "OtherButton";
                break;
        }
        Log.d(TAG, "onViewClicked: " + btnName);
        mFirebaseAnalytics.logEvent(btnName, params);
    }

    private void setStatus(String s) {
        status.setText(s);
    }
}
