package stream.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.moos.library.HorizontalProgressView;

import java.util.Date;

public class RoomActivity extends AppCompatActivity {

    FrameLayout mRoomLayout;
    HorizontalProgressView mHeroHealthBar;
    HorizontalProgressView mEnemyHealthBar;
    ImageView mEnemyImage;
    ImageView mHeroImage;
    TextView mHeroHealthText;
    TextView mHeroAmmoText;

    int heroMoney = 0;
    int heroHealth = 0;
    int heroAmmo = 0;
    int enemyHealth = 100;

    int gameHandlerTick = 1000;
    int exitTime = 0;

    InterstitialAd mInterstitialAd;
    RewardedVideoAd mRewardedVideoAd;

    AppLovinAd loadedAd;
    AdColonyInterstitial mAdColonyInterstitial;

    boolean adColonyLoaded = false;

    Context mContext;
    SharedPreferences sharedPreferences;
    Handler mGameHandler;

    final String HERO_MONEY = "HERO_MONEY";
    final String HERO_HEALTH = "HERO_HEALTH";
    final String HERO_AMMO = "HERO_AMMO";
    final String ENEMY_HEALTH = "ENEMY_HEALTH";

    final String EXIT_TIME = "EXIT_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        mContext = getApplication().getApplicationContext();
        sharedPreferences = getSharedPreferences("Account", Context.MODE_PRIVATE);

        heroMoney = sharedPreferences.getInt(HERO_MONEY, 0);
        heroHealth = sharedPreferences.getInt(HERO_HEALTH, 0);
        heroAmmo = sharedPreferences.getInt(HERO_AMMO, 0);
        enemyHealth = sharedPreferences.getInt(ENEMY_HEALTH, 0);
        exitTime = sharedPreferences.getInt(EXIT_TIME, 0);

        CalculateTimeElapsed();

        mRoomLayout = findViewById(R.id.room_layout);
        mHeroHealthBar = findViewById(R.id.hero_health);
        mEnemyHealthBar = findViewById(R.id.enemy_health);
        mEnemyImage = findViewById(R.id.enemy_image);
        mHeroImage = findViewById(R.id.hero_image);
        mHeroHealthText = findViewById(R.id.health_text);
        mHeroAmmoText = findViewById(R.id.ammo_text);

        //Use this to load image into the ImageViews.
//        Glide.with(mContext).asDrawable().load(getDrawable(R.drawable.bg_rectangle_green_solid)).into(mEnemyImage);

        //Use this to set Progress Bar.
        mHeroHealthBar.setEndProgress(80);
        mHeroHealthBar.startProgressAnimation();

        UpdateUI();

        mRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heroAmmo += 1;
                mHeroAmmoText.setText(String.format("Ammo: %d", heroAmmo));
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
            }
        });
        loadGoogleRewardedVideo();

        //AdColony Initialization
        AdColony.configure(this, getString(R.string.adcolony_app_id), getString(R.string.adcolony_zone_id));

        //Ad loading handler
        Handler adHandler = new Handler();
        Runnable r1 = new Runnable() {
            public void run() {
                loadAdColonyVideo();
                loadAppLovinVideo();
            }
        };
        adHandler.postDelayed(r1, 100);

        mGameHandler = new Handler();
        adHandler.postDelayed(new Runnable(){
            public void run(){
                //Do something
                if (heroAmmo > 0)
                {
                    heroAmmo -= 1;
                }
                UpdateUI();

                //Redo this method
                mGameHandler.postDelayed(this, gameHandlerTick);
            }
        }, gameHandlerTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Date currentDate = new Date();
        exitTime = (int) (currentDate.getTime() / 1000);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HERO_MONEY, heroMoney);
        editor.putInt(HERO_HEALTH, heroHealth);
        editor.putInt(HERO_AMMO, heroAmmo);
        editor.putInt(ENEMY_HEALTH, enemyHealth);
        editor.putInt(EXIT_TIME, exitTime);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CalculateTimeElapsed();
        UpdateUI();
    }

    public void UpdateUI()
    {
        //Set player text values.
        mHeroAmmoText.setText(String.format("Ammo: %d", heroAmmo));
        mHeroHealthText.setText(String.format("Health: %d", heroHealth));
    }

    public void CalculateTimeElapsed()
    {
        Date currentDate = new Date();
        int currentTime = (int) (currentDate.getTime() / 1000);
        if (exitTime == 0)
        {
            exitTime = currentTime;
        }

        int timeElapsed = currentTime - exitTime;

        //Calculate changed stats
        heroAmmo -= timeElapsed;
        //Before setting ammo to 0, the amount of ammo spent can be used to calculate damage and enemies killed.
        if (heroAmmo < 0)
        {
            heroAmmo = 0;
        }
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
