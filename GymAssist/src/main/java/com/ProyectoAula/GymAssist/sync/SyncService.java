package com.ProyectoAula.GymAssist.sync;

import com.ProyectoAula.GymAssist.mongoModels.*;
import com.ProyectoAula.GymAssist.sqlModels.*;
import com.ProyectoAula.GymAssist.sqlRepositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SyncService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DimAdminRepository dimAdminRepository;

    @Autowired
    private DimGimnasioRepository dimGimnasioRepository;

    @Autowired
    private DimPlanRepository dimPlanRepository;

    @Autowired
    private DimRutinaRepository dimRutinaRepository;

    @Autowired
    private FactClienteRepository factClienteRepository;

    @Autowired
    private FactAsistenciaRepository factAsistenciaRepository;

    @Autowired
    private FactMedicionRepository factMedicionRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.springframework.transaction.annotation.Transactional
    private void truncarTablas() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE fact_asistencia, fact_cliente, fact_medicion, dim_rutina, dim_plan, dim_gimnasio, dim_admin")
                .executeUpdate();
    }

    // Se ejecuta cada hora
    @Scheduled(fixedRate = 3600000)
    @org.springframework.transaction.annotation.Transactional
    public void sincronizar() {
        System.out.println(">>> Iniciando sincronización MongoDB → PostgreSQL");
        truncarTablas();
        syncAdmins();
        syncGimnasios();
        syncPlanes();
        syncRutinas();
        syncClientes();
        syncMediciones();
        System.out.println(">>> Sincronización completada");
    }

    private void syncAdmins() {
        List<AdminEntity> admins = mongoTemplate.findAll(AdminEntity.class);
        List<DimAdmin> lista = new ArrayList<>();
        for (AdminEntity a : admins) {
            DimAdmin dim = new DimAdmin();
            dim.setIdAdmin(a.getId().toHexString());
            dim.setIdUser(a.getUserId() != null ? a.getUserId().toHexString() : null);
            if (a.getUserId() != null) {
                UserEntity user = mongoTemplate.findById(a.getUserId(), UserEntity.class);
                if (user != null) {
                    dim.setEmail(limpiar(user.getEmail()));
                    dim.setUsername(limpiar(user.getUsername()));
                }
            }
            lista.add(dim);
        }
        dimAdminRepository.saveAll(lista);
        System.out.println("  ✓ Admins sincronizados: " + lista.size());
    }

    private void syncGimnasios() {
        List<GimnasiosEntity> gimnasios = mongoTemplate.findAll(GimnasiosEntity.class);
        List<DimGimnasio> lista = new ArrayList<>();
        for (GimnasiosEntity g : gimnasios) {
            DimGimnasio dim = new DimGimnasio();
            dim.setIdGimnasio(g.getId().toHexString());
            dim.setNit(g.getNit());
            dim.setNombreGimnasio(limpiar(g.getNombreGymnasio()));
            dim.setDireccion(limpiar(g.getDireccion()));
            dim.setIdAdmin(g.getAdminId() != null ? g.getAdminId().toHexString() : null);
            lista.add(dim);
        }
        dimGimnasioRepository.saveAll(lista);
        System.out.println("  ✓ Gimnasios sincronizados: " + lista.size());
    }

    private void syncPlanes() {
        List<PlanEntity> planes = mongoTemplate.findAll(PlanEntity.class);
        List<DimPlan> lista = new ArrayList<>();
        for (PlanEntity p : planes) {
            DimPlan dim = new DimPlan();
            dim.setIdPlan(p.getId().toHexString());
            dim.setNombre(limpiar(p.getNombre()));
            dim.setDescripcion(limpiar(p.getDescripcion()));
            dim.setPrecio(p.getPrecio());
            dim.setDuracion(p.getDuracion());
            dim.setIdGimnasio(p.getGymId() != null ? p.getGymId().toHexString() : null);
            lista.add(dim);
        }
        dimPlanRepository.saveAll(lista);
        System.out.println("  ✓ Planes sincronizados: " + lista.size());
    }

    private void syncRutinas() {
        List<RutinaEntity> rutinas = mongoTemplate.findAll(RutinaEntity.class);
        List<DimRutina> lista = new ArrayList<>();
        for (RutinaEntity r : rutinas) {
            DimRutina dim = new DimRutina();
            dim.setIdRutina(r.getId().toHexString());
            dim.setNombreEjercicio(limpiar(r.getNombreEjercicio()));
            dim.setGrupoMuscular(limpiar(r.getGrupoMuscular()));
            dim.setRepeticiones(limpiar(r.getRepeticiones()));
            dim.setSeries(limpiar(r.getSeries()));
            dim.setIdGimnasio(r.getGymId() != null ? r.getGymId().toHexString() : null);
            lista.add(dim);
        }
        dimRutinaRepository.saveAll(lista);
        System.out.println("  ✓ Rutinas sincronizadas: " + lista.size());
    }

    @org.springframework.transaction.annotation.Transactional
    private void syncClientes() {
        entityManager.createNativeQuery("TRUNCATE TABLE fact_asistencia, fact_cliente").executeUpdate();
        entityManager.flush();

        List<PlanEntity> todosLosPlanes = mongoTemplate.findAll(PlanEntity.class);
        Map<String, String> gymIdPorPlanId = new HashMap<>();
        for (PlanEntity plan : todosLosPlanes) {
            if (plan.getId() != null && plan.getGymId() != null) {
                gymIdPorPlanId.put(plan.getId().toHexString(), plan.getGymId().toHexString());
            }
        }

        int pagina = 0;
        int tamano = 2000;
        int totalClientes = 0;
        int totalAsistencias = 0;

        while (true) {
            org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query()
                    .skip((long) pagina * tamano)
                    .limit(tamano);

            List<ClientEntity> clientes = mongoTemplate.find(query, ClientEntity.class);
            if (clientes.isEmpty())
                break;

            List<Object[]> rowsCliente = new ArrayList<>();
            List<Object[]> rowsAsistencia = new ArrayList<>();

            for (ClientEntity c : clientes) {
                rowsCliente.add(new Object[] {
                        c.getId().toHexString(),
                        limpiar(c.getNombre()),
                        limpiar(c.getCorreo()),
                        limpiar(c.getIdDocumento()),
                        c.getTelefono(),
                        c.getEstado() != null ? c.getEstado().name() : null,
                        limpiar(c.getSubscriptionStatus()),
                        c.getFechaIngresoCliente(),
                        c.getFechaInicioMembresia(),
                        c.getFechaFinMembresia(),
                        c.getInasistencias(),
                        c.getPlanId() != null ? c.getPlanId().toHexString() : null,
                        c.getPlanId() != null ? gymIdPorPlanId.get(c.getPlanId().toHexString()) : null
                });

                if (c.getAsistencias() != null) {
                    for (ClientEntity.Asistencia a : c.getAsistencias()) {
                        rowsAsistencia.add(new Object[] {
                                c.getId().toHexString(),
                                a.getFecha(),
                                a.getMusculos() != null ? limpiar(String.join(", ", a.getMusculos())) : null,
                                c.getPlanId() != null ? gymIdPorPlanId.get(c.getPlanId().toHexString()) : null
                        });
                    }
                }
            }

            jdbcTemplate.batchUpdate(
                    "INSERT INTO fact_cliente (id_cliente,nombre,correo,id_documento,telefono,estado,subscription_status,fecha_ingreso,fecha_inicio_membresia,fecha_fin_membresia,inasistencias,id_plan,id_gimnasio) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    rowsCliente);
            jdbcTemplate.batchUpdate(
                    "INSERT INTO fact_asistencia (id_cliente,fecha,musculos,id_gimnasio) VALUES (?,?,?,?)",
                    rowsAsistencia);

            totalClientes += rowsCliente.size();
            totalAsistencias += rowsAsistencia.size();
            System.out.println("  ... clientes procesados: " + totalClientes);
            pagina++;
        }
        System.out.println("  ✓ Clientes sincronizados: " + totalClientes);
        System.out.println("  ✓ Asistencias sincronizadas: " + totalAsistencias);
    }

    @org.springframework.transaction.annotation.Transactional
    private void syncMediciones() {
        List<PlanEntity> todosLosPlanes = mongoTemplate.findAll(PlanEntity.class);
        Map<String, String> gymIdPorPlanId = new HashMap<>();
        for (PlanEntity plan : todosLosPlanes) {
            if (plan.getId() != null && plan.getGymId() != null) {
                gymIdPorPlanId.put(plan.getId().toHexString(), plan.getGymId().toHexString());
            }
        }

        org.springframework.data.mongodb.core.query.Query q = new org.springframework.data.mongodb.core.query.Query();
        q.fields().include("_id").include("planId");
        List<ClientEntity> clientes = mongoTemplate.find(q, ClientEntity.class);
        Map<String, String> gymPorCliente = new HashMap<>();
        for (ClientEntity c : clientes) {
            if (c.getId() != null && c.getPlanId() != null) {
                String gymIdStr = gymIdPorPlanId.get(c.getPlanId().toHexString());
                gymPorCliente.put(c.getId().toHexString(), gymIdStr);
            }
        }

        entityManager.createNativeQuery("TRUNCATE TABLE fact_medicion").executeUpdate();
        entityManager.flush();

        int pagina = 0;
        int tamano = 5000;
        int total = 0;

        while (true) {
            org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query()
                    .skip((long) pagina * tamano)
                    .limit(tamano);

            List<MedicionesEntity> mediciones = mongoTemplate.find(query, MedicionesEntity.class);
            if (mediciones.isEmpty())
                break;

            List<Object[]> rows = new ArrayList<>();
            for (MedicionesEntity m : mediciones) {
                rows.add(new Object[] {
                        m.getClienteId() != null ? m.getClienteId().toHexString() : null,
                        m.getPeso(),
                        m.getEstatura(),
                        m.getFechaRegistro(),
                        m.getClienteId() != null ? gymPorCliente.get(m.getClienteId().toHexString()) : null
                });
            }

            jdbcTemplate.batchUpdate(
                    "INSERT INTO fact_medicion (id_cliente,peso,estatura,fecha_registro,id_gimnasio) VALUES (?,?,?,?,?)",
                    rows);

            total += rows.size();
            System.out.println("  ... mediciones procesadas: " + total);
            pagina++;
        }
        System.out.println("  ✓ Mediciones sincronizadas: " + total);
    }

    private String limpiar(String valor) {
        if (valor == null)
            return null;
        return valor.replace("\0", "");
    }
}