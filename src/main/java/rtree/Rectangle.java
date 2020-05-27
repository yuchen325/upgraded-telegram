package rtree;

import entity.RoadPoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 外包矩形<p>
 * 实现Cloneable和Comparable接口，可以根据Hilbert值进行比较排序
 * @ClassName Rectangle 
 * @Description 
 */
public class Rectangle implements Cloneable, Serializable
{

	private static final long serialVersionUID = -3784504003866244090L;
	/**
	 * 左下角坐标
	 */
	private Point low;
	
	/**
	 * 右上角坐标
	 */
	private Point high;
	
	/**
	 * 坐标点的描述，即关键字
	 */
	private String description;

	/**
	 * 如果我用一个MBR描述一条路,仅仅上下两个端点是不够的
	 * 下面这个成员变量用于存储道路上的点
	 * 这种高级类型的成员变量,setter和getter方法必须采用元素克隆机制,否则会产生穿透对象的引用
	 */
	private ArrayList<RoadPoint> road;

	public  ArrayList<RoadPoint> getRoad()
	{
		return (ArrayList<RoadPoint>) road.clone();
	}

	// 构造方法
	public Rectangle(Point p1, Point p2, ArrayList<RoadPoint> roadpoints)
	{
		if(p1 == null || p2 == null)
		{
			throw new IllegalArgumentException("Points cannot be null.");
		}
		if(p1.getDimension() != p2.getDimension())
		{
			throw new IllegalArgumentException("Points must be of same dimension.");
		}
		//check the points
		for(int i = 0; i < p1.getDimension(); i ++)
		{
			if(p1.getDoubleCoordinate(i) > p2.getDoubleCoordinate(i))
			{
				throw new IllegalArgumentException("坐标点为先左下角后右上角");
			}
		}
		low = (Point) p1.clone();
		high = (Point) p2.clone();

		this.road = (ArrayList<RoadPoint>) roadpoints.clone();

	}
	
	public Rectangle(Point p1, Point p2)
	{
		if(p1 == null || p2 == null)
		{
			throw new IllegalArgumentException("Points cannot be null.");
		}
		if(p1.getDimension() != p2.getDimension())
		{
			throw new IllegalArgumentException("Points must be of same dimension.");
		}
		/*先左下角后右上角
		for(int i = 0; i < p1.getDimension(); i ++)
		{
			if(p1.getDoubleCoordinate(i) > p2.getDoubleCoordinate(i))
			{
				throw new IllegalArgumentException("坐标点为先左下角后右上角");
			}
		}*/
		low = (Point) p1.clone();
		high = (Point) p2.clone();
		
	}
	
	public Rectangle(Point p1, Point p2, String description)
	{
		this(p1, p2);
		this.description = description;
	}

	/**
	 * 返回Rectangle左下角的Point
	 * @return Point
	 */
	public Point getLow()
	{
		return (Point) low.clone();
	}

	/**
	 * 返回Rectangle右上角的Point
	 * @return Point
	 */
	public Point getHigh()
	{
		return high;
	}
	
	/**
	 * @param rectangle
	 * @return 包围两个Rectangle的最小Rectangle
	 */
	public Rectangle getUnionRectangle(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle must be of same dimension.");
		}
		
		double[] min = new double[getDimension()];
		double[] max = new double[getDimension()];
		
		for(int i = 0; i < getDimension(); i++)
		{
			min[i] = Math.min(low.getDoubleCoordinate(i), rectangle.low.getDoubleCoordinate(i));
			max[i] = Math.max(high.getDoubleCoordinate(i), rectangle.high.getDoubleCoordinate(i));
		}
		
		Rectangle ret = new Rectangle(new Point(min), new Point(max));
//		ret.hilbertValue = ((hilbertValue >= rectangle.hilbertValue) ? hilbertValue : rectangle.hilbertValue);
		
