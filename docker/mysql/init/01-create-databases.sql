CREATE DATABASE IF NOT EXISTS saga_sale;
CREATE DATABASE IF NOT EXISTS saga_inventory;
CREATE DATABASE IF NOT EXISTS saga_payment;



USE saga_sale;

DROP TABLE IF EXISTS sales;

CREATE TABLE sales (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    product_id      INT NOT NULL,
    user_id         INT NOT NULL,
    seller_id  INT NOT NULL,
    offer_id        INT NOT NULL,
    value           DECIMAL(15, 2) NOT NULL,
    status_id       INT NOT NULL,
    quantity        INT NOT NULL,
    type_sale_id    INT NOT NULL
);

COMMIT;

USE saga_inventory;

DROP TABLE IF EXISTS inventories;

CREATE TABLE inventories (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    seller_id    INT NOT NULL,
    product_id   INT NOT NULL,
    quantity     INT NOT NULL,
    offer_price  DECIMAL(15, 2) NOT NULL
);

-- Inserts de exemplo (seguindo seu script anterior de inventário)
INSERT INTO inventories (seller_id, product_id, quantity, offer_price) VALUES
(1,  1, 50,  19.90),   -- Wolverine vende produto 1
(1,  1, 30,  35.50),   -- Wolverine vende produto 1 (outra oferta)
(4,  1, 20,  99.99),   -- Nightcrawler vende produto 1
(4,  1, 10, 250.00);   -- Nightcrawler vende produto 1

COMMIT;

USE saga_payment;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    balance  DECIMAL(15, 2) NOT NULL
);

-- Inserts de exemplo (compradores / vendedores)
INSERT INTO users (name, balance) VALUES
('Wolverine',    0.00),     -- id 1 (vendedor)
('Jean Grey', 1000.00),     -- id 2 (comprador)
('Storm',     1500.00),     -- id 3 (comprador)
('Nightcrawler', 0.00);     -- id 4 (vendedor)



CREATE TABLE payments (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id  INT NOT NULL,
    selle_id  INT NOT NULL,
    offer_id  INT NOT NULL,
    value     DECIMAL(15, 2) NOT NULL
);

-- Inserts de exemplo (pagamentos já executados)
INSERT INTO payments (buyer_id, selle_id, offer_id, value) VALUES
(2, 1, 1001,  19.90),
(2, 1, 1002,  35.50),
(3, 4, 1003,  99.99),
(3, 4, 1004, 250.00);


COMMIT;



