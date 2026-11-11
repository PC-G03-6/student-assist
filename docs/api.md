# API

Base path: `/api`. All JSON field names are camelCase.


`.env` is the only place where the key is stored. The application fails to start if `TRANZY_API_KEY` is missing.

| Variable | Description | Default |
|---|---|---|
| `TRANZY_API_KEY` | Tranzy API key (sent as `X-API-KEY`) | none, required |
| `TRANZY_AGENCY_ID` | Tranzy agency id (sent as `X-Agency-Id`) | `2` (CTP Cluj-Napoca) |

## Caching

The backend is the only component that calls Tranzy. Clients read from an in-memory cache.

| Data | TTL | Property |
|---|---|---|
| vehicles | 20 s | `tranzy.vehicles-cache-ttl` |
| stops, routes, trips, shapes | 30 min | `tranzy.static-cache-ttl` |

If a refresh fails and an older value exists, the older value is served.

Worst case Tranzy usage with constant traffic: 4,320 calls/day for vehicles plus about 190 for static data, which stays under the assumed limit of 5,000 calls/day.

## Errors

Errors use the `application/problem+json` format.

| Status | Meaning |
|---|---|
| 502 | Tranzy returned an error or is unreachable |
| 503 | Tranzy rate limited the backend (HTTP 429) |

## Transit

The fields match the Tranzy OpenData API, converted to camelCase (`stop_id` becomes `stopId`).

### GET /api/transit/stops

Returns all stops.

```json
[
  {
    "stopId": 1,
    "stopName": "Example stop",
    "stopDesc": null,
    "stopLat": 46.77,
    "stopLon": 23.59,
    "locationType": 0,
    "stopCode": "1"
  }
]
```

### GET /api/transit/routes

Returns all routes.

```json
[
  {
    "routeId": 70,
    "agencyId": 2,
    "routeShortName": "35",
    "routeLongName": "Example route",
    "routeColor": "FF0000",
    "routeType": 3,
    "routeDesc": ""
  }
]
```

`routeType`: 0 tram, 3 bus, 11 trolleybus.

### GET /api/transit/vehicles

Returns live vehicle positions.

| Query param | Type | Default | Description |
|---|---|---|---|
| `activeOnly` | boolean | `false` | When `true`, returns only vehicles with a `routeId`, coordinates and a position update from the last 90 seconds |

```json
[
  {
    "id": "6",
    "label": "850",
    "latitude": 46.7797616,
    "longitude": 23.5916016,
    "timestamp": "2026-11-05T10:55:13.000Z",
    "vehicleType": 3,
    "bikeAccessible": "BIKE_INACCESSIBLE",
    "wheelchairAccessible": "WHEELCHAIR_ACCESSIBLE",
    "speed": 5,
    "routeId": 70,
    "tripId": "70_0"
  }
]
```

Without `activeOnly`, the response is the raw Tranzy data. In practice `routeId`, `tripId`, `latitude`, `longitude` and `speed` can be `null`, and some timestamps are old.
