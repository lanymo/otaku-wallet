package com.otaku.wallet.controller;


import com.otaku.wallet.dto.ExpenseDto;
import com.otaku.wallet.dto.StatisticsDto;
import com.otaku.wallet.service.ExpenseService;
import com.otaku.wallet.service.SessionService;
import jakarta.servlet.http.HttpSession;
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
    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<ExpenseDto.Response> createExpense(
            @Valid @RequestBody ExpenseDto.Request request,
            HttpSession session) {
        String userId = sessionService.getUserId(session);
        return ResponseEntity.ok(expenseService.createExpense(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto.Response>> getAllExpenses(HttpSession session) {
        String userId = sessionService.getUserId(session);
        return ResponseEntity.ok(expenseService.getAllExpenses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> getExpense(
            @PathVariable Long id,
            HttpSession session){
        String userId = sessionService.getUserId(session);
        return ResponseEntity.ok(expenseService.getExpense(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto.Response> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto.Request request,
            HttpSession session) {
        String userId = sessionService.getUserId(session);
        return ResponseEntity.ok(expenseService.updateExpense(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            HttpSession session){
        String userId = sessionService.getUserId(session);
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsDto> getStatistics(HttpSession session) {
        String userId = sessionService.getUserId(session);
        return ResponseEntity.ok(expenseService.getStatistics(userId));
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
