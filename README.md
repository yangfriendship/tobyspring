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
       }
```

## 1.1.4 웹 애플리케이션의 IoC 컨테이너 구성
서버에서 스프링 컨텍스트를 사용하는 방법은 대략 3가지로 구분된다.

### 1. 웹 어플리케이션 안에 `WepApplicationContext`를 두는 방법
- 스프링을 사용한다면 보통 독립적으로 배치치가 가능한 `WAR`(웹모듈)형태의 어플리케이션을 배포
- 대표 서블릿을 놓고 공통 선행 작업을 진행 후, 각 요청에 따라 `핸들러`라고 불리는 클래스를 호출하는 방식으로 개발한다.
- 몇 개의 서블릿이 중앙집권식으로 모든 요청을 받아서 처리하는 방식을 `프론트 컨트롤 패턴`이라고 한다.
- 스프링 웹 어플리케이션에 사용되는 서블릿의 개수는 많아야 `2~3개` 정도
- 서블릿 안에 만들어지는 방법
- 웹 어플리케이션 레벨에서 만들어지는 방법
- 일반적으로 이 상위 `두가지 방법 모두를 사용하여` 어플리케이션 컨텍스트를 만든다.

### 웹 애플리케이션의 컨렉스트 계층구조
- 웹 어플리케이션 레벨에 등록된 컨테이너를 보통 `루트 웹 어플리케이션 컨텍스트`라고 부른다.
- 서블릿 레벨에 등록되는 컨테이너들의 부모가 된다.
- 전체 계층 구조상 제일 root단에 위치하는 컨텍스트
- 웹 어플리케이션에는 하나 이상의 `프론트 컨트롤` 역할을 하는 서블릿이 등록될 수 있다.
- 이 서블릿에도 독립적인 어플리케이션 컨텍스트가 만들어진다.
- 굳이 서블릿을 2개 이상 등록할 필요가 없으므로, 일반적으로 스프링 애플리케이션 컨텍스트를 가지면서 `프론트 컨트롤러`역할을 하는 하나의 서블릿을 만들어 사용한다.
- 스프링은 웹 어플리케이션 마다 하나씩 존재하는 서플릿 컨텍스트를 통해 루트 컨텍스트에 접근하는 방법을 제공
- `ServletContext`는 웹 어플리케이션 마다 하나씩 만들어지며 `HttpSession`,`HttpServletRequest`를 통해서 간단히 `ServletContext`를 가져올 수 있다.
- 서블릿 컨텍스트는 계층구조상 루트 컨텍스트 보다 밑에 있으므로, 설정된 빈 설정이 `무시`되거나 `참조`될 수 있다.
- 또한, `AOP관련설정`은 영향을 미치지 않는다. 

### 웹 플리케이션의 컨텍스트 구성 방법
어플리케이션 컨텍스트를 구성하는 방법은 `3가지`로 볼 수 있다.
1. 서블릿릿 컨텍스트와 루트 어플리케이션 컨텍스트 계층구조
    - 사실상 제일 많이 사용되는 `국룰`같은 방법
    - 웹 관련 기술들을 서블릿 컨텍스트에 둔다.
    - 나머지는 `루트 웹 컨텍스트`에 등록
    - `루트 웹 컨텍스트는` 모든 서블릿 레벨의 컨텍스트의 부모가 된다.
2. 루트 어플리케이션 컨텍스트 `단일구조`
    - 스프링 웹 기술을 사용하지 않고 `서드파티 웹 프레임워크`나 `서비스 엔진`만을 사용하는 경우
    - 굳이 스프릥 서블릿(DispatcherServlet)을 둘 필요 없이, `루트 웹 컨텍스트`만 등록해준다.
3. 서블릿 컨텍스트 `단일구조`
    - 스프링 웹 기술만을 사용하는 경우, `루트 웹 컨텍스트`를 생략할 수 있다.
    - 서블릿 안에 만들어지는 컨텍스트가 부모 컨텍스트를 갖지 않으므로 루트 컨텍스트가 된다.
    
### 루트 어플리케이션 컨텍스트 등록
- 웹 어플리케이션에서 `루트 웹 컨텍스트`는 서블릿의 `이벤트리스너`를 이용해서 만든다.
- `ServletContextListener`인터페이스의 구현체는 `DB연결`, `로깅`같은 서비스를 만드는데 유용하게 쓰인다.
   어플리케이션의 시작될 때 루트 컨텍스트를 생성하고 초기화하고, 종료될 때 컨텍스트와 함께 종료되는 리스너를 만들 수 있다.
- `ContextLoaderListener`라는 구현체를 이용한다.(스프링제공)

- 컨텍스트 클래스 변경
애노테이션을 이용한 설정을 사용하려면 <context-param>을 이용해서 파라미터를 꼭 넘겨 줘야한다.
```
  <!-- ContextLoaderListener의 디폴트 클래스는 XmlWebApplicationContext이지만 아래와 같이 변경 가능하다. -->
  <context-param>
    <param-name>contextClass</param-name>
    <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext
    </param-value>
  </context-param>

  <context-param>
    <param-name>contextConfigurationLocation</param-name>
    <param-value>
      [위치]
    </param-value>
  </context-param>
