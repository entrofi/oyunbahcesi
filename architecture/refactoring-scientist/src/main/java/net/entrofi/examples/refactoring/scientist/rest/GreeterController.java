package net.entrofi.examples.refactoring.scientist.rest;

import net.entrofi.examples.refactoring.scientist.service.GreeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreeterController {

    @Autowired
    private GreeterService greeterService;

    @GetMapping("/greet/{name}")
    public String greet(@PathVariable("name") String name) {
        return greeterService.greet(name);
    }
}
