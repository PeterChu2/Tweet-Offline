package chu.ForCHUApps.tweetoffline.util;

import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

import chu.ForCHUApps.tweetoffline.R;
import chu.ForCHUApps.tweetoffline.sms.SMSHelper;
import chu.ForCHUApps.tweetoffline.ui.ConfirmDialogFragment;

public class TwitterListListener implements MultiChoiceModeListener {
    private FragmentActivity fragmentActivity;
    private SimpleCustomCursorAdapter customAdapter;
    private int nr = 0;

    public TwitterListListener(FragmentActivity fragmentActivity, SimpleCustomCursorAdapter customAdapter) {
        this.fragmentActivity = fragmentActivity;
        new SMSHelper(fragmentActivity);
        this.customAdapter = customAdapter;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ConfirmDialogFragment confirmDialog;
        switch (item.getItemId()) {
            case R.id.remove_entry:
                confirmDialog = ConfirmDialogFragment.newInstance("Remove selected users?", false, 0);
                confirmDialog.show(fragmentActivity.getFragmentManager(), "remove_entries");
                return true;
            case R.id.unfollow_entry:
                confirmDialog = ConfirmDialogFragment.newInstance("Unfollow selected users?", false, 0);
                confirmDialog.show(fragmentActivity.getFragmentManager(), "unfollow_entries");
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = fragmentActivity.getMenuInflater();
        inflater.inflate(R.menu.contextual_action_bar_menu, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        customAdapter.clearSelection();
        resetCount();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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

    private void resetCount() {
        nr = 0;
    }
}
