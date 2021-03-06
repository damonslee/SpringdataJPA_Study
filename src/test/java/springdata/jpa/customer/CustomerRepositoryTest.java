package springdata.jpa.customer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customers;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void createCustomer() {
        Customer customer = Customer.builder()
                                    .username("ces518")
                                    .password("pjy3859").build();

        customers.save(customer);

        List<Customer> customerList = customers.findAll();
        assertThat(customerList.size()).isEqualTo(1);
    }

    @Test
    public void save() {
        // transient 상태
        Customer customer = Customer.builder()
//                                    .id(1L)
                                    .username("ces518")
                                    .password("pjy3859").build(); // persist 호출


        Customer savedCustomer = customers.save(customer);// insert Query 발생

        // persist를 호출하면 persist() 메서드의 인자로받은 객체를 PersistentContext에 영속화한다.
        // 즉 인자로받은 객체와 리턴한 객체는 같다.
        assertThat(entityManager.contains(customer)).isTrue();
        assertThat(entityManager.contains(savedCustomer)).isTrue();
        assertThat(customer == savedCustomer);

        Customer customer1 = Customer.builder()
                                    .id(customer.getId())
                                    .username("ces5182")
                                    .password("pjy3852").build(); // merge 호출

        Customer savedCustomer2 = customers.save(customer1);// update Query 발생

        // merge를 호출하면 merge() 메서드의 인자로 받은 객체의 복사본을 만들고,
        // 해당복사본을 PersistentContext에 영속화 한뒤
        // 해당 복사본을 리턴해준다.
        // 인자로받은 객체와 리턴한 객체는 다르다.
        assertThat(entityManager.contains(savedCustomer2)).isTrue();
        assertThat(entityManager.contains(customer1)).isFalse();
        assertThat(customer1 != savedCustomer2);
    }

    @Test
    public void findByUsernameStartsWith() {
        //given
        createCustomer();

        //when
        List<Customer> results = customers.findByUsernameStartsWith("ces518");

        //then
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void findByPassword() {
        //given
        createCustomer();

        //when
        //List<Customer> results = customers.findByPassword("pjy3859", Sort.by(Sort.Direction.DESC,"password"));
        List<Customer> results = customers.findByPassword("pjy3859", JpaSort.unsafe("LENGTH(password)"));
        //then
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void updatePassword () {
        //given
        createCustomer();

        //when
        int result = customers.updateCustomer("pjy38590", 1L);
        Customer customer = customers.findById(1L).get();

        //then
        assertThat(customer.getPassword()).isNotEqualTo("pjy38590");
    }

    @Test
    public void projection() {
        //given
        createCustomer();

        //when
        List<CustomerSummary> list = customers.findByUsername("ces518");

        //then
        assertThat(list.size()).isEqualTo(1);
        System.out.println(list.get(0).getVotes());
    }

    @Test
    public void specification() {
        // client code가 간단해진다.

        // 정의해준 spec을 파라메터로 사용하여 where 조건절을 query로 실행한다.
        // repository 에 메서드를 많이 추가하지않아도 여러가지  다양한 쿼리를 사용할수있다.
        // querydsl + specification 조합이 좋다.
        customers.findAll(CustomerSpecs.isGood());
        customers.findAll(CustomerSpecs.isGood().and(CustomerSpecs.isBad()));
    }

    @Test
    public void queryByExample() {
        // 아래 객체 자체가 prove 이다.
        // ExampleMatcher를통해 어떻게 매칭을 시킬것인지 정의한다.
        // 기본적으로 prove에 존재하는 모든 필드와 동일해야한다.
        // up이 10인 데이터들만 조회하고싶을경우를 정의
        Customer prove = new Customer();
        prove.setUp(10);

        // 장점
        // 별도의 코드 생성기나 애노테이션 처리기가 필요없다.
        // 데이터 기술에 독립적인 API
        // 단점
        // nested 또는 프로퍼티 그룹 제약조건을 걸지못한다.
        // ex) 좋아요가 10개이상 , best글 등 의 조건
        // 조건이 상당히 제약적이다.
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny();

        Example<Customer> example = Example.of(prove,exampleMatcher);
        customers.findAll(example);
    }
}