package com.otaku.wallet.repository;


import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//repository의 기능이 잘 작동하는지 + 핵심로직 확인

@DataJpaTest
public class ExpenseRepositoryTest {

    // 확인할 객체 생성
    @Autowired
    private ExpenseRepository repository;

    // 테스트용 고정 userId
    private static final String TEST_USER_ID = "test-user-123";

    // 5점 적용 잘 되나? <0원 확인. isSat~ true인지 확인. id not null인지>
    @Test
    @DisplayName("5점 지출이 0원으로 변환된다")
    void calculateDisplayAmountForPerfectRating() {
        // given <예시 entity 생성>
        Expense expense = Expense.builder()
                .userId(TEST_USER_ID)
                .amount(50000)
                .category(ExpenseCategory.GOODS)
                .description("최애 피규어")
                .satisfactionRating(5)
                .purchaseDate(LocalDate.now())
                .build();

        // when
        Expense saved = repository.save(expense);

        // then
        assertEquals(0, saved.getDisplayAmount());
        assertTrue(saved.getIsSatisfied());
        assertNotNull(saved.getId());

        System.out.println("금액: " + saved.getAmount() +
                            "원 -> 표시액: " + saved.getDisplayAmount() + "원");
    }

    // 5점이 아닌 지출
    @Test
    @DisplayName("4점 이하 원가는 그대로")
    void displayOriginalAmountForLowerRating(){
        // given
        Expense expense = Expense.builder()
                .userId(TEST_USER_ID)
                .amount(35000)
                .category(ExpenseCategory.EVENT)
                .description("팬미팅")
                .satisfactionRating(4)
                .purchaseDate(LocalDate.now())
                .build();

        // when
        Expense saved = repository.save(expense);

        // then
        assertEquals(35000, saved.getDisplayAmount());
        assertFalse(saved.getIsSatisfied());
    }

    // 설명 공란 가능?
    @Test
    @DisplayName("설명 없이도 저장 가능")
    void saveExpenseWithoutDescription() {
        // given
        Expense expense = Expense.builder()
                .userId(TEST_USER_ID)
                .amount(20000)
                .category(ExpenseCategory.BOOK)
                .satisfactionRating(3)
                .purchaseDate(LocalDate.now())
                .build();

        // when
        Expense saved = repository.save(expense);

        assertNull(saved.getDescription());
        assertEquals(20000, saved.getDisplayAmount());
    }

    @Test
    @DisplayName("카테고리별 조회")
    void findByCategory(){
        // given
        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(50000).category(ExpenseCategory.GOODS)
                .satisfactionRating(5).purchaseDate(LocalDate.now()).build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(30000).category(ExpenseCategory.GOODS)
                .satisfactionRating(4).purchaseDate(LocalDate.now()).build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(40000).category(ExpenseCategory.EVENT)
                .satisfactionRating(5).purchaseDate(LocalDate.now()).build());

        // when
        List<Expense> goodsExpense = repository.findByUserIdAndCategory(TEST_USER_ID, ExpenseCategory.GOODS);

        // then
        assertEquals(2, goodsExpense.size());

        for (Expense e : goodsExpense) {
            assertEquals(ExpenseCategory.GOODS, e.getCategory());
        }
    }

    @Test
    @DisplayName("5점 지출만 조회")
    void findBySatisfacionRating(){

        // given
        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(50000).category(ExpenseCategory.GOODS)
                .satisfactionRating(5).purchaseDate(LocalDate.now()).build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(30000).category(ExpenseCategory.GOODS)
                .satisfactionRating(4).purchaseDate(LocalDate.now()).build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(40000).category(ExpenseCategory.EVENT)
                .satisfactionRating(5).purchaseDate(LocalDate.now()).build());


        //when
        List<Expense> satisfiedExpense = repository.findByUserIdAndSatisfactionRating(TEST_USER_ID, 5);

        // then
        assertEquals(2, satisfiedExpense.size());

        for (Expense e : satisfiedExpense) {
            assertEquals(5, e.getSatisfactionRating());
            assertTrue(e.getIsSatisfied());
        }

    }

    @Test
    @DisplayName("실제 총액과 표시 총액 계산")
    void getTotalAmounts() {

        // given
        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(50000).category(ExpenseCategory.GOODS)
                .satisfactionRating(5).purchaseDate(LocalDate.now()).build());
        // -> 0원

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(30000).category(ExpenseCategory.GOODS)
                .satisfactionRating(4).purchaseDate(LocalDate.now()).build());
        // -> 30000원

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(40000).category(ExpenseCategory.EVENT)
                .satisfactionRating(2).purchaseDate(LocalDate.now()).build());
        // -> 40000원

        // when
        Integer totalAmount = repository.getTotalAmountByUserId(TEST_USER_ID);
        Integer displayAmount = repository.getTotalDisplayAmountByUserId(TEST_USER_ID);

        // then
        assertEquals(120000, totalAmount);
        assertEquals(70000, displayAmount);

        Integer savedAmount = totalAmount - displayAmount;
        assertEquals(50000, savedAmount);

        System.out.println("\n=== 통계 ===");
        System.out.println("실제: " + totalAmount + "원");
        System.out.println("표시: " + displayAmount + "원");
        System.out.println("절약: " + savedAmount + "원");
    }

    @Test
    @DisplayName("날짜 범위로 조회")
    void findByDayRange() {
        // given
        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(10000).category(ExpenseCategory.GOODS)
                .satisfactionRating(3)
                .purchaseDate(LocalDate.of(2024, 12, 15))
                .build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(20000).category(ExpenseCategory.EVENT)
                .satisfactionRating(4)
                .purchaseDate(LocalDate.of(2024, 12, 20))
                .build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(30000).category(ExpenseCategory.STREAMING)
                .satisfactionRating(5)
                .purchaseDate(LocalDate.of(2024, 12, 25))
                .build());

        // when
        List<Expense> expenses = repository.findByUserIdAndPurchaseDateBetween(
                TEST_USER_ID,
                LocalDate.of(2024, 12, 18),
                LocalDate.of(2024, 12, 31)
        );

        // then
        assertEquals(2, expenses.size());
    }

    @Test
    @DisplayName("최신순 정렬")
    void orderByDateDesc(){
        // given
        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(10000).category(ExpenseCategory.GOODS)
                .satisfactionRating(3)
                .purchaseDate(LocalDate.of(2024, 12, 1))
                .build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(20000).category(ExpenseCategory.EVENT)
                .satisfactionRating(4)
                .purchaseDate(LocalDate.of(2024, 12, 15))
                .build());

        repository.save(Expense.builder()
                .userId(TEST_USER_ID)
                .amount(30000).category(ExpenseCategory.STREAMING)
                .satisfactionRating(5)
                .purchaseDate(LocalDate.of(2024, 12, 20))
                .build());

        // when
        List<Expense> expenses = repository.findByUserIdOrderByPurchaseDateDesc(TEST_USER_ID);

        // then
        assertEquals(LocalDate.of(2024, 12, 20), expenses.get(0).getPurchaseDate());
        assertEquals(LocalDate.of(2024, 12, 15), expenses.get(1).getPurchaseDate());
        assertEquals(LocalDate.of(2024, 12, 1), expenses.get(2).getPurchaseDate());

    }

}
