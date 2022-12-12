-- This script creates an initial dataset in the established H2 database.
-- It inserts some records into the "floors" and "parking_spaces" tables.
-- It basically initializes an automated parking lot.

-- When the Spring Boot application starts, this script is automatically executed
-- after the creation of these tables corresponding to the Floor and Parking Space entities.

DELETE FROM floors;
DELETE FROM parking_spaces;
DELETE FROM parking_records;
DELETE FROM bills;

INSERT INTO floors (ceiling_height, number, allowed_weight, weight_capacity)
VALUES (195, 1, 25000, 25000),
       (130, 2, 30000, 30000),
       (170, 3, 20000, 20000);

INSERT INTO parking_spaces (floor, occupying_car_id)
VALUES (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (1, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (2, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null),
       (3, null);
