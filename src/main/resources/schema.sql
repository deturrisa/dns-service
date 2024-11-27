CREATE TABLE cluster (
    id SERIAL PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    subdomain VARCHAR(2) NOT NULL
);

CREATE TABLE server (
    id SERIAL PRIMARY KEY,
    friendly_name VARCHAR(255) NOT NULL,
    cluster_id INT NOT NULL,
    ip_string VARCHAR(39) NOT NULL,
    FOREIGN KEY (cluster_id) REFERENCES cluster(id)
);
