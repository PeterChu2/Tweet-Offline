package chu.ForCHUApps.tweetoffline;

import java.util.HashMap;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

public class TwitterListListener implements MultiChoiceModeListener{

	private FragmentActivity fragmentActivity;
	private ConfirmDialogFragment confirmDialog;
	private SimpleCustomCursorAdapter customAdapter;

	private int nr = 0;
	
	public TwitterListListener(String DATABASE_NAME, FragmentActivity fragmentActivity, SimpleCustomCursorAdapter customAdapter)
	{
		this.fragmentActivity = fragmentActivity;
		new SMSHelper(fragmentActivity);
		this.customAdapter = customAdapter;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.remove_entry:
			confirmDialog = ConfirmDialogFragment.newInstance("Remove selected users?", false, 0);
			confirmDialog.show(fragmentActivity.getFragmentManager(), "remove_entries");
			mode.finish();
			return true;
		case R.id.follow_entry:
			mode.finish();
			return true;
		case R.id.unfollow_entry:
			confirmDialog = ConfirmDialogFragment.newInstance("Unfollow selected users?", false, 0);
			confirmDialog.show(fragmentActivity.getFragmentManager(), "unfollow_entries");
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
		customAdapter.clearSelection();
		resetCount();
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
			boolean checked) {
        if (checked) {
            nr++;
            customAdapter.setNewSelection(position, id, checked);                    
        } else {
            nr--;
            customAdapter.removeSelection(position, id);
        }
        actionMode.setTitle(nr + " selected");

	}
	
	private void resetCount()
	{
		nr = 0;
	}
}
