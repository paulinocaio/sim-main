package io.sim.Project;

import java.util.ArrayList;
import java.util.UUID;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.Auto;
import io.sim.DrivingData;
import it.polito.appeal.traci.SumoTraciConnection;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;

public class Car extends Auto {
    private UUID ID;
    private Route currentRoute;
    private double fuelTank;
    private Driver driver;
    private boolean isRunning;
    private boolean isWaitingForRefuel;
    private double MAX_FUEL_CAPACITY = 45;
    private double MIN_FUEL_CAPACITY = 5;
    private SumoTraciConnection sumo;
    private double distance = 0.0;
    public RouteData routeData;

    public Car(boolean _on_off, String _idAuto, SumoColor _colorAuto, String _driverID, SumoTraciConnection _sumo,
            long _acquisitionRate, int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity,
            int _personNumber) throws Exception {
        super(_on_off, _idAuto, _colorAuto, _driverID, _sumo, _acquisitionRate, _fuelType, _fuelPreferential,
                _fuelPrice, _personCapacity, _personNumber);
        this.fuelTank = 25;
        this.currentRoute = null;
        this.isRunning = false;
        this.isWaitingForRefuel = false;
        this.sumo = _sumo;

        this.ID = UUID.randomUUID();
    }

    @Override
    public void run() {
        while (true) {
            if (currentRoute != null) {

                if (fuelTank < MIN_FUEL_CAPACITY && !isWaitingForRefuel) {
                    setWaitingForRefuel(true);
                    waitToRefuel();
                }

                if (!isWaitingForRefuel) {
                    try {
                        atualizaSensores();
                        distance = (double) sumo.do_job_get(Vehicle.getDistance(this.getIdAuto()));
                        consumeFuel();
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            if (this.sumo.isClosed() && currentRoute != null) {
                driver.requestPayment(distance);
                try {
                    generateLog();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                currentRoute = null;
                driver.getCompany().addExecutedRoute(currentRoute);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void executeRoute(Driver driver, Route currentRoute, SumoTraciConnection sumo) {
        this.driver = driver;
        this.currentRoute = currentRoute;
        this.sumo = sumo;
        if (!isAlive()) {
            this.start();
        }

        // start da thread para obter os pontos
        routeData = new RouteData(sumo, this, this.getCurrentEdges());
        routeData.start();

    }

    private void consumeFuel() {
        try {
            double fuelConsumption = (double) getSumo().do_job_get(Vehicle.getFuelConsumption(this.getIdAuto())) / 1000;
            if (fuelConsumption > 0) {
                fuelTank -= fuelConsumption;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitToRefuel() {
        System.out.println("REABASTECENDO O CARRO " + getID());

        driver.goToFuelStation();

        // Aguarda até que o abastecimento seja concluído
        while (!isWaitingForRefuel) {
            try {
                Thread.sleep(1000); // Verifica a cada segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void refuelTank(double liters) {
        double litersToPay = liters;

        fuelTank += litersToPay;

        if (fuelTank > 45) {
            litersToPay = litersToPay - (fuelTank - 45);
        }

        driver.getBotPayment().payFuelStation(litersToPay, driver.getFuelStation());
        setWaitingForRefuel(false);
    }

    // Getters e setters
    public UUID getID() {
        return ID;
    }

    public double getFuelTank() {
        return fuelTank;
    }

    public void setFuelTank(double fuelTank) {
        this.fuelTank = fuelTank;
    }

    public Route getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(Route currentRoute) {
        this.currentRoute = currentRoute;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isWaitingForRefuel() {
        return isWaitingForRefuel;
    }

    public void setWaitingForRefuel(boolean isWaitingForRefuel) {
        this.isWaitingForRefuel = isWaitingForRefuel;
    }

    public double getMAX_FUEL_CAPACITY() {
        return MAX_FUEL_CAPACITY;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    private void generateLog() throws IOException {
        String path = "C:/Users/caio_/Desktop/sim-main/src/main/java/io/sim/Project/Report.csv";

        CSVWriter writer = new CSVWriter(new FileWriter(path, true));

        // Adicionar dados à linha
        for (DrivingData report : super.getDrivingRepport()) {

            String[] data = {
                    String.valueOf(System.nanoTime()),
                    getIdAuto(),
                    report.getRouteIDSUMO(),
                    String.valueOf(report.getSpeed()),
                    String.valueOf(report.getOdometer()),
                    String.valueOf(report.getFuelConsumption()),
                    String.valueOf(report.getFuelType()),
                    String.valueOf(report.getCo2Emission())
            };

            // Adicionar a linha ao arquivo CSV
            writer.writeNext(data);
        }
    }

    private ArrayList<String> getCurrentEdges() {
        ArrayList<String> edge = new ArrayList<String>();
        edge.clear();
        String[] aux = this.currentRoute.getItinerary();

        for (String e : aux[1].split(" ")) {
            edge.add(e);
        }
        return edge;
    }
}