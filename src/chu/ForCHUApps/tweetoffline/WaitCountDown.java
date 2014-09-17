package chu.ForCHUApps.tweetoffline;

import android.os.CountDownTimer;

public class WaitCountDown extends CountDownTimer{

	Object waitToken;
	public WaitCountDown(long millisInFuture, long countDownInterval, Object waitToken) {
		super(millisInFuture, countDownInterval);
		this.waitToken = waitToken;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		
	}

}
