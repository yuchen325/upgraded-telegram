package entity;

//RoadPoint��Point������,�̳���Point�����г�Ա
public class RoadPoint extends Point
{
	// ���ǹ�·�ϵ�GPS��,���˾�γ��֮�⻹��Ҫ3������
	
	private String roadName;        // ��ʶ��,���������·�ε�����
	private String description;		// �����ֶ�
	private int  limitSpeed=0;      // ��·�ε�����
	private double angle=0;         // �Ƕ�,���û�����,���ǵ�·��˫���,ȡֵ��ΧΪ[0,pi]

	// 5�����췽��
	public RoadPoint(String roadName, String description, double longitude, double latitude)
	{
		super(longitude,latitude);
		this.roadName = roadName;
		this.description = description;
	}

	public RoadPoint(String n, double lon, double lati, int limit, double angle)
	{
		super(lon,lati);
		this.roadName=n;
		this.limitSpeed=limit;
		this.angle=angle;
	}
	public RoadPoint(String n, double lon, double lati, int limit)
	{
		super(lon,lati);
		this.roadName=n;
		this.limitSpeed=limit;
	}
	public RoadPoint(String n, double lon, double lati, double angle)
	{
		super(lon,lati);
		this.roadName=n;
		this.angle=angle;
	}
	public RoadPoint(String n, double lon, double lati)
	{
		super(lon,lati);
		this.roadName=n;
	}

	public void setRoadName(String roadName)   //��ȡ ·������
	{
		this.roadName = roadName;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	
	// 3��getter����
	public String getRoadName()   //��ȡ ·������
	{
		return roadName;
	}
	public int getLimitSpeed()    // ��ȡ ·������ֵ
	{
		return this.limitSpeed;
	}
	public double getAngle()      // ��ȡ ·�εĽǶ�
	{
		return this.angle;
	}

	public String toString()
	{
		return this.roadName+","+this.description+","+String.valueOf(this.longitude)+","+String.valueOf(this.latitude);
	}
	
}
