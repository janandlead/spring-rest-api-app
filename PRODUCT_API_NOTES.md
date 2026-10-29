# Product API notes

These notes describe the current controller and service implementation. Examples assume the application runs locally on port 8080.

Before starting the application, set the `DB_PASSWORD` environment variable to your PostgreSQL password. The configured database is `product_db` on localhost, using the `postgres` user. Keep credentials out of source control.

## Endpoints

### Product search examples

Create two products using `POST /api/v1/products`, for example
`{"name":"Keyboard","category":"Electronics","price":49.5}` and
`{"name":"Mouse","category":"Electronics","price":19.5}`.
Then call any of these URLs (GET):

| URL | Repository method | Query style |
| --- | --- | --- |
| `/api/v1/products/search/derived?category=Electronics` | `findByCategory` | Spring Data derives the query from the method name |
| `/api/v1/products/search/jpql?category=Electronics` | `getProductCategory` | JPQL uses the `Product` entity and its fields |
| `/api/v1/products/search/native?name=Keyboard` | `gerProductName` | SQL matches the `name` column in the `products` database table |
| `/api/v1/products/search/category-and-name?category=Electronics&name=Keyboard` | `getProductCategoryAndName` | JPQL combines category (`?1`) and name (`?2`) with `AND` |

The first two return both products; the third and fourth return only Keyboard.
All searches return HTTP 200 with a JSON array of `ProductResponse` objects,
or `[]` when nothing matches. Matching uses equality without case normalization
or substring matching; ordering is unspecified. The native search requires name;
derived and JPQL searches require category; the combined search requires both (missing parameters return
HTTP 400). URL-encode values containing spaces or special characters.

These separate routes demonstrate the three repository query styles. Each flows
through `ProductController` → `ProductService` → `ProductRepository`, then maps
entities to response DTOs. Lists allow multiple products in the same category,
including multiple rows matching both category and name.

`ProductSearchTests` exercises these routes and actual repository queries using
an isolated H2 database in PostgreSQL compatibility mode. Run `mvn test`; test
database settings live in `src/test/resources/application.properties` and do not
change the application's PostgreSQL configuration. H2 checks do not establish
compatibility with a live PostgreSQL instance.

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
5. The service maps the entity to `ProductResponse`, or throws `ProductNotFoundException` when the ID is absent; the global handler maps it to HTTP 404.

A nonnumeric ID such as `/api/v1/products/abc` cannot be converted to `Long` and normally returns HTTP 400. A numeric ID with no matching row returns HTTP 404. For a missing product, the global handler returns `ErrorResponse` with timestamp, status, error, message, and path.

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
    () -> new ProductNotFoundException("Product Not Found wiht id:" + id));
```

`orElseThrow` returns the product when present and throws otherwise. `ProductNotFoundException` is translated to HTTP 404 by `GlobalExceptionHandler`. Lookup, update, and delete throw it for an absent ID.

| Failure | Handling in this project | HTTP status |
| --- | --- | --- |
| Missing product in lookup, update, or delete | Service throws `ProductNotFoundException`; global handler returns `ErrorResponse` | 404 |
| Invalid POST product name | Global handler handles `MethodArgumentNotValidException` | 400 |
| Nonnumeric ID or malformed JSON | Spring MVC error handling; not the custom validation handler | Normally 400 |
| Missing/null price with otherwise valid input reaching a save | Null unboxing fails; no custom handler is defined | Normally 500 |

Name validation uses the validation response shown below. Missing products use `ErrorResponse` with HTTP 404. The handler also maps `DuplicateProductException` to HTTP 409, although the current create service does not explicitly detect duplicates. Other error bodies depend on Spring configuration.

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

`@RestControllerAdvice` makes this advice available across controllers and writes returned bodies as response content. `@ExceptionHandler` selects the exception type handled by this method. Global scope does not mean every exception is caught: this class handles `MethodArgumentNotValidException`, `ProductNotFoundException`, and `DuplicateProductException`. The excerpt above shows the validation handler only.

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

Run the search scenarios with:

```shell
mvn -Dtest=ProductSearchTests test
```

The latest run passed all 12 test invocations with zero failures or errors. `ProductSearchTests` uses MockMvc and actual repository queries against H2 in PostgreSQL compatibility mode. It seeds Keyboard/Electronics, Mouse/Electronics, and Keyboard/Office, each priced at 49.5, and rolls back each test.

Covered scenarios:

- Derived and JPQL category searches return both Electronics products, map null availability to false, and expose numeric IDs and the expected price.
- Native search with `name=Keyboard` returns both Keyboards across categories, excluding Mouse. The call flows through `searchNative` to `getProductsByNameNative` to `gerProductName`, which executes `select * from products where name=:name`.
- The combined search returns only the Keyboard in Electronics.
- All four routes return HTTP 200 with `[]` for unmatched values and HTTP 400 when required query parameters are missing.
- Supplying only category to the native search returns HTTP 400; native search requires name.
- Supplying only category to the combined search returns HTTP 400; both fields are required.

See the [README test scenario table](README.md#search-test-scenarios) for concrete requests and expected results. The two-product examples earlier in these notes return one Keyboard; the three-product test fixture deliberately includes a second Keyboard in another category.

`ProductServiceImplTests` separately covers response mapping, null availability, and `ProductNotFoundException` for missing IDs using a mocked repository. It does not assert HTTP status or error-body mapping. `SpringRestApiApplicationTests` checks context startup using H2.

```shell
mvn -Dtest=ProductServiceImplTests test
mvn test
```

No tests were rerun for this documentation-only update. The recorded search results establish local H2 behavior, not compatibility with a live PostgreSQL instance. The search suite does not cover create/update validation or the custom not-found and duplicate-product error responses.
