CREATE TABLE IF NOT EXISTS cluster (
    id INT PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    subdomain VARCHAR(25) NOT NULL
);

CREATE TABLE IF NOT EXISTS server (
    id INT PRIMARY KEY,
    friendly_name VARCHAR(255) NOT NULL,
    cluster_id INT NOT NULL,
    ip_string VARCHAR(39) NOT NULL,
    FOREIGN KEY (cluster_id) REFERENCES cluster(id)
);

INSERT INTO cluster (id, name, subdomain) VALUES
(4,'Hong Kong','hongkong');

INSERT INTO server (id, friendly_name, cluster_id, ip_string) VALUES
(4, 'rackspace-1', 4, '234.234.234.234'),
(5, 'rackspace-2', 4, '235.235.235.235');