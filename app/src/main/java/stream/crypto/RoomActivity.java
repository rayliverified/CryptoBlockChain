package stream.crypto;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
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

public class RoomActivity extends AppCompatActivity {

    HorizontalProgressView mHealthBar;
    ImageView mEnemyImage;
    ImageView mHeroImage;

    InterstitialAd mInterstitialAd;
    RewardedVideoAd mRewardedVideoAd;

    AppLovinAd loadedAd;

    AdColonyInterstitial mAdColonyInterstitial;

    boolean adColonyLoaded = false;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        mContext = getApplication().getApplicationContext();

        mHealthBar = findViewById(R.id.health_bar);
        mEnemyImage = findViewById(R.id.enemy_image);
        mHeroImage = findViewById(R.id.hero_image);

        //Use this to load image into the ImageViews.
        Glide.with(mContext).asDrawable().load(getDrawable(R.drawable.bg_rectangle_green_solid)).into(mEnemyImage);

        //Use this to set Progress Bar.
        mHealthBar.setEndProgress(80);
        mHealthBar.startProgressAnimation();

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

        Handler handler = new Handler();
        Runnable r1 = new Runnable() {
            public void run() {
                loadAdColonyVideo();
                loadAppLovinVideo();
            }
        };
        handler.postDelayed(r1, 100);
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
