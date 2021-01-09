# Toby Spring vol.1
토비의 스프링 vol.1 2회차

## 1.2.1~ 1.2.2
### UserDao의 관심사 및 분리
1. DB와 연결을 위한 컨넥션
2. DB에 보낼 SQL 문장을 담을 Statement를 만들고 실행하는 
   것
3. 리소스 반환
 ### DB Connection을 분리한다.
1. get/add 메서드에 중복되는 코드
```
        Class.forName("org.h2.Driver");
        Connection c = DriverManager.getConnection(
            "jdbc:h2:tcp://localhost/~/test", "sa", "");
```
메서드로 추출(`extract method`)
```
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection(
            "jdbc:h2:tcp://localhost/~/test", "sa", "");
    }
```

## 1.2.3 DB connection 관심사를 상속을 통하여 분리
```
public abstract class UserDao {
    public abstract Connection getConnection() throws ClassNotFoundException, SQLException;
    /*
    * 기존의 add/get Mehtod 생략
    */
}
```
슈퍼 클래스에서 기본적의 로직의 흐름(sql생성,리소스반환 등)을 처리하고
서브 클래스에서 이런 메서드를 필요에 맞게 구현해서 사용하도록 하는 방식을
템플릿 메소드 패턴(`template method pattern`) 이라고 한다.
```
public class YUserDao extends UserDao {
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection(
            "jdbc:h2:tcp://localhost/~/test", "sa", "");
    }
}
```
현재와 같이 서브클래스에서 구체적인 오브젝트 생성 방법을 결정하게 하는 것을
팩토리 메서드 패턴(`factory method pattern`)이라고 한다.
서브클래스(YUserDao)에서 Connection 생성에 대한 메서드를 설정하고, 슈퍼클래스(USerDao)에 정의된
구체적인 로직을 이용한다.

## 1.3.1 클래스 분리 
서로 다른 관심사를 클래스 단위로 분리한다.
DB Connection을 생성하는 SimpleConnectionMaker 클래스를 작성
```
public class SimpleConnectionMaker {
    public Connection makeNewConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection(
            "jdbc:h2:tcp://localhost/~/test", "sa", "");
    }
}
```

```
public class UserDao {

    private SimpleConnectionMaker connectionMaker;

    public UserDao() {
        this.connectionMaker = new SimpleConnectionMaker();
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
          Connection c = connectionMaker.makeNewConnection(); // ConnectionMaker을 통해서 Connection을 생성
      // 생략
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeNewConnection(); // ConnectionMaker을 통해서 Connection을 생성
      // 생략
    }

```
현재 UserDao는 Connection을 받아오는 SimplecConnectionMaker에 대해서 구체적으로 알고 있다.
```
public class UserDao {

    private SimpleConnectionMaker connectionMaker;

    public UserDao() {
        this.connectionMaker = new SimpleConnectionMaker();
    }
```

## 1.3.2 인터페이스를 이용한 관심사 분리
1. 어떤 것들의 공통적인 성격을 뽑아내여 이를 따로 분리하는 작업을 추상화라고 한다.
2. 자바가 추상화를 위해 지원하는 가장 유요한 도구는 `인터페이스`
3. `인터페이스`는 자신을 구현한 클래스의 `구현`을 모두 감춘다. Public Interface을 제외한
구현 부분을 메세지를 응답 받는 객체에게 공개하지 않는다.
4. 이로써 실제 구현 부분을 바꿔도 사용하는 객체의 코드에 영향을 미치지 않는다.
```
public interface ConnectionMaker {

    public Connection makeConnection() throws ClassNotFoundException, SQLException;
}
```
ConnectionMaker의 구현체, DB Connection을 생성하는 메서드를 구현
```
public class YConnectionMaker implements ConnectionMaker {

    @Override
    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection(
            "jdbc:h2:tcp://localhost/~/test", "sa", "");
    }
}
```
```
    private ConnectionMaker connectionMaker;

    public UserDao() {
        this.connectionMaker = new YConnectionMaker();
        // 아직도 ConnectionMaker의 구현체에 대해서 구체적으로 알고 있다.
    }
    // get/add 생략
```

