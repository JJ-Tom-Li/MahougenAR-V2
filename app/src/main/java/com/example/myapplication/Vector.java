package com.example.myapplication;

/**
 * Created by pandatom on 2017/6/4.
 * This code is modified from https://github.com/pistatium/mahougen
 */

public class Vector{

    double x,y;
    public Vector(double x,double y){
        this.x=x;
        this.y=y;
    }

    public Vector plus(Vector v)
    {
        return new Vector(this.x+v.x,this.y+v.y);
    }

    public Vector minus(Vector v)
    {
        return new Vector(this.x-v.x,this.y-v.y);
    }

    public Vector times(double r)
    {
        return new Vector(this.x*r,this.y*r);
    }

    public double size()
    {
        return Math.sqrt((this.x*this.x+this.y*this.y));
    }

    public double angle()
    {
        return Math.atan2(this.x,this.y);
    }

    public static Vector ofAngle(double theta)
    {
        return new Vector(Math.cos(theta), Math.sin(theta));
    }
}
