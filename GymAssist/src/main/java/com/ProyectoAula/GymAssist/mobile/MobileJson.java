package com.ProyectoAula.GymAssist.mobile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;

/** Convierte las entidades de Mongo en JSON simple para Flutter (ids como texto, fechas ISO). */
final class MobileJson {

    private MobileJson() {}

    static String id(ObjectId id) {
        return id == null ? null : id.toHexString();
    }

    static String txt(Object o) {
        return o == null ? null : o.toString();
    }

    static Map<String, Object> gym(GimnasiosEntity g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(g.getId()));
        m.put("nombre", g.getNombreGymnasio());
        m.put("nit", g.getNit());
        m.put("direccion", g.getDireccion());
        m.put("descripcion", g.getDescripcion());
        return m;
    }

    static Map<String, Object> plan(PlanEntity p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(p.getId()));
        m.put("nombre", p.getNombre());
        m.put("descripcion", p.getDescripcion());
        m.put("precio", p.getPrecio());
        m.put("duracionMeses", p.getDuracion());
        m.put("gymId", id(p.getGymId()));
        return m;
    }

    static Map<String, Object> cliente(ClientEntity c, PlanEntity p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(c.getId()));
        m.put("nombre", c.getNombre());
        m.put("correo", c.getCorreo());
        m.put("username", c.getUsername());
        m.put("documento", c.getIdDocumento());
        m.put("telefono", txt(c.getTelefono()));
        m.put("estado", c.getEstado() == null ? "PENDIENTE" : c.getEstado().name());
        m.put("planId", id(c.getPlanId()));
        m.put("planNombre", p != null ? p.getNombre() : c.getMensualidad());
        m.put("planPrecio", p != null ? p.getPrecio() : null);
        m.put("planDuracionMeses", p != null ? p.getDuracion() : null);
        m.put("fechaIngreso", txt(c.getFechaIngresoCliente()));
        m.put("fechaInicioMembresia", txt(c.getFechaInicioMembresia()));
        m.put("fechaFinMembresia", txt(c.getFechaFinMembresia()));
        m.put("inasistencias", c.getInasistencias());
        m.put("totalAsistencias", c.getAsistencias() == null ? 0 : c.getAsistencias().size());
        m.put("suscripcion", c.getSubscriptionStatus());
        return m;
    }

    static List<Map<String, Object>> asistencias(ClientEntity c) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (c.getAsistencias() == null) return out;
        for (ClientEntity.Asistencia a : c.getAsistencias()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", txt(a.getFecha()));
            m.put("musculos", a.getMusculos() == null ? List.of() : a.getMusculos());
            out.add(m);
        }
        out.sort((x, y) -> String.valueOf(y.get("fecha")).compareTo(String.valueOf(x.get("fecha"))));
        return out;
    }

    static Map<String, Object> rutina(RutinaEntity r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(r.getId()));
        m.put("nombre", r.getNombreEjercicio());
        m.put("grupoMuscular", r.getGrupoMuscular());
        m.put("repeticiones", r.getRepeticiones());
        m.put("series", r.getSeries());
        List<Map<String, Object>> fotos = new ArrayList<>();
        if (r.getFotosRutina() != null) {
            for (RutinaEntity.FotoRutina f : r.getFotosRutina()) {
                Map<String, Object> fm = new LinkedHashMap<>();
                fm.put("url", f.getRutaImagen());
                fm.put("descripcion", f.getDescripcion());
                fotos.add(fm);
            }
        }
        m.put("fotos", fotos);
        return m;
    }

    static Map<String, Object> medicion(MedicionesEntity me, double imc, String interpretacion) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id(me.getId()));
        m.put("peso", me.getPeso());
        m.put("estatura", me.getEstatura());
        m.put("fecha", txt(me.getFechaRegistro()));
        m.put("imc", Math.round(imc * 10.0) / 10.0);
        m.put("interpretacion", interpretacion);
        return m;
    }
}
