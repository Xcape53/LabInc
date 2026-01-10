# Dokumentacja Techniczna - LabInc: Chemical Tycoon

**Wersja:** 2.0
**Autor:** Zespół Deweloperski
**Data:** Grudzień 2025
**Technologia:** Java Desktop (Java 8+, Swing, Maven)

---

## 1. Opis Projektu

### 1.1 Cel gry (Serious Game)
**LabInc: Chemical Tycoon** to gra edukacyjna typu *incremental/idle game*, której celem jest nauczanie chemii nieorganicznej i ekonomii przemysłowej poprzez interaktywną rozgrywkę. Gracz wciela się w rolę zarządcy imperium chemicznego, poznając:

*   **Układ Okresowy:** Właściwości, symbole i grupy 118 pierwiastków.
*   **Procesy Przemysłowe:** Od prostego wydobycia węgla, przez destylację frakcyjną powietrza, aż po zaawansowaną syntezę jądrową.
*   **Syntezę Chemiczną:** Tworzenie związków chemicznych (np. kwas siarkowy, amoniak) z prostszych substratów.
*   **Ekonomię:** Zarządzanie budżetem, inwestowanie w infrastrukturę i optymalizację łańcuchów dostaw.

### 1.2 Mechanika Podstawowa
Rozgrywka opiera się na pętli sprzężenia zwrotnego:
1.  **Produkcja:** Fabryki i kopalnie generują surowce w czasie rzeczywistym.
2.  **Sprzedaż:** Gracz sprzedaje surowce na rynku, zdobywając fundusze.
3.  **Inwestycja:** Fundusze są przeznaczane na ulepszanie istniejących fabryk (zwiększenie produkcji) lub zakup nowych technologii.
4.  **Odblokowanie:** Osiągnięcie odpowiedniego poziomu rozwoju technologicznego odblokowuje dostęp do nowych, rzadszych i droższych pierwiastków.

---

## 2. Szczegółowa Lista Fabryk i Wymagań

Drzewo technologiczne gry składa się z 16 głównych obiektów przemysłowych. Każdy kolejny obiekt wymaga osiągnięcia określonego poziomu (Tier) poprzedniego obiektu.

| Lp. | Skrót | Nazwa Pełna | Wymaganie Odblokowania | Główne Produkty |
|:---:|:-----:|:------------|:-----------------------|:----------------|
| 1 | KW | Kopalnia Węgla | Brak (Startowa) | Węgiel (C), German (Ge), Siarka (S) |
| 2 | SN | Szyb Naftowy | Kopalnia Węgla (Poziom 3) | Wodór (H), Hel (He) |
| 3 | KDA | KDA (Destylacja Powietrza) | Szyb Naftowy (Poziom 3) | Azot (N), Tlen (O), Argon (Ar), Neon (Ne) |
| 4 | SAL | Salina | KDA (Poziom 6) | Sól (Na, Cl), Lit (Li), Magnez (Mg) |
| 5 | KRZ | Kopalnia Rudy Żelaza | Salina (Poziom 13) | Żelazo (Fe), Krzem (Si), Mangan (Mn) |
| 6 | KB | Kopalnia Boksytu | Kopalnia Rudy Żelaza (Poziom 4) | Glin (Al), Gal (Ga) |
| 7 | KRM | Kopalnia Rudy Miedzi | Kopalnia Boksytu (Poziom 2) | Miedź (Cu), Srebro (Ag), Złoto (Au) |
| 8 | KRCO | Kop. Cynku i Ołowiu | Kopalnia Rudy Miedzi (Poziom 7) | Cynk (Zn), Ołów (Pb), Kadm (Cd) |
| 9 | KRN | Kopalnia Rudy Niklu | Kop. Cynku i Ołowiu (Poziom 8) | Nikiel (Ni), Kobalt (Co) |
| 10 | KMW | Kop. Magnetytu Wanadowego | Kopalnia Rudy Niklu (Poziom 2) | Wanad (V) |
| 11 | KF | Kopalnia Fluorytu | Kop. Magnetytu Wanad. (Poziom 2) | Fluor (F) |
| 12 | KRCW | Kop. Cyny i Wolframu | Kopalnia Fluorytu (Poziom 1) | Cyna (Sn), Wolfram (W) |
| 13 | KPM | Kop. Piasków Mineralnych | Kop. Cyny i Wolframu (Poziom 6) | Tytan (Ti), Cyrkon (Zr) |
| 14 | KP | Kopalnia Platynowców | Kop. Piasków Mineral. (Poziom 3) | Platyna (Pt), Pallad (Pd), Iryd (Ir) |
| 15 | KZR | Kopalnia Ziem Rzadkich | Kopalnia Platynowców (Poziom 7) | Neodym (Nd), Lantan (La), Cer (Ce) |
| 16 | RJ | Reaktor Jądrowy | Kopalnia Ziem Rzadkich (Poziom 17)| Pluton (Pu), Uran (U), Pierwiastki syntetyczne |

