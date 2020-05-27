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
 * ����һ: ����zys-output ���һ���켣
 * ������: ����һ��ԭʼ����part-m00000 ��������켣
 *
 */

public class TrajectoryFromFile 
{

	/**
	 * 
	 * @param filePath zys-output��ʽ�ļ�������·��
	 * @return һ���켣
	 */
	@SuppressWarnings("deprecation")
	private static ArrayList<CarPoint> getTrajectoryFromFile(String filePath)
	{
		ArrayList<CarPoint> trajectory = new ArrayList<CarPoint>();
		
		try	// ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw
		{
			/* ����TXT�ļ� */  
            String pathname = filePath; // ����·�������·�������ԣ������Ǿ���·����д���ļ�ʱ��ʾ���·��  
            File filename = new File(pathname); // Ҫ��ȡ����·����input��txt�ļ�  
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // ����һ������������reader  
            BufferedReader br = new BufferedReader(reader); // ����һ�����������ļ�����ת�ɼ�����ܶ���������  
            
            String line = "";  
            double longitude=0;
            double latitude=0;
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
            Date timestamp;
            
            while ((line=br.readLine()) != null)  // ��������ļ���������
            {
            		String[] data = line.split(","); // �����Ž�ÿ�в�ֳ��ĸ�����:���ƺţ����ȣ�γ�ȣ�ʱ���
            		
            		longitude = Double.parseDouble(data[1]);//��ȡ����            		
            		latitude = Double.parseDouble(data[2]);	//��ȡγ�� 
            		timestamp=ft.parse(data[3]);			//��ȡʱ���
            		
            		CarPoint cp = new CarPoint(data[0],longitude,latitude,timestamp); //����CarPoint����
            		
            		trajectory.add(cp);						// ���½���CarPoint����װ��ȥ
            		
            }
            
            br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		CarPointComparator comparator = new CarPointComparator();	//����CarPoint�Ƚ���
		
		trajectory.sort(comparator); 	// �ѱȽ�������sort������������,����Ƚ��������sort������αȽ�����CarPoint�Ĵ�С
		
		return trajectory;
	}
	
	/**
	 * 
	 * @param filePath ԭʼ����part-m00000������·��
	 * @return �����켣
	 */
	// ���޷�ȷ�����ļ��й켣������,Ҳ�޷�ȷ��ÿ���켣�ĳ���,����ʹ����ArrayList
	public static ArrayList<ArrayList<CarPoint>> multipleTrajectoriesFromFile(String filePath)
	{
		// ���ڴ洢�����켣,ÿ���켣����һ��ArrayList<CarPoint>
		ArrayList<ArrayList<CarPoint>> trajectories = new ArrayList<ArrayList<CarPoint>>();

		
		try	// ��ֹ�ļ��������ȡʧ�ܣ���catch��׽���󲢴�ӡ��Ҳ����throw
		{
			/* ����TXT�ļ� */  
            File filename = new File(filePath);	// Ҫ��ȡ����·����input.txt�ļ�  
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"UTF-8"); // ����һ������������reader  
            BufferedReader br = new BufferedReader(reader); // ����һ�����������ļ�����ת�ɼ�����ܶ���������  
            
    		String[] field;	//���ڴ洢ÿ�еĸ����ֶ�
    		
    		String carID; 	//���ƺ�
    		
    		int index;      // ���ƺŶ�Ӧ��trajectory�±�
    		
    		HashMap<String,Integer> id_tra = new HashMap<String,Integer>();  // �����Ԫ����һ����<���ƺ�Car ID,��Ӧ��trajectory�±�>��ֵ��
    		
    		id_tra.clear();
            
            String line = "";  
            
            double longitude=0;
            double latitude=0;
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
            Date timestamp;
            
            while ((line=br.readLine()) != null)  // ��������ļ���������
            {
            	field = line.split(","); 		  // �����Ž�ÿ�в�ֳ�һ��String����,ÿ��Ԫ����һ���ֶ�
            	
            	carID = field[0];		  			// �±�Ϊ0���ֶ��ǳ��ƺ�
            	
            	if (id_tra.get(carID) == null)	  // �����û����������ƺ�,��ҪΪ�ó��ƺŽ����µ�trajectory,��trajectories�ж�Ӧ��index=id_tra.size()
            	{
            		index = id_tra.size();	  	// ����³��ƺŶ�Ӧ����trajectory���±�
            		
            		id_tra.put(carID,index);     // ����µ�<���ƺ�Car ID,��Ӧ��trajectory�±�>��¼
            		
            		longitude = Double.parseDouble(field[3]);//��ȡ����            		
            		latitude = Double.parseDouble(field[4]);	//��ȡγ�� 
            		timestamp = ft.parse(field[5].substring(0,19).replaceAll("T"," "));//��ȡʱ���
            		
            		CarPoint cp = new CarPoint(carID,longitude,latitude,timestamp); //����CarPoint����
            		
            		ArrayList<CarPoint> trajectory = new ArrayList<CarPoint>();  //�����¹켣
            		
            		trajectory.add(cp);			
            		
            		trajectories.add(trajectory);
            	}
            	else 	//����Ѿ�����������ƺ�
            	{
            		index = id_tra.get(carID);		// ��ȡ�ó��ƺŶ�Ӧ��trajectory�±�
            		longitude = Double.parseDouble(field[3]);//��ȡ����            		
            		latitude = Double.parseDouble(field[4]);	//��ȡγ�� 
            		timestamp=ft.parse(field[5].substring(0,19).replaceAll("T"," "));			//��ȡʱ���
            		CarPoint cp = new CarPoint(carID,longitude,latitude,timestamp); //����CarPoint����
            		
            		trajectories.get(index).add(cp);	// �����CarPoint��ӵ�trajectories�еĶ�Ӧ��trajectory��ȥ
            	}
            }
            
            br.close();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }   
		
		CarPointComparator comparator = new CarPointComparator();	//����CarPoint�Ƚ���
		
		//����Щ�켣��������
		for(ArrayList<CarPoint> trajectory:trajectories)
			trajectory.sort(comparator); 	// �ѱȽ�������sort������������,����Ƚ��������sort������αȽ�����CarPoint�Ĵ�С
		
		return trajectories;
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		// ����ԭʼ�г��켣
		ArrayList<ArrayList<CarPoint>> trajectories_raw = TrajectoryFromFile.multipleTrajectoriesFromFile(
				"D:\\ѧϰ������������������\\BigData\\�γ����\\data-gps\\taxi_gps\\2017-03-07.gz\\part-m-00000\\part-m-00000");
				
		System.out.println("��������"+trajectories_raw.size()+"�����Ĺ켣");

		System.out.println("������������һ���켣������,����Ĺ켣���ǰ���ʱ��������");
				
		ArrayList<CarPoint> trajectory = trajectories_raw.get(0);
				
		for(CarPoint c:trajectory)
			System.out.println(c.toString());
	}

}
