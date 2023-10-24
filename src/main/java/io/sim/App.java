package io.sim;

import io.sim.Project.Route;

import io.sim.Project.AlphaBank;
import io.sim.Project.Company;
import io.sim.Project.Driver;
import io.sim.Project.FuelStation;

public class App {
    public static void main( String[] args ) {
        AlphaBank alphaBank = new AlphaBank();
        alphaBank.createAccount("Company", "123");
        alphaBank.createAccount("Driver1", "123");
        //alphaBank.createAccount("Driver2", "123");
        //alphaBank.createAccount("Driver3", "123");
        alphaBank.createAccount("fuelStation", "123");

        FuelStation fuelStation = new FuelStation(alphaBank.getAccount("fuelStation"), 2);
        fuelStation.start();

        Company company = new Company(alphaBank.getAccount("Company"));

        for (int i = 0; i < 5; i++) {
            String routeId = Integer.toString(i);
            company.addRoute(new Route("data/dados2.xml", routeId));
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Driver driver1 = new Driver(alphaBank.getAccount("Driver1"), fuelStation);
        //Driver driver2 = new Driver(alphaBank.getAccount("Driver2"), fuelStation);
        //Driver driver3 = new Driver(alphaBank.getAccount("Driver3"), fuelStation);

        company.addDriver(driver1);
        //company.addDriver(driver2);
        //company.addDriver(driver3);

        company.start();

    }
}