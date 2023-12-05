package io.sim.Project;

import java.util.ArrayList;

public class Company extends Thread {

    private ArrayList<Route> routes; // Rotas a serem executadas
    private ArrayList<Route> runningRoutes; // Rotas em execução
    private ArrayList<Route> executedRoutes; // Rotas executadas
    private ArrayList<Driver> drivers; // Motoristas
    private BotPayment botPayment; // Thread para pagamento
    private Account account; // Conta-Corrente

    // Construtor
    public Company(Account account) {
        this.routes = new ArrayList<>();
        this.runningRoutes = new ArrayList<>();
        this.executedRoutes = new ArrayList<>();
        this.drivers = new ArrayList<>();
        this.botPayment = new BotPayment(this);
        this.account = account;
    }

    @Override
    public void run() {
        double initialSize = routes.size();
        while (true) {
            if (routes.size() != 0) {
                Driver availableDriver = getAvailableDriver();

                if (availableDriver != null) {
                    Route routeToExecute = routes.remove(0);
                    new Thread(() -> {
                        try {
                            availableDriver.setCurrentRoute(routeToExecute);
                            if (!availableDriver.isAlive()) {
                                availableDriver.start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            if(executedRoutes.size() == initialSize) {
                for(Driver driver : drivers) {
                    driver.calculate(1);
                    driver.saveExcel(driver.getReconciliatedDistances(),"distanceData");
                    driver.saveExcel(driver.getReconciliatedTimes(),"timeData");
                    driver.createReconciliationFluxChart(driver.getReconciliatedDistances(), driver.getRawDistances(),"F1 distances", "Flux", "Distance");
                }
                break;
            }

            try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void removeRoute(Route route) {
        routes.remove(route);
    }

    public void addRunningRoute(Route route) {
        runningRoutes.add(route);
    }

    public void removeRunningRoute(Route route) {
        runningRoutes.remove(route);
    }

    public void addExecutedRoute(Route route) {
        executedRoutes.add(route);
    }

    public ArrayList<Driver> getDrivers() {
        return drivers;
    }

    public void addDriver(Driver driver) {
        drivers.add(driver);
    }

    public void removeDriver(Driver driver) {
        drivers.remove(driver);
    }

    private Driver getAvailableDriver() {
        for (Driver driver : drivers) {
            if (driver.isAvailable()) {
                return driver;
            }
        }
        return null;
    }

    public BotPayment getBotPayment() {
        return botPayment;
    }

    public Account getAccount() {
        return account;
    }
}