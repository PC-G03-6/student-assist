# Spike Tranzy API

Scop: documentam API-ul Tranzy OpenData, limitele lui, ID-ul agentiei Cluj si decidem cum calculam timpii de transport pentru aplicatie.

---

## 1. Prezentare generala

- URL de baza: `https://api.tranzy.ai/v1/opendata`
- Documentatie Swagger: `https://api.tranzy.ai/v1/opendata/docs`
- Status API: BETA (se poate schimba fara notificare)
- Model de date: asemanator cu GTFS
- Endpoint-uri disponibile: Agency, Vehicles, Routes, Trips, Shapes, Stops
- Nu exista endpoint pentru Stop Times. Orele la statii vin din zip-ul GTFS oficial CTP (vezi sectiunea 7).
- Format: JSON
- Coduri de raspuns la fiecare endpoint: `200`, `403`, `429`, `500`

### Autentificare si headere

| Header | Valoare                                    | Note                                                                                         |
|---|--------------------------------------------|----------------------------------------------------------------------------------------------|
| `X-Agency-Id` | ID-ul agentiei, de exemplu `2` pentru Cluj | ID-ul agentiei de la care se iau datele. Lista agentiilor se vede la `/agency`.              |
| Header cheie API | `X-API-KEY`              | Cheia principala este in `.env`, si este inregistrata pentru aplicatia noastra in tranzy.dev |

### ID-ul agentiei Cluj-Napoca

- `agency_id = 2` pentru CTP Cluj-Napoca. 
- Se confirma cu `GET /agency`, citind `agency_name`.

### Exemplu de request

```bash
curl -H "X-API-KEY: $TRANZY_KEY" \
     -H "X-Agency-Id: 2" \
     "https://api.tranzy.ai/v1/opendata/vehicles"
```

---

## 2. Agency

`GET https://api.tranzy.ai/v1/opendata/agency`

O lista de branduri de transport, adesea sinonime cu o agentie de transport. De retinut ca, in unele cazuri, de exemplu cand o singura agentie opereaza mai multe servicii separate, agentiile si brandurile sunt distincte. Un set de date poate contine date de la mai multe agentii.
Mai multe detalii: https://gtfs.org/schedule/reference/#agencytxt

Raspunsuri: `200`, `403`, `429`, `500`

Body (application/json), array de:

| Camp | Tip | Obligatoriu | Descriere |
|---|---|---|---|
| `agency_id` | number | da | Identifica un brand de transport, adesea sinonim cu o agentie de transport. In unele cazuri, de exemplu cand o singura agentie opereaza mai multe servicii separate, agentiile si brandurile sunt distincte. Acest document foloseste termenul "agentie" in loc de "brand". Un set de date poate contine date de la mai multe agentii. |
| `agency_name` | string | da | Numele complet al agentiei de transport. |
| `agency_timezone` | string | da | Fusul orar in care se afla agentia de transport. Daca in setul de date sunt specificate mai multe agentii, fiecare trebuie sa aiba acelasi `agency_timezone`. |
| `agency_lang` | string sau null | nu | Limba principala folosita de agentia de transport. |
| `agency_url` | string sau null | nu | URL-ul agentiei de transport. |
| `agency_urls` | array[string] sau null | nu | URL-urile agentiei de transport. |

---

## 3. Vehicles

`GET https://api.tranzy.ai/v1/opendata/vehicles`

Pozitia vehiculului este folosita pentru a furniza informatii generate automat despre locatia unui vehicul, de exemplu de la un dispozitiv GPS de la bord. Trebuie furnizata o singura pozitie pentru fiecare vehicul care o poate transmite.
Mai multe detalii: https://gtfs.org/realtime/feed-entities/vehicle-positions/

Headere:

| Header | Tip | Descriere |
|---|---|---|
| `X-Agency-Id` | string | ID-ul agentiei de la care se iau datele. Lista agentiilor se vede la `/agency`. |

Raspunsuri: `200`, `403`, `429`, `500`

Body (application/json), array de:

