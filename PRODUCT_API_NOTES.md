# Product API notes

These notes describe the current controller and service implementation. Examples assume the application runs locally on port 8080.

Before starting the application, set the `DB_PASSWORD` environment variable to your PostgreSQL password. The configured database is `product_db` on localhost, using the `postgres` user. Keep credentials out of source control.

## Endpoints

| Method | Path | Behavior | Success |
| --- | --- | --- | --- |
| GET | `/api/v1/products` | Returns all products; an empty database returns `[]`. | 200 |
| GET | `/api/v1/products/{id}` | Returns one product by database ID. | 200 |
| POST | `/api/v1/products` | Creates a product and sets availability to `true`. | 200 |
| PUT | `/api/v1/products/id/{id}` | Updates only name and price for an existing product. | 200 |

The GET and PUT paths differ: PUT currently includes the extra `/id/` segment.

## Retrieve a product by ID

```http
GET http://localhost:8080/api/v1/products/7
```

Example response when product 7 exists:

```json
{
  "id": 7,
  "name": "Keyboard",
  "category": "Electronics",
  "available": true,
  "price": 49.5
}
```

1. `@GetMapping("/{id}")` matches the URL beneath the controller's base path.
2. `@PathVariable("id") Long id` binds the URL segment to the Java argument. This is a path variable, not a query parameter such as `?id=7`.
3. The controller delegates to `ProductService.getProductById(id)`.
4. `ProductRepository.findById(id)` returns an `Optional<Product>` using the entity's primary key. Spring Data supplies this method; no custom repository query is needed.
5. The service maps the entity to `ProductResponse`, or throws `ResponseStatusException` with HTTP 404 when the ID is absent.

A nonnumeric ID such as `/api/v1/products/abc` cannot be converted to `Long` and normally returns HTTP 400. A numeric ID with no matching row returns HTTP 404. The error body's fields and inclusion of the exception reason depend on Spring Boot error configuration.

## Create and update requests

Send JSON with `Content-Type: application/json`.

Create a product with `POST /api/v1/products`:

```json
{
  "name": "Keyboard",
  "category": "Electronics",
  "price": 49.5
}
```

The database generates the ID. The service sets availability to `true`, ignoring the request DTO's legacy `isAvialable` field.

Update product 7 with `PUT /api/v1/products/id/7`:

```json
{
  "name": "Wireless Keyboard",
  "price": 59.5
}
```

The update preserves the stored category and availability. An absent ID returns HTTP 404; the update does not create a new product.

## Mapping notes

- `Product` is the database entity; `ProductResponse` is the response body used by the controller.
- Entity availability uses `Boolean` so existing database `NULL` values can be loaded. `Boolean.TRUE.equals(...)` maps those values to `false` in responses without changing the stored value.
- The database column remains `is_avialable` for compatibility with the existing schema.
- `ProductRequest.price` is a nullable `Double`, while the entity uses primitive `double`. A missing or null price currently causes an unboxing error; it is not handled as a validation error. Supply a non-null price for create and update.

## Exceptions and HTTP errors

An exception signals that an operation could not complete normally. Throwing it stops the normal execution path; Spring can translate it into an HTTP error response.

For a missing product, the service uses:

```java
Product product = productRepository.findById(id).orElseThrow(
    () -> new ResponseStatusException(
        HttpStatus.NOT_FOUND, "Product not found with id: " + id));
```

`orElseThrow` returns the product when present and throws otherwise. `ResponseStatusException` is an unchecked exception that carries an HTTP status and reason. The current lookup, update, and delete operations use it to return HTTP 404 for an absent ID.

| Failure | Handling in this project | HTTP status |
| --- | --- | --- |
| Missing product in lookup, update, or delete | Service throws `ResponseStatusException` | 404 |
| Invalid POST product name | Global handler handles `MethodArgumentNotValidException` | 400 |
| Nonnumeric ID or malformed JSON | Spring MVC error handling; not the custom validation handler | Normally 400 |
| Missing/null price with otherwise valid input reaching a save | Null unboxing fails; no custom handler is defined | Normally 500 |

