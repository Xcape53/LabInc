# Ikony do gry LabInc

Umieść ikony **SVG** lub **PNG** w tym folderze. System automatycznie wykryje format.

## Lista wymaganych ikon:

### Sidebar (lewy panel nawigacji):
| Nazwa pliku | Rozmiar | Opis |
|-------------|---------|------|
| `mining.svg` / `.png` | 24x24 | Kilof/wydobycie |
| `factory.svg` / `.png` | 24x24 | Fabryka/budynek |
| `market.svg` / `.png` | 24x24 | Koszyk/wykres |
| `achievements.svg` / `.png` | 24x24 | Puchar/trofeum |
| `settings.svg` / `.png` | 20x20 | Koło zębate |
| `save.svg` / `.png` | 20x20 | Dyskietka |
| `help.svg` / `.png` | 20x20 | Znak zapytania |
| `logo.svg` / `.png` | 48x48 | Logo gry (kolba) |

### Top Panel (górny pasek statystyk):
| Nazwa pliku | Rozmiar | Opis |
|-------------|---------|------|
| `money.svg` / `.png` | 18x18 | Moneta/dolary |
| `income.svg` / `.png` | 18x18 | Strzałka w górę |
| `prestige.svg` / `.png` | 18x18 | Gwiazda |

### Bottom Panel (dolny pasek):
| Nazwa pliku | Rozmiar | Opis |
|-------------|---------|------|
| `sound_on.svg` / `.png` | 16x16 | Głośnik z falami |
| `sound_off.svg` / `.png` | 16x16 | Głośnik przekreślony |
| `autosave.svg` / `.png` | 16x16 | Zegar/timer |

## Obsługiwane formaty:
- **SVG** (preferowany) - skalowalne, ostre na każdym rozmiarze
- **PNG** - z przezroczystością (alpha)

## Priorytet ładowania:
1. SVG z resources (po kompilacji)
2. PNG z resources
3. SVG z folderu `src/main/resources/icons/`
4. PNG z folderu `src/main/resources/icons/`

## Zalecenia dla SVG:
- Użyj viewBox np. `viewBox="0 0 24 24"`
- Kolory: pomarańczowy `#ff780f` lub biały `#ffffff`
- Styl: Flat/minimalistyczny
- Bez osadzonych bitmap

## Źródła darmowych ikon:
- https://icons8.com
- https://flaticon.com  
- https://iconmonstr.com
- https://feathericons.com
- https://heroicons.com (SVG)
