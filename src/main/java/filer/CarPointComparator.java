package filer;

import entity.CarPoint;

import java.util.Comparator;

//�����������CarPoint�ıȽ���

public class CarPointComparator implements Comparator<CarPoint>
{

	// ����CarPoint����ľ���ȽϷ���
	public int compare(CarPoint cp1, CarPoint cp2)
	{
		return cp1.timestamp.compareTo(cp2.timestamp);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
