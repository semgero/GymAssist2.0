package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {

    @Autowired
    private RutinaService rutinaService;

    @PostMapping
    public ResponseEntity<RutinaEntity> createRutina(@RequestBody RutinaEntity rutina) {
        return ResponseEntity.ok(rutinaService.createRutina(rutina));
    }

    @GetMapping("/gym/{gymId}")
    public ResponseEntity<List<RutinaEntity>> getRutinasByGymId(@PathVariable Long gymId) {
        return ResponseEntity.ok(rutinaService.getRutinasByGymId(gymId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutinaEntity> getRutinaById(@PathVariable String id) {
        return rutinaService.getRutinaById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutinaEntity> updateRutina(@PathVariable String id, @RequestBody RutinaEntity rutina) {
        return ResponseEntity.ok(rutinaService.updateRutina(new ObjectId(id), rutina));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRutinaById(@PathVariable String id) {
        rutinaService.deleteRutinaById(new ObjectId(id));
        return ResponseEntity.noContent().build();
    }
}