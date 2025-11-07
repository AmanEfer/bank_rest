package com.example.bankcards.util;

public final class CardMasker {

    private static final String FULL_MASK = "**** **** **** ****";
    private static final String MASK = "**** **** **** ";


    private CardMasker() {
    }


    public static String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty())
            throw new IllegalArgumentException("Неверное значение строки cardNumber: null или пустая строка");

        return cardNumber.length() == 16
                ? MASK + cardNumber.substring(12)
                : FULL_MASK;
    }

}
