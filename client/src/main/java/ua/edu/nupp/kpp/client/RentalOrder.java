package ua.edu.nupp.kpp.client;

import java.time.LocalDate;

/** Клієнтська модель заявки на прокат. */
public class RentalOrder {
    private long id;
    private long clientId;
    private long equipmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private OrderState state;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
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
