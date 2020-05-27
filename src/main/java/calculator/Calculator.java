package calculator;

import entity.CarPoint;
import entity.MatchedPoint;
import entity.RoadPoint;
import rtree.Point;
import rtree.Rectangle;

import java.util.ArrayList;
import java.util.Date;

public class Calculator {
	//地球平均半径,单位为米
    private static final double EARTH_RADIUS = 6371393;
    //把经纬度转为弧度（°）
    private static double rad(double d){
       return d * Math.PI / 180.0;
    }

    private static double heversin(double theta)
	{
		return Math.pow(Math.sin(theta/2),2);
	}

	private static double getDistance(MatchedPoint p1,CarPoint p2)
	{
		double la1 = rad(p1.getLatitude());
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));
	}



	
	// 计算两点的距离
	public static double getDistance(RoadPoint p1, CarPoint p2)
	{
		double la1 = rad(p1.getLatitude());
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));


	}

	/*public static double getDistance(MatchedPoint road, CarPoint car)
	{
		// 将地图看做一张2维平面图 以纬度(latitude)为横坐标 经度(Longitude)为纵坐标
		// 这里用了平面上两点的距离公式
		double radLat1 = rad(car.getLatitude());
		double radLat2 = rad(road.getLatitude());
		double a = radLat1 - radLat2;
		double b = rad(car.getLongitude()) - rad(road.getLongitude());
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;  //四舍五入 10000d表示10000是double型
		return s;
		return Math.sqrt(
				Math.pow((road.getLatitude()-car.getLatitude()),2)+
				Math.pow((road.getLongitude()-car.getLongitude()),2));

	}*/

	// 计算两个候选匹配点的欧式距离
	public static double getDistance(RoadPoint p1, RoadPoint p2)
	{
		// 将地图看做一张2维平面图 以纬度(latitude)为横坐标 经度(Longitude)为纵坐标
		// 这里用了平面上两点的距离公式
		double la1 = rad(p1.getLatitude());
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));
		/*return Math.sqrt(
				Math.pow((road.getLatitude()-car.getLatitude()),2)+
				Math.pow((road.getLongitude()-car.getLongitude()),2));
				*/
	}

	// 计算两点的欧几里得距离，后面的转移概率要用到
	public static double getDistance(MatchedPoint p1, MatchedPoint p2)
	{

		double la1 = rad(p1.getLatitude());
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));
	}

	// 计算两点的距离
	/*public static double getDistance(Point p1, CarPoint p2)
	{
		double la1 = rad(p1.);
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));
		return Math.sqrt(
				Math.pow((road.getLatitude()-car.getLatitude()),2)+
				Math.pow((road.getLongitude()-car.getLongitude()),2));

	}*/

	/**
	 *
	 * @param carpoint
	 * @param distance(m)
	 * @return
	 */
	public static Rectangle getCatchRectangle(CarPoint carpoint, double distance)
	{
		double[] coordinates = doLngDegress(carpoint.getLongitude(),carpoint.getLatitude(),distance);

		Point left_low = new Point(new double[]{coordinates[0],coordinates[1]});

		Point right_high = new Point(new double[]{coordinates[2],coordinates[3]});

		Rectangle catchRectangle = new Rectangle(left_low,right_high);

		return catchRectangle;
	}

	private static double[] doLngDegress(double longitude,double latitude,double distance)
	{
		//先计算查询点的经纬度范围
		double r = EARTH_RADIUS/1000;//地球半径千米
		double dis=distance/1000;
		double dlng =  2*Math.asin(Math.sin(dis/(2*r))/Math.cos(latitude*Math.PI/180));
		dlng = dlng*180/Math.PI;//角度转为弧度
		double dlat = dis/r;
		dlat = dlat*180/Math.PI;
		double minlat =latitude-dlat;
		double maxlat = latitude+dlat;
		double minlng = longitude -dlng;
		double maxlng = longitude + dlng;
		final double[] coordinates={minlng,minlat,maxlng,maxlat};
		return  coordinates;
	}

	// 计算夹角Cos值
	public static double getCos(RoadPoint road, CarPoint car)
	{
		// dangle表示两个角度的差值
		// dangle=car.angle-road.angle,取值范围(-pi,2*pi)
		// 概率=|cos(dangle)|
		
		double dangle = car.getAngle()-road.getAngle();
		
		// 下面这个表达式由cos^2+sin^2=1导出,还确保了概率为正值
		return Math.sqrt(1-Math.pow(Math.sin(dangle),2));
		
	}
	
	// 计算行驶速度和路段限速的差值
	public static double getDspeed(RoadPoint road, CarPoint car)
	{
		return Math.abs(road.getLimitSpeed()-car.getSpeed());
	}
	
	/* 最终的概率计算
	public static double probability(RoadPoint road, CarPoint car, double distanceWeight, double angleWeight, double speedWeight)
	{
		// 设:d=两点距离 距离部分的概率=e^(-(d^2))
		// 角度部分的概率就是getCos的返回值
		// 设:d=速率之差 速率部分的概率=e^(-(d^2))
		//distanceWeight,angleWeight,speedWeight 是这3部分概率对应的权重,取值范围[0,1]
		// 三者之和的取值范围[0,1]
		
		double pro1=distanceWeight*Math.pow(Math.E,-1*Math.pow(Calculator.getDistance(road, car),2));
		double pro2=angleWeight* Calculator.getCos(road, car);
		double pro3=speedWeight*Math.pow(Math.E,-1*Math.pow(Calculator.getDspeed(road, car),2));
		
		return pro1+pro2+pro3; //这里用累乘也可以吗?
	}*/
	
