# Wireframes si Design System (SCRUM-22)

> **Task Jira:** SCRUM-22 (Wireframes + design system mic)  
> **Modul:** Frontend (Vue 3 + TypeScript)  
> **Status:** Pregatit pentru review  

---

## 1. Scop

Un sistem vizual mic si coerent pentru **Student Assist**, plus wireframe-uri interactive pentru ecranele din **Demo 1**:

1. **Orar** (`/schedule`)
2. **Transport** (`/transit`)
3. **Bara de navigatie plutitoare** (jos, pe mobil)
4. **Catalogul de componente** (`/design-system`, vizibil doar in modul dev)

---

## 2. Dependente noi

| Pachet | Rol |
|---|---|
| `lucide-vue-next` | Iconite SVG (inlocuiesc emoji-urile, arata la fel pe orice sistem) |
| `@fontsource-variable/inter` | Fontul Inter, inclus local (merge si offline, important pentru PWA) |

Instalare: `npm install lucide-vue-next @fontsource-variable/inter` (in folderul `frontend`).

---

## 3. Design Tokens (`frontend/src/assets/styles/theme.css`)

### 3.1. Culori de baza
* **Primary:** `#0f172a` (negru-grafit)
* **Fundal:** `#f8fafc`
* **Suprafata (carduri):** `#ffffff`
* **Selectie (chip / buton activ):** fundal `rgba(15, 23, 42, 0.09)`, contur `rgba(15, 23, 42, 0.2)`

### 3.2. Culori pentru orar
Toate perechile text/fundal respecta contrastul minim WCAG AA (4.5:1) pentru text mic:

| Tip | Fundal | Text | Contrast |
|---|---|---|---|
| Laborator | `#fff1f2` | `#be123c` | 5.7:1 |
| Seminar | `#fffbeb` | `#b45309` | 4.9:1 |
| Curs | `#ecfdf5` | `#047857` | 5.2:1 |
| Neutru (frecventa) | `#f1f5f9` | `#475569` | 6.9:1 |

### 3.3. Culori pentru transport
* **Linie autobuz:** fundal `#f5f3ff`, text `#6d28d9`
* **Live GPS:** text `#047857`, bulina `#10b981` (cu puls)
* **Orar (fara GPS):** neutru

### 3.4. Tipografie
* **Font:** Inter (cu fonturile de sistem ca rezerva)
* **Titlu pagina:** 21-22px (`1.35rem`)
* **Titlu sectiune:** ~16px (`0.98rem`)
* **Text card:** 14px (`0.88rem`)
* **Detalii:** 12-13px (`0.75rem - 0.8rem`)
* **Ore si timpi:** cifre de latime egala (`tabular-nums`), ca sa se alinieze

---

## 4. Componente (`frontend/src/components/ui/`)

| Componenta | Cand se foloseste |
|---|---|
| `AppButton` | Actiuni (`primary` o singura data pe ecran, ex: "Salveaza grupa"; `secondary`; `outline`) |
| `AppChip` | Filtre si selectie (zile, linii de autobuz) |
| `AppSegmented` | Comutator compact intre 2-4 optiuni (Toate / S1 / S2), cu `v-model` |
| `AppBadge` | Etichete: `course`, `seminar`, `lab`, `bus`, `live`, `neutral`; prop `dot` pentru bulina |
| `AppCard` | Container; `hoverable` si `accent` (`course`, `seminar`, `lab`, `bus`, `primary`) pentru dunga din stanga |
| `BottomNav` | Navigatie plutitoare; tab-ul Design apare doar in `npm run dev` |

Regula: ecranele nu definesc stiluri proprii pentru butoane sau filtre, ci folosesc componentele de mai sus.

---

## 5. Ecrane

### 5.1. Orar (`/schedule`)
* Comutator saptamana (`AppSegmented`) si zile (`AppChip`), ambele functionale (filtreaza lista)
* Carduri cu dunga colorata, tipul orei, frecventa, sala, cladire, cadru didactic
* Stare goala cand nu exista ore in ziua selectata
* Date: subset real pentru grupa 231 din `backend/src/main/resources/seed/schedule-seed.json`

### 5.2. Transport (`/transit`)
* Filtre de linii (`AppChip`), functionale
* Zona de harta (placeholder pentru Leaflet) cu vehicule si statii
* Card cu ruta recomandata spre un curs real din seed (Laborator, L320, Campus FSEGA)
* Lista de sosiri cu ETA si status Live / Orar
* Liniile si timpii sunt **ilustrativi**; datele reale vor veni din Tranzy + GTFS (vezi `docs/tranzy-spike.md`)

### 5.3. Design System (`/design-system`)
* Catalog cu toate componentele si culorile, pentru echipa (doar in modul dev)

---

## 6. Accesibilitate si responsive
* Contrast WCAG AA pe toate etichetele colorate
* Zone de atingere de minimum 38-48px pe butoane, chip-uri si tab-uri
* Focus vizibil la navigarea din tastatura
* Animatiile se opresc daca utilizatorul are activat "reduce motion"
* Randurile din carduri trec pe linia urmatoare pe ecrane inguste (360px), iar textele lungi se taie cu "..."
* Bara de jos tine cont de zona de siguranta de pe iPhone (`safe-area-inset-bottom`)
