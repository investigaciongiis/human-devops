package com.suken27.humanfactorsjava.rest.api;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.suken27.humanfactorsjava.model.controller.ModelController;
import com.suken27.humanfactorsjava.model.dto.HumanFactorDto;

@RestController
public class HumanFactorController {
    
    private ModelController modelController;

    public HumanFactorController(ModelController modelController) {
        this.modelController = modelController;
    }

    @GetMapping("/humanfactor")
    public List<HumanFactorDto> all() {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return modelController.getAllHumanFactors(teamManagerEmail);
    }

    @GetMapping("/humanfactor/{id}")
    public HumanFactorDto one(@PathVariable Long id) {
        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return modelController.getHumanFactor(teamManagerEmail, id);
    }

}
