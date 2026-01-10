# LabInc: Chemical Tycoon

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

Gra symulacyjna typu incremental/idle, w której zarządzasz kopalniami i fabrykami chemicznymi. Rozpocznij od prostej kopalni węgla i rozwijaj się przez 16 fabryk, aż do reaktora jądrowego produkującego pierwiastki syntetyczne.

---

## Spis treści

* [O projekcie](#o-projekcie)
* [Funkcjonalności](#funkcjonalności)
* [Technologie](#technologie)
* [Uruchomienie](#uruchomienie)
* [Credits](#credits)
* [Kontakt](#kontakt)

---

## O projekcie

**LabInc: Chemical Tycoon** to gra edukacyjno-strategiczna łącząca mechanikę incremental games z nauką chemii. Gracz zarządza systemem produkcji 118 pierwiastków chemicznych – od podstawowego węgla po oganeson.

Rozpoczynając od Kopalni Węgla, gracz automatycznie produkuje surowce do sprzedaży. Za zgromadzony kapitał kupuje ulepszenia fabryk, zwiększając mnożniki produkcji. Odpowiedni poziom odblokowuje kolejne fabryki – od Szybu Naftowego, przez Kriogeniczną Destylację Powietrza, aż po Reaktor Jądrowy.

**Kluczowe cechy:**
- 16 fabryk z unikalną produkcją
- 118 pierwiastków chemicznych
- Wykładniczy wzrost od $0.01 do $10^35
- 18 osiągnięć do odblokowania
- System zapisu i wczytywania gry

---

## Funkcjonalności

### System produkcji
- Automatyczne wydobycie surowców z kopalń
- Przetwarzanie materiałów w fabrykach chemicznych
- Progresywne odblokowywanie nowych obiektów

### Interfejs
- Nowoczesny ciemny motyw GUI
- Panele: Wydobycie, Fabryki, Rynek, Ulepszenia, Osiągnięcia
- Animowane efekty i dźwięki
- Automatyczny i ręczny zapis gry

### Balans gry
- Starannie wyważone koszty i mnożniki
- Długoterminowa progresja gracza

---

## Zrzuty ekranu

### Interfejs gry
![wydobycie_podglad](docs/wydobycie_podglad.png)

---

## Technologie

### Java Stack
- **Java 8+** – język programowania
- **Swing** – framework GUI
- **Maven 3.6+** – build system
- **Java Serialization** – system zapisu (.dat)

### Biblioteki
- **JNA** – integracja z systemem operacyjnym
- **Batik** – obsługa plików SVG
- **MP3SPI/JLayer** – odtwarzanie dźwięków

---

## Uruchomienie

### Wymagania
- Java 8 lub nowsza
- Maven 3.6+

### Kompilacja i uruchomienie
```bash
# Kompilacja
mvn clean package

# Uruchomienie
java -jar target/labinc-game-1.0.0-jar-with-dependencies.jar
```

Lub użyj skryptu:
```bash
compile-and-run.bat
```

---

## Credits

### Kod
- **Piotr Jeleniewicz** – autor projektu
- Wsparcie AI: **Claude Opus 4.5, Gemini 3 Pro**

### Dźwięk
- Muzyka: **Suno AI**
- Efekty dźwiękowe: **Ja**, **Soundly**

---

## Kontakt

**Autor:** Piotr Jeleniewicz

- Email: s197840@student.pg.edu.pl
- GitHub: [@Xcape53](https://github.com/Xcape53)

**Link do projektu:** [https://github.com/Xcape53/LabInc](https://github.com/Xcape53/LabInc)

<div align="center">

Made by Xcape

</div>
