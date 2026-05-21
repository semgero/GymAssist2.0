package com.ProyectoAula.GymAssist.sqlRepositories;

import com.ProyectoAula.GymAssist.sqlModels.FactCliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactClienteRepository extends JpaRepository<FactCliente, String> {}