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
 * ��ԭʼ��������ת����zys-output��ʽ
 *
 */

public class FileStandard {

	/**
	 *
	 * @param filePath ������·���ļ���·��
	 * @return ����һ��rtree.test1��Ҫ���һ��Float���͵�array list
	 */
	public static ArrayList<Double> readnodes_original(String filePath)
	{
		ArrayList<Double> points = new ArrayList<Double>();

		try	// ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw
		{
			/* ����TXT�ļ� */
			String pathname = filePath; // ����·�������·�������ԣ������Ǿ���·����д���ļ�ʱ��ʾ���·��
			File filename = new File(pathname); // Ҫ��ȡ����·����input��txt�ļ�
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // ����һ������������reader
			BufferedReader br = new BufferedReader(reader); // ����һ�����������ļ�����ת�ɼ�����ܶ���������

			String line = "";
			Double longitude; //��¼����
			Double latitude;  //��¼ά��

			while ((line=br.readLine()) != null)  // ��������ļ���������
			{
				String[] data = line.split(" "); // �����Ž�ÿ�в�ֳ��ĸ�����:��ı�ţ����ȣ�γ��

				longitude = Double.parseDouble(data[1]);  //��ȡ����
				latitude = Double.parseDouble(data[2]);	//��ȡγ��

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
	 * �������daochuת��Ϊrtree.Test2Ҫ������ݸ�ʽ
	 * @param filePath  �����daochu�ļ�·��
	 * @return ����·�ε� MBR
	 */
	public static ArrayList<Rectangle>  readnodes_cfw(String filePath)
	{

		ArrayList<Rectangle> MBRs = new ArrayList<Rectangle>();//����һ���ɾ�����ɵ�����

		try	// ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw
		{
			/* ����TXT�ļ� */
			String pathname = filePath; // ����·�������·�������ԣ������Ǿ���·����д���ļ�ʱ��ʾ���·��
			File filename = new File(pathname); // Ҫ��ȡ����·����input��txt�ļ�
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // ����һ������������reader
			BufferedReader br = new BufferedReader(reader); // ����һ�����������ļ�����ת�ɼ�����ܶ���������

			String line = "";

			ArrayList<Integer> id = new ArrayList<Integer>();//��·���
			id.add(0);  //0�ǵ�һ����·���

			ArrayList<Double> longitudes = new ArrayList<Double>(); //�洢����ֵ,ֻ�洢һ��·��,������

			ArrayList<Double> latitudes = new ArrayList<Double>();  //�洢γ��ֵ,ֻ�洢һ��·��,


			while ((line=br.readLine()) != null)  // ��������ļ���������
			{
				String[] data = line.split(","); // �����Ž�ÿ�в�ֳ�3������:·�ı�ţ����ȣ�γ��

				if (!id.contains(Integer.parseInt(data[0])))
				{
					//�����������ҳ��˵������ֵ
					Double[] coordinates = new Double[4];		//�洢MBR�Ķ˵�����ֵ,����,һ����4λ
					// ������coordinatesд��ѭ������,��ô����MBRs�е�coordinates��ָ��ͬһ����ַ

					coordinates[0] = (Double) Collections.min(longitudes);
					coordinates[1] = (Double) Collections.min(latitudes);
					coordinates[2] = (Double) Collections.max(longitudes);
					coordinates[3] = (Double) Collections.max(latitudes);


					Point leftlow =new Point(new double[]{coordinates[0],coordinates[1]});//���ε����¶˵�
					Point righthigh= new Point(new double[]{coordinates[2],coordinates[3]});//���ε����϶˵�

					ArrayList<RoadPoint>  roadpoints = new ArrayList<RoadPoint>();//��¼һ��·���еĵ�

					for(int i=0;i<latitudes.size();i++) //�������о�γ�ȣ���������ȫ��װ��һ��������
					{
						double longitude=longitudes.get(i);
						double latitude=latitudes.get(i);
						RoadPoint roadpoint=new RoadPoint(String.valueOf(id.get(id.size()-1)),longitude,latitude);
						roadpoints.add(roadpoint);
					}

					// Ϊһ��·����һ����������·�㼰��˵��MBR����
					Rectangle MBR=new Rectangle(leftlow,righthigh,roadpoints);

					MBRs.add(MBR); // ����һ��coordinates�д洢��·�ε�MBR����ֵ,��ӽ�ȥ

					longitudes.clear(); //��վ���ֵ,Ϊ��һ��·��׼��
					latitudes.clear();    //���γ��ֵ,Ϊ��һ��·��׼��

					id.add(Integer.parseInt(data[0])); // �����һ��·��id

				}
				longitudes.add(Double.parseDouble(data[1])); // ��¼��ľ���ֵ
				latitudes.add(Double.parseDouble(data[2]));  // ��¼���ά��ֵ

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
		//ArrayList<Float> points = readnodes_original("D:\\ѧϰ������������������\\BigData\\�γ����\\����-·��\\nodes");


		ArrayList<Rectangle> MBRs = readnodes_cfw("D:\\daochu.txt");

		for (Rectangle c:MBRs)
		{
			System.out.println(MBRs.indexOf(c)+c.describe());
		}

	}

}
