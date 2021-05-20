package com.example.chesswithpythonserver2;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * The type Image adapter 2.
 */
public class ImageAdapterPieces extends BaseAdapter {
    private Context mContext;
    /**
     * The Board.
     */
    public String[][] board;
    private Integer[] mThumbIds = new Integer[64];

    /**
     * Instantiates a new Image adapter 2.
     *
     * @param c     the c
     * @param board the board
     */
    public ImageAdapterPieces(Context c, String[][] board) {
        mContext = c;
        this.board = board;

        for(int k = 0;k < 8;k++)
        {
            for (int j = 0; j < 8; j++) {
                Integer x;
                switch (board[k][j]){
                    case "WQ": x = R.drawable.whitequeen;
                        break;
                    case "WK": x = R.drawable.whiteking;
                        break;
                    case "WN": x = R.drawable.whiteknight;
                        break;
                    case "WB": x = R.drawable.whitebishop;
                        break;
                    case "WR": x = R.drawable.whiterook;
                        break;
                    case "WP": x = R.drawable.whitepawn;
                        break;
                    case "BQ": x = R.drawable.blackqueen;
                        break;
                    case "BK": x = R.drawable.blackking;
                        break;
                    case "BN": x = R.drawable.blackknight;
                        break;
                    case "BB": x = R.drawable.blackbishop;
                        break;
                    case "BR": x = R.drawable.blackrook;
                        break;
                    case "BP": x = R.drawable.blackpawn;
                        break;
                    default: x = android.R.color.transparent;
                        break;
                }

                mThumbIds[k * 8 + j] = x;
            }

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
            int a = (Resources.getSystem().getDisplayMetrics().widthPixels)/9;
            int b = (Resources.getSystem().getDisplayMetrics().heightPixels)/15;
            imageView.setLayoutParams(new GridView.LayoutParams(a, b));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }


};
