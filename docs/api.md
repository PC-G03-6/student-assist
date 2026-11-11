# API

Base path: `/api`. All JSON field names are camelCase.

## Endpoints overview

| Method | Path | Description | Params |
|---|---|---|---|
| GET | `/api/transit/stops` | All transit stops | none |
| GET | `/api/transit/routes` | All transit routes | none |
| GET | `/api/transit/vehicles` | Live vehicle positions | `activeOnly` (optional, boolean) |
| GET | `/api/buildings` | All known UBB buildings with coordinates | none |
| GET | `/api/buildings/{id}/route` | Transit options and estimated time from a point to a building | `id` (path), `fromLat`, `fromLon` (required) |


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

## Buildings

### GET /api/buildings

Returns all known UBB buildings. Buildings are stored in the SQLite database and the default set is inserted at startup if missing.

```json
[
  {
    "id": 1,
    "code": "CENTRAL",
    "name": "Cladirea Centrala UBB",
    "address": "Str. Mihail Kogalniceanu 1",
    "latitude": 46.7675278,
    "longitude": 23.5913333
  }
]
```

Default buildings: `CENTRAL`, `AVRAM_IANCU`, `FSEGA`, `DIMITRIE_CANTEMIR`, `MATHEMATICA`, `DREPT`.

### GET /api/buildings/{id}/route

Returns up to three direct transit lines from a point to the building, fastest first. Lines are lines whose shape passes near the user and later near the building, in the direction of travel.

| Query param | Type | Required | Description |
|---|---|---|---|
| `fromLat` | number | yes | Latitude of the user, between -90 and 90 |
| `fromLon` | number | yes | Longitude of the user, between -180 and 180 |

Example: `GET /api/buildings/3/route?fromLat=46.7712&fromLon=23.5897`

Response (values are illustrative):

```json
{
  "building": {
    "id": 3,
    "code": "FSEGA",
    "name": "Cladirea FSEGA",
    "address": "Str. Teodor Mihali 58-60",
    "latitude": 46.77325,
    "longitude": 23.6216111
  },
  "origin": { "latitude": 46.7712, "longitude": 23.5897 },
  "directDistanceMeters": 2380,
  "walkOnlyMinutes": 42,
  "options": [
    {
      "routeId": 70,
      "routeShortName": "35",
      "routeLongName": "Example route",
      "routeType": 3,
      "directionId": 0,
      "headsign": "Example destination",
      "boardStop": {
        "stopId": 101,
        "stopName": "Example stop A",
        "latitude": 46.7714,
        "longitude": 23.5901,
        "walkMeters": 60
      },
      "alightStop": {
        "stopId": 202,
        "stopName": "Example stop B",
        "latitude": 46.7731,
        "longitude": 23.6209,
        "walkMeters": 90
      },
      "walkToStopMinutes": 2,
      "busArrivesInMinutes": 4,
      "rideMinutes": 9,
      "walkToBuildingMinutes": 2,
      "totalMinutes": 15,
      "liveEstimate": true,
      "vehicleLabel": "850",
      "path": [
        { "latitude": 46.7714, "longitude": 23.5901 }
      ]
    }
  ]
}
```

| Field | Description |
|---|---|
| `directDistanceMeters`, `walkOnlyMinutes` | Straight-line distance and walking time without transit, to compare against the options |
| `boardStop`, `alightStop` | Stops to board and leave at. `walkMeters` is the straight-line distance from the user (board) or to the building (alight) |
| `walkToStopMinutes` | Walking time from the user to `boardStop` |
| `busArrivesInMinutes` | Minutes from now until the bus reaches `boardStop`, never less than `walkToStopMinutes` |
| `rideMinutes` | Estimated time on the vehicle |
| `walkToBuildingMinutes` | Walking time from `alightStop` to the building |
| `totalMinutes` | `busArrivesInMinutes + rideMinutes + walkToBuildingMinutes` |
| `liveEstimate` | `true` when a live vehicle was found, `false` when the wait is an assumed 8 minutes after reaching the stop |
| `vehicleLabel` | Label of the live vehicle used for the estimate, or `null` |
| `path` | Coordinates of the line between `boardStop` and `alightStop`, for drawing on a map |

`options` is an empty list when no direct line is found. Walking legs are not included in `path`: draw `origin` to `boardStop` and `alightStop` to the building.

#### How the estimate is computed

- Stops within 700 m of the user and of the building are considered, and a line is used only if the stops are within 80 m of its shape.
- Walking: straight-line distance multiplied by 1.3, at 75 m per minute.
- Vehicle: speed of 300 m per minute (18 km/h) along the shape. The speed reported by Tranzy is not used.
- Live wait: the nearest active vehicle behind the boarding stop on the same route whose `tripId` ends with the line direction (`routeId_direction`), that the user can still reach in time.
- Schedule data (`stop_times`) is not used yet.

#### Errors

| Status | Meaning |
|---|---|
| 400 | Missing or out of range `fromLat` or `fromLon` |
| 404 | Building not found |
| 502, 503 | Tranzy error, see above |

## Upstream calls to Tranzy

These are not exposed by the app. The backend calls them with the `X-API-KEY` and `X-Agency-Id` headers, base URL `https://api.tranzy.ai/v1/opendata`.

| Tranzy endpoint | Used by | Cached for |
|---|---|---|
| `GET /stops` | `/api/transit/stops`, building routes | 30 min |
| `GET /routes` | `/api/transit/routes`, building routes | 30 min |
| `GET /vehicles` | `/api/transit/vehicles`, building routes | 20 s |
| `GET /trips` | building routes | 30 min |
| `GET /shapes` | building routes | 30 min |