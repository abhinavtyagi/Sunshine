package aktyagi.com.sunshine;

/**
 * Created by abhinavt on 11/15/2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import aktyagi.com.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private boolean mUseTodayLayout;
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mUseTodayLayout = false;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    private static int VIEW_TYPE_TODAY      = 0;
    private static int VIEW_TYPE_FUTURE_DAY = 1;

    @Override
    public int getItemViewType(int position) {
        return (position==0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
            Remember that these views are reused as needed.
         */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layout_id;
        layout_id = (viewType==VIEW_TYPE_TODAY)? R.layout.list_item_forecast_firstelem :
                                                 R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layout_id, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        int rowIdx = cursor.getPosition();
        TextView textView = null;

        boolean isMetric = Utility.isMetric(context);
        String hi = Utility.formatTemperature(context, cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP), isMetric);
        String lo = Utility.formatTemperature(context, cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP), isMetric);
        String desc = cursor.getString(MainActivityFragment.COL_WEATHER_DESC);
        String date = rowIdx==0? "Today" : Utility.getFriendlyDayString(context, cursor.getLong(MainActivityFragment.COL_WEATHER_DATE));

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if(viewHolder!=null) {
            viewHolder.highTempView.setText(hi);
            viewHolder.lowTempView.setText(lo);
            viewHolder.descriptionView.setText(desc);
            viewHolder.dateView.setText(date);
            boolean bUseArtWork = rowIdx==0 && mUseTodayLayout;
            viewHolder.iconView.setImageResource(Utility.getResourceIdByDesc(desc, bUseArtWork));
        }
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }

    public class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            this.iconView = (ImageView) view.findViewById(R.id.id_list_item_icon);
            this.dateView = (TextView) view.findViewById(R.id.id_list_item_date_textView);
            this.descriptionView = (TextView) view.findViewById(R.id.id_list_item_forecast_textView);
            this.highTempView = (TextView) view.findViewById(R.id.id_list_item_high_textView);
            this.lowTempView = (TextView) view.findViewById(R.id.id_list_item_low_textView);
        }
    }
}