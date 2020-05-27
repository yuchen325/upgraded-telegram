package rtree;

// �Ʋ�:�����node���õĽӿ�
public interface INode  //����һ���ӿ�,����������г�Ա���ǳ����Ա,����ʹ�øýӿڵ�classȥʵ��
{
	public RTNode getParent();    // ������
	public String getUniqueId();  // ��ȡ��һ�޶���ID
	public int getLevel();        // ��ȡ�ڵ����ڵĲ��
	public Rectangle getNodeRectangle(); // ��ȡ�ڵ��MBR����
	public boolean isLeaf();
    public boolean isRoot();
    public boolean isIndex(); // ����:һ���ڵ�????what����
}
