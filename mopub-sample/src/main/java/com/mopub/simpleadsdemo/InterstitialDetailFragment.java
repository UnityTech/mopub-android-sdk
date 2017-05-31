package com.mopub.simpleadsdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mopub.common.MoPubReward;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import static com.mopub.simpleadsdemo.Utils.hideSoftKeyboard;
import static com.mopub.simpleadsdemo.Utils.logToast;

public class InterstitialDetailFragment extends Fragment implements InterstitialAdListener , MoPubRewardedVideoListener{
    private MoPubInterstitial mMoPubInterstitial;
    private static boolean sRewardedVideoInitialized;

    @Nullable private String mAdUnitId;
    @Nullable private Map<String, MoPubReward> mMoPubRewardsMap;
    @Nullable private MoPubReward mSelectedReward;
    @Nullable private Button mShowButtonRewarded;
    private Button mLoadButtonRewarded;
    private Button mShowButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final MoPubSampleAdUnit adConfiguration =
                MoPubSampleAdUnit.fromBundle(getArguments());
        final View view = inflater.inflate(R.layout.interstitial_detail_fragment, container, false);
        final DetailFragmentViewHolder views = DetailFragmentViewHolder.fromView(view);
        hideSoftKeyboard(views.mKeywordsField);

        final String adUnitId = adConfiguration.getAdUnitId();
        views.mDescriptionView.setText(adConfiguration.getDescription());
        views.mAdUnitIdView.setText(adUnitId);
        views.mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowButton.setEnabled(false);
                if (mMoPubInterstitial == null) {
                    mMoPubInterstitial = new MoPubInterstitial(getActivity(), adUnitId);
                    mMoPubInterstitial.setInterstitialAdListener(InterstitialDetailFragment.this);
                }
                final String keywords = views.mKeywordsField.getText().toString();
                mMoPubInterstitial.setKeywords(keywords);
                mMoPubInterstitial.load();
            }
        });
        mShowButton = (Button) view.findViewById(R.id.interstitial_show_button);
        mShowButton.setEnabled(false);
        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMoPubInterstitial.show();
            }
        });
        
        if (!sRewardedVideoInitialized) {
            MoPubRewardedVideos.initializeRewardedVideo(getActivity());
            sRewardedVideoInitialized = true;
        }
        MoPubRewardedVideos.setRewardedVideoListener(this);

        mAdUnitId = "facae35b91a1451c87b2d6dcb9776873";
        mLoadButtonRewarded = (Button) view.findViewById(R.id.load_rewarded);
        mShowButtonRewarded = (Button) view.findViewById(R.id.rewarded_show_button);

        mLoadButtonRewarded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowButtonRewarded.setEnabled(false);
                if (mAdUnitId == null) {
                    return;
                }
                MoPubRewardedVideos.loadRewardedVideo(mAdUnitId,
                        new MoPubRewardedVideoManager.RequestParameters("rewarded", null,
                                "sample_app_customer_id"));
                if (mShowButtonRewarded != null) {
                    mShowButtonRewarded.setEnabled(false);
                }

            }
        });

        mShowButtonRewarded.setEnabled(false);
        mShowButtonRewarded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoPubRewardedVideos.showRewardedVideo(mAdUnitId);
            }
        });



        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
            mMoPubInterstitial = null;
        }
    }

    // InterstitialAdListener implementation
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        mShowButton.setEnabled(true);
        logToast(getActivity(), "Interstitial loaded.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        mShowButton.setEnabled(false);
        final String errorMessage = (errorCode != null) ? errorCode.toString() : "";
        logToast(getActivity(), "Interstitial failed to load: " + errorMessage);
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        mShowButton.setEnabled(false);
        logToast(getActivity(), "Interstitial shown.");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial clicked.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial dismissed.");
    }

    // MoPubRewardedVideoListener implementation
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull final String adUnitId) {
        if (adUnitId.equals(mAdUnitId)) {
            if (mShowButtonRewarded != null) {
                mShowButtonRewarded.setEnabled(true);
            }
            logToast(getActivity(), "Rewarded video loaded.");

            Set<MoPubReward> availableRewards = MoPubRewardedVideos.getAvailableRewards(mAdUnitId);

            // If there are more than one reward available, pop up alert dialog for reward selection
            if (availableRewards.size() > 1) {
                final RewardedVideoDetailFragment.SelectRewardDialogFragment selectRewardDialogFragment
                        = RewardedVideoDetailFragment.SelectRewardDialogFragment.newInstance();

                // The user must select a reward from the dialog
                selectRewardDialogFragment.setCancelable(false);

                // Reset rewards mapping and selected reward
                mMoPubRewardsMap.clear();
                mSelectedReward = null;

                // Initialize mapping between reward string and reward instance
                for (MoPubReward reward : availableRewards) {
                    mMoPubRewardsMap.put(reward.getAmount() + " " + reward.getLabel(), reward);
                }

                selectRewardDialogFragment.loadRewards(mMoPubRewardsMap.keySet()
                        .toArray(new String[mMoPubRewardsMap.size()]));
                selectRewardDialogFragment.setTargetFragment(this, 0);
                selectRewardDialogFragment.show(getActivity().getSupportFragmentManager(),
                        "selectReward");
            }
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (adUnitId.equals(mAdUnitId)) {
            if (mShowButtonRewarded != null) {
                mShowButtonRewarded.setEnabled(false);
            }
            logToast(getActivity(), String.format(Locale.US, "Rewarded video failed to load: %s",
                    errorCode.toString()));
        }
    }

    @Override
    public void onRewardedVideoStarted(@NonNull final String adUnitId) {
        if (adUnitId.equals(mAdUnitId)) {
            logToast(getActivity(), "Rewarded video started.");
            if (mShowButtonRewarded != null) {
                mShowButtonRewarded.setEnabled(false);
            }
        }
    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull final String adUnitId, @NonNull final MoPubErrorCode errorCode) {
        if (adUnitId.equals(mAdUnitId)) {
            logToast(getActivity(), String.format(Locale.US, "Rewarded video playback error: %s",
                    errorCode.toString()));
            if (mShowButtonRewarded != null) {
                mShowButtonRewarded.setEnabled(false);
            }
        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull final String adUnitId) {
        if (adUnitId.equals(mAdUnitId)) {
            logToast(getActivity(), "Rewarded video clicked.");
        }
    }

    @Override
    public void onRewardedVideoClosed(@NonNull final String adUnitId) {
        if (adUnitId.equals(mAdUnitId)) {
            logToast(getActivity(), "Rewarded video closed.");
            if (mShowButtonRewarded != null) {
                mShowButtonRewarded.setEnabled(false);
            }
        }
    }

    @Override
    public void onRewardedVideoCompleted(@NonNull final Set<String> adUnitIds,
                                         @NonNull final MoPubReward reward) {
        if (adUnitIds.contains(mAdUnitId)) {
            logToast(getActivity(),
                    String.format(Locale.US,
                            "Rewarded video completed with reward  \"%d %s\"",
                            reward.getAmount(),
                            reward.getLabel()));
        }
    }

    public void selectReward(@NonNull String selectedReward) {
        mSelectedReward = mMoPubRewardsMap.get(selectedReward);
        MoPubRewardedVideos.selectReward(mAdUnitId, mSelectedReward);
    }

    public static class SelectRewardDialogFragment extends DialogFragment {
        @NonNull private String[] mRewards;
        @NonNull private String mSelectedReward;

        public static RewardedVideoDetailFragment.SelectRewardDialogFragment newInstance() {
            return new RewardedVideoDetailFragment.SelectRewardDialogFragment();
        }

        public void loadRewards(@NonNull String[] rewards) {
            mRewards = rewards;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Select a reward")
                    .setSingleChoiceItems(mRewards, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedReward = mRewards[which];
                        }
                    })
                    .setPositiveButton("Select", null)
                    .create();

            // Overriding onShow() of dialog's OnShowListener() and onClick() of the Select button's
            // OnClickListener() to prevent the dialog from dismissing upon any button click without
            // selecting an item first.
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button selectButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    selectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mSelectedReward != null) {
                                ((RewardedVideoDetailFragment) getTargetFragment())
                                        .selectReward(mSelectedReward);
                                dismiss();
                            }
                        }
                    });
                }
            });

            return dialog;
        }
    }
}
