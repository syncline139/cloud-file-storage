package com.example.project.exceptions.storage;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String normalizedNewPath) {
        super(String.format("Ресурс, лежащий по пути '%s' уже существует", normalizedNewPath));
    }
}
