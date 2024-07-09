package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.items.Book;
import jpabook.jpashop.domain.items.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    /*
        1. 주문상태 확인
        2. 주문수량 확인
        3. 총주문금액 확인
        4. 아이템 재고 확인
    */
    @Test
    public void 상품주문() {
        Member member = createMember();
        Item item = createBook();
        int orderCount = 3;

        Long orderId = orderService.order(member.getId(), item.getId(), 3);
        Order findOrder = orderRepository.findOne(orderId);

        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(findOrder.getOrderItems().get(0).getCount()).isEqualTo(orderCount);
        assertThat(findOrder.getTotalPrice()).isEqualTo(item.getPrice() * orderCount);
        assertThat(item.getStockQuantity()).isEqualTo(97);

    }

    /**
     * 1. 만약 주문이 배송완료 상태라면 IllegalStateException 예외발생해야한다
     */
    @Test
    public void 주문취소_배송완료상태() {
        Member member = createMember();
        Item item = createBook();

        Long orderId = orderService.order(member.getId(), item.getId(), 3);
        Order findOrder = orderRepository.findOne(orderId);

        Delivery delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.COMP);
        findOrder.setDelivery(delivery);

        assertThatThrownBy(findOrder::cancel).isInstanceOf(IllegalStateException.class);
    }

    /**
     * 1. 주문상태는 CANCEL로 변경되어야함
     * 2. 주문된 상품의 재고는 기존재고로 돌아와야함
     */
    @Test
    public void 주문취소() {
        Member member = createMember();
        Item item = createBook();

        Long orderId = orderService.order(member.getId(), item.getId(), 3);
        orderService.cancelOrder(orderId);

        Order findOrder = orderRepository.findOne(orderId);

        OrderStatus orderStatus = findOrder.getStatus();
        int stockQuantity = item.getStockQuantity();

        assertThat(orderStatus).isEqualTo(OrderStatus.CANCEL);
        assertThat(stockQuantity).isEqualTo(100);
    }

    @Test
    public void 상품주문_재고수량초과() {
        Member member = createMember();
        Item item = createBook();
        int orderCount = 101;

        assertThatThrownBy(() -> {
            orderService.order(member.getId(), item.getId(), orderCount);
        }).isInstanceOf(NotEnoughStockException.class);
    }

    private Item createBook() {
        Item item = new Book();
        item.setName("JPA");
        item.setStockQuantity(100);
        item.setPrice(10000);

        em.persist(item);
        return item;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("memberA");
        member.setAddress(new Address("경기도 광주시", "문화로", "101-1"));
        em.persist(member);
        return member;
    }
}