## 1.3.3 관계설정 책임의 분리
1. 클래스와 클래스 관계를 설정하지 말고 객체(Object)와 객체 사이 관계를 설정해야한다.
2. 오브젝트사이의 관계는 런타임 시에 한쪽이 다른오브젝트의 레퍼런스를갖고 있는 
   방식으로 만들어진다. `사용`이라는 관계를 만들어 줘야한다.
3. 꼭 사용하는 하는 객체에 사용되는 객체를 만들 필요는 없다.
    ```
    public class UserDao {
        public UserDao() {
            this.connectionMaker = new YConnectionMaker();
            // 아직도 ConnectionMaker의 구현체에 대해서 구체적으로 알고 있다.
        }
    }
    ```
    UserDao(클라이언트)객체 코드 속에 `ConnectionMaker`의 구현체가 생성되고 있다.
4. 메소드 파라미터(생성자,수정자)를 통해서 내부에 전달할 수 있다.
```
public class UserDao {

    private ConnectionMaker connectionMaker;

    public UserDao(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }
```
5. 4번의 방법을 통해서 필요한 인터페이스를 구현한 객체의 메서드를 사용할 수 있다.
구체적인 구현방식과 클래스의 종류를 신경쓸 필요도 없다.
    ```
        private UserDao userDao;
    
        @Before
        public void setUp() {
            this.userDao = new UserDao(new YConnectionMaker());
        }
    ```
    UserDao를 사용하는 클라이언트에서 ConnectionMaker를 생성자를 통해서 주입
    클라이언트(UserDaoTest)는 UserDao와 ConnectionMaker 구현 클래스와의 런타임 오브젝트 의존 
    관계를 설정히는 책임을 담당해야 한다.
6. UserDao와 Connection을 가져오는 객체가 분리 됐다.(관심사) 서로 영향을 주지 않으면서도 자유롭게 확장할 수 있는 구조가 됐다.

## 1.3.4 원칙과 패턴
1. 개방 폐쇄 원칙(Open Close Principle)
    클래스나 모률은 확장에는 열려 있어야 하고 변경에는 닫혀 있어야 한다
2. 높은 응집도
   - 응집도가 높다는 것은 변화가 일어날 때 해당 모률에서 변하는 부분이 크다는 것으 
     로 설명할 수도 었다. 즉 변경이 일어날 때 모률의 많은 부분이 함께 바뀐다면 응집 
     도가 높다고 말할 수 있다.
3. 낮은 결합도
   - 결합도가 낮아지면 변화에 대응하는속도가높아지고 구성이 깔팝해진다. 또한확장하기에도 
    매우편리하다.
   - 결합도란 ‘하나의 오브젝트가 변경이 일어날 때에 관계를 맺고 있는 다 
     른 오브젝트에게 변화를 요구히는 정도’라고 설명할 수 있다.

## 1.4.1 오브젝트 팩토리
1.   이 클래스의 역할은 객체의 생성 방법을 결정하고 그렇게 만들어진 오브젝트를 돌려주는 것인데， 이런 일을 하는 오 
    브젝트를 흔히 팩토리factory 라고 부른다.
    ```
    public class DaoFactory {
    
        public UserDao userDao() {
            YConnectionMaker connectionMaker = new YConnectionMaker();
            UserDao userDao = new UserDao(connectionMaker);
            return userDao;
        }
    }
    ```
2. 팩토리를 이용한 UserDaoTest
   ```
   public class UserDaoTest {
   
       private UserDao userDao;
   
       @Before
       public void setUp() {
           this.userDao = new DaoFactory().userDao();
           // DaoFactory를 이용해 UserDao를 생성
       }
   ```
3. UserDaoTest도 userDao에 대해서 구체적으로 알지 못하며 Test만 책임진다.

