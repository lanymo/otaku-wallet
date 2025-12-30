package com.otaku.wallet.service;

import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import com.otaku.wallet.dto.ExpenseDto;
import com.otaku.wallet.dto.ExpenseDto.Response;
import com.otaku.wallet.dto.StatisticsDto;
import com.otaku.wallet.exception.ExpenseNotFoundException;
import com.otaku.wallet.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    // 지출 생성(post)
    @Transactional
    public ExpenseDto.Response createExpense(String userId, ExpenseDto.Request request){
        Expense expense = Expense.builder()
                .userId(userId)
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .satisfactionRating(request.getSatisfactionRating())
                .purchaseDate(request.getPurchaseDate())
                .build();

        return ExpenseDto.Response.from(expenseRepository.save(expense));
    }

    // 지출 단건 조회(get)
    public ExpenseDto.Response getExpense(String userId, Long expenseId){
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        return ExpenseDto.Response.from(expense);
    }

    // 지출 전체 조회 (getAll)
    public List<Response> getAllExpenses(String userId) {
        return expenseRepository.findByUserId(userId).stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 카테고리 별 조회
    public List<Response> getExpensesByCategory(String userId, ExpenseCategory category){
        return expenseRepository.findByUserIdAndCategory(userId, category).stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 만족 지출 조회
    public List<ExpenseDto.Response> getSatisfiedExpenses(String userId) {
        return expenseRepository.findByUserIdAndSatisfactionRating(userId, 5)
                .stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 통계 조회
    public StatisticsDto getStatistics(String userId) {
        Integer totalAmount = expenseRepository.getTotalAmountByUserId(userId);
        Integer displayAmount = expenseRepository.getTotalDisplayAmountByUserId(userId);

        totalAmount = totalAmount != null ? totalAmount : 0;
        displayAmount = displayAmount != null ? displayAmount : 0;

        return StatisticsDto.builder()
                .totalAmount(totalAmount)
                .displayAmount(displayAmount)
                .savedAmount(totalAmount - displayAmount)
                .satisfiedCount(expenseRepository.countByUserIdAndSatisfactionRating(userId, 5))
                .totalCount(expenseRepository.countByUserId(userId))
                .build();
    }


    // 지출 update
    @Transactional
    public ExpenseDto.Response updateExpense(String userId, Long expenseId, ExpenseDto.Request request){
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        // update 가능 항목 (제목, 금액, 카테고리, 만족도, 설명, 구매일)
        expense.update(request.getTitle(), request.getAmount(), request.getCategory(),
                request.getDescription(), request.getSatisfactionRating(), request.getPurchaseDate());
        return ExpenseDto.Response.from(expense);
    }


    // 지출 delete
    @Transactional
    public void deleteExpense(String userId, Long expenseId){
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));

        expenseRepository.delete(expense);

    }
}
