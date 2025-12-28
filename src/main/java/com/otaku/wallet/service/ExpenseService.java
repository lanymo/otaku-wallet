package com.otaku.wallet.service;

import com.otaku.wallet.domain.Expense;
import com.otaku.wallet.domain.ExpenseCategory;
import com.otaku.wallet.dto.ExpenseDto;
import com.otaku.wallet.dto.ExpenseDto.Response;
import com.otaku.wallet.dto.StatisticsDto;
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
    public ExpenseDto.Response createExpense(ExpenseDto.Request request){
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .satisfactionRating(request.getSatisfactionRating())
                .purchaseDate(request.getPurchaseDate())
                .build();

        return ExpenseDto.Response.from(expenseRepository.save(expense));
    }

    // 지출 단건 조회(get)
    public ExpenseDto.Response getExpense(Long expenseId){
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        return ExpenseDto.Response.from(expense);
    }

    // 지출 전체 조회 (getAll)
    public List<Response> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 카테고리 별 조회
    public List<Response> getExpensesByCategory(ExpenseCategory category){
        return expenseRepository.findByCategory(category).stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 만족 지출 조회
    public List<ExpenseDto.Response> getSatisfiedExpenses() {
        return expenseRepository.findBySatisfactionRating(5)
                .stream()
                .map(ExpenseDto.Response::from)
                .collect(Collectors.toList());
    }

    // 통계 조회
    public StatisticsDto getStatistics() {
        Integer totalAmount = expenseRepository.getTotalAmount();
        Integer displayAmount = expenseRepository.getTotalDisplayAmount();

        totalAmount = totalAmount != null ? totalAmount : 0;
        displayAmount = displayAmount != null ? displayAmount : 0;

        return StatisticsDto.builder()
                .totalAmount(totalAmount)
                .displayAmount(displayAmount)
                .savedAmount(totalAmount - displayAmount)
                .satisfiedCount(expenseRepository.countBySatisfactionRating(5))
                .totalCount(expenseRepository.count())
                .build();
    }


    // 지출 update
    @Transactional
    public ExpenseDto.Response updateExpense(Long expenseId, ExpenseDto.Request request){
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // update 가능 항목 < 금액(잘못 입력한 경우), satisfactionRating(만족도 수정), description 수정)
        expense.update(request.getAmount(), request.getCategory(), request.getDescription()
                , request.getSatisfactionRating(), request.getPurchaseDate());
        return ExpenseDto.Response.from(expense);
    }


    // 지출 delete
    @Transactional
    public void deleteExpense(Long expenseId){
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        expenseRepository.delete(expense);

    }
}
