### SPI浅析

相关代码已上传到[github](https://github.com/Liuzeqin/spidemo)

#### 1. JDK自带SPI机制

##### (1) 简介

SPI 全称为 Service Provider Interface，是一种服务发现机制。SPI 的本质是将接口实现类的全限定名配置在文件中，并由服务加载器读取配置文件，加载实现类。这样可以在运行时，动态为接口替换实现类。正因此特性，我们可以在不改动接口源代码的情况下，通过 SPI 机制为我们的程序提供拓展功能。

##### (2) 编写示例

新建maven工程后，先定义一个接口

```java
public interface UploadCDN {
    void upload(String url);
}
```

接下来定义两个实现类

```java
public class QiyiCDN implements UploadCDN {
    @Override
    public void upload(String url) {
        System.out.println("upload to Qiyi");
    }
}
public class ChinaNetCDN implements UploadCDN {
    @Override
    public void upload(String url) {
        System.out.println("upload to ChinaNetCDN");
    }
}
```

在resource/META-INF/services下创建一个文本文件，文件名为接口的全限定名：com.lzq.spidemo.service.UploadCDN，并写入以下内容

```shell
com.lzq.spidemo.service.Impl.QiyiCDN
com.lzq.spidemo.service.Impl.ChinaNetCDN
```

开始编写测试代码

```java
public class Demo {
    public static void main(String[] args) {
        //注意："META-INF/services/"这个目录是在ServiceLoader写死的
        ServiceLoader<UploadCDN> uploadCDN = ServiceLoader.load(UploadCDN.class);
        for (UploadCDN u : uploadCDN) {
            u.upload("filePath");
        }
    }
}
```

![image-20200923174559026](C:\Users\86173\AppData\Roaming\Typora\typora-user-images\image-20200923174559026.png)

可以看到，所有的实现类都会创建新实例并调用对应方法。

##### (3) ServiceLoader源码

load函数会将当前线程的类加载器和接口的类对象传入ServiceLoader对象

```java
    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
    }
```

创建ServiceLoader对象后，会初始化一个内部的迭代器对象LazyIterator

```java
private class LazyIterator
        implements Iterator<S>
    {

        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    //查找到对应接口的文本文件，然后解析获取到其中记录的所有实现类
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        private S nextService() {
            if (!hasNextService())
                throw new NoSuchElementException();
            String cn = nextName;
            nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader);
            } catch (ClassNotFoundException x) {
                fail(service,
                     "Provider " + cn + " not found");
            }
            if (!service.isAssignableFrom(c)) {
                fail(service,
                     "Provider " + cn  + " not a subtype");
            }
            try {
                S p = service.cast(c.newInstance());
                providers.put(cn, p);
                return p;
            } catch (Throwable x) {
                fail(service,
                     "Provider " + cn + " could not be instantiated",
                     x);
            }
            throw new Error();          // This cannot happen
        }

        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
                    public Boolean run() { return hasNextService(); }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public S next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<S> action = new PrivilegedAction<S>() {
                    public S run() { return nextService(); }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
```

#### 2.Dubbo的SPI机制

##### (1) 简介

dubbo的spi在jdk的spi基础上做了扩展，可以指定或者某个实现类

##### (2) 实例

先定义一个接口LoadBalance, 这里的@SPI作用于接口类上，用来指定默认的实现类标识。

@Adaptive表名该方法会被代理动态实现。

```java
@SPI("demo")
public interface LoadBalance {

    @Adaptive
    void Hello();
}
```

定义两个实现类

```java
public class DemoLoadbalance implements LoadBalance {

    @Override
    public void Hello() {
        System.out.println("this is demo balance");
    }
}
public class TestLoadBalance implements LoadBalance {
    @Override
    public void Hello() {
        System.out.println("this is test balance");
    }
}

```

在resource/META-INF/services下创建一个文本文件，文件名为接口的全限定名：

com.lzq.dubbospidemo.service.LoadBalance，并写入以下内容，注意这里和jdk的写法不一样，前面需要写上标识

```shell
demo=com.lzq.dubbospidemo.service.impl.DemoLoadbalance
test=com.lzq.dubbospidemo.service.impl.TestLoadBalance
```

开始测试

```java
public static void main(String[] args) {
    ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
    LoadBalance demoBalance = extensionLoader.getExtension("demo");
    demoBalance.Hello();
    LoadBalance testBalance = extensionLoader.getExtension("test");
    testBalance.Hello();
    LoadBalance balance = extensionLoader.getDefaultExtension();
    balance.Hello();
}
```

测试结果如图

![image-20200924092737752](C:\Users\86173\AppData\Roaming\Typora\typora-user-images\image-20200924092737752.png)

##### 3. 源码解析

先看一下获取ExtensionLoader的过程

```java
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
     		//不能传入null参数
            throw new IllegalArgumentException("Extension type == null");
        } else if (!type.isInterface()) {
            //需要是接口
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        } else if (!withExtensionAnnotation(type)) {
            //需要带有SPI注解
            throw new IllegalArgumentException("Extension type(" + type + ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        } else {
            //EXTENSION_LOADERS是一个可以并发访问的map对象，在这里相当于一个缓存
            //下面的步骤首先尝试从缓存获取loader对象，若缓存中不存在则新建loader对象
            //放入缓存，然后再次从缓存中获取
            ExtensionLoader<T> loader = (ExtensionLoader)EXTENSION_LOADERS.get(type);
            if (loader == null) {
                EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader(type));
                loader = (ExtensionLoader)EXTENSION_LOADERS.get(type);
            }

            return loader;
        }
    }
```

下面看看获取具体实现类的过程

```java
public T getExtension(String name) {
    if (name != null && name.length() != 0) {
        //参数为true，使用默认的extension
        if ("true".equals(name)) {
            return this.getDefaultExtension();
        } else {
            //和上面一样，先查看缓存是否有对应的实例对象，没有就新建然后再获取
            //这里Holder对象持有一个volatile的value属性，保证了对所有线程的可见性
            Holder<Object> holder = (Holder)this.cachedInstances.get(name);
            if (holder == null) {
                this.cachedInstances.putIfAbsent(name, new Holder());
                holder = (Holder)this.cachedInstances.get(name);
            }

            Object instance = holder.get();
            if (instance == null) {
                //个人认为这里的holder对象主要是减小锁的粒度。
                synchronized(holder) {
                    //两次检查获取，所以这里是线程安全
                    //这里有点像懒汉式单例模式的创建
                    instance = holder.get();
                    if (instance == null) {
                        instance = this.createExtension(name);
                        holder.set(instance);
                    }
                }
            }

            return instance;
        }
    } else {
        throw new IllegalArgumentException("Extension name == null");
    }
}
```

创建实例的过程如下：

```java
   private T createExtension(String name) {
       //这里会先从缓存中查找class对象，如果没有的话就会从配置文件中加载所有的扩展类，最后得到扩展名与对应类的map，详见loadExtensionClasses函数
        Class<?> clazz = (Class)this.getExtensionClasses().get(name);
        if (clazz == null) {
            throw this.findException(name);
        } else {
            try {
                T instance = EXTENSION_INSTANCES.get(clazz);
                if (instance == null) {
                    EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                    instance = EXTENSION_INSTANCES.get(clazz);
                }
				// 向实例中注入依赖，IOC的实现
                this.injectExtension(instance);
                Set<Class<?>> wrapperClasses = this.cachedWrapperClasses;
                Class wrapperClass;
                if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
                	// 将当前instance作为参数传给Wrapper的构造方法，并通过反射创建Wrapper实例。
                	// 然后向 Wrapper 实例中注入依赖，最后将 Wrapper 实例再次赋值给 instance 变量
                    //这里实际上是AOP的实现
                    for(Iterator i$ = wrapperClasses.iterator(); i$.hasNext(); instance = this.injectExtension(wrapperClass.getConstructor(this.type).newInstance(instance))) {
                    // 通过含参的构造方法将SPI实例（根据指定名字创建好的）注入进去
                    // 注入成功并创建好实例之后会把这个组装好的Wrapper实例返回
                    // 这样循环到下一个Wrapper类时其实注入的是上一个Wrapper类实例
                    // 这也解释了为什么后定义的先执行
                        wrapperClass = (Class)i$.next();
                    }
                }

                return instance;
            } catch (Throwable var7) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: " + this.type + ")  could not be instantiated: " + var7.getMessage(), var7);
            }
        }
    }
	
```

```java
    //对SPI注解进行解析,从配置文件的目录加载扩展类
	private Map<String, Class<?>> loadExtensionClasses() {
        SPI defaultAnnotation = (SPI)this.type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + this.type.getName() + ": " + Arrays.toString(names));
                }

                if (names.length == 1) {
                    this.cachedDefaultName = names[0];
                }
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap();
        this.loadDirectory(extensionClasses, "META-INF/dubbo/internal/");
        this.loadDirectory(extensionClasses, "META-INF/dubbo/");
        this.loadDirectory(extensionClasses, "META-INF/services/");
        return extensionClasses;
    }


```

这里的IOC是基于setter函数注入依赖来实现

```java
    private T injectExtension(T instance) {
        try {
            if (this.objectFactory != null) {
                Method[] arr$ = instance.getClass().getMethods();
                int len$ = arr$.length;
				
                for(int i$ = 0; i$ < len$; ++i$) {
                    Method method = arr$[i$];
                    if (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers())) {
                        //获取setter方法参数类型
                        Class pt = method.getParameterTypes()[0];

                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            // 从 ObjectFactory 中获取依赖对象
                            Object object = this.objectFactory.getExtension(pt, property);
                            if (object != null) {
                                // 通过反射调用 setter 方法设置依赖
                                method.invoke(instance, object);
                            }
                        } catch (Exception var9) {
                            logger.error("fail to inject via method " + method.getName() + " of interface " + this.type.getName() + ": " + var9.getMessage(), var9);
                        }
                    }
                }
            }
        } catch (Exception var10) {
            logger.error(var10.getMessage(), var10);
        }

        return instance;
    }
```

##### 4.聊聊AOP的实现

参考[博客](https://blog.csdn.net/baidu_29609961/article/details/106068531)

在上面的源码实现中，涉及到wapper类的处理，dubbo正是基于wapper类来实现wapper，其判断一个类是否为wapper，其实就是判断该类是否含有一个参数类型为SPI接口类型的构造函数

```java

    private boolean isWrapperClass(Class<?> clazz) {
        try {
            // 尝试取得参数类型为SPI接口类型的构造函数
            clazz.getConstructor(this.type);
            return true;
        } catch (NoSuchMethodException var3) {
            return false;
        }
    }
```

同时，在上面的循环部分代码中，可以看出当存在多个wapper时，每个wapper被注入的instance对象实际上是上一个wapper。下面通过一个示例来进行演示

* 同样创建一个接口类AopService

```java
@SPI
public interface AopService {

    @Adaptive
    void service();
}
```

* 创建两个实现类

```java
public class CppAopService implements AopService {
    @Override
    public void service() {
        System.out.println("this is c++ aop service");
    }
}
public class JavaAopService implements AopService {
    @Override
    public void service() {
        System.out.println("this is java aop service");
    }
}
```

* 创建wapper类，用来增强实现类（AOP)

```java
public class AopServiceWapper1 implements AopService {
    private AopService aopService;

    //必须有这个构造方法才能被判断为wapper类
    public AopServiceWapper1(AopService service) {
        this.aopService = service;
    }

    @Override
    public void service() {
        System.out.println("before wapper1");
        aopService.service();
        System.out.println("after wapper1");
    }
}

public class AopServiceWapper2 implements AopService {
    private AopService aopService;

    //必须有这个构造方法才能被判断为wapper类
    public AopServiceWapper2(AopService service) {
        this.aopService = service;
    }

    @Override
    public void service() {
        System.out.println("before wapper2");
        aopService.service();
        System.out.println("after wapper2");
    }
}
```

* 在配置文件中需要加入两个wapper

```shell
wapper1=com.lzq.dubboaopdemo.aopservice.Impl.AopServiceWapper1
wapper2=com.lzq.dubboaopdemo.aopservice.Impl.AopServiceWapper2
java=com.lzq.dubboaopdemo.aopservice.Impl.JavaAopService
cpp=com.lzq.dubboaopdemo.aopservice.Impl.JavaAopService
```

* 进行测试

```java
    public static void main(String[] args) {
        ExtensionLoader<AopService> loader = ExtensionLoader.getExtensionLoader(AopService.class);
        AopService service = loader.getExtension("java");
        service.service();
    }
```

![image-20200924151325846](C:\Users\86173\AppData\Roaming\Typora\typora-user-images\image-20200924151325846.png)