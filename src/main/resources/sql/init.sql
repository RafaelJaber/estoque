INSERT INTO permission VALUES('ROLE_ADMIN');
INSERT INTO permission VALUES('ROLE_ROOT');
INSERT INTO permission VALUES('ROLE_PRODUCTS_READ');
INSERT INTO permission VALUES('ROLE_PRODUCTS_WRITE');
INSERT INTO permission VALUES('ROLE_UNITS_READ');
INSERT INTO permission VALUES('ROLE_PURCHASE_ORDERS_READ');

INSERT INTO permission VALUES('ROLE_ENTRIES_READ');
INSERT INTO permission VALUES('ROLE_ENTRIES_WRITE');
INSERT INTO permission VALUES('ROLE_ENTRIES_WRITE_MANUAL');

INSERT INTO permission VALUES('ROLE_STOCKS_READ');
INSERT INTO permission VALUES('ROLE_STOCKS_WRITE');

INSERT INTO permission VALUES('ROLE_PATRIMONIES_READ');
INSERT INTO permission VALUES('ROLE_PATRIMONIES_WRITE');
INSERT INTO permission VALUES('ROLE_PATRIMONIES_WRITE_ROOT');

INSERT INTO permission VALUES('ROLE_PATRIMONIES_LOCATIONS_READ');
INSERT INTO permission VALUES('ROLE_PATRIMONIES_LOCATIONS_WRITE');

INSERT INTO permission VALUES('ROLE_MOVES_READ');
INSERT INTO permission VALUES('ROLE_MOVES_WRITE_ENTRY_ITEMS');
INSERT INTO permission VALUES('ROLE_MOVES_WRITE_BETWEEN_STOCKS');
INSERT INTO permission VALUES('ROLE_MOVES_WRITE_OUT_ITEM');
INSERT INTO permission VALUES('ROLE_MOVES_WRITE_ROOT');
INSERT INTO permission VALUES('ROLE_MOVES_SERVICE_ORDER_WRITE');
INSERT INTO permission VALUES('ROLE_MOVES_WRITE_BETWEEN_BRANCH_OFFICE');

INSERT INTO permission VALUES('ROLE_NOTIFICATIONS');
INSERT INTO permission VALUES('ROLE_NOTIFICATIONS_STOCK_ITEM_LOW_LEVEL');
INSERT INTO permission VALUES('ROLE_NOTIFICATIONS_STOCK_ITEM_VERY_LOW_LEVEL');

INSERT INTO permission VALUES('ROLE_TEMPLATES_READ');
INSERT INTO permission VALUES('ROLE_TEMPLATES_WRITE');

INSERT INTO permission VALUES('ROLE_BRANCH_OFFICES_READ');
INSERT INTO permission VALUES('ROLE_BRANCH_OFFICES_WRITE');

INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('ShedStock', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Galpão - Ipatinga Horto', 'SHED', 'IPATINGA_HORTO', null, 1);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('MaintenanceStock', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Manutenção - Ipatinga Horto', 'MAINTENANCE', 'IPATINGA_HORTO', null, 1);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('ObsoleteStock', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Obsoletos - Ipatinga Horto', 'OBSOLETE', 'IPATINGA_HORTO', null, 1);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('DefectiveStock', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Defeituosos - Ipatinga Horto', 'DEFECTIVE', 'IPATINGA_HORTO', null, 1);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('CustomerStock', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Clientes', 'CUSTOMER', null, null, 1);

INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('ShedStock', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Galpão - Governador Valadares', 'SHED', 'GOVERNADOR_VALADARES', null, 2);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('MaintenanceStock', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Manutenção - Governador Valadares', 'MAINTENANCE', 'GOVERNADOR_VALADARES', null, 2);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('ObsoleteStock', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Obsoletos - Governador Valadares', 'OBSOLETE', 'GOVERNADOR_VALADARES', null, 2);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('DefectiveStock', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Defeituosos - Governador Valadares', 'DEFECTIVE', 'GOVERNADOR_VALADARES', null, 2);
INSERT INTO stocks(dtype, "id", createddate, lastmodifieddate, "name", type, city, technician, branchoffice) VALUES ('CustomerStock', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Clientes', 'CUSTOMER', null, null, 2);

INSERT INTO branch_offices("id", createddate, lastmodifieddate, city, "name") VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'IPATINGA_HORTO', 'Estoque Ipatinga');
INSERT INTO branch_offices("id", createddate, lastmodifieddate, city, "name") VALUES (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'GOVERNADOR_VALADARES', 'Estoque Governador Valadares');


INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (1, 'Galpão - Ipatinga Horto', '1', null, 'SHED');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (2, 'Manutenção - Ipatinga Horto', '2', null, 'MAINTENANCE');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (3, 'Obsoletos - Ipatinga Horto', '3', null, 'OBSOLETE');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (4, 'Defeituosos - Ipatinga Horto', '4', null, 'DEFECTIVE');

INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (5, 'Galpão - Governador Valadares', '5', null, 'SHED');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (6, 'Manutenção - Governador Valadares', '6', null, 'MAINTENANCE');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (7, 'Obsoletos - Governador Valadares', '7', null, 'OBSOLETE');
INSERT INTO patrimony_locations("id", "name", "code", "note", "type") VALUES (8, 'Defeituosos - Governador Valadares', '8', null, 'DEFECTIVE');