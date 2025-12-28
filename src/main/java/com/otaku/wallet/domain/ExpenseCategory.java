package com.otaku.wallet.domain;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum ExpenseCategory {
    GOODS("굿즈", "🎁"),
    EVENT("이벤트/콘서트", "🎫"),
    STREAMING("스트리밍", "📺"),
    GAME("게임", "🎮"),
    BOOK("책/만화", "📚"),
    FOOD("덕질 음식", "🍜"),
    ETC("기타", "💰");

    private final String displayName;
    private final String emoji;

    ExpenseCategory(String displayName, String emoji){
        this.displayName = displayName;
        this.emoji = emoji;

    }
}
