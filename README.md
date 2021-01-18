# Toby Spring vol.1
토비의 스프링 vol.1 2회차

# 1장
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

## 1.7.3 의존관계 검색과 주입
의존관계 검색(Dependency Lookup) : 런타임 시, 자신이 필요로 하는 의존 오브젝를 능동적으로 찾는다.직접 대상을 선택하지는 않지만, 메소드나 생성자를 통한 주입 대신 스스로 컨테이너에게 요청하는 방법을 사용한다.
ex) getBean()메서드
```
        ApplicationContext context = new AnnotationConfigApplicationContext(
            DaoFactory.class);

        UserDao userDao1 = context.getBean("userDao",UserDao.class);
```
UserDaoTest(클라이언트)가 UserDao라는 의존객체를 getBean()을 통해서 검색한다. 하지만 UserDaoTest는 여전히 어떤 UserDao가 올 지 모른다.
대부분 DI를 통해서 객체를 생성하는 편이 낫지만, DL을 사용해야 하는 경우도 있다.
어플리케이션 컨텍스트에게 DI를 받도록 설정하려면, 의존객체를 필요로 하는 객체 또한 컨텍스트가 관리하는 Bean으로 등록되어야 한다.
하지만 DL을 통해 의존 객체를 능동적으로 검색하여 사용한다면, `Bean으로 등록되지 않아도 된다`.

## 1.7.4 의존관계 주입의 응용
객체지향 설계와 프로그래밍의 원칙을 따랐을 때 얻을 수 있는 장점
1. 코드에는 런타임 클래스에 대한 의존관계가 나타나지 않는다.
2. 인터페이스를 이용하여 의존도가 낮은 코드를 작성할 수 있다.
3. 다른 책임을 가진 시용 의존관계에 있는 대상이 바뀌거나 변경되더라도 자신은 영향을 받지 않는다.
4. 변경을 통한 다양한 확장방법에 지유롭다.

부가기능을 추가하기 쉽다.
UserDao의 관심사는 DB Connection이 아니다. Connection과 UserDao의 책임을 완전히 분리했기 때문에
이런식의 Connection에 부가기능을 추가할 수 있고, 두 객체의 결합도가 낮기 때문에 Connection객체의 변화가 Dao에 영황을 주지 않는다.
```
public class CountingConnectionMaker implements ConnectionMaker {

    private int count = 0;
    private ConnectionMaker connectionMaker;

    public CountingConnectionMaker(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }

    public void setConnectionMaker(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }

    @Override
    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        this.count++;
        return connectionMaker.makeConnection();
    }

    public int getCount() {
        return this.count;
    }
}
```
CountingConnectionMaker는 ConnectionMaker의 다른 구현체를 주입받음으로써 기존 기능뿐만 아니라
추가된 counting기능까지 사용할 수있다. ConnectionMaker를 의존객체로 사용하는 클라이언트 객체에 변화된 CountingConnectionMaker를
주입하도록 설정하기도 매우 간단하다.
```
    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao(countingConnectionMaker());
        return userDao;
    }

    @Bean
    public ConnectionMaker connectionMaker() {
        return new YConnectionMaker();
    }

    @Bean
    public ConnectionMaker countingConnectionMaker(){
        return new CountingConnectionMaker(connectionMaker());
    }
```

## 1.7.5 메소드를 이용한 의존관계 주입
1. 생성자(Constructor) 주입
생성자를 통해서 의존객체를 주입 받는다. 생성자가 아닌 다른 메서드를 만들어서 주입해도 상관 없다.
```
public class UserDao {

    private ConnectionMaker connectionMaker;

    public UserDao(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }
```
```
    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao(connectionMaker());
        return userDao;
    }
```
2. 수정자(Setter) 주입
Setter메서드를 통하여 의존객체를 주입받는 방식
    - Test를 하기 용이하다.
    - 꼭 필요한 의존객체 주입을 하나씩 주입하기 때문에,필요한 객체의 수가 많아지면 실수할 가능성이 있다.
    - 모든 파라미터를 한 번에 받는 생성자 주입보다 낫다고 한다.
```
public class UserDao {

    private ConnectionMaker connectionMaker;

    public void setConnectionMaker(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }
```
```
    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setConnectionMaker(connectionMaker());
        return userDao;
    }
```

## 1.8 XML을 이용한 설정
Spring은 자바코드, XML, 언노테이션을 이용한 DI의존 관계 설정이 가능하다.
1. XML을 통한 의존관계 설정은 별도의 컴파일 빌드 과정이 없다는 장점이 있다.
2. 또한 스프링이 지정한 `네임스페이스`를 적극적으로 사용할 수 있다.
3. 이전에 가장 어렵다고 생각했던 설정 방식이지만 IDE의 도움을 받으니 딱히 어렵지도 않은 것 같다.
4. 가장 가독성이 높다고 생각한다(극히 개인적인 의견)

## 1.8.1 XML 설정
1. Spring Bean을 등록할 Xml의 DTD와 스키마
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
</beans>
```

2. 기존의 DaoFactory(Configuration)을 XML형식으로 변환(ApplicationContext.xml)
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="userDao" class="springbook.user.dao.UserDao">
    <property name="connectionMaker" ref="connectionMaker"/>
    
  </bean>
  <bean id="connectionMaker" class="springbook.user.YConnectionMaker"/>
</beans>
```
3. XML에 설정된 Bean 검색
기존의 Annotation(@Configuration)을 이용한 설정을 불러들일 때는  AnnotationConfigApplicationContext을 사용했다.
```
 ApplicationContext context = new AnnotationConfigApplicationContext([className]);
```
Xml을 통한 설정을 사용할 때는 GenericXmlApplicationContext을 이용한다.
```
 ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
```
4. ClassPathXmlApplicationContext
GenericXmlApplicationContext에 클래스패스에 대한 힌트를 제공할 수 있다.
```
 ApplicationContext context = new ClassPathXmlApplicationContext([xml이있는패키지의클래스.class],"applicationContext.xml");
```
## 1.8.3 DataSource 인터페이스로 변환

1. DataSource인터페이스를 사용하도록 변경, 생성자를 통해서 DataSource구현체를 주입받는다.
```
public class UserDao {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = dataSource.getConnection();
        ...
    }
    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = dataSource.getConnection();
        ...
    }
}
```

2. 자바 코드 방식
필요한 4개의 값을 넣어주면 된다.(DriverClass, Url, UserName,Password)
```
    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);    //H2Driver
        dataSource.setUrl("jdbc:h2:tcp://localhost/~/test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
```
3. Xml을 이용한 방식
```
  <bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
    <property name="driverClass" value="org.h2.Driver" />
    <property name="url" value="jdbc:h2:tcp://localhost/~/test" />
    <property name="username" value="sa" />
    <property name="password" value="" />
  </bean>
```

# 2장 테스트 
## 2.1.1 테스트의 유용성
테스트 : 예상하고 의도했던 대로 코드가 정확히 동작하는지를 확인해서 만든 코드를 확신할 수 있게 해주는 작업이다.
테스트를 통해서 코드 설계에 결함이 있음을 알 수 있다. 최종적으로 테스트가 성공하면 결함이 제거 됐음을 알 수 있다.

### 작은 단위의 테스트
테스트 역시 관심사 분리의 원리를 이용하여 대상을 분리하고 접근해야 한다. 이렇게 작은 단위로 테스트 하는 것을 `단위테스트`라고 한다.
단위 테스트는 주로 개발자가 만든 코드를 스스로 확인하는데 사용하기 때문에 이를 `개발자 테스트` 또는 `프로그래머 테스트
라고도 한다.

### JUnit 프레임워크
사실상 자바 표준으로 여겨지고 있는 테스트 프레임워크, Spring Test 모듈도 JUnit을 이용한다.
JUnit 프레임 워크의 두 가지 조건
1. 모든 메서드는 public이여야 한다.
2. 테스트 메서드에 @Test 애노테이션을 붙여줘야 한다.
JUnit은 한 클래스 내에 있는 메서드를 부가적인 설정이 되어있지 않다면 실행 순서를 보장해주지 않는다.

## 2.3.2 ~ 2.3.4  테스트 결과의 일관성
원활한 테스트를 위해서 users 테이블 초기화를 위해 `deleteAll()`과 `getCount()`메서드를 추가 했다.
각 테스트를 수행할 때 DB를 초기화하기 위해서 추가했다.
기존의 DB table에서 primary Key가 설정되어 있지 않아서 테이블을 초기화하지 않고도 테스트를 진행할 수 있었지만
PK가 설정되어 있었다면 매번 테스트를 진행하기 전에 테이블을 초기화 해야한다.
- 추가된 deleteAll,getCount 메서드
```
    public void deleteAll() throws SQLException {
        Connection c = dataSource.getConnection();

        PreparedStatement ps = c
            .prepareStatement("delete from users");
        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public int getCount() throws SQLException {
        Connection c = dataSource.getConnection();

        PreparedStatement ps = c
            .prepareStatement("select count(*) from users");
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        ps.close();
        c.close();
        return count;
    }
``` 
- getCount()메서드 테스트
```
    @Test
    public void countTest() throws SQLException, ClassNotFoundException {
        userDao.add(new User("1","name1","ps1"));
        Assert.assertTrue(userDao.getCount() == 1);

        userDao.add(new User("2","name2","ps2"));
        Assert.assertTrue(userDao.getCount() == 2);

        userDao.add(new User("3","name3","ps3"));
        Assert.assertTrue(userDao.getCount() == 3);
    }
```
- AddAndGetTest : User 추가한 후 값을 다시 가져오는 테스트
```
    @Test
    public void addAndGetTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);

        userDao.add(this.user);
        Assert.assertTrue(userDao.getCount() == 1);

        User user2 = new User("2", "name2", "ps2");
        userDao.add(user2);
        Assert.assertTrue(userDao.getCount() == 2);

        User find = userDao.get(this.user.getId());
        Assert.assertEquals(this.user.getId() , find.getId());
        Assert.assertEquals(this.user.getName() , find.getName());
        Assert.assertEquals(this.user.getPassword() , find.getPassword());

        User find2 = userDao.get(user2.getId());
        Assert.assertEquals(user2.getId() , find2.getId());
        Assert.assertEquals(user2.getName() , find2.getName());
        Assert.assertEquals(user2.getPassword() , find2.getPassword());

        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);
    }
```
- get()메서드가 실패할 경우(해당 Id가 존재하지 않음) Exception을 던지도록 설정
```
    public User get(String id) throws ClassNotFoundException, SQLException {
        // Connecton ~ Sql 생략
        ResultSet rs = ps.executeQuery();

        User user = null;
        if (rs.next()) {
            user = new User();
        //user에 찾은 값 set 생략
        }
        // 리소스 반환 생략
        if(user == null ){
            throw new EmptyResultDataAccessException(1);
        }
        return user;
    }
``` 
- 수정된 get()메서드 테스트
@Test 애노테이션 옆에 예상하는 Exception.class를 설정해주면 된다.
```
    @Test(expected = EmptyResultDataAccessException.class)
    public void getNotFountExceptionTest() throws SQLException, ClassNotFoundException {
        //given
        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);

        //when 
        User find = userDao.get("이상한Id값");
        //then
    }