```

### 서블릿 애플리케이션 컨텍스트 등록
- `DispatcherServlet`을 이용하여 프론트 컨트롤 방식으로 웹 기능을 지원한다.
- `Web.xml`에 등록하여 사용
- `DispatcherServlet`은 서블릿이 초기화 될 때, 자신의 컨텍스트 생성, 초기화한다.
- 동시에 루트 웹 어플리케이션을 찾아 자신의 부모 컨텍스트로 등록한다.


## 1.2 IoC DI를 위한 빈 설정 메타정보 작성
IoC 컨테이너의 기본적인 역할은 어플리케이션의 빈을 생성, 관리하는 것이다.
`BeanDefinitionReader` 인터페이스를 구현한다면 어떠한 형식의 빈 설정도 읽어드릴 수 있다.
`BeanDefinition`로 만들어진 오브젝트를 `BeanDefinitionReader`가 읽어드리는 것이다.

## 1.2.1 빈 메타 정보
### 빈 설정 메타정보 항목 (P.84 참고)
요약 : 컨테이너에 빈의 메타정보가 등록될 때, 필수인 것은 `클래스 이름`과 `빈 이름` 또는 `빈 아이디`이다.

## 1.2.2 빈 등록 방법
스프링에서 자주 이용되는 빈 등록 방법(5가지)

1. XML:<bean> 태그
 
2. XML:네임스페이스와 전용 태그
빈의 속성을 분류하기 위해서 스프링에서는 네임스페이스와 전용 태그를 제공한다.
1권에서 AOP를 틍록할 때 사용했던 `<AOP:Config >` 등이 이러한 기능이다.
    - 의도를 파악하기 좋다.
    - 애트리뷰티의 타입과 필수 사용 여부 등을 검증하기가 쉽다.
    - 여러 가지 설정을 한 번에 할 수 있다. ex)`<context:annotation-config >`는 다섯 가지 빈 설정이 선언되어있다.
3. 자동인식을 이용한 빈 등록(@Component,@ComponentScan)
    - 특정 애너네이션이 붙은 빈을 등록된 패키지을 `빈 스캐닝`하여 자동으로 빈으로 등록해주는 기능, 스캐닝을 책임지는 오브젝트를 `빈 스캐너`라고 한다.
    - `@Component`를 포함해 디폴트 필터에 적용되는 애노테이션을 `스테레오타입 애노테이션`이라고 한다.
    - `@Component`으로 등록된 빈의 이름은 자동으로 `본래이름 -> 카멜케이스`로 변경해서 등록한다. 
    - `@Component(${지정이름})`으로 직접 빈의 이름을 지정해줄 수 있다.
    - `AnnotationConfigApplicationContext`는 빈 스캐너를 내장하고 있다. 즉,<context:component-scan base-package="" />을 기본 포함하는 것이다.
    1. 테스트 코드 작성 
    ```
        @Test
        public void annotationConfigApplicationContext(){
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                "springbook/learningtest");
    
            AnnotationHello hello = context.getBean("annotationHello", AnnotationHello.class);
            Assert.assertNotNull(hello);
        }
    ```
    2. `@Component`를 이용한 빈 등록
    ```
    @Component
    public class AnnotationHello extends Hello {
    }
    ```
    - 개발 생산성에 있어서는 애노테이션을 이용한 빈 등록이 좋다.
    - 세밀한 빈 관리,설정이 필요하다면 Xml을 이용한 빈 등록을 사용하는 것이 좋다!
4. Xml을 이용한 빈 스캐너 등록
    - `<context:component-scan base-package="${패키지}" />`을 통해서 빈 스캐너를 선언할 수 있다.
    - 설정된 패키지에 `@Component`가 붙은 클래스를 빈으로 자동 등록해준다.

5. 빈 스캐너를 저장한 어플리케이션 컨텍스트 사용
    - `AnnotationConfigWebApplicationContext` 역시 빈 스캐너를 내장하고 있다.
    - `<ContextParam >` 컨텍스트 파라미터로 `ContextConfigLocation`의 값을 설정할 수 있다.

### 자바 코드에 의한 빈 등록: @Configuration 클래스의 @Bean 메소드
- `@Configuration` 애너테이션을 이용해서 만든다.
```
@Configuration
public class AnnotationHelloConfig {
    @Bean
    public AnnotationHello annotationHello() {
        return new AnnotationHello();
    }
```
- 빈 설정 정보를 담은 자바 클래스 역시 빈으로 등록된다.
- Xml파일에서도 `@Configuration`에 등록된 빈을 사용할 수 있다.
아래와 같이 컴포넌트 스캔을 설정해준다면 검색할 수 있다.
```
  <context:component-scan base-package="springbook.learningtest" />

```
- 자바 코드로 등록된 빈들은 별도의 설정이 없다면 `싱글톤`으로 만들어진다.
    - `new`를 사용해 매번 새로운 인스턴스를 생성하여 반환하는 것처럼 보이지만 내부적으로 이미 생성되어 있는 빈을 반환하는 것이다.
    - `getBean()` 메서드로 빈을 받아와도 별도의 설정을 하지 않았다면 `싱글톤`이기 때문에 항상 같은 빈을 반환한다.
    - hello와 hello2는 동일한 printer의 구현체를 주입 받는다.
        ```
      @Configuration
      public class HelloConfig {
          @Bean
          public Hello hello(){
              Hello hello = new Hello();
              hello.setName("hello");
              hello.setPrinter(printer());
              return hello;
          }
          @Bean
          public Hello hello2(){
              Hello hello = new Hello();
              hello.setName("hello2");
              hello.setPrinter(printer());
              return hello;
          }
          @Bean
          public Printer printer() {
              return new StringPrinter();
          }
      ```
    - `printer()` 메서드가 총 2번 호출된 것 처럼 보이지만, 모두 같은 printer의 구현체를 주입받는다.
        ```
      @Test
          public void helloConfigSingleTonTest(){
              ApplicationContext context = new AnnotationConfigApplicationContext(
                  HelloConfig.class);
              Hello hello = context.getBean("hello", Hello.class);
              Hello hello2 = context.getBean("hello2", Hello.class);
      
              Assert.assertNotSame(hello,hello2);
              Assert.assertSame(hello.getPrinter(),hello2.getPrinter());
          }
      ```
    - 자바 코드로 빈 정보를 설정한다면 컴파일러와 IDE의 적극적인 도움을 받을 수 있다.

### 자바 코드에 의한 빈 등록: 일반 빈 클래스의 @Bean 메소드  
- 일반 POJO 클래스에도 `@Bean`애너테이션을 이용하여 빈 등록을 할 수 있다.(@Configuration이 없는) 
- POJO 클래스로만 빈을 등록했다면 `싱글톤을 보장해줄 수 없다.`
- 유연성이 떨어지는 단점이 있다.
- 수정을 위해 직접 빈 클래스를 수정해야한다는 단점이 있다.

### 빈 등록 메타정보 구성 전략
- XML 단독 사용
    - 모든 빈 설정을 XML을 이용해서 설정
    - 스프링에서 지원해주는 `네임스페이스`와 `전용태그`를 적극 사용할 수 있다.
    - 특화된 스키마와 전용태그를 만든다면 XML 설정의 양을 대폭 줄일 수 있다.
- XML과 빈 스캐닝의 혼용
    - 등록하기 번거러운 AOP, 내장형DB, OXM마샬러 등 전용 스키마와 전용태그로 만든다.
    - 나머지 부분은 빈 스캐너의 도움을 받아 빈으로 등록
    - 빈 스캐닝과 XML을 이용한 빈 등록 각각의 장점만을 이용하여 개발한다.
- XML 없이 빈 스캐닝 단독 사용
    - @Configuration이 붙은 자바 설정 클래스를 생성한다.
    - 루트 컨텍스트와 서블릿 컨텍스트의 contextClass를 `AnnotationConfigWebApplicationContext`로 변경해준다.
    - `AnnotationConfigWebApplicationContext`는 내부적으로 `ComponentScan` 기능을 갖고 있기 때문에 `contextLocations`를 통해서 스캔대상 패키지를 설정해야한다.

## 1.2.3 빈 의존관계 설정 방법

1. XML: <property>,<constructor-arg>
- <property> setter 주입 방식 
빈으로 등록될 클래스에 `setXXXX` 메서드가 꼭 정의되어 있어야한다. <br >
`ref=`를 이용하여 등록된 빈을 참조할 수 있다. <br >
`value=`를 이용하여 String,int,char 등 뿐만 아니라 `class` 역시 자동 타입 변환하여 넣어준다.
- <constructor-arg>
빈으로 등록될 클래스에 정의된 생성자를 기준으로 의존객체를 주입한다. <br >
Lombok을 이용하면 불편함을 많이 줄일 수 있다. <br >
`<constructor-arg type=${classLocation}>`을 통해서 타입을 기준으로 의존성 주입이 가능하다. <br >
중복되는 파라미터가 있다면 `<constructor-arg index=${order}>`로 파라미터의 순서를 설정할 수 있다. <br >
`<constructor-arg name=${parameterName}>` 파라미터의 이름을 기준으로도 가능하다. <br >

2. XML 자동 와이어링

## 1.2.4 프로퍼티 값설정 방법
DI를 통해서 주입되는 것은 두 가지다. 하나는 다른 빈 오브젝트의 `레퍼런스`이고, 다른 하나는 단순한 `값`(value)이다. <br >
싱글톤은 동시성 문제 때문에 필드 값을 함부로 수정하지 않는다. 보통 상태가 없는 `읽기전용`으로 만든다. <br >

### 메타정보 종류에 따른 값 설정 방법
- XML: <property>와 전용 태그

- 애노테이션 : `@Value`
    - 빈이 사용해야할 값을 코드에 담지 않고 설정을 통해 런타임시에 주입해야할 때 사용하는 방법
    - 환경이 달라질 수 있는 경우 (ex.DataSource)
    - 테스트나 특별한 이벤트 때, 초기값 대신 다른 값을 사용하고 싶은 경우
    - `@Value`는 스프링이 넣어주는 것이기 때문에 컨테이너 밖에서 사용한다면 값이 등록되지 않는다.
- 자바 코드: `@Value`
    - 외부 리소스를 참고할 때, 사용 가능하다. 
    ```
  @Value("${database.url}")
  ```
    - 파라미터에 바로 사용 가능하다.
    ```
  public void method(@Value("${database.url}")String url ){
  ...
  }
  ```
### PropertyEditor와 ConversionService
PropertyEditor : `@Value`를 통해서 값을 전달할 때, 자동을 값을 변환해주는 기능, 자바가 지원해주는 기본기능
ConversionService : 스프링이 지원해주는 타입변환 기능
    - 따로 빈으로 등록해줘야 한다.
    - org.springframework.context.support.ConversionServiceFactoryBean
    - `PropertyEditor`를 대신해서 기능을 제공
    
### 컬렉션
컬렉션의 값을 XMl을 이용해서 작성할 수 있다.
- List, Set
    - `<list>`,`<set>`
    - `<value>`를 사용해서 값을 설정

- Map
    - `<map>`
    - `<entry key=${keyName} value=${value}>`

- Properties
```
<properties name=${name} >
    <props>
        <prop key=${key}>${value}></ props>
        <prop key=${key}>${value}></ props>
    </props>
</properties>
```
- 컬렉션에는 `<value>`로 값을 넣을 뿐만 아니라 `ref bean=${beanName}`을 통해 정의된 빈을 넣을 수도 있다.

### <util:list >, <util:set >
util스키마의 전용태그를 이용하면 아래와 같이 컬렉션도 빈으로 등록하여 사용할 수 있다.
- `<util:list>`
`<util:list id=${name} list-class="java.util.ArrayList" ">`
- `<util:set>`
`<util:set id=${name} set-class="java.util.##set" ">`
- `<util:map>`
`<util:map id=${name} map-class="java.util.##map" ">`
- <util:properties>
location으로 외부 설정 파일을 가져와서 사용할 수 있따.
`<util:properties id=${name} location="classpath:xxxx"">`

### Null과 빈문자열
- Null을 주입할 때는 `<null />`을 사용한다.
- 빈 문자열을 입력할 땐, ""을 넣어주면 된다.

### 프로퍼티 파일을 이용한 값 설정
외부설정은 프로퍼티 파일을 이용해서 외부로 분리하자 이것이 객체지향적이기도 하고 변경과 수정에 쉽게 대응할 수 있기 때문이다. <br />
프로퍼티는 xml과 비교해 복잡한 구성이 필요 없고, key와 value만으로 이루어져 있기 때문에 손쉽게 이용할 수 있다 <br />
프로퍼티가 변경될 경우에 따로 코드를 변경해 재 컴파일할 필요도 없다. <br/ >

- `PropertyPlaceHolderConfigurer`를 이용한 수동 변환
    - `PropertyPlaceHolderConfigurer`를 빈으로 등록해준다.
    - 치환자 `${peroperty.ke}`를 이용해서 값을 매칭시킨다.
    - `@Value`를 사용해서 값을 불러올 수도 있다.
```
  @Bean
  public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
  }
```
```
  <context:property-placeholder location="classpath:database.properties"/>
```
- 능동변환: `SpEL`
스프링 전용 표현식 언어. SpEL를 사용하면 손쉽게 다른 빈 오브젝트나 프로퍼티에 접근할 수 있다. `#{}`의 표현식을 이용해서 사용한다.

## 1.2.5 컨테이너가 자동등록하는 빈
스프링 컨테이너가 초기화 과정에서 자동으로 등록되는 빈, 직접 사용하는 일은 없지만 기억해두면 좋다!
```
public interface ApplicationContext extends 
    EnvironmentCapable, 
    ListableBeanFactory, 
    HierarchicalBeanFactory,
    MessageSource, 
    ApplicationEventPublisher, 
    ResourcePatternResolver {
}
```
### ApplicationContext, BeanFactory
1. ApplicationContext의 구현체
2. BeanFactory
    - ApplicationContext는 BeanFactory의 서브인터페이스
    - 직접 생성하기 보다는 ApplicationContext 내부에 정의된 `DefaultListableBeanFactory`를 사용
### ResourceLoader, ApplicationEventPublisher
1. ResourceLoader
    - 서버 환경에 따라서 리소스를 로딩할 수 있는 기능
    - 서블릿 컨텍스트의 리소스를 사용할 때, `ResourceLoader` 타입으로 DI를 받으면 된다.
2.  ApplicationEventPublisher
    - 자주 사용되지 않는다.
### systemProperties, systemEnvironment
스프링 컨테이너가 직접 등록하는 빈 중에서 타입이 아니라 이름을 통해 접근하는 빈
- JVM이 생성하는 시스템 프로퍼티 값을 읽을 수 있다.
- `@Resource`로 오토 와이어링하는 것이 바람직하다. 
```
@Configuration
public class HelloConfig {
    @Resource
    private Properties systemProperties;
```
```
    @Test
    public void systemPropertiesTest(){
        ApplicationContext context = new AnnotationConfigApplicationContext(
            HelloConfig.class);
        Properties properties = context.getBean("systemProperties", Properties.class);
        for(String prop : properties.stringPropertyNames()){
            System.out.println(prop.toString() + " : " + properties.get(prop));
        }
        Assert.assertNotNull(properties);
    }
```

## 1.3 프로토타입과 스코프
스프링이 관리하는 빈은 보통 `싱글톤` 오브젝트로 생성된다. 멀티스레드 환경에서 여러 요청에 사용되는 싱글톤 빈은 `상태값`을 갖으면 안된다.
가끔은 싱글톤 대신 하나의 빈을 여러개 만들어야 할 때가 있는데 그때 `프로토타입`으로 생성하면 된다! <br />
싱글톤이 아닌 빈은 두 가지로 나뉜다.
스코프 : 스프링 컨테이너가 관리하는 빈 오브젝트의 생명 주기를 나타낸다. 싱글톤 스코프의 경우에는 컨테이너의 생성과 동시에 생성돼 종료할 때 같이 
소멸되므로 스프링컨테이너와 생명주기가 같다.그래서 컨테이너 스코프라고도 한다.
1. 프로토타입
2. 스코프 빈

### 1.3.1 프로토타입 스코프
프로토타입으로 설정된 빈은 컨테이너에 DI/DL을 요청하면 매번 새로운 빈을 만들어 반환한다. <br />
1. HelloConfig 설정 클래스에 `@Scope("prototype")`를 추가한다.
```
    @Bean
    @Scope("prototype")
    public Printer printer() {
        return new StringPrinter();
    }
```
2. 싱글톤 테스트
예전에 작성했던 싱글톤 테스트가 실패한다. 
`printer()`로 호출하던 빈이 싱글톤이 아니기 때문에
hello,hello2이 호출하는 각 2번의 빈 요청에서 2개의 새로운 `Printer`의 구현체를 생성 했기 때문이다. <br />
(다시 스코프 제거) 
```
    @Test
    public void helloConfigSingleTonTest(){
        ApplicationContext context = new AnnotationConfigApplicationContext(
            HelloConfig.class);
        Hello hello = context.getBean("hello", Hello.class);
        Hello hello2 = context.getBean("hello2", Hello.class);

        Assert.assertNotSame(hello,hello2);
        Assert.assertSame(hello.getPrinter(),hello2.getPrinter());
    }
```

### 프로토타입 빈의 생명주기와 종속성
프로토타입의 빈은 IoC의 기본 원칙을 따르지 않는다. 스프링 컨테이너는 프로토타입 빈을 생성하고 DI를 하면,더이상 관리하지 않는다.
DI 이후에는 DI를 받은 `클라이언트 오브젝트`에서 제공받은 `프로토타입 빈`을 관리한다. 그리하여 `프로토타입 빈`은 제공 받은 `클라이언트 오브젝트`에 종속된다.
클라이언트 오브젝트가 `싱글톤`이라면 `프로토타입 빈`의 생명주기 또한 싱글톤과 같게 컨테이너의 종료와 함께 소멸될 것이다.

### 프로토타입빈의 용도
서버가요청에 따라 독립적으로 오브젝트를 사용해야 한다면 도메인 오브젝트나 DTO를 사용하면 된다.
만약, DI가 필요하면서, 매번 새로운 오브젝트가 필요하다면 `프로토타입`으로 빈을 만드는게 유용하다.<br />
매번 새로운 요청에 생성되어야 하는 `Request`라는 빈이 있는데, 그 빈이 DI를 필요로 한다면 스프링이 관리하는 IoC빈으로 등록되어야 한다.
하지만 여러 요청을 처리하는 웹 환경에서는 상태값을 갖을 수 없기 때문에 싱글톤으로는 등록될 수 없다. 매번 생성되면서 DI를 받아야 한다면
등록될 빈(Request)도 스프링이 관리하는 빈으로 등록하면서 스코프를 `prototype`으로 설정해주면 된다. 또 한 가지 문제가 있다면 
`Request`객체를 사용하는 `Controller` 역시 싱글톤 빈으로 등록된다. 클라이언트 오브젝트의 생명주기가 싱글톤이라면 주입되는 
`prototype`의 빈 역시도 같은 생명주기를 갖게 되기 때문에 하나의 객체를 공유하면서 데이터를 덮어 씌우는 문제가 발생한다.
그러므로 `prototype`의 빈을 사용하려면 `DI`가 아닌 `DL` 방식을 사용해야 한다.

<br />`P161 ~ `내용을 꼭 다시 읽자 <br /> 

- Xml에서 ` <bean id="생략" class="생략" scope="prototype"/>`
- 자바 코드에서 `@Scope("prototype")` 

### DI(Dependency Injection)와 DL(Dependency Lookup)

### 프로토타입 빈의 DL 전략
1. ApplicationContext, BeanFactory
- @AutoWired나 @Resource를 이용해 `ApplicationConetxt`,`BeanFactory`를 받아와 `getBean()`으로 빈을 가져오는 방식
- 코드에 스프링 API가 나타난다는 단점이 있다.
2. ObjectFactory, ObjectFactoryCreatingFactoryBean
스프링 컨테이너와 클라이언트 오브젝트 사이에 `Object Factory`를 이용해서 프로토타입 빈을 얻는 방식
    1. ObjectFactory
        - `ObjectFactory`인터페이스는 타입 따라미터와 getObject()를 갖고 있다.
        - `ApplicationContext`나 `BeanFactory`만틈 로우 레벨의 API를 사용하는 것이 아니라 좀 더 깔끔하다.
        - `ObjectFactoryCreatingFactoryBean`라는 구현체를 제공해주기 때문에 직접 인터페이스를 구현할 필요는 없다.
        - 빈으로 등록된 `ObjectFactory`를 사용하는 클라이언트 오브젝트는 `@Resource`를 통해서 빈을 주입받는 것이 좋다.
        ```
         <bean id="objFactory"
           class="org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean">
           <property name="targetBeanName" value="${protoTypeBean}"/>
         </bean>
       ```
       - 자바 코드로 설정이 가능하다 (생략)
       - DL
       ```
        @Resource
        private ObjectFactory<ProtoTypeBean> typeBeanObjectFactory;
    
        public void setUp() {
            ProtoTypeBean object = typeBeanObjectFactory.getObject();
        }
        ```
   2. ServiceLocatorFactoryBean
        1. `Request`를 반환하는 메서드가 정의된 인터페이스를 만든다.
           ```
            public interface ServiceRequestFactory {
                ServiceRequest getServiceRequest();
            }
            ```
        2. `ServiceLocatorFactoryBean`를 통해서 빈으로 등록
            ```
            <bean class="org.springframework.beans.factory.config.ServiceLocatorFactoryBean">
              <property name="serviceLocatorInterface" value="springbook.learningtest.hello.ServiceRequestFactory"/>
            </bean>
            ```
        3. 테스트 코드
            ```
              @Autowired
              private ServiceRequestFactory serviceRequestFactory;
          
              @Test
              public void serviceRequestFactoryTest(){
                  ServiceRequest serviceRequest = serviceRequestFactory.getServiceRequest();
                  ServiceRequest serviceRequest2 = serviceRequestFactory.getServiceRequest();
          
                  Assert.assertNotNull(serviceRequest);
                  Assert.assertNotNull(serviceRequest2);
                  Assert.assertNotSame(serviceRequest,serviceRequest2);
              }
            ```
3. 메서드 주입
상위 두 가지 방식은 코드량이 증가하는 단점과 Spring API에 의존하는 단점이 있다.
두 가지 단점을 모두 보안하는 방법이 `메서드 주입`방식이다.
    - 프로토타입 빈을 사용할 클라이언트 오브젝트에 `abstrac`로 프로토타입 빈을 불러들이는 메서드를 정의한다.
    - xml 설정에서 클라이언트 빈을 등록할때 `<lookup-method name=${methodName} bean=${beanRef}>`로 설정한다.
    - 스프링에 의존적이지 않지만 `단위 테스트`를 진행한다면 오버라이딩을 하는 번거러움이 있다. 

#### 4. Provider<T> : 최신 기술!
- `Provider<T>`는 자바 표준 인터페이스!
    ```
    public interface Provider<T> {
        T get();
    }
    ```
- `@Inject`애너테이션과 함께 쓰는 사용한다.
- JavaEE6 표준 인터페이스이기 때문에 스프링 API보다 호환성이 좋다.
- `ObjectFactory`와 다르게 빈으로 등록해주지 않아도 된다
    ```
       @Inject
        private Provider<ServiceRequest> serviceRequestProvider;
    
        @Test
        public void providerTest(){
            ServiceRequest serviceRequest = serviceRequestProvider.get();
            ServiceRequest serviceRequest2 = serviceRequestProvider.get();
    
            Assert.assertNotNull(serviceRequest);
            Assert.assertNotNull(serviceRequest2);
            Assert.assertNotSame(serviceRequest,serviceRequest2);
        }
    ```
  
## 1.3.2 스코프
싱글톤, 프로토타입를 제외하고 `스코프`라는 4가지 형식으로 나뉘어 진다. <br >
1. 요청 스코프 (Request Scope)
    - 하나의 웹 요청 안에서 만들어지고 해당 요청이 끝날 때 같이 제거된다.
    - 애플라케이션 코드에서 생성한 정보를 프레임워크 레벨의 서비스나 인터셉터에 전달하는 용도로 사용
    - 반대의 경우도 사용할 수 있다.
    - 요청 스코프를 사용하는 컨트롤러단(싱글톤)에서 요청을 받기 전에 요청 스코프로 설정된 빈은 생성되지 않는다.
    DI를 이용한 의존객체 주입을 한다면 초기화 중에서 에러가 발생하기 때문에 꼭 `DL`을 사용해야한다.
2. 세션 스코프， 글로벌 스코프
    - HttpSession와 같은 생명주기를 갖는 빈을 만드는 스코프
    - HttpSession에 저장되는 정보를 모든 계층에서 안전하게 사용할 수 있다.
    - 웹 페이지의 변화에도 세션 스코프 빈은 항상 유지된다.
    - 사용자 별로 생성되기 때문에 데이터가 덮어 씌어지는 것에 안전하다.
3. 어플리케이션 스코프
    - 서블릿 컨텍스트에 저장되는 빈 오브젝트
    - 웹 어플리케이션과 어플리케이션 컨텐스트의 존재 범위가 다를 때 사용
    - 상태가 있더라도 `읽기전용` 만들거나 멀티스레드 환경에 안전하게 설계해야한다.

### 스코프빈의 사용방법
- 요청 스코프, 세션 스코프 글로벌, 글로벌 스코프는 프로토타입과 마찬가지로 `1개 이상`의 빈이 생성된다.
- 프로토타입 빈과 다르게 스프링 컨테이너가 생성 후에도 제거까지 모든 과정을 관리해준다.
- 프로토타입 빈과 마찬가지로 `DI`가 아닌 `DL` 방식으로 빈을 사용해야한다.
- 스코프 빈을 `DI`방식으로 사용하려면 `프록시`를 이용해야한다.
    ```
    // 자바코드 설정
    @Scope(value="session" , proxyMode=ScopedProxyMode.TARGET_CLASS)
    public class SometingObj { ... }
  
    // XML 설정
    <bean id =${id} class=${class..}>
        <aop:scoped-proxy proxy-target-class="true" />
    </bean>
    ``` 
- `프록시`를 이용해서 `DI`를 사용한다면 스코프 빈을 싱글톤 빈 마냥 편하게 쓸 수 있다.

### 커스텀 스코프와 상태를 저장하는 빈 사용하기
- `싱글톤`이외의 스코프를 사용한다는 것은 오브젝트에 상태를 저장하고 사용한다는 뜻!
- `싱글톤`을 사용하면서 상태 값을 저장해야 한다면 세션, 쿠키, 파라미터 등을 이용해서 자료값을 관리/저장해야한다.

## 1.4.1 빈 이름
- `name`은 여러가지를 지정할 수 있다. 형식도 다향해서 심지어 한글도 가능
- `id`는 한 가지만 지정할 수 있다. `.`,`_` 두 가지 허용한다.
- 자동 스캔으로 등록한 빈에 이름 설정
    - @Component("myUserService")
    - @Named("myUserService")  
- 자바 코드에서 name 설정하는 방법은 아래와 같다
    - @Bean(name="myUserDao")

## 1.4.2 빈 생명주기 메서드
초기화 메서드 :빈 오브젝트가 생성되고 DI까지 마친 후에 실행되는 메서드 <br />
오브젝트의 기본적인 초기화 작업은 생성자가 담당하지만, DI후에 해야하는 작업도 있다. 이럴때 초기화 메서드를 이용한다.

1. 초기화 콜백 인터페이스
    - `InitializingBean` 인터페이스를 구현한 빈을 작성하는 방법
    - `afterPropertiesSet()`메서드를 이용해 초기화 작업을 한다.
    - 프로퍼티 작업을 마친 후, 실행된다.
    - 클래스에 스프링 인터페이스를 노출시키기 때문에 `권장하지 않는다`
2. init-method
    - `@Bean`애노테이션을 이용한 빈이라면 `@Bean(init-method="init")`이렇게 등록한다.
    - 빈이 생성되고 DI된 후에 실행될 메서드를 직접 지정해준다.
        ```
      @Component
      @Scope("prototype")
      public class ServiceRequest {
      
          private String name;
          public void init() {
              this.name = "youzheng";
          }
      }
      ```
    -  <bean id="serviceRequest" class="springbook.learningtest.hello.ServiceRequest"
          init-method="init"/> 이처럼 설정한다.
3. 테스트
    ```
        @Test
        public void initMehtodTest(){
            GenericXmlApplicationContext context = new GenericXmlApplicationContext(
                "/vol2/helloAppContext.xml");
            ServiceRequest request = context.getBean("serviceRequest", ServiceRequest.class);
            Assert.assertNotNull(request);
            Assert.assertEquals("youzheng",request.getName());
        }
   ```
4. @PostConstruct
    - `@PostConstruct`는 자바 표준 애노테이션 (Spring에 종속되는 기능이 아님)
    - `init-method`보다 직관적이다.
    - `제일 권장하는 방식`
    
### 제거 메서드
컨테이너가 종료될 때 리소스를 반환하거나 처리해야할 작업이 있을 때 사용된다.
1. 제거 콜백 인터페이스 : `DisposableBean`를 구현
2. `destroy-method` : Xml으로 빈 등록시 <bean id=${id}...destroy-method=${methodName} >
3. `@PreDestroy` : 클래스에 있는 제거용 메서드에 추가하는 방식
4. @Bean(destroyMethod=${methodName})

## 1.4.3 팩토리 빈과 팩토리 메소드
빈 팩토리 : 생성자 대신 코드의 도움을 받아서 빈 오브젝트를 생성한다. 빈 팩토리랑 다르다! <br />
- 빈 팩토리는 자신을 빈 오브젝드로 사용하지 않는다. 빈 오브젝트를 생성해주는 것만 한다.

1. `FactoryBean`인터페이스
    -  new 키워드나 리플렉션 API등 생성자를 통해서 만들 수 없는 오브젝트를 빈으로 등록하기 위해 사용
    `FactoryBean`를 구현한 후, 빈으로 등록해 사용하는 방식
        ```
        public interface FactoryBean<T> {
            T getObject() throws Exception;
            Class<?> getObjectType();
            boolean isSingleton();
        }
        ```
2. 스태틱 팩토리 메서드
    - 오브젝트 생성과 함께 초기화 작업이 필요할 때 사용
    - <bean id="counter" class="class.." factory-method=${factoryMehtodName} />
3. 인스턴스 팩토리 메서드 
    - <bean id="log" factory-bean="logFactory" factory-method='createLog" />
    - `이해가 잘 되지않는다. 따로 알아보자`
4. @Bean 메서드
    - 메서드를 @Bean으로 등록하는 방법

## 1.5 스프링3.1의 Ioc 컨테이너와 DI

## 1.5.1 빈의 역할과 구분
### 빈의 종류
1. 어플리케이션 로직 빈
    - 일반적으로 스프링 컨테이너에게 관리되는 빈
    - 일반적으로 어플리케이션의 로직을 담고 있다. 
    - Dao,Service, Controller 등등 모두 `어플리케이션 로직 빈`이다.
2. 어플리케이션 인프라 빈
    - 어플리케이션 로직 빈을 지원하는 빈(DataSource)
    - 어플리케이션의 로직을 담당하지는 않는다.
    - 스프링 또는 외부에서 만들어진 인터페이스를 사용한다.
    - 외부 라이브러리
    - TransactionManager, DataSource 등등이 `어플리케이션 인프라 빈`에 속한다.
3. 컨테이너 인프라 빈
    - 스프링 컨테이너의 기능을 확장시키는 것에 참여하는 빈
    - 빈의 등록, 생성, 관계설정, 초기화 작업 등에 사용된다.
    
### 컨테이너 인프라 빈과 전용 태그
- 컨테이너 인프라 빈은 DefaultAdvisorAutoProxyCreator 처럼 <bean> 태그를 이용해 직접 등록 가능
- <context:component-scan > 등록시 함께 등록되는 빈
    - @PostContruct, @Configuration, @Bean, @AutoWired 등의 애노테이션은 스프링이 직접 등록해주는 기능이 아니다.
    - `<context:component-scan >`를 등록해야지 사용할 수 있다.
        - 함께 등록되는 빈
        - ConfigurationClassPostProcessor$ImportAwareBeanPostProcessor
        - ConfigurationClassPostProcessor : @Configuration과 @Bean을 담당
        - AutowiredAnnotationBeanPostProcessor : @AutoWired
        - RequiredAnnotationBeanPostProcessor
        - PersistenceAnnotationBeanPostProcessor
        - 대부분 후처리기 기능
        
### 빈의 역할
1. ROLE_APPLICATION : 어플리케이션 작동중에 사용되는 빈
    -  애플리케이션을 구성하는 빈
2. ROLE_SUPPORT :  복합 구조의 빈을 정의할 때 보조적으로 사용되는 빈의 역할을 지정하려고 정의된 것
    - 거의 사용되지 않는다 무시해도 좋다고 하십니다.
3. ROLE_INFRASTRUCTURE : 전용태그에 의해서 등록되는 빈
    - <context:annotation-config > , <context:component-scan > 등등
    - 컨테이너 인프라빈이 여기에 속한다.
스프링 3.1 부터는 `@Beam`애노테이션을 이용해 개발자가 직접 빈의 역할을 지정해줄 수 있다.

### 1.5.2 컨테이너 인프라 빈을 위한 자바 코드 메타정보
생략... P198 ~ 
### 자바 코드를 이용한 컨테이너 인프라 빈 등록

1. `@ComponentScan`
    - `@ComponentScan(${pakage})` : 등록된 패키지를 기준으로 하위 패키지 탐색
    - `@ComponentScan(basepackageClasses = ${MarkerInterface.class})` : 마커용 클래스를 기준으로 탐색
    - `@ComponentScan(basePackage=${package}, excludeFilters=@Filter(Cofuguration.class))` : <br />`Cofuguration`가 붙은 클래스를 컴포턴트 스캔하지 않는다.
    - `@ComponentScan(basePackage=${package}, excludeFilters=@Filter(type lterType.ASSIGNABLE_TYPE, value=${class}))` :<br /> 특정 클래스를 컴포턴트 스캔하지 않는다.
2. `@Import`
    - @Configuration이 붙은 빈 설정 클래스를 임포트할 때 사용한다.
    - 7장에서 다시소개하신다고 한다.
3.  `@ImportResource`
    - `@ImportResource(${xmlFileLocation})` : xml형식의 빈 설정을 임포트할 때 사용
4. `@EnableTransactionManagement`
    - <tx:annotation-driven/ >와 동일하다.
    - `@Transactional`을 사용가능하게 해준다.

## 1.5.3 웹 어플리케이션의 새로운 IoC 컨테이너 구성
웹 환경은 보통 `루트 어플리케이션 컨텍스트`와 `서플릿 어플리케이션 컨텍스트`로 나뉜다 <br />
직접 책을 읽자! P.206 ~ 
- 루트 어플리케이션 컨텍스트의 디폴트 클래스는 : XmlConfigApplicationContext이므로 @Configuration이 붙은 설정 클래스를 사용하려면 
`contextClass`를 `AnnotationConfigWebApplicationContext`로 변경하고 `contextClassLoacation`에 위치를 설정해야한다.

## 1.5.4 런타임 환경 추상화와 프로파일
1. 빈 설정 파일의 변경
    - 매번 직접 설저 파일을 변경하는 것은 바람직하지 못하다..

2. 프로퍼티 파일 활용
    - 1.2.4 참고
    - 변화하는 환경에 따른 값을 프로퍼티를 변경함으로써 대응한다.
3. `이해 못함`

### 런타임 환경과 프로파일
컨텍스트 내부에 `Envìronment`인터페이스를 구현한 런타임 환경 오브젝트가 만들어져서 빈을 생성하거나 DI할 때 사용한다.<br />
런타임 환경은 `프로파일`과 `프로퍼티 소스`로 구성된다. 환경에 따라 다르게 구성된 빈들을 다른 이름을 가진 `프로파일`안에 정의한다. <br />
각자 다른 환경에 따라 어플리케이션 컨텍스트가 초기화될 때 각자 다른 프로파일을 사용하여 각자 다른 빈들을 만드는 것이다.<br />
1. Xml <beans>안에 1개 이상의 <beans profile=${name}> 을 지정할 수 있다.
```
 <beans>
      <beans profile="dev">
            <bean id="devDataSource"...></bean>
      </beans>
            <bean id="devDataSource"...></bean>
    </beans>
```
2. 자바 코드에서 프로파일 설정
    - Xml과 마찬가지로 `@Cofiguration`이 붙은 클래스 내부에 `@Cofiguration`를 지정한 클래스로 설정한다.  
3. 활성화 방법
    1. @ActiveProfile(${prifileName})
    2. 루트 어플리케이션 컨텍스트 <context-param>
        ```
        <context-param>
        <param-name>spring.profiles.active</param-name> 
        <param-value>${prifileName}</param-value>
        </context-param>
       ```
    3. 서블릿 컨텍스트
        ```
       <init-param>을 사용하하고 2번과 같다.
       ```
    4. `-Dspring.profiles.active=dev` : 시스템 프로퍼티를 변경 
    5. ApplicationConetext를 사용해서 프로파일을 활성화 하려면 `.getEnvironment() ,setActiveProfiles(${profileName});`

## 1.5.5 프로퍼티 소스
- 런타임 환경 마다 변화되는 값은 프로퍼티로 저장한다.
- DB정보와 같은 정보는 외부 리소스로 저장하여 값을 불러들이도록 설계한다

### 프로퍼티
- 한 개의 키와 대응하는 값으로 이루어져 있다.
- 프로퍼티 파일은 텍스트 파일로 포맷 되어 있다.

1. 자바에서 프로퍼티 파일을 불러들이는 방법
    ```
        @Test
        public void propertiesLoadFIleTest() throws IOException {
            Properties prop = new Properties();
            prop.load(Resources.getResourceAsStream("database.properties"));
            for(String name : prop.stringPropertyNames()){
                System.out.printf("%s : %s \n   ",name, prop.get(name));
            }
        }
    ```
2. xml파일에 properties파일을 불러들이고 빈으로 등록
    ```
      <util:properties id="database" location="database.properties" />
   ```
3. `<context:property-placeholder >`을 이용한 등록
    ```
   <contexxt:property-placeholder location=${filePath} />
   ```

### 스프링에서 사용되는 프로퍼티 종류
1. 환경변수
    - 자바에서는 `System.getEnv()`메서드로 환경변수 값을 가져올 수 있다.
    - 스프링에서는 환경변수 값을 프로퍼티 형식으로 얻어올 수 있다. `systemEnviroment`이름의 프로퍼티 타입으로 등록되어 있음
        ```
       @Resource // 빈 이름으로 검색해야한다.
      private Properties systemEnviroment;
      ```
2. 시스템 프로퍼티
    - JVM 레벨에 정의된 프로퍼티
    - 1번 환경변수와 마찬가지로 얻어올 수 있다.
3. JNDI - 완전 모르는 내용이라 생략..

4. 서블릿 컨텍스트 프로퍼티
    - web.xml에 서블릿 컨텍스트를 초기화하면서 설정한 값 `<context-param>`
    - ServletContext 오브젝트를 직접 받아오면 해당 값을 얻을 수 있다. `getInitParameter()`
    -  `ServletContextPropertyPlaceholderConfigurer`
5. 서블릿 컨픽(Config) 파라미터
    - 서블릿 컨텍스트 : 서블릿이 소속된 어플리케이션 컨텍스트
    - 서블릿 컨픽 : 개별 서블릿을 위한 설정
    - ServletConfigAware 인터페이스를 구현하거나 @Autowired 로 주입받아서 `getInitParameter()` 메서드로 얻는다.

### 프로퍼티 통합과 추상화
- `Environment`타입의 런타임 오브젝트를 이용해 일관된 방식으로 프로퍼티를 얻을 수 있다. <br />
- `StandardEnvironment`는 `GenericXmlApplicationContext`나 `AnnotationConfigApplicationContext` 처럼 독립형
어플리케이션용 컨텍스트에서 사용되는 런타임 환경 오브젝트다.
- `시스템 프로퍼티`와 `환경변수 프로퍼티`를 제공한다. 우선순위: 시스템 > 환경변수
- ApplicationContext의 `getEnvironment()`를 통해서 `Properties`타입을 추가하거나 기존의 등록된 값을 얻을 수 있다.
- `Environment`의 `addFirst()`,`addLast()`,`addBefore()`,`addAfter()` 메서드를 통해서 우선순위를 조절하며 값을 추가할 수 있다.

### 프로퍼티 소스의 사용
1. `Environment.getProperty()`
    - `@AutoWired`를 통해서 `Envioronment`를 받아와 프로퍼티를 얻을 수 있다.
    - `@PostConturctor`을 통해서 런타임 시 프로퍼티를 가져와 값을 넣을 수 있다.
2. `PropertySourceConfigurerPlaceholder`와 `<context:property-placeholder >`
    - `PropertySourceConfigurerPlaceholder`를 등록하면 `${}`치환자를 사용하여 값을 불러올 수 있다.
    - `attribute`로는 `location=${propertyFileLocation}`을 준다. 해당 프로퍼티 파일의 값을 등록해준다. 
    - 자바 코드로 등록시 빈을 `static`으로 설정해야 런타임 시에 다른 빈에게 값을 넘겨줄 수 있다.
    - 어플리케이션 컨텍스트는 `static`이 붙은 메서드를 우선으로 만들기 때문에 꼭 static으로 설정!
    - xml을 이용하여 설정할 시에는 `<context:property-placeholder />`을 추가해야 사용할 수 있다.
    
### @PropertySource와 프로퍼티 파일
- 프로퍼티 파일도 프로퍼티 소스로 등록하여 사용할 수 있다.(시스템, 환경변수 이외의 값을 추가할 수 있다.)
- @PropertySource(name = ${name}, value=${XXXX.properties,XXXX.properties}) 이름 저장할 수 있고, 다수의 값을 넣을 수도 있다.

### 웹 환경에서 사용되는 프로퍼티 소스와 프로퍼티 소스 초기화 오브젝트
- 루트 웹 컨텍스트나 서블릿 웹 컨텍스트에 의해 만들어지는 웹 어플리케이션 컨텍스트는 `StandardServletEnvironment` 런타임 환경 오브젝트를 사용한다.
- 우선순위 순서 : 서블릿 컨픽 컨텍스트 > 서블릿 컨텍스트 > JNDI 컨텍스트 > 시스템 > 환경변수 <- 다섯가지는 기본적으로 등록된다.
- `spring.profiles.active`라는 키를 찾아서 값을 사용하는 것이다.
- 웹 환경에서 `@PropertySource`를 추가한 프로퍼티가 `우선순위가 가장 낮다`.
- `ApplicationContextInitializer`인터페이스의 구현체를 이용하여 프로퍼티 소스를 추가한다.
```
package org.springframework.context;

public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {
    void initialize(C var1);
}
```
- `ApplicationContextInitializer`의 구현체
```
    public class applicationContextInitializer implements ApplicationContextInitializer {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            ConfigurableEnvironment evn = configurableApplicationContext.getEnvironment();
            /*
            * 추가할 프로퍼티 파일을 불러들이는 작업
            * */
            evn.getPropertySources().addFirst(${추가할 프로퍼티});
        }
    }
```
- 루트 컨텍스트나 서블릿 컨텍스트에 추가한다.
    - 루트 컨텍스트라면 `<context-param>`을 이용하여 추가, 서블릿 컨텍스트라면 `init-param`을 이용하여 추가한다.

# 2장 데이터 엑세스 기술

## 2.1.1 DAO 패턴
- 엔터프라이즈 어플리케이션이라면 `데이터 엑세스 계층`을 `Dao 패턴`으로 분리한다.
- Dao는 구현 기술에 대한 정보를 외부에 공개해서는 안 된다.
- 기술에 종속되지 않고 계발한 Dao인터페이스는 기술과 상관없이 단순한 DTO나 Mock오브젝트로 단위테스트를 작성할 수 있다.

### DAO 인터페이스와 DI
- Dao는 인터페이스를 이용해 DI되도록 만들어야 한다.
- 구체적인 내부 구현은 `외부로 노출하지 않는다`
- 인터페이스에 정의된 퍼블릭 인터페이스를 제외하고 직접 퍼플릭 인터페이스를 추가하면 안 된다.

### 예외처리
- Dao 메서드 선언부에 `throw XXXXException` 기술 내부의 예외를 노출시키면 안 된다.
- Dao 내부의 메서드의 예외는 모두 `RuntimeException`으로 설계한다. Dao를 사용하는 서비스 계층에서는 Dao의 예외를 처리할 이유가 없다.
- `중복아이디` 등등 서비스 계층에서 처리해야할 예외들도 존재한다. `낙관적인 락킹`
- 하지만 데이터 엑세스 기술에 따라서 발생시키는 `Exception`이 다르기 때문에 서비스 계층에 `Exception`에 대한 구체적인 정보를 알고 있어야 한다.
- 스프링에서는 이러한 `Eception`을 추상화하여 제공한다.
- 이러한 예외를 스프링 데이터로 변환한다.

### 2.1.2 템플릿과 API
- 스프링의 데이터 기술은 `템플릿/콜백 패턴`을 사용하여 제공한다.
- 미리 만들어진 템플릿은 반복되는 코드를 줄여준다. ex. try~catch~finally
- 또한 예외변환과 트랜잭션 동기화 기능도 제공해준다.
- 템플릿의 단점 : 
    - 템플릿이 제공하는 API에 종속되는 문제
    - 콜백 오브젝트를 익명 클래스로 작성하여 코드 이해가 조금 난해하다.
- 하지만 스프링이 제공하는 `내장 콜백`을 이용하면 된다.

### 2.1.3 DataSource
- 풀링 기법 : 미리 정해진 개수의 DB 컨넥션을 만들어 놓고, 어플리케이션의 요청에 따라 하나씩 할당해주고 돌려받는 방식
- DataSource는 특정 기술에 종속되면 되면 안 되기 때문에 하나의 `독립적인 빈`으로 등록해야한다. 

### 학습 테스트와 통합 테스트를 위한 DataSource
- SimpleDriverDataSource
    - 스프링에서 제공하는 가장 단순한 DataSource 구현체
    - `getConnection()` 메서드를 통해서 매번 새로운 컨넥션을 생성
    - 따로 컨넥션을 관리하지 않는다.
    - 오직 `테스트용`
- SingleConnectionDataSource
    - 하나의 물리 DB Connection을 만들어서 사용
    - 그래도 하나라도 만들넣고 사용해서 SimpleDriverDataSource보다는 빠르다

### 오픈소스 또는 상용 DB 컨넥션 풀
직접 찾아보자

## 2.2 JDBC
JDBC : 자바의 데이터 엑세스 기술 중 `가장 로우 레벨`의 API <br />
1권에서 많이 공부해서 생략하신다고 한다.

## 2.2.1 스프링 JDBC 기술과 동작원리
JdbcTemplate 인터페이스 역시 여러가지 구현 클레스가 존재한다. 아래 두 가지는 가장 쉽고 자주 사용되는 구현 클래스
- SimpleJdbcTemplate <br />
JDBC의 모든 기능을 최대한 활용할 수 있는 유연성을 갖고 있다.
- SimpleJdbcInsert. SimpleJdbcCall <br />
DB가 제공해주는 메트 정보를 이용하여 컬럼정보와 파라미터 정보를 가져와 삽입용 Sql과 프로시저 호출작업에 사용해준다.

### 스프링 JDBC가 해주는 작업
- Connection 열기/닫기
- Statement 준비와 닫기
    - Sql을 담을 Statement 또는 PreparedStatement를 생성 
- Statement 실행
    - Sql이 담긴 Statement를 실행
- ResultSet 루프
    - ResultSet의 루프를 만들어 다수의 커리 결과를 처리해준다.
    - 스프링 JDBC가 미리 준비해둔 포맷 또는 오브젝트를 사용하면 편리하다.
- 예외처리와 변환
    - JDBC를 이용한 작업에서 발생하는 예외를 스프링의 예외 변환기가 처리한다.
    - SqlException을 `DataAccessException` 타입으로 변환해준다.
- 트랜잭션 처리
    - ㅇ
## 2.2.2 SimpleJdbcTemplate
-  실행, 조회, 배치 세 가지 기능으로 구분

### SimpleJdbcTemplate 생성
- DataSource를 파라미터로 받아서 생성
- 애노테이션을 이용해 오토 와이어링하는 방식을 이용
- Dao(JdbcTemplate를 사용하는 객체)에서 DataSource를 주입받아 JdbcTemplate의 구현체를 인스턴스화 
하도록 설계한다. Dao는 DataSource를 주입 받아 직접적으로 사용하는게 아니라, JdbcTemplate를 생성하기 위해서 주입받는다.
```
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
```
### SQL 파라미터    
- `?` 를 이용한 치환자 뿐만 아니라 `:${columnName}`을 이용해서도 값을 지정할 수 있다.
- Map/MapSqIParameterSource
    - Map에 데이터 값을 넣어 파라미터로 넘기는 방식
    - 맵의 key가 컬럼이름이 되고 value가 들어갈 데이터가 된다.
    - 메서드 체인 형식을 지원하기 때문에 코드가 깔끔해진다.
- BeanPropertySqlParameterSource
    - `도메인 오브젝트`나 `DTO`를 사용할 수 있게 해준다.

### SQL 실행 메소드
- varargs
    - `?` 치환자를 이용한다면 순서를 지커야한다.
    - `:` 치환자를 이용한다면 순서를 알아서 바인딩 해준다.
    - 가변인지 이므로 파라미터가 없다면 생략할 수 있다.
- Map
    - Sql Query에서 `Values(${:치환자를이용})`로 설정하고 Map을 통해 값을 넘긴다.
    - key와 value를 기준으로 자동 바이딩하여 쿼리를 실행

### SqlParameterSource
- `BeanPropertySqlParameterSource`를 이용해 `도메인 오브젝트`나 `DTO`를 `Update`한다.
- Sql실행으로 영향받은 레코드의 수를 반환해준다.

### SQL 조회 메소드
- queryForInt
    - 하나의 Int 타입 값을 조회할 때 사용
    - 쿼리 실행후 결과가 `2개 이상`이라면 `예외발생`
```
    public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException {...}
```
- queryForLong
    - `queryForInt`의 Long 버젼
```
    public long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException {...}
``` 
- queryForObject
    - 하나의 값을 가져올 때 사용한다.
    - `단일 컬럼`의 값을 `Class<T> requiredType`으로 파리미터에 받을 수 있다. ex) String.class 
    ```
        public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
            return this.queryForObject(sql, this.getSingleColumnRowMapper(requiredType));
        }
    ```
    - `RowMapper<T> rowMapper`를 이용해 `단중 컬럼`의 값을 저장할 수 있다. 
    - 콜백 오브젝트로써 직접 구현해야한다. 
    ```
        public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {...}
    ```
    - Sql 실행 후, 결과의 컬럼과 값을 담을 Object의 프로퍼티가 일치한다면 
    `BeanPropertyRowMapper<T>`를 이용하여 자동으로 값을 바인딩할 수 있다.
- query
    - 컬럼의 값을 RowMapper을 이용하여 저장한 후 `List`로 반환해준다.
    ```
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {...}
    ```
- queryForMap
    - Sql 실행 후, 결과 값을 column-> key, data -> value로 바인딩하여 반환한다.
    ```
      public Map<String, Object> queryForMap(String sql) throws DataAccessException {...}
  ```
- queryForList
    - `query`와 `queryForMap`의 다중버전
    ```
      public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException {...}
  
      public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {...}
     ```
### SQL 배치 메소드
- SQL 배치 메소드는 update( )로 실행하는 SQ뚫을 배치 모드로 실행하게 해준다.
- 한번에 많은 Sql(update,insert)를 할 때 사용한다.
- batchUpdate
    - 데이터 타입에는 아래와 같은 파라미터를 넣을 수 있다.
    - List<Object[]>
    - SqlParameterSource[], List<SqlParameterSource>
    ```
      public int[] batchUpdate(final String[] sql, ${데이터타입}) throws DataAccessException {...}
  ```
### 2.2.3 SimpleJdbcInsert
P256

### SimpleJdbcInsert 실행
- execute()
    - 파라미터로 `Map<String, Object>`과 `SqlParameterSource` 타입 가능하다.
```
    public int execute(Map<String, Object> args) {...}

    public int execute(SqlParameterSource parameterSource) {...}
```
- executeAndReturnKey()
    - Sql Query 실행후 자동으로 생성되는 값이 있다면 반환 ex)MySql의 `Auto_Increment`
    - `usingGeneratedKeyColumns()`를 이용해 초기화 과정에서 자동생성 컬럼을 지정해야한다. 
    ```
            public Number executeAndReturnKey(Map<String, Object> args) {
                return this.doExecuteAndReturnKey(args);
            }
    ```
    - `Number` 타입으로 반환한다. 메서드 마지막에 메서드 체인형식으로 리턴 형식을 지정할 수 있다. ex).intValue();
