package ua.edu.nupp.kpp.server;

import java.time.LocalDate;

/** Заявка на прокат — об'єкт чотиристанного життєвого циклу. */
public class RentalOrder {
    private Long id;
    private long clientId;
    private long equipmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private OrderState state = OrderState.CREATED;
    private double f1;            // терміновість початку (рахує сервер)
    private double f2;            // попит на обладнання (0..100)
    private double f3;            // дефіцит доступних одиниць (0..100)
    private double priorityIndex; // I = 0,40*F1 + 0,35*F2 + 0,25*F3

    public RentalOrder() {
    }

    public RentalOrder(Long id,
            long clientId,
            long equipmentId,
            LocalDate startDate,
            LocalDate endDate,
            OrderState state,
            double f1,
            double f2,
            double f3,
            double priorityIndex) {
        this.id = id;
        this.clientId = clientId;
        this.equipmentId = equipmentId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.priorityIndex = priorityIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getClientId() { return clientId; }
    public void setClientId(long clientId) { this.clientId = clientId; }
    public long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(long equipmentId) { this.equipmentId = equipmentId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }
    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }
    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }
    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }
    public double getPriorityIndex() { return priorityIndex; }
    public void setPriorityIndex(double priorityIndex) { this.priorityIndex = priorityIndex; }
}