// 下面是0.3版本的新增部分
	
	// 计算两点的欧几里得距离，后面的转移概率要用到
	public static double getDistance(CarPoint p1, CarPoint p2)
	{

		double la1 = rad(p1.getLatitude());
		double la2 = rad(p2.getLatitude());
		double dLat = la1 - la2;
		double dLon = rad(p1.getLongitude()) - rad(p2.getLongitude());

		//2*R*scala.math.asin(scala.math.sqrt(haversin(dLati)+scala.math.cos(la1)*scala.math.cos(la2)*haversin(dLon)))
		return
				2*EARTH_RADIUS*Math.asin(Math.sqrt(heversin(dLat)+Math.cos(la1)*Math.cos(la2)*heversin(dLon)));
	}
	
	// 计算转移概率
	/*public static double getTransitionProbability(CarPoint car1,CarPoint car2)
	{
		double l,d,a,p;
		a=Calculator.getDistance(car1, car2);
		double T;
		return T=l*a/d+(1-l)*p;
		其中，a表示相邻观察点(两个carpoint)之间的欧氏距离，d 表示两个点对应候选点间(两个loadpoint)的最短路径。
		其中最短路径可以由迪杰斯特拉算法得到。所以a/d以评估欧氏距离和最短路径的相似程度。
		p表示相邻时刻距离误差的变化趋势。
		参数l表示距离相识度与距离误差变化趋势对转移概率的影响程度。
		// l,d,p 它们3个计算方法暂时还没能弄出来
	}
	*/
	
	/*计算观测概率
	public static double getObservationProbability(RoadPoint road, CarPoint car)
	{
		double f;
		System.out.println("输入f(f是GPS观测点的标准偏差，取值可以通过实验评估得到):");
		Scanner sc=new Scanner(System.in);
		f=sc.nextDouble();
		double c=-Math.pow(Calculator.getDistance(road, car),2)/2*Math.pow(f,2);
		double E1=1/(Math.pow(2*Math.PI*f, -1/2)*Math.pow(Math.E, c));
		double E2= Calculator.getCos(road, car);
		return E1*E2;
	} */


	/*

	public static double getObservationProbability_onlydistance(RoadPoint road, CarPoint car)
	{
		double result = Math.pow(Math.E,(-0.01)*getDistance(road, car));

		return result;
	} */

	/**
	 * 计算发射概率
	 * @param road 道路上的候选点
	 * @param car  车辆轨迹点
	 *@return  返回为观测概率
	 */
	public static double observationProbability_onlyDistance(MatchedPoint road, CarPoint car)
	{
		return Math.pow(Math.E,(-0.01)*getDistance(road, car));
	}

	public static double getAverage(ArrayList<Double> a)
	{
		double sum = 0;

		for(double b:a)
			sum+=b;

		return sum/a.size();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MatchedPoint km = new MatchedPoint("1","2",new Date(),104.07,30.67,0);

		MatchedPoint cd = new MatchedPoint("1","2",new Date(),102.72,25.05,0);

		System.out.println(getDistance(cd,km));

	}

}
