package ua.edu.nupp.kpp.server;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Договір прокату, укладений за заявкою. */
public class RentalContract {
    private Long id;
    private long orderId;
    private String contractNumber;
    private LocalDate signDate;
    private BigDecimal totalPrice;

    public RentalContract() {
    }

    public RentalContract(Long id,
            long orderId,
            String contractNumber,
            LocalDate signDate,
            BigDecimal totalPrice) {
        this.id = id;
        this.orderId = orderId;
        this.contractNumber = contractNumber;
        this.signDate = signDate;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