| Camp | Tip | Obligatoriu | Descriere |
|---|---|---|---|
| `id` | string | da | Identificarea interna a vehiculului in sistem. Trebuie sa fie unica per vehicul si este folosita pentru urmarirea vehiculului pe parcursul traseului. Acest id nu trebuie afisat utilizatorului final; pentru asta se foloseste campul `label`. Exemplu: `1` |
| `label` | string | da | Eticheta vehiculului. Exemplu: `1` |
| `latitude` | number | da | Latitudine. Exemplu: `47.0271633` |
| `longitude` | number | da | Longitudine. Exemplu: `28.8871366` |
| `timestamp` | string (date-time) | da | Data si ora ultimei actualizari a pozitiei vehiculului. Exemplu: `2023-03-06 18:48:45` |
| `vehicle_type` | number | da | `0` - Tramvai, Streetcar, Light rail; `1` - Metrou; `2` - Tren; `3` - Autobuz; `4` - Feribot; `5` - Cable tram; `6` - Aerial lift; `7` - Funicular; `11` - Troleibuz; `12` - Monorail |
| `bike_accessible` | string | da | `UNKNOWN` - vehiculul nu are nicio valoare de accesibilitate; `BIKE_INACCESSIBLE` - vehiculul este accesibil pentru biciclete; `BIKE_ACCESSIBLE` - vehiculul nu este accesibil pentru biciclete. Valori permise: `BIKE_INACCESSIBLE`, `BIKE_ACCESSIBLE`, `UNKNOWN` |
| `wheelchair_accessible` | string | da | `UNKNOWN` - vehiculul nu are nicio valoare de accesibilitate; `WHEELCHAIR_ACCESSIBLE` - vehiculul este accesibil pentru scaune cu rotile; `WHEELCHAIR_INACCESSIBLE` - vehiculul nu este accesibil pentru scaune cu rotile. Valori permise: `NO_VALUE`, `UNKNOWN`, `WHEELCHAIR_ACCESSIBLE`, `WHEELCHAIR_INACCESSIBLE` |
| `speed` | number | da | (fara descriere in specificatie) |
| `route_id` | number | da | (fara descriere in specificatie) |
| `trip_id` | string | nu | (fara descriere in specificatie) |

Nota: in specificatia originala descrierile pentru `BIKE_INACCESSIBLE` si `BIKE_ACCESSIBLE` par inversate. Sunt pastrate exact cum au fost date.

---

## 4. Routes

`GET https://api.tranzy.ai/v1/opendata/routes`

Rutele sunt esentiale pentru operarea transportului pe trasee fixe, deoarece descriu aria geografica a unei retele de transport. In GTFS, definirea rutelor este primul pas in descrierea operarii unei agentii de transport.
Mai multe detalii: https://gtfs.org/schedule/examples/routes-stops-trips/#routes

Headere:

| Header | Tip | Descriere |
|---|---|---|
| `X-Agency-Id` | string | ID-ul agentiei de la care se iau datele. Lista agentiilor se vede la `/agency`. |

Raspunsuri: `200`, `403`, `429`, `500`

Body (application/json), array de:

| Camp | Tip | Obligatoriu | Descriere |
|---|---|---|---|
| `route_id` | number | da | Identificator unic pentru ruta. |
| `agency_id` | number | da | Agentia pentru ruta specificata. |
| `route_short_name` | string | da | Numele scurt al rutei. Adesea un identificator scurt si abstract (de exemplu "32", "100X", "Green") pe care calatorii il folosesc pentru a identifica o ruta. |
| `route_long_name` | string | da | Numele complet al rutei. Acest nume este in general mai descriptiv decat `route_short_name` si include adesea destinatia sau o oprire a rutei. |
| `route_color` | string | da | Culoarea rutei, care corespunde materialelor publice. |
| `route_type` | number | da | Indica tipul de transport folosit pe o ruta. Optiuni valide: `0` - Tramvai, Streetcar, Light rail; `1` - Metrou; `2` - Tren; `3` - Autobuz; `4` - Feribot; `5` - Cable tram; `6` - Aerial lift; `7` - Funicular; `11` - Troleibuz; `12` - Monorail |
| `route_desc` | string | da | Descrierea rutei, cu informatii utile si de calitate. Nu trebuie sa dubleze `route_short_name` sau `route_long_name`. |

---

## 5. Trips

`GET https://api.tranzy.ai/v1/opendata/trips`

