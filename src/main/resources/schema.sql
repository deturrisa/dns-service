CREATE TABLE IF NOT EXISTS cluster (
    id INT PRIMARY KEY,
    name VARCHAR(25) NOT NULL UNIQUE,
    subdomain VARCHAR(25) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS server (
    id INT PRIMARY KEY,
    friendly_name VARCHAR(255) NOT NULL,
    cluster_id INT NOT NULL,
    ip_string VARCHAR(39) NOT NULL,
    FOREIGN KEY (cluster_id) REFERENCES cluster(id)
);