# Architektura Kodu - LabInc

## Wzorce Projektowe

### 1. Singleton (GameState)
Stan gry jest zarządzany przez jedną centralną instancję klasy `GameState`. Przechowuje ona wszystkie kluczowe dane, takie jak ilość pieniędzy, zasoby, fabryki oraz odblokowane reakcje chemiczne. Dostęp do tej instancji jest przekazywany do paneli GUI, co zapewnia spójność danych w całej aplikacji.

### 2. Observer (GameEventListener)
Zastosowano wzorzec Obserwator do komunikacji między logiką gry a interfejsem użytkownika. Interfejs `GameEventListener` pozwala panelom nasłuchiwać zmian w modelu.

```java
public interface GameEventListener {
    void onMoneyChanged(double newMoney);
    void onResourceChanged(String resourceSymbol, double amount);
    void onFactoryUpgraded(String factoryName, int newTier);
    void onRecipeUpgraded(String recipeId, int newLevel);
}
```

### 3. Strategy (unlockCondition w Factory)
Każda instancja klasy `Factory` posiada własną strategię określającą warunki odblokowania, zaimplementowaną jako wyrażenie lambda (`Function<Map<String, Factory>, Boolean>`). Pozwala to na elastyczne definiowanie zależności między fabrykami (np. "Fabryka B wymaga 3 poziomu Fabryki A").

---

## Pakiety i Struktura Kodu

Projekt podzielony jest na trzy główne pakiety funkcjonalne wewnątrz `com.labinc`:

### 1. com.labinc.model
Zawiera logikę biznesową gry. Klasy w tym pakiecie nie zależą od bibliotek graficznych (Swing).

| Klasa | Odpowiedzialność |
|-------|------------------|
| `GameState` | Centralny magazyn danych i koordynator logiki (produkcja, sprzedaż). |
| `Factory` | Reprezentacja kopalni/fabryki, obliczanie kosztów i produkcji. |
| `Resource` | Dane o pierwiastku lub związku chemicznym (cena, ilość). |
| `Recipe` | Definicja reakcji chemicznej (wejścia -> wyjścia). |
| `AchievementManager` | Logika sprawdzania i odblokowywania osiągnięć. |
| `SaveGame` | Struktura danych serializowana do pliku zapisu. |

### 2. com.labinc.gui
Zawiera warstwę prezentacji opartą na Java Swing.

| Klasa | Odpowiedzialność |
|-------|------------------|
| `LabIncGame` | Punkt wejściowy, zarządzanie oknem głównym i pętla gry. |
| `MiningPanel` | Widok listy fabryk i kopalń. |
| `FactoryPanel` | Widok zarządzania reakcjami chemicznymi. |
| `MarketPanel` | Widok sprzedaży surowców i konfiguracji auto-sprzedaży. |
| `PeriodicTablePanel` | Wizualizacja układu okresowego pierwiastków. |
| `SidebarPanel` | Panel boczny z nawigacją i podsumowaniem surowców. |

### 3. com.labinc.util
Klasy pomocnicze i narzędziowe.

| Klasa | Odpowiedzialność |
|-------|------------------|
| `SoundManager` | Obsługa efektów dźwiękowych i muzyki w tle. |
| `IconLoader` | Ładowanie i buforowanie zasobów graficznych. |
| `ElectronConfigCalculator` | Obliczanie konfiguracji elektronowej dla tablicy Mendelejewa. |

---

## Szczegółowy Przepływ Danych

### Cykl Aktualizacji (Game Loop)
Główna pętla gry zaimplementowana jest w `LabIncGame` przy użyciu `javax.swing.Timer` z interwałem 100ms.

1.  **Timer Tick (co 100ms):**
    *   Wywołanie `gameState.produceResources(deltaTime)`.
2.  **Produkcja (Model):**
    *   `GameState` iteruje po wszystkich fabrykach.
    *   Pobiera aktualną wydajność z `Factory.getCurrentProduction()`.
    *   Aktualizuje ilości surowców w mapie `resources`.
3.  **Reakcje Chemiczne (Model):**
    *   Przetwarzanie aktywnych receptur (`processReactions`).
    *   Konsumpcja substratów i produkcja produktów.
4.  **Auto-sprzedaż (Model):**
    *   Uruchomienie algorytmu `processAutoSell`.
    *   Automatyczna sprzedaż surowców po przekroczeniu progu.
5.  **Powiadomienia (Observer):**
    *   `GameState` zbiera zmiany i wywołuje metody listenerów (`onResourceChanged`, `onMoneyChanged`).
6.  **Odświeżenie Widoku (View):**
    *   Panele GUI (np. `MiningPanel`) otrzymują sygnały i aktualizują etykiety tekstowe oraz paski postępu.

---

## Kluczowe Algorytmy

### Obliczanie Kosztu Ulepszenia Fabryki
Koszt rośnie wykładniczo wraz z poziomem fabryki.
Wzór:
`Koszt = KosztBazowy * (WspółczynnikWzrostu ^ (ObecnyPoziom - PoziomBazowy))`

### Algorytm Auto-sprzedaży (Smart Autosell)
System zapobiega sprzedaży małych ilości surowców, które zaśmiecałyby powiadomienia i dźwięk.
Sprzedaż następuje, gdy wartość posiadanych zasobów przekroczy wartość `effectiveThreshold`, która jest większa z dwóch wartości:
1.  Progu ustawionego przez użytkownika.
2.  Dynamicznego progu wynoszącego 0.1% aktualnych pieniędzy gracza (minimum $100).

---

## Decyzje Techniczne

1.  **Format Zapisu Danych:** Użyto standardowej serializacji Javy (`java.io.Serializable`). Obiekt `SaveGame` jest kontenerem na stan `GameState` i `AchievementManager`, co ułatwia zapis i odczyt całego stanu gry jednym strumieniem.
2.  **Reprezentacja Liczb:** Użyto typu `double` dla wszystkich wartości numerycznych (pieniądze, ilości). Pozwala to na obsługę bardzo dużych liczb wymaganych w późniejszej fazie gry (rząd wielkości 10^35).
3.  **Identyfikacja Obiektów:** Wszystkie zasoby i fabryki są identyfikowane przez unikalne klucze typu `String` (np. "Au", "KW"), co ułatwia zarządzanie nimi w `HashMap`.


