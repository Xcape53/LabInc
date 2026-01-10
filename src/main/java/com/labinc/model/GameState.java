package com.labinc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.labinc.util.ResourceLoader;

/**
 * Główna klasa przechowująca stan gry
 */
public class GameState {
    private double money;
    private final Map<String, Resource> resources;
    private final Map<String, Factory> factories;
    private final Map<String, Recipe> recipes;
    private final List<GameEventListener> listeners;

    public interface GameEventListener {
        default void onMoneyChanged(double newMoney) {
        }

        default void onResourceChanged(String resourceSymbol, double newAmount) {
        }

        default void onFactoryUpgraded(String factoryName, int newTier) {
        }

        default void onRecipeUpgraded(String recipeId, int newLevel) {
        } // Nowy event
    }

    public GameState() {
        this.money = 0.0;
        this.resources = new LinkedHashMap<>();
        this.factories = new LinkedHashMap<>();
        this.recipes = new LinkedHashMap<>();
        this.listeners = new ArrayList<>();

        initializeResources();
        initializeFactories();
        initializeRecipes();
    }

    private void initializeResources() {
        // Wczytaj surowce z pliku JSON
        resources.putAll(ResourceLoader.loadResources());
    }

    private void initializeFactories() {
        // 1. Kopalnia Węgla
        Map<String, Factory.ProductionData> kwProd = new HashMap<>();
        kwProd.put("C", new Factory.ProductionData(1000, 1));
        kwProd.put("Ge", new Factory.ProductionData(0.1, 2));
        kwProd.put("S", new Factory.ProductionData(50, 3));
        Factory kw = new Factory("KW", "Kopalnia Węgla", 3, 2500, 1.45, 2, 1.0, 1.15, kwProd, null, null);
        kw.setCurrentTier(1);
        kw.setMultiplier(1.0);
        factories.put("KW", kw);

        // 2. Szyb Naftowy
        Map<String, Factory.ProductionData> snProd = new HashMap<>();
        snProd.put("He", new Factory.ProductionData(25.0 / 60, 1));
        snProd.put("H", new Factory.ProductionData(250.0 / 60, 2));
        factories.put("SN", new Factory("SN", "Szyb Naftowy", 3, 9000, 1.45, 1, 11.0, 1.20, snProd,
                f -> f.get("KW").getCurrentTier() >= 3, "Wymaga: KW T3"));

        // 3. KDA
        Map<String, Factory.ProductionData> kdaProd = new HashMap<>();
        kdaProd.put("Ar", new Factory.ProductionData(2.0 / 60, 1));
        kdaProd.put("Xe", new Factory.ProductionData(2.0 / 60, 2));
        kdaProd.put("Kr", new Factory.ProductionData(3.0 / 60, 3));
        kdaProd.put("Ne", new Factory.ProductionData(7.5 / 60, 4));
        kdaProd.put("O", new Factory.ProductionData(20.0 / 60, 5));
        kdaProd.put("N", new Factory.ProductionData(80.0 / 60, 6));
        factories.put("KDA", new Factory("KDA", "KDA", 6, 50000, 1.45, 1, 1700, 1.05, kdaProd,
                f -> f.get("SN").getCurrentTier() >= 3, "Wymaga: SN T3"));

        // 4. Salina
        Map<String, Factory.ProductionData> salProd = new HashMap<>();
        salProd.put("Cs", new Factory.ProductionData(4.0 / 60, 1));
        salProd.put("Ba", new Factory.ProductionData(5.0 / 60, 2));
        salProd.put("Br", new Factory.ProductionData(5.0 / 60, 3));
        salProd.put("Rb", new Factory.ProductionData(7.5 / 60, 4));
        salProd.put("K", new Factory.ProductionData(10.0 / 60, 5));
        salProd.put("Ca", new Factory.ProductionData(15.0 / 60, 6));
        salProd.put("B", new Factory.ProductionData(15.0 / 60, 7));
        salProd.put("Li", new Factory.ProductionData(20.0 / 60, 8));
        salProd.put("Sr", new Factory.ProductionData(20.0 / 60, 9));
        salProd.put("Mg", new Factory.ProductionData(24.0 / 60, 10));
        salProd.put("I", new Factory.ProductionData(25.0 / 60, 11));
        salProd.put("Cl", new Factory.ProductionData(80.0 / 60, 12));
        salProd.put("Na", new Factory.ProductionData(80.0 / 60, 13));
        factories.put("SAL", new Factory("SAL", "Salina", 13, 4500000, 1.33, 1, 72000, 1.014, salProd,
                f -> f.get("KDA").getCurrentTier() >= 6, "Wymaga: KDA T6"));

        // Pozostałe kopalnie (pełna lista wymagana dla zgodności)
        // 5. Kopalnia Rudy Żelaza
        Map<String, Factory.ProductionData> krzProd = new HashMap<>();
        krzProd.put("Mn", new Factory.ProductionData(20.0 / 60, 1));
        krzProd.put("P", new Factory.ProductionData(40.0 / 60, 2));
        krzProd.put("Fe", new Factory.ProductionData(200.0 / 60, 3));
        krzProd.put("Si", new Factory.ProductionData(150.0 / 60, 4));
        factories.put("KRZ", new Factory("KRZ", "Kopalnia Rudy Żelaza", 4, 500_000_000, 1.45, 1, 1_600_000, 1.125,
                krzProd, f -> f.get("SAL").getCurrentTier() >= 13, "Wymaga: SAL T13"));

        // 6. Kopalnia Boksytu
        Map<String, Factory.ProductionData> kbProd = new HashMap<>();
        kbProd.put("Ga", new Factory.ProductionData(5.0 / 60, 1));
        kbProd.put("Al", new Factory.ProductionData(300.0 / 60, 2));
        factories.put("KB", new Factory("KB", "Kopalnia Boksytu", 2, 20_000_000_000.0, 1.45, 1, 210_000_000, 1.19,
                kbProd, f -> f.get("KRZ").getCurrentTier() >= 4, "Wymaga: KRZ T4"));

        // 7. Kopalnia Rudy Miedzi
        Map<String, Factory.ProductionData> krmProd = new HashMap<>();
        krmProd.put("Re", new Factory.ProductionData(4.0 / 60, 1));
        krmProd.put("Te", new Factory.ProductionData(6.0 / 60, 2));
        krmProd.put("Ag", new Factory.ProductionData(6.4 / 60, 3));
        krmProd.put("Au", new Factory.ProductionData(6.5 / 60, 4));
        krmProd.put("Se", new Factory.ProductionData(12.5 / 60, 5));
        krmProd.put("Mo", new Factory.ProductionData(24.0 / 60, 6));
        krmProd.put("Cu", new Factory.ProductionData(100.0 / 60, 7));
        factories.put("KRM", new Factory("KRM", "Kopalnia Rudy Miedzi", 7, 1_500_000_000_000.0, 1.45, 1,
                19_100_000_000.0, 1.047, krmProd, f -> f.get("KB").getCurrentTier() >= 2, "Wymaga: KB T2"));

        // 8. KRCO
        Map<String, Factory.ProductionData> krcoProd = new HashMap<>();
        krcoProd.put("Tl", new Factory.ProductionData(5.0 / 60, 1));
        krcoProd.put("Sb", new Factory.ProductionData(10.0 / 60, 2));
        krcoProd.put("Cd", new Factory.ProductionData(15.0 / 60, 3));
        krcoProd.put("In", new Factory.ProductionData(15.0 / 60, 4));
        krcoProd.put("Bi", new Factory.ProductionData(20.0 / 60, 5));
        krcoProd.put("Pb", new Factory.ProductionData(80.0 / 60, 6));
        krcoProd.put("Zn", new Factory.ProductionData(120.0 / 60, 7));
        krcoProd.put("As", new Factory.ProductionData(10.0 / 60, 8));
        factories.put("KRCO", new Factory("KRCO", "Kopalnia Rudy Cynku i Ołowiu", 8, 150_000_000_000_000.0, 1.45, 1,
                1_600_000_000_000.0, 1.037, krcoProd, f -> f.get("KRM").getCurrentTier() >= 7, "Wymaga: KRM T7"));

        // 9. KRN
        Map<String, Factory.ProductionData> krnProd = new HashMap<>();
        krnProd.put("Co", new Factory.ProductionData(160.0 / 60, 1));
        krnProd.put("Ni", new Factory.ProductionData(160.0 / 60, 2));
        factories.put("KRN", new Factory("KRN", "Kopalnia Rudy Niklu", 2, 20_000_000_000_000_000.0, 1.45, 1,
                7_000_000_000_000.0, 1.14, krnProd, f -> f.get("KRCO").getCurrentTier() >= 8, "Wymaga: KRCO T8"));

        // 10. KMW
        Map<String, Factory.ProductionData> kmwProd = new HashMap<>();
        kmwProd.put("V", new Factory.ProductionData(300.0 / 60, 2));
        factories.put("KMW", new Factory("KMW", "Kopalnia Magnetytu Wanadowego", 2, 50_000_000_000_000_000.0, 1.45, 1,
                52_000_000_000_000.0, 1.15, kmwProd, f -> f.get("KRN").getCurrentTier() >= 2, "Wymaga: KRN T2"));

        // 11. KF
        Map<String, Factory.ProductionData> kfProd = new HashMap<>();
        kfProd.put("F", new Factory.ProductionData(300.0 / 60, 1));
        factories.put("KF", new Factory("KF", "Kopalnia Fluorytu", 1, 400_000_000_000_000_000.0, 1.0, 1,
                71_000_000_000_000.0, 1.0, kfProd, f -> f.get("KMW").getCurrentTier() >= 2, "Wymaga: KMW T2"));

        // 12. KRCW
        Map<String, Factory.ProductionData> krcwProd = new HashMap<>();
        krcwProd.put("Hg", new Factory.ProductionData(8.0 / 60, 1));
        krcwProd.put("Ta", new Factory.ProductionData(9.0 / 60, 2));
        krcwProd.put("Nb", new Factory.ProductionData(12.5 / 60, 3));
        krcwProd.put("Be", new Factory.ProductionData(16.0 / 60, 4));
        krcwProd.put("Sn", new Factory.ProductionData(120.0 / 60, 5));
        krcwProd.put("W", new Factory.ProductionData(150.0 / 60, 6));
        factories.put("KRCW", new Factory("KRCW", "Kopalnia Rudy Cyny i Wolframu", 6, 500_000_000_000_000_000.0, 1.35,
                1, 2_700_000_000_000_000.0, 1.037, krcwProd, f -> f.get("KF").getCurrentTier() >= 1, "Wymaga: KF T1"));

        // 13. KPM
        Map<String, Factory.ProductionData> kpmProd = new HashMap<>();
        kpmProd.put("Hf", new Factory.ProductionData(16.0 / 60, 1));
        kpmProd.put("Zr", new Factory.ProductionData(200.0 / 60, 2));
        kpmProd.put("Ti", new Factory.ProductionData(200.0 / 60, 3));
        factories.put("KPM",
                new Factory("KPM", "Kopalnia Piasków Mineralnych", 3, 40_000_000_000_000_000_000.0, 1.40, 1,
                        120_000_000_000_000_000.0, 1.25, kpmProd, f -> f.get("KRCW").getCurrentTier() >= 6,
                        "Wymaga: KRCW T6"));

        // 14. KP
        Map<String, Factory.ProductionData> kpProd = new HashMap<>();
        kpProd.put("Ru", new Factory.ProductionData(3.0 / 60, 1));
        kpProd.put("Os", new Factory.ProductionData(7.5 / 60, 2));
        kpProd.put("Ir", new Factory.ProductionData(9.0 / 60, 3));
        kpProd.put("Rh", new Factory.ProductionData(10.0 / 60, 4));
        kpProd.put("Cr", new Factory.ProductionData(30.0 / 60, 5));
        kpProd.put("Pd", new Factory.ProductionData(45.0 / 60, 6));
        kpProd.put("Pt", new Factory.ProductionData(40.0 / 60, 7));
        factories.put("KP",
                new Factory("KP", "Kopalnia Platynowców", 7, 1_500_000_000_000_000_000_000.0, 1.45, 1,
                        28_000_000_000_000_000_000.0, 1.071, kpProd, f -> f.get("KPM").getCurrentTier() >= 3,
                        "Wymaga: KPM T3"));

        // 15. KZR
        Map<String, Factory.ProductionData> kzrProd = new HashMap<>();
        kzrProd.put("Tm", new Factory.ProductionData(6.0 / 60, 1));
        kzrProd.put("Sc", new Factory.ProductionData(7.5 / 60, 2));
        kzrProd.put("Th", new Factory.ProductionData(7.5 / 60, 3));
        kzrProd.put("Lu", new Factory.ProductionData(8.0 / 60, 4));
        kzrProd.put("Eu", new Factory.ProductionData(10.0 / 60, 5));
        kzrProd.put("Ho", new Factory.ProductionData(12.5 / 60, 6));
        kzrProd.put("Y", new Factory.ProductionData(15.0 / 60, 7));
        kzrProd.put("Sm", new Factory.ProductionData(15.0 / 60, 8));
        kzrProd.put("Tb", new Factory.ProductionData(17.5 / 60, 9));
        kzrProd.put("Dy", new Factory.ProductionData(20.0 / 60, 10));
        kzrProd.put("Pr", new Factory.ProductionData(25.0 / 60, 11));
        kzrProd.put("Gd", new Factory.ProductionData(30.0 / 60, 12));
        kzrProd.put("Er", new Factory.ProductionData(30.0 / 60, 13));
        kzrProd.put("Yb", new Factory.ProductionData(36.0 / 60, 14));
        kzrProd.put("Ce", new Factory.ProductionData(40.0 / 60, 15));
        kzrProd.put("La", new Factory.ProductionData(40.0 / 60, 16));
        kzrProd.put("Nd", new Factory.ProductionData(40.0 / 60, 17));
        factories.put("KZR", new Factory("KZR", "Kopalnia Ziem Rzadkich", 17, 300_000_000_000_000_000_000_000.0, 1.32,
                1, 2.41e21, 1.014, kzrProd, f -> f.get("KP").getCurrentTier() >= 7, "Wymaga: KP T7"));

        // 16. Reaktor Jądrowy
        Map<String, Factory.ProductionData> rjProd = new HashMap<>();
        rjProd.put("Am", new Factory.ProductionData(40.0 / 60, 1));
        rjProd.put("Bk", new Factory.ProductionData(40.0 / 60, 2));
        rjProd.put("Cm", new Factory.ProductionData(100.0 / 60, 3));
        rjProd.put("Fm", new Factory.ProductionData(100.0 / 60, 4));
        rjProd.put("Pu", new Factory.ProductionData(160.0 / 60, 5));
        rjProd.put("No", new Factory.ProductionData(200.0 / 60, 6));
        rjProd.put("Np", new Factory.ProductionData(250.0 / 60, 7));
        rjProd.put("Cf", new Factory.ProductionData(250.0 / 60, 8));
        rjProd.put("Es", new Factory.ProductionData(250.0 / 60, 9));
        rjProd.put("Md", new Factory.ProductionData(250.0 / 60, 10));
        rjProd.put("Lr", new Factory.ProductionData(450.0 / 60, 11));
        rjProd.put("Rf", new Factory.ProductionData(1000.0 / 60, 12));
        rjProd.put("Sg", new Factory.ProductionData(2000.0 / 60, 13));
        rjProd.put("Db", new Factory.ProductionData(2500.0 / 60, 14));
        rjProd.put("Hs", new Factory.ProductionData(3000.0 / 60, 15));
        rjProd.put("Bh", new Factory.ProductionData(4000.0 / 60, 16));
        rjProd.put("Ds", new Factory.ProductionData(4000.0 / 60, 17));
        rjProd.put("Po", new Factory.ProductionData(5000.0 / 60, 18));
        rjProd.put("Mt", new Factory.ProductionData(5000.0 / 60, 19));
        rjProd.put("Cn", new Factory.ProductionData(5000.0 / 60, 20));
        rjProd.put("Fl", new Factory.ProductionData(7000.0 / 60, 21));
        rjProd.put("Rg", new Factory.ProductionData(7500.0 / 60, 22));
        rjProd.put("Ra", new Factory.ProductionData(9000.0 / 60, 23));
        rjProd.put("Lv", new Factory.ProductionData(9000.0 / 60, 24));
        rjProd.put("Pa", new Factory.ProductionData(10000.0 / 60, 25));
        rjProd.put("Nh", new Factory.ProductionData(10000.0 / 60, 26));
        rjProd.put("Og", new Factory.ProductionData(10000.0 / 60, 27));
        rjProd.put("Mc", new Factory.ProductionData(12500.0 / 60, 28));
        rjProd.put("Ac", new Factory.ProductionData(14000.0 / 60, 29));
        rjProd.put("Ts", new Factory.ProductionData(15000.0 / 60, 30));
        rjProd.put("Tc", new Factory.ProductionData(150000.0 / 60, 31));
        rjProd.put("Pm", new Factory.ProductionData(700000.0 / 60, 32));
        rjProd.put("Fr", new Factory.ProductionData(4000000.0 / 60, 33));
        rjProd.put("At", new Factory.ProductionData(5000000.0 / 60, 34));
        rjProd.put("Rn", new Factory.ProductionData(5000000.0 / 60, 35));
        factories.put("RJ", new Factory("RJ", "Reaktor Jądrowy", 35, 3e27, 1.30, 1, 3.7e23, 1.05, rjProd,
                f -> f.get("KZR").getCurrentTier() >= 17, "Wymaga: KZR T17"));
    }

