package io.sim.Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.TransportService;
import it.polito.appeal.traci.SumoTraciConnection;

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
    private Company company;
    private ArrayList<ArrayList<Double>> reconciliatedDistances;
    private ArrayList<ArrayList<Double>> reconciliatedTimes;
    private ArrayList<ArrayList<Double>> rawDistances;

    // Construtor
    public Driver(Account account, FuelStation fuelStation, Company company) {
        this.ID = UUID.randomUUID();
        currentRoute = null;
        this.account = account;
        this.executedRoutes = new ArrayList<>();
        this.botPayment = new BotPayment(this);
        this.available = true;
        this.fuelStation = fuelStation;
        this.company = company;
        this.reconciliatedDistances = new ArrayList<>();
        this.reconciliatedTimes = new ArrayList<>();
        this.rawDistances = new ArrayList<>();

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

                if (sumo.isClosed()) {
                    // RECONCILIAÇÃO
                    distanceReconciliation();
                    timeReconciliation();
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    public void saveExcel(ArrayList<ArrayList<Double>> list, String fileName) {
        DataSaver dataSaver = new DataSaver(list, fileName + ".csv");
        dataSaver.saveData();
    }

    public void createReconciliationFluxChart(ArrayList<ArrayList<Double>> reconcileatedMeasures,
            ArrayList<ArrayList<Double>> rawMeasures, String title, String xAxisLabel, String yAxisLabel) {

        double[] measures1 = getAllElementsAt(rawMeasures, 1);
        double[] measures2 = getAllElementsAt(reconciliatedDistances, 1);

        GraphPlotter plotter = new GraphPlotter(title, xAxisLabel, yAxisLabel, measures1, measures2);
        plotter.plotGraph();
    }

    public void calculate(int index) {
        // Calculos para as medidas de distancia
        System.out.println("Média = " + calculateMedia(reconciliatedDistances, index));
        System.out.println("Desvio Padrão = " + calculateStandardDeviation(reconciliatedDistances, index));
        System.out.println("Precisão = " + calculatePrecision(reconciliatedDistances, index));
    }

    private double calculateMedia(ArrayList<ArrayList<Double>> list, int index) {
        double soma = 0;
        double[] all = getAllElementsAt(list, 0);
        for (int i = 0; i < all.length; i++) {
            soma += all[i];
        }

        double media = soma / list.size();

        return media;
    }

    private double calculateStandardDeviation(ArrayList<ArrayList<Double>> list, int index) {
        double media = calculateMedia(list, index);
        double soma = 0;

        double[] all = getAllElementsAt(list, 0);
        for (int i = 0; i < all.length; i++) {
            soma += Math.pow((all[i] - media), 2);
        }

        double standardDeviation = Math.sqrt(soma / list.size());

        return standardDeviation;
    }

    private double calculatePrecision(ArrayList<ArrayList<Double>> list, int index) {
        double standardDeviation = calculateStandardDeviation(list, index);
        double precision = 2 * standardDeviation;

        return precision;
    }

    private double[] getAllElementsAt(ArrayList<ArrayList<Double>> values, int index) {
        double[] allElementsAt = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            allElementsAt[i] = values.get(i).get(index);
        }
        return allElementsAt;
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

    public Company getCompany() {
        return company;
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

    public ArrayList<ArrayList<Double>> getReconciliatedDistances() {
        return reconciliatedDistances;
    }

    public ArrayList<ArrayList<Double>> getReconciliatedTimes() {
        return reconciliatedTimes;
    }

    public void goToFuelStation() {
        fuelStation.addCarToWaitingList(car);
    }

    public void requestPayment(double distance) {
        company.getBotPayment().payDriver(distance, this);
    }

    private double[] arrayListToDouble(ArrayList<Double> arrayList) {
        // Create a new array with the same size as the ArrayList
        double[] doubleArray = new double[arrayList.size()];
        // Copy each element from the ArrayList to the array
        for (int i = 0; i < arrayList.size(); i++) {
            doubleArray[i] = arrayList.get(i);
        }

        return doubleArray;
    }

    private void distanceReconciliation() {
        // 1º realizar medições da rota
        ArrayList<Double> distanceArray = this.getCar().routeData.getEdgesDistances();

        // add a distancia total na primeira posicao (F1 do fluxo de reconciliacao)
        distanceArray.set(0, car.getDistance());

        // matriz de variancia
        double[] varianceVector = new double[distanceArray.size()];
        for (int i = 0; i < distanceArray.size(); i++) {
            varianceVector[i] = 0.0000000000000001;
        }

        // matriz de incidencia
        double[] matrixA = new double[distanceArray.size()];
        matrixA[0] = 1;
        for (int i = 1; i < distanceArray.size(); i++) {
            matrixA[i] = -1;
        }

        // converter arraylist para array de double
        double[] doubleDistanceArray = arrayListToDouble(distanceArray);

        // chamada da reconciliacao
        Reconciliation distanceReconciliation = new Reconciliation(doubleDistanceArray, varianceVector, matrixA);

        // print
        System.out.println("Distancia total: " + car.getDistance());
        System.out.println("Vetor de Medições: ");
        car.routeData.printDistance();
        System.out.println("Vetor Reconciliado: ");
        Reconciliation.printMatrix(distanceReconciliation.getReconciledFlow());

        ArrayList<Double> formatedDoubles = new ArrayList<>();
        for (int i = 1; i < distanceReconciliation.getReconciledFlow().length - 1; i++) {
            formatedDoubles.add(distanceReconciliation.getReconciledFlow()[i]);
        }

        // armazena o vetor reconciliado num array
        reconciliatedDistances.add(formatedDoubles);
        rawDistances.add(distanceArray);
    }

    private void timeReconciliation() {
        // 1º realizar medições da rota
        ArrayList<Double> timeArray = this.getCar().routeData.getTimeMeasurements();

        // add a distancia total na primeira posicao (F1 do fluxo de reconciliacao)
        timeArray.set(0, this.getCar().routeData.getTotalTime());

        // matriz de variancia
        double[] varianceVector = new double[timeArray.size()];
        for (int i = 0; i < timeArray.size(); i++) {
            varianceVector[i] = 0.0000000000000001;
        }

        // matriz de incidencia
        double[] matrixA = new double[timeArray.size()];
        matrixA[0] = 1;
        for (int i = 1; i < timeArray.size(); i++) {
            matrixA[i] = -1;
        }

        // converter arraylist para array de double
        double[] doubleTimeArray = arrayListToDouble(timeArray);

        // chamada da reconciliacao
        Reconciliation timeReconciliation = new Reconciliation(doubleTimeArray, varianceVector,
                matrixA);

        // print
        System.out.println("Tempo total: " + car.getDistance());
        System.out.println("Vetor de Medições: ");
        car.routeData.printTime();
        System.out.println("Vetor Reconciliado: ");
        Reconciliation.printMatrix(timeReconciliation.getReconciledFlow());

        ArrayList<Double> formatedDoubles = new ArrayList<>();
        for (int i = 1; i < timeReconciliation.getReconciledFlow().length - 1; i++) {
            formatedDoubles.add(timeReconciliation.getReconciledFlow()[i]);
        }

        // armazena o vetor reconciliado num array
        reconciliatedTimes.add(formatedDoubles);
    }

    public ArrayList<ArrayList<Double>> getRawDistances() {
        return rawDistances;
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