## 1.4.3 제어권의 이전을 통한 제어관계 역전
1. 프로그램 제어 흐름의 구조가 뒤바뀌는 것
2. 각 오브젝트는 프로그램 흐름을 결정하거나 사용할 오브젝트를 구성하는 작업에 능동적으로 참여한 
   다.
3. 하지만 제어의 역전에서는 오브젝트가 자신이 사용할 오브젝트를 스스로 선택 혹은 생성하지 않는다
4   제어권을 상위 템플릿 메소드에 넘기고 자신은 필요할 때 호출되어 사용되도록 한다.

## 1.5.1 오브젝트 팩토리를 이용한 스프링 loC
1. 스프링에서는 스프링이 제어권을 가지고 직접 만들고 관계를 부여하는 오브젝트를 Bean이라고 부른다.
2. 빈은 스프링 컨테이너가 생성과 관계설정, 사용등을 제어해주는제어의 역전이 적용된 오브젝트를 뜻한다.
3. 스프링에서는 빈의 생성과 관계설정 같은 제어를 담당하는 IoC 오브젝트를 빈 팩토 
   Bean Factory 라고 부른다.
4. 빈 팩토리보다는 이를 좀 더 확장한 애플리케이션 컨텍스트를 주로 사용

스프링을 통한 BeanFactory
1. 언노테이션을 통한 Spring Bean 등록
    - @Configuration : 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정정보라는 표시
    - @Bean : 오브젝트 생성을 담당는 IoC용 메서드라는 표시
```
@Configuration
public class DaoFactory {

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao(connectionMaker());
        return userDao;
    }

    @Bean
    public ConnectionMaker connectionMaker() {
        return new YConnectionMaker();
    }
}
```

2. ApplicationContext를 이용해서 UserDao를 생성
```
 @Test
    public void addTestWithContext() throws SQLException, ClassNotFoundException {

        ApplicationContext context = new AnnotationConfigApplicationContext(
            DaoFactory.class);

        UserDao userDao = context.getBean("userDao",UserDao.class);
```

## 1.5.2 애플리케이션 컨텍스트의 동작방식
애플리케이션 컨텍스트는 ApplicationContext 인터페이스를 구현하는데. ApplicationContext 빈 팩토리가 구현하는 
BeanFactory 인터페이스를 상속했으므로 애플리케이션 컨텍스트는 일종의 빈 팩토리인 셈이다.
### ApplicationContext의 장점
1. 를라이언트는 구체적인 팩토리 를래스롤 알 필요가 없다
2. 애를리케이션 컨텍스트는 종합 loC 서비스톨 제공해준다
3. 애풀리케이션 컨텍스트는 빈올 검색하는 다양한 방법올 제공해준다

## 1.5.3 스프링 oC의 용어 정리
1. 빈(Bean)
    - Spring이 Ioc방식으로 관리하는 오브젝트
    - Spring이 직접 생성과 제어를 담당하는 오브젝트만 해당
2. 빈 팩토리(Bean Factory)
    - 스프링의 IoC를 담당하는 핵심 컨테이너를 가리킨다.
    - 빈을 등록, 생성, 조회, 반환 등 전반적인 빈 관리를 담당한다.
    - 빈 팩토리를 직접 사용하지 않고 보통 AplicationContext를 이용한다.
3. 애플리케이션 컨텍스트(Application Context)
    - 빈 팩토리를 확장한 IoC 컨테이너
    - Bean Factory 인터페이스를 구현한다.
      ```
      public interface BeanFactory {
            ...      
          <T> T getBean(String var1, Class<T> var2) throws BeansException;
      
          <T> T getBean(Class<T> var1) throws BeansException;
            ...
      ```
      
4. 설정정보/설정 메타정보
    - 스프링의 설정정보란 애플리케이션 컨텍스트 또는 빈 팩토리가 IoC를 적용하기 위 
      해 사용하는 메타정보를 말한다
    -  IoC 컨테이너에 의해 관리되는 애플리케이션 오브젝트를 생성하고 구성할 때 사용된다.