    private void initializeRecipes() {
        // 1. Woda
        Recipe rWater = new Recipe("H2O_SYN", "Synteza Wody", "H2O", 1.0, 2.0);
        rWater.addInput("H", 2.0);
        rWater.addInput("O", 1.0);
        rWater.setUnlocked(true); // Dostępna od początku (jeśli masz H i O)
        recipes.put(rWater.getId(), rWater);

        // 2. CO2
        Recipe rCO2 = new Recipe("CO2_SYN", "Spalanie Węgla", "CO2", 1.0, 3.0);
        rCO2.addInput("C", 1.0);
        rCO2.addInput("O", 2.0);
        rCO2.setUnlocked(true);
        recipes.put(rCO2.getId(), rCO2);

        // 3. Metan
        Recipe rCH4 = new Recipe("CH4_SYN", "Synteza Metanu", "CH4", 1.0, 4.0);
        rCH4.addInput("C", 1.0);
        rCH4.addInput("H", 4.0);
        rCH4.setUnlocked(true);
        recipes.put(rCH4.getId(), rCH4);

        // 4. Amoniak
        Recipe rNH3 = new Recipe("NH3_SYN", "Proces Habera", "NH3", 1.0, 5.0);
        rNH3.addInput("N", 1.0);
        rNH3.addInput("H", 3.0);
        rNH3.setUnlocked(true);
        recipes.put(rNH3.getId(), rNH3);

        // 5. Kwas Siarkowy
        Recipe rH2SO4 = new Recipe("H2SO4_SYN", "Kwas Siarkowy", "H2SO4", 1.0, 8.0);
        rH2SO4.addInput("S", 1.0);
        rH2SO4.addInput("H", 2.0);
        rH2SO4.addInput("O", 4.0);
        rH2SO4.setUnlocked(true);
        recipes.put(rH2SO4.getId(), rH2SO4);

        // 6. Sól
        Recipe rNaCl = new Recipe("NACL_SYN", "Synteza Soli", "NaCl", 1.0, 6.0);
        rNaCl.addInput("Na", 1.0);
        rNaCl.addInput("Cl", 1.0);
        rNaCl.setUnlocked(true);
        recipes.put(rNaCl.getId(), rNaCl);
    }