Only the name-validation case has the custom response format shown below. Other error bodies depend on Spring's error handling and configuration.

## Bean Validation and `@Valid`

`pom.xml` declares `spring-boot-starter-validation`. The DTO uses constraints from `jakarta.validation.constraints`, and the controller imports `jakarta.validation.Valid`.

The current name constraints in `ProductRequest` are:

```java
@NotBlank(message = "Name is required")
@Size(min = 3, max = 10, message = "Name must be 3-10 charectors")
private String name;
```

- `@NotBlank` rejects null, empty, and whitespace-only names.
- `@Size` requires a length of 3 through 10, inclusive. It allows null by itself, so `@NotBlank` supplies the required-value check. It does not trim the name.
- `message` supplies the field error text. The spelling `charectors` above matches the current code and response.

The POST method activates validation for the deserialized request:

```java
@PostMapping
public ProductResponse addProduct(@Valid @RequestBody ProductRequest productRequest) {
    return productService.createProduct(productRequest);
}
```

`@RequestBody` reads JSON into `ProductRequest`. `@Valid` asks Spring to validate that object's constraints before invoking the controller method. It does not define constraints itself. If validation fails here, Spring raises `MethodArgumentNotValidException`, and the service is not called.

The flow is: JSON request → DTO binding → validation → controller → service → repository. Failed validation branches to the exception handler instead of continuing to the controller.

### Current validation limits

- POST uses `@Valid`; PUT currently has only `@RequestBody`, so it does not activate these DTO constraints. The longer name in the PUT example above therefore is not rejected by this validation mechanism.
- Category and price have no Bean Validation constraints. A null price can still cause an unboxing error; zero and negative prices are not rejected by DTO validation.
- Calling the service directly does not trigger the controller's `@Valid` check.
- Possible future improvements are adding `@Valid` to PUT and adding `@NotNull` plus `@Positive` to price if the business requires a price greater than zero. These are suggestions, not current behavior.

## Global exception handler

`GlobalExceptionHandler` centralizes validation error responses:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
            error -> errors.put(error.getField(), error.getDefaultMessage()));

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .validationErrors(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

`@RestControllerAdvice` makes this advice available across controllers and writes returned bodies as response content. `@ExceptionHandler` selects the exception type handled by this method. Global scope does not mean every exception is caught: this class currently handles only `MethodArgumentNotValidException`.

`getBindingResult().getFieldErrors()` provides the failed fields and constraint messages. The handler collects them into a map and builds `ValidationErrorResponse`, whose fields are `timestamp`, `status`, `error`, and `validationErrors`. `ResponseEntity` sets the actual HTTP status to 400; the body's `status` field merely reports that same value.

The map stores one message per field. If multiple constraints fail for the same field, later entries overwrite earlier ones; clients should not rely on which message survives. Only field errors are collected, not object-level errors.

### Invalid request example

Send `POST /api/v1/products` with `Content-Type: application/json`:

```json
{
  "name": "AB",
  "category": "Electronics",
  "price": 49.5
}
```

Expected HTTP 400 response based on the handler (timestamp is illustrative):

```json
{
  "timestamp": "2026-10-29T14:30:00",
  "status": 400,
  "error": "Validation Failed",
  "validationErrors": {
    "name": "Name must be 3-10 charectors"
  }
}
```

Omitting `name` or sending it as null produces `"Name is required"`. The timestamp uses server-local `LocalDateTime` and contains no timezone offset.

## Verification

`ProductServiceImplTests` covers lookup mapping with null availability and HTTP 404 status on a missing product. It uses a mocked repository and does not verify HTTP routing or a live database.

Run it when Maven dependencies are available:

```shell
mvn -Dtest=ProductServiceImplTests test
```

The previous execution was blocked because the Spring Boot parent dependency was unavailable locally and dependency downloads were restricted. These notes were checked against the DTO, controller, service, exception handler, and dependency declarations; no live endpoint calls were run for this documentation update. The existing service tests do not establish HTTP validation or the global handler's response behavior.