---

## 3. System Reakcji Chemicznych

Gra umożliwia przeprowadzanie reakcji chemicznych w specjalnym panelu "Fabryki". Reakcje te pozwalają na przetworzenie podstawowych pierwiastków w bardziej wartościowe związki chemiczne.

**Dostępne Reakcje:**

1.  **Synteza Wody (H2O):** `2H + O -> H2O`
    *   Podstawowa reakcja, dostępna od początku po zdobyciu Wodoru i Tlenu.
2.  **Spalanie Węgla (CO2):** `C + 2O -> CO2`
    *   Wykorzystuje nadmiar węgla i tlenu.
3.  **Synteza Metanu (CH4):** `C + 4H -> CH4`
    *   Wymaga dużych ilości wodoru.
4.  **Proces Habera (NH3):** `N + 3H -> NH3`
    *   Produkcja amoniaku, kluczowego dla przemysłu nawozowego.
5.  **Synteza Kwasu Siarkowego (H2SO4):** `S + 2H + 4O -> H2SO4`
    *   Skomplikowana reakcja wymagająca trzech substratów. Bardzo dochodowa.
6.  **Synteza Soli Kuchennej (NaCl):** `Na + Cl -> NaCl`
    *   Wykorzystuje produkty z Saliny.

---

## 4. Struktura Projektu

```
LabInc-Release/
├── src/main/java/com/labinc/
│   ├── LabIncGame.java          # Główna klasa uruchomieniowa
│   ├── model/                   # Logika biznesowa (8 klas)
│   │   ├── GameState.java       # Stan gry (Singleton)
│   │   ├── Factory.java         # Definicja fabryki
│   │   ├── Resource.java        # Definicja zasobu
│   │   ├── Recipe.java          # Definicja reakcji
│   │   ├── SaveGame.java        # Klasa do serializacji
│   │   ├── Achievement.java     # Definicja osiągnięcia
│   │   ├── AchievementManager.java # Zarządzanie osiągnięciami
│   │   └── GameSettings.java    # Ustawienia globalne
│   ├── gui/                     # Interfejs użytkownika (17 klas)
│   │   ├── MainMenuFrame.java   # Ekran startowy
│   │   ├── SidebarPanel.java    # Nawigacja boczna
│   │   ├── MiningPanel.java     # Panel wydobycia
│   │   ├── MarketPanel.java     # Panel rynku
│   │   ├── FactoryPanel.java    # Panel reakcji chemicznych
│   │   ├── PeriodicTablePanel.java # Tablica Mendelejewa
│   │   └── ... (pozostałe komponenty widoku)
│   └── util/                    # Narzędzia (4 klasy)
│       ├── SoundManager.java    # System dźwiękowy
│       ├── IconLoader.java      # Ładowanie grafik
│       ├── WindowsThemeUtil.java # Integracja z systemem Windows
│       └── ElectronConfigCalculator.java # Fizyka atomowa
```

---

## 5. Algorytmy i Dane

### 5.1 Skalowanie Kosztów
Gra wykorzystuje model wzrostu wykładniczego dla kosztów ulepszeń, co zapewnia balans rozgrywki w długim okresie (endgame). Wraz z każdym poziomem fabryki, koszt kolejnego poziomu rośnie o zdefiniowany procent (zazwyczaj 30-45%).

### 5.2 System Zapisu
Stan gry jest automatycznie serializowany do pliku binarnego `labinc_save.dat`. Zapis obejmuje:
*   Wszystkie parametry fabryk (poziomy, mnożniki).
*   Ilości wszystkich 118 pierwiastków i związków.
*   Stan konta gracza.
*   Odblokowane osiągnięcia.
*   Konfiguracje auto-sprzedaży (progi).

