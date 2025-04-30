package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoServices.GimnasiosServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gimnasios")
public class GimnasiosController {

    @Autowired
    private GimnasiosServices gimnasiosServices;

    @PostMapping("/{adminId}")
    public ResponseEntity<GimnasiosEntity> createGym(@PathVariable Long adminId, @RequestBody GimnasiosEntity gimnasio) {
        gimnasio.setAdminId(adminId);
        GimnasiosEntity createdGym = gimnasiosServices.createGym(gimnasio);
        return ResponseEntity.ok(createdGym);
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<GimnasiosEntity> getGym(@PathVariable Long adminId) {
        return gimnasiosServices.getGymByAdminId(adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{adminId}")
    public ResponseEntity<GimnasiosEntity> updateGym(@PathVariable Long adminId, @RequestBody GimnasiosEntity gimnasio) {
        return ResponseEntity.ok(gimnasiosServices.updateGym(adminId, gimnasio));
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteGymByAdminId(@PathVariable Long adminId) {
        gimnasiosServices.deleteGymByAdminId(adminId);
        return ResponseEntity.noContent().build();
    }
}
