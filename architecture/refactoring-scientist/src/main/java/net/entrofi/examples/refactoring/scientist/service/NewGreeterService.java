package net.entrofi.examples.refactoring.scientist.service;

import java.util.Random;

public class NewGreeterService implements GreeterService {
    @Override public String greet(String name) {
        String[] greetings = {"Hallo ", "Hello "};
        int choice = new Random().nextInt(2);
        return greetings[choice] + name;
    }
}
