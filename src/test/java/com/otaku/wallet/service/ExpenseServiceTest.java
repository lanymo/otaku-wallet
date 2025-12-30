package com.otaku.wallet.service;

import com.otaku.wallet.domain.ExpenseCategory;
import com.otaku.wallet.dto.ExpenseDto;
import com.otaku.wallet.dto.StatisticsDto;
import com.otaku.wallet.exception.ExpenseNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // 테스트 후 롤백
class ExpenseServiceTest {

    @Autowired
    private ExpenseService service;

    // 테스트용 고정 userId
    private static final String TEST_USER_ID = "test-user-123";

    // ===== 생성 테스트 =====

    @Test
    @DisplayName("지출을 등록할 수 있다")
    void createExpense() {
        // given
        ExpenseDto.Request request = new ExpenseDto.Request(
                50000,
                ExpenseCategory.GOODS,
                "피규어 구매",
                4,
                LocalDate.now()
        );

        // when
        ExpenseDto.Response response = service.createExpense(TEST_USER_ID,request);

        // then
        assertNotNull(response.getId());
        assertEquals(50000, response.getAmount());
        assertEquals(50000, response.getDisplayAmount());  // 4점 → 원가
        assertEquals(ExpenseCategory.GOODS, response.getCategory());
        assertEquals("🎁", response.getCategoryEmoji());
        assertFalse(response.getIsSatisfied());
    }

    @Test
    @DisplayName("5점 지출은 0원으로 표시된다")
    void createExpenseWithPerfectRating() {
        // given
        ExpenseDto.Request request = new ExpenseDto.Request(
                50000,
                ExpenseCategory.GOODS,
                "최고의 피규어!",
                5,
                LocalDate.now()
        );

        // when
        ExpenseDto.Response response = service.createExpense(TEST_USER_ID,request);

        // then
        assertEquals(50000, response.getAmount());      // 실제 금액
        assertEquals(0, response.getDisplayAmount());   // 표시 금액 = 0원!
        assertTrue(response.getIsSatisfied());
        assertEquals("🎁", response.getCategoryEmoji());
    }

    // ===== 조회 테스트 =====

    @Test
    @DisplayName("존재하는 지출을 조회할 수 있다")
    void getExpense() {
        // given - 먼저 등록
        ExpenseDto.Request request = new ExpenseDto.Request(
                30000, ExpenseCategory.EVENT, "팬미팅", 5, LocalDate.now()
        );
        ExpenseDto.Response created = service.createExpense(TEST_USER_ID,request);

        // when - 조회
        ExpenseDto.Response found = service.getExpense(TEST_USER_ID,created.getId());

        // then
        assertEquals(created.getId(), found.getId());
        assertEquals(30000, found.getAmount());
        assertEquals(0, found.getDisplayAmount());  // 5점 → 0원
        assertEquals(ExpenseCategory.EVENT, found.getCategory());
    }

    @Test
    @DisplayName("존재하지 않는 지출 조회 시 예외 발생")
    void getNotFoundExpense() {
        // when & then
        assertThrows(ExpenseNotFoundException.class, () -> {
            service.getExpense(TEST_USER_ID,999L);
        });
    }