Dupa descrierea rutelor agentiei, se pot descrie cursele (trips) deservite de fiecare ruta.
Mai multe detalii: https://gtfs.org/schedule/reference/#tripstxt

Headere:

| Header | Tip | Descriere |
|---|---|---|
| `X-Agency-Id` | string | ID-ul agentiei de la care se iau datele. Lista agentiilor se vede la `/agency`. |

Raspunsuri: `200`, `403`, `429`, `500`

Body (application/json), array de:

| Camp | Tip | Obligatoriu | Descriere |
|---|---|---|---|
| `direction_id` | number | da | Permite distingerea curselor aceleiasi rute care merg in directii diferite. `0` - curse pe directia "way"; `1` - curse pe directia "roundway" |
| `route_id` | number | da | (fara descriere in specificatie) |
| `trip_id` | string | da | (fara descriere in specificatie) |
| `trip_headsign` | string | da | (fara descriere in specificatie) |
| `block_id` | number | da | (fara descriere in specificatie) |
| `shape_id` | number | da | (fara descriere in specificatie) |
| `wheelchair_accessible` | number | da | (fara descriere in specificatie) |
| `bikes_allowed` | number | da | (fara descriere in specificatie) |

---

## 6. Shapes

`GET https://api.tranzy.ai/v1/opendata/shapes`

Shapes descriu traseul pe care il parcurge un vehicul de-a lungul unei rute si sunt definite in fisierul `shapes.txt`. Shapes sunt asociate cu Trips si constau intr-o secventa de puncte prin care trece vehiculul, in ordine. Shapes nu trebuie sa intersecteze exact locatia statiilor, dar toate statiile unei curse trebuie sa se afle la o distanta mica de shape-ul cursei, adica aproape de segmentele de dreapta care leaga punctele shape-ului.
Mai multe detalii: https://gtfs.org/schedule/reference/#shapestxt

Parametri de query:

| Parametru | Tip | Descriere |
|---|---|---|
| `shape_id` | string | ID-ul shape-ului. |

Headere:

| Header | Tip | Descriere |
|---|---|---|
| `X-Agency-Id` | string | ID-ul agentiei de la care se iau datele. Lista agentiilor se vede la `/agency`. |

Raspunsuri: `200`, `403`, `429`, `500`

Body (application/json), array de:

| Camp | Tip | Obligatoriu | Descriere |
|---|---|---|---|
| `shape_id` | string | da | Identificator unic pentru shape. |
| `shape_pt_lat` | number | da | Latitudinea unui punct al shape-ului. Fiecare inregistrare din shapes reprezinta un punct folosit pentru a defini shape-ul. |
| `shape_pt_lon` | number | da | Longitudinea unui punct al shape-ului. Fiecare inregistrare din shapes reprezinta un punct folosit pentru a defini shape-ul. |
| `shape_pt_sequence` | number | da | Secventa in care punctele shape-ului se leaga pentru a forma shape-ul. Valorile trebuie sa creasca de-a lungul cursei, dar nu trebuie sa fie consecutive. |
| `shape_dist_traveled` | number | nu | Distanta parcursa de-a lungul shape-ului, de la primul punct pana la punctul specificat in aceasta inregistrare. |

---

## 7. Stops

Statiile nu sunt luate din API-ul Tranzy. Folosim zip-ul GTFS oficial CTP:

```
https://external.gtfs.ro/cluj/CLUJ.zip
```

De aici luam si orele la statii (`stop_times.txt`), pentru ca Tranzy nu are endpoint pentru ele.

De verificat inainte de folosire:

- ca `calendar.txt` si `feed_info.txt` acopera orarul curent (site-ul CTP arata orarul nou valabil din 02.11.2026, iar snapshot-ul public din MobilityDatabase acoperea doar nov 2025 - iun 2026)
- ca valorile `route_id`, `stop_id` si `direction_id` corespund cu ce returneaza Tranzy `/vehicles`

---

## 8. Limite API

Neconfirmate din documentatia oficiala. Le tratam ca ipoteze de lucru **(de verificat in dashboard sau prin testare pana la raspunsuri 429)**:

| Limita | Valoare |
|---|---|
| Requesturi pe secunda | 5 |
| Requesturi pe zi | 5.000 |

