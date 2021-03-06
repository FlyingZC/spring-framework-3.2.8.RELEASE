/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.framework;

import java.io.Serializable;

import org.springframework.aop.SpringProxy;

/**
 * Default {@link AopProxyFactory} implementation,
 * creating either a CGLIB proxy or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true
 * for a given {@link AdvisedSupport} instance:
 * <ul>
 * <li>the "optimize" flag is set
 * <li>the "proxyTargetClass" flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>Note that the CGLIB library classes have to be present on
 * the class path if an actual CGLIB proxy needs to be created.
 *
 * <p>In general, specify "proxyTargetClass" to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {


	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) { // 1.isOptimize() 方法为 true,表示让 Spring 自己去优化而不是用户指定;2.ProxyConfig.isProxyTargetClass()方法为 true,这表示配置了 proxy-target-class="true"; 3.ProxyConfig 满足 hasNoUserSuppliedProxyInterfaces() 方法执行结果为 true，这表示 <bean> 对象没有实现任何接口或者实现的接口是 SpringProxy 接口
			Class targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface()) { // 目标类 本身就是接口,使用 JDK 生成代理对象
				return new JdkDynamicAopProxy(config); // 对接口生成代理使用 JDK 原生的 Proxy
			}
			return CglibProxyFactory.createCglibProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config); // 1.默认使用 JDK 自带的 Proxy 生成代理
		}
	}

	/**
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class[] interfaces = config.getProxiedInterfaces();
		return (interfaces.length == 0 || (interfaces.length == 1 && SpringProxy.class.equals(interfaces[0])));
	}


	/**
	 * Inner factory class used to just introduce a CGLIB dependency
	 * when actually creating a CGLIB proxy.
	 */
	private static class CglibProxyFactory {

		public static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			return new CglibAopProxy(advisedSupport);
		}
	}

}
