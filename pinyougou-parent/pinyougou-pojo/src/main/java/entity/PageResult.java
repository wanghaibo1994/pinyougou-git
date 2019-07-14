package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果返回对象
 * @author asus-1
 *
 */
public class PageResult implements Serializable {
	
	private long total; //返回的总记录数
	private List rows; //返回当前页的结果集
	public PageResult(long total, List rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List getRows() {
		return rows;
	}
	public void setRows(List rows) {
		this.rows = rows;
	}
	

}