```
테스트는 항상 부정적인 실패를 염두하는 식으로 작성해야한다. 일반적으로 성공하는 테스트를 작성하기 쉽다. 초록막대기 보는 쾌감이 문제다 문제..
책에서는 테스트 코드를 먼저 작성한 후, 테스트가 실패하는 것을 확인한 후, UserDao의 get()을 수정했다.
이러한 개발 방식을 TDD이라고 한다.
TDD는 `실패한 테스트를 성공시키기 위한 목적이 아닌 코드는 만들지 않는다`라는 원칙을 갖고 만든다.
테스트 코드를 작성하는 요령
- 조건(given) : 어떠한 조건이 주어졌는가? -> userdao.deleteAll(),
- 행위(when)  : 무엇을 할 때?  -> User find = userDao.get("이상한Id값");
- 결과(then)  : 어떤 결과가 나온다 -> @Test(expected = EmptyResultDataAccessException.class)

## 2.3.5 테스트코드개선
- @Before
    @Before 애노테이션이 붙은 메서드는 각 테스트들이 실행되기 전에 실행되는 메서드
    아래 코드와 같이 ApplicationContext를 초기화하는 과정을 미리 설정할 수 있다.
    해당 클래스의 테스트 메서드 개수만큼 실행된다.
    ```
        private UserDao userDao;
        private ApplicationContext context;
        private User user;
    
        @Before
        public void setUp() {
            this.context = new GenericXmlApplicationContext(
                "applicationContext.xml");
            this.userDao = this.context.getBean("userDao", UserDao.class);
    
            this.user = new User("1", "youzheng", "ps");
        }
    ```  
- @After
    @After 애노테이션이 붙은 메서드는 모든 테스트가 종료된 후에 실행된다.
    자동으로 userDao.deleteAll()을 실행함으로써 다음 테스트를 위해 초기화를 미리 할 수 있다.
    ```
        @After
        public void reset() throws SQLException {
            userDao.deleteAll();
        }
    ```
    JUnit은 각 테스트(@Test가 붙어있는 메서드)가 독립적인 테스트를 하기 위해서 테스트 메서드를 실행할 때 마다 독립적인 오브젝트를 만들어서 테스트를 진행한다.
    매번 다른 오브젝트를 만드는 것은 비효율적일지 몰라도 서로 다른 테스트들이 주지 않고 독립적인 환경을 만들기 위해서 이러한 방법을 채택했다.

- 픽스처 : 테스트를 수행히는 데 펼요한 정보나 오브젝트
    테스트에 필요한 여러개의 user객체를 미리 등록해서 사용한다.
    또한 테스트에 필요한 userDao 역시 대표적인 픽스처
    ```
      private UserDao userDao;
      private ApplicationContext context;
  
      private User user1;
      private User user2;
      private User user3;
  
      @Before
      public void setUp() {
          this.context = new GenericXmlApplicationContext(
              "applicationContext.xml");
          this.userDao = this.context.getBean("userDao", UserDao.class);
  
          this.user1 = new User("1", "youzheng", "ps1");
          this.user2 = new User("2", "woojung", "ps2");
          this.user3 = new User("3`", "yang", "ps3");
      }
    ```
## 2.4.1 테스트를 위한 애플리케이션 컨텍스트 관리

```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {
    @Autowired
    private UserDao userDao;
    ...
```
UserDaoTest에 두 개의 애노테이션을 추가한다.
- @RunWith(SpringJUnit4ClassRunner.class)
    JUnit 프레임워크를 확장시키기 위한 애너테이션
- @ContextConfiguration(locations = "/applicationContext.xml")
    적용할 applicationContext 설정이 있는 xml위치를 설정하는 애노테이션이다.
    `locations`대신 `classes`를 적용하면 자바코드로 설정도 적용할 수 있다.(Spring3.1이상)
- @Autowired
    @Autowired를 통해서 ApplicationContext 설정에 등록된 UserDao.class를 가져온다.
    Type을 기준으로 가져오고 비슷한 애노테이션으로 `@Resource`가 있는데 이 애노테이션은 등록된 Bean의 이름을 기준으로 DI를 해준다.

## 2.4.2 DI와 테스트
DI를 할 때눈 항상 Interface를 사용해야한다. 해당 인터페이스를 구현한 구현 클래스를 DI해도 되지만 아래와 같은 이유로 인터페이스 DI를 권장한다고 한다.
1. 소프트웨어 개발에서 절대로 바뀌지 않는 것은 없기 때문이다.
    인터페이스를 이용한 DI는 나중에 혹시모르게 생길 변경에 시간과 비용 부담을 줄여준다.
    즉, 변화에 대응하기가 용이하다.
2. 클래스의 구현 방식은 바뀌지 않는다고 하더라도 인터페이스를 두고 DI를 적용하게 해두면 다른 차원의 서비스 기능을 도입할 수 있기 때문이다.
    새로운 기능을 추가하더라도 기존 코드는 수정할 필요가 없다.
3. 테스트
    테스느는 자동으로 실행되며 빠르게 할 수 있어야한다. 또한 테스트 범위는 되도록 작아야한다.
    DI는 테스트가 작은 단위로 독립적으로 실행되도록 도와준다.
### 테스트코드에 의한 DI
테스트 클래스에서 등록될 Bean을 조작할 수 있다. 예를 들어서 applicationContext.xml에는 h2DB에 접속하도록 설정되어있는 DadaSource가 등록되어있다.
하지만 테스트하는 도중에 다른 DataSource를 주입하도록 설정할 수 있다.
- @DirtiesContext
    `@DirtiesContext` 애노테이션을 설정하면 JUnit에게 다른 applicationContext설정을 사용할 것이라고 알려주는 것이다.
    applicationContext에 설정된 Bean 의존 관계를 강제적으로 바꾸는 것이 가능하다. 
    ```
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = "/applicationContext.xml")
    @DirtiesContext
    public class UserDaoTest {
    ```
    class단에 추가한다면 해당 클래스 모든 테스느 메서드의 설정에 대해서 Bean의존 관계가 변견된다.
    method상단에 붙이는 것을 추천한다.
    강제로 setter메서드를 이용해서 의존관계를 바꾸는 것이 아니다.
- @DirtiesContext 테스트

사실 이해를 다 못해서 직접 실험해봤다.
- 테스트를 위한 객체
    SayObject는 TestInterface say()메서드를 이용해서 메세지를 출력한다. 간단한 의존관계를 설계하고 실험했다.
    TestInterface는 A와 B라는 두 개의 구현체가 있다.
    ```
    public class SayObject {
    
        private TestInterface testInterface;
    
        public void setTestInterface(TestInterface testInterface) {
            this.testInterface = testInterface;
        }
    
        public void say() {
            testInterface.say();
        }
    
    }
    
    public interface TestInterface {
    
        public void say();
    
    }
    
    public class ATestImpl implements TestInterface {
    
        @Override
        public void say() {
            System.out.println("ATestImpl");
        }
    }
    
    public class BTestImpl implements TestInterface {
    
        @Override
        public void say() {
            System.out.println("BTestImpl");
        }
    }
    ```
    - applicationContext.xml에 추가
    ```
      <bean id="testInterface" class="springbook.temp.ATestImpl"/>
      <bean id="sayObejct" class="springbook.temp.SayObject">
        <property name="testInterface" ref="testInterface"/>
      </bean>
    ```
    - Test코드는 아래와 같다.
    ```
        @Test
        @DirtiesContext
        public void test() {
            sayObject.setTestInterface(new BTestImpl());
            System.out.println(1);
            sayObject.say();
        }
    
        @Test
        public void test2() {
            System.out.println(2);
            sayObject.say();
        }
    ```
    - result message
    ```
    1
    BTestImpl
    2
    ATestImpl
    ```
    테스트의 순서는 보장되지 않지만 일단 테스트를 돌려봤다.
    test1()메서드에서 의존 관계를 강제를 A에서 B로 변경했지만 test2()가 테스트 될 때는 다시 .xml에 설정된 의존관계로 바뀌는 것을 볼 수 있다.
    test1()의 @DirtiesContext를 제거하고 테스트를 돌리면 test1에서 강제로 바꿨던 의존관계가 test2까지 영향을 미친다.
    ```
    1
    BTestImpl
    2
    BTestImpl
    ```
    @DirtiesContext가 붙어있는 테스트만 의존관계를 변경하고 다른 테스트에는 변경된 의전관계 설정을 적용하지 않게 하게하는 기능이다.
    독립적인 테스트를 진행시켜주는 설정!
### 테스트를 위한 별도의 DI 설정
테스트를 위한 xml 설정파일을 만드는 것도 좋은 방법인데 생략한다.

### 컨테이너 없는 DI 테스트
DI는 프레임워만의 기술이 아니다. 자바 객체지향 프로그램의 한 가지 기술이기 때문에 프레임워크에 종속적일 필요가 없다.
굳이 @RunWith()애노테이션을 붙여서 스프링 컨텍스트에 연결할 필요없이 그냥 직접 객체들 간 의존설정을 해서 테스트를 진행해도 된다.
직접 DI를 해서 테스트를 진행한다면 Spring Application이 생성되는 만큼의 시간이 절약되서 가볍고 빠른 테스트를 진행할 수 있다.
물론 직접 DI를 해줘야하는 불편함도 있다.

# 템플릿
탬플릿 : 바뀌는 성질이 다른 코드 중에서 변경이 거의 일어나지 않으며 일정한 패턴으로 유지되는 특성을 가진 부분을 자유롭게 변경되는 성질을 가진 부분으 
로부터 독립시켜서 효과적으로 활용할 수 있도록 하는 방법이다.

## 3.1.1 예외처리 기능을 갖춘 DAO
이전에 만들었던 UserDao에는 예외처리가 되어있지 않다. JDBC를 사용하는 과정에서 예외가 발생했더라도 
사용하고 있던 `한정된`리소스는 꼭 반환해야 한다.
일반적으로 close()로 되어있는 메서드들은 리소스반납을 하기 위한 메서드이다.
PreparedStatement, Connection,ResultSet에 close()메서드를 이용해서 해당 자원을 반납할 수 있다.
하지만 해당 메서드들도 SqlException이 발생할 수 있기에 자원 반납을 위해서는 try catch블럭으로 예외에 대응하면서 반납해야한다.
자원을 반납하는 순서는 가져온 순서의 반대로 반납한다.

## 3.2.2
복잡한 try~catch 블럭에서 변하지 않는 부분과 변하는 부분을 분리한다.
1. 변하지 않는 부분
    - 자원을 가져오는 부분
    - 자원을 반납하는 부분
2. 변하는 부분
    - Sql Query
    - Result를 처리하는 부분

### 템플릿 메소드 패턴의 적용
```
    public class UserDaoDeleteAll extends UserDao {
        protected PreparedStatement makeStatement(Connection c) throws SQLException {
            PreparedStatement ps = c.prepareStatement("delete from users");
            return ps;
        }
    }
```
템플릿 메서드 패턴 : 변하지 않는 부분은 슈퍼클래스에 두고 변하는 부분은 추상 메소드로 정의해둬서 서브클래스에서 재구현하는 방법
Connection을 이용해서 PreparedStatement를 가져오는 부분이 변하는 부분이다.
그러므로 PreparedStatement를 추상 메서드로 만들고, 서브클래스에서 구현하도록 설정할 수 있다.
하지만, 템플릿 메서드 패턴는 여러가지로 단점이 있다. 가장 큰 이유로는 현재 UserDao를 구현하기 위해서 
템플릿 메서드 패턴을 적용한다면 각 로직(deleteAll,add등등)에 맞게 새로운 클래스를 만들어야 한다.  
또한 확장구조가 이미 클래스를 설계하는 시점에서 고정되어 버린다는 단점이 있다

### 전략패턴의적용
전략패턴 : 오브젝트를 둘로 분리하고 클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 전략
아래와 같이 PreparedStatement를 얻어오는 부분을 구현할 인터페이스로 지정해준다.
```
    public interface StatementStrategy {
        PreparedStatement makePreparedStatement(Connection c) throws SQLException;
    }
```
하지만 여러개의 로직이 담겨 있는 UserDao에서 각 메서드들이 서로다른  StatementStrategy의 구현체를 사용한다면
new DeleteAllStatementStrategy();, new AddStatementStrategy();등등 정확하게 구현체에 대해서 알고 있어야 한다.
전력 패턴 또한 OCP에도 잘 들어맞는다고 볼 수 없다.

PreparedStatement를 생성하는 StatementStrategy구현체를 파라미터로 받고 로직을 처리하는 `컨텍스트`부분 메서드를 따로 분리한다.
그리고 각 dao메서드에서 PreparedStatement를 호출한 후 `컨텍스트`에 해당하는 메서드에 오브젝트를 전달함으로써 로직을 수행한다.
- jdbcContextWithStatementStrategy(컨텍스트)
    ```
     private void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException {
            Connection connection = null;
            PreparedStatement ps = null;
            try {
                connection = dataSource.getConnection();
    
                ps = stmt.makePreparedStatement(connection);
    
                ps.executeUpdate();
            } catch (SQLException e) {
                throw e;
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
    ```
- 변경된 add()와 deleteAll()
    ```
        public void add(User user) throws ClassNotFoundException, SQLException {
            AddStatement stmt = new AddStatement(user);
            jdbcContextWithStatementStrategy(stmt);
        }
    
        public void deleteAll() throws SQLException {
            DeleteAllStatement statement = new DeleteAllStatement();
            jdbcContextWithStatementStrategy(statement);
        }
    ```
클라이언트(add,deleteAll등 메서드)에게 PreparedStatement를 `전략오브젝트`(StatementStrategy)를 넘겨받고 `컨텍스트`부분에서 핵심로직을 처리한다.
`컨텍스트`가 사용할 `전략오브젝트`를 `클라이언트`에게 일종의 DI를 이용한 방법이다.

## 3.3 JDBC 전략 패턴의 최적화
컨텍스트   : PreparedStatement를 실행하는 JDBC의 흐름
전략      : PreparedStatement를 생성

## 3.3.2 전략과 클라이언트의 동거
현재 UserDao에서 컨텍스트와 전략을 분리해서 사용하고 있지만 아직 문제가 있다. 
1. Dao 메서드마다 StatementStrategy를 구현한 클래스를 만들어야 하므로, 클래스 파일이 늘어난다.
2. add()메서드 처럼 부가적으로 파라미터를 받아야 하는 경우에 파라미터를 전달받는 생성자를 따로 만들어야 하는 문제

### 1번 문제 해결 방법 : `로컬 클래스`를 이용한다.
```
          public void add(final User user) throws ClassNotFoundException, SQLException {
                     class AddStatement implements StatementStrategy {
                         @Override
                         public PreparedStatement makePreparedStatement(Connection connection)
                             throws SQLException {
                             PreparedStatement ps = connection.prepareStatement(
                                 "insert into users(id, name, password) values (?,?,?)");
                             ps.setString(1, user.getId());
                             ps.setString(2, user.getName());
                             ps.setString(3, user.getPassword());
                             return ps;
                         }
                     }
                     AddStatement stmt = new AddStatement();
                     jdbcContextWithStatementStrategy(stmt);
                 }
```
내부 클래스의 장점
1. 해당 메서드 내부에 정의되기 때문에 add()메서드의 user객체에 바로 접근가능하다.
    내부 클래스에서 자신이 정의된 클래스의 외부 데이터에 접근하려면 `final`을 추가해야한다. 
    ```
   public void add(final User user) 
   ```
### 1번 문제 해결 방법 : `익명 내부 클래스`를 이용한다.
`컨텍스트`(jdbcContextWithStatementStrategy)에 바로 익명 클래스를 파라미터로 넘기도록 만든다.
```
    public void add(final User user) throws ClassNotFoundException, SQLException {
        jdbcContextWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection connection)
                throws SQLException {
                PreparedStatement ps = connection.prepareStatement(
                    "insert into users(id, name, password) values (?,?,?)");
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                return ps;
            }
        });
    }
```

## 3.4.1 JdbcContext의 분리
Dao와 컨텍스트 역시 분리할 수 있다. 관심사가 다르기 때문
- JdbcContext 클래스 생성
    ```
    public class JdbcContext {
    
        private DataSource dataSource;
    
        public void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }
    
        public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
          
         //... 생략
    }
    ``` 
- UserDao에서 JdbcContext를 주입받을 수 있도록 변경
add()메서드를 제외한 다른 메서드는 적용하지 않았기에 DataSource도 주입받아야 한다.
    ```
    public class UserDao {
    
        private JdbcContext jdbcContext;
    
        public void setJdbcContext(JdbcContext jdbcContext) {
            this.jdbcContext = jdbcContext;
        }
        //.. 생략
        public void add(final User user) throws ClassNotFoundException, SQLException {
            this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {
                @Override
                public PreparedStatement makePreparedStatement(Connection connection)
                    throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(
                        "insert into users(id, name, password) values (?,?,?)");
                    ps.setString(1, user.getId());
                    ps.setString(2, user.getName());
                    ps.setString(3, user.getPassword());
                    return ps;
                }
            });
        }
    ```
- applicationContext.xml 변경
    ```
    <bean id="userDao" class="springbook.user.dao.UserDao">
      <property name="dataSource" ref="dataSource"/>
      <property name="jdbcContext" ref="jdbcContext" />
    </bean>
  
    <bean id="jdbcContext" class="springbook.user.strategy.JdbcContext">
      <property name="dataSource" ref="dataSource"/>
    </bean>
  ```
## 3.4.2
### 인터페이스와 DI
DI의 기본은 인터페이스를 이용한 주입이다. 스프링에서 DI란 Ioc의 개념도 포괄한다.
클라이언트가 런타임 시점에 의존성을 주입해주는 것이 올바른 스프링의 DI

### JdbcContext가 UserDao와 DI구조로 만들어져야하는 이유
1.  JdbcContext가 스프링 컨테이너의 싱글톤 레지스트리에서 관리되는 싱글톤 빈이기 때문이다.
    JdbcContext는 필드변수 등 데이터 값을 갖지 않는다. 싱글톤으로 설정하고 사용할 수 있다는 뜻이다.
    DataSource 또한 읽기 전용이므로 JdbcContext가 싱글톤으로 사용되는데 전혀 문제가 없다.
2. JdbcContext가 DI를 통해 다른 빈에 의존하고 있기 때문이다.
    처음에는 이 부분이 이해가 가지 않았지만, 1장에서 DI와 DL부분을 다시 읽고 이해가 갔다.
    Spring에게 의존객체를 주입받는 객체 또한 Spring Bean으로 등록되어야 한다.그러므로 Spring에게 DI를 받고 싶다면
    JdbcContext도 Bean으로 등록되어 스프링이 생성 관리하는 Ioc대상이 되어야 한다. 매번 수동 DL을 할 수는 없다.

## 코드를 통한 DI
JdbcContext가 DataSource를 주입받으려면 스프링 컨텍스트가 관리하는 빈으로 등록되어야 한다.
하지만 UserDao가 SpringBean으로 등록되므로, UserDao를 생성하는 과정에서 대신 DI를 받을 수 있다.
JdbcContext를 필요로 하는 Dao의 숫자만큼 JdbcContext가 생성되지만, 성능에 크게 무리가 가지는 않는다고 한다.
싱글톤으로 등록되는 Bean에게 사용되므로 사실상 Scope 역시 해당 객체의 스코프와 같고 볼 수 있다.
- 수정된 UserDao
```
public class UserDao {

    private DataSource dataSource;  //일단 살려둔다.
    private JdbcContext jdbcContext;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcContext = new JdbcContext(dataSource);
    }
// 변경된 applicationContext.xml
// jdbcContext를 등록했던 빈을 제거
      <bean id="userDao" class="springbook.user.dao.UserDao">
        <property name="dataSource" ref="dataSource"/>
      </bean>
```
- 수정된 JdbcContext
```
    ```
      private DataSource dataSource;
  
      public JdbcContext(DataSource dataSource) {
          this.dataSource = dataSource;
      }
```

## 3.5 템플릿과 콜백
1. 템플릿  : 변화지 않는 고정된 로직을 담는 부분, 일반적으로 슈퍼클래스에 해당을 넣고, 서브클래스에서 부가적인 내용을 구현하도록 설계하는 방식이다.
2. 콜백   : 실행되는 것을 목적으로 다른 오브젝트의 메서드에 전달되는 객체, 파라미터로써 값을 담고 전달되는 것이 아니고 메서드 실행을 위해서 전달되는 객체이다. 메서드를 담은 오브젝트라서 `Functional Object`라고 부른다.
    - 
템플릿 콜백 패턴은 보통 단일 메서드를 구현한 익명클래스(콜백객체)를 사용한다.
또한 보통 파라미터를 하나 전받는다. StatementStrategy(콜백)는 Connection을 JdbcContext(템플릿)에게 전달받는다.
전달받는 파라미터는 템플릿의 작업 흐름중에 만들어지는 컨텍스트 정보이다.

## 3.5.2 편리한 콜백의 재활용
1. 중복되는 익명클래스를 만들고 템플릿에 전달하는 부분을 메서드 추출했다.
Sql Query를 전달하면 콜백 인터페이스를 구현하고 템플릿에 전달하는 과정을 처리한다.
add()메서드처럼 부가적인 정보를 전달받아야 한다면 가변인자를 이용해서 전달받으라하는데
나는 못하겠다.
```
public class UserDao {
    //.. 생략
    public void deleteAll() throws SQLException {
        executeQuery("delete from users");
    }

    private void executeQuery(final String query) throws SQLException {
        this.jdbcContext.workWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection connection)
                throws SQLException {
                PreparedStatement ps = connection.prepareStatement(
                    query);
                return ps;
            }
        });
    }

}
```
## 3.5.3 템플릿/콜백의 응용
1. 3번 이상 반복된다면 코드를 개선할 시점이다.
2. 계산기 코드를 템플릿/콜백 패턴으로 리팩토링

#### 템플릿/콜백을 이용한 계산기
1. fileReadTemplate메서드에 try~catch~finally를 넣으므로써 중복제거
    ```
    private Integer fileReadTemplate(String filepath, BufferedReaderCallback callback)
            throws IOException {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filepath));
                Integer sum = callback.doSomethingWithReader(br);
                return sum;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw e;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        throw e;
                    }
                }
            }
        }
    ```
    
2. BufferedReaderCallback 인터페이스를 구현함으로써, 서로 다른 기능의 메서드를 구현할 수 있다.
```
    public int multiply(String filePath) throws IOException {
        return fileReadTemplate(filePath, new BufferedReaderCallback() {
            @Override
            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
                Integer multiply = 1;
                String line = null;
                while ((line = br.readLine()) != null) {
                    multiply *= Integer.parseInt(line);
                }
                return multiply;
            }
        });
    }
```

#### 제네릭스를 이용한 리팩토링
1. Callback 인터페이스에 제네릭스를 적용
```
public interface LineReadCallback<T> {

   T doSomethingWithLine(String line, T value) throws IOException;
}

```
2. Callback 인터페이스를 사용하는 클라이언트에서 타입을 결정
```
    public Integer multiply(String filePath) throws IOException {
        return lineReadTemplate(filePath, new LineReadCallback<Integer>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) throws IOException {
                return value * Integer.parseInt(line);
            }
        }, 1);
    }

    public String concatenate(String filepath) throws IOException {
        return lineReadTemplate(filepath, new LineReadCallback<String>() {
            @Override
            public String doSomethingWithLine(String line, String value) throws IOException {
                return value + line;
            }
        }, "");
    }
```

## 3.6 스프링의 JdbcTemplate
스프링이 기본적으로 지원해주는 템플릿/콜백을 이용한 JdbcTemplate

