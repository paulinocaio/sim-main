package io.sim.Project;

public class BotPayment extends Thread {

    private Company company; // Referência para a empresa
    private Driver driver; // Referência para o motorista

    // Construtor 1
    public BotPayment(Company company) {
        this.company = company;
    }

    // Construtor 2
    public BotPayment(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void run() {
    }

    public void payDriver(double PaymentToReceive, Driver driver) {
        if (driver != null) {
            double payment = PaymentToReceive; // R$3,25 por km
            driver.getAccount().updateBalance(payment, TransactionType.DEPOSIT);
            System.out.println("Pagamento de R$" + payment + " realizado ao motorista " + driver.getId());
        } else {
            System.out.println("Nenhum motorista associado para realizar o pagamento.");
        }
    }

    public void payFuelStation(double fuelLiters, FuelStation fuelStation) {
        if (fuelStation != null) {
            double payment = fuelLiters * 5.87; // R$5,87 por litro
            fuelStation.getAccount().updateBalance(payment, TransactionType.WITHDRAWAL);
            System.out.println("Pagamento de R$" + payment + " realizado à FuelStation.");
        } else {
            System.out.println("Nenhuma FuelStation associada para realizar o pagamento.");
        }
    }
}
