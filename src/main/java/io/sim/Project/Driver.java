package io.sim.Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.TransportService;
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;

public class Driver extends Thread {

    private UUID ID;
    private Car car;
    private Route currentRoute;
    private ArrayList<Route> executedRoutes;
    private Account account; // Conta-Corrente
    private BotPayment botPayment; // Thread para pagamento
    private boolean available;
    private SumoTraciConnection sumo;
    private FuelStation fuelStation;

    // Construtor
    public Driver(Account account, FuelStation fuelStation) {
        this.ID = UUID.randomUUID();
        currentRoute = null;
        this.account = account;
        this.executedRoutes = new ArrayList<>();
        this.botPayment = new BotPayment(this);
        this.available = true;
        this.fuelStation = fuelStation;

        // Link do carro com o SUMO
        int fuelType = 2;
        int fuelPreferential = 2;
        double fuelPrice = 5.87;
        int personCapacity = 1;
        int personNumber = 1;
        SumoColor green = new SumoColor(0, 255, 0, 126);
        try {
            this.car = new Car(true, UUID.randomUUID().toString(), green, this.ID.toString(), sumo, 100, fuelType,
                    fuelPreferential, fuelPrice, personCapacity, personNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {

            if (currentRoute != null) {
                setAvailable(false); // motorista ocupado
                simulator();
                TransportService transportService = new TransportService(true, UUID.randomUUID().toString(),
                        currentRoute, car, sumo);
                transportService.start();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                car.setSumo(sumo);
                runRoute();
                try {
                    transportService.join();
                    setCurrentRoute(null);
                    setAvailable(true); // motorista disponível
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            

        }
    }

    private void runRoute() {
        car.executeRoute(this, currentRoute, sumo);
        executedRoutes.add(currentRoute);
        currentRoute = null;
    }

    public void addExecutedRoute(Route route) {
        executedRoutes.add(route);
    }

    public ArrayList<Route> getExecutedRoutes() {
        return executedRoutes;
    }

    public Route getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(Route currentRoute) {
        this.currentRoute = currentRoute;
        this.available = false;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public BotPayment getBotPayment() {
        return botPayment;
    }

    public FuelStation getFuelStation() {
        return fuelStation;
    }

    public void goToFuelStation() {
        fuelStation.addCarToWaitingList(car);
    }

    private void simulator() {
        /* SUMO */
        String sumo_bin = "sumo-gui";
        String config_file = "map/map.sumo.cfg";

        // Sumo connection
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        this.sumo.addOption("start", "1"); // auto-run on GUI show
        sumo.addOption("quit-on-end", "1"); // auto-close on end

        try {
            this.sumo.runServer(12345);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