1. JdbcTempalte를 적용한 deleteAll()
PreparedStatement를 반환하는 PreparedStatementCreator 인터페이스(콜백)을 구현해서 넘긴다.
```
    public void deleteAll() throws SQLException {
        this.jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection)
                throws SQLException {
                return connection.prepareStatement("delete from users");
            }
        });
    }
```
기본적으로 내장되어 있는 콜백 함수가 많아서 쿼리만 날려도 된다.
```
    public void deleteAll() throws SQLException {
        this.jdbcTemplate.update("delete from users");
    }
```
2. queryForObject
세 가지 인자를 넘겨준다.query,오브젝트배열의 sql 바인딩 파라미터, RowMapper구현체
만약 해당하는 값을 찾지 못햇다면 `EmptyResultDataAccessException`가 발생
RowMapper역시 중복되는 부분이기에 따로 내부클래스로 구현해 놓는것이 좋다.
```
    public User get(String id) throws ClassNotFoundException, SQLException {
        return this.jdbcTemplate.queryForObject("select * from users where id= ?", new Object[]{id}
            , new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet resultSet, int i) throws SQLException {
                    User user = new User();
                    user.setId(resultSet.getString("id"));
                    user.setName(resultSet.getString("name"));
                    user.setPassword(resultSet.getString("password"));
                    return user;
                }
            }
        );
    }
```
3. getAll()메서드를 추가하기 전 미리 테스트 코드 작성
```
    @Test
    public void getAllTest() throws SQLException, ClassNotFoundException {
        // 테이블이 비어있다면 비어있는 리스트를 반환한다.
        userDao.deleteAll();
        List<User> users0 = userDao.getAll();
        Assert.assertEquals(users0.size(),0);

        userDao.add(this.user1);
        List<User> users = userDao.getAll();
        Assert.assertEquals(users.size(), 1);

        userDao.add(this.user3);
        List<User> users2 = userDao.getAll();
        Assert.assertEquals(users2.size(), 2);

        userDao.add(this.user2);
        List<User> users3 = userDao.getAll();
        Assert.assertEquals(users3.size(), 3);

        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));
    }

    private void checkSameUser(User user1, User user2) {
        Assert.assertEquals(user1.getId(), user2.getId());
        Assert.assertEquals(user1.getName(), user2.getName());
        Assert.assertEquals(user1.getPassword(), user2.getPassword());
    }
```
4. getAll()메서드
```
    public List<User> getAll() {
        return this.jdbcTemplate
            .query("select * from users order by id", userRowMapper());
    }
```

# 4장 예외처리
## 4.1.2 예외의종류와특징
### Error
1. `Error` 클래스의 서브클래스
2. 주로 VM단에서 발생하는 예외, 어플리케이션 코드에서 잡아도 대응 방법이 없다.
3. 어플리케이션에서 신경쓰지 않아도 된다.
### 체크 예외(Checked Exception)
1. `Exception` 서브클래스
2. 체크 예외가 발생할 수 있는 메소드를 샤용할 경우 반드시 예외를 처리할 코드를 함께 작성해야 한다.
3. `catch` 문으로 잡던지, `throws` 로 날라던지 꼭 처리해야 한다.
3. 일반적인 예외
### 언체크 예외(Unchecked Exception)
1. `RuntimeException` 서브클래스
2. catch문, throws로 처리할 필요가 없다.
3. `NullPointerException`,  `IllegalArgumentException` 등등 코드에서 미리 조건을 주어 방지할 수 있다.
4. 개발자의 부주의로 발생하는 예외
5. 시스템 장애 및 프로그램상의 오류

## 4.1.3 예외처리 방법
### 예외 복구
예외 발생 시, 다른 작업 흐름으로 자연스럽게 유도하는 방법
예외처리를 강제하는 체크 예외들은 어떤 식으로든 복구할 가능성이 있는 경우에 사용한다.

### 예외처리 회피
자신을 호출한 객체에게 예외를 throw한다.
예외를 전달 받은 객체는 반드시 예외를 처리해야한다. 예외를 회피하려면 반드시 의도가 분명해야한다.
무책임하게 무작정 throw를 하는 것은 옳지 못하다. 반드시 호출하는 메서드에서 처리해야 한다.

### 예외 전환
복구할 가능성이 없다고 생각하여, 회피와 마찬가지로 밖으로 예외를 던진다.
하지만 발생한 예외를 그대로 던지는 것이 아니고 적절한 예외로 전환하여 던진다.
### 예외 전환의 목적
1. 예외를 좀 더 세부적인 내용으로 바꾸어 던진다면 서비스 계층에서 예외를 복구할 수 있다.
2. 예외를 처리하기 쉽게 포장하는 방법

API가 던지는 예외가 아닌 어플리케이션 코드에서 나오는 예외라면 체크예외로 처리하는 것이 적절하다.

### 자바 엔터프라이즈 환경에서의 예외
자바 엔터프라이즈 환경에서는 하나의 요청에서 예외가 발생한다면, 그 예외만 정지시키면 된다.
작업을 일시 중단하고 예외를 복구할 방법이 없다. 어플리케이션 차원에서 요청을 빠르게 취소하고 관리자에게 통보하는 편이 낫다.
대부분의 SqlException(99%복구불가)의 경우에는 복구가 불가능하기 때문에 빠르게 RuntimeException을 던져지는 게 낫다.
SqlException이 발생하는 경우
1. Sql문이 틀렸을 경우
2. Connection 풀이 모자른 경우
3. 서버가 다운된 경우
4. 네트워크 환경이 불안정한 경우
일반 적으로 어플리케이션단에서 처리할 수 있는 경우가 아니므로 빠르게 예외를 던져주는 방식이 좋다.
#### 어플리케이션 예외
시스템 또는 외부 상황이 아닌 어플리케이션 자체의 로직에서 발생하는 반드시 catch해서 조치를 취해야하는 예외

## 4.1.5 SQLException은 어떻게 됐나?
JdbcTemplate를 적용한 후, Dao의 메서드에서 throw Exceptio이 전부 사라졌다.
Spring이 제공하는 API의 메서드에 정의되어 있는 예외는 대부분 런타임 예외다.
Sql 표준Sql문법이 있기 때문에 많은 DB가 이 표준 Sql을 따르지만 벤더사의 고유의 비표준Sql은 항상 존재한다.
마찬가지로 Jdbc를 각 DB벤더사가 인터페이스를 구현했지만 각자 DB의 에러코드는 같지 않다.
Jdbc는 SqlException만을 던져, 자세한 상황을 알려면  `getSQLState()` 메소드로 확인해야 한다.
즉 SqlException만으로 DB에 독립적인 유여한 코드를 작성할 수 없다.

## 스프링에서 SqlException
Spring은 `DataAccessException`이라는 SqlException을 대체할 수 있는 런타임 예외가 존재한다.
또한  DataAccessException의 다양한 서브클래스도 존재한다. 데이터 엑세스 과정에서 발생할 수 있는 예외상황을 분류하고 이를 추상화하여 가지고 있다.
### SQLErrorCodeSQLExceptionTranslator를 이용한 예외 전환
```
    @Test
    public void sqlExceptionTranslateTest(){
        try {
            userDao.add(user1);
            userDao.add(user1);
        }catch (DuplicateKeyException e){
            SQLException rootCause = (SQLException)e.getRootCause();
            SQLErrorCodeSQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(
                this.dataSource);
            Assert.assertTrue(set.translate(null,null,rootCause) instanceof DuplicateKeyException);
        }
    }
```

## 5.1 ~ 5.1.4 Level,UserService  추가
특별히 메모할 내용 없고, 직접 책 한번 더 읽자

## 5.1.5 코드 개선(UserService.upgradeLevels)

### 리팩토링
#### 1.메서드 추출
`업그레이드 실행`메서드와 `업그레이드 가능여부`를 판단하는 메서드를 분리한다.
```
    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (canUpgradeUser(user)) {
                user.upgradeLevel();
                userDao.update(user);
            }
        }
    }

    private boolean canUpgradeUser(User user) {
        Level currentLevel = user.getLevel();

        switch (currentLevel) {
            case BASIC: {
                return user.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER;
            }
            case SILVER: {
                return user.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_SILVER;
            }
            case GOLD: {
                return false;
            }
            default: {
                throw new IllegalArgumentException("Unknown Level:" + currentLevel);
            }
        }

    }
```
#### 2. 다음 업그레이드가 무엇인지 확인하는 로직
User객체 스스로 판단하도록 만든다. 스스로 값을 교체하도록 설정한다.
```
public class User {

    private Level level;
    //... 생략
    public void upgradeLevel() {
        Level nextLevel = this.level.nextLevel();
        if (nextLevel == null) {
            throw new IllegalStateException(this.level + "은 더이상 업그레이드가 불가능합니다");
        }
        this.level = nextLevel;
    }
    //... 생략
```
#### 3. 다음 단계 레벨
Level 스스로 다음 업그레이드 단계를 갖고 있는다.
주의)Enum타입은 어플리케이션 실행과 함께 static 메모리에 올라간다(싱글톤).
만약 Level의 순서를 BASIC->SILVER->GOLD로 해놓으면 BASIC이 메모리에 올라가는 중에 Level.GOLD의 값이 필요하지만
아직 GOLD가 메모리에 올라가 있지 않기 때문에 참조할 값이 없어서 에러가 발생한다. 
순서를 고려해서 배치해야 한다.
```
public enum Level {

    GOLD(1, null), SILVER(2, Level.GOLD), BASIC(3, Level.SILVER);

    private final int value;
    private final Level next;

    Level(int value, Level next) {
        this.value = value;
        this.next = next;
    }
    public Level nextLevel(){
        return next;
    }
}
```

## 5.2 트랜잭션 추상화
### 트랜잭션 롤백(transaction rollback)
어느 로직이 실행되는 중에 문제가 생겼을 경우, DB의 정보를 Sql실행 이전으로 돌려놓는 것
### 트랜잭션 커밋(transaction commit)
모든 Sql이 성공적으로 마무리된 후, DB에 작업을 확정시키는 것

### JDBC의 트랜잭션
- 하나의 Connection을 가져와 사용하다가 닫는 순간 일어난다.
- Connection 오브젝트를 통해서 이루어진다.
- 트랜잭션을 실행하려면 자동옵션을 `false`로 설정한다.(기본값=true)
- 트랜잭션이 시작되면 `commit()`혹은 `rollback()`메서드가 호출될 때 까지 하나의 트랜잭션으로 묶인다.
- 일반적으로 예외가 발생하면 `rollback`한다.
- `setAutoCommit(false);`으로 트랜잭션을 시작하며  `commit()`혹은 `rollback()`종료하는 작업을 `트랜잭션의 경계작업`이라고 한다.
- 하나의 DB안에서 만들어지는 트랜잭션을 `로컬 트랜잭션`(Local Transaction)이라고 한다

## 5.2.3 트랜잭션동기화
서비스객체에서 트랜잭션을 시작하기 위해 Connection을 어느 특정 장소에 보관해 놨다가 이후 호출되는 Dao 메서드에서 저장된 COnnection을 사용하는 방법
- 트랜잭션 동기화 저장소는 작업 스레드마다 독립적으로 Connection을 생성하기 때문에 멀티스레드 환경에서 충돌할 위험이 없다.
- `TransactionSynchronizationManager`을 이용해 동기화
- `DataSourceUtils`를 이용해 Connection을 가져오고 반납
```
    public void upgradeLevels() throws Exception {
        //트랜잭션 관리자를 이용해 동기화 작업을 초기화
        TransactionSynchronizationManager.initSynchronization();
        //DB컨넥션을 생성
        Connection connection = DataSourceUtils.getConnection(this.dataSource);
        //트랜잭션을 실행
        connection.setAutoCommit(false);

        try {
            
            // 업데이트하는 부분 생략..
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            // DB컨넥션 반환
            DataSourceUtils.releaseConnection(connection, this.dataSource);
            // 동기화 해제
            TransactionSynchronizationManager.unbindResource(this.dataSource);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
``` 

## 5.2.4 트랜잭션 서비스 추상화
상위 코드에 문제는 UserService에 Transaction 기술이 종속되어 있다.
- 현재 난해한 부분이 많아서 생략
- 하나 이상의 DB가 참여하는 트랜잭션을 만들 때는 `JTA`를 이용한다는 것만 기억하자

### 스프링의 트랜잭션 서비스 추상화
스프링은 DB에 종속되지 않는 트랜잭션 기술을 제공함
#### PlatformTransactionManager
- 트랜잭션을 위해 스프링이 지원하는 트랜잭션 추상 인터페이스
- JDBC 로컬 트랜잭션을 이용한다면 `DataSourceTransactionManager`구현체를 사용한다.
- `DataTransactionDefinition` 오브잭트는 트랜잭션에 대한 속성을 담고 있다.
-  `TransactionStatus` 타입의 변수에 저장된다.

## 트랜잭션 기술 설정의 분리
`PlatformTransactionManager` 역시 스프링 컨텍스트가 관리하는 빈으로 만들어 준다.
UserService가 구체적인 구현체 `new DataSourceTransactionManager`를 알고 있는 것은 객체지향적 설계에 위배된다.
```
  <bean id="userService" class="springbook.user.service.UserService" >
    <property name="userDao" ref="userDao" />
    <property name="transactionManager" ref="transactionManager" />
  </bean>

  <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager" >
    <property name="dataSource" ref="dataSource" />
  </bean>
```

## 5.4.1 ~ 5.4.2 JavaMail을 이용한 메일 발송 기능
등급이 업그레이드된 사용자에게 안내 매일을 보내는 기능 추가
- 서비스가 완성되지 않은 시점에서 메일을 보낼 필요가 없다.
- 메일 기능을 체크하려면 전송 여부만 확인하면 된다.
- 메일 발송 기능을 사용하는 UserService를 테스트할 때는 메일기능이 주요 관심사가 아니다. 
```
    private void sendUpgradeEmail(User user) {

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "mail.ksug.org");
        Session session = Session.getInstance(properties, null);

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress("youzheng@gmail.com"));
            message.addRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
            message.setSubject("upgrade 안내");
            message.setText(
                String.format("사용자님의 등급이 %s로 업그레이드되었습니다.", user.getLevel().name()));

            Transport.send(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
```
## 5.4.3 테스트를 위한 서비스 추상화
자바에서 지원하는 JavaMail은 테스트하기가 매우 힘들다.
스프링이 내부적으로 JavaMail을 사용하는 `JavaMailSenderImpl`를 제공해준다.
- UserService에게 메일 발송기능은 필수 오브젝트이지만 테스트에 있어서 필수가 아니다.
`JavaMailSenderImpl`는 `JavaMailSender`인터페이스의 구현체이다.
- `JavaMailSender`를 구현한 테스트용 더비 클래스를 이용해서 테스트를 진행한다.
```
public class DummyMailSender implements JavaMailSender {
       // 모든 메서드를 오버라이딩 하지만 아무 기능을 하지 않기 때문에 구현하지 않는다.
}

```
- 실제 서비스가 시작하기 전까지는 메일을 발송할 일이 없기 때문에 더미 메일을 이용해서 UserService를 테스트한다.
```
  <bean id="userService" class="springbook.user.service.UserService" >
    <property name="userDao" ref="userDao" />
    <property name="transactionManager" ref="transactionManager" />
    <property name="mailSender" ref="mailSender" />
  </bean>

  <bean id="mailSender" class="springbook.user.service.DummyMailSender"/>
```
### 테스트 스텁
- 테스트 대상의 의존 오브젝트로서 존재하면서 테스트 동안에 코드가 정상적으로 진행되도록 돕는 것
- 테스트 내부에서 간접적으로 사용된다.
- DI를 통해서 미리 테스트 스텁으로 변경해야 한다.
- 실제 의존객체가 완성되지 않은 상황에서 특정 값을 리턴하도록 설정하여 독립적인 테스트와 개발을 하도록 해준다.

#### MockMailSender
Mock Object 발송이 이루어진 User의 이메일을 저장하도록 설계
UserService의 테스트는 온전히 UserService의 기능에만 집중할 수 있게 됐다.
```
public class MockMailSender implements MailSender {
    private List<String> requests = new ArrayList<String>();

    public List<String> getRequests() {
        return requests;
    }
    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        for (String email : simpleMailMessage.getTo()) {
            requests.add(email);
        }
    }
    public void send(SimpleMailMessage[] simpleMailMessages) throws MailException {

    }
}
```



##  6.1 트랜잭션 코드의 분리
UserService는 내부 코드에 트랜잭션을 담당하는 코드가 들어있다. 의존 오브젝트로 `PlatformTransactionManager`의 구현체를 주입받는다.
서비스 로직을 담당하는 UserService가 부가적인 기능인 트랜잭션까지 구현하고 있는 것이 문제 이것을 분리한다!

1. 현재 구현해 놓은 UserService를 UserServiceImpl로 변경하고 UserService라는 인터페이스를 생성한다.

2. `UserServiceTx`라는 클래스를 생성
PlatformTransactionManager, UserServiceImple를 의존하며 본인의 메서드가 호출될 시 UserServiceImpl의 메서드를 다시 호출한다.
트랜잭션이 필요한 곳에 직접 부가적으로 try~catch문으로 트랜잭션 경계를 설정한다.
```
public class UserServiceTx implements UserService {

    private UserService userService;
    private PlatformTransactionManager transactionManager;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setTransactionManager(
        PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void add(User user) {
        this.userService.add(user);
    }

    @Override
    public void upgradeLevels() {
        TransactionStatus status = transactionManager
            .getTransaction(new DefaultTransactionDefinition());
        try {
            this.userService.upgradeLevels();

            System.out.println("commit");
            transactionManager.commit(status);
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
```
3. 변경된 빈 의존관계
```
  <bean id="userService" class="springbook.user.service.UserServiceTx" >
    <property name="transactionManager" ref="transactionManager"/>
    <property name="userService" ref="userServiceImpl" />
  </bean>

  <bean id="userServiceImpl" class="springbook.user.service.UserServiceImpl" >
    <property name="userDao" ref="userDao" />
    <property name="mailSender" ref="mailSender" />
  </bean>
```
UserServiceImpl는 서비스 로직에 집중할 수 있고, UserServiceTx를 따로 구현함으로써 트랜잭션 기능을 따로 구현했다.

