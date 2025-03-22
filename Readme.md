# Server Rotation Management System

This project involves the management of servers in a rotation system. It allows users to add servers to rotation for customer availability and remove servers from rotation for maintenance.
It uses:

- Postgres to contain purchases servers
- AWS Route53 to add A Records from servers

## High Level Overview

## Add a Server to Rotation

1. Purchase a new server and manually configure it in the database for this app.
2. Click on "Add to Rotation".
3. The system creates a DNS entry to make the new server available to customers.

## Remove a Server from Rotation

1. When a server needs maintenance, click on "Remove from Rotation".
2. The system removes any DNS entries associated with the server, preventing new connections from customers.

## Types of DNS A Records

- `#{cluster_subdomain}.domain.com`: Contains 1 or more IPs. Clients make a DNS lookup and connect to one of these IPs at random.

## Data Model

### Clusters
1. `name`: string
2. `subdomain`: string (When adding a server from this cluster to rotation, an A record for `subdomain.domain.com` will be created)

### Servers
1. `friendly_name`: string
2. `cluster_id`: int
3. `ip_string`: string

### Sequence Diagram

![alt text](https://github.com/deturrisa/dns-service/blob/master/other/uml/sequence_diagram.png?raw=true)