package entity;


import rtree.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// CarPoint是Point的子类,继承了Point的所有成员
public class CarPoint extends Point
{
	public String id ;	 //车牌号
	public Date timestamp  ; //时间戳
	private double speed=0;      //速率
	private double angle=0;		 //速度的方向角,弧度制,取值范围[0,2*pi)

	public CarPoint(double longitude,double latitude)
	{
		super(longitude,latitude);
	}
	
	public CarPoint(String id,double longitude,double latitude,Date timestamp,double s,double a)
	{
		super(longitude,latitude);
		this.id=id;
		this.timestamp=timestamp;
		this.speed=s;
		this.angle=a;
	}

	public CarPoint(String id,double longitude,double latitude,Date timestamp)
	{
		this(id,longitude,latitude,timestamp,0,0);
	}

	public CarPoint(String id,double longitude,double latitude,Date timestamp,double rate)
	{

		this(id,longitude,latitude,timestamp,rate,0);
	}

	public double getSpeed()
	{
		return this.speed;
	}
	public double getAngle()
	{
		return this.angle;
	}
	
	public String toString()
	{
		return id+","+String.valueOf(this.longitude)+","+String.valueOf(this.latitude)+","+this.timestamp.toString();
	}
	
	public static void main(String[] args) 
	{

		// TODO Auto-generated method stub
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
		Date t1;
		Date t2;
		try 
		{ 
	          t1 = ft.parse("2017-03-07 06:17:08");
	          t2 = ft.parse("2017-03-07 07:18:47");
	          
	          System.out.println(t1.compareTo(t2));  //return 1 -> t1大于t2,return -1 -> t1小于t2
	    } 
		catch (ParseException e) 
		{ 
	          System.out.println("Unparseable using " + ft); 
	    }
	}
}