## 6.2 테스트 대상 오브젝트 고립시키기
UserService를 테스트하는 과정에서 UserDao를 통해서 DB에 값을 넣고, 불러들이는 과정은 주 관심사가 아니다.
그러므로 해당 UserDao의 작동과정을 Mock오브젝트를 이용해 생략할 수 있다.
- 고립 테스트를 위한 MockUserDao 오브젝트
```
    public class MockUserDao implements UserDao {
    private List<User> users;
    private List<User> updated = new ArrayList<User>();

    public MockUserDao(List<User> users) {
        this.users = users;
    }
    @Override
    public List<User> getAll() {
        return this.users;
    }
    @Override
    public void update(User user) {
        updated.add(user);
    }
    public List<User> getUpdated() {
        return this.updated;
    }
    // 다른 메서드들은 throw new UnsupportedOperationException();로 혹시모를 사용을 방지한다.
}
```
- MockUserDao 오브젝트를 이용한 UserService 테스트
```
  @Test
    public void upgradeLevelsTest() throws Exception {
        MockUserDao mockUserDao = new MockUserDao(this.users);

        // 메일발송 여부를 체크하기 위해 Mock오브젝트를 생성 후 삽입
        MockMailSender mailSender = new MockMailSender();
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        userServiceImpl.setMailSender(mailSender);
        userServiceImpl.setUserDao(mockUserDao);

        // 테스트 대상 실행
        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();

        Assert.assertEquals(2, updated.size());
        checkUserAndLevel(updated.get(0), "2", Level.SILVER);
        checkUserAndLevel(updated.get(1), "4", Level.GOLD);

        // Mcok 오브젝트를 이용한 확인
        List<String> requests = mailSender.getRequests();
        Assert.assertEquals(requests.get(0), this.users.get(1).getEmail());
        Assert.assertEquals(requests.get(1), this.users.get(3).getEmail());
    }

    private void checkUserAndLevel(User user, String expectedId, Level level) {
        Assert.assertEquals(user.getId(), expectedId);
        Assert.assertEquals(user.getLevel(), level);
    }
```
    1. 고립테스트를 위해서 Spring의 DI를 사용하지 않는다.
    2. UserServiceImpl 객체를 직접 인스턴스화
    3. 의전 객체를 직접 Setter메서드를 이용해 주입
테스트의 목적인 부분만 확인하도록 테스트 준비과정인 유저 등록과 호출 부분을 Mock오브젝트 생성을 이용하여 단축시켰다.

## 6.2.3 단위 테스트와 통합 테스트
### 단위테스트, 통합테스트 가이드라인  (P424)
- 항상 단위테스트를 고려
- 외부 리소스를 사용해야하는 경우에는 `통합테스트`로 만든다.
- Dao를 테스트할 때는 DB연결까지 만드는 것이 효과적이다.
- Dao테스트는 외부리소스(DB)를 이용하기 때문에 `통합테스트로`분류 된다.
- 스프링 테스트 컨텍스트를 이용하는 테스트는 `통합테스트`다.
- 나머지 생략  (P424) 다시 읽자!

## 6.2.4 목 프레임워크
단위 테스트를 만들기 위해서는 스텁이나 목 오브젝트 사용이 필수적이다. 
의존관계가 없는 클래스나 세부 로직을 검증하기 위해 메서드 단위로 테스트 하는 것이 아니라면 대부분 의존 관계 오브젝트가 생성되어야 한다.
매번 테스트를 위한 목을 생성하는 것도 번거러운 일이다. 이러한 테스트를 위해서 `Mockito`프레임워크를 사용하자
### Mockito 프레임워크를 이용한 UserService.updateLevles테스트
- 테스트를 위해서 스텁 역할을 하는 클래스를 생성할 필요가 없다.
- 메서드 리턴값을 직접 설정할 수 있다. `when().thenReturn()`
- 등등 테스트를 위한 여러가지 기능을 제공
```
    @Test
    public void upgradeLevelsTest() throws Exception {
        // 목 프레임워크를 이용한 UserDao객체 생성
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);

        // 목 프레임워크를 이용한 MailSender객체 생성
        MailSender mockMail = mock(MailSender.class);

        UserServiceImpl userServiceImpl = new UserServiceImpl();

        userServiceImpl.setMailSender(mockMail);
        userServiceImpl.setUserDao(mockUserDao);

        // 테스트 대상 실행
        userServiceImpl.upgradeLevels();

        // mockUserDao 확인
        verify(mockUserDao,times(2)).update(any(User.class));
        verify(mockUserDao,times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        verify(mockUserDao).update(users.get(3));

        // mockMail 확인
        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor
            .forClass(SimpleMailMessage.class);
        verify(mockMail,times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        Assert.assertEquals(mailMessages.get(0).getTo()[0],users.get(1).getEmail());
        Assert.assertEquals(mailMessages.get(1).getTo()[0],users.get(3).getEmail());
    }
```

## 6.3.1 프록시와 프록시 패턴，데코레이터 패턴
UserServiceTx를 생성하여 트랜잭션 기능구현한 방법
- `전략패턴`을 이용한 핵심기능과 부가기능의 분리
- 핵심기능(기존의서비스로직)은 부가기능(트랜잭션)을 모른다.
- 부가기능(UserServiceTx)를 구현한 클래스도 해당 인터페이스(UserService)를 구현한 후 끼어들어야 한다.
- 클라이언트가 자신을 거쳐 핵심기능(UserServiceImpl)를 직접 사용해버린다면 부가기능을 사용할 수 없다.

### 프록시
클라이언트가 해당 객체를 사용하려고 할 때, 실제 대상인 것 처럼 위장하여 클라이언트의 요청을 받아주고, 같은 역할을 한는 것
프록시를 통해 최종적으로 요청을 처리하는 실제 오브젝트를 타켓 또는 실체라고 부른다.

### 데코레이션 패턴
테코레이션 패턴은 런타임 시 다이나믹하게 부가적인 기능을 추가하기 위해 프록시를 이용하는 패턴을 말한다.
코드상에서 어떤 방법과 순서로 프록시와 타켓을 연결되어 사용하는지 정해지지 않았다.
인터페이스를 통해서 DI하는 방법을 이용한다.

### 프록시 패턴
상위 서술한 `프록시`와 `프록시 패턴은` 구분할 필요가 있음.
- `프록시`란 클러이언트와 사용 대상 사이에 대리 역할을 하는 맡은 오브젝트를 두는 방법을 총칭
- `프록시 패턴`는 `프록시`를 사용하는 방법 중에서 타깃에 대한 접근 방식을 제어하려는 목적을 가진 경우
- `프록시패턴`의 `프록시`는 타깃의 기능에 부가적인 기능을 추가하는 것이 아니다. 
- `프록시패턴`을 적용한 대표적은 예로는 Collections의 `unmodifiableCollection()`
-  타깃의 기능 자체에는 관여하지 않으면서 접근히는 방법을 제어해주는 프록시를 이용하는 것이다.

## 6.3.2 다이내믹프록시
프록시는 두 가지 기능으로 구성된다. `위임`과 `부가작업`
1. 타깃과 같은 메소드를 구현하고 있다가 메소드가 호출되면 `타깃 오브젝트로 위임`한다
2. 지정된 요청에서 `부가기능`을 수행한다.
3. `UserServiceTx`에서 트랜잭션 기능이 추가된 `upgradeUserLeels()`함으로써  메서드가 `부가기능`이 추가된 것이다. 
4. 기타 메서드들은 의존 오브잭트로 주입된 `userServiceImpl`객체에게 위임한다.

### 리플랙션(reflection)
간단해서 생략

### 프록시 클래스
- Hello 인터페이스
```
public interface Hello {
    String sayHello(String name);
    String sayHi(String name);
    String sayThankYou(String name);
}
```
- Hello 인터페이스를 구현한 HelloTarget 클래스(@Override생략)
```
public class HelloTarget implements Hello {
    public String sayHello(String name) {
        return "Hello "+ name;
    }
    public String sayHi(String name) {
        return "Hi "+ name;
    }
    public String sayThankYou(String name) {
        return "Thank You "+ name;
    }
}
```
- Hello 인터페이스를 구현하고 구현체를 의존하는 HelloUppercase 클래스
    Hello인터페이스의 구현체를 주입받은 후, target(주입받은 구현체)에 `toUpperCase`라는 `부기가능`을 추가하면서
    기존의 주입받은 구현체의 메서드를 호출한다.
```
public class HelloUppercase implements Hello {
    private Hello hello;

    public HelloUppercase(Hello hello) {
        this.hello = hello;
    }
    public String sayHello(String name) {
        return hello.sayHello(name).toUpperCase();
    }
    public String sayHi(String name) {
        return hello.sayHi(name).toUpperCase();
    }
    public String sayThankYou(String name) {
        return hello.sayThankYou(name).toUpperCase();

    }
```
#### 문제점
1. 프록시를 추가할 때, 부가기능에 대한 class를 모두 직접 설계해야한다.
2. 부가기능인 대문자 변환로 변환하는 기능이 모든 메서드에 나타난다.

### 다이나믹 프록시 적용
- 다이나믹 프록시 오브젝트는 타깃 인터페이스와 동일한 타입으로 만들어진다.
- 다이나믹 프록시가 인터페이스 구현 클래스의 오브젝트를 만들어주지만 부가기능은 직접 코드로 작성해야 한다.
- 부가기능은 프록시 오브젝트와 독립적으로 `InvocationHandler`를 구현한 오브젝트에 담는다.
```
      public interface InvocationHandler {
          public Object invoke(Object proxy, Method method, Object[] args)
              throws Throwable;
      }
```
- 다이나믹 프록시 오브젝트는 클라이언트의 모든 요청을 리플랙션 정보로 변환하여 `InvocationHandler`구현체에게 `invoke()`메서드에 넘기는 것이다.
대문자 변환 부가기능인 `InvocationHandler`의 구현체
```
public class UppercaseHandler implements InvocationHandler {
    private Hello target;
    public UppercaseHandler(Hello target) {
        this.target = target;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String result =  (String)method.invoke(target,args);    // 타켓에게 위임
        return result.toUpperCase();    // 부가기능 실행
    }
}
```
- 수동으로 Proxy 객체 생성과 테스트
```
    @Test
    public void invocationHandlerTest(){
        Hello helloProxy = (Hello) Proxy.newProxyInstance(
            getClass().getClassLoader()
            , new Class[]{Hello.class}
            , new UppercaseHandler(new HelloTarget())       // 부기가능이 들어있는 `IncvocationHandler`구현제
        );
        String name = "youzheng";
        Assert.assertEquals(helloProxy.sayHello(name),"HELLO "+name.toUpperCase());
        Assert.assertEquals(helloProxy.sayHi(name),"HI "+name.toUpperCase());
        Assert.assertEquals(helloProxy.sayThankYou(name),"THANK YOU "+name.toUpperCase());
    }
```

### 다이나믹 프록시의 확장
- 인터페이스와 반환 타입이 다른 경우
`InvocationHandler`의 구현체를 사용할 때, 만약 지정된 클래스가 아닌 경우  테스트
```

public class OutputNumber {
    public int printNumber(int number) {
        return number * 2;
    }
}

```
- 테스트
`IllegalArgumentException`가 발생
```
    @Test(expected = IllegalArgumentException.class)
    public void invocationHandlerTest2(){
        OutputNumber helloProxy = (OutputNumber) Proxy.newProxyInstance(
            getClass().getClassLoader()
            , new Class[]{OutputNumber.class}
            , new UppercaseHandler(new HelloTarget())
        );
            helloProxy.printNumber(2);
    }
```
- 다이나믹 프록시의 확장을 위해서, `UppercaseHandler` 수정
    1. Hello인터페이스의 구현체를 딱 정해놓지 않고 받는다.
    2. invoke() 메서드 내부에서 Object의 클래스 타입을 확인한 후, 지정된 타입이 맞다면 부가기능 제공
    3. meothd().startWith([prefix])를 통해서 특정 메서드에만 부가기능을 제공할 수도 있다.
```
public class UppercaseHandler implements InvocationHandler {

    private Object target;

    public UppercaseHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result =  method.invoke(target,args);    // 타켓에게 위임

        if(result instanceof String){
            return ((String) result).toUpperCase();    // 부가기능 실행
        }
        return result;
    }
}
```

## 6.3.3 다이내믹 프록시를 이용한 트랜잭션 부가기능
- 트랜잭션 부가기능을 적용한 `InvocationHandler`의 구현체
```
public class TransactionHandler implements InvocationHandler {

    // 타겟 오브젝트
    private Object target;
    // 부가기능을 위한 오브젝트
    private PlatformTransactionManager transactionManager;
    // 부가기능 적용 대상 메서드의 패턴
    private String pattern;

    / Setter 메서드 생략..

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.getName().startsWith(pattern)) {
            return invocationTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invocationTransaction(Method method, Object[] args)
        throws InvocationTargetException, IllegalAccessException {
        TransactionStatus status = transactionManager
            .getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            this.transactionManager.commit(status);
            return result;
        } catch (InvocationTargetException e) {
            this.transactionManager.rollback(status);
            throw e;
        } catch (IllegalAccessException e) {
            this.transactionManager.rollback(status);
            throw e;
        }
    }
}
```
- 테스트 적용
```
 @Test
    public void upgradeAllOrNothing() {
        // TestService 생성 및 의존 객체 주입 생략

        TransactionHandler transactionHandler = new TransactionHandler();
        transactionHandler.setTarget(testService);
        transactionHandler.setTransactionManager(this.transactionManager);
        transactionHandler.setTarget("updateLevels");

        UserService userServiceTx = (UserService) Proxy.newProxyInstance(getClass().getClassLoader()
            , new Class[]{UserService.class}
            , transactionHandler
        );
        // 검사 로직 생략
```

## 6.3.4 다이나믹 프록시를 위한 팩토리 빈
다이나믹 프록시를 구현한 클래스를 스프링 컨텍스트가 관리하는 빈으로 할 수 없다.
- Dl의 대상이 되는 다이내믹 프록시 오브젝트는 일반적인 스프링의 빈으로 등록할 방법이 없다.
- 스프링 빈은 기본적으로 `클래스 이름`과 `프로퍼티 타입`으로 정의된다.
- 스프링은 내부적으로 리플랙션 API를 이용해 등록된 빈을 생성하지만 다이나믹 프록시 오브젝트는 이런 방식으로 생성할 수 없다.
- 다이나믹 프록시는 Proxy의 `newProxyInstance()`메서드를 통해서만 만들 수 있다.

스프링에 다이나믹 프록시 빈을 등록하기 위해서는 `팩토리 빈` 인터페이스를 이용해야한다.
생성자가 `private`로 설정된 경우에도 스프링은 리플랙션을 이용하여 오브젝트를 만들어 주지만, 내부적으로 생성자를 통한 오브젝트 생성을
막은 오브젝트를 강제로 생성하면 위험하다.

### FactoryBean 인터페이스를 이용한 스프링 빈 등록
- 생성자가 `private`로 설정된 Message 클래스 
`newMessage()` 메서드를 이용해서 객체를 생성할 수 있다.
```
public class Message {
    private String text;
    private Message(String text) {
        this.text = text;
    }
    public String getText() {
        return this.text;
    }
    public static Message newMessage(String text) {
        return new Message(text);
    }

}
```
- FactoryBean 인터페이스를 구현한 MessageBeanFactory(오버라이드 생략)
```
public class MessageFactoryBean implements FactoryBean<Message> {
    private String text;
    public void setText(String text) {
        this.text = text;
    }
    public Message getObject() throws Exception {
        return Message.newMessage(this.text);
    }
    public Class<?> getObjectType() {
        return Message.class;
    }
    public boolean isSingleton() {
        return false;
    }
}
```
- application.xml에 MessageFactoryBean 등록
```
  <bean id="message" class="springbook.learningtest.factorybean.MessageFactoryBean" >
    <property name="text" value="Factory Bean" />
  </bean>
```
- 테스트
```
    @Test
    public void factoryBeanTest() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/applicationContext.xml");
        Message message = context.getBean("message", Message.class);
        Assert.assertEquals("Factory Bean", message.getText());

        // &를 붙이면 FactoryBean 구현체를 가져온다.
        Object factoryBean = context.getBean("&message");
        Assert.assertTrue(factoryBean instanceof MessageFactoryBean);

    }
```

### FactoryBean 구현체를 이용한 트랜잭션 프록시 빈 생성
- TxFactoryBean
    1. FactoryBean 인터페이스 구현, 범용적 사용을 위하여 타입을 특정 인터페이스가 아닌 Obejct로 설정
    2. `부가기능`을 갖고 구현한 프록시 빈을 생성하기 위한 target,pattern 등 값을 setter메서드를 통해서 주입