		return ret;
	}
	
	/**
	 * @return 返回Rectangle的面积
	 */
	public float getArea()
	{
		float area = 1;
		for(int i = 0; i < getDimension(); i ++)
		{
			area *= high.getDoubleCoordinate(i) - low.getDoubleCoordinate(i);
		}
		
		return area;
	}
	
	/**
	 * @param rectangles
	 * @return 包围一系列Rectangle的最小Rectangle
	 */
	public static Rectangle getUnionRectangle(Rectangle[] rectangles)
	{
		if(rectangles == null || rectangles.length == 0)
			throw new IllegalArgumentException("Rectangle array is empty.");
		
		Rectangle r0 = (Rectangle) rectangles[0].clone();
		for(int i = 1; i < rectangles.length; i ++)
		{
			r0 = r0.getUnionRectangle(rectangles[i]);
		}
		
		return r0;
	}
	
	@Override
	protected Object clone()
	{
		Point p1 = (Point) low.clone();
		Point p2 = (Point) high.clone();
		return new Rectangle(p1, p2);
	}
	
	@Override
	public String toString() 
	{
//		if(description == null)
//			return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue + "\n";
		return "Rectangle Low:" + low + "\tHigh:" + high  /* + "\tCenter: " + getCenter() + "\n"*/;
//		return "Rectangle Low:" + low + " High:" + high + "\n" + description ;
	}
	
	public String toStr() {
		if(description == null)
			return "Rectangle Low:" + low + "\tHigh:" + high ;
		return "Rectangle Low:" + low + "\tHigh:" + high  + "\n" + description ;
	}

	/**
	 * 两个Rectangle相交的面积
	 * @param rectangle Rectangle
	 * @return float
	 */
	public double intersectingArea(Rectangle rectangle)
	{
		if(! isIntersection(rectangle))
		{
			return 0;
		}
		
		double ret = 1;
		for(int i = 0; i < rectangle.getDimension(); i ++)
		{
			double l1 = this.low.getDoubleCoordinate(i);
			double h1 = this.high.getDoubleCoordinate(i);
			double l2 = rectangle.low.getDoubleCoordinate(i);
			double h2 = rectangle.high.getDoubleCoordinate(i);
			
			//rectangle1在rectangle2的左边
			if(l1 <= l2 && h1 <= h2)
			{
				ret *= (h1 - l1) - (l2 - l1);
			}else if(l1 >= l2 && h1 >= h2)
			//rectangle1在rectangle2的右边
			{
				ret *= (h2 - l2) - (l1 - l2);
			}else if(l1 >= l2 && h1 <= h2)			
			//rectangle1在rectangle2里面
			{
				ret *= h1 - l1;
			}else if(l1 <= l2 && h1 >= h2)	
			//rectangle1包含rectangle2
			{
				ret *= h2 - l2;
			}
		}
		return ret;
	}
	
	/**
	 * @param rectangle
	 * @return 判断两个Rectangle是否相交
	 */
	public boolean isIntersection(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}
		
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(low.getDoubleCoordinate(i) > rectangle.high.getDoubleCoordinate(i) ||
					high.getDoubleCoordinate(i) < rectangle.low.getDoubleCoordinate(i))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return 返回Rectangle的维度
	 */
	public int getDimension() 
	{
		return low.getDimension();
	}

	/**
	 * 判断rectangle是否被包围
	 * @param rectangle
	 * @return
	 */
	public boolean enclosure(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
			throw new IllegalArgumentException("Rectangle dimension is different from current dimension.");
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(rectangle.low.getDoubleCoordinate(i) < low.getDoubleCoordinate(i) ||
					rectangle.high.getDoubleCoordinate(i) > high.getDoubleCoordinate(i))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Rectangle)
		{
			Rectangle rectangle = (Rectangle) obj;
			if(low.equals(rectangle.getLow()) && high.equals(rectangle.getHigh()))
				return true;
		}
		return false;
	}

	/**
	 * MINDIST距离<p>
	 * 1.查询点p在R内或R的边界上则MINDIST=0<br>
     * 2.查询点p在R外，若最短距离距离（p到R的边）存在，则MINDIST=p到R的边的最短距离， 否则，MINDIST=p到R的顶点的最短最短距离。<p>
	 * MINDIST(P,R):  the shortest distance from P to R
	 * @param point
	 * @return 返回Point到Rectangle的最小距离，即MINDIST距离
	 */
	public double getMinDist(Point point)
	{
		if(point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if(point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		double ret = 0;
		for(int i = 0; i < getDimension(); i ++)
		{
			double p = point.getDoubleCoordinate(i);
			double l = low.getDoubleCoordinate(i);
			double h = high.getDoubleCoordinate(i);
			double r;
			
			if(p < l)
				r = l;
			else if(p > h)
				r = h;
			else 
				r = p;
			
			ret += Math.pow(Math.abs(p - r), 2);
		}
		
		return ret;
		
	}
	
	/**
	 * MINMAXDIST距离<p>
	 * 1.找出与第k轴垂直的并且离查询点p最近的面，记为H<br>
     * 2.选择从查询点p到面H中距离最远的那个点，记为a<br>
     * 3.计算查询点p到点a的距离,记为dk<br>
     * 4.对每个坐标轴重复步骤1-步骤3，记计算所得距离为d1，d2，...dk<br>
     * 5.从所有计算所得距离中选出最小的那一个，即MINMAXDIST<p>
	 * MINMAXDIST(P,R)：the minimum over all dimensions distance from P  to the furthest point of the closest face of the R<p>
	 * The important of MINMAXDIST(P,M) is that it computes the smallest distance between point P
	 * and MBR M that gurantees the finding of an object in M at a Enclidean distance less than or equal to MINMAXDIST(P,M).
	 * @param point
	 * @return 返回Point到Rectangle的最小最大距离，即MINMAXDIST距离
	 */
	public double getMinMaxDist(Point point)
	{
		if(point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if(point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		double ret = Float.POSITIVE_INFINITY;
		for(int k = 0; k < getDimension(); k ++)
		{
			double p = point.getDoubleCoordinate(k);
			double s = low.getDoubleCoordinate(k);
			double t = high.getDoubleCoordinate(k);
			double rm;
			
			if(p <= (s + t)/2.0)
				rm = s;
			else
				rm = t;
			
			double sum = 0;
			for(int i = 0; i < getDimension(); i ++)
			{
				double p_ = point.getDoubleCoordinate(i);
				double s_ = low.getDoubleCoordinate(i);
				double t_ = high.getDoubleCoordinate(i);
				
				if(i != k)
				{
					double rM;
					
					if(p_ >= (s_ + t_)/2.0)
						rM = s_;
					else
						rM = t_;
					
					sum += Math.pow(Math.abs(p_ - rM), 2);
				}
			}
			
			sum += Math.pow(Math.abs(p - rm), 2);
			
			if(sum < ret)
			{
				ret = sum;
			}
			
		}
		
		return ret;
	}
	public String describe()
	{

		return "th road has " +this.road.size()+" points,all points constitute a MBR";
	}

	public String describeCatchRectangle()
	{
		return "This is a catchRectangle.";
	}

	public static void main(String[] args) 
	{



		/*ArrayList<Integer> a1 = new ArrayList<Integer>();
		ArrayList<Integer> a2 = new ArrayList<Integer>();

		for(int i=0;i<10;i++)
			a1.add(i);

		a2=a1; //最浅拷贝

		for (int i=100;i<110;i++)
			a2.add(i);

		for (int a:a1)
			System.out.println(a);

		ArrayList<Integer> a3 = (ArrayList<Integer>) a1.clone(); // 此种拷贝方式可以使a1,a2互相独立

		for (int i=100;i<110;i++)
			a3.add(i);

		for (int a:a1)
			System.out.println(a); */

		Point p = new Point(new double[]{3.5,3.5});

		ArrayList<Point> a1 = new ArrayList<Point>();
		ArrayList<Point> a2 = new ArrayList<Point>();

		//a2 = (ArrayList<Point>) a1.clone();// 此种拷贝方式可以使a1,a2互相独立

		// a2=a1; 最浅拷贝 a1,a2指向同一地址

		a1.add(p);

		System.out.println(a2.size());
	}

}
