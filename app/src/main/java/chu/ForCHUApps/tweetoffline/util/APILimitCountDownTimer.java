package chu.ForCHUApps.tweetoffline.util;

import android.os.CountDownTimer;

public class APILimitCountDownTimer extends CountDownTimer {
    final private Object mWaitToken;
    private SyncTwitterContacts mSyncTask;

    public APILimitCountDownTimer(long millisInFuture, long countDownInterval,
                                  final Object waitToken, SyncTwitterContacts task) {
        super(millisInFuture, countDownInterval);
        mWaitToken = waitToken;
        mSyncTask = task;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mSyncTask.showProgress("Rate Limit of 180"
                + " has been reached. Your remaining contacts will be fetched in " +
                (millisUntilFinished / 1000) / 60 + " minutes, " +
                (millisUntilFinished / 1000) % 60 + " seconds.\n" +
                "Dismiss to load in background.");
    }

    @Override
    public void onFinish() {
        synchronized (mWaitToken) {
            mSyncTask.showProgress("Fetching User Contacts \n" +
                    "They will be fetched in the background if this dialog is dismissed");
            mWaitToken.notify();
        }
    }
}
