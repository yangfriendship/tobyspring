# Toby Spring vol.2
토비의 스프링 vol.2

### 스프링 3.0 이후로 도입된 기술들
- Java5와 JavaEE 6
- 스프링 표현식 언어(SpEL)
- 지바 코드룰 이용한 DI설정과 DIJ
    - 자바 클래스에서 애너테이션을 이용한 빈 설정
    - 자바 표준 DI 애너테이션을 이용한 빈 설정
- OXM
    - Object-Xml-Mapping
    - Oxm 추상화 기술을 지원
- @MVC와 REST
    - 웹 프레젠테이션 계층을 편리하게 개발하게 최신 앱 기술
- 내장형 DB 지원
    - 내형 DB 사용을 도와주는 기술들 지원

# 1장 Ioc 컨테이너와 DI
- 스프링 어플리케이션은 오브젝트의 생성, 사용, 제거 등의 작업을 어플리케이션 코드가 아닌 독립된 컨테이너가 담당. 컨테이너가 코드 대신 오브젝트를 관리하는 것을 `Ioc`라고 부른다.
- 오브젝트의 생성, 런타임시 관계를 설정하는 관점으로 볼 때, 컨테이너를 `빈 팩토리` 혹은 `애플리케이션 컨택스트라고 부른다.
- 여러가지 기능을 추가한 컨테이너를 `어플리케이션 컨텍스트`라고 부른다.
- 어플리케이션 컨텍스트는 빈 팩토리 이상의 기능을 가졌다고 보면 된다.
- `ApplicationContext` 인터페이스는 `BeanFactory`를 상속한 서브인터페이스 그 외에도 몇 가지 인터페이스를 상속했다.
```
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory
, HierarchicalBeanFactory, MessageSource, ApplicationEventPublisher, ResourcePatternResolver {
```
- 스프링 어플리케이션은 하나 이상의 어플리케이션 컨텍스트를 갖을 수 있다.
- 실제로 스프링 컨텍스트 Ioc 컨테이너라고 말하는 것은 `ApplicationContext`의 구현 오브젝트라고 볼 수 있다.

## 1.1.1 Ioc 컨테이너를 이용해 어플리케이션 만들기

### POJO 클래스
- POJO의 원칙에 따라 어플리케이션 코드를 작성한다.
- 특정 기술과 스펙에서 독립적일뿐만 아니라 의존관계가 있는 다른 POJO클래스와 느슨한 결합을 갖도록 해야한다.
 
1. POJO로 설계한 객체
- Hello 클래스
`Printer`를 인터페이스로 설계함으로써 의존객체와 느슨한 결합 관계를 갖고 있다.
```
public class Hello {
    private String name;
    private Printer printer;
    public String sayHello() {
        return "Hello " + name;
    }
    public void print(){
        this.printer.print(sayHello());
    }
    // get/set 생략..
}
```
- `Printer`의 구현체들 
```
public class StringPrinter implements Printer {
    private StringBuffer buffer = new StringBuffer();
    @Override
    public void print(String message) {
        this.buffer.append(message);
    }
    @Override
    public String toString() {
        return this.buffer.toString();
    }

public class ConsolePrinter implements Printer {
    @Override
    public void print(String message) {
        System.out.println(message);
    }
}
```
### 설정 메타 정보
- 스프링은 XML뿐만 아니라 다양한 방식으로 빈 메타정보를 설정할 수 있다.
-  `BeanDefinition` 인터페이스의 구현체에 담긴 오브젝트를 사용하여 DI,IoC작업을 수행한다.
- 스프링의 메타정보는 특정 파일 포맷 및 형식 등에 종속되지 않는다.
- `BeanDefinitionReade`(인터페이스)를 이용해 `BeanDefinition`구현 오브젝트의 정보를 읽어드린다.
`BeanDefinitionReade`를 구현함에 따라, 설정 메타정보는 어떤 방식으로도 만들 수 있다.

### IoC 컨테이너가 시용하는 빈 메타정보
1. 빈 아이디, 이름 별칭
2. 클래스,클래스 이름
3. 스코프 : 싱글톤, 프로토타입과 같은 빈의 생존 방식과 존재 범위
4. 프로퍼티 또는 참조
5. 생성자 파라미터값 또는 참조
6. 지연로딩, 우선 빈, 자동와이어링, 부모 빈, 빈팩토리 등

- 대략 적인 순서
    1. `BeanDefinitionReade`의 구현체가 `BeanDefinition`의 구현오브젝트(설정)을 읽어드린다.
    2. 만들어진 설정 정보를 `ApplicationContext`의 구현체가 `POJO클래스`를 이용하여 DI,Ioc 작업을 수행
- StaticApplicationContext를 이용한 빈 등록
    - `context.registerSingleton("등록할빈이름",빈의타입.class);` 등록할 빈의 이름, 클래스 타입을 설정
    - 빈을 꺼낼 때는, `context.getBean("등록했던빈이름", 빈의타입.class);`
```
public class HelloBeanTest {
    @Test
    public void staticApplicationContextTest(){
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerSingleton("hello1",Hello.class);
        Hello hello = context.getBean("hello1", Hello.class);
        Assert.notNull(hello);
    }
```
- `BeanDefinition`을 이용한 Bean 등록
`ApplicationContext`의 구현체는 `BeanDefinitionReader`의 구현체를 이용해서 `BeanDefinition`구현 오브젝트를 이용해 설정 메타정보를 만든다.
`RootBeanDefinition`클래스를 이용해서 빈 설정 메타정보를 생성한다.
    1. `RootBeanDefinition`를 생성할 때, 등록할 빈의 타입을 넘겨준다.
    2. `getPropertyValues()`를 이용해 파라미터를 추가
    3. `addPropertyValue("실제클래스변수", "값)`
    4. `registerBeanDefinition(등록할빈이름,BeanDefinition구현체)`
```
    @Test
    public void beanDefinitionRegisterTest() {
        String name = "youzheng";
        String beanName = "hello";
        StaticApplicationContext context = new StaticApplicationContext();
        RootBeanDefinition definition = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);
        context.registerBeanDefinition(beanName, definition);
        Hello hello = context.getBean(beanName, Hello.class);
        Assert.assertNotNull(hello);
        Assert.assertEquals(name,hello.getName());
    }
```
- 등록된 빈 개수 확인
`context.getBeanFactory().getBeanDefinitionCount()`를 이용하여 현재 컨텍스트의 빈 팩토리에 등록된 빈의 개수, 정보를 받아올 수 있다.
```
    @Test
    public void hasDuplicateTypeBeansTest() {
        String name = "youzheng";
        String beanName = "hello";
        StaticApplicationContext context = new StaticApplicationContext();

        RootBeanDefinition definition = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);

        RootBeanDefinition definition2 = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);

        context.registerBeanDefinition(beanName, definition);
        context.registerBeanDefinition("hello2", definition2);

        Hello hello = context.getBean(beanName, Hello.class);

        Assert.assertEquals(2, context.getBeanFactory().getBeanDefinitionCount());
    }
```
- `RuntimeBeanReference`를 이용한 런타임 시 빈의존 관계 주입
```
    @Test
    public void registerBeanWithDependency() {
        context.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class));

        RootBeanDefinition beanDefinition = new RootBeanDefinition(Hello.class);
        beanDefinition.getPropertyValues()
            .addPropertyValue("name", "youzheng");
        beanDefinition.getPropertyValues()
            .addPropertyValue("printer", new RuntimeBeanReference("printer"));

        context.registerBeanDefinition("hello",beanDefinition);

        Hello hello = context.getBean("hello", Hello.class);
        Assert.assertEquals("Hello youzheng",hello.sayHello());
    }

```
## 1.1.2 IoC 컨테이너의 종류와 사용 방법
`ApplicationContext`의 여러가지 구현체가 있다. 딱히 개발자가 직접 구현할 일을 없을 것이란다.
- StaticApplicationContext
    - BeanDefinition을 이용하여 빈 설정 메타정보를 등록
    - 학습이 아니라면 실제로는 사용되지 않는다.
    - 그냥 이런 친구가 존재한다는 것만 기억하자!
- `GenericApplicationContext`
    - 가장 일반적인 어플리케이션 컨텍스트, 실전에서 사용되는 모든 기능을 갖췄다.
    - 외부의 리소스를 불러들여 빈 설정 메타정보로 변환한다. (Xml)
    - `BeanDefinitionReader`의 구현체에 따라 읽어들이는 방식을 다르게 할 수 있다.
    - Xml형식의 설정 정보를 읽어들이기 위해 `XmlBeanDefinitionReader`를 선언한다.
    - 적용할 어플리케이션 컨텍스트를 생성자 파라미터로 넣어준다.
    - `XmlBeanDefinitionReader`에 xml파일의 위치를 파라미터로 넘겨준다.
    - 사용할 어플리케이션 컨텍스트에 `refresh()` 메서드를 실행해 설정 파일을 적용한다.
    - ` PropertiesBeanDefinitionReader`를 이용하여 프로퍼티로 작성한 빈 설정메타 정보를 읽을 수도 있다.
    - 독립적인 어플리케이션을 개발하지 않는 이상 해당 오브젝트는 사용할 일이 없다. JUnit이 이 오브젝트를 이용한 경우
```
    @Test
    public void genericApplicationContextTest(){
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.genericContext);
        reader.loadBeanDefinitions("/helloAppContext.xml");
        this.genericContext.refresh();

        Hello hello = this.genericContext.getBean("hello", Hello.class);
        Assert.assertNotNull(hello);
        Assert.assertEquals("Hello youzheng",hello.sayHello());
    }
```
- `GenericXmlApplicationContext`
    - `GenericApplicationContext` + `XmlBeanDefinitionReader` 두 가지 오브젝트를 합쳐놓은 오브젝트
    - 생성 시, 파라미터에 xml파일의 위치를 넘겨주기만 하면 설정 정보가 등록된다.
```
    @Test
    public void genericXmlApplicationContext() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/helloAppContext.xml");
        Hello hello = context.getBean("hello", Hello.class);
        Assert.assertNotNull(hello);
        Assert.assertEquals("Hello youzheng", hello.sayHello());
    }
```
- WebApplicationContext
    - 스프링에서 가장 많이 사용되는 어플리케이션 컨텍스트(WebApplicationContext의 구현체들)
    - `ApplicationContext`의 서브인터페이스, `ApplicationContext`에 웹 환경에서 필요한 기능들을 확장시켜놨다. 
    - 가장 많이 사용되는 것은 `XmlWebApplicationContext`
    - `ApplicationContext`가 작동하려면 한 번이라도 `getBean()`메서드가 실행함으로써, 빈을 호출해줘야 한다.
    - 웹 어플리케이션에서의 작동 방식은 `main()`메서드 역할을 하는 서블릿을 먼저 만들고, 애플리케이션 컨텍스트를 미리 활성화 시킨 후, 필요한 요청에 따라 빈을 반환하는 식이다.
    - 스프링은 이러한 클러이언트의 요청에 적절한 Bean을 찾아주는 `DispatcherServlet`이라는 서블릿을 제공한다.
    - 서블릿을 `web.xml`을 등록하여 사용한다.

## 1.1.3 IoC 컨테이너 계층구조
한 개 이상의 Ioc컨테이너를 만들 때는, 계층구조를 이용하여 만들 수 있다.

### 부모 컨텍스트를 이용한 계층구조 효과
- 계층 구조 형태에서는 각자의 빈을 스스로 관리한다.
- 자식에게 필요한 빈이 등록되지 않았다면 부모 컨텍스로 올라가 필요한 빈을 검색한다.
- 하지만, 부모 컨텍스트에 빈이 없다면, 부모의 부모 컨텍스트에 빈 검색을 한다.
- 부모 컨텍스트에 빈이 없다고 자식 컨텍스트에 빈 검색을 할 수는 없다.
- 빈 검색 순위
    1. 자신
    2. 직계 부모 순서
    3. 자식 -> 직계부모 (O), 부모 -> 자식(X) 
- 중복되는 빈이 있다면 자식 컨텍스트의 설정이 부모 컨텍스트의 설정을 `오버라이딩`한다.    
### 컨텍스트 계층구조 테스트
1. xml설정
childContext.xml
`Printer`인터페이스의 구현체가 빈으로 등록되어 있지 않다. 부모 컨텍스트에 빈 탐색을 요청해야한다.
IDE가 `ref=printer`라는 빈이 없다고 붉은색 글씨로 경고하지만 런타임 시에, 부모 컨텍스트를 탐색해서
의존 객체를 다이나믹하게 주입해준다.
```
  <bean id="hello" class="springbook.learningtest.hello.Hello">
    <property name="name" value="child"/>
    <property name="printer" ref="printer"/>
  </bean>
```
parentContext.xml
`Printer`인터페이스의 구현체가 빈으로 등록되어 있다.
```

  <bean id="hello" class="springbook.learningtest.hello.Hello">
    <property name="name" value="parent"/>
    <property name="printer" ref="printer"/>
  </bean>

  <bean id="printer" class="springbook.learningtest.hello.StringPrinter"/>

```
2. 테스트 코드
```
    @Test
    public void childAndParentContextTest(){
        GenericXmlApplicationContext parentContext = new GenericXmlApplicationContext(
            "/vol2/parentContext.xml");
        GenericApplicationContext childContext = new GenericApplicationContext(
            parentContext);

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(childContext);
        int result = reader.loadBeanDefinitions("vol2/childContext.xml");
        childContext.refresh();

        Hello parentHello = parentContext.getBean("hello", Hello.class);
        Hello childHello = childContext.getBean("hello", Hello.class);
        Assert.assertNotNull(childHello);
        Assert.assertEquals("Hello child",childHello.sayHello());
        Assert.assertEquals("Hello parent",parentHello.sayHello());
    }
```
3. DI뿐만 아니라 DL도 가능하다.
childContext에는 `Printer`구현체가 빈으로 등록되어 있지 않지만, 호출할 시 부모 컨텍스트의 빈을 찾아서 반환한다.
```@Test
       public void childAndParentContextTest2(){
            // 컨텍스트 상속 생략...
           Printer printer = childContext.getBean("printer", Printer.class);
           Assert.assertNotNull(printer);
           Assert.assertTrue(printer instanceof StringPrinter);
       }```