    public void produceResources(double deltaTime) {
        // Zbieraj zmienione surowce do batch notification
        java.util.Set<String> changedResources = new java.util.HashSet<>();

        // 1. Produkcja z kopalń
        for (Factory factory : factories.values()) {
            Map<String, Double> production = factory.getCurrentProduction();
            for (Map.Entry<String, Double> entry : production.entrySet()) {
                String resourceSymbol = entry.getKey();
                double amount = entry.getValue() * deltaTime;

                Resource resource = resources.get(resourceSymbol);
                if (resource != null) {
                    resource.addAmount(amount);
                    changedResources.add(resourceSymbol);
                }
            }
        }

        // 2. Produkcja z Reaktorów
        processReactions(deltaTime, changedResources);

        // 3. Wykonaj autosell dla wszystkich surowców z włączonym autosell
        processAutoSell(changedResources);

        // 4. Batch notification - wyślij tylko raz na tick
        for (String symbol : changedResources) {
            Resource resource = resources.get(symbol);
            if (resource != null) {
                notifyResourceChanged(symbol, resource.getAmount());
            }
        }
    }

    /**
     * Inteligentny autosell - sprzedaje gdy wartość jest "znacząca"
     * Znacząca = min(threshold, dynamicThreshold) gdzie dynamicThreshold = 0.1%
     * pieniędzy
     */
    private void processAutoSell(java.util.Set<String> changedResources) {
        // Dynamiczny próg = 0.1% aktualnych pieniędzy, minimum $100
        double dynamicThreshold = Math.max(100.0, money * 0.001);

        for (Resource resource : resources.values()) {
            if (!resource.isAutoSellEnabled())
                continue;
            if (resource.getAmount() <= 0)
                continue;

            double currentValue = resource.getTotalValue();
            double userThreshold = resource.getAutoSellThreshold();

            // Użyj mniejszego z: progu użytkownika lub dynamicznego
            // Dzięki temu przy miliardach nie sprzedajemy za grosze
            double effectiveThreshold = Math.min(userThreshold, dynamicThreshold);

            // Ale jeśli użytkownik ustawił wysoki próg, respektuj go
            if (userThreshold > dynamicThreshold) {
                effectiveThreshold = userThreshold;
            }

            if (currentValue >= effectiveThreshold) {
                resource.setAmount(0);
                addMoney(currentValue);
                changedResources.add(resource.getSymbol());
            }
        }
    }