```
public class TxProxyFactoryBean implements FactoryBean<Object> {

    private Object target;
    private PlatformTransactionManager transactionManager;
    private String pattern;
    private Class<?> serviceInterface;

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTransactionManager(
        PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object getObject() throws Exception {
        TransactionHandler txHandler = new TransactionHandler();
        txHandler.setTarget(this.target);
        txHandler.setTransactionManager(this.transactionManager);
        txHandler.setPattern(this.pattern);

        return Proxy.newProxyInstance(getClass().getClassLoader()
            , new Class[]{serviceInterface}
            , txHandler
        );
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
```
- application.xml 빈 등록
```
  <bean id="userService" class="springbook.user.service.TxProxyFactoryBean">
    <property name="pattern" value="upgradeLevels"/>
    <property name="transactionManager" ref="transactionManager"/>
    <property name="serviceInterface" value="springbook.user.service.UserService"/>
    <property name="target" ref="userServiceImpl"/>
  </bean>
```
- 기존 테스트케이스 코드 수정
```
    @Test
    @DirtiesContext // 해당 테스트만 빈 의존관계가 변경되도록 애너테이션을 꼭 붙이자.
    public void upgradeAllOrNothing() throws Exception {
        // TestService, MockSendMail 생성 및 주입 생략..

        TxProxyFactoryBean factoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
        factoryBean.setTarget(testService);

        UserService userService = (UserService)factoryBean.getObject();

        // 업그레이드 및 검증로직 생략..
    }
```

### 프록시 빈 팩토리의 한계
1. 한 번에 여러개의 클래스에 적용할 수 없다.
    - 프록시를 이용한 부가기능 제공은 `메서드 단위`로 일어난다.
    - 해당 클래스의 모든 메서드에 부가기능을 제공하는 것은 간단하다
    - 하지만 여러 개의 부가기능을 한 번에 적용할 수 없다.
2. 하나의 타겟에 여러 개의 부가기능을 적용할 수 없다.

## 6.4.1 ProxyFactoryBean
### 스프링의 ProxyFactoryBean
- 프록시를 생성하여 빈으로 등록하게 도와주는 팩토리 빈
- `MethodInterceptor`인터페이스를 이용하여 `부가기능`을 생성
- `InvocationHandler()`의 경우에는 오브젝트에 대한 정보를 제공하지 않는다.
- 하지만 `MethodInterceptor`의 메서드는 `ProxyFactoryBean`으로 부터 오브젝트에 관한 정보까지 함께 제공받는다.
- 타겟 오브젝트에 상관없이 독립으로 생성할 수 있다.
- `MethodInterceptor`의 오브젝트는 다른 여러 프록시와 함께 사용 가능(Advice인터페이스를 상속한 서브인터페이스)
- 싱글톤으로 등록 가능

### ProxxyFactoryBean을 이용한 부가기능 추가
- `MethodInterceptor`인터페이스 구현으로 부가기능 설정
    1. `proceed()`메서드를 통해서 타켓 오브젝트의 메서드를 실행
    2. `MethodInvocation`를 통해서 오브젝트의 구체적인 정보를 전달받는다.
    3. 부가기능에 대한 로직에 집중할 수 있다.
    4. 범용적으로 사용하기 위하여 싱글톤으로 등록한다. (상태 값을 저장할 수 없다.)
    5. `MethodInterceptor`는 부가기능만 담는 객체이며
    6. 타켓을 선정하는 역할은 `PointCut`을 이용한다.
```
    static class UpperAdvice implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            String proceed = (String) methodInvocation.proceed();
            return proceed.toUpperCase();
        }
```
- `ProxyFactoryBean`을 통한 ProxyBean 생성
    1. `setTarget()`메서드로 부가기능이 적용될 타켓 오브젝트 설정
    2. `addAdvice()`메서드로 부기가능 추가, set이 아니고 add인 이유는 다수의 부기가능을 추가할 수 있기 때문이다.
    3. `템플릿/콜백` 패턴을 이용했다. ProxyFactoryBean이 `템플릿`, MethodInterceptor가 `콜백`
```
    @Test
    public void springProxyFactoryBeanTest(){
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new HelloTarget());
        proxyFactoryBean.addAdvice(new UpperAdvice());

        Hello hello = (Hello) proxyFactoryBean.getObject();

        String name = "youzheng";
        Assert.assertEquals(hello.sayHello(name),"HELLO "+name.toUpperCase());
        Assert.assertEquals(hello.sayHi(name),"HI "+name.toUpperCase());
        Assert.assertEquals(hello.sayThankYou(name),"THANK YOU "+name.toUpperCase());
    }

```

## 6.4.2 ProxyFactoryBean 적용
ProxyFactoryBean은 대상을 선별하는 포인트컷과 실제 부가기능을 추가하는 어드바이스를 `템플릿/콜백`패턴을 이용하여 분리했다.
이 둘을 가지고 있는 객체를 어드바이저라고 하고, 포인트컷과 어드바이스를 빈으로 등록하면 재활용이 가능하다.
- Advisor, Advice, Pointcut, ProxyFactoryBean 등록
부기가능에 대한 구체적인 구현이 있는 `Advice`를 제외하면 클래스를 구현하지 않고 바로 xml을 통해서 등록해도 된다.
```
 <bean id="userService" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target" ref="userServiceImpl"/>
    <property name="interceptorNames">
      <list>
        <value>transactionAdvisor</value>
      </list>
    </property>
  </bean>

  <bean id="transactionAdvisor" class="org.springframework.aop.support.DefaultPointcutAdvisor">
    <property name="advice" ref="transactionAdvice"/>
    <property name="pointcut" ref="transactionPointcut"/>
  </bean>

  <bean id="transactionAdvice" class="springbook.user.service.TransactionAdvice">
    <property name="transactionManager" ref="transactionManager"/>
  </bean>

  <bean id="transactionPointcut" class="org.springframework.aop.support.NameMatchMethodPointcut">
    <property name="mappedName" value="upgradeLevels"/>
  </bean>
```
- MethodInterceptor인터페이스를 구현한 TransactionAdvice(어드바이스) 
```
public class TransactionAdvice implements MethodInterceptor {

    private PlatformTransactionManager transactionManager;
    // Setter 생략

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        TransactionStatus status = transactionManager
            .getTransaction(new DefaultTransactionDefinition());

        try {
            Object proceed = methodInvocation.proceed();
            transactionManager.commit(status);
            return proceed;
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
```
// 테스트 생략

## 6.5.1 자동 프록시 생성
### 빈 후처리기를 이용한 자동 프록시 생성기
빈 후처리기 : `BeanPostProcessor` 인터페이스를 구현해서 만든다. 스프링이 빈을 둥록한 후 빈 오브젝트를 가공한다.

### DefaultAdvisorAutoProxyCreator
스프링이 제공하는 자동 프록시 빈 생성기
- 빈 후처리기 자체를 빈으로 등록
- 빈 오브젝트를 강제로 수정, 별도의 초기화 작업을 수행할 수도 있다.
- 어드바이저 내의 포인트컷을 확인하여 전달받은 빈이 프록시 대상인지 확인
- 프록시 적용 대상이라면 내장된 프록시 생성기를 이용해 프록시 빈을 생성, 어드바이스를 연결한다.
- 프록시 빈이 생성되면, 원래 오브젝트 대신 생성한 프록시 빈을 컨테이너에게 전달한다.

### Pointcut
1. `MethodMatcher`에 등록된 메서드 이름을 통해서 해당 오브젝트의 메서드가 프록시 대상인지 확인한다.
2. `ClassFilter`에 등록된 클래스 종류를 통해서 해당 오브젝트가 프록시 대상인지 확인한다.
3. `ClassFilter`를 확인 후, `MethodMatcher`에 등록된 메서드 이름을 확인하기 때문에, 대상 클래스가 아니라면 메서드 이름을 확인하지 않는다.
4. 이 두가지 조건을 전부 만족해야 해당 오브젝트에 어드바이스(부가기능)이 적용된다.
// Pointcut 관련 테스트 예제 생략 `ReflectionTest` 하단 참고

## 6.5.2 DefaultAdvisorAutoProxyCreator의 적용
### 클래스 필터를 이용한 포인트컷 작성
- `NameMatchMethodPointcut`를 상속해서 만든다.
    1. `SimpleClassFilter`내부클래스로 만든다.
    2. `SimpleClassFilter`를 
```
public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {

    public void setMappedClassName(String mappedClassName) {
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    static class SimpleClassFilter implements ClassFilter {

        private String mappedClassName;

        private SimpleClassFilter(String mappedClassName) {
            this.mappedClassName = mappedClassName;
        }

        @Override
        public boolean matches(Class<?> aClass) {
            return PatternMatchUtils.simpleMatch(mappedClassName, aClass.getSimpleName());
        }
    }
}
```
- applicationContext.xml에 `DefaultAdvisorAutoProxyCreator`등록
    1. Advisor 인터페이스를 구현한 것을 모두 찾는다.
    2. 빈 클래스가 어드바이스의 포인트컷 대상이라면 프록시 대상으로 적용된다.
    3. `id`와`attribute`가 없어도 된다.
```
  <!-- 프록시 자동 생성 등록-->
  <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>
  <!-- 프로바이저 = 어드바이스(부가기능) + 포인트컷(포인트컷 대상 선정기준)-->
  <bean id="transactionAdvisor" class="org.springframework.aop.support.DefaultPointcutAdvisor">
    <property name="advice" ref="transactionAdvice"/>
    <property name="pointcut" ref="transactionPointcut"/>
  </bean>

  <bean id="transactionAdvice" class="springbook.user.service.TransactionAdvice">
    <property name="transactionManager" ref="transactionManager"/>
  </bean>

  <bean id="transactionPointcut" class="springbook.user.service.NameMatchClassMethodPointcut">
    <property name="mappedClassName" value="*ServiceImpl" />
    <property name="mappedName" value="upgrade*" />
   </bean>

  <bean id="userService" class="springbook.user.service.UserServiceImpl">
    <property name="userDao" ref="userDao"/>
    <property name="mailSender" ref="mailSender" />
  </bean>
```
`DefaultAdvisorAutoProxyCreator`에 의해서 `UserServiceImpl`가 자동으로 부가기능이 추가된 프록시 빈으로 변경되기 때문에
별다른 설정을 하지 않고 서비스 로직만 구현한 `userServiceImpl`를 바로 빈으로 등록한다.
### 빈 후처리기를 이용한 트랜잭션 등록 방법
1. 의존 객체 설계
    - 부가기능 : 트랜잭션 기능은 `MethodInterceptor`인터페이스를 구현하여 만든다.
    - 포인트컷 : `NameMatchMethodPointcut`를 상속하여 class 이름으로도 프록시 대상을 판별하도록 만든다. 
    - 어드바이저 : 상위 2가지를 담고 있는 객체, `DefaultPointcutAdvisor`인터페이스를 빈으로 등록한다.
```
     <bean id="transactionAdvisor" class="org.springframework.aop.support.DefaultPointcutAdvisor">
       <property name="advice" ref="${adviceId}"/>
       <property name="pointcut" ref="${pointcutId}"/>
     </bean>
```
2. 프록시 빈을 자동으로 등록해주는 객체 `DefaultAdvisorAutoProxyCreator`
```
  <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>
```

### 트랜잭션 어드바이스를 적용한 프록시 자동 생성기를 빈 후처리기 메커니즘을 적용할 시 주의점
1. 원하는 대상이 부가기능(트랜잭션)이 적용되었는가?
2. 원하는 대상을 제외한 기타 객체들 또한 부기가능이 적용되었는가? (`비슷한 이름`, 혹은 `동일한 인터페이스를 구현한 객체`)
    - 1번을 실행 한 후, 등록된 `pointcut`의 적용 대상을 변경해서 다시 테스트를 실행한다. ex) *serviceImpl ->*NotserviceImpl로 변경
    ```
         <bean id="transactionPointcut" class="springbook.user.service.NameMatchClassMethodPointcut">
           <property name="mappedClassName" value="*NotServiceImpl" />
           <property name="mappedName" value="upgrade*" />
          </bean>
   ```
    - 어드바이스가 적용될 프록시의 타켓은 프록시로 변경된다면 Proxy.class로 변경도니다
    ```
       @Test
       public void proxyObjectTest(){
           Assert.assertTrue(testUserService instanceof Proxy);
           Assert.assertTrue(userService instanceof Proxy);
       }
   ```
   
## 6.5.3 포인트컷 표현식을 이용한 포인트컷
### 포인트컷표현식
- AspectJExpressionPointcut 클래스를 이용
- 자세한 내용은 ~P497 참고 및 구글링

### 기존의 포인트컷을 포인트컷 표현식으로 변경
```
  <bean id="transactionPointcut" class="org.springframework.aop.aspectj.AspectJExpressionPointcut">
    <property name="expression" value="execution(* *..*ServiceImpl.upgrade*(..))" />
  </bean>
```
### 포인트컷 표현식 주의점
`execution(* *..*ServiceImpl.upgrade*(..))`
1. 포인트컷 표현식에서 클래식 이름에 적용되는 패턴은 `클래스 이름`이 아니라 `클래스 타입`이다.
2. `UserServiceImpl`는 `UserService`인터페이스의 구현체이기 때문에, `UserService`를 구현한 모든 구현체이 포인트컷의 대상이 된다.
3. 포인트 컷은 `타입 패턴`을 기준으로 원리가 작동한다.

~ P508 내용은 다시 읽자!
## 6.5.6 AOP의 용어
1. 타겟
    - 부가기능을 구현한 대상
    - 경우에 따라서는 다른 부가기능을 제공하는 프록시 오브젝트일 수도 있다.(예:데코레이션 패턴)
2. 어드바이스
    - 타겟에게 제공할 부가기능을 담은 모듈
    - 오브젝트로 정의하지만 메서드 레벨에서 정의할 수도 있다.
    - `Methodlnterceptor`인터페이스를 구현해서 만들었다.
3. 조인 포인트
    - 어드바이스가 적용될 위치
    - 스프링의 프록시 AOP에서 조인포인트는 메서드의 실행 단계뿐이다.
    - 타겟 오브젝트가 구현한 인터페이스의 모든 메서드는 조인 포인트가 된다.
4. 포인트컷
    - 어드바이스가 적용할 조인 포인트를 선별하는 작업 또는 그 기능을 정의한 모듈
    - 메서드 선정이란 곧 결국 클래스를 선정하고 그 안의 메서드를 선별하는 과정을 거친다.
5. 프록시
    - 클라이언트와 타겟 사이에서 부가기능을 제공하는 오브젝트
    - 클라이언트의 호출을 대신 받아서 부가기능을 제공한 후 타겟에게 위임한다.
6. 어드바이스
    - 포인트컷과 어드바이스를 갖고 있는 오브젝트
    - 스프링 프록시 AOP에서 사용되는 용어, 일반적인 AOP에서는 사용되지 않는다.
7. 에스팩트
    - 포인트컷과 어드바이스의 조합으로 만들어진다.
    - 싱글톤 형태로 존재한다.
    - 클래스와 같은 모률 정의와 오브젝트와 같은 실제의 구분이 특별히 없다.(두 가지 모두 에스팩트라 부른다)

## 6.5.7 AOP 네임스페이스
### 스프링의 프록시 방식 AOP를 적용을 위한 네 가지 빈
1. 자동 프록시 생성기
    - `DefaultAdvisorAutoProxyCreator`을 빈으로 등록
    - `id`와 `attribute`는 따로 등록하지 않아도 된다.
    - 빈이 생성되는 과정에서 `빈 후처리기`로 참여한다.
    - 빈으로 등록된 어드바이스를 이용해서 프록시를 자동으로 생성하는 기능을 담당
2. 어드바이스
    - 부가기능을 구현한 클래스. ex) `MeothodInterceptor`인터페이스
    - AOP관련 빈 중에서 유일하게 직접 구현한다.
3. 포인트컷
    - `AspectJExpressionPointcut`을 이용해 등록
    - `expression`을 이용해 어드바이스가 적용될 클래스, 메서드를 설정한다.
    - 선정 기준은 클래스이름이 아니고 `클래스 타입`이다! 
4. 어드바이스
    - `DefaultPointcutAdvisor`를 이용해서 빈으로 등록
    - 프로퍼티로 `어드바이스`와 `포인트컷` 구현체를 받는다.
    - 자동 프록시 생성기에 의해서 검색되어 사용된다.
### AOP 네임스페이스
```
  <AOP:config >
    <AOP:pointcut id="transactionPointcut" expression="execution(* *..*ServiceImpl.upgrade*(..))"/>
    <AOP:advisor advice-ref="transactionAdvice" pointcut-ref="transactionPointcut"   />
  </AOP:config>

  <bean id="transactionAdvice" class="springbook.user.service.TransactionAdvice">
    <property name="transactionManager" ref="transactionManager"/>
  </bean>
```
1. <AOP:config > : `DefaultAdvisorAutoProxyCreator`을 빈으로 등록해준다.
2. <AOP:pointcut > : 포인트컷을 설정 `AspectJExpressionPointcut`를 등록하는 것과 동일
3. <AOP:advisor > : `DefaultPointcutAdvisor`을 등록 `advice`와 `pointcut`을 프로퍼티로 갖는다.
    - 내장 포인트컷을 이용할수도 있다
    `<AOP:advisor advice-ref="transactionAdvice" 
                pointcut="execution(* *..*ServiceImpl.upgrade*(..))" />`

## 6.6.1 트랜잭션 정의
### 트랜잭션 전파(Transaction Propagation) 
트랜잭션의 경계에서 이미 진행중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식
propagate: 전파하다

1. PROPAGATION_REQUIRED
    - 가장 많이 사용되는 `트랜잭션 전파 속성`
    - 이미 진행되고 있는 트랜잭션이 있다면 `참여`, 없다면 `새로 시작`
    - `DefaultTransactionDefinition`의 트랜잭션 전파속성이 `PROPAGATION_REQUIRED`
2. PROPAGATION_REQUIRES_NEW
    - 항상 독자적으로 `새로운 트랜잭션을 시작`
    - 독립적인 트랜잭션이 보장되어야 하는 상황에서 사용
3. PROPAGATION_NOT_SUPPORTED
    - 진행중인 트랜잭션이 있더라도 무시
    - 트랜잭션이 없이 동작하도록 만든다

