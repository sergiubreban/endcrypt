package com.brb.breban.endcript.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brb.breban.endcript.R;

import java.util.ArrayList;

/**
 * Created by breban on 29.04.2017.
 */

public class EndCriptListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> itemname;
    private Paint paint = new Paint();

    public EndCriptListAdapter(Activity context, ArrayList<String> itemname) {
        super(context, R.layout.contact_list, itemname);
        this.context=context;
        this.itemname=itemname;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.contact_list, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        txtTitle.setText(itemname.get(position));

        Bitmap b=Bitmap.createBitmap(imageView.getLayoutParams().width, imageView.getLayoutParams().height, Bitmap.Config.ARGB_8888);

        String[] name = itemname.get(position).split(" ");
        String initials = "";
        for(int i=0;i<name.length;i++){
            initials += String.valueOf(name[i].charAt(0));
        }
        Canvas c = new Canvas(b);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(imageView.getLayoutParams().width/2,imageView.getLayoutParams().height/2, imageView.getLayoutParams().width/2, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(50);

        c.drawText(initials,20,60,paint);
        imageView.setImageBitmap(b);

        return rowView;
    };
}
