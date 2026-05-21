package com.ProyectoAula.GymAssist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ProyectoAula.GymAssist.models.ModeloRequest;
import com.ProyectoAula.GymAssist.models.ModeloResponse;
import com.ProyectoAula.GymAssist.mongoServices.PythonOptimizationService;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/modelo")
public class ModeloController {
    
    @Autowired
    private PythonOptimizationService service;

    @GetMapping
    public String vistaModelo(Model model) {

        model.addAttribute("request", new ModeloRequest());

        return "modelo";
    }

    @PostMapping("/calcular")
    public String calcular(
            @ModelAttribute("request") ModeloRequest request,
            Model model
    ) {

        ModeloResponse resultado = service.resolver(request);

        // enviar resultado
        model.addAttribute("resultado", resultado);

        // IMPORTANTE: volver a enviar request
        model.addAttribute("request", request);

        return "modelo";
    }
}
