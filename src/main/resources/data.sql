
INSERT INTO cluster (id, name, subdomain)
VALUES (1, 'Los Angeles', 'la'),
       (2, 'New York', 'nyc'),
       (3, 'Frankfurt', 'fra'),
       (4, 'Hong Kong', 'hongkong'),
       (5, 'Geneva', 'ge')
ON CONFLICT DO NOTHING; -- avoids conflicts when test data is redeployed with app

INSERT INTO server (id, friendly_name, cluster_id, ip_string)
VALUES (1, 'ubiq-1', 1, '123.123.123.123'),
       (2, 'ubiq-2', 1, '125.125.125.125'),
       (3, 'leaseweb-de-1', 3, '12.12.12.12'),
       (4, 'rackspace-1', 4, '234.234.234.234'),
       (5, 'rackspace-2', 4, '235.235.235.235'),
       (20, 'something', 5, '192.1.1.1')
ON CONFLICT DO NOTHING;