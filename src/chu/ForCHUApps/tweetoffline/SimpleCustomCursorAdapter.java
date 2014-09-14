package chu.ForCHUApps.tweetoffline;

import java.util.HashSet;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

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
        mSelection.delete(position);//remove(position);
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
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
        v.setBackgroundColor(((Activity)context).getResources().getColor(android.R.color.background_light)); //default color
        
        if ( mSelection.get(position) != false) {
            v.setBackgroundColor(((Activity)context).getResources().getColor(R.color.button_clicked));// this is a selected position so make it red
        }
        return v;
		
	}

}
