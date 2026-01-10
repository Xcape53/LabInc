# Instrukcja Gry - LabInc: Chemical Tycoon

## 1. Uruchamianie Gry

### Metoda 1: Szybki Start (Windows)
W katalogu głównym gry uruchom skrypt:
`run.bat`

### Metoda 2: Uruchomienie z konsoli (Maven)
```bash
mvn clean package
java -jar target/labinc-game-1.0.0-jar-with-dependencies.jar
```

---

## 2. Podstawy Rozgrywki (Pierwsze 10 minut)

### Krok 1: Początek
Rozpoczynasz grę z jedną działającą **Kopalnią Węgla (Poziom 1)**.
*   Produkuje ona automatycznie Węgiel (C), German (Ge) i Siarkę (S).


### Krok 2: Sprzedaż
1.  Przejdź do zakładki **Rynek** (ikona dolara).
2.  Znajdź Węgiel na liście.
3.  Kliknij przycisk **"Sprzedaj"**, aby zamienić surowce na gotówkę.

### Krok 3: Pierwsza Inwestycja
1.  Gdy uzbierasz $2,500, wróć do zakładki **Wydobycie** (ikona młota).
2.  Kliknij przycisk **"UPGRADE"** przy Kopalni Węgla.
3.  Twoja kopalnia osiągnie Poziom 2 (Tier 2), podwajając produkcję.

### Krok 4: Automatyzacja
1.  Wróć na **Rynek**.
2.  Kliknij przycisk **"Auto-Sprzedaż"** przy Węglu.
3.  Ustaw próg ilości (np. 1000 kg).
4.  Od teraz gra będzie automatycznie sprzedawać nadmiar węgla, generując pasywny dochód.

---

## 3. Drzewo Technologiczne (Ścieżka Rozwoju)

Aby odblokować nowe budynki, musisz ulepszać poprzednie. Oto pełna ścieżka:

1.  **Kopalnia Węgla (KW)** - Dostępna od startu.
    *   *Cel:* Osiągnij Poziom 3.
2.  **Szyb Naftowy (SN)** - Wymaga KW Poz. 3.
    *   *Cel:* Osiągnij Poziom 3.
3.  **KDA (Destylacja Powietrza)** - Wymaga SN Poz. 3.
    *   *Cel:* Osiągnij Poziom 6.
4.  **Salina** - Wymaga KDA Poz. 6.
    *   *Cel:* Osiągnij Poziom 13.
5.  **Kopalnia Rudy Żelaza** - Wymaga Saliny Poz. 13.
    *   *Cel:* Osiągnij Poziom 4.
6.  **Kopalnia Boksytu** - Wymaga Rudy Żelaza Poz. 4.
    *   *Cel:* Osiągnij Poziom 2.

*(Dalsze kopalnie odblokowują się analogicznie, aż do Reaktora Jądrowego)*

---

## 4. Reakcje Chemiczne

Gdy zdobędziesz odpowiednie surowce (np. Wodór z Szybu Naftowego i Tlen z KDA), możesz rozpocząć produkcję związków chemicznych w zakładce **Fabryki** (ikona fabryki).

### Jak to działa?
1.  Każda reakcja wymaga surowców wejściowych (Substratów).
2.  Jeśli masz wystarczającą ilość substratów, reakcja zachodzi automatycznie w każdej sekundzie.
3.  Produkt trafia do magazynu i może być sprzedany znacznie drożej niż surowce.

### Przykład: Synteza Wody
*   **Wymaga:** 2 jednostki Wodoru (H) + 1 jednostka Tlenu (O).
*   **Produkuje:** 1 jednostkę Wody (H2O).
*   **Zysk:** Cena Wody jest wyższa niż suma cen Wodoru i Tlenu.

**Ważne:** Możesz ulepszać reakcje (zwiększać ich poziom), aby przyspieszyć proces przetwarzania.

---

## 5. Porady Strategiczne

1.  **Nie sprzedawaj wszystkiego:** Niektóre surowce (jak Wodór, Tlen, Siarka) są potrzebne do reakcji chemicznych. Jeśli sprzedasz je wszystkie automatycznie, Twoje fabryki chemiczne staną.
2.  **Zarządzaj energią:** Ulepszanie fabryk staje się coraz droższe. Czasami lepiej zainwestować w nową, bardziej dochodową kopalnię niż w nieskończoność ulepszać stary szyb węglowy.
3.  **Śledź Osiągnięcia:** W zakładce **Osiągnięcia** znajdziesz cele, które warto realizować dla satysfakcji i (w przyszłych wersjach) bonusów.
4.  **Tablica Mendelejewa:** Użyj panelu **Tablica**, aby sprawdzić, które pierwiastki już odkryłeś. To Twoja główna miara postępu w grze.