-  executeAndReturnKeyHolder()
    - 쿼리 실행후 자동으로 생성되는 값이 두 개 이상일 때 반환 값을 받아준다.
    - 반환 값은 `KeyHolder`으로 맵핑된다.
    ```
    public interface KeyHolder {
        Number getKey() throws InvalidDataAccessApiUsageException;
        Map<String, Object> getKeys() throws InvalidDataAccessApiUsageException;
        List<Map<String, Object>> getKeyList();
    }
    ```
## 2.2.4 SimpleJdbcCall
DB에 저장된 `프로시저` 또는 `함수`를 호출하는 기능 <br/ >
프로시저를 잘 몰라서 생략

## 2.2.5 스프링 JDBC DAO
Spring에서 Dao 설계
- 일반적으로 한 테이블에 한 개의 Dao를 설계한다.
- `DataSource`에만 의존하도록 설계
- `JdbcTemplate`를 Dao에 인스턴스 변수로 설정하고 Dao마다 새로운 로직을 직접 설정
- Dao는 DB Connection 정보를 담고 있는 `DataSource`를 주입받아 직접 사용하지 않고 
`SimpleJdbcTemplate`,`SimpleJdbcInsert`,`SimpleJdbcCall`등을 생성할 때 사용하기 위해 주입받는다.
- `SimpleJdbcTemplate` 역시 독립적인 인터페이스로 분리하여 구현해도 좋다.

