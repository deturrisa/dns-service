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

INSERT INTO cluster (id, name, subdomain) VALUES
(1,'Los Angeles','la'),
(5,'Geneva','ge');

INSERT INTO server (id, friendly_name, cluster_id, ip_string) VALUES
(1, 'ubiq-1', 1, '123.123.123.123'),
(2, 'ubiq-2', 1, '125.125.125.125'),
(20, 'something', 5, '192.1.1.1');