5. 컨테이너 또는 loC컨테이너 
    - IoC 방식으로 빈을 관리한다는 의미에서 애플리케이션 컨텍스트나 빈 팩토리
    - 대체로 애플리케이션 컨텍스트를 가르킨다.
6. 스프링프레임워크 (Spring Framework)
    - 스프링 프레임워크는 IoC 컨테이너， 애플리케이션 컨텍스트를 포함해서 스프링이 
      제공하는 모든 기능을 통틀어 말할 때 주로 사용

## 1.6 싱글톤 레지스트리오} 오브젝트 스코프

오브젝트의 `동등성`과 `동일성`
1. 동일성은 `==`연산자를 통해서 판단, 일반적으로 메모리 주소값이 같아야지 동일한 객체로 판단
2. 동등성 `equals`(Override필수)를 통해서 비교하는 두 객체의 값이 같은지 확인하는 것

기존의 DaoFactory를 이용해서 UserDao를 생성한 테스트
```
    @Test
    public void daoFactorySingleTonTest(){
        DaoFactory factory = new DaoFactory();
        UserDao userDao1 = factory.userDao();
        UserDao userDao2 = factory.userDao();

        Assert.assertNotSame(userDao1,userDao2);
    }
```
dao1,dao2가 동일하지 않다. 호출할 때 마다 UserDao를 생성해서 반환하기 때문
즉, 각 호출마다 새로운 오브젝트가 생성됐다.
```
    public UserDao userDao() {
        UserDao userDao = new UserDao(connectionMaker());
        return userDao;
    }
```
SingleTon 디자인 패턴을 이용해 싱글톤으로 만들 수 있기는 하다. 이전  테스트 실패
```
public class DaoFactory {
    private static final UserDao instance = new UserDao(new YConnectionMaker());

    public UserDao userDao() {

        return instance;
    }
```
스프링을 통한 UserDao 생성 테스트
getBean()메서드를 통해 얻어온 UserDao 객체가 동일한 객체
```
    @Test
    public void appContextSingleTonTest(){
        ApplicationContext context = new AnnotationConfigApplicationContext(
            DaoFactory.class);

        UserDao userDao1 = context.getBean("userDao",UserDao.class);
        UserDao userDao2 = context.getBean("userDao",UserDao.class);
        System.out.println("userDao1 = " + userDao1);
        System.out.println("userDao2 = " + userDao2);
        Assert.assertSame(userDao1,userDao2);
    }
```
## 1.6.1 싱글톤 레지스트리로서의 애플리케이션 컨텍스트
스프링의 애플리케이션 컨텍스트는 싱글톤 레지스트리이다.
별다른 설정을 하지 않으면 생성하는 빈(오브젝트)는 모두 싱글톤을 유지한다. 여러 스레드에서
하나의 오브젝트를 공유해 동시에 사용함. 즉, 생성되는 오브젝트는 변환되는 상태값을 갖으면 안된다.

### 자바에서 싱글톤의 한계
1. private 생성자를 갖고 있기 때문에 상속 불가
    싱글톤으로 만들어진 객체는 하나의 인스턴스를 유지하기 위하여 객체 내부적으로 static이 붙은 객체(Instance)를 생성한다.
    즉, 생성자를 private를 사용하여 getInstance() 메서드로만 객체를 사용하도록 강제하기 때문에 상속을 이용할 수 없다.
2. 싱글톤 객체는 테스트하기가 어렵다.
    싱글톤은 초기화 과정에서 생성자 등을 통해 사용할 오브젝트를 다이내믹하게 주입하기도 힘들기 때문에 펼요한 오브젝트는 직접 오브젝트 
    를 만들어 사용할 수밖에 없다.
