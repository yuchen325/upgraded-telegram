package rtree;

import java.util.HashMap;
import java.util.Map;


// �������еĳ־û�page�ļ�
public class CachedPersistentPageFile extends PersistentPageFile // a sub-class of PersistentPageFile
{
	private Map<Integer, CachedObject> cache; // һ��<Integer, CachedObject>��ֵ������ CachedObject�������Զ����һ����,���м�¼��node�����Ӧpage�ı�ŵ���Ϣ
	private int usedSpace = 0; // ʹ���˶��ٿռ�
	private int cacheSize = 0; // ��������С

	/**
	 * ���췽��1
	 * @param fileName �ļ���,�ļ�������
	 * @param cacheSize �������Ĵ�С
	 */
	public CachedPersistentPageFile(String fileName, int cacheSize)
	{
		super(fileName);
		this.cacheSize = cacheSize;
		cache = new HashMap<Integer, CachedObject>(cacheSize);
	}

	/**
	 * ��CachedPersistentPageFile(�������еĳ־û�page�ļ�)�ж�ȡ�ڵ�
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
	 * �ѽڵ�д��CachedPersistentPageFile(�������еĳ־û�page�ļ�)��
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
	 * Ҳ�Ƕ�ȡ,����ò���Ǵӻ������ж�ȡ?
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
	 * ���ڵ�д�뻺����?
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
	 * ɾ���ڵ�
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
	// 3����Ա����
	int rank = 0; 	// ��ţ���α�ţ�
	int page = -1;  // page���?
	RTNode object;  // node

	public CachedObject(RTNode o, int page, int rank) {
		this.object = o;
		this.page = page;
		this.rank = rank;
	}
}