# 2.3 iBatis ~ 2.6 JPA,하이버네이트 전부 생략
ibatis는 오래된 기술이라, <마이바티스 프로그래밍>으로 대체 <br />
JPA 다른 책으로 대체 <br />

### 2.6.1 트랜잭션 추상화와 동기화 

~

## 3.2.1 간단한 스프링 웹 프로젝트 생성

### 루트 웹 애플리케이션 컨텍스트

## 3.6.1 플래시 맵 매니저 전략
플래시 애트리뷰트를 저장하는 맵, 하나의 요청에서 생성된 후 다음 요청으로 전달되는 정보 <br />
- 보통 `Post요청`을 처리하는 컨트롤러에서 생성된다.
- `FlashMap` 오브젝트를 생성해 Map오브젝트, 시간설정, DirectUrl을 설정할 수 있다.
    ```
          FlashMap flashMap = new FlashMap();
          flashMap.put("message","Hi");
          flashMap.setTargetRequestPath("/hello/result");
          flashMap.startExpirationPeriod(10);
  ```
### 플래시맵 매니저
- `FlashMapManager`을 통해서 `FlashMap` 오브젝트를 저장할 수 있다.
- request가 일종의 `KEY`로 사옹되는 것 같다.
    ```
          RequestContextUtils.getFlashMapManager(request)
              .saveOutputFlashMap(flashMap, request, response);
  ```
