package com.mopub.mobileads;

import com.unity3d.ads.UnityAds;

/**
 * Created by rossr on 6/12/17.
 */
class UnityAdsException extends RuntimeException {
    private final UnityAds.UnityAdsError errorCode;

    public UnityAdsException(UnityAds.UnityAdsError errorCode, String detailFormat, Object... args) {
        this(errorCode, String.format(detailFormat, args));
    }

    public UnityAdsException(UnityAds.UnityAdsError errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public UnityAds.UnityAdsError getErrorCode() {
        return errorCode;
    }
}
