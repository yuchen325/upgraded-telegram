package entity;

import java.io.Serializable;

/**
 * ��������GPS��
 */
public class Point implements Serializable
{
	protected double latitude,longitude; //latitude γ��,longitude ����
	
	// ���췽��
	public Point(double lon,double lati)
	{
		this.latitude=lati;             
		this.longitude=lon;
	}
	
	// getter����
	public double getLatitude()  // ��ȡγ��ֵ
	{
		return latitude;
	}
	public double getLongitude() // ��ȡ����ֵ
	{
		return longitude;
	}

}
