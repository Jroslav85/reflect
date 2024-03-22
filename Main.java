package annot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws Exception {
		Set<Object> list = createObjects("annot");
		for (Object object : list) {
			if (object instanceof Controller) {
				Controller controller = (Controller) object;
				controller.setStringUser("main");
			}
		}
	}

	public static Set<Object> createObjects(String packageName) throws Exception {
		Set<Object> objects = new HashSet<>();
		Set<Object> innerSet = new HashSet<>();
		for (Map.Entry<String, Class<?>> entry : getClasses(packageName).entrySet()) {
			if (entry.getValue().isAnnotationPresent(RunClass.class)) {
				Constructor<?>[] constructors = entry.getValue().getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					if (constructor.getParameterCount() == 0) {
						innerSet.add(constructor.newInstance());
					}
				}
			}
		}
		for (Map.Entry<String, Class<?>> entry : getClasses(packageName).entrySet()) {
			if (entry.getValue().isAnnotationPresent(RunClass.class)) {
				Constructor<?>[] constructors = entry.getValue().getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					if (constructor.getParameterCount() > 0) {
						Object[] parameters = new Object[constructor.getParameterCount()];
						int i = 0;
						for (Class<?> parameterType : constructor.getParameterTypes()) {
							String nameClass = parameterType.getName()
									.substring(packageName.length() + 1);
						//	System.out.println(nameClass);
							for (Object entryObject : innerSet) {
//								System.out.println(
//										entryObject.getClass().getName()
//												.substring(packageName.length() + 1)
//								);
								if (entryObject.getClass().getName()
										.substring(packageName.length() + 1).equals(nameClass)) {
									parameters[i] = entryObject;
								}
							}
							i++;
						}
						objects.add(constructor.newInstance(parameters));
					} 
				}
			}
		}
		return objects;
	}

	private static Map<String, Class<?>> getClasses(String packageName) throws Exception {
		Map<String, Class<?>> classes = new HashMap<>();
		String path = packageName.replace('.', '/');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> resources = classLoader.getResources(path);
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			File file = new File(resource.toURI());
			if (file.isDirectory()) {
				for (File subFile : file.listFiles()) {
					String name = subFile.getName();
					if (name.endsWith(".class")) {
						String className = packageName + "." + name.substring(0, name.length() - 6);
						Class<?> clazz = Class.forName(className);
						classes.put(className, clazz);
					}
				}
			}
		}
		return classes;
	}
}
