package rtree;

import org.apache.log4j.Logger;

import java.io.Serializable;


/**
 * ����һ��������,��������page
 * ÿ��������洢��Ψһ��page�����ڵ����Ǵ洢��page 0.
 * �ؼ���:�ڵ�洢��page֮��
 */
public abstract class PageFile implements Serializable
{

	private static final long serialVersionUID = 5369100432986265599L;
	/**
	 * һ�������ӵ�е������Ŀ(����)��������fan-out<br>
	 * һ����Ŀ����һ������
	 * branchingFactor�Ĵ�С��disk block��file system page�Ĵ�Сһ��
	 */
	private int branchingFactor; //��Ա����1

	private static Logger logger = Logger.getLogger(PageFile.class); //��־��¼��,һ�����Ա
	
	protected RTree tree = null; // ��Ա����2 һ��R-Tree

	/**
	 * ά��,�к��ô�?
	 */
	protected int dimension = -1; //��Ա����3

	/**
	 * ���������ӣ�0-0.5 �к��ô���
	 */
	protected float fillFactor = -1; //��Ա����4

	/**
	 * �������
	 * �Ʋ�1:һ���ڵ�Ĵ�С����?
	 * �Ʋ�2:һ�����нڵ���������ޣ�
	 */
	protected int nodeCapacity = -1; // ��Ա����5

	/**
	 * һ��������ֽ����洢�����㹫ʽ���£� [nodeCapacity * (sizeof(Rectangle) + sizeof(Branch))]
	 * + parent + level + usedSpace = {nodeCapacity * [(2 * dimension *
	 * sizeof(float)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int)
	 * ���page�Ĵ�С?
	 */
	protected int pageSize = -1; //		 ��Ա����6

	/**
	 * ������
	 * �������������
	 */
	protected int treeType = -1; //		 ��Ա����7
	
	/**
	 * ��ȡpage�е�node
	 * @param page
	 * @return ��������page�д洢��node
	 * @throws PageFaultError
	 */
	public abstract RTNode readNode(int page) throws PageFaultError; //���󷽷�1

	/**
	 * ��nodeд��page
	 * @param node
	 * @return ��nodeд���һ�����õ�page�У������ش�page
	 * @throws PageFaultError
	 */
	protected abstract int writeNode(RTNode node) throws PageFaultError; //���󷽷�2

	/**
	 * ɾ��page�е�node
	 * @param page
	 * @return ���ָ����pageΪ��
	 * @throws PageFaultError
	 */
	protected abstract RTNode deletePage(int page) throws PageFaultError; //���󷽷�3

	/**
	 * ��Ա����1
	 * PageFile��ʼ����Ϊ���е����Ը�ֵ,�е����췽��
	 * 
	 * @param tree  һ��R-tree��
	 * @param dimension ����ά��
	 * @param fillFactor �������
	 * @param capacity ��������?
	 * @param treeType ��������
	 */
	protected void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType)
	{
		logger.info("initializing...");
		this.dimension = dimension;
		this.fillFactor = fillFactor;
		this.nodeCapacity = capacity;
		this.treeType = treeType;
		this.tree = tree;

		this.pageSize = capacity * (8 * dimension + 4) + 12;
	}

	// ��������
	protected void finalize() throws Throwable
	{
		super.finalize();
	}
}
