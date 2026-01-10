package com.labinc.util;

import java.text.DecimalFormat;

/**
 * Utility class for formatting numbers and money values.
 * Provides consistent formatting across the entire application.
 */
public final class Formatter {

    private Formatter() {
    } // Prevent instantiation

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final String[] SUFFIXES = { "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc" };

    /**
     * Formats a money value with appropriate suffix (K, M, B, T, etc.)
     * For values >= 1000, uses compact notation.
     */
    public static String formatMoney(double amount) {
        if (amount < 0)
            return "-" + formatMoney(-amount);
        if (amount < 1000)
            return MONEY_FORMAT.format(amount);

        int suffixIndex = 0;
        double value = amount;
        while (value >= 1000 && suffixIndex < SUFFIXES.length - 1) {
            value /= 1000;
            suffixIndex++;
        }

        if (value >= 100) {
            return String.format("%.0f%s", value, SUFFIXES[suffixIndex]);
        } else if (value >= 10) {
            return String.format("%.1f%s", value, SUFFIXES[suffixIndex]);
        } else {
            return String.format("%.2f%s", value, SUFFIXES[suffixIndex]);
        }
    }

    /**
     * Formats amount with scientific notation for very large values.
     */
    public static String formatAmount(double value) {
        if (value < 1000) {
            return String.format("%.2f", value);
        } else if (value < 1_000_000) {
            return String.format("%.1fK", value / 1000);
        } else if (value < 1_000_000_000) {
            return String.format("%.2fM", value / 1_000_000);
        } else if (value < 1e12) {
            return String.format("%.2fB", value / 1e9);
        } else {
            return String.format("%.2e", value);
        }
    }

    /**
     * Formats mass values (kg, t, kt, Mt, etc.)
     */
    public static String formatMass(double val) {
        if (val < 1000)
            return String.format("%.1f kg", val);
        double t = val / 1000;
        if (t < 1000)
            return String.format("%.1f t", t);
        double kt = t / 1000;
        if (kt < 1000)
            return String.format("%.1f kt", kt);
        double mt = kt / 1000;
        if (mt < 1000)
            return String.format("%.1f Mt", mt);
        return String.format("%.1e kg", val);
    }

    /**
     * Formats multiplier values (e.g. 1.5x, 2.00x)
     */
    public static String formatMultiplier(double val) {
        if (val >= 100)
            return String.format("%.0fx", val);
        return String.format("%.2fx", val);
    }

    /**
     * Formats cost with $ prefix.
     */
    public static String formatCost(double amount) {
        return "$" + formatMoney(amount);
    }
}
