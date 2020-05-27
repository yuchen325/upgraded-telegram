package entity;

import java.io.Serializable;

/**
 * 用来描述GPS点
 */
public class Point implements Serializable
{
	protected double latitude,longitude; //latitude 纬度,longitude 经度
	
	// 构造方法
	public Point(double lon,double lati)
	{
		this.latitude=lati;             
		this.longitude=lon;
	}
	
	// getter方法
	public double getLatitude()  // 获取纬度值
	{
		return latitude;
	}
	public double getLongitude() // 获取经度值
	{
		return longitude;
	}

}
