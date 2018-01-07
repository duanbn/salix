package org.duanbn.salix.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class JavassistProxy {

	/** 动态代理类的类名后缀 */
	private final static String PROXY_CLASS_NAME_SUFFIX = "$JavassistProxy_";

	/** 动态代理类的类名索引，防止类名重复 */
	private static int proxyClassIndex = 1;

	/**
	 * 暴露给用户的动态代理接口，返回某个接口的动态代理对象.
	 * 
	 * @param interfaceClassName
	 *            String 要动态代理的接口类名
	 * @param interceptorHandlerImplClassName
	 *            String 用户提供的拦截器接口的实现类的类名
	 * @return Object 返回某个接口的动态代理对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @see com.cuishen.myAop.InterceptorHandler
	 */
	public static Object newProxyInstance(String interfaceClassName, InvocationHandler interceptorHandler)
			throws InstantiationException, IllegalAccessException, NotFoundException, CannotCompileException,
			ClassNotFoundException, NoSuchFieldException, SecurityException {

		Class<?> interfaceClass = Class.forName(interfaceClassName);

		ClassPool cp = ClassPool.getDefault();
		String interfaceName = interfaceClass.getName();
		// 动态指定代理类的类名
		String proxyClassName = interfaceName + PROXY_CLASS_NAME_SUFFIX + proxyClassIndex++;

		CtClass cc = cp.makeClass(proxyClassName);

		CtClass ctInterface = cp.getCtClass(interfaceName);
		cc.addInterface(ctInterface);

		CtClass ctInvocationHandler = cp.get(interceptorHandler.getClass().getName());
		CtField ihField = new CtField(ctInvocationHandler, "interceptor", cc);
		ihField.setModifiers(Modifier.PRIVATE);
		cc.addField(ihField);

		Method[] methods = interfaceClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String methodCode = generateMethodCode(method, interfaceClass, i);

			CtMethod cm = CtNewMethod.make(methodCode, cc);

			cc.addMethod(cm);
		}

		Object obj = (Object) cc.toClass().newInstance();
		Field f = obj.getClass().getDeclaredField("interceptor");
		f.setAccessible(true);
		f.set(obj, interceptorHandler);

		return obj;
	}

	/**
	 * 动态组装方法体，当然代理里面的方法实现并不是简单的方法拷贝，而是反射调用了拦截器里的invoke方法，并将接收到的参数进行传递
	 * 
	 * @param methodToImpl
	 *            Method 动态代理类里面要实现的接口方法的包装
	 * @param interceptorClass
	 *            Class 用户提供的拦截器实现类
	 * @param methodIndex
	 *            int 要实现的方法的索引
	 * @return String 动态组装的方法的字符串
	 */
	private static String generateMethodCode(Method methodToImpl, Class interfaceClass, int methodIndex) {
		String methodName = methodToImpl.getName();
		String methodReturnType = methodToImpl.getReturnType().getName();
		Class[] parameters = methodToImpl.getParameterTypes();

		Class[] exceptionTypes = methodToImpl.getExceptionTypes();
		StringBuffer exceptionBuffer = new StringBuffer();
		// 组装方法的Exception声明
		if (exceptionTypes.length > 0)
			exceptionBuffer.append(" throws ");
		for (int i = 0; i < exceptionTypes.length; i++) {
			if (i != exceptionTypes.length - 1)
				exceptionBuffer.append(exceptionTypes[i].getName()).append(",");
			else
				exceptionBuffer.append(exceptionTypes[i].getName());
		}

		StringBuffer parameterBuffer = new StringBuffer();
		// 组装方法的参数列表
		for (int i = 0; i < parameters.length; i++) {
			Class parameter = parameters[i];
			String parameterType = parameter.getName();
			// 动态指定方法参数的变量名
			String refName = "a" + i;
			if (i != parameters.length - 1)
				parameterBuffer.append(parameterType).append(" " + refName).append(",");
			else
				parameterBuffer.append(parameterType).append(" " + refName);
		}

		StringBuffer methodDeclare = new StringBuffer();
		// 方法声明，由于是实现接口的方法，所以是public
		methodDeclare.append("public ").append(methodReturnType).append(" ").append(methodName).append("(")
				.append(parameterBuffer).append(")").append(exceptionBuffer).append(" {\n");
		// 反射调用用户的拦截器接口
		methodDeclare.append("Object returnObj = this.interceptor.invoke(null, Class.forName(\""
				+ interfaceClass.getName() + "\").getMethods()[" + methodIndex + "], ");
		// 传递方法里的参数
		if (parameters.length > 0)
			methodDeclare.append("new Object[]{");
		for (int i = 0; i < parameters.length; i++) {
			if (i != parameters.length - 1)
				methodDeclare.append("($w)a" + i + ",");
			else
				methodDeclare.append("($w)a" + i);
		}
		if (parameters.length > 0)
			methodDeclare.append("});\n");
		else
			methodDeclare.append("null);\n");
		// 对调用拦截器的返回值进行包装
		if (methodToImpl.getReturnType().isPrimitive()) {
			if (methodToImpl.getReturnType().equals(Boolean.TYPE))
				methodDeclare.append("return ((Boolean)returnObj).booleanValue();\n");
			else if (methodToImpl.getReturnType().equals(Integer.TYPE))
				methodDeclare.append("return ((Integer)returnObj).intValue();\n");
			else if (methodToImpl.getReturnType().equals(Long.TYPE))
				methodDeclare.append("return ((Long)returnObj).longValue();\n");
			else if (methodToImpl.getReturnType().equals(Float.TYPE))
				methodDeclare.append("return ((Float)returnObj).floatValue();\n");
			else if (methodToImpl.getReturnType().equals(Double.TYPE))
				methodDeclare.append("return ((Double)returnObj).doubleValue();\n");
			else if (methodToImpl.getReturnType().equals(Character.TYPE))
				methodDeclare.append("return ((Character)returnObj).charValue();\n");
			else if (methodToImpl.getReturnType().equals(Byte.TYPE))
				methodDeclare.append("return ((Byte)returnObj).byteValue();\n");
			else if (methodToImpl.getReturnType().equals(Short.TYPE))
				methodDeclare.append("return ((Short)returnObj).shortValue();\n");
		} else {
			methodDeclare.append("return (" + methodReturnType + ")returnObj;\n");
		}

		methodDeclare.append("}");

		return methodDeclare.toString();
	}
}
