--liquibase formatted sql

--changeset luntik:1
CREATE TABLE users
(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(50) NOT NULL
);