package entity;

//RoadPoint是Point的子类,继承了Point的所有成员
public class RoadPoint extends Point
{
	// 这是公路上的GPS点,除了经纬度之外还需要3个属性
	
	private String roadName;        // 标识符,这个点所属路段的名称
	private String description;		// 描述字段
	private int  limitSpeed=0;      // 该路段的限速
	private double angle=0;         // 角度,采用弧度制,考虑到路是双向的,取值范围为[0,pi]

	// 5个构造方法
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

	public void setRoadName(String roadName)   //获取 路的名字
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
	
	// 3个getter方法
	public String getRoadName()   //获取 路的名字
	{
		return roadName;
	}
	public int getLimitSpeed()    // 获取 路段限速值
	{
		return this.limitSpeed;
	}
	public double getAngle()      // 获取 路段的角度
	{
		return this.angle;
	}

	public String toString()
	{
		return this.roadName+","+this.description+","+String.valueOf(this.longitude)+","+String.valueOf(this.latitude);
	}
	
}