- `FlashMap`을 찾을 때는 `getInputFlashMap(${request})`를 이용하여 `Map<String,Object>` 반환 받을 수 있다.

## 3.6.2 WebApplicationInitializer를 이용한 컨텍스트 등록
```
public interface WebApplicationInitializer {
    void onStartup(ServletContext var1) throws ServletException;
}
```
- 프레임워크 모듈에서 직접 서블릿 컨텍스트 초기화 작업을 관리하는 방법을 지원한다.
- 서블릿 컨텍스트 초기화 작업에는 `서블릿 등록`, `리스너 등록`, `필터 등록`등 xml이 도맡아 하던 작업들을 말한다.
- 웹 어플리케이션이 실행될 때,`WebApplicationInitializer`구현한 클래스의 `onStartUp()`메서드를 실행시킨다.

### 루트 웹 컨렉스트 등록
- web.xml에서 `Listner`을 이용해 `ApplicationContext`을 생성했던 이유는 이 둘의 생명주기가 일치하기 때문이다.
- `WebApplicationInitializer`를 이용하더라도 `Listner`를 통해서 관리되도록 설정해야한다. 종료될 시점을 놓칠 수도 있다.(리소스 반환 등등)
- `WebApplicationInitializer`의 메서드에 들어온 `ServletContext`에 값을 넘겨주는 식으로 이용하여 등록한다.
- 자세한 내용 구글링 너무 길다.
