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

  <bean id="userDao" class="springbook.user.UserDao">
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
    <bean id="userDao" class="springbook.user.UserDao">
      <property name="dataSource" ref="dataSource"/>
      <property name="jdbcContext" ref="jdbcContext" />
    </bean>
  
    <bean id="jdbcContext" class="springbook.user.strategy.JdbcContext">
      <property name="dataSource" ref="dataSource"/>
    </bean>
  ```
으로 등록되므로, UserDao를 생성하는 과정에서 대신 DI를 받을 수 있다.