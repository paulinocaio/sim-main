package io.sim.Project;

import java.util.ArrayList;

import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import it.polito.appeal.traci.SumoTraciConnection;

public class RouteData extends Thread {
    private SumoTraciConnection sumo;
    private Car _car;
    private ArrayList<String> currentEdges = new ArrayList<String>();
    private String currentEdge = "";
    private String lastEdge = "";
    private double simulationTime = 0.0;
    private double totalDistance = 0.0;
    private double previousTime = 0.0;
    private double totalTime = 0.0;
    private ArrayList<Double> timeMeasurements = new ArrayList<Double>();
    private ArrayList<Double> edgesDistances = new ArrayList<Double>();

    public RouteData(SumoTraciConnection _sumo, Car _car, ArrayList<String> _currentEdges) {
        this.sumo = _sumo;
        this._car = _car;
        this.currentEdges = _currentEdges;
    }

    @Override
    public void run() {
        while (!this.sumo.isClosed()) {
            try {

                currentEdge = (String) this.sumo.do_job_get(Vehicle.getRoadID(_car.getIdAuto()));
                simulationTime = (double) this.sumo.do_job_get(Simulation.getTime());
                totalDistance = (double) this.sumo.do_job_get(Vehicle.getDistance(_car.getIdAuto()));

                if (!currentEdge.equals(lastEdge) && currentEdges.contains(currentEdge)) {
                    try {
                        double edgeTime = simulationTime - previousTime;
                        previousTime = simulationTime;
                        lastEdge = currentEdge;
                        timeMeasurements.add(edgeTime);
                        getDistanceTraveledAtEdge();
                        System.out.println("Tempo: " + edgeTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(50);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (this.sumo.isClosed()) {
            timeMeasurements.add(simulationTime - previousTime);
            totalTime = simulationTime;
            getDistanceTraveledAtEdge();
        }

        // printTime();
        // printDistance();
    }

    public ArrayList<Double> getTimeMeasurements() {
        return timeMeasurements;
    }

    public ArrayList<Double> getEdgesDistances() {
        return edgesDistances;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void getDistanceTraveledAtEdge() {
        double distanceSum = 0.0;

        for (Double distance : edgesDistances) {
            distanceSum += distance;
        }

        edgesDistances.add(totalDistance - distanceSum);
    }

    public void printTime() {
        for (Double time : timeMeasurements) {
            System.out.println("| " + time + " | ");
        }
    }

    public void printDistance() {
        for (Double distance : edgesDistances) {
            System.out.println("| " + distance + " | ");
        }
    }

}