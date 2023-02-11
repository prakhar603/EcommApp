# EcommApp - Backend

# The project is to create an eCommerce application with a MySQL database.
# There will be 2 microservices, User and Product. 

1. The User microservice will have API's for registering a user, which will include name, email, password (which should be strong and encrypted), gender and age. 
The second API in the User microservice is for logging in, which will require email and password. Upon successful login, all user data will be sent with a JWT token. 

2. The Product microservice will have API's for ordering a product by user id, which will store the product id, user id, and quantity. 
The quantity will be removed from the stock table based on the user's input. The second API in the Product microservice is for checking stock by product id, which will return the stock level for a particular product. 
The third API in the Product microservice is for getting all orders by user id, which will fetch product details from the product table based on the user id inserted into the order table.
