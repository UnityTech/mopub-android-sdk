package com.mopub.mobileads;

import com.unity3d.ads.UnityAds;

class UnityAdsUtils {
    static MoPubErrorCode getMoPubErrorCode(UnityAds.UnityAdsError unityAdsError) {
        MoPubErrorCode errorCode;
        switch (unityAdsError) {
            case VIDEO_PLAYER_ERROR:
                errorCode = MoPubErrorCode.VIDEO_PLAYBACK_ERROR;
                break;
            case INVALID_ARGUMENT:
                errorCode = MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
                break;
            case INTERNAL_ERROR:
                errorCode = MoPubErrorCode.NETWORK_INVALID_STATE;
                break;
            default:
                errorCode = MoPubErrorCode.UNSPECIFIED;
                break;
        }
        return errorCode;
    }
}
