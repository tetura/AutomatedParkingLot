## Development
* I developed the project using IntelliJ IDEA Ultimate Edition.
* It is a simple Spring Boot application.
* For getters-setters and constructors, I preferred using Lombok. Classes look much more clear in that way.
* Apart from JavaDocs, I wrote some small comments in the codes for the sake of clarification too.
* I paid attention to warnings and tips both of the SonarLint plugin and the IDE itself.
* Before the submission, I automatically rearranged/optimized imports and automatically reformatted (beautified) codes in all classes.


## Database
H2 is used. You can see H2 configurations in the `application.properties` file. 

Database tables reflecting entities mentioned below are automatically created when the application starts up. Therefore, there is no need for a separate DDL script `schema.sql` under resources where tables are defined.

To log in the database, after starting the application up, on your browser, you can visit the following URL: http://localhost:8080/h2-console/login.jsp. Username and password can be found in the `application.properties` file. When you are in the H2 console, feel free to modify data.


## Design
The database of the system consists of and the data flow within the application goes through 4 essential entities.
* `Floor`: Each floor of the automated parking lot
* `Parking Space`: Parking slots on the floors of the automated parking lot
* `Parking Record`: Wrapped-up information about a parking event itself
* `Bill`: Parking bill/receipt/invoice generated at the end of a parking


## Flow
The flow of the system is briefly and simply as follows.
* A car to be parked enters the parking lot.
* Floors are checked. If there are floors with available (not occupied) parking spaces fitting the car in terms of ceiling height and allowed weight, they are returned.
* It is a principe to save space in an automated parking lot as much as possible. An efficient volume occupation can be accomplished by putting the car into a parking space on the floor whose ceiling height is the closest one to the car's height.
    * Reference: https://en.wikipedia.org/wiki/Automated_parking_system#Space_saving
* The most suitable floor is the floor meeting the criteria above.
* An available parking space on the floor is assigned to the car.
* This car can be pulled out of the parking lot later. Once the car left the parking space/lot, the system generates a bill for the completed parking.
  * Parking fee on the generated bill is calculated by multiplying the price-per-minute rate by parking duration in minutes within the scope of the billing process.


## Data
Under `resources`, you will find the SQL script `data.sql`. This script is executed by the application automatically when the application starts.

This script basically contains data insertions into the `floor` and `parking_spaces` tables corresponding to the `Floor` and `Parking Space` entities mentioned above. Feel free to modify these data.


## Price-per-Minute Formula
The way to determine price-per-minute is not specified in the homework instructions. Personally I preferred a kind of demand-based calculation.

```
x = floor's maximum weight capacity - occupied weight = remaining available weight

y = floor's maximum weight capacity

Price-per-minute rate = x/y
```

The less availability on the floor, the lower rate, the more preferable pricing. As mentioned, this rate is used to calculate total parking fee. (multiplied by the parking duration in minutes)


## Units
In the initial dataset created by the `data.sql` script and in the written tests,

* Height unit: centimeter
* Weight unit: kilogram

If you prefer other units, e.g. meter for height or pound for weight, feel free to modify data and test codes accordingly.


## Car ID
"Car ID" value can be any string to identify to car, but it can most likely be the licence plate code of the car.


## How to Run and Test
As mentioned, this is a simple Spring Boot application. After compilations, just run the application. You will see some data in the `floors` and `parking_places` tables in the H2 DBMS, but feel free to alter these data.

Basically, as can be seen from the `AutomatedParkingLotController` class, the application has two essential endpoints:
* `/automated-parking-lot/park`
* `/automated-parking-lot/pull-out-and-bill/{carId}`

To park a car in the parking lot, the first endpoint, `/park`, can be called. `POSTMAN` can be used to test it by sending a POST request to the following address: http://localhost:8080/automated-parking-lot/park. 
The content type must be `application/json` and the body of the request can be a JSON serialization of the `ParkingRequest` class. Here is an example request:

```
{
  "carId": "AA-11",
  "carWeight": "1190.00",
  "carHeight": "155.70"
}
```

This request will try to park a car whose ID is AA-11, weight is 1190.00 kg, and height is 155.70 cm.

To pull a parked car out of the parking lot and move to the billing process for it, the second endpoint, `/pull-out-and-bill/{carId}`, can be called. 
Unlike the parking endpoint, this one accepts a parameter embedded in the URL. Again, `POSTMAN` can be used to test it by sending a POST request to the following address: http://localhost:8080/automated-parking-lot/pull-out-and-bill/{carId} where {carId} can be replaced with the ID of the car to leave the parking lot. 
Here is an example request.

```
http://localhost:8080/automated-parking-lot/pull-out-and-bill/AA-11
```

This request will try to pull the car with the ID AA-11, which is assumed to have already been parked in the parking lot, out of the parking lot.