    private void processReactions(double deltaTime, java.util.Set<String> changedResources) {
        for (Recipe recipe : recipes.values()) {
            if (recipe.getLevel() == 0)
                continue;

            double productionSpeed = recipe.getProductionSpeed(); // Reakcje na sekundę (zależne od levelu)
            double cycles = productionSpeed * deltaTime;

            // Sprawdź czy stać nas na tę turę
            boolean canProduce = true;
            for (Map.Entry<String, Double> input : recipe.getInputs().entrySet()) {
                Resource res = resources.get(input.getKey());
                double required = input.getValue() * cycles;
                if (res == null || res.getAmount() < required) {
                    canProduce = false;
                    break;
                }
            }

            if (canProduce) {
                // Pobierz surowce
                for (Map.Entry<String, Double> input : recipe.getInputs().entrySet()) {
                    Resource res = resources.get(input.getKey());
                    double required = input.getValue() * cycles;
                    res.setAmount(res.getAmount() - required);
                    changedResources.add(res.getSymbol());
                }

                // Dodaj produkt
                Resource output = resources.get(recipe.getOutputResource());
                if (output != null) {
                    double produced = recipe.getOutputAmount() * cycles;
                    output.addAmount(produced);
                    changedResources.add(output.getSymbol());
                }
            }
        }
    }

