# Login

http://localhost:8084/api/v1/public/auth/login

body:

{
  "phone": "9991557878",
  "pin": "123"
}

Es nuevo
response:

** No lo encontro porque no existe en la base de datos
{
  "ok": true,
  "result": {
    "isVerified": false,
    "jwt": null,
    "user": null,
    "tenant": null,
    "role": null
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-06T21:51:10.341897-06:00"
}

** Si lo encontramos.
{
  "ok": true,
  "result": {
    "isVerified": true,
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT1dORVIiLCJ0ZW5hbnRJZCI6IjYyZDRkY2IxLTcxZGQtNGEyOC04NWM2LWE2ODI2ZmY1NmM4NSIsInR5cGUiOiJDUk0iLCJzdWIiOiI3ZDdjMzQ5Yy1hZWU1LTQ5ZDktOTFjMS0wZjVkMzljNTU0NGMiLCJpYXQiOjE3NzU1MzQzNjksImV4cCI6MTc3NTYyMDc2OX0.EZq1fw5Y8zkiXAZegA0Ws4_nRCgB9r3hAxlWm_9D5nU",
    "user": {
      "id": "7d7c349c-aee5-49d9-91c1-0f5d39c5544c",
      "phone": "9991557878",
      "email": "bicosind@gmail.com",
      "fullName": "Gabriel Juarez",
      "role": "OWNER"
    },
    "tenant": {
      "id": "62d4dcb1-71dd-4a28-85c6-a6826ff56c85",
      "name": "Tienda Meyi",
      "email": "bicosind@gmail.com",
      "phone": "9991557878",
      "address": null,
      "plan": "STARTER",
      "subscriptionStatus": "TRIAL",
      "trialEndsAt": "2026-04-22T03:57:38.308033Z"
    },
    "role": "OWNER"
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-06T21:59:29.384213-06:00"
}



# Registro 
http://localhost:8084/api/v1/public/auth/register


{
  "storeName": "Tienda Meyi",
  "ownerName": "Gabriel Juarez",
  "phone": "9991557878",
  "email": "bicosind@gmail.com",
  "pin": "1234"
}


Respuesta:

{
  "ok": true,
  "result": {
    "isVerified": true,
    "jwt": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT1dORVIiLCJ0ZW5hbnRJZCI6IjYyZDRkY2IxLTcxZGQtNGEyOC04NWM2LWE2ODI2ZmY1NmM4NSIsInR5cGUiOiJDUk0iLCJzdWIiOiI3ZDdjMzQ5Yy1hZWU1LTQ5ZDktOTFjMS0wZjVkMzljNTU0NGMiLCJpYXQiOjE3NzU1MzQyNjAsImV4cCI6MTc3NTYyMDY2MH0.hEpTgnTSJZmuutu4giJwUHMBik5MKERGqYTlSOhw_C8",
    "user": {
      "id": "7d7c349c-aee5-49d9-91c1-0f5d39c5544c",
      "phone": "9991557878",
      "email": "bicosind@gmail.com",
      "fullName": "Gabriel Juarez",
      "role": "OWNER"
    },
    "tenant": {
      "id": "62d4dcb1-71dd-4a28-85c6-a6826ff56c85",
      "name": "Tienda Meyi",
      "email": "bicosind@gmail.com",
      "phone": "9991557878",
      "address": null,
      "plan": "STARTER",
      "subscriptionStatus": "TRIAL",
      "trialEndsAt": "2026-04-21T21:57:38.3080331-06:00"
    },
    "role": "OWNER"
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-06T21:57:40.3599186-06:00"
}


Errors:

{
  "timestamp": "2026-04-06 22:08:39",
  "status": 400,
  "error": "Bad Request",
  "code": "AUTH-005",
  "message": "El número telefónico ya se encuentra registrado.",
  "path": "/api/v1/public/auth/register",
  "validationErrors": null
}



# Verificación splash

curl -X 'GET' \
  'http://localhost:8084/api/v1/user/init' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT1dORVIiLCJ0ZW5hbnRJZCI6ImQ3YzM5NGNhLWUwNzQtNDMzOC04MWM4LTgzYjVmMmQxNThlNSIsInR5cGUiOiJDUk0iLCJzdWIiOiJhMjEzYTZlNS0wMz

 200 ok
{
  "ok": true,
  "result": {
    "user": {
      "id": "a213a6e5-038b-49a8-99fc-85a9222920cd",
      "phone": "9991557878",
      "email": "bicosind@gmail.com",
      "fullName": "Gabriel Juarez",
      "emailVerified": false
    },
    "tenant": {
      "id": "d7c394ca-e074-4338-81c8-83b5f2d158e5",
      "name": "Tienda Meyi",
      "email": "bicosind@gmail.com",
      "phone": "9991557878",
      "address": null,
      "plan": "STARTER",
      "subscriptionStatus": "TRIAL",
      "trialEndsAt": "2026-04-22T20:28:30.012027Z"
    },
    "role": "OWNER"
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-07T15:55:08.2926023-06:00"
}


  # Carga principal de nuestra pantalla reports_screen
  curl -X 'GET' \
  'http://localhost:8084/api/v1/dashboard/init' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.ey
 

 
  {
  "ok": true,
  "result": {
    "hasOpenCashSession": false,
    "cashSession": null,
    "lastClosedSession": null,
    "messages": [
      "No hay ninguna sesión de caja abierta. Por favor, abre una para comenzar a vender."
    ]
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-07T15:55:42.1367162-06:00"
}

# apertura de caja

curl -X 'POST' \
  'http://localhost:8084/api/v1/cash/open' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT1dORVIiLCJ0ZW5hbnRJZCI6ImQ3YzM5NGNhLWUwNzQtNDMzOC04MWM4LTgzYjVmMmQxNThlNSIsInR5cGUiOiJDUk0iLCJzdWIiOiJhMjEzYTZlNS0wMzhiLTQ5YTgtOTlmYy04NWE5MjIyOTIwY2QiLCJpYXQiOjE3NzU1OTg4ODksImV4cCI6MTc3NTY4NTI4OX0.CKKwXMYH1hP2iVf1U09GruWrQX81MbqzE02kOBmDyPk' \
  -H 'Content-Type: application/json' \
  -d '{
  "initialAmount": 200
}'

{
  "ok": true,
  "result": {
    "id": "f6db5c0b-c4b7-49de-808e-100fb08e7c90",
    "initialAmount": 200,
    "totalSales": 0,
    "cashTotal": 0,
    "cardTotal": 0,
    "transferTotal": 0,
    "openedAt": "2026-04-07T15:56:19.5558721-06:00",
    "closedAt": null
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-07T15:56:19.6284128-06:00"
}


# cierre de caja
{
  "actualCash": 20
}

{
  "ok": true,
  "result": {
    "id": "f6db5c0b-c4b7-49de-808e-100fb08e7c90",
    "initialAmount": 200,
    "totalSales": 0,
    "cashTotal": 0,
    "cardTotal": 0,
    "transferTotal": 0,
    "openedAt": "2026-04-07T21:56:19.555872Z",
    "closedAt": "2026-04-07T15:57:28.4451555-06:00",
    "expectedCash": 200,
    "actualCash": 20,
    "difference": -180
  },
  "errorCode": null,
  "userMessage": null,
  "timestamp": "2026-04-07T15:57:28.4978516-06:00"
}


