import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String csvToJson = "dataJ.json";
        String xmlToJson = "data2.json";

        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeToJsonFile(json, csvToJson);

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeToJsonFile(json2, xmlToJson);
    }

    private static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        List<Employee> employees = new ArrayList<Employee>();

        try {
            File file = new File(fileName);
            builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Node root = doc.getDocumentElement();
            NodeList empNodes = root.getChildNodes();

            for (int i = 0; i < empNodes.getLength(); i++) {
                if (Node.ELEMENT_NODE == empNodes.item(i).getNodeType()) {
                    NodeList nodeListProperties = empNodes.item(i).getChildNodes();

                    int id = 0;
                    String firstName = "";
                    String lastName = "";
                    String country = "";
                    int age = 0;

                    for (int j = 0; j < nodeListProperties.getLength(); j++) {
                        if (Node.ELEMENT_NODE == nodeListProperties.item(j).getNodeType()) {
                            Element prop = (Element) nodeListProperties.item(j);

                            switch (prop.getNodeName()) {
                                case "id":
                                    id = Integer.parseInt(prop.getTextContent());
                                    break;
                                case "firstName":
                                    firstName = prop.getTextContent();
                                    break;
                                case "lastName":
                                    lastName = prop.getTextContent();
                                    break;
                                case "country":
                                    country = prop.getTextContent();
                                    break;
                                case "age":
                                    age = Integer.parseInt(prop.getTextContent());
                                    break;
                            }
                        }
                    }

                    Employee emp = new Employee(id, firstName, lastName, country, age);
                    employees.add(emp);
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return employees;

    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    private static void writeToJsonFile(String json, String file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csvBuilder = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            return csvBuilder.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
