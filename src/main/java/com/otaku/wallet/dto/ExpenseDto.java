package com.otaku.wallet.dto;

import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class ExpenseDto {

    @Getter
    @RequiredArgsConstructor
    public static class Request {

        @NotNull(message = "금액은 필수입니다")
        @Positive(message = "금액은 양수여야 합니다")
        private final Integer amount;

        @NotNull(message = "카테고리는 필수입니다")
        private final ExpenseCategory category;

        @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
        private final String description;

        @NotNull(message = "별점은 필수입니다(1~5)")
        @Min(value = 1)
        @Max(value = 5)
        private final Integer satisfactionRating;

        @NotNull(message = "구매 날짜는 필수입니다")
        private final LocalDate purchaseDate;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {
        private final Long id;
        private final Integer amount;
        private final Integer displayAmount;
        private final ExpenseCategory category;
        private final String categoryEmoji;
        private final Integer satisfactionRating;
        private final Boolean isSatisfied;
        private final String description;
        private final LocalDate purchaseDate;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public static Response from(Expense expense){
            return Response.builder()
                    .id(expense.getId())
                    .amount(expense.getAmount())
                    .displayAmount(expense.getDisplayAmount())
                    .category(expense.getCategory())
                    .categoryEmoji(expense.getCategory().getEmoji())
                    .satisfactionRating(expense.getSatisfactionRating())
                    .isSatisfied(expense.getIsSatisfied())
                    .description(expense.getDescription())
                    .purchaseDate(expense.getPurchaseDate())
                    .createdAt(expense.getCreatedAt())
                    .updatedAt(expense.getUpdatedAt())
                    .build();
        }

    }

}
