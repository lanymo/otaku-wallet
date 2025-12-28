package com.otaku.wallet.controller;

import com.otaku.wallet.domain.ExpenseCategory;
import com.otaku.wallet.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ExpenseService expenseService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("expenses", expenseService.getAllExpenses());
        model.addAttribute("statistics", expenseService.getStatistics());
        return "index";
    }

    @GetMapping("/expenses/new")
    public String newExpenseForm(Model model) {
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("isEdit", false);
        return "form";
    }

    @GetMapping("/expenses/{id}/edit")
    public String editExpenseForm(@PathVariable Long id, Model model) {
        model.addAttribute("expense", expenseService.getExpense(id));
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("isEdit", true);
        return "form";
    }
}