### 격리수준(Isolation Level)
- 기본적으로는 DB나 DataSource 에 설정된 디폴트 격리 수준을 따르는 것이 좋다.
-  `DefaultTransactionDefinition`의 격리수준은 `Default`이다

### 제한 시간(Timeout)
- `DefaultTransactionDefinition`의 제한 시간은 없다
- `PROPAGATION_REQUIRED`나 `PROPAGATION_REQUIRES_NEW`와 함께 사용해야만 의미가 있다.

### 읽기 전용(Read Ony)
- DB에 접속시 트랜잭션 내에서 데이터 조작을 금지하게 한다.
- 데이터 엑세스 기술에 따라서 성능이 상향될 수도 있다.

## 6.6.2 트랜잭션 인터셉터와 트랜잭션 속성
메서드 별로 다른 트랜잭션 정의를 하려면 어드바이스의 기능을 확장해야 한다.
### TransactionInterceptor
스프링에서 지원하는 `TransactionInterceptor`를 사용한다. `TransactionAdvice`를 사용할 필요가 없다.
기존의 `TransactionAdvice`의 어드바이스를 제공하는 기능에 메서드 이름 패턴을 지정하는 기능이 추가된 것이다.
`PlatformTransactionManager`와 `Properties`(transactionAttributes) 타입의 두 가지 프로퍼티를 갖는다.
`rollbackOn()`메서드에 상위 프로퍼티가 사용된다.
1. `TransactionInterceptor`
- 런타임 예외가 발생하면 `rollback`한다.
- 런타임 예외가 아닌 체크예외를 발생시키면 예외상황으로 인식하지 않는다(rooback하지 않고 commit)

2. `Properties` 트랜잭션 속성 지정
    1. PROPAGATION_[VALUE]	: 트랜잭션 전파 방식, `필수사항`
    2. ISOLATION_[VALUE] 	: 격리 수준, 생략시 기본값으로 지정된다.
    3. readOnly				: 읽기전용으로 지정, 기본값은 false
    4. timeout_[seconds]		: 제한시간, 생략가능
    5. -Exceptionl			: `체크 예외` 중에서 롤백할 대상을 추가한다.
    6. +Exceptionl			: `런타임 예외`지만 롤백할 대상에서 제외한다.

3. applicationContext.xml에 `TransactionInterceptor`등록
```
  <bean id="transactionAdvice" class="org.springframework.transaction.interceptor.TransactionInterceptor" >
    <property name="transactionManager" ref="transactionManager" />
    <property name="transactionAttributes" >
      <props>
        <prop key="get*">PROPAGATION_REQUIRED,readOnly,timeout_30</prop>
        <prop key="upgrade*">PROPAGATION_REQUIRES_NEW,ISOLATION_SERIALIZABLE</prop>
        <prop key="*">PROPAGATION_REQUIRED</prop>
      </props>
    </property>
  </bean>
```

### tx네임스페이스를 이용한 설정 방법
tx 스키마의 전용 태그를 이용해 `TransactionInterceptor`와 `transactionAttributes`를 지정할 수 있다.
IDE의 도움을 받을 수 있어서(자동완성), 실수할 확율이 매우 적다.
```
  <tx:advice id="transactionAdvice" transaction-manager="transactionManager" >
    <tx:attributes>
      <tx:method name="get*" propagation="REQUIRED" read-only="true" timeout="30"/>
      <tx:method name="upgrade*" propagation="REQUIRES_NEW" isolation="SERIALIZABLE" />
      <tx:method name="*" propagation="REQUIRED" />
    </tx:attributes>
  </tx:advice>
``` 
## 6.6.3 포인트컷과 트랜잭션 속성의 적용 전략
1. 트랜잭션 포인트컷 표현식은 타입 패턴이나 번 이름을 이용한다
    - 일반적으로 트랜잭션을 적용할 타깃 클래스의 메소드는 모두 트랜잭션 적용 후보가 되는 것이 바람직하다.
    - 너무 세세하게 메서드 단위로 트랜잭션을 줄 필요는 없다.
    - 클래스보다는 인터페이스 타입에게 트랜잭션을 준다.
    - `bean() 표현식`을 이용해서 타겟을 선정하는 것도 고려해보자.  
      `bean() 표현식`은 빈 이름을 기준으로 메서드를 선정한다.
2. 공통된 메소드 이름 규칙을 통해 최소한의 트랜잭션 어드바이스와 속성을 정의한다.
    - 읽기 속성을 넣는 경우에는 메서드의 접두사를 `get`,`find`와 같이 지정하여 규칙을 지정해준다.
3. 프록시 방식 AOP는 같은 타깃 오브젝트 내의 메소드를 호출할 때는 적용되지 않는다.(주의사항!)
    - 타깃 오브젝트가 트랜젹선 대상이라 할지라도 트랜잭션 오브젝트를 통한 호출이 아닌 타깃 오브젝트 내부에서 
    메서드 호출이라면 트랜잭션이 적용되지 않는다.
    - 같은 타깃 오브젝트 안에서 메소드 호출이 일어나는 경우에는 프록시 AOP를 통해 부여해준 부가기능이 적용되지 않는다.

## 6.6.4 트랜잭션 속성 적용
UserService에 aop 프록시를 이용한 트랙잭션 적용
1. xml 설정
기존에 사용하던 H2 DB를 MySql로 변경했다.
H2에서는 readOnly설정을 하더라도 예외가 발생하지 않는 문제를 해결하지 못했다..
```
  <bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
    <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url"
      value="jdbc:mysql://127.0.0.1:3306/book?useSSL=false&amp;serverTimezone=Asia/Seoul"/>
    <property name="username" value="root"/>
    <property name="password" value="1234"/>
  </bean>

  <AOP:config >
    <AOP:advisor advice-ref="transactionAdvice" pointcut="bean(*Service)" />
  </AOP:config>

  <tx:advice id="transactionAdvice" >
    <tx:attributes>
      <tx:method name="get*" read-only="true"/>
      <tx:method name="*" />
    </tx:attributes>
  </tx:advice>
```

2. UserService에 메서드 추가 (코드 생략)

3. readOnly 설정 확인을 위한 테스트 코드
TestUserService의 코드에서 getAll()메서드를 오버라이딩했다.
getAll()메서드가 진행되는 중에 update()메서드를 사용하도록 변경(코드생략)
```
    @Test(expected = TransientDataAccessResourceException.class)
    public void readOnlyExceptionTest(){

        this.userDao.addAll(this.users);

        testUserService.getAll();

    }
```

## 6.7.1 트랜잭션 애노테이션 `@Transactional`
- 포인트컷은 `TransactionAttributeSourcePointcut`을 이용한다.
- 따로 선정기준을 가지지 않는다.
- `@Transactional`을 갖고 있는 모든 빈을 찾아서 트랜잭션을 적용한다.
- 트랜잭션을 적용하는 동시에 애노테이션을 이용하여 속성 또한 적용한다.
- 메서드 단위로 세분화해서 트랜잭션을 적용할 수 있다. 때문에 매우 세밀한 트랜잭션 속성 제어가 가능한다.

### 대체정책
1. `메서드`에 트랜잭션 애노테이션이 있는지 확인 없다면 `클래스` 확인
2. `클래스`에 트랜잭션 애노테이션이 있는지 확인 없다면 `인터페이스` 확인
3. `인터페이스의 메서드`에 트랜잭션 애노테이션이 있는지 확인 없다면 `인터페이스`상위 확인
4. 여기에도 없다면 그냥 없는 것이다.
5. `인터페이스`에 트랜잭션을 놓는 것이 트랜잭션 속성을 유지할 하나의 방법, 구현체가 바뀌더라도
    인터페이스에 설정된 트랜잭션 속성도 따라간다.

### 트랜잭션 애노테이션을 사용을 위한 설정
annotationContext.xml에 아래와 같은 설정을 추가한다.
```
<tx :annotation-driven />
```
xml 설정에 있던 트랜잭션 설정을 모두 제거하고 UserService 인터페이스에 기존 설정을
애노테이션을 이용하여 설정해준다.(생략)

## 6.8.1 선언적 트랜잭션과 트랜잭션 전파 속성
선언적 트랜잭션 : AOP를 이용해 코드 외부에서 트랜잭션의 기능을 부여해주고 속성을 지정 
프로그램에 의한 트랜잭션 :`TransactionTemplate`이나 개별 데이터 기술의 트랜잭션 API를 사용해 직접 코드 안에 
특별한 경우가 아니라면 `선언적 트랜잭션`을 이용

## 6.8.2 트랜잭션 동기화와 테스트
AOP를 이용한 프록시 기술로 트랜잭션이라는 부가기능을 애플리케이션 전반에 적용할 수 있다.
AOP가 없었다면 선언적 트랜잭션과 트랜잭션 전파 등은 불가능 했을 것이다.

### 트랜잭션 매니저와 트랜잭션 동기화
트랜잭션 추상화 기술의 핵심은 트랜잭션 매니저와 트랜잭션 동기화
`PlatformTransactionManager`의 구현체를 이용하여 트랜잭션 기술을 추상화했고
트랜잭션 기술의 종류에 상관 없이 일관적인 트랜잭션 기술을 적용할 수 있다.
1. 트랜잭션 매니저를 이용한 트랜잭션을 미리 시작하는 방법
deleteAll(), add() 이 두가지 메서드는 전부 트랜잭션 기술이 적용되어 있다.
전파 수준이 `REQUIRED`로 되어 있기에 사전에 시작한 트랜잭션 경계가 존재한다면
해당 트랜잭션에 합류한다.
`transactionManager`를 이용해 3개의 메서드가 실행되기 전에 트랜잭션 경계를 시작한다면
후에 시작되는 3개의 메서드는 해당 트랜잭션에 합류한다.
`transactionManager`를 이용해서 `setReadOnly(true)` 읽기전용 속성을 추가했다.
deleteALl()메서드는 DB의 값을 조작하기 때문에 `TransientDataAccessResourceException`가 발생한다.
즉, `getTransaction()`을 통해서 시작된 트랜잭션이 아래에 있는 다른 메서드(전파=`REQUIRED`)에게 영향을
미치는 것이다.
```
    @Test(expected = TransientDataAccessResourceException.class)
    public void transactionSync() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setReadOnly(true);
        TransactionStatus status = transactionManager.getTransaction(definition);

        userService.deleteAll();
        userService.add(users.get(0));
        userService.add(users.get(1));
        transactionManager.commit(status);
    }
```
2. 트랜잭션 동기화 테스트 코드
```
    @Test
    public void transactionSync() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        Assert.assertEquals(0,this.userDao.getCount());
        userService.deleteAll();
        userService.add(users.get(0));
        userService.add(users.get(1));
        Assert.assertEquals(2,this.userDao.getCount());


        transactionManager.rollback(status);
        Assert.assertEquals(0,this.userDao.getCount());
    }
```

## 6.8.3 테스트를 위한 트랜잭션 애노테이션
테스트에서 `@Transactional`를 사용할 수 있다. 예전에 블로그에서 보고 몇 번 적용해 봤었지만
테스트 롤백이 실행되지 않았던 이유는 `<tx :annotation-driven />`를 설정하지 않았기 때문이다..

### 트랜잭션에 관한 내용은 따로 블로그에 정리

# 7장 스프링 핵심 기술의 응용
## 7.1 Sql과 Dao의 분리
Dao안에 있는 Sql은 변경될 가능성이 있기 때문에 Dao와 분리한다.

## 7.1.1 XML 설정을 이용한 분리
### 개별 SQL 프로퍼티 방식
각 클래스에 필요한 Sql을 필드 변수로 저장하는 방식, Dao의 수량이나 메서드의 양이 늘어난다면 관리하기 힘듬
`
    private static final SQL_ADD = "insert into users([properties..] valuse(?,?...))"
    // Sql이 필요한 부분에 해당 상수를 넣어주고 applicationContext에서 해당 sql을 설정하는 방식이다.
` 
### SQL 맵프로퍼티 방식

1. applicationContext를 통해서 빈 생성시 query를 갖고 있는 Map을 주입 해준다.
```
  <bean id="userDao" class="springbook.user.dao.UserDaoJdbc">
    <property name="dataSource" ref="dataSource"/>
    <property name="sqlMap">
      <map>
        <entry key="add"
          value="insert into users(id,name,password,level,login,recommend,email ) values(?,?,?,?,?,?,?)"/>
        <entry key="get" value="select * from users where id= ?"/>
        <entry key="deleteAll" value="delete from users"/>
        <entry key="getCount" value="select count(*) from users"/>
        <entry key="update"
          value="update users set name=?, password=?, level=?, login=?,recommend=?,email=? where id=?"/>
        <entry key="getAll" value="select * from users order by id" />
      </map>
    </property>
  </bean>
```
2. UserDaoJdbc 적용
sqlMap에서 해당 메서드에게 필요한 쿼리를 찾아서 실행
```
public class UserDaoJdbc implements UserDao {
    private Map<String, String> sqlMap;
    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }
    public void add(final User user) throws DuplicateKeyException {
        this.jdbcTemplate.update(
            this.sqlMap.get("add")
            , user.getId(), user.getName(), user.getPassword(), user.getLevel().intValue(),
            user.getLogin(), user.getRecommend(), user.getEmail());
    }
    // 기타 생략..
}
```
## 7.1.2 SQL제공 서비스
상위 두 가지 방법은 Dao와 Sql을 분리했지만 스프링 빈을 설정하는 xml에 Sql을 저장할 필요가 없다.
독립적인 Sql 제공 서비스가 필요하다. 스프링 빈 설정에서 Sql을 또 분리한다. 

### SQL 서비스 인터폐이스

1. SqlService 인터페이스 생성 및 SimpleSqlService(Map을 통해 저장) 구현
```
public class SimpleSqlService implements SqlService {

    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";
    private Map<String, String> sqlMap;

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }
    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        if (!this.sqlMap.containsKey(key)) {
            throw new SqlRetrievalFailureException(key + ERROR_MESSAGE);
        }
        return this.sqlMap.get(key);
    }
}
```
2. applicationContext 수정
sqlService 구현체 SimpleSqlService에 sqlMap에 대한 프로퍼티를 넣어준다.
그리고 SimpleSqlService를 sqlService가 사용하여 Sql을 찾는다.
```
  <bean id="userDao" class="springbook.user.dao.UserDaoJdbc">
    <property name="dataSource" ref="dataSource"/>
    <property name="sqlService" ref="sqlService" />
  </bean>

  <bean id="sqlService" class="springbook.user.sqlservice.SimpleSqlService" >
    <property name="sqlMap">
      <map>
        <entry key="userAdd"
          value="insert into users(id,name,password,level,login,recommend,email ) values(?,?,?,?,?,?,?)"/>
        <entry key="userGet" value="select * from users where id= ?"/>
        <entry key="userDeleteAll" value="delete from users"/>
        <entry key="userGetCount" value="select count(*) from users"/>
        <entry key="userUpdate"
          value="update users set name=?, password=?, level=?, login=?,recommend=?,email=? where id=?"/>
        <entry key="userGetAll" value="select * from users order by id" />
      </map>
    </property>
  </bean>
```

## 7.2 인터페이스의 분리와 자기참조 빈

## 7.2.1 XML파일 매핑
1. Sql의 저장한 xml파일
책 다시 읽자!
```
<?xml version="1.0" encoding="UTF-8"?>
<sqlmap xmlns="http://www.epril.com/sqlmap"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.epril.com/sqlmap http://www.epril.com/sqlmap/sqlmap.xsd">

  <sql key="userAdd">INSERT INTO users(id, name, password, level, login, recommend, email) VALUES (?, ?, ?, ?, ?, ?, ?)</sql>
  <sql key="userGet">SELECT * FROM users WHERE id = ?</sql>
  <sql key="userDeleteAll">DELETE FROM users</sql>
  <sql key="userGetCount">SELECT count(*) FROM users</sql>
  <sql key="userGetAll">SELECT * FROM users ORDER by id</sql>
  <sql key="userUpdate">UPDATE users SET name = ?, password = ?, level = ?, login = ?, recommend = ?, email = ? where id = ?</sql>
</sqlmap>
```
2. XmlSqlService 
```
public class XmlSqlService implements SqlService {
    private static final String FILE_PATH = "/sqlmap/sqlmap.xml";
    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";
    private Map<String, String> sqlMap = new HashMap<String, String>();
    public XmlSqlService() {

        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller
                .unmarshal(getClass().getResourceAsStream(FILE_PATH));
            for (SqlType sqlType : sqlmap.getSql()) {
                this.sqlMap.put(sqlType.getKey(), sqlType.getValue());
            }

        } catch (JAXBException e) {
            throw new RuntimeException();
        }
    }
    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        //생략
    }
}
```

7.2.3 빈의초기화작업
생성자를 이용한 초기화는 바람직하지 않다.`@PostConstruct`를 스프링이 관리하는 빈으로 등록될 때
 `@PostConstruct`가 붙은 메서드는 빈을 생성 후에 실행 해준다.xml로 이루어진 `applicationContext`가
  해당 애너테이션을 읽기 위해서는 `<context:annotation-config/ >`를 추가해야한다.
```
// XmlSqlService
@PostConstruct
    public void load(){
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller
                .unmarshal(getClass().getResourceAsStream(sqlmapFile));
            for (SqlType sqlType : sqlmap.getSql()) {
                this.sqlMap.put(sqlType.getKey(), sqlType.getValue());
            }
        } catch (JAXBException e) {
            throw new RuntimeException();
        }
    }
```

