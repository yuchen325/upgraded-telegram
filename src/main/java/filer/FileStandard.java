package filer;


import entity.RoadPoint;
import rtree.Point;
import rtree.Rectangle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * 
 * @author rth
 * 
 * 将原始车辆数据转换成zys-output格式
 *
 */

public class FileStandard {

	/**
	 *
	 * @param filePath 告诉你路网文件的路径
	 * @return 返回一个rtree.test1中要求的一个Float类型的array list
	 */
	public static ArrayList<Double> readnodes_original(String filePath)
	{
		ArrayList<Double> points = new ArrayList<Double>();

		try	// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
		{
			/* 读入TXT文件 */
			String pathname = filePath; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
			File filename = new File(pathname); // 要读取以上路径的input。txt文件
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // 建立一个输入流对象reader
			BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言

			String line = "";
			Double longitude; //记录经度
			Double latitude;  //记录维度

			while ((line=br.readLine()) != null)  // 遍历这个文件的所有行
			{
				String[] data = line.split(" "); // 按逗号将每行拆分成四个部分:点的编号，经度，纬度

				longitude = Double.parseDouble(data[1]);  //提取经度
				latitude = Double.parseDouble(data[2]);	//提取纬度

				points.add(longitude);  				//
				points.add(latitude);

			}

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return points;

	}

	/**
	 * 将发哥的daochu转化为rtree.Test2要求的数据格式
	 * @param filePath  发哥的daochu文件路径
	 * @return 所有路段的 MBR
	 */
	public static ArrayList<Rectangle>  readnodes_cfw(String filePath)
	{

		ArrayList<Rectangle> MBRs = new ArrayList<Rectangle>();//这是一个由矩形组成的链表

		try	// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
		{
			/* 读入TXT文件 */
			String pathname = filePath; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
			File filename = new File(pathname); // 要读取以上路径的input。txt文件
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // 建立一个输入流对象reader
			BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言

			String line = "";

			ArrayList<Integer> id = new ArrayList<Integer>();//道路编号
			id.add(0);  //0是第一个道路编号

			ArrayList<Double> longitudes = new ArrayList<Double>(); //存储经度值,只存储一条路的,不定长

			ArrayList<Double> latitudes = new ArrayList<Double>();  //存储纬度值,只存储一条路的,


			while ((line=br.readLine()) != null)  // 遍历这个文件的所有行
			{
				String[] data = line.split(","); // 按逗号将每行拆分成3个部分:路的编号，经度，纬度

				if (!id.contains(Integer.parseInt(data[0])))
				{
					//下面五行是找出端点的坐标值
					Double[] coordinates = new Double[4];		//存储MBR的端点坐标值,定长,一定是4位
					// 如果你把coordinates写在循环外面,那么所有MBRs中的coordinates都指向同一个地址

					coordinates[0] = (Double) Collections.min(longitudes);
					coordinates[1] = (Double) Collections.min(latitudes);
					coordinates[2] = (Double) Collections.max(longitudes);
					coordinates[3] = (Double) Collections.max(latitudes);


					Point leftlow =new Point(new double[]{coordinates[0],coordinates[1]});//矩形的左下端点
					Point righthigh= new Point(new double[]{coordinates[2],coordinates[3]});//矩形的右上端点

					ArrayList<RoadPoint>  roadpoints = new ArrayList<RoadPoint>();//记录一条路所有的点

					for(int i=0;i<latitudes.size();i++) //遍历所有经纬度，并把它们全部装到一个链表里
					{
						double longitude=longitudes.get(i);
						double latitude=latitudes.get(i);
						RoadPoint roadpoint=new RoadPoint(String.valueOf(id.get(id.size()-1)),longitude,latitude);
						roadpoints.add(roadpoint);
					}

					// 为一条路创建一个包含所有路点及其端点的MBR矩形
					Rectangle MBR=new Rectangle(leftlow,righthigh,roadpoints);

					MBRs.add(MBR); // 这样一来coordinates中存储了路段的MBR坐标值,添加进去

					longitudes.clear(); //清空经度值,为下一条路做准备
					latitudes.clear();    //清空纬度值,为下一条路做准备

					id.add(Integer.parseInt(data[0])); // 添加下一条路的id

				}
				longitudes.add(Double.parseDouble(data[1])); // 记录点的经度值
				latitudes.add(Double.parseDouble(data[2]));  // 记录点的维度值

			}

			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return MBRs;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ArrayList<Float> points = readnodes_original("D:\\学习！！！！！！！！！\\BigData\\课程设计\\深圳-路网\\nodes");


		ArrayList<Rectangle> MBRs = readnodes_cfw("D:\\daochu.txt");

		for (Rectangle c:MBRs)
		{
			System.out.println(MBRs.indexOf(c)+c.describe());
		}

	}

}