    // --- Zarządzanie Budynkami Chemicznymi ---

    public boolean upgradeRecipe(String recipeId) {
        Recipe recipe = recipes.get(recipeId);
        if (recipe == null)
            return false;

        double cost = recipe.getUpgradeCost();
        if (money >= cost) {
            money -= cost;
            recipe.setLevel(recipe.getLevel() + 1);
            notifyMoneyChanged(money);
            notifyRecipeUpgraded(recipeId, recipe.getLevel());
            return true;
        }
        return false;
    }

    public Map<String, Recipe> getRecipes() {
        return recipes;
    }

    // --- Reszta standardowa ---

    public double calculateIncomePerSecond() {
        double income = 0.0;
        for (Factory factory : factories.values()) {
            Map<String, Double> production = factory.getCurrentProduction();
            for (Map.Entry<String, Double> entry : production.entrySet()) {
                Resource resource = resources.get(entry.getKey());
                if (resource != null) {
                    income += entry.getValue() * resource.getBasePrice();
                }
            }
        }
        return income;
    }

    /**
     * Oblicza przychód tylko z surowców które mają włączony autosell
     */
    public double calculateAutoSellIncomePerSecond(java.util.Set<String> autoSellSymbols) {
        double income = 0.0;
        for (Factory factory : factories.values()) {
            Map<String, Double> production = factory.getCurrentProduction();
            for (Map.Entry<String, Double> entry : production.entrySet()) {
                String symbol = entry.getKey();
                // Tylko surowce z autosell (sprawdź w Resource)
                Resource resource = resources.get(symbol);
                if (resource != null && resource.isAutoSellEnabled()) {
                    income += entry.getValue() * resource.getBasePrice();
                }
            }
        }
        return income;
    }

