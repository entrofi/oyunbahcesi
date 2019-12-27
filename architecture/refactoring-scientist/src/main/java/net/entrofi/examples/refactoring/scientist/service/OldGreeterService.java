package net.entrofi.examples.refactoring.scientist.service;

public class OldGreeterService implements GreeterService {
    @Override
    public String greet(String name) {
        return "Hello " + name;
    }
}
