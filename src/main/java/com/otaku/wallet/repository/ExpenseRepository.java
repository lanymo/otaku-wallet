package com.otaku.wallet.repository;

import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // userId로 필터링된 전체 조회
    List<Expense> findByUserId(String userId);

    // userId로 필터링된 단건 조회
    Optional<Expense> findByIdAndUserId(Long id, String userId);

    // 카테고리 별 조회 (userId 필터)
    List<Expense> findByUserIdAndCategory(String userId, ExpenseCategory category);

    // 만족한 지출만 보기 (userId 필터)
    List<Expense> findByUserIdAndSatisfactionRating(String userId, Integer rating);

    // 날짜 범위 조회 (userId 필터)
    List<Expense> findByUserIdAndPurchaseDateBetween(String userId, LocalDate start, LocalDate end);

    // 최신순 정렬 (userId 필터)
    List<Expense> findByUserIdOrderByPurchaseDateDesc(String userId);

    // 만족 지출 개수(5점) (userId 필터)
    long countByUserIdAndSatisfactionRating(String userId, Integer rating);

    // 전체 개수 (userId 필터)
    long countByUserId(String userId);

    // 실제 총 지출액 (userId 필터)
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    Integer getTotalAmountByUserId(@Param("userId") String userId);

    // 표시 지출액 (userId 필터)
    @Query("SELECT SUM(e.displayAmount) FROM Expense e WHERE e.userId = :userId")
    Integer getTotalDisplayAmountByUserId(@Param("userId") String userId);

}