    /**
     * Oblicza przychód z autosell (bez parametru - używa stanu Resource)
     */
    public double calculateAutoSellIncomePerSecond() {
        double income = 0.0;
        for (Factory factory : factories.values()) {
            Map<String, Double> production = factory.getCurrentProduction();
            for (Map.Entry<String, Double> entry : production.entrySet()) {
                String symbol = entry.getKey();
                Resource resource = resources.get(symbol);
                if (resource != null && resource.isAutoSellEnabled()) {
                    income += entry.getValue() * resource.getBasePrice();
                }
            }
        }
        return income;
    }

    public void sellResource(String resourceSymbol, double amount) {
        Resource resource = resources.get(resourceSymbol);
        if (resource != null && resource.getAmount() >= amount) {
            double value = amount * resource.getBasePrice();
            resource.setAmount(resource.getAmount() - amount);
            addMoney(value);
            notifyResourceChanged(resourceSymbol, resource.getAmount());
        }
    }

    public void sellAllResources() {
        for (Resource resource : resources.values()) {
            if (resource.getAmount() > 0) {
                double value = resource.getTotalValue();
                addMoney(value);
                resource.setAmount(0);
                notifyResourceChanged(resource.getSymbol(), 0);
            }
        }
    }

    public boolean upgradeFactory(String factoryName) {
        Factory factory = factories.get(factoryName);
        if (factory == null) {
            return false;
        }

        if (!factory.isUnlocked(factories)) {
            return false;
        }

        double cost = factory.getUpgradeCost();

        // Użyj metody z Factory dla spójności
        if (factory.canAfford(money)) {
            money -= cost;
            factory.upgrade();
            notifyMoneyChanged(money);
            notifyFactoryUpgraded(factoryName, factory.getCurrentTier());
            return true;
        }
        return false;
    }