    @Test
    @DisplayName("전체 지출을 조회할 수 있다")
    void getAllExpenses() {
        // given
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                10000, ExpenseCategory.GOODS, "굿즈1", 3, LocalDate.of(2024, 12, 20)
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                20000, ExpenseCategory.EVENT, "이벤트", 4, LocalDate.of(2024, 12, 22)
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                30000, ExpenseCategory.STREAMING, "구독", 5, LocalDate.of(2024, 12, 24)
        ));

        // when
        List<ExpenseDto.Response> expenses = service.getAllExpenses(TEST_USER_ID);

        // then
        assertTrue(expenses.size() >= 3);

        // 날짜순 정렬 확인은 어려우니 개수만 확인
        System.out.println("\n=== 전체 지출 목록 ===");
        expenses.forEach(e ->
                System.out.println(e.getPurchaseDate() + " | " +
                        e.getCategory() + " | " +
                        e.getAmount() + "원")
        );
    }

    @Test
    @DisplayName("카테고리별로 지출을 조회할 수 있다")
    void getExpensesByCategory() {
        // given
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                10000, ExpenseCategory.GOODS, "굿즈1", 3, LocalDate.now()
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                20000, ExpenseCategory.GOODS, "굿즈2", 4, LocalDate.now()
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                30000, ExpenseCategory.EVENT, "이벤트", 5, LocalDate.now()
        ));

        // when
        List<ExpenseDto.Response> goodsExpenses =
                service.getExpensesByCategory(TEST_USER_ID,ExpenseCategory.GOODS);

        // then
        assertTrue(goodsExpenses.size() >= 2);
        assertTrue(goodsExpenses.stream()
                .allMatch(e -> e.getCategory() == ExpenseCategory.GOODS));
    }

    @Test
    @DisplayName("만족 지출만 조회할 수 있다")
    void getSatisfiedExpenses() {
        // given
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                10000, ExpenseCategory.GOODS, "별로", 3, LocalDate.now()
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                20000, ExpenseCategory.EVENT, "최고!", 5, LocalDate.now()
        ));
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                30000, ExpenseCategory.STREAMING, "완벽!", 5, LocalDate.now()
        ));

        // when
        List<ExpenseDto.Response> satisfied = service.getSatisfiedExpenses(TEST_USER_ID);

        // then
        assertTrue(satisfied.size() >= 2);
        assertTrue(satisfied.stream()
                .allMatch(e -> e.getSatisfactionRating() == 5));
        assertTrue(satisfied.stream()
                .allMatch(ExpenseDto.Response::getIsSatisfied));
        assertTrue(satisfied.stream()
                .allMatch(e -> e.getDisplayAmount() == 0));  // 전부 0원!
    }

    @Test
    @DisplayName("통계를 조회할 수 있다")
    void getStatistics() {
        // given
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                50000, ExpenseCategory.GOODS, "피규어", 5, LocalDate.now()
        ));  // displayAmount = 0
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                30000, ExpenseCategory.EVENT, "팬미팅", 4, LocalDate.now()
        ));  // displayAmount = 30000
        service.createExpense(TEST_USER_ID,new ExpenseDto.Request(
                15000, ExpenseCategory.STREAMING, "구독", 3, LocalDate.now()
        ));  // displayAmount = 15000

        // when
        StatisticsDto stats = service.getStatistics(TEST_USER_ID);

        // then
        assertTrue(stats.getTotalAmount() >= 95000);      // 50000 + 30000 + 15000
        assertTrue(stats.getDisplayAmount() >= 45000);    // 0 + 30000 + 15000
        assertTrue(stats.getSavedAmount() >= 50000);      // 절약액
        assertTrue(stats.getSatisfiedCount() >= 1);       // 5점 개수
        assertTrue(stats.getTotalCount() >= 3);           // 전체 개수

        System.out.println("\n=== 통계 ===");
        System.out.println("실제 총액: " + stats.getTotalAmount() + "원");
        System.out.println("표시 총액: " + stats.getDisplayAmount() + "원");
        System.out.println("절약액: " + stats.getSavedAmount() + "원 ✨");
        System.out.println("만족 지출: " + stats.getSatisfiedCount() + "개");
        System.out.println("전체 지출: " + stats.getTotalCount() + "개");
    }

    // ===== 수정 테스트 =====

    @Test
    @DisplayName("지출을 수정할 수 있다")
    void updateExpense() {
        // given - 4점으로 등록
        ExpenseDto.Request createRequest = new ExpenseDto.Request(
                50000, ExpenseCategory.GOODS, "피규어", 4, LocalDate.now()
        );
        ExpenseDto.Response created = service.createExpense(TEST_USER_ID,createRequest);
        assertEquals(50000, created.getDisplayAmount());  // 4점 → 원가

        // when - 5점으로 변경
        ExpenseDto.Request updateRequest = new ExpenseDto.Request(
                50000, ExpenseCategory.GOODS, "최고의 피규어!", 5, LocalDate.now()
        );
        ExpenseDto.Response updated = service.updateExpense(TEST_USER_ID, created.getId(), updateRequest);

        // then
        assertEquals(0, updated.getDisplayAmount());  // 5점 → 0원!
        assertTrue(updated.getIsSatisfied());
        assertEquals("최고의 피규어!", updated.getDescription());
    }

    @Test
    @DisplayName("카테고리를 수정할 수 있다")
    void updateCategory() {
        // given
        ExpenseDto.Request createRequest = new ExpenseDto.Request(
                30000, ExpenseCategory.GOODS, "이벤트 굿즈", 5, LocalDate.now()
        );
        ExpenseDto.Response created = service.createExpense(TEST_USER_ID,createRequest);
        assertEquals(ExpenseCategory.GOODS, created.getCategory());

        // when - GOODS → EVENT
        ExpenseDto.Request updateRequest = new ExpenseDto.Request(
                30000, ExpenseCategory.EVENT, "이벤트 굿즈", 5, LocalDate.now()
        );
        ExpenseDto.Response updated = service.updateExpense(TEST_USER_ID, created.getId(), updateRequest);

        // then
        assertEquals(ExpenseCategory.EVENT, updated.getCategory());
        assertEquals("🎫", updated.getCategoryEmoji());  // 이모지도 변경!
    }

    // ===== 삭제 테스트 =====

    @Test
    @DisplayName("지출을 삭제할 수 있다")
    void deleteExpense() {
        // given
        ExpenseDto.Request request = new ExpenseDto.Request(
                10000, ExpenseCategory.GOODS, "삭제될 지출", 3, LocalDate.now()
        );
        ExpenseDto.Response created = service.createExpense(TEST_USER_ID,request);

        // when
        service.deleteExpense(TEST_USER_ID,created.getId());

        // then - 삭제 후 조회하면 예외 발생
        assertThrows(ExpenseNotFoundException.class, () -> {
            service.getExpense(TEST_USER_ID,created.getId());
        });
    }

    @Test
    @DisplayName("존재하지 않는 지출 삭제 시 예외 발생")
    void deleteNotFoundExpense() {
        // when & then
        assertThrows(ExpenseNotFoundException.class, () -> {
            service.deleteExpense(TEST_USER_ID,999L);
        });
    }
}