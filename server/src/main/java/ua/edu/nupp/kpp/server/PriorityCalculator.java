package ua.edu.nupp.kpp.server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Розрахунок фактора F1 (терміновість початку прокату) та інтегрального показника
 * пріоритетності заявки I = 0,40*F1 + 0,35*F2 + 0,25*F3.
 */
public final class PriorityCalculator {
    /** Горизонт планування: якщо до початку прокату 14 днів і більше, F1 = 0. */
    public static final int HORIZON_DAYS = 14;

    private PriorityCalculator() {
    }

    /** F1 = max(0; min(100; (14 - d) / 14 * 100)), d - днів від today до startDate. */
    public static double factor1(LocalDate today, LocalDate startDate) {
        long days = ChronoUnit.DAYS.between(today, startDate);
        double value = (HORIZON_DAYS - days) * 100.0 / HORIZON_DAYS;
        return round2(Math.max(0.0, Math.min(100.0, value)));
    }

    /** Інтегральний показник; усі фактори мають бути в діапазоні 0..100. */
    public static double calculate(double f1, double f2, double f3) {
        checkRange("F1", f1);
        checkRange("F2", f2);
        checkRange("F3", f3);
        return round2(0.40 * f1 + 0.35 * f2 + 0.25 * f3);
    }

    private static void checkRange(String name, double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(name + " має бути в діапазоні 0..100");
        }
    }

    static double round2(double value) {
        return new BigDecimal(Double.toString(value)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
