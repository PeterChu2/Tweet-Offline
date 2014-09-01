package chu.ForCHUApps.tweetoffline;

import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

public class TwitterListListener implements MultiChoiceModeListener {

	private String DATABASE_NAME;
	private FragmentActivity fragmentActivity;

	public TwitterListListener(String DATABASE_NAME, FragmentActivity fragmentActivity)
	{
		this.DATABASE_NAME = DATABASE_NAME;
		this.fragmentActivity = fragmentActivity;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.remove_entry:
			mode.finish();
			return true;
		case R.id.follow_entry:
			mode.finish();
			return true;
		case R.id.unfollow_entry:
			mode.finish();
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = fragmentActivity.getMenuInflater();
		inflater.inflate(R.menu.contextual_action_bar_menu, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
			boolean checked) {
		// TODO Auto-generated method stub

	}

}
