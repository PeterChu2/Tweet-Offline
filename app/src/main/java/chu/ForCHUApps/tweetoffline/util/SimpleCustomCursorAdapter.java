package chu.ForCHUApps.tweetoffline.util;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.HashSet;

import chu.ForCHUApps.tweetoffline.R;

public class SimpleCustomCursorAdapter extends SimpleCursorAdapter{
	
    private SparseBooleanArray mSelection = new SparseBooleanArray();
    private Context context;
    private HashSet<Long> selectedUserIDs = new HashSet<Long>();
    
    public void setNewSelection(int position, long id, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
        selectedUserIDs.add(id);
    }
    
    public HashSet<Long> getSelectedUserIDs()
    {
    	return selectedUserIDs;
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public void removeSelection(int position, long id) {
        mSelection.delete(position);
        notifyDataSetChanged();
        selectedUserIDs.remove(id);
    }

    public void clearSelection() {
        mSelection.clear();
        notifyDataSetChanged();
    }
    
    public void clearSelectionIDs() {
    	selectedUserIDs.clear();
    }

	public SimpleCustomCursorAdapter(Context context, int layout, Cursor c,
		String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
	}
	
	@Override
	public void bindView (View view, Context context, Cursor cursor)
	{
		String picURL = cursor.getString(cursor.getColumnIndex("pic"));
		ImageView icon = (ImageView) view.findViewById(R.id.twitterSmallIcon);
		Ion.with(icon)
		.error(R.drawable.tweet_offline_logo)
		.load(picURL);
		TextView username = (TextView) view.findViewById(R.id.usernameTextView);
		username.setText(cursor.getString(cursor.getColumnIndex("username")));
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
        if ( mSelection.get(position) != false) {
        	// selected via. long click
            TextView username = (TextView) v.findViewById(R.id.usernameTextView);
    		username.setBackgroundColor(context.getResources().getColor(R.color.button_clicked));
    		v.setBackgroundColor(context.getResources().getColor(R.color.button_clicked));
        }
        else
        {
        	// unselected
        	TextView username = (TextView) v.findViewById(R.id.usernameTextView);
    		username.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
        	v.setBackgroundColor(context.getResources().getColor(android.R.color.background_light)); //default color
        }
        return v;
	}
}