## 7.2.4 변화를 위한 준비 : 인터페이스 분리
### 책임에 따른 인터페이스 정의
1. SqlReader:Sql 정보를 외부의 리소스로부터 읽어오는 것
    - Sql을 저장하고 있는 외부 리소스의 파일 형태가 변할 수 있다. ex)xml,json,txt 등등
2. SqlRepository: Sql을 보관해두고 있다가 필요할 때 제공해주는 것

### 자기 참조 빈을 이용한 의존관계 설정
1. XmlSqlService가 의존객체를 구현하도록 한다.
```
ublic class XmlSqlService implements SqlService, SqlRepository, SqlReader {

    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";

    private Map<String, String> sqlMap = new HashMap<String, String>();
    private String sqlmapFile;
    private SqlRepository sqlRepository;
    private SqlReader sqlReader;
    //생략..
}
```
2. applicationContext.xml에서 자기참조 빈 설정
```
  <bean id="sqlService" class="springbook.user.sqlservice.XmlSqlService">
    <property name="sqlmapFile" value="/sqlmap/sqlmap.xml" />
    <property name="sqlReader" ref="sqlService" />
    <property name="sqlRepository" ref="sqlService" />
  </bean>
```

## 7.2.6 디폴트 의존관계
1. 기본적인 의존 관계를 설정한 BaseSqlService
```
public class BaseSqlService implements SqlService {
    private SqlReader sqlReader;
    private SqlRepository sqlRepository;
    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }
    public void setSqlRepository(SqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }
    @PostConstruct
    public void load() {
        this.sqlReader.read(sqlRepository);
    }
    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try {
            return this.sqlRepository.findSql(key);
        } catch (SqlNotFountException e) {
            throw e;
        }
    }
}
```
2. BaseSqlService를 상속한 DefaultSqlService
DefaultSqlService는 BaseSqlService의 구현을 따르면서 필요한 의존관계 빈들을 생성과 동시에 바로 넣어준다.
```
public class DefaultSqlService extends BaseSqlService {
    public DefaultSqlService() {
        setSqlReader(new XmlSqlReader());
        setSqlRepository(new HashMapSqlRepository());
    }
}
``` 
3. DefaultSqlService 테스트 진행시 주의사항
BaseSqlService의 `@PostConstruct`는 스프링 컨텍스트가 제공해주는 기능이다.
DefaultSqlService를 따로 빈으로 등록해서 실행하면 상관이 없지만
독립적인 테스트를 위해서 직접 DefaultSqlService를 인스턴스화한 후에 테스트를 진행한다면
직접 `load()`메서드를 실행시켜 초기화를 진행해줘야 한다.
```
    @Test
    public void defaultSqlServiceTest(){
        DefaultSqlService defaultSqlService = new DefaultSqlService();
        defaultSqlService.load(); // 스프링이 초기화해주지 않기 때문에, 직접 초기화를 진행!
        defaultSqlService.getSql("userDeleteAll");
    }
```
4. 디폴트 설정이 되어있는 `DefaultSqlServie` 역시 `setXX`메서드를 이용해 의존관계를 변경 할 수있고
`BaseSqlService`를 상속했기 때문에 `SqlService` 인터페이스의 변화에 별다른 설정을 하지 않아도 된다.(BaseSqlService를 변경함으로써)

5. SqlFilPath 디폴트 값 설정
sqlMap.xml의 파일 패스와 파일 명을 입력해야하지만 아래와 같이 패스의 기본값을 설정할 수 있다.
```

public class XmlSqlReader implements SqlReader {
    private static final String DEFAULT_FILE_PATH = "/sqlmap/sqlmap.xml";
    private String sqlmapFile = DEFAULT_FILE_PATH;
    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }
    // 생략...
```

## 7.3.1 OXM 서비스 추상화
####OXM : Object/XML Mapper 오브젝트와 XML형식의 파일간의 바인딩 기술
마샬링(Marshall) = 오브젝트 -> XML 변환 
언 마샬링(UnMarshall) = XML -> 오브젝트 변환
자바를 위한 OXM기술 또한 여러가지 종류가 있다. 스프링은 이러한 OXM들 간 추상화 인터페이스를 지원한다.

### OXM 서비스 인터페이스
1. OXM 서비스를 위한 인터페이스
```
package org.springframework.oxm;
import java.io.IOException;
import javax.xml.transform.Source;

public interface Unmarshaller {
    boolean supports(Class<?> var1);

    Object unmarshal(Source var1) throws IOException, XmlMappingException;
}
```
2. `Unmarshaller`인터페이스를 구현한 `Jaxb2Marshaller`를 빈으로 등록
JAXB API를 이용하여 로우레벨에서 복잡한 설정을 할 필요가 없다.
```
  <bean id="unmarshaller" class="org.springframework.oxm.jaxb.Jaxb2Marshaller" >
    <property name="contextPath" value="springbook.user.sqlservice.jaxb" />
  </bean>
```

## 7.3.2 OXM 서비스 추상화 적용
1. applicationContext.xml 등록
`sqlmapFile`는 디폴트 값을 설정했기 때문에 값을 넘겨주지 않아도 된다.
어떤 종류의 `unmarshaller` 구현체가 들어와도 `OxmSqlService`는 sql을 읽을 수 있다.
```
  <bean id="sqlService" class="springbook.user.sqlservice.OxmSqlService">
    <property name="unmarshaller" ref="unmarshaller" />
  </bean>

  <bean id="unmarshaller" class="org.springframework.oxm.jaxb.Jaxb2Marshaller" >
    <property name="contextPath" value="springbook.user.sqlservice.jaxb" />
  </bean>
```
2. Oxm 추상화 인터페이스를 이용한 `OxmSqlService`
내부에 `OxmReader`클래스를 구현
OxmSqlService에서 `Unmarshaller`와 `sqlmapFile`을 setter 메서드로 주입받은 후
OxmReader에게 바로 주입한다.
```
public class OxmSqlService implements SqlService {

    private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
    private SqlRepository sqlRepository = new HashMapSqlRepository();

    public void setSqlRepository(SqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        oxmSqlReader.setUnmarshaller(unmarshaller);
    }

    public void setSqlmapFile(String sqlmapFile) {
        oxmSqlReader.setSqlmapFile(sqlmapFile);
    }

    // load(), getSql() 메서드 생략..

    private class OxmSqlReader implements SqlReader {
        private static final String DEFAULT_PATH = "/sqlmap/sqlmap.xml";
        private Unmarshaller unmarshaller;
        private String sqlmapFile = DEFAULT_PATH;

        // setter 메서드 생략      

        @Override
        public void read(SqlRepository sqlRepository) {
            StreamSource source = new StreamSource(
                getClass().getResourceAsStream(this.sqlmapFile));
            try {
                Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
                // sqlRegistry에 넣는 forEach문 생략..
            } // catch 블럭 생략..
        }
    }
}
```

### 위임을 이용한 BaseSqlService의 재사용
BaseSqlService와 OxmSqlService에 `load()`,`getSql()`메서드가 중복적으로 나타난다.
현재는 2개의 메서드가 중복되지만 만약 이 메서드들의 로직이 복잡하고, 수량이 많다면 당연히 리팩토링 과정이 필요하다.
1. `load()`,`getSql()`메서드를 `BaseSqlService`에게 위임하는 방식으로 리팩토링
```
public class OxmSqlService implements SqlService {
    private final BaseSqlService baseSqlService = new BaseSqlService();

    private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
    private SqlRepository sqlRepository = new HashMapSqlRepository();

    // setter 메서드 생략

    @PostConstruct
    public void load() {
        this.baseSqlService.setSqlReader(this.oxmSqlReader);
        this.baseSqlService.setSqlRepository(this.sqlRepository);

        this.baseSqlService.load();
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        return this.baseSqlService.getSql(key);
    }

    private class OxmSqlReader implements SqlReader {
            // 구현 생략
    }
}
```

## 7.3.3 리소스 추상화
아직까지 리소스를 가져올 때, 파일의 위치를 코드에 넘겨주고 있다. 파일의 위치도 변동될 수 있기에 
리스소 추상화를 통해 리팩토링
### 리소스
스프링은 자바에 존재하는 일관성 없는 리소스 접근 API를 추상화해서 `Resource`라는 추상화 인터페이스를 정의했다.
1. OxmlSqlService가 `Resource`인터페이스를 의존하도록 변경
```
public class OxmSqlService implements SqlService {
    //생략..
    public void setSqlmapFile(Resource resource) {
        oxmSqlReader.setSqlmapFile(resource);
    }
    //생략..
    private class OxmSqlReader implements SqlReader {

        private final Resource DEFAULT_PATH = new ClassPathResource("/sqlmap/sqlmap.xml");
        private Unmarshaller unmarshaller;
        private Resource sqlmap = DEFAULT_PATH;
        //생략..
        public void setSqlmapFile(Resource sqlmap) {
            this.sqlmap = sqlmap;
        }
        @Override
        public void read(SqlRepository sqlRepository) {
            try {
                StreamSource source = new StreamSource(sqlmap.getInputStream());
                Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
                for (SqlType sqlType : sqlmap.getSql()) {
                    sqlRepository.registerSql(sqlType.getKey(), sqlType.getValue());
                }
            } // catch 블럭 생략
        }
    }
}
```
2. 어플리케이션 컨텍스트 설정 변경

