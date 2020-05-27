package rtree;

import org.apache.log4j.Logger;

import java.io.Serializable;


/**
 * 这是一个抽象类,用于描述page
 * 每个结点必须存储在唯一的page，根节点总是存储在page 0.
 * 关键点:节点存储在page之中
 */
public abstract class PageFile implements Serializable
{

	private static final long serialVersionUID = 5369100432986265599L;
	/**
	 * 一个结点能拥有的最多条目(矩形)个数，即fan-out<br>
	 * 一个条目就是一个矩形
	 * branchingFactor的大小与disk block或file system page的大小一致
	 */
	private int branchingFactor; //成员变量1

	private static Logger logger = Logger.getLogger(PageFile.class); //日志记录器,一个类成员
	
	protected RTree tree = null; // 成员变量2 一个R-Tree

	/**
	 * 维度,有何用处?
	 */
	protected int dimension = -1; //成员变量3

	/**
	 * 结点填充因子，0-0.5 有何用处？
	 */
	protected float fillFactor = -1; //成员变量4

	/**
	 * 结点容量
	 * 推测1:一个节点的大小上限?
	 * 推测2:一个树中节点个数的上限？
	 */
	protected int nodeCapacity = -1; // 成员变量5

	/**
	 * 一个结点以字节来存储，计算公式如下： [nodeCapacity * (sizeof(Rectangle) + sizeof(Branch))]
	 * + parent + level + usedSpace = {nodeCapacity * [(2 * dimension *
	 * sizeof(float)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int)
	 * 这个page的大小?
	 */
	protected int pageSize = -1; //		 成员变量6

	/**
	 * 树类型
	 * 这个树它的类型
	 */
	protected int treeType = -1; //		 成员变量7
	
	/**
	 * 读取page中的node
	 * @param page
	 * @return 返回请求page中存储的node
	 * @throws PageFaultError
	 */
	public abstract RTNode readNode(int page) throws PageFaultError; //抽象方法1

	/**
	 * 将node写入page
	 * @param node
	 * @return 将node写入第一个可用的page中，并返回此page
	 * @throws PageFaultError
	 */
	protected abstract int writeNode(RTNode node) throws PageFaultError; //抽象方法2

	/**
	 * 删除page中的node
	 * @param page
	 * @return 标记指定的page为空
	 * @throws PageFaultError
	 */
	protected abstract RTNode deletePage(int page) throws PageFaultError; //抽象方法3

	/**
	 * 成员方法1
	 * PageFile初始化，为其中的属性赋值,有点像构造方法
	 * 
	 * @param tree  一颗R-tree？
	 * @param dimension 树的维度
	 * @param fillFactor 填充因子
	 * @param capacity 树的容量?
	 * @param treeType 树的类型
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

	// 析构函数
	protected void finalize() throws Throwable
	{
		super.finalize();
	}
}
