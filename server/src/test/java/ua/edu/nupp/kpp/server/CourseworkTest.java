package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Модульні тести предметної логіки: не потребують MySQL, Tomcat або JavaFX. */
class CourseworkTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 22);

    @Test
    void f1WhenStartIsTodayIs100() {
        assertEquals(100.0, PriorityCalculator.factor1(TODAY, TODAY), 0.001);
    }

    @Test
    void f1WhenStartIn14DaysIsZero() {
        assertEquals(0.0, PriorityCalculator.factor1(TODAY, TODAY.plusDays(14)), 0.001);
    }

    @Test
    void f1ForDemoOrderIs78_57() {
        assertEquals(78.57, PriorityCalculator.factor1(TODAY, LocalDate.of(2026, 11, 25)), 0.001);
    }

    @Test
    void indexForDemoOrderIs70_93() {
        assertEquals(70.93, PriorityCalculator.calculate(78.57, 70, 60), 0.001);
    }

    @Test
    void indexRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(101, 10, 10));
    }

    @Test
    void createdToConfirmedAllowed() {
        assertTrue(RentalOrderService.isAllowedTransition(OrderState.CREATED, OrderState.CONFIRMED));
    }

    @Test
    void confirmedToIssuedAllowed() {
        assertTrue(RentalOrderService.isAllowedTransition(OrderState.CONFIRMED, OrderState.ISSUED));
    }

    @Test
    void issuedToReturnedAllowed() {
        assertTrue(RentalOrderService.isAllowedTransition(OrderState.ISSUED, OrderState.RETURNED));
    }

    @Test
    void createdToIssuedRejected() {
        assertFalse(RentalOrderService.isAllowedTransition(OrderState.CREATED, OrderState.ISSUED));
    }

    @Test
    void equipmentValidationRejectsEmptyCode() {
        Equipment e = new Equipment(0L, "", "Ноутбук Dell Latitude 5440", "Комп'ютерна техніка", new BigDecimal("250.00"));
        assertThrows(IllegalArgumentException.class, () -> EquipmentService.validate(e));
    }

    @Test
    void orderValidationRejectsEndBeforeStart() {
        RentalOrder o = new RentalOrder();
        o.setClientId(1);
        o.setEquipmentId(1);
        o.setStartDate(LocalDate.of(2026, 12, 5));
        o.setEndDate(LocalDate.of(2026, 12, 1));
        o.setF2(50);
        o.setF3(50);
        assertThrows(IllegalArgumentException.class, () -> RentalOrderService.validate(o));
    }

    @Test
    void contractTotalForFiveDaysIs3000() {
        BigDecimal total = RentalContractService.calculateTotal(
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14), new BigDecimal("600.00"));
        assertEquals(new BigDecimal("3000.00"), total);
    }
}