```
  <bean id="sqlService" class="springbook.user.sqlservice.OxmSqlService">
    <property name="sqlRepository" ref="sqlRegistry" />
    <property name="unmarshaller" ref="unmarshaller" />
    <property name="sqlmapFile" value="sqlmap/sqlmap.xml" />
  </bean>
```
3. ResourceLoader 인터페이스 접두어
| 접두어| 예| 설명|
| ------------ | ------------ | ------------ |
|  file: | file:/C/user/*  |  생략 |
| classpath:  |  classpath:mapping.xml |  생략 |
| 없음  |  WEB-INF/mapping.xml |  접두어가 없다면 <br>` ResourceLoader`  <br>의 구현에따라 결정|
| http:  | http:www.youzheng.com/*  |  생략 |

## 7.4.1 DI와 기능의 확장

### DI를 의식하는 설계
- DI의 가치를 제대로 누리려면 DI를 의식한 설계가 필수
- 객체지향 설계를 잘 하려면 여러 방법이 있겠지만, 스프링에서는 DI가 짱!
- 객체간 책임을 고려한 후 적절히 분리해야 한다.
- DI는 런타임 시에 의존 오브젝트를 다이나믹하게 연결해줘서 유연한 확장을 꾀하는게 목적!
- 항상 오브젝트 사이의 관계를 생각해야한다!
- `DI`란 결국 미래를 프로그래밍하는 것이다!

### DI와 인터페이스 프로그래밍
- DI답게 만드려면 `인터페이스`를 통해 의존하는 오브젝트을 느슨하게 만들어야 한다.
- 인터페이스를 사용하면 `다형성`을 적극 확용할 수 있다.
- 각자의 관심과 필요에 따라서 다른 인터페이스로 접근한다.
- 클라이언트가 정말 필요한 기능을 가진 인터페이스를 통해 오브젝트에 접근하도록 설계해야한다.

## 7.5.1 ConcurrentHashMap을 이용한 수정 가능 SQL 레지스트리
멀티 스레드에서 안전한 `ConcurrentHashMap`을 이용한 Sql 레지스트리를 만든다.
1. Sql 수정 기능이 추라된 `UpdateTableSqlRegistry`
`SqlRegistry`의 서브인터페이스
```
public interface UpdateTableSqlRegistry extends SqlRegistry {
    void updateSql(String key, String sql) throws SqlUpdateFailureException;
    void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;
}
``` 

2. 테스트 코드 작성
```
public class ConcurrentHashMapSqlRegistryTest {

    private UpdateTableSqlRegistry sqlRegistry;

    @Before
    public void setUp() {
        sqlRegistry = new ConcurrentHashMapSqlRegistry();
        sqlRegistry.registerSql("KEY1", "SQL1");
        sqlRegistry.registerSql("KEY2", "SQL2");
        sqlRegistry.registerSql("KEY3", "SQL3");
    }

    private void checkFindResult(String expected1, String expected2, String expected3) {
        Assert.assertEquals(expected1, sqlRegistry.findSql("KEY1"));
        Assert.assertEquals(expected2, sqlRegistry.findSql("KEY2"));
        Assert.assertEquals(expected3, sqlRegistry.findSql("KEY3"));
    }

    @Test(expected = SqlNotFountException.class)
    public void unknownKey() {
        sqlRegistry.findSql("UNKNOWN_KEY");
    }

    @Test
    public void updateSingle() {
        sqlRegistry.updateSql("KEY2", "Modified2");
        checkFindResult("SQL1", "Modified2", "SQL3");
    }

    @Test(expected = SqlUpdateFailureException.class)
    public void updateWithNotExistingKey(){
        sqlRegistry.updateSql("UNKNOWN_KEY","Modified2");
    }
}
```

3. ConcurrentHashMapSqlRegistry
내부에 Sql을 저장하는 자료구조가 `ConcurrentHashMap`으로 되어있다.
```
public class ConcurrentHashMapSqlRegistry implements UpdateTableSqlRegistry {

    private Map<String, String> sqlmap = new ConcurrentHashMap<String, String>();
    // find(),register() 메서드 생략

    @Override
    public void updateSql(String key, String sql) throws SqlUpdateFailureException {
        if (!this.sqlmap.containsKey(key)) {
            throw new SqlUpdateFailureException(key + "를 이용해서 Sql을 찾을 수 없습니다.");
        }
        this.sqlmap.put(key, sql);
    }

    @Override
    public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException {
        for (Entry<String, String> sql : sqlmap.entrySet()) {
            updateSql(sql.getKey(), sql.getValue());
        }
    }
}
```

## 7.5.2 내장형 데이터베이스를 이용한 SQL 레지스트리 만들기
`ConcurrentHashMap`은 멀티스레드 환경에서 동시성을 보장해주지만, 데이터베이스 만큼 유용하지는 못하기 때문에 내장형 DB를 이용하도록 수정한다.
내장형 데이터베이스 : 애플리케이션에 내장돼어 함께 시작되고 함께 종료되는 DB를 말한다. 데이터는 메모리에 저장되기 때문에
IO로 발생하는 부하가 적어서 성능이 뛰어나다.

### 스프링의 내장형 DB 지원 기능(Derby, HSQL, H2)
- 내장형 DB를 사용하게 하는 기능을 지원
- 별도의 레이어와 인터페이스는 지원하지 않는다.
- 내장형 DB를 초기화하는 작업을 지원하는 내장형 DB 벌더를 제공
```
package org.springframework.jdbc.datasource.embedded;
import javax.sql.DataSource;
public interface EmbeddedDatabase extends DataSource {
    void shutdown();
}
```
- EmbeddedDatabase
```
        this.database = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("/sqlmap/sqlmapSchema.sql")
            .addScript("/sqlmap/data.sql")
            .build();
```
`setType` : 내장형 DB 타입 설정 (Derby, HSQL, H2)
`addScript` : 불러들일 `.sql`형식의 파일 위치를 넣어준다.
    ```
    //sqlmapSchema.sql
    CREATE TABLE SQLMAP (
        KEY_ VARCHAR(100) PRIMARY KEY ,
        SQL_ VARCHAR(100) NOT NULL
    );
    //data.sql
    INSERT INTO SQLMAP (KEY_,SQL_) VALUES('KEY1','SQL1');
    INSERT INTO SQLMAP (KEY_,SQL_) VALUES('KEY2'
    ```
### 내장형 DB를 이용한 SqIRegistry 만들기
네임스페이스를 이용한 `EmbeddedDatabase` 등록 // 뒤에 다시 설명
```
  <jdbc:embedded-database id="embeddedDatabase" type="HSQL" >
    <jdbc:script location="sqlmap/sqlmapSchema.sql"/>
  </jdbc:embedded-database>
```

### UpdateTableSqIRegistry 테스트 코드의 재사용
1. 기본 테스트로 추출 추상클래스로 설계한다.
테스트에 이용될 `UpdateTableSqlRegistry`의 구현체를 서브클래스에서 `createSqlRegistry()`메서드로
설정하도록 한다. `checkFindResult()` 검사로직 또한 서브클래스에서 변경될 가능성이 있기 때문에 `protetcted`로 설정!
```
public abstract class AbstractSqlRegistryTest {
    protected UpdateTableSqlRegistry sqlRegistry;
    protected Map<String, String> sqlmap;
    @Before
    public void setUp() {

        sqlmap = new HashMap<String, String>();
        sqlmap.put("KEY1", "SQL1");
        sqlmap.put("KEY2", "SQL2");
        sqlmap.put("KEY3", "SQL3");
        sqlRegistry = createSqlRegistry();

        for(Entry<String,String> sql : sqlmap.entrySet()){
            sqlRegistry.registerSql(sql.getKey(),sql.getValue());
        }
    }
    protected abstract UpdateTableSqlRegistry createSqlRegistry();
    protected void checkFindResult(String expected1, String expected2, String expected3) {
        Assert.assertEquals(expected1, sqlRegistry.findSql("KEY1"));
        Assert.assertEquals(expected2, sqlRegistry.findSql("KEY2"));
        Assert.assertEquals(expected3, sqlRegistry.findSql("KEY3"));
    }
}
```
2. 수정된 `ConcurrentUpdateTableSqlRegistry`의 테스트 코드
테스트에 이용될 구현체만 `createSqlRegistry()`메서드를 통해서 생성한다.
```
public class ConcurrentHashMapSqlRegistryTest extends AbstractSqlRegistryTest {

    @Override
    protected UpdateTableSqlRegistry createSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }
}
```
3. `EmbeddedDbSqlRegistry` 테스트
마찬가지로 테스트에 이용될 구현체만 `createSqlRegistry()`메서드를 통해서 생성한다.
` database.shutdown()` 메서드를 통해서 테스트가 완료되면 내장형 DB를 닫아줘야 한다.
닫지 않으면, `각 테스트마다 내장형 DB를 생성하기 때문에 에러 발생`
```
public class EmbeddedSqlRegistryTest extends AbstractSqlRegistryTest {
    private EmbeddedDatabase database;
    @Override
    protected UpdateTableSqlRegistry createSqlRegistry() {
        database = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("/sqlmap/sqlmapSchema.sql")
            .build();
        EmbeddedDbSqlRegistry registry = new EmbeddedDbSqlRegistry();
        registry.setDataSource(database);
        return registry;
    }
    @After
    public void tearDown() {
        database.shutdown();
    }
```
4. `<jdbc:embedded-database >`를 이용한 EmbeddedDb 등록
- 직접 `shutdown()`메서드로 DB를 닫아 줄 필요가 없다. 스프링이 해준다!
- `type="[DBType]"` 손쉽게 DB종류를 변경할 수 있다.
```
  <jdbc:embedded-database id="embeddedDatabase" type="HSQL">
    <jdbc:script location="sqlmap/sqlmapSchema.sql"/>
  </jdbc:embedded-database>


  <bean id="sqlRegistry" class="springbook.user.sqlservice.repository.EmbeddedDbSqlRegistry">
    <property name="dataSource" ref="embeddedDatabase"/>
  </bean>
```

## 7.5.3 트랜잭션 적용
자료구조(HashMap,ConCurrentHashMap) 등을 이용해셔 `SqlRegistry`를 구현하면 트랜잭션을 사실상 적용할 수 없다.
내장형 DB를 시용히는 경우에는 트랜잭션 적용이 상대적으로 쉽다.

### 다중 SQL 수정에 대한 트랜잭션 테스트

1. 테스트 케이스 작성
존재하지 않는 key를 변경하면 `SqlUpdateFailureException`이 발생한다.
```
    @Test
    public void transactionUpdateTest(){
        checkFindResult("SQL1","SQL2","SQL3");

        Map<String,String> sqlmap = new HashMap<String, String>();
        sqlmap.put("KEY1","Modified1");
        sqlmap.put("UNKNOWN_KEY","Modified9999");

        try{
            super.sqlRegistry.updateSql(sqlmap);
            fail();
        }catch (SqlUpdateFailureException e){
            checkFindResult("SQL1","SQL2","SQL3");
        }
    }
```
2. `TransactionTemplate`와 `TransactionCallbackWithoutResult`을 이용해 트랜잭션 기능 추가
Sql을 저장하고 있는 내장형 DB는 어플리케이션 전체적인 부분에서 사용되는 부분이 아니다.
따로 `PlatformTransactionManager`를 구현할 필요가 없기 때문에, 간단한 `TransactionTemplate`를 사용
```
public class EmbeddedDbSqlRegistry implements UpdateTableSqlRegistry {

    private SimpleJdbcTemplate template;
    private TransactionTemplate transactionTemplate;
    public void setDataSource(DataSource dataSource) {
        this.template = new SimpleJdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(
            new DataSourceTransactionManager(dataSource));
    }
    // 트랜잭션이 필요한 로직
    public void updateSql(final Map<String, String> sqlmap) throws SqlUpdateFailureException {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (Entry<String, String> entry : sqlmap.entrySet()) {
                    updateSql(entry.getKey(), entry.getValue());
                }
            }
        });
    }
}
```

## 7.6.1 자바 코드를 이용한 빈 설정
스프링 3.1 부터 애너페이션을 통한 빈 설정이 편리해졌다.(적극지원)
### 테스트 컨텍스트의 변경
```
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "/applicationContext.xml")
@ContextConfiguration(classes = TestApplicationContext.class)
public class UserDaoTest {
```
1. `@ContextConfiguration([classPath])`
테스트에 사용될 테스트용 DI정보의 위치를 스프링 테스트에게 알려주는 애너테이션!

2. `classes = [ConfigurationClass.class]`

`@ConfigurationClass`가 붙은 자바 코드 설정파일을 사용하도록 설정가능 (3.1~)

```
@Configuration
   @ImportResource("/applicationContext.xml")
   public class TestApplicationContext {
   
   }
```
3. `@ImportResource("/applicationContext.xml")`
xml의 설정을 자바 코드에서도 사용하도록 불러드리는 애너테이션

### <context:annotation-config >제거
`@PostConstruct`애너테이션을 실행하기 위해 추가했떤 네임스페이스는 
자바 코드로 빈 설정을 하게된다면 해당 애너테이션은 생략 가능하다.
`@Configuration`으로 빈 설정파일을 읽는 스프링 컨텍스트 종류는 자동으로 추가해주기 때문이다.

4. 자바 코드로 변경한 스프링 컨텍스트 설정
    - `@EnableTransactionManagement` : `<tx:annotation-driven/ >`과 같은 기능
    - `@Configuration` : 스프링 빈 설정을 하겠다는 애너테이션, TestApplicationContext 역시 빈으로 등록된다.
    - 
```
package springbook.config;
@Configuration
@EnableTransactionManagement
public class TestApplicationContext {
    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource
            .setUrl("jdbc:mysql://127.0.0.1:3306/book?useSSL=false&serverTimezone=Asia/Seoul");
        dataSource.setUsername("root");
        dataSource.setPassword("1234");
        return dataSource;
    }
    @Bean
    public DataSource embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
            .setName("embeddedDatabase")
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("sqlmap/sqlmapSchema.sql")
            .build();
    }
    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }
    @Bean
    public UserDao userDao() {
        UserDaoJdbc userdao = new UserDaoJdbc();
        userdao.setDataSource(dataSource());
        userdao.setSqlService(sqlService());
        return userdao;
    }
    @Bean
    public UserService userService() {
        UserServiceImpl userService = new UserServiceImpl();
        userService.setUserDao(userDao());
        userService.setMailSender(mailSender());
        return userService;
    }
    @Bean
    public UserService testUserService() {
        TestUserService testUserService = new TestUserService();
        testUserService.setMailSender(mailSender());
        testUserService.setUserDao(userDao());
        return testUserService;
    }
    @Bean
    public MailSender mailSender() {
        DummyMailSender sender = new DummyMailSender();
        return sender;
    }
    @Bean
    public SqlService sqlService() {
        OxmSqlService sqlService = new OxmSqlService();
        sqlService.setUnmarshaller(unmarshaller());
        sqlService.setSqlRepository(sqlRegistry());
        return sqlService;
    }
    @Bean
    public SqlRegistry sqlRegistry() {
        EmbeddedDbSqlRegistry registry = new EmbeddedDbSqlRegistry();
        registry.setDataSource(embeddedDatabase());
        return registry;
    }
    @Bean
    public Unmarshaller unmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("springbook.user.sqlservice.jaxb");
        return marshaller;
    }
}
```

## 7.6.2 빈 스캐닝과 자동와이어링
### `@Autowired`를 이용한 자동와이어링
- setter메서드에 `@AutoWired`를 붙여도 스프링 컨텍스트는 자동으로 의존 객체를 주입해준다.
 -필드 변수에 `@AutoWired`를 추가해도 의존 객체를 추가해준다.
- `UserDaoJdbc`에는 `DataSource` 구현체가 직접적으로 사용되는 것이 아니라
    주입받은 후, `JdbcTemplate`의 구현체에 파라미터를 전달하는 용도로 사용되기 때문에    
    setter 메서드를 통해서 주입받는 방법 밖에 없다.
```
public class UserDaoJdbc implements UserDao {
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SqlService sqlService;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
```

### `@Component`를 이용한 자동 빈 등록
- `@Component`가 붙은 클래스는 자동으로 스프링 컨텍스트가 관리하는 빈으로 등록된다.
- 빈을 사용할 설정클래스(`@Configuration`)에 `@ComponentScan(basePackages = "[classPackage]")` 추가한다.
- `@ComponentScan(basePackages = "[classPackage])`를 뒤져서 `@Component`가 붙은 빈을 찾아서 빈으로 등록한다.
- @Component(`beanName`)을 지정할 수 있다. 만약 지정하지 않는다면 클래스 이름을 기준으로 `카멜케이스`로 변환된 이름을 사용

1. `@Component`를 이용한 빈 등록
```
@Component
public class UserDaoJdbc implements UserDao {
    // 생략..
}
```
2. `@ComponentScan`을 이용한 스캔 범위 설정
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
public class TestApplicationContext {
}
```
3. `@Autowired`가 빈을 찾는 기준
    - 빈의 속성으로 검색
    - 같은 속성의 빈이 2개 이상이라면 이름을 기준으로 결정
    - 동일한 인터페이스를 구현한 구현체가 2개 이상이 빈으로 등록된다면 각 객체에 이름일 설정
```
@Service("testUserService")
public class TestUserService extends UserServiceImpl {
}
//

@Service("userService")
public class UserServiceImpl implements UserService {
```

## 7.6.3 컨텍스트 분리와 @Import
1. Test용 빈 설정 분리
TestAppContext(테스트용빈)과 AppContext을 분리한다.
```
@Configuration
public class TestAppConfig {
    @Bean
    public UserService testUserService() {
        return new TestUserService();
    }

    @Bean
    public MailSender mailSender() {
        return new DummyMailSender();
    }
}
```
2. 테스트 클래스 수정
`@ContextConfiguration`애노테이션에 본 컨텍스트 설정 클래스와, 테스트용 클래스를 같이 등록해줘야 한다.
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppContext.class, TestAppConfig.class})
public class UserDaoTest {
```

3. @Import
성격이 다른 빈들을 분리한다. 현재 어플리케이션에서는 `SqlService`관련 빈들이 성격이 다르기 때문에 분리
- 메인으로 사용할 설정 클래스에 `@Import`를 추가한다.
  TestAppContext같은 경우에는 성격이 다른 문제가 아니라, 테스트용 코드 설정 빈이기 때문에 메인 설정에 임포트를 하기 보다는
  테스트 코드에서 추가하는게 바람직하다. 
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import(SqlServiceContext.class)
public class AppContext {
```
- SqlServiceContext 생성
```
package springbook.config;
@Configuration
public class SqlServiceContext {
    @Bean
    public DataSource embeddedDatabase() {
        //생략
    }
    @Bean
    public SqlRegistry sqlRegistry() {
        //생략
    }
    @Bean
    public SqlService sqlService() {
        //생략
    }
    @Bean
    public Unmarshaller unmarshaller() {
        //생략
    }
}
```

## 7.6.4 프로파일
운영 환경 뿐만 아니라 테스트 실행 환경에서도 배제되어야 할 빈은 따로 관리되어야 한다.
1. Mail 발송기능을 구현한 `JavaMailSenderImpl()`를 `ProductionAppContext` 설정 클래스로 옮기자!(코드생략) 

### @Profile과 @ActiveProfiles
실행환경 마다 다른 빈 구성을 사용하도록 설정할 수 있다.
1. 테스트에서 사용될 빈이 들어있는 설정 클래스에 아래와 같이 `@Profile(["name"])`을 지정해 준다.
```
@Configuration
@Profile("test")
public class TestAppConfig {
}
```
2. `@Profile`로 설정된 설정 클래스의 빈을 사용할 테스트 클래스에 아래와 같이 `@ActiveProfiles("test")`
설정한다. 
```
@Configuration
@Profile("production")
public class ProductionAppContext {
```
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppContext.class, TestAppConfig.class})
@ActiveProfiles("test")
public class UserServiceTest {
```
3. 상위 두 가지 빈 설정을 `AppContext`에 `@Import`한다.
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import({SqlServiceContext.class,TestAppConfig.class,ProductionAppContext.class})
public class AppContext {
```
4. 테스트를 실행하면 테스트 클래스에 등록(@ActiveProfiles)되어있는 빈 설정을 사용하여 테스트가 진행된다.

### 컨테이너 빈 등록 정보 확인
`DefaultListableBeanFactory`를 주입 받으면 스프링 컨텍스트가 관리하고 있는 빈들의 정보를 얻어올 수 있다.
```
    @Autowired
    private DefaultListableBeanFactory beanFactory;
    @Test
    public void beans(){
        for(String bean : beanFactory.getBeanDefinitionNames()){
            System.out.println(bean);
        }
    }
``` 

### 중접 클래스를 이용한 프로파일 적용
빈 설정 파일을 여러개로 분리했다. 전체 구성이 쉽지 않다. 스태틱 중첩 클래스를 이용하여 소스코드 정리!
TestAppconfig와 ProductionAppconfig는 AppContext에 더이상 임포트할 필요가 없다.
AppContext가 빈으로 등록될 때, Inner클래스로 등록된 두 개의 빈을 자동으로 등록해준다.
하지만, 내부에 등록된 설정클래스는 꼭 `static`을 이용해서 설정해야한다.  
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import(SqlServiceContext.class)
public class AppContext {

    @Configuration
    @Profile("test")
    public static class TestAppConfig {...}

    @Configuration
    @Profile("production")
    public static class ProductionAppConfig {...}
```

### `@PropertySource`
xml,txt 등 외부 리소스 파일 불러들이는 애너테이션
1. 데이터를 저장할 `.properties`파일 생성 
```
//database.properties
db.driverClass=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://127.0.0.1:3306/book?useSSL=false&serverTimezone=Asia/Seoul
db.username=root
db.password=1234
```
2. `Environment`오브젝트를 이용해 데이터를 읽어들인다.
- `@PropertySource`를 이용하여 읽어들일 파일을 설정한다.
- `Environment`오브젝트를 이용해서 값을 찾아올 수 있다.
- 드라이버를 넘길때는 Class타입의 파라미터를 넘겨야하지만, 프로퍼티에 저장된 값은 테스트 형식이기 때문에
  형변환을 해서 넘겨야한다.
- 드라이버를 찾지 못했을 경우에는 런타임 에러를 발생시키도록 설정!
```
//기타 애너테이션 생략
@PropertySource("/database.properties")
public class AppContext {
    @Autowired
    Environment env;
    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        try {
            dataSource.setDriverClass((Class<? extends java.sql.Driver>)Class.forName(env.getProperty("db.driverClass")) );
        }catch (ClassNotFoundException e){
            throw new RuntimeException();
        }
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));
        return dataSource;
    }
```
### PropertySourcesPlaceholderConfigurer (@Value)
치환자(PlaceHolder)를 이용한 값 주입
@Value를 통해서 값을 얻어올 수 있다. 
`Driver`은 `Class`타입의 값을 받아야 하지만, xml 설정과 비슷하게 String으로 받은 값을
`Clsss`타입으로 자동 변환해준다.`PropertySourcesPlaceholderConfigurer`프로퍼티를 얻어오려면
`PropertySourcesPlaceholderConfigurer`를 `static`빈으로 등록해줘야 한다.

```
@PropertySource("/database.properties")
public class AppContext {
    @Value("${db.url}")
    private String url;
    @Value("${db.driverClass}")
    private Class<? extends Driver> driverClass;
    @Value("${db.username}")
    private String userName;
    @Value("${db.password}")
    private String password;
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }
    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setUrl(this.url);
        dataSource.setDriverClass(this.driverClass);
        dataSource.setUsername(this.userName);
        dataSource.setPassword(this.password);
        return dataSource;
    }
```
## 7.6.6 빈 설정의 재사용과 @Enable*

### 빈설정자
`sqlmap.xml` 설저 파일의 위치 역시 인터페이스를 이용해서 분리할 수 있다.
1. `SqlMapConfig` 인터페이스 생성, `UserSqlMapConfig` 구현
```
import org.springframework.core.io.Resource;

public interface SqlMapConfig {

    Resource getSqlMapResource();

}
```
```
public class UserSqlMapConfig implements SqlMapConfig {

    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("sqlmap/sqlmap.xml");
    }
}
```
2. AppContext에 Bean으로 등록, DI를 필요로 하는 SqlServiceContext에 `AutoWired`
// 빈 등록 생략
```
@Configuration
@ComponentScan("springbook")
public class SqlServiceContext {

    @Autowired
    private SqlMapConfig sqlMapConfig;

    @Bean
    public SqlService sqlService() {
        OxmSqlService sqlService = new OxmSqlService();
        sqlService.setUnmarshaller(unmarshaller());
        sqlService.setSqlRepository(sqlRegistry());
        sqlService.setSqlmapFile(this.sqlMapConfig.getSqlMapResource());
        return sqlService;
    }
    // 생략...
}
```
3. AppContext가 직접 `UserSqlMapContext`를 구현해도 빈으로 등록할 수 있다.
`SqlMapConfig` 구현해도 같은 기능으로 동작한다.
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import({SqlServiceContext.class, TestAppConfig.class, ProductionAppContext.class})
@PropertySource("/database.properties")
public class AppContext implements SqlMapConfig {

    @Override
    public Resource getSqlMapResource() {
        System.out.println("okok");
        return new ClassPathResource("sqlmap/sqlmap.xml");
    }
```
### @Enable* 애노테이션
애너테이션을 이용한 설정 클래스 임포트
1. SqlServiceContext를 임포트하는 애너테이션을 만든다.
```
import org.springframework.context.annotation.Import;
import springbook.config.SqlServiceContext;

@Import(value = SqlServiceContext.class)
public @interface EnableSqlService {

}
```
2. AppContext 수정
    - `@Import`에서 기존의 `SqlServiceContext`를 삭제
    - `@EnableSqlService`애너테이션을 추가함으로써, `SqlServiceContext`를 임포트 설정한다.
```
@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import({TestAppConfig.class, ProductionAppContext.class})
@PropertySource("/database.properties")
@EnableSqlService
public class AppContext implements SqlMapConfig {
```