3. 서버환경에서는 싱글톤이 하나만 만들어지는 것을 보장하지 못한다.
    여러 개의 JVM에 분산돼서 설치가 되는 경우에도 각각 독립적으로 오브젝트가 생기기 때문에 싱글톤으로서의 가치가떨어진다.
4. 싱글톤의 사용은 전역 상태를 만들 수 있기 때문에 바람직하지 못하다
     아무 객체나 자유롭게 접근하고 수정하고 공유할 수 있는 전역 상태를 갖는 것은 객체지향 프로그래밍에서는 권장되지 않는 프로그래밍 모델이다.

## 1.6.2 싱글톤과 오브젝트의 상태
1. 싱글톤은 멀티스레드 환경에서 여러 스레드가 동시에 접근하여 사용하기 때문에 읽기전용을 제외하면 기본적으로 상태정보를 갖지 않는 `무상태`방식으로 만들어야한다.
2. 다수의 스레드가 싱글톤 오브젝트에 서로의 값을 엎어 씌우고 사용하는 것은 매우 위험하다.
3. 필드 변수는 읽기 전용`final`을 제외하면 설정하지 않도록 객체를 설계하자.
4. 개별적으로 바뀌는 정보는 `로컬 변수`로 정의하거나 `파라미터`로 주고받도록 설계하자.

## 1.6.3 스프링 민의 스코프
스코프(Scope) : 스프링이 관리하는 오브젝트， 즉 빈이 생성되고， 존재하고， 적용되는 범위
1. 스프링의 Bean은 기본적으로 싱글톤
2. 어플리케이션이 실행되는 동시에 객체(Bean)이 생성되며 스프링 컨테이가 존재하는 동안 계속 유지된다.
3. 경우에 따라서 싱글톤 이외 스코프를 갖을 수 있다. 프로토타입 스코프
    - 프로토 타입 스코프 : 빈을 요청할 때 마다 새로운 객체를 생성한다.
    - 하지만 사용하는 객체(클라이언트)의 스코프가 싱글톤이라면 사실상 생명주기가 싱글톤과 같다.
## 1.7 의존관계 주입(Dependency Injection)
## 1.7.1 제어의 역전과 의존관계 주입
 오브젝트의 레퍼런스를 전달. DI는 오브젝트 레퍼런스톨 외부로부터 주입받고 이톨 통해 여타 오브젝트와 다이내믹하게 의존관계가 만들어진다.

## 1.7.2 런타임 의존관계 설정
의존관계 : A객체와 B객체가 있다고 가정하고, A객체가 B객체를 사용한다면 A객체는 B객체에 의존 한다.
즉 B객체의 변화가 A객체에게 `영향`을 줄 수 있다면 A객체는 B객체에 의존하고 있다고 할 수있다.(ex:UserDao는 ConneectionMaker을 의존한다.)
1. ConnectionMaker는 인터페이스로 정의되어 있다.
2. UserDao는 ConnectionMaker의 구현체(YConnectionMaker)에 의존하고 있다.
3. 하지만 인터페이스를 통해 UserDao는 ConnectionMaker의 구현체의 Public Interface에만 의존하고 있고
4. ConnectionMaker의 구현체의 구현 방식은 모르기 때문에 두 객체의 결합도가 낮다고 설명할 수 있다.
5. UserDao는 런타임 시 사용할 오브젝트(ConnectionMaker)를 미리 알 수 없다.
6. 런타임 시에 의존관계를 맺는 대상, 실제 사용대상인 오브젝트를 `의존 오브젝트`라고 말한다.

의존관계 주입의 3가지 조건
1. 인터페이스에만 의존하고 있어야 한다.
2. 런타임 시점의 의존관계는 컨테이너나 팩토리 제 3의 존재가 결정한다.
3. 외부에서 의존 오브젝트의 레퍼런스를 주입함으로써 만들어진다.
Spring에서는 Ioc컨터에너, 빈 팩토리 ,어플리케이션 컨텍스트 등이 제 3자라고 할 수 있다.



 
