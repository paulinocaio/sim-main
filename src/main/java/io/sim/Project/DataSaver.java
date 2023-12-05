package io.sim.Project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataSaver {

    private ArrayList<ArrayList<Double>> data;
    private String filePath;

    public DataSaver(ArrayList<ArrayList<Double>> data, String filePath) {
        this.data = data;
        this.filePath = filePath;
    }

    public void saveData() {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Escrever cabeçalhos
            writeHeaders(writer);

            // Escrever dados
            for (ArrayList<Double> row : data) {
                writeRow(writer, row);
            }

            System.out.println("Dados salvos com sucesso no arquivo: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeaders(FileWriter writer) throws IOException {
        if (!data.isEmpty() && !data.get(0).isEmpty()) {
            int numColumns = data.get(0).size();
            for (int i = 1; i <= numColumns; i++) {
                writer.append("F" + i);
                if (i < numColumns) {
                    writer.append(",");
                }
            }
            writer.append("\n");
        }
    }

    private void writeRow(FileWriter writer, ArrayList<Double> row) throws IOException {
        for (int i = 0; i < row.size(); i++) {
            writer.append(row.get(i).toString());
            if (i < row.size() - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");
    }
}

