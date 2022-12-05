package me.astagg.core;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Available here: https://disease-ontology.org/downloads/
        final String diseaseOntologyPath = "D:\\Users\\astagg\\Documents\\HumanDiseaseOntology-main\\src\\ontology\\releases\\doid.owl";


        final Model model = ModelFactory.createDefaultModel();

        try (InputStream in = RDFDataMgr.open(diseaseOntologyPath)) {
            model.read(in, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String diseaseName = "";

        System.out.print("Ingrese el nombre de la enfermedad: ");
        try {
            diseaseName = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (diseaseName.isEmpty()) {
            System.err.println("ERROR: el nombre de la enfermedad es necesario");
            System.exit(1);
        }

        final ResultSet rs = QueryExecutionFactory.create("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "SELECT ?lbl WHERE {\n"
                + "?s rdfs:label ?lbl\n"
                + "FILTER regex(?lbl, \"" + diseaseName + "\", \"i\")\n"
                + "}", model).execSelect();

        final List<Map> jsonList = new Vector<>();
        while (rs.hasNext()) {
            final QuerySolution qs = rs.nextSolution();

            Iterator<String> varNames = qs.varNames();
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            while (varNames.hasNext()) {
                String varName = varNames.next();
                jsonMap.put(varName, qs.get(varName).toString());
            }

            jsonList.add(jsonMap);
        }

        if (jsonList.size() > 0) {
            System.out.println("Hemos encontrando las siguientes enfermedades relacionadas a tu búsqueda:");

            try {
                System.out.println(JsonUtils.toPrettyString(jsonList));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("ERROR: Tu búsqueda no arrojó ningún resultado");
        }
    }
}
