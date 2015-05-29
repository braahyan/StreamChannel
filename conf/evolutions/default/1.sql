# Queue schema

# --- !Ups

CREATE TABLE Queue (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    data CLOB,
    gathered_time timestamp,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE Queue;