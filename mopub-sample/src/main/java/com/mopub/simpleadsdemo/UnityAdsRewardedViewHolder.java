package com.mopub.simpleadsdemo;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class UnityAdsRewardedViewHolder {
    @NonNull Button mInitializeButton;
    @NonNull Button mPlayButton;
    @NonNull EditText mGameIdField;
    @NonNull EditText mPlacementIdField;

    public UnityAdsRewardedViewHolder(@NonNull Button initializeButton, @NonNull Button playButton, @NonNull EditText gameIdField, @NonNull EditText placementIdField) {
        mInitializeButton = initializeButton;
        mPlayButton = playButton;
        mGameIdField = gameIdField;
        mPlacementIdField = placementIdField;
    }

    static UnityAdsRewardedViewHolder fromView(View view) {
        final Button initializeButton = view.findViewById(R.id.init_ads);
        final Button playButton = view.findViewById(R.id.play_rewarded);
        final EditText gameIdField = view.findViewById(R.id.gameId);
        final EditText placementIdField = view.findViewById(R.id.rewarded_placement_id_text);
        return new UnityAdsRewardedViewHolder(initializeButton, playButton, gameIdField, placementIdField);
    }
}
