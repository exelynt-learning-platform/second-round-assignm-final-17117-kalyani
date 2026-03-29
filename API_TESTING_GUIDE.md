# E-commerce Backend API Testing Guide

This guide covers:
- All APIs
- Sample JSON request/response
- Where to add JWT token
- Step-by-step testing flow

Base URL:
`http://localhost:8080`

Content Type:
`application/json`

---

## 1. Authentication APIs

### 1.1 Register (No token required)

- Method: `POST`
- URL: `/api/auth/register`

Request:
```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "Rahul@123"
}
```

Success Response (`201`):
```json
{
  "token": null,
  "email": "rahul@example.com",
  "role": "ROLE_USER"
}
```

Notes:
- Register does not return JWT token now.
- Use login API to get token.

---

### 1.2 Login (Token comes here)

- Method: `POST`
- URL: `/api/auth/login`

Request:
```json
{
  "email": "rahul@example.com",
  "password": "Rahul@123"
}
```

Success Response (`200`):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "email": "rahul@example.com",
  "role": "ROLE_USER"
}
```

Save this `token` and use it in protected APIs.

---

## 2. How to Add Token

Use this header in protected APIs:

```http
Authorization: Bearer <your_jwt_token>
```

In Postman:
1. Open request.
2. Go to `Authorization` tab.
3. Type: `Bearer Token`.
4. Paste token from login response.

Or in Headers tab:
- Key: `Authorization`
- Value: `Bearer <your_jwt_token>`

---

## 3. Product APIs

### 3.1 Get All Products (Public)
- Method: `GET`
- URL: `/api/products`
- Token: Not required

Response (`200`):
```json
[
  {
    "id": 1,
    "name": "Samsung Galaxy S24",
    "description": "8GB RAM, 256GB Storage",
    "price": 74999.00,
    "stockQuantity": 15,
    "imageUrl": "https://example.com/images/s24.jpg"
  }
]
```

### 3.2 Get Product By ID (Public)
- Method: `GET`
- URL: `/api/products/{id}`
- Token: Not required

Response (`200`):
```json
{
  "id": 1,
  "name": "Samsung Galaxy S24",
  "description": "8GB RAM, 256GB Storage",
  "price": 74999.00,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/images/s24.jpg"
}
```

### 3.3 Create Product (Protected: USER or ADMIN)
- Method: `POST`
- URL: `/api/products`
- Token: Required

Request:
```json
{
  "name": "Samsung Galaxy S24",
  "description": "8GB RAM, 256GB Storage",
  "price": 74999.00,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/images/s24.jpg"
}
```

Response (`201`):
```json
{
  "id": 1,
  "name": "Samsung Galaxy S24",
  "description": "8GB RAM, 256GB Storage",
  "price": 74999.00,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/images/s24.jpg"
}
```

### 3.4 Update Product (Protected: ADMIN only)
- Method: `PUT`
- URL: `/api/products/{id}`
- Token: Required (admin)

Request:
```json
{
  "name": "Samsung Galaxy S24 Ultra",
  "description": "12GB RAM, 512GB Storage",
  "price": 124999.00,
  "stockQuantity": 8,
  "imageUrl": "https://example.com/images/s24-ultra.jpg"
}
```

Response (`200`):
```json
{
  "id": 1,
  "name": "Samsung Galaxy S24 Ultra",
  "description": "12GB RAM, 512GB Storage",
  "price": 124999.00,
  "stockQuantity": 8,
  "imageUrl": "https://example.com/images/s24-ultra.jpg"
}
```

### 3.5 Delete Product (Protected: ADMIN only)
- Method: `DELETE`
- URL: `/api/products/{id}`
- Token: Required (admin)

Response: `204 No Content`

---

## 4. Cart APIs (Protected: own cart only)

### 4.1 Get My Cart
- Method: `GET`
- URL: `/api/cart`
- Token: Required

Response (`200`):
```json
{
  "items": [
    {
      "cartItemId": 1,
      "productId": 1,
      "productName": "Samsung Galaxy S24",
      "unitPrice": 74999.00,
      "quantity": 2,
      "subtotal": 149998.00
    }
  ],
  "total": 149998.00
}
```

### 4.2 Add Item to Cart
- Method: `POST`
- URL: `/api/cart`
- Token: Required

Request:
```json
{
  "productId": 1,
  "quantity": 2
}
```

Response (`200`): same shape as cart response.

### 4.3 Update Cart Item Quantity
- Method: `PUT`
- URL: `/api/cart/{cartItemId}`
- Token: Required

Request:
```json
{
  "quantity": 1
}
```

Response (`200`): same shape as cart response.

### 4.4 Remove Cart Item
- Method: `DELETE`
- URL: `/api/cart/{cartItemId}`
- Token: Required

Response (`200`):
```json
{
  "items": [],
  "total": 0
}
```

---

## 5. Order APIs (Protected)

### 5.1 Create Order from Cart
- Method: `POST`
- URL: `/api/orders`
- Token: Required

Request:
```json
{
  "shippingDetails": {
    "address": "221B Baker Street",
    "city": "London",
    "state": "London",
    "postalCode": "NW16XE"
  }
}
```

Response (`201`):
```json
{
  "orderId": 1,
  "totalPrice": 149998.00,
  "orderStatus": "CREATED",
  "paymentStatus": "PENDING",
  "shippingAddress": "221B Baker Street",
  "shippingCity": "London",
  "shippingState": "London",
  "shippingPostalCode": "NW16XE",
  "paymentReference": null,
  "createdAt": "2026-03-29T19:10:00.000",
  "items": [
    {
      "productId": 1,
      "productName": "Samsung Galaxy S24",
      "quantity": 2,
      "priceAtPurchase": 74999.00
    }
  ]
}
```

### 5.2 Get My Orders
- Method: `GET`
- URL: `/api/orders`
- Token: Required

Response (`200`): list of order objects.

### 5.3 Get Order By ID
- Method: `GET`
- URL: `/api/orders/{orderId}`
- Token: Required

Response (`200`): single order object.

---

## 6. Payment APIs (Protected except webhook)

### 6.1 Create Payment Intent
- Method: `POST`
- URL: `/api/payments/orders/{orderId}/intent`
- Token: Required

Response (`200`, mock mode if Stripe key is placeholder):
```json
{
  "orderId": 1,
  "paymentIntentId": "pi_mock_12345",
  "clientSecret": "pi_mock_secret_12345",
  "status": "MOCK_PENDING"
}
```

### 6.2 Update Payment Result
- Method: `POST`
- URL: `/api/payments/orders/{orderId}/result`
- Token: Required

Request (success):
```json
{
  "paymentIntentId": "pi_mock_12345",
  "success": true
}
```

Response (`200`):
```json
{
  "message": "Payment status updated"
}
```

### 6.3 Stripe Webhook
- Method: `POST`
- URL: `/api/payments/stripe/webhook`
- Token: Not required

Request:
```json
{
  "id": "evt_test_webhook"
}
```

Response (`200`):
```json
{
  "message": "Webhook endpoint ready",
  "payloadLength": "26"
}
```

---

## 7. Error Response Format

Validation error (`400`):
```json
{
  "timestamp": "2026-03-29T19:15:00.000",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "price": "must be greater than 0.0"
  }
}
```

Not found (`404`):
```json
{
  "timestamp": "2026-03-29T19:15:00.000",
  "status": 404,
  "message": "Product not found"
}
```

Unauthorized (`401`):
```json
{
  "timestamp": "2026-03-29T19:15:00.000",
  "status": 401,
  "message": "You are not allowed to access this resource"
}
```

---

## 8. Full Step-by-Step Testing Flow

1. Register normal user:
   - `POST /api/auth/register`

2. Login normal user and save token:
   - `POST /api/auth/login`
   - Copy `token` value

3. Login admin (optional for admin-only APIs):
   - `POST /api/auth/login` with:
   ```json
   {
     "email": "admin@shop.com",
     "password": "Admin@123"
   }
   ```

4. Create product:
   - `POST /api/products`
   - Add token in Authorization header

5. Get products:
   - `GET /api/products`

6. Add product to cart:
   - `POST /api/cart`
   - Use user token

7. Check cart:
   - `GET /api/cart`

8. Create order:
   - `POST /api/orders`

9. Get order list/details:
   - `GET /api/orders`
   - `GET /api/orders/{orderId}`

10. Create payment intent:
   - `POST /api/payments/orders/{orderId}/intent`

11. Mark payment result:
   - `POST /api/payments/orders/{orderId}/result`

12. Confirm payment status in order:
   - `GET /api/orders/{orderId}`

---

## 9. Quick Token Map

- No token:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `POST /api/payments/stripe/webhook`

- Token required:
  - `POST /api/products`
  - `PUT /api/products/{id}` (admin)
  - `DELETE /api/products/{id}` (admin)
  - `/api/cart/**`
  - `/api/orders/**`
  - `/api/payments/orders/**`

