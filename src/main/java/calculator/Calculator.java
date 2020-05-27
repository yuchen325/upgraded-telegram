package calculator;

import entity.CarPoint;
import entity.MatchedPoint;
import entity.RoadPoint;
import rtree.Point;
import rtree.Rectangle;

import java.util.ArrayList;
import java.util.Date;

public class Calculator {
	//����ƽ���뾶,��λΪ��
    private static final double EARTH_RADIUS = 6371393;
    //�Ѿ�γ��תΪ���ȣ��㣩
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



	
	// ��������ľ���
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
		// ����ͼ����һ��2άƽ��ͼ ��γ��(latitude)Ϊ������ ����(Longitude)Ϊ������
		// ��������ƽ��������ľ��빫ʽ
		double radLat1 = rad(car.getLatitude());
		double radLat2 = rad(road.getLatitude());
		double a = radLat1 - radLat2;
		double b = rad(car.getLongitude()) - rad(road.getLongitude());
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;  //�������� 10000d��ʾ10000��double��
		return s;
		return Math.sqrt(
				Math.pow((road.getLatitude()-car.getLatitude()),2)+
				Math.pow((road.getLongitude()-car.getLongitude()),2));

	}*/

	// ����������ѡƥ����ŷʽ����
	public static double getDistance(RoadPoint p1, RoadPoint p2)
	{
		// ����ͼ����һ��2άƽ��ͼ ��γ��(latitude)Ϊ������ ����(Longitude)Ϊ������
		// ��������ƽ��������ľ��빫ʽ
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

	// ���������ŷ����þ��룬�����ת�Ƹ���Ҫ�õ�
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

	// ��������ľ���
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
		//�ȼ����ѯ��ľ�γ�ȷ�Χ
		double r = EARTH_RADIUS/1000;//����뾶ǧ��
		double dis=distance/1000;
		double dlng =  2*Math.asin(Math.sin(dis/(2*r))/Math.cos(latitude*Math.PI/180));
		dlng = dlng*180/Math.PI;//�Ƕ�תΪ����
		double dlat = dis/r;
		dlat = dlat*180/Math.PI;
		double minlat =latitude-dlat;
		double maxlat = latitude+dlat;
		double minlng = longitude -dlng;
		double maxlng = longitude + dlng;
		final double[] coordinates={minlng,minlat,maxlng,maxlat};
		return  coordinates;
	}

	// ����н�Cosֵ
	public static double getCos(RoadPoint road, CarPoint car)
	{
		// dangle��ʾ�����ǶȵĲ�ֵ
		// dangle=car.angle-road.angle,ȡֵ��Χ(-pi,2*pi)
		// ����=|cos(dangle)|
		
		double dangle = car.getAngle()-road.getAngle();
		
		// ����������ʽ��cos^2+sin^2=1����,��ȷ���˸���Ϊ��ֵ
		return Math.sqrt(1-Math.pow(Math.sin(dangle),2));
		
	}
	
	// ������ʻ�ٶȺ�·�����ٵĲ�ֵ
	public static double getDspeed(RoadPoint road, CarPoint car)
	{
		return Math.abs(road.getLimitSpeed()-car.getSpeed());
	}
	
	/* ���յĸ��ʼ���
	public static double probability(RoadPoint road, CarPoint car, double distanceWeight, double angleWeight, double speedWeight)
	{
		// ��:d=������� ���벿�ֵĸ���=e^(-(d^2))
		// �ǶȲ��ֵĸ��ʾ���getCos�ķ���ֵ
		// ��:d=����֮�� ���ʲ��ֵĸ���=e^(-(d^2))
		//distanceWeight,angleWeight,speedWeight ����3���ָ��ʶ�Ӧ��Ȩ��,ȡֵ��Χ[0,1]
		// ����֮�͵�ȡֵ��Χ[0,1]
		
		double pro1=distanceWeight*Math.pow(Math.E,-1*Math.pow(Calculator.getDistance(road, car),2));
		double pro2=angleWeight* Calculator.getCos(road, car);
		double pro3=speedWeight*Math.pow(Math.E,-1*Math.pow(Calculator.getDspeed(road, car),2));
		
		return pro1+pro2+pro3; //�������۳�Ҳ������?
	}*/
	
// ������0.3�汾����������
	
	// ���������ŷ����þ��룬�����ת�Ƹ���Ҫ�õ�
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
	
	// ����ת�Ƹ���
	/*public static double getTransitionProbability(CarPoint car1,CarPoint car2)
	{
		double l,d,a,p;
		a=Calculator.getDistance(car1, car2);
		double T;
		return T=l*a/d+(1-l)*p;
		���У�a��ʾ���ڹ۲��(����carpoint)֮���ŷ�Ͼ��룬d ��ʾ�������Ӧ��ѡ���(����loadpoint)�����·����
		�������·�������ɵϽ�˹�����㷨�õ�������a/d������ŷ�Ͼ�������·�������Ƴ̶ȡ�
		p��ʾ����ʱ�̾������ı仯���ơ�
		����l��ʾ������ʶ����������仯���ƶ�ת�Ƹ��ʵ�Ӱ��̶ȡ�
		// l,d,p ����3�����㷽����ʱ��û��Ū����
	}
	*/
	
	/*����۲����
	public static double getObservationProbability(RoadPoint road, CarPoint car)
	{
		double f;
		System.out.println("����f(f��GPS�۲��ı�׼ƫ�ȡֵ����ͨ��ʵ�������õ�):");
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
	 * ���㷢�����
	 * @param road ��·�ϵĺ�ѡ��
	 * @param car  �����켣��
	 *@return  ����Ϊ�۲����
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
