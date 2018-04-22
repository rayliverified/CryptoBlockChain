package stream.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.applovin.sdk.AppLovinSdk;
import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;

public class MainActivity extends AppCompatActivity {

    TextView mBtn1;
    TextView mBtn2;
    TextView mBtn3;
    TextView mBtn4;
    TextView mBtn5;
    TextView mBtn6;

    TextView moneydisplay;
    int heroMoney = 0;
    int heroHealth = 0;
    int enemyHealth = 100;

    InterstitialAd mInterstitialAd;
    RewardedVideoAd mRewardedVideoAd;

    AppLovinAd loadedAd;
    AdColonyInterstitial mAdColonyInterstitial;

    boolean adColonyLoaded = false;

    Context mContext;
    SharedPreferences sharedPreferences;

    final String HERO_HEALTH = "HERO_HEALTH";
    final String HERO_MONEY = "HERO_MONEY";
    final String ENEMY_HEALTH = "ENEMY_HEALTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplication().getApplicationContext();
        sharedPreferences = getSharedPreferences("Account", Context.MODE_PRIVATE);

        heroHealth = sharedPreferences.getInt(HERO_HEALTH, 0);
        heroMoney = sharedPreferences.getInt(HERO_MONEY, 0);
        enemyHealth = sharedPreferences.getInt(ENEMY_HEALTH, 0);
        Log.d("Main", "SharedPrefs Restore");

        moneydisplay=findViewById(R.id.moneydisplay);
        moneydisplay.setText("$:" + Integer.toString(heroMoney));

        mBtn1 = findViewById(R.id.btn_1);
        mBtn2 = findViewById(R.id.btn_2);
        mBtn3 = findViewById(R.id.btn_3);
        mBtn4 = findViewById(R.id.btn_4);
        mBtn5 = findViewById(R.id.btn_5);
        mBtn6 = findViewById(R.id.btn_6);

        //Show Google Interstitials
        mBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });
        //Show Google Rewarded Video
        mBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                }
            }
        });
        mBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLovinInterstitialAdDialog interstitialAd = AppLovinInterstitialAd.create( AppLovinSdk.getInstance(mContext), mContext);

                // Optional: Assign listeners
                interstitialAd.setAdDisplayListener(new AppLovinAdDisplayListener() {
                    @Override
                    public void adDisplayed(AppLovinAd appLovinAd) {

                        heroMoney += 1;
                        moneydisplay.setText("$:" + Integer.toString(heroMoney));
                    }

                    @Override
                    public void adHidden(AppLovinAd appLovinAd) {
                        loadAppLovinVideo();
                    }
                });
                interstitialAd.setAdClickListener(new AppLovinAdClickListener() {
                    @Override
                    public void adClicked(AppLovinAd appLovinAd) {

                    }
                });
                interstitialAd.setAdVideoPlaybackListener(new AppLovinAdVideoPlaybackListener() {
                    @Override
                    public void videoPlaybackBegan(AppLovinAd appLovinAd) {

                    }

                    @Override
                    public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {

                    }
                });

                interstitialAd.showAndRender(loadedAd);
            }
        });
        mBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adColonyLoaded)
                {
                    mAdColonyInterstitial.show();

                    heroMoney += 1;
                    moneydisplay.setText("$:" + Integer.toString(heroMoney));
                }
                else
                {
                    loadAdColonyVideo();
                }
            }
        });
        mBtn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heroMoney += 1;
                moneydisplay.setText("$:" + Integer.toString(heroMoney));
            }
        });

        //Google Interstitials Initialization
        MobileAds.initialize(mContext, "ca-app-pub-3940256099942544~3347511713");
        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        //Google Rewarded Video Initialization
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewarded(RewardItem reward) {
                Toast.makeText(mContext, "onRewarded! currency: " + reward.getType() + "  amount: " + reward.getAmount(), Toast.LENGTH_SHORT).show();
                // Reward the user.
                heroMoney+=10;
                moneydisplay.setText("$:" + Integer.toString(heroMoney));
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Toast.makeText(mContext, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Toast.makeText(mContext, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
                // Load the next rewarded video ad.
                loadGoogleRewardedVideo();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                Toast.makeText(mContext, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdLoaded() {
                Toast.makeText(mContext, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Toast.makeText(mContext, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoStarted() {
                Toast.makeText(mContext, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoCompleted() {
                Toast.makeText(mContext, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
                heroMoney+=1;
                moneydisplay.setText("$:" + Integer.toString(heroMoney));
            }
        });
        loadGoogleRewardedVideo();

        //AdColony Initialization
        AdColony.configure(this, getString(R.string.adcolony_app_id), getString(R.string.adcolony_zone_id));

        Handler handler = new Handler();
        Runnable r1 = new Runnable() {
            public void run() {
                loadAdColonyVideo();
                loadAppLovinVideo();
            }
        };
        handler.postDelayed(r1, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Main", "Pause");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HERO_HEALTH, heroHealth);
        editor.putInt(HERO_MONEY, heroMoney);
        editor.putInt(ENEMY_HEALTH, enemyHealth);
        editor.apply();
    }

    public void loadGoogleRewardedVideo() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    public void loadAdColonyVideo()
    {
        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                //Store and use this ad object to show your ad when appropriate
                mAdColonyInterstitial = ad;
                adColonyLoaded = true;
            }
        };
        AdColony.requestInterstitial(getString(R.string.adcolony_zone_id), listener);
    }

    public void loadAppLovinVideo()
    {
        AppLovinSdk.initializeSdk(mContext);
        AppLovinSdk.getInstance(mContext).getAdService().loadNextAd( AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener()
        {
            @Override
            public void adReceived(AppLovinAd ad)
            {
                loadedAd = ad;
            }

            @Override
            public void failedToReceiveAd(int errorCode)
            {
                // Look at AppLovinErrorCodes.java for list of error codes.
            }
        } );
    }
}
