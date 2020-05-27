package rtree;

import java.util.HashMap;
import java.util.Map;


// 缓冲区中的持久化page文件
public class CachedPersistentPageFile extends PersistentPageFile // a sub-class of PersistentPageFile
{
	private Map<Integer, CachedObject> cache; // 一个<Integer, CachedObject>键值对链表 CachedObject是下面自定义的一个类,其中记录了node及其对应page的编号等信息
	private int usedSpace = 0; // 使用了多少空间
	private int cacheSize = 0; // 缓冲区大小

	/**
	 * 构造方法1
	 * @param fileName 文件名,文件的名字
	 * @param cacheSize 缓冲区的大小
	 */
	public CachedPersistentPageFile(String fileName, int cacheSize)
	{
		super(fileName);
		this.cacheSize = cacheSize;
		cache = new HashMap<Integer, CachedObject>(cacheSize);
	}

	/**
	 * 从CachedPersistentPageFile(缓冲区中的持久化page文件)中读取节点
	 * @param page
	 * @return
	 * @throws PageFaultError
	 */
	public RTNode readNode(int page) throws PageFaultError
	{
		RTNode n = readFromCache(page);
		if (null != n)
		{
			return n;
		}
		else {
			return super.readNode(page);
		}
	}

	/**
	 * 把节点写入CachedPersistentPageFile(缓冲区中的持久化page文件)中
	 * @param n
	 * @return
	 * @throws PageFaultError
	 */
	protected int writeNode(RTNode n) throws PageFaultError
	{
		int page = super.writeNode(n);
		writeToCache(n, page);
		return page;
	}

	/**
	 * 也是读取,但是貌似是从缓冲区中读取?
	 * @param page
	 * @return
	 */
	private RTNode readFromCache(int page)
	{
		CachedObject c = cache.get(page);
		if (c != null)
		{
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
			return c.object;
		} else
			{
			return null;
		}
	}

	/**
	 * 将节点写入缓冲区?
	 * @param o
	 * @param page
	 */
	private void writeToCache(RTNode o, int page) {
		CachedObject c = cache.get(page);

		if (null != c) {
			c.object = o;
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
		} else if (usedSpace < cacheSize) {// cache is not full
			cache.put(page, new CachedObject(o, page, usedSpace));
			usedSpace++;
			return;
		} else {// cache is full
			for (CachedObject co : cache.values()) {
				if (co.rank == 0) {
					cache.remove(co.page);
					break;
				}
			}
			for (CachedObject co : cache.values()) {
				co.rank--;
			}

			cache.put(page, new CachedObject(o, page, usedSpace - 1));
		}
	}

	/**
	 * 删除节点
	 * @param page
	 * @return
	 * @throws PageFaultError
	 */
	protected RTNode deletePage(int page) throws PageFaultError
	{
		CachedObject c = cache.get(page);
		if (c != null) {
			int rank = c.rank;

			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				}
			}

			cache.remove(page);
			usedSpace--;
		}
		return super.deletePage(page);
	}
}

class CachedObject
{
	// 3个成员变量
	int rank = 0; 	// 编号？层次编号？
	int page = -1;  // page编号?
	RTNode object;  // node

	public CachedObject(RTNode o, int page, int rank) {
		this.object = o;
		this.page = page;
		this.rank = rank;
	}
}