Alte informatii:

- API-ul este BETA. Termeni si conditii: https://tranzy.dev/accounts/terms-and-conditions
- Harta live oficiala CTP se actualizeaza la 20 de secunde, deci polling mai rapid de 20 s nu aduce nimic.
- O cheie API este valabila pentru o singura agentie.

---

## 9. Strategia pentru calculul timpilor

### 9.1 Principii

1. **Orarul static vine din GTFS, pozitiile live vin din Tranzy.** Datele de orar nu consuma din cota API.
2. **Doar backendul vorbeste cu Tranzy.** Un singur poller, un singur cache. Aplicatia si utilizatorii citesc de la backendul nostru. Numarul de requesturi nu depinde de numarul de utilizatori.
3. **ETA bazat pe intarziere.** Nu estimam din viteza instantanee. Masuram cat de intarziat sau in avans este un vehicul fata de orar si aplicam asta pe orele programate ale urmatoarelor statii.

### 9.2 Folosirea API-ului si bugetul de requesturi

| Apel | Frecventa | Note |
|---|---|---|
| `GET /vehicles` | la fiecare 30 s in intervalul de serviciu (aprox. 05:00 - 24:00 ora locala) | principalul consumator de cota |
| `GET /agency` | o data, la setup | confirma agency id |
| `GET /routes`, `/trips` | o data la pornire si o data pe zi | mici, optionale daca folosim zip-ul GTFS |
| `GET /shapes` | o data pe zi sau deloc, folosind zip-ul GTFS | optional |

Buget zilnic:

- Vehicles: 19 h x 3600 s / 30 s = **2.280 requesturi/zi** (limita 5.000)
- Apeluri statice: aproximativ 5 pana la 10 pe zi
- Total: aproximativ **2.300 pe zi**, cam 46% din limita presupusa
- Ritm: 1 request la 30 s = 0,033 req/s (limita 5 req/s)
- Pentru date mai proaspete: polling la 20 s = 3.420 pe zi, tot sub limita. Nu avem de ce sa coboram sub 20 s.

Reguli de siguranta:

- Optional: facem polling doar cat timp a existat un client activ in ultimele minute.
- La `429`: backoff exponential si sarim ciclul. Nu reincercam niciodata in bucla rapida.
- La `403`: logam. Inseamna cheie gresita, agentie gresita sau cota depasita. Alertam, nu reincercam.
- La `500` sau timeout: servim ultimul snapshot din cache si il marcam ca invechit in raspuns.
- Tinem un contor zilnic de requesturi in backend si oprim polling-ul la un plafon (de exemplu 4.500).

### 9.3 Pipeline de date statice (GTFS)

1. Descarcam `CLUJ.zip` si il incarcam intr-o baza de date locala (SQLite sau Postgres).
2. Indexam `stop_times` dupa `stop_id` si dupa `trip_id`.
3. Stabilim ce curse ruleaza azi din `calendar.txt` plus `calendar_dates.txt`.
4. Parsam orele ca secunde de la inceputul zilei de serviciu. GTFS poate avea ore de tip `25:10:00` pentru cursele de dupa miezul noptii.
5. Re-descarcam feed-ul periodic (verificare zilnica, inlocuim daca s-a schimbat). Un feed invechit da ETA gresit.

### 9.4 Potrivirea unui vehicul live cu o cursa programata

`trip_id` din Tranzy este de forma `<route>_<directie>`, deci cursa programata exacta trebuie dedusa.

Pentru fiecare vehicul utilizabil:

1. Luam `route_id` si directia din `trip_id` (verificam ca sufixul este egal cu `direction_id` din GTFS).
2. Luam cursele GTFS ale rutei si directiei respective, active azi.
3. Proiectam pozitia vehiculului pe shape-ul cursei, ca sa aflam distanta pe traseu si statiile dinainte si de dupa.
4. Pentru fiecare cursa candidata, interpolam ora programata la pozitia vehiculului.
5. Alegem candidata a carei ora programata este cea mai apropiata de ora locala curenta.
6. `intarziere = acum - ora_programata` (pozitiv = intarziat).
7. Pastram atribuirea per `id` de vehicul intre polling-uri. Nu recalculam de la zero de fiecare data. Asa evitam sa alegem cursa gresita cand intarzierea depaseste jumatate din frecventa.

