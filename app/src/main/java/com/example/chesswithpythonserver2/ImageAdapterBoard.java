package com.example.chesswithpythonserver2;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * The type Image adapter.
 */
public class ImageAdapterBoard extends BaseAdapter {
    private Context mContext;
    private Integer[] mThumbIds = {

            // notice also:
            // dark squares positions show 7, 5, 3, 1a, c, e, g
            // white squares positions show 8, 6, 4, 2, b, d, f, h


            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.light_square, R.drawable.dark_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square,
            R.drawable.dark_square, R.drawable.light_square
    };

    /**
     * Instantiates a new Image adapter.
     *
     * @param c the c
     */
    public ImageAdapterBoard(Context c) {
        mContext = c;
    }

    /**
     * Instantiates a new Image adapter.
     *
     * @param c   the c
     * @param arr the arr
     */
    public ImageAdapterBoard(Context c, int[] arr) {
        mContext = c;
        for(int i = 0;i < 64;i++)
        {
            if(arr[i]==1)
                mThumbIds[i] = R.drawable.available_moves_gray;
            if(arr[i]==2)
                mThumbIds[i] = R.drawable.move_from_to;

        }
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            int a = Resources.getSystem().getDisplayMetrics().widthPixels/9;
            int b = Resources.getSystem().getDisplayMetrics().heightPixels/15;
            imageView.setLayoutParams(new GridView.LayoutParams(a, b));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
   // public void setView(int pos)
   // {
   //    this.mThumbIds[pos] = R.drawable.
   // }
    // references to our images

}