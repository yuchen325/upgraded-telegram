package filer;

import entity.CarPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 
 * @author rth,zys
 * 方法一: 输入zys-output 输出一条轨迹
 * 方法二: 输入一个原始数据part-m00000 输出多条轨迹
 *
 */

public class TrajectoryFromFile 
{

	/**
	 * 
	 * @param filePath zys-output格式文件的输入路径
	 * @return 一条轨迹
	 */
	@SuppressWarnings("deprecation")
	private static ArrayList<CarPoint> getTrajectoryFromFile(String filePath)
	{
		ArrayList<CarPoint> trajectory = new ArrayList<CarPoint>();
		
		try	// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
		{
			/* 读入TXT文件 */  
            String pathname = filePath; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径  
            File filename = new File(pathname); // 要读取以上路径的input。txt文件  
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // 建立一个输入流对象reader  
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言  
            
            String line = "";  
            double longitude=0;
            double latitude=0;
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
            Date timestamp;
            
            while ((line=br.readLine()) != null)  // 遍历这个文件的所有行
            {
            		String[] data = line.split(","); // 按逗号将每行拆分成四个部分:车牌号，经度，纬度，时间戳
            		
            		longitude = Double.parseDouble(data[1]);//提取经度            		
            		latitude = Double.parseDouble(data[2]);	//提取纬度 
            		timestamp=ft.parse(data[3]);			//提取时间戳
            		
            		CarPoint cp = new CarPoint(data[0],longitude,latitude,timestamp); //构造CarPoint对象
            		
            		trajectory.add(cp);						// 把新建的CarPoint对象装进去
            		
            }
            
            br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		CarPointComparator comparator = new CarPointComparator();	//创建CarPoint比较器
		
		trajectory.sort(comparator); 	// 把比较器传给sort函数进行排序,这个比较器会告诉sort函数如何比较两个CarPoint的大小
		
		return trajectory;
	}
	
	/**
	 * 
	 * @param filePath 原始数据part-m00000的输入路径
	 * @return 多条轨迹
	 */
	// 我无法确定该文件中轨迹的数量,也无法确定每条轨迹的长度,所以使用了ArrayList
	public static ArrayList<ArrayList<CarPoint>> multipleTrajectoriesFromFile(String filePath)
	{
		// 用于存储多条轨迹,每条轨迹都是一个ArrayList<CarPoint>
		ArrayList<ArrayList<CarPoint>> trajectories = new ArrayList<ArrayList<CarPoint>>();

		
		try	// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
		{
			/* 读入TXT文件 */  
            File filename = new File(filePath);	// 要读取以上路径的input.txt文件  
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // 建立一个输入流对象reader  
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言  
            
    		String[] field;	//用于存储每行的各个字段
    		
    		String carID; 	//车牌号
    		
    		int index;      // 车牌号对应的trajectory下标
    		
    		HashMap<String,Integer> id_tra = new HashMap<String,Integer>();  // 里面的元素是一个个<车牌号Car ID,对应的trajectory下标>键值对
    		
    		id_tra.clear();
            
            String line = "";  
            
            double longitude=0;
            double latitude=0;
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
            Date timestamp;
            
            while ((line=br.readLine()) != null)  // 遍历这个文件的所有行
            {
            	field = line.split(","); 		  // 按逗号将每行拆分成一个String数组,每个元素是一个字段
            	
            	carID = field[0];		  			// 下标为0的字段是车牌号
            	
            	if (id_tra.get(carID) == null)	  // 如果还没见过这个车牌号,就要为该车牌号建立新的trajectory,在trajectories中对应的index=id_tra.size()
            	{
            		index = id_tra.size();	  	// 这个新车牌号对应的新trajectory的下标
            		
            		id_tra.put(carID,index);     // 添加新的<车牌号Car ID,对应的trajectory下标>记录
            		
            		longitude = Double.parseDouble(field[3]);//提取经度            		
            		latitude = Double.parseDouble(field[4]);	//提取纬度 
            		timestamp = ft.parse(field[5].substring(0,19).replaceAll("T"," "));//提取时间戳
            		
            		CarPoint cp = new CarPoint(carID,longitude,latitude,timestamp); //构造CarPoint对象
            		
            		ArrayList<CarPoint> trajectory = new ArrayList<CarPoint>();  //创建新轨迹
            		
            		trajectory.add(cp);			
            		
            		trajectories.add(trajectory);
            	}
            	else 	//如果已经见过这个车牌号
            	{
            		index = id_tra.get(carID);		// 获取该车牌号对应的trajectory下标
            		longitude = Double.parseDouble(field[3]);//提取经度            		
            		latitude = Double.parseDouble(field[4]);	//提取纬度 
            		timestamp=ft.parse(field[5].substring(0,19).replaceAll("T"," "));			//提取时间戳
            		CarPoint cp = new CarPoint(carID,longitude,latitude,timestamp); //构造CarPoint对象
            		
            		trajectories.get(index).add(cp);	// 把这个CarPoint添加到trajectories中的对应的trajectory中去
            	}
            }
            
            br.close();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }   
		
		CarPointComparator comparator = new CarPointComparator();	//创建CarPoint比较器
		
		//对这些轨迹进行排序
		for(ArrayList<CarPoint> trajectory:trajectories)
			trajectory.sort(comparator); 	// 把比较器传给sort函数进行排序,这个比较器会告诉sort函数如何比较两个CarPoint的大小
		
		return trajectories;
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		// 多条原始行车轨迹
		ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(
				"D:\\学习！！！！！！！！！\\BigData\\课程设计\\data-gps\\taxi_gps\\2017-03-07.gz\\part-m-00000\\part-m-00000");
				
		System.out.println("这里面有"+trajectories_raw.size()+"辆车的轨迹");

		System.out.println("下面我来看看一条轨迹的内容,里面的轨迹点是按照时间戳排序的");
				
		ArrayList<CarPoint> trajectory = trajectories_raw.get(0);
				
		for(CarPoint c:trajectory)
			System.out.println(c.toString());
	}

}
