package uce.edu.ProgramacionWeb.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long numProducto;

    // Validation rules: the same ones are checked in the HTML form and in the REST API
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code can have at most 20 characters")
    private String codigo;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Name can have at most 100 characters")
    private String nomProducto;

    @Size(max = 255, message = "Description can have at most 255 characters")
    private String descripcion;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price can have at most 2 decimals")
    private BigDecimal precio;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    @PastOrPresent(message = "The date added cannot be in the future")
    private LocalDate fechIngreso;

    // getters and setters

    public Long getNumProducto() {
        return numProducto;
    }

    public void setNumProducto(Long numProducto) {
        this.numProducto = numProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNomProducto() {
        return nomProducto;
    }

    public void setNomProducto(String nomProducto) {
        this.nomProducto = nomProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDate getFechIngreso() {
        return fechIngreso;
    }

    public void setFechIngreso(LocalDate fechIngreso) {
        this.fechIngreso = fechIngreso;
    }
}
