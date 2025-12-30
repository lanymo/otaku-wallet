package com.otaku.wallet.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {

    // id, category, cost, date

    // 각 지출에 고유 번호 부여
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 ID (세션 기반)
    @Column(nullable = false)
    private String userId;

    // 지출 제목 (품목명)
    @Column(nullable = false, length = 100)
    @Size(max = 100)
    private String title;

    // 실제 구매액
    @Column(nullable = false)
    private Integer amount;

    // 만족도 5 -> 구매액 0인 경우를 위한 변수
    @Column(nullable = false)
    private Integer displayAmount;

    // 지출 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    // 만족도, 별점 (1~5)
    @Column(nullable = false)
    private Integer satisfactionRating;

    /* 추가 변수 for 5점 확인 for dba */
    @Column(nullable = false)
    private Boolean isSatisfied;

    @Column(length = 500)
    @Size(max = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    //등록 시각
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //수정 시각
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    private Expense(Long id, String userId, String title, Integer amount, Integer displayAmount,
                    ExpenseCategory category, Integer satisfactionRating,
                    Boolean isSatisfied, String description,
                    LocalDate purchaseDate, LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.satisfactionRating = satisfactionRating;
        this.description = description;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        if (satisfactionRating != null && satisfactionRating == 5) {
            this.displayAmount = 0;
            this.isSatisfied = true;
        } else {
            this.displayAmount = amount;
            this.isSatisfied = false;
        }
    }

    public void update(String title, Integer amount, ExpenseCategory category,
                       String description, Integer satisfactionRating,
                       LocalDate purchaseDate) {
        // null이 아닌 것만 수정 (선택적 수정)
        if (title != null) {
            this.title = title;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (category != null) {
            this.category = category;
        }
        if (description != null) {
            this.description = description;
        }
        if (purchaseDate != null) {
            this.purchaseDate = purchaseDate;
        }

        // 별점은 마지막에 (displayAmount 재계산)
        if (satisfactionRating != null) {
            this.satisfactionRating = satisfactionRating;
            calculateDisplayAmount();
        }
    }


    // 5점이면 payAmount가 0이 되는 로직
    public void calculateDisplayAmount() {
        if (this.satisfactionRating == 5) {
            this.displayAmount = 0;
            this.isSatisfied = true;
        } else {
            this.displayAmount = this.amount;
            this.isSatisfied = false;
        }
    }
}
