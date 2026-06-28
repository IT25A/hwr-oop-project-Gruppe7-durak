--liquibase formatted sql

--changeset system:1 dbms:postgresql
CREATE TABLE durak_games
(
	id   VARCHAR(255) PRIMARY KEY,
	game JSONB NOT NULL
);