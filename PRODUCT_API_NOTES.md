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

## Mapping and validation notes

- `Product` is the database entity; `ProductResponse` is the response body used by the controller.
- Entity availability uses `Boolean` so existing database `NULL` values can be loaded. `Boolean.TRUE.equals(...)` maps those values to `false` in responses without changing the stored value.
- The database column remains `is_avialable` for compatibility with the existing schema.
- `ProductRequest.price` is a nullable `Double`, while the entity uses primitive `double`. A missing or null price currently causes an unboxing error; it is not handled as a validation error. Supply a non-null price for create and update.
- There are currently no Bean Validation constraints for name, category, or price. These notes describe implementation behavior, not additional validation guarantees.

## Verification

`ProductServiceImplTests` covers lookup mapping with null availability and HTTP 404 status on a missing product. It uses a mocked repository and does not verify HTTP routing or a live database.

Run it when Maven dependencies are available:

```shell
mvn -Dtest=ProductServiceImplTests test
```

The previous execution was blocked because the Spring Boot parent dependency was unavailable locally and dependency downloads were restricted. These documentation changes have not been validated with generated Javadoc or live endpoint calls.
