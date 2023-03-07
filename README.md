# # HVCodeTest
## Introduction


First I would like to clarify that I think I did not do exactly what CodeTest asked for. I decided to make a kind of mini API with 2 endpoints.

 - One that brings the results of hitting the Endpoint of the exercise with the page number **/{page}**
 - And another one that receives the maximum number of pages (pageCount) and performs the download of all the photos of the houses on page 1 up to pageCount. **/download/{pageCount}**

## How to use the application

### Github Project (Java 17 with Springboot and Reactor)

Clone project locally

### Local Configuration

There are 4 properties that can be defined at will by modifying the application.properties file, they are:

- ```server.port```: On which port will you want to launch the application (default ```8080```)
- ```api.photo.folder```: The name of the directory where the photos will be saved (by default ```images```)
- ```api.homevision.endpoint.url```: The url of the  endpoint provided for the CodeTest (Default: ```http://app-homevision-staging.herokuapp.com/api_project```)
- ```api.homevision.endpoint.path```: the endpoint path provided for the CodeTest (default is ```/houses```)

### Build and Run from Terminal

- Install Maven build Tool if not already installed
  ```brew install maven```
- Build project from project's root directory.
  ```mvn clean install```
- Run app ffrom project's root directory
  ```mvn spring-boot:run```


### Build and Run from IDE  

If you prefer, you can open project in an IDE such as IntelliJ:

- Update maven project: In the maven bar menu, click on circular arrows.
- Clean and install project: In the maven bar menu, expand project name, then expand Lifecycle item, click on clean first and then on install.
- Run the following class (right click on class, then choose Run/Debug): ```src/main/java/com/waltercasis/homevision/homevisioncodetest/HomevisionCodeTestApplication.java```

### Invoke API

Any of the two endpoints listed below must be used. You can also use the Postman file inside the PostmanCollection directory by importing it into Postman.

*To take into account: the methods listed here assume that the server.port is still 8080, if it was changed, replace the port with the one placed in the application.properties*

```GET 'localhost:8080/houses/{page}'```

```GET 'localhost:8080/houses/download/{pageCount}'```


## Brief summary of what I did

- First create a HouseClient which contains the methods that will connect to the provided Endpoint. Or that it has the method that is used to download the photos of each house.

- Then create the HouseService interface and its respective implementation. This is the middle layer that is used to connect the HouseController with the HouseClient. It is the one that has the functionality that is desired and makes use of the Client when necessary, if data should be manipulated or transformations must be made, it would be done here.

- Finally, there is the HouseController, which is the class in charge of exposing the endpoints, receiving the requests and calling the Service methods to obtain the results and return the response to the consumer.

## What function does each endpoint fulfill?

- The first endpoint **/houses/{page}**, is the simplest of all, it simply calls the provided endpoint, passes the sent page as a parameter, and returns the list of houses for that page as the original endpoint but wrapped in a custom response.
- The second endpoint **/download/{pageCount}**, is the one used to download the photos. What it does in simple words, it calls the endpoint N number of times, passing it page parameters from 1 to pageCount, it takes the photoUrl of all the houses of all the pages and saves the photos in an internal folder in the project.

`Information to take into account`

- All methods have a retry scheme in case certain types of exceptions are returned by the provided endpoint. I decided to implement this retry strategy, because I believe that it improves reliability against intermittent failures of external systems, increases fault tolerance, optimizes performance by reducing the number of failed operations, and avoids unnecessary interruptions in case of temporary failures.

- The download of the photos is done using reactive programming and using multiple threads. Which depends on the number of pages that you want to go through, it is possible that you receive the response from the API with a 200 ok but you continue to see how photos appear in the imgs folder, because they are still being downloaded.

## Things I should improve (TODO)

There are things that could be improved, but I did not want to delay the delivery of the Test any longer. Since I was sick and couldn't do it when I wanted to.

 - Downloading the photos has a simple validation that if the filename already exists, don't download it again. But it could be encoded as a Chain of Responsibility. And depending on how you want to solve it, you can first return the photo that is stored and if it does not exist, look for it in the API (Cache) or First download the photo, to be always updated, and if it is not possible to download it, use the stored one (Fallback) .

- Precisely the endpoint that downloads the photos, being programmed in a reactive way, the threads, even when the API has already responded, continue to be executed. This generates that although it responds with a 200ok, the response is empty. And in turn, if the maximum number of retries (2) fails in any of the threads, it provides an error response, but it is possible that all or most of the photos have been downloaded anyway. I should improve the code to correct both cases.

- Return the exceptions in their own DTO with the data considered necessary and have their own list of exceptions for each case. Ex: DownloadPhotoException, HouseNotFoundException, etc.

- Probably more and better defined logs. And definitely tests!

