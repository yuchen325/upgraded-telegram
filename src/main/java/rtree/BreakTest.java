package rtree;

// �������������break��continue������,��ɵ��
public class BreakTest
{
    public static void main(String args[]) {
        System.out.println("ѭ��û�п�ʼ");

        System.out.println("���ڿ�ʼ����continue");
        
        for (int i = 0; i < 3; i++) {
            System.out.println("��ʼ��" + i + "��forѭ��");
            if (i == 1) {
                continue;
            }
            System.out.println("����continue������ִ������");
        }
        System.out.println("continue�������\n***********************");

        System.out.println("���ڿ�ʼ����break");
        for (int i = 0; i < 3; i++) {
            System.out.println("��ʼ��" + i + "��forѭ��");
            if (i == 1){
                break;
            }

            System.out.println("����break������ִ������");
        }
        System.out.println("break�������\n***********************");
    }
}