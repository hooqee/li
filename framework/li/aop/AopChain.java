package li.aop;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;

/**
 * Aop方法执行链
 * 
 * @author li (limw@w.cn)
 * @version 0.1.1 (2012-09-20)
 */
public class AopChain {
	/**
	 * 目标对象
	 */
	private Object target;
	/**
	 * 方法对象
	 */
	private Method method;
	/**
	 * 参数数组
	 */
	private Object[] args;
	/**
	 * 方法返回值
	 */
	private Object result;
	/**
	 * AopFilter列表
	 */
	private List<AopFilter> filters;
	/**
	 * 方法代理
	 */
	private MethodProxy proxy;
	/**
	 * AopFilter索引
	 */
	private int index = 0;

	/**
	 * 返回被代理方法宿主对象
	 */
	public Object getTarget() {
		return this.target;
	}

	/**
	 * 返回被代理方法
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * 返回方法上的AopFilter列表
	 */
	public List<AopFilter> getFilters() {
		return this.filters;
	}

	/**
	 * 返回方法代理
	 */
	public MethodProxy getProxy() {
		return this.proxy;
	}

	/**
	 * 返回方法参数
	 */
	public Object[] getArgs() {
		return this.args;
	}

	/**
	 * 设置方法参数,在doChain之前才有效
	 */
	public AopChain setArgs(Object[] args) {
		this.args = args;
		return this;
	}

	/**
	 * 返回方法返回值,在方法执行后才有值
	 */
	public Object getResult() {
		return this.result;
	}

	/**
	 * 设置方法返回值,设置后不再doChain才有效
	 */
	public AopChain setResult(Object result) {
		this.result = result;
		return this;
	}

	/**
	 * 初始化一个AopChain
	 */
	public AopChain(Object target, Method method, Object[] args, List<AopFilter> filters, MethodProxy proxy) {
		this.target = target;
		this.method = method;
		this.args = args;
		this.filters = filters;
		this.proxy = proxy;
	}

	/**
	 * 执行AopChain,执行AopFilter链条或者执行代理方法
	 */
	public AopChain doFilter() {
		if (null == filters || index == filters.size()) {// 如果没有AopFilter或者已经经过全部AopFilter
			try {
				this.result = proxy.invokeSuper(target, args);// 则执行目标方法
			} catch (Throwable e) {
				throw new RuntimeException("May be because your AopFilter is not a Bean", e);
			}
		} else {
			filters.get(index++).doFilter(this);// 执行第index个AopFilter然后index++
		}
		return this;
	}
}