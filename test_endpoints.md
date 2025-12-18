# Testing Organization Endpoints

After running the database migration, test these endpoints:

## Prerequisites
1. Run the migration script: `fix_groups_table.sql`
2. Ensure you have a valid JWT token from logging in as an organization admin

## Test Endpoints

### 1. Get Organization Details
```bash
GET http://localhost:8081/api/v1/organizations/{orgId}
Headers:
  Authorization: Bearer {your_jwt_token}
```

### 2. Get Organization Groups
```bash
GET http://localhost:8081/api/v1/organizations/{orgId}/groups
Headers:
  Authorization: Bearer {your_jwt_token}
```

### 3. Get Organization Employees
```bash
GET http://localhost:8081/api/v1/organizations/{orgId}/employees
Headers:
  Authorization: Bearer {your_jwt_token}
```

### 4. Create Group
```bash
POST http://localhost:8081/api/v1/organizations/{orgId}/groups
Headers:
  Authorization: Bearer {your_jwt_token}
Body:
{
  "name": "Developers",
  "description": "Development team"
}
```

### 5. Create Employee
```bash
POST http://localhost:8081/api/v1/organizations/{orgId}/employees
Headers:
  Authorization: Bearer {your_jwt_token}
Body:
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

## Expected Results
- All endpoints should return 200 OK (or 201 Created for POST)
- No 500 errors
- Data should be returned correctly

