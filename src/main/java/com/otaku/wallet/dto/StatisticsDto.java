package com.otaku.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsDto {
    private final Integer totalAmount;      // 실제 총액
    private final Integer displayAmount;    // 표시 총액
    private final Integer savedAmount;      // 절약액
    private final Long satisfiedCount;      // 만족 지출 개수
    private final Long totalCount;          // 전체 지출 개수
}