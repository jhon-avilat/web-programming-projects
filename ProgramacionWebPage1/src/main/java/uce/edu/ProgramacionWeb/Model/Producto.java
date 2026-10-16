package uce.edu.ProgramacionWeb.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long numProducto;

    private String codigo;
    private String nomProducto;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
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
