# Student Assist

Scopul acestei aplicatii este de a ajuta studentii pe parcursul studiilor universitare prin a le oferi cai mai usoare de a interactiona cu noul oras in care invata, fara sa se imparta in 100 de locuri

---

## Functionalitatile aplicatiei

- **orar in functie de facultate**
- **harta transport in comun**
- **afisarea punctelor de interes in functie de facultate**
- **locatii de iesit in oras recomandate de alti studenti**
- **(optional) sistem de a cunoaste alti studenti in functie de facultatea la care studiati si interese**

---

## Functionalitati detaliate

### 1. Orar in functie de facultate

Prin aceasta functionalitate ar trebui sa putem sa preluam datele despre orar de la mai multe facultati, sa le formatam si sa afisam orele intr-un format mai friendly si usor de inteles pentru oricine. Vom mai introduce si optiunea ca studentii sa-si poata introduce ei un orar custom si facultatile sa poata incarca ele orarul in platforma, iar pentru studenti sa fie actualizat imediat

> **Limitare:** pentru acest proiect ne vom limita la extragerea orarului de pe site-ul UBB FMI

### 2. Harta transport in comun

Afisarea unei harti pentru transportul in comun pentru a ajuta studenti sa ajunga in diferite parti ale orasului cu usurinta

> **Limitare:** pentru inceput vom acoperi doar orasul Cluj, folosind API-ul oferit de Tranzy

### 3. Afisarea punctelor de interes in functie de facultate

Ar trebui ca pentru facultatea aleasa de student la orar sa fie afisate pe harta locurile in care studentul isi desfasoara orele. Ne-am dori ca la final sa calculam si timpul exact pana la urmatorul curs, iar aplicatia sa calculeze automat si sa informeze utilizatorul despre ora la care sa se porneasca pentru a ajunge la timp si rutele pe care ar trebui s-o ia

### 4. Locatii de iesit in oras recomandate de alti studenti

Sistem prin care studentii pot adauga recomandari de iesit in oras pentru a le fi mai usor celorlalti sa descopere activitati noi

---

## Plan de dezvoltare

### Demo 1

- orar pentru FMI
- harta transport in comun

### Demo 2

- afisarea punctelor de interes
- locatii de iesit
- (optional) sistem de a cunoaste alti studenti

### Obiectivul demo-urilor

La finalul primului demo ne dorim sa avem o aplicatie care ajuta studentul in a se orienta mai usor prin oras si cu facultatea, in timp ce la in cel de-al doilea am vrea sa ne axam pe partea de interactiune si socializare.

---

## Stack aplicatie

| Componenta   | Tehnologie                                     |
| :----------- | :--------------------------------------------- |
| **backend**  | Spring Boot (Java 17)                          |
| **frontend** | Vue 3 + Vite + TypeScript + Pinia + Vue Router |
| **db**       | SQLite                                         |
