# Queue schema

# --- !Ups

CREATE TABLE Queue (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    data CLOB,
    gathered_time timestamp,
    server_time timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE VisitorData(
    data_time timestamp,
    website varchar(300),
    visitors int,
    primary key (data_time, website)
);

CREATE TABLE ReferrerData(
    data_time timestamp,
    website varchar(300),
    referrer varchar(300),
    visitors int,
    primary key (data_time, website, referrer)
);

# --- !Downs

DROP TABLE Queue;
DROP TABLE ReferrerData;
DROP TABLE VisitorData;