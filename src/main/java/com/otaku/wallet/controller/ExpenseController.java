package com.otaku.wallet.controller;


import com.otaku.wallet.dto.ExpenseDto;
import com.otaku.wallet.dto.StatisticsDto;
import com.otaku.wallet.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //JSON 반환 controller
@RequestMapping("/api/expenses") //기본 경로 설정
@RequiredArgsConstructor //final 필드를 이용한 생성자
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto.Response> createExpense(
            @Valid @RequestBody ExpenseDto.Request request) {
        return ResponseEntity.ok(expenseService.createExpense(request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto.Response>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> getExpense(@PathVariable Long id){
        return ResponseEntity.ok(expenseService.getExpense(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto.Request request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDto> getStatistics() {
        return ResponseEntity.ok(expenseService.getStatistics());
    }


    /*
    *  POST /api/expenses - 지출 등록
    *  GET /api/expenses - 지출 목록 조회
    *  GET /api/expenses/{id} - 특정 지출 조회
    *  PUT /api/expenses/{id} - 수정
    *  DELETE /api/expenses/{id} - 식제
    *  GET /api/expenses/statistics - 통계
    * */
}
