package io.sim.Project;

import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class FuelStation extends Thread {

    private Account account;
    private Semaphore fuelPumps;
    private ArrayList<Car> carsWaiting;
    private double VALUE_TO_REFUEL = 20.0;

    public FuelStation(Account account, int numFuelPumps) {
        this.account = account;
        this.fuelPumps = new Semaphore(numFuelPumps, true);
        this.carsWaiting = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (carsWaiting.size() > 0) {
                        fuelPumps.acquire();
                        Car carToRefuel = carsWaiting.remove(0);
                        Thread.sleep(5000);
                        carToRefuel.refuelTank(VALUE_TO_REFUEL);
                        fuelPumps.release();
                    }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void addCarToWaitingList(Car car) {
        carsWaiting.add(car);
    }

}