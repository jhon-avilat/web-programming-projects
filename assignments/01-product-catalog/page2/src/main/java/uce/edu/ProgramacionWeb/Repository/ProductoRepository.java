package uce.edu.ProgramacionWeb.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.ProgramacionWeb.Model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
