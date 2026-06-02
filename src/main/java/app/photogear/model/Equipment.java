package app.photogear.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de equipo fotográfico.
 * Categorías válidas: camera, lens, tripod, lighting, bag, accessory
 * Condición: excellent, good, fair, poor
 * Estado:    active, repair, lost, stolen, sold
 */
public class Equipment {

    private String        id;
    private String        category;
    private String        brand;
    private String        model;
    private String        serialNumber;
    private LocalDate     purchaseDate;
    private BigDecimal    purchasePrice;
    private String        condition;   // maps to equipment_condition in DB
    private String        status;

    // Garantía
    private boolean       warrantyHas;
    private LocalDate     warrantyExpiry;
    private String        warrantyProvider;

    // Seguro
    private boolean       insuranceHas;
    private String        insuranceProvider;
    private String        insurancePolicyNumber;
    private LocalDate     insuranceExpiry;

    // Reporte de incidente
    private LocalDate     reportDate;
    private String        reportDetails;

    // Metadatos
    private String        notes;
    private String        photos;      // JSON array: ["data:image/jpeg;base64,...", ...]
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Equipment() {}

    // ── Getters & Setters ────────────────────────────────────

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }

    public String getBrand()                       { return brand; }
    public void setBrand(String brand)             { this.brand = brand; }

    public String getModel()                       { return model; }
    public void setModel(String model)             { this.model = model; }

    public String getSerialNumber()                { return serialNumber; }
    public void setSerialNumber(String s)          { this.serialNumber = s; }

    public LocalDate getPurchaseDate()             { return purchaseDate; }
    public void setPurchaseDate(LocalDate d)       { this.purchaseDate = d; }

    public BigDecimal getPurchasePrice()           { return purchasePrice; }
    public void setPurchasePrice(BigDecimal p)     { this.purchasePrice = p; }

    public String getCondition()                   { return condition; }
    public void setCondition(String condition)     { this.condition = condition; }

    public String getStatus()                      { return status; }
    public void setStatus(String status)           { this.status = status; }

    public boolean isWarrantyHas()                 { return warrantyHas; }
    public void setWarrantyHas(boolean b)          { this.warrantyHas = b; }

    public LocalDate getWarrantyExpiry()           { return warrantyExpiry; }
    public void setWarrantyExpiry(LocalDate d)     { this.warrantyExpiry = d; }

    public String getWarrantyProvider()            { return warrantyProvider; }
    public void setWarrantyProvider(String s)      { this.warrantyProvider = s; }

    public boolean isInsuranceHas()                { return insuranceHas; }
    public void setInsuranceHas(boolean b)         { this.insuranceHas = b; }

    public String getInsuranceProvider()           { return insuranceProvider; }
    public void setInsuranceProvider(String s)     { this.insuranceProvider = s; }

    public String getInsurancePolicyNumber()       { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String s) { this.insurancePolicyNumber = s; }

    public LocalDate getInsuranceExpiry()          { return insuranceExpiry; }
    public void setInsuranceExpiry(LocalDate d)    { this.insuranceExpiry = d; }

    public LocalDate getReportDate()               { return reportDate; }
    public void setReportDate(LocalDate d)         { this.reportDate = d; }

    public String getReportDetails()               { return reportDetails; }
    public void setReportDetails(String s)         { this.reportDetails = s; }

    public String getNotes()                       { return notes; }
    public void setNotes(String notes)             { this.notes = notes; }

    public String getPhotos()                      { return photos; }
    public void setPhotos(String photos)           { this.photos = photos; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)     { this.createdAt = dt; }

    public LocalDateTime getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(LocalDateTime dt)     { this.updatedAt = dt; }

    @Override
    public String toString() {
        return String.format("Equipment{id='%s', brand='%s', model='%s', status='%s'}",
                id, brand, model, status);
    }
}
