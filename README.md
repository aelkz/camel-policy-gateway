# camel-policy-gateway

### APPLICATION DEFAULT ENDPOINTS

| Method | URL | Description |
| ------ | --- | ----------- |
| GET,PUT,POST,DELETE,PATCH | <container-exposed> http://0.0.0.0:8080 | Proxy Endpoint |
| GET,PUT,POST,DELETE,PATCH | <internal> http://0.0.0.0:8081 | Rest Endpoint that receives proxy requests |

### TESTING (WORKING EXAMPLES)

```
curl -k -vvv http://www.postman-echo.com/get -H 'Accept: application/json' -x "http://0.0.0.0:8080"
curl -k -vvv http://localhost:8080 -H 'Accept: application/json' # will not act as proxy server
curl -k -vvv http://localhost:8081 -H 'Accept: application/json' # will not act as proxy server
```

### TESTING (FAILED)

curl -k -vvv https://www.postman-echo.com/get -H 'Accept: application/json' -x "http://0.0.0.0:8080"

### 3SCALE CAMEL POLICY TIPS
##### BACKEND REGISTRATION (WORKAROUND)

All Backends must be registered without *https* SCHEMA.
The Backends must be registered using only *http*, then this application will exchange the Exchange.HTTP_SCHEME to *https*

