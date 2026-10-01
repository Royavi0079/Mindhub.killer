package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.google.android.gms.ads.RequestConfiguration

/**
 * High-performance AdMob Ads Manager singleton.
 * Pre-loads Interstitial Ads and Rewarded Ads in background for instant playback.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // AdMob App & Unit IDs
    const val APP_ID = "ca-app-pub-2224641890648702~9425334928"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-2224641890648702/5566358264"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2224641890648702/5977044503"

    // Google Official Test IDs for fallback & offline testing
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false
    private var isInitialized = false

    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded: StateFlow<Boolean> = _isRewardedLoaded.asStateFlow()

    private val _isInterstitialLoaded = MutableStateFlow(false)
    val isInterstitialLoaded: StateFlow<Boolean> = _isInterstitialLoaded.asStateFlow()

    val isAdLoaded: StateFlow<Boolean> = _isRewardedLoaded

    /**
     * Initialize Google Mobile Ads SDK safely and preload ads in background.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "Google Mobile Ads SDK initialized: $status")
                preloadRewardedAd(context)
                preloadInterstitialAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during MobileAds.initialize", e)
            isInitialized = true
            preloadRewardedAd(context)
            preloadInterstitialAd(context)
        }
    }

    /**
     * Preload Rewarded Video Ad
     */
    fun preloadRewardedAd(context: Context) {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                    rewardedAd = ad
                    isLoadingRewarded = false
                    _isRewardedLoaded.value = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Primary rewarded ad failed (${loadAdError.message}). Retrying with test ad...")
                    RewardedAd.load(
                        context,
                        TEST_REWARDED_AD_UNIT_ID,
                        adRequest,
                        object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(testAd: RewardedAd) {
                                Log.d(TAG, "Test Rewarded ad loaded fallback.")
                                rewardedAd = testAd
                                isLoadingRewarded = false
                                _isRewardedLoaded.value = true
                            }

                            override fun onAdFailedToLoad(testError: LoadAdError) {
                                Log.w(TAG, "Test rewarded ad fallback failed: ${testError.message}")
                                rewardedAd = null
                                isLoadingRewarded = false
                                _isRewardedLoaded.value = false
                            }
                        }
                    )
                }
            }
        )
    }

    /**
     * Preload Interstitial Ad (Shown per 4 hints or level transitions)
     */
    fun preloadInterstitialAd(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    _isInterstitialLoaded.value = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Primary Interstitial ad failed (${loadAdError.message}). Retrying with test ad...")
                    InterstitialAd.load(
                        context,
                        TEST_INTERSTITIAL_AD_UNIT_ID,
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(testAd: InterstitialAd) {
                                Log.d(TAG, "Test Interstitial ad loaded fallback.")
                                interstitialAd = testAd
                                isLoadingInterstitial = false
                                _isInterstitialLoaded.value = true
                            }

                            override fun onAdFailedToLoad(testError: LoadAdError) {
                                Log.w(TAG, "Test Interstitial ad fallback failed: ${testError.message}")
                                interstitialAd = null
                                isLoadingInterstitial = false
                                _isInterstitialLoaded.value = false
                            }
                        }
                    )
                }
            }
        )
    }

    /**
     * Show Rewarded Ad
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem?) -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        val currentAd = rewardedAd
        if (currentAd == null) {
            preloadRewardedAd(activity)
            onAdFailedToShow("Rewarded ad is loading in background...")
            return
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isRewardedLoaded.value = false
                preloadRewardedAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                _isRewardedLoaded.value = false
                preloadRewardedAd(activity)
                onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed.")
            }
        }

        currentAd.show(activity) { rewardItem ->
            onUserEarnedReward(rewardItem)
        }
    }

    /**
     * Show Interstitial Ad automatically per level transition when user taps Next Level.
     * Guaranteed to never block gameplay if offline or loading.
     */
    fun showNextLevelInterstitial(
        activity: Activity,
        onFinished: () -> Unit
    ) {
        val currentAd = interstitialAd
        if (currentAd == null) {
            preloadInterstitialAd(activity)
            onFinished()
            return
        }

        var hasExecuted = false
        val executeOnce = {
            if (!hasExecuted) {
                hasExecuted = true
                activity.runOnUiThread {
                    onFinished()
                }
            }
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _isInterstitialLoaded.value = false
                preloadInterstitialAd(activity)
                executeOnce()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Level transition interstitial failed: ${adError.message}")
                interstitialAd = null
                _isInterstitialLoaded.value = false
                preloadInterstitialAd(activity)
                executeOnce()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Next level interstitial ad presented.")
            }
        }

        try {
            currentAd.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing interstitial ad", e)
            executeOnce()
        }
    }

    /**
     * Show Interstitial Ad (e.g. for every 4 hints used to grant +4 more hints)
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdCompleted: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        val currentAd = interstitialAd
        if (currentAd == null) {
            preloadInterstitialAd(activity)
            onAdFailedToShow("Interstitial ad is loading...")
            return
        }

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _isInterstitialLoaded.value = false
                preloadInterstitialAd(activity)
                onAdCompleted()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                _isInterstitialLoaded.value = false
                preloadInterstitialAd(activity)
                onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed.")
            }
        }

        currentAd.show(activity)
    }
}
