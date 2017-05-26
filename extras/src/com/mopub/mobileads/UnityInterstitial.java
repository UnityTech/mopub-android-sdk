package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;

import com.mopub.common.logging.MoPubLog;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.UnityAds;

import java.util.Map;

public class UnityInterstitial extends CustomEventInterstitial implements IUnityAdsExtendedListener {

    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private Context mContext;
    private String mPlacementId = "video";

    @Override
    protected void loadInterstitial(Context context,
                                    CustomEventInterstitialListener customEventInterstitialListener,
                                    Map<String, Object> localExtras,
                                    Map<String, String> serverExtras) {

        mPlacementId = UnityRouter.placementIdForServerExtras(serverExtras, mPlacementId);
        mCustomEventInterstitialListener = customEventInterstitialListener;
        mContext = context;

        if (!UnityAds.isInitialized()) {
            if (context == null || !(context instanceof Activity)) {
                mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
                return;
            }

            if (!UnityRouter.initUnityAds(serverExtras, (Activity) mContext, new Runnable() {
                public void run() {
                    mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                }
            })) {
                return;
            }

            UnityRouter.addListener(mPlacementId, this);

            UnityRouter.initPlacement(mPlacementId, new Runnable() {
                public void run() {
                    mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_INVALID_STATE);
                }
            }, new Runnable() {
                public void run() {
                    mCustomEventInterstitialListener.onInterstitialLoaded();
                }
            });
        } else {
            UnityRouter.addListener(mPlacementId, this);

            if (UnityAds.isReady(mPlacementId)) {
                mCustomEventInterstitialListener.onInterstitialLoaded();
            }
        }
    }

    @Override
    protected void showInterstitial() {
        if (UnityAds.isReady(mPlacementId) && mContext != null) {
            UnityAds.show((Activity) mContext, mPlacementId);
        } else {
            MoPubLog.d("Attempted to show Unity interstitial video before it was available.");
        }
    }

    @Override
    protected void onInvalidate() {
        UnityRouter.removeListener(mPlacementId);
    }

    @Override
    public void onUnityAdsReady(String placementId) {
        if (placementId.equals(mPlacementId)) {
            mCustomEventInterstitialListener.onInterstitialLoaded();
        }
    }

    @Override
    public void onUnityAdsStart(String placementId) {
        mCustomEventInterstitialListener.onInterstitialShown();
    }

    @Override
    public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
        if (finishState == UnityAds.FinishState.ERROR) {
            MoPubLog.d("Unity interstitial video encountered a playback error for placement " + placementId);
            mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        } else {
            MoPubLog.d("Unity interstitial video completed for placement " + placementId);
            mCustomEventInterstitialListener.onInterstitialDismissed();
        }

        UnityRouter.removeListener(placementId);
    }

    @Override
    public void onUnityAdsClick(String placementId) {
        mCustomEventInterstitialListener.onInterstitialClicked();
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String message) {
        MoPubLog.d("Unity interstitial video cache failed for placement " + mPlacementId + ".");
        MoPubErrorCode errorCode = UnityRouter.getMoPubErrorCode(unityAdsError);
        mCustomEventInterstitialListener.onInterstitialFailed(errorCode);
    }
}