Vehicule cu `trip_id = null` dar `route_id` valid: deducem directia comparand ultimele doua pozitii cu cele doua shape-uri ale rutei. Daca e ambiguu, ignoram vehiculul.

### 9.5 ETA la o statie

```
eta(statie) = ora_programata(cursa_potrivita, statie) + intarziere
```

- Daca vehiculul a trecut deja de statie pe cursa lui, il ignoram pentru acea statie.
- Optional, reducem intarzierea spre zero pentru statiile departe in fata.
- Daca niciun vehicul live nu e potrivit cu o cursa programata, aratam doar ora programata si o marcam "orar, nu live".

### 9.6 Functia din aplicatie: ajungi la o cladire UBB la o anumita ora

Input de la utilizator:

- cladirea UBB de destinatie (si ora de inceput a cursului)
- locatia curenta sau o origine aleasa

Output:

- ce linie sa ia
- ora la care trebuie sa fie in statie
- ora estimata de sosire la statia de destinatie
- timpul de mers pe jos la ambele capete

Algoritm (MVP, doar linii directe, fara transferuri):

1. **Statii de destinatie:** gasim statiile aflate la distanta de mers pe jos de coordonatele cladirii UBB. Pastram cateva dintre cele mai apropiate.
2. **Statii de origine:** gasim statiile aflate la distanta de mers pe jos de originea utilizatorului.
3. **Curse candidate:** din GTFS, gasim cursele care trec printr-o statie de origine si, mai tarziu (`stop_sequence` mai mare), printr-o statie de destinatie, pe aceeasi cursa.
4. **Termen limita la statia de destinatie:**
   `ora_curs - timp_mers(statie_destinatie -> cladire) - buffer_siguranta`
   Buffer de siguranta implicit: 5 minute.
5. **Alegem cursa:** pastram cursele a caror sosire programata la statia de destinatie plus intarzierea live este inainte de termenul limita. Alegem ultima care inca ajunge la timp si returnam si cursa dinainte ca varianta de rezerva.
6. **Ora la care trebuie sa fii in statia de origine:**
   `plecare_la_statia_de_origine (programata + intarziere live) - 1 pana la 2 min buffer`
7. **Ora la care pleci de acasa:**
   `ora_in_statie - timp_mers(origine -> statia_de_origine)`
8. **Afisare:** numarul liniei, headsign, "fii in statia X la HH:MM", ETA live al autobuzului, sosirea estimata si daca datele sunt live sau doar din orar.

Timp de mers pe jos pentru MVP: distanta in linie dreapta x 1,3 / 1,2 m/s. Inlocuim ulterior cu un router pietonal real.

Imbunatatiri ulterioare (in afara spike-ului):

- transferuri (necesita un router de tip RAPTOR sau un motor extern precum OpenTripPlanner alimentat cu zip-ul GTFS, iar Tranzy folosit doar pentru vehiculele live)
- trasee pietonale reale
- salvarea intarzierilor istorice per linie, statie si ora, pentru ETA mai bun

### 9.7 Decizia pentru rutare

- Tranzy nu are endpoint de planificare de ruta. **Nu** folosim Tranzy pentru rutare.
- MVP: logica proprie peste GTFS pentru linii directe (sectiunea 9.6).
- Transferuri: decidem mai tarziu intre o implementare proprie RAPTOR si OpenTripPlanner.


---


## 10. Rezumat

| Subiect | Decizie |
|---|---|
| Sursa orar | Zip GTFS oficial CTP (`https://external.gtfs.ro/cluj/CLUJ.zip`) |
| Sursa live | Tranzy `GET /vehicles`, un apel pe ciclu pentru toata flota |
| Polling | La fiecare 30 s in intervalul de serviciu, aprox. 2.300 requesturi/zi |
| Cine apeleaza Tranzy | Doar backendul, clientii citesc din cache-ul nostru |
| Metoda ETA | Ora programata la statie + intarzierea curenta a vehiculului potrivit |
| Rutare | Logica proprie peste GTFS pentru linii directe, transferuri mai tarziu |
| Fallback | Doar ora programata, marcata ca nu este live |