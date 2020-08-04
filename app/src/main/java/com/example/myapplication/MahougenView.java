package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by pandatom on 2017/5/28.
 * This code is modified from https://github.com/pistatium/mahougen
 *

 */

public class MahougenView extends View {
    /**Variables declaration*/
    private Paint mPaint = null;
    private Bitmap mBitmap = null;
    private Vector center=new Vector(0.0,0.0);
    private int vertexCount=10; //=MP
    ArrayList<Path> pathArray;

    public MahougenView(Context context , AttributeSet attrs)
    {
        super(context,attrs);
        this.setBackgroundColor(Color.BLACK);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(10);
        mPaint.setAntiAlias(true);
        pathArray=new ArrayList<Path>();
        for(int i=0;i<vertexCount;i++) //initialize the path array by the number of vertexCount
        {
            pathArray.add(new Path());
        }

    }

    public void onWindowFocusChanged(boolean hasWindowFocus){
        super.onWindowFocusChanged(hasWindowFocus);
        /**set the center */
        this.center = new Vector((this.getRight() - this.getLeft())/2,(this.getBottom()-this.getTop())/2);


    }

    public boolean onTouchEvent(MotionEvent event)
    {
        /**Calculate the path while touching*/
        Vector current = new Vector(event.getX(),event.getY());
        //System.out.println("current.x="+current.x+"current.y="+current.y);
        //System.out.println("center.x="+center.x+"center.y="+center.y);
        Vector direction = current.minus(center);
        double r = direction.size();
        double alpha =(2.0* Math.PI/this.vertexCount);
        double theta = direction.angle();

        for(int i=0;i<this.vertexCount;i++)
        {
            if(i*alpha>theta){
                theta -= (i-1)*alpha;
                break;
            }
        }

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                int i=0;
                for(Path p : pathArray) {
                    Vector target = this.center.plus(Vector.ofAngle(theta + i * alpha).times(r));
                    if(vertexCount%2==0&&i%2==0)
                        target = this.center.plus(Vector.ofAngle(-theta+(i+1)*alpha).times(r));
                    p.moveTo((float)target.x,(float)target.y);
                    i++;
                }
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:

                i=0;
                for(Path p : pathArray) {
                    Vector target = this.center.plus(Vector.ofAngle(theta + i * alpha).times(r));
                    if(vertexCount%2==0&&i%2==0)
                        target = this.center.plus(Vector.ofAngle(-theta+(i+1)*alpha).times(r));
                    p.lineTo((float)target.x,(float)target.y);
                  //  System.out.println("x="+target.x+",y="+target.y);
                    i++;
                }
                break;

        }
        invalidate();
        return true;
    }

    public void changeMP(int MP)
    {
        /**Changing the MP means change the number of lines.*/
        vertexCount=MP;
        // clean the canvas
        clear();
        // initialize the path array again
        pathArray=new ArrayList<Path>();
        for(int i=0;i<vertexCount;i++)
        {
            pathArray.add(new Path());
        }
    }

    public void onDraw(Canvas canvas)
    {
        // draw the paths
        super.onDraw(canvas);
          for(Path p:pathArray) {
              canvas.drawPath(p, mPaint);
          }
    }

    public void saveBitmap(OutputStream stream)
    {
        /**Trying to save the mahougen in the file stream*/
        try {
            // get the mahougen
            mBitmap=getBitmapFromView(this);
            // put bitmap into file stream
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        catch(Exception e)
        {
            // output the error
            System.out.println("compress error");
            e.printStackTrace();
        }
    }

    public void clear()
    {
        /**clean the paths*/
        for(Path p:this.pathArray)
        {
            p.reset();
        }
        invalidate();

    }

    public void changeBackgroundColor(String color)
    {
        /**Changes the background color*/
        try {
            //First turn the string into id.
            //And than set the background color.
            this.setBackgroundColor(Color.class.getField(color).getInt(null));
        }
        catch(Exception e)
        {
            //output the error
            System.out.println("change background color error.");
            e.printStackTrace();
        }
    }

    public void changeLineColor(String color)
    {
        /**Changes the line color*/
        try {
            //First turn the string into id.
            //And than set the line color.
            mPaint.setColor(Color.class.getField(color).getInt(null));
        }
        catch(Exception e)
        {
            System.out.println("change line color error.");
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        /**Turn the view into bitmap.
         * This code is copy from the below website:
         * https://stackoverflow.com/questions/2801116/converting-a-view-to-bitmap-without-displaying-it-in-android
         */
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
