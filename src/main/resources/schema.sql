CREATE TABLE IF NOT EXITS cluster (
    id INT PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    subdomain VARCHAR(2) NOT NULL
);

CREATE TABLE IF NOT EXISTS server (
    id INT PRIMARY KEY,
    friendly_name VARCHAR(255) NOT NULL,
    cluster_id INT NOT NULL,
    ip_string VARCHAR(39) NOT NULL,
    FOREIGN KEY (cluster_id) REFERENCES cluster(id)
);
