package ua.edu.nupp.kpp.server;

import java.math.BigDecimal;

/** Обладнання з каталогу прокату. */
public class Equipment {
    private Long id;
    private String code;
    private String name;
    private String category;
    private BigDecimal pricePerDay;

    public Equipment() {
    }

    public Equipment(Long id,
            String code,
            String name,
            String category,
            BigDecimal pricePerDay) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.pricePerDay = pricePerDay;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
}
