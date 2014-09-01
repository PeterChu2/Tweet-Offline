package chu.ForCHUApps.tweetoffline;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

public class SimpleCustomCursorAdapter extends SimpleCursorAdapter{

	public SimpleCustomCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		// TODO Auto-generated constructor stub
	}

}
