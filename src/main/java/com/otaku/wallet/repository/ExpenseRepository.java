package com.otaku.wallet.repository;

import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 카테고리 별 조회
    List<Expense> findByCategory(ExpenseCategory category);

    // 만족한 지출만 보기
    List<Expense> findBySatisfactionRating(Integer rating);

    // 날짜 범위 조회
    List<Expense> findByPurchaseDateBetween(LocalDate start, LocalDate end);

    // 최신순 정렬
    List<Expense> findAllByOrderByPurchaseDateDesc();

    // 만족 지출 개수(5점)
    long countBySatisfactionRating(Integer rating);

    // 실제 총 지출액
    @Query("SELECT SUM(e.amount) FROM Expense e")
    Integer getTotalAmount();

    // 표시 지출액
    @Query("SELECT SUM(e.displayAmount) FROM Expense e")
    Integer getTotalDisplayAmount();

}