    private void addMoney(double amount) {
        money += amount;
        notifyMoneyChanged(money);
        // Sprawdź osiągnięcia przy zmianie pieniędzy (opcjonalnie, normalnie robi to
        // timer w LabIncGame)
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    private void notifyMoneyChanged(double newMoney) {
        for (GameEventListener listener : listeners)
            listener.onMoneyChanged(newMoney);
    }

    private void notifyResourceChanged(String symbol, double amount) {
        for (GameEventListener listener : listeners)
            listener.onResourceChanged(symbol, amount);
    }

    private void notifyFactoryUpgraded(String factoryName, int tier) {
        for (GameEventListener listener : listeners)
            listener.onFactoryUpgraded(factoryName, tier);
    }

    private void notifyRecipeUpgraded(String recipeId, int level) {
        for (GameEventListener listener : listeners)
            listener.onRecipeUpgraded(recipeId, level);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
        notifyMoneyChanged(money);
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public Map<String, Factory> getFactories() {
        return factories;
    }

    /**
     * Sprawdza czy dany surowiec jest produkowany przez jakąkolwiek fabrykę lub
     * recepturę
     */
    public boolean isResourceBeingProduced(String resourceSymbol) {
        // Sprawdź fabryki (kopalnie)
        for (Factory factory : factories.values()) {
            if (factory.getCurrentTier() > 0) {
                java.util.Map<String, Double> production = factory.getCurrentProduction();
                if (production.containsKey(resourceSymbol) && production.get(resourceSymbol) > 0) {
                    return true;
                }
            }
        }

        // Sprawdź receptury (fabryki chemiczne)
        for (Recipe recipe : recipes.values()) {
            if (recipe.getLevel() > 0 && recipe.getOutputResource().equals(resourceSymbol)) {
                return true;
            }
        }

        return false;
    }
}
