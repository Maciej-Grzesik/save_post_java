# Posts Application

An application for fetching posts from an API and saving them as JSON files.

## Requirements

- **Java 21**
- **Maven 3.8+**
- (Optional) **Docker**

## Building and Running Locally

1. **Build the project:**

```sh
mvn clean package
```

2. **Run the application:**

```sh
java -jar target/*.jar
```

3. **Result:**

- JSON files will be saved in the `output/` directory (you can change this in `application.yml`).

## Configuration

In the `src/main/resources/application.yml` file you can set:

```yaml
api:
  url: https://jsonplaceholder.typicode.com/posts
save:
  directory: ./output
```


## Tests

To run tests:

```sh
mvn test
```
