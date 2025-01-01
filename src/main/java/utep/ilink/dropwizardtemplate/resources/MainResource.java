package utep.ilink.dropwizardtemplate.resources;

import utep.ilink.dropwizardtemplate.api.AssumptionIgnore;
import utep.ilink.dropwizardtemplate.api.Input;
import utep.ilink.dropwizardtemplate.api.Model;
import utep.ilink.dropwizardtemplate.api.ModelCatalog;
import utep.ilink.dropwizardtemplate.api.Output;
import utep.ilink.dropwizardtemplate.api.Variable;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import io.swagger.annotations.*;

@Path("/main")
@Api(value = "/main", description = "Main entry point")
@Produces({ "application/json" })
public class MainResource {
    private static final String ERROR = "{\"error\":\"An error ocurred\"}";
    private final String ontologySource;
    private final String ontologySavePath;
    private final String iriPrefix;
    private final String EQUIVALENTCLASS;

    public MainResource(String ontologySource, String ontologySavePath, String iriPrefix, String equivalentClass) {
        this.ontologySource = ontologySource;
        this.ontologySavePath = ontologySavePath;
        this.iriPrefix = iriPrefix;
        this.EQUIVALENTCLASS = equivalentClass;
    }

    /**
     * Main entry point. This method is the entry point for variable reconciliation.
     * @param assumptionsIgnore List of assumptions to ignore as a JSON
     * @param availableData List of available variables as JSON
     * @param componentCatalog List of components as JSON
     * @return
     * @throws OWLOntologyStorageException
     */
    @POST
    @Timed
    @ApiOperation(value = "Main entry point", notes = "Constructs a JSON that represents all equivalent variables")
    public String main(@FormParam("ignore") String assumptionsIgnore,
            @FormParam("availableData") String availableData,
            @FormParam("componentCatalog") String componentCatalog) throws OWLOntologyStorageException {
        
        /*
         * var name
         * var data property
         * var class
         * for each var
         * assign class
         * for each assumption
         * create data property
         * end for
         * add owl
         * end for
         * run reasoner
         * get subclasses equivalent
         * for each subclass
         * get individuals
         * create 1-1 equivalency rules
         * end for
         * return equivalency rules
         */


        StringBuilder equivalencyStringBuilder = new StringBuilder("{\"rules\":[");

        PrefixManager prefixManager = new DefaultPrefixManager(iriPrefix);

        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = owlManager.getOWLDataFactory();
        OWLOntology ontology = null;

        try {

            // Read ontology
            ontology = owlManager.loadOntologyFromOntologyDocument(new File(ontologySource));

            ObjectMapper mapper = new ObjectMapper();

            // Read assumptions ignore list
            String[] assumptionsIgnoreArray = mapper.readValue(assumptionsIgnore, AssumptionIgnore.class).getIgnore();

            HashSet<String> assumptionsIgnoreList = new HashSet<>();
            for (String assumptionToIgnore : assumptionsIgnoreArray) {
                assumptionsIgnoreList.add(assumptionToIgnore);
            }

            // Read inputs
            Variable[] variables;
            Input inputCatalogObject = mapper.readValue(availableData, Input.class);
            variables = inputCatalogObject.getVariables();

            System.out.println("Collected " + variables.length + " input variables");
            createIndividuals(dataFactory, prefixManager, ontology, owlManager, variables);

            // Read outputs
            Output outputCatalogObject = mapper.readValue(availableData, Output.class);
            variables = outputCatalogObject.getVariables();

            System.out.println("Collected " + variables.length + " output variables");
            createIndividuals(dataFactory, prefixManager, ontology, owlManager, variables);

            // Read model catalog
            ModelCatalog modelCatalogObject = mapper.readValue(componentCatalog, ModelCatalog.class);
            Model[] models = modelCatalogObject.getModels();

            if (models != null && models.length != 0) {
                for (Model model : models) {
                    variables = model.getInputs();
                    System.out.println("Collected " + variables.length + " model input variables");
                    createIndividuals(dataFactory, prefixManager, ontology, owlManager, variables);
                    variables = model.getOutputs();
                    System.out.println("Collected " + variables.length + " model output variables");
                    createIndividuals(dataFactory, prefixManager, ontology, owlManager, variables);
                }
            }

            // Saving ontology
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
            LocalDateTime now = LocalDateTime.now();
            String dateTimeString = dtf.format(now);
            String filename = ontologySavePath + "ontology_" + dateTimeString + ".owl";
            owlManager.saveOntology(ontology, IRI.create(new File(filename)));

            // Retrieving individuals that are equivalent between each other
            // Use reasoner for getting individuals

            // Using hermit
            OWLReasoner reasoner = new ReasonerFactory().createReasoner(ontology);

            if (reasoner.isConsistent()) {

                OWLEntityChecker entityChecker = getEntityChecker(owlManager, ontology);

                ManchesterOWLSyntaxClassExpressionParser parser = new ManchesterOWLSyntaxClassExpressionParser(
                        dataFactory,
                        entityChecker);

                // for every input & output of every model
                // get superclass using data property
                // use superclass and type for getting individuals equivalent to input model
                System.out.println("Ignore list: " + assumptionsIgnore);

                boolean addCommaEquivalencyRule = false;
                for (Model model : models) {

                    System.out.println("\n\nModel: " + model.getId());

                    for (Variable input : model.getInputs()) {
                        System.out.println("Input: " + input);
                        equivalencyStringBuilder.append(
                                queryEquivalentIndividualsAndSerialize(input, parser, reasoner, dataFactory, ontology,
                                        assumptionsIgnoreList, addCommaEquivalencyRule));
                        addCommaEquivalencyRule = true;
                    }
                    for (Variable output : model.getOutputs()) {
                        System.out.println("Output: " + output);
                        equivalencyStringBuilder.append(queryEquivalentIndividualsAndSerialize(output, parser, reasoner,
                                dataFactory, ontology, assumptionsIgnoreList, addCommaEquivalencyRule));
                    }
                }

                // if (reasoner.isConsistent()) {
                // /**
                // * run reasoner
                // * for each model
                // ** for each input
                // *** call create equivalency rule, concatenate to rules
                // ** end for inputs
                // ** for each output
                // *** call create equivalency rule, concatenate to rules
                // ** end for outputs
                // * end for models
                // * return equivalency rules
                // */
                // /**
                // * (method) create equivalency rule
                // * create DL query = get type
                // * for each assumption
                // ** if the String set exceptions does not contain assumption
                // *** concatenate assumption to DL query
                // ** end if exceptions
                // * end for
                // * run DL query on reasoner, get individuals
                // * create equivalency rule
                // * return equivalency rule
                // */

                equivalencyStringBuilder.append("]");
                equivalencyStringBuilder.append(serializeAssumptionsIgnoreList(assumptionsIgnoreArray));
                return equivalencyStringBuilder.toString();

            } else {
                System.out.println("Rules are inconsistent");
                return ERROR;
            }

        } catch (OWLOntologyCreationException | JsonProcessingException e) {
            System.out.println("Couldn't process request into a variable (from JSON to Object)");
            System.out.println(e.getMessage());
            // } catch (OWLOntologyStorageException e) {
            // System.out.println("Couldn't save ontology in hard drive");
            // System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ERROR;

    }

    /**
     * Retrieve variables that are semantically equivalent and serialize them
     * @param variable
     * @param parser
     * @param reasoner
     * @param dataFactory
     * @param ontology
     * @param assumptionsIgnoreList
     * @param addCommaEquivalencyRule
     * @return
     */
    private Object queryEquivalentIndividualsAndSerialize(Variable variable,
            ManchesterOWLSyntaxClassExpressionParser parser, OWLReasoner reasoner,
            OWLDataFactory dataFactory, OWLOntology ontology, HashSet<String> assumptionsIgnoreList,
            boolean addCommaEquivalencyRule) {

        StringBuilder dlQueryEquivalency = new StringBuilder(variable.getType());

        StringBuilder equivalencyStringBuilder = new StringBuilder();

        HashMap<String, String> variableAssumptions = variable.getAssumptions();
        PrefixManager prefixManager = (PrefixManager) ontology.getFormat();

        String compoundAssumption = "";

        String assumptionValue = "";
        Set<OWLDataPropertyRangeAxiom> rangeAxioms = null;
        OWLLiteral literal = null;

        OWLClass thingClass = dataFactory.getOWLThing();
        OWLClass nothingClass = dataFactory.getOWLNothing();

        if (variableAssumptions != null) {
            // for every assumption, get superclass (if any)
            // concat to dlqueryequivalency
            OWLDataProperty dataProperty;
            Set<OWLClassExpression> compoundOWLClassExpressionSet = new HashSet<>();
            for (String assumption : variableAssumptions.keySet()) {
                rangeAxioms = null;
                literal = null;

                if (!assumptionsIgnoreList.contains(assumption)) {
                    System.out.println("-----------------");
                    System.out.println("Assumption: " + assumption);

                    if (assumption.equals("hasLatitude") || assumption.equals("hasLongitude")) {

                        OWLDataProperty compoundDataProperty = dataFactory
                                .getOWLDataProperty(prefixManager.getDefaultPrefix() + "#" + assumption);

                        // Create the individual values (literals)

                        assumptionValue = variableAssumptions.get(assumption);
                        // Get range of assumption (data property)
                        rangeAxioms = ontology.getDataPropertyRangeAxioms(compoundDataProperty);

                        if (rangeAxioms.isEmpty()) {
                            System.out.println(
                                    "The component catalog contains a variable that uses a scientific assumption (data property) undefined");
                            return ERROR;
                        }
                        OWLDataPropertyRangeAxiom axiom = rangeAxioms.iterator().next();
                        OWLDataRange range = axiom.getRange();

                        System.out.println("Range: " + range.toString());

                        OWLDatatype datatype = range.asOWLDatatype();

                        literal = dataFactory.getOWLLiteral(assumptionValue, datatype);

                        OWLClassExpression hasValueRestriction = dataFactory.getOWLDataHasValue(compoundDataProperty,
                                literal);
                        compoundOWLClassExpressionSet.add(hasValueRestriction);
                        if (compoundOWLClassExpressionSet.size() > 1) {
                            // Combine the axioms into a class expression
                            OWLClassExpression compoundQueryExpression = dataFactory
                                    .getOWLObjectIntersectionOf(compoundOWLClassExpressionSet);
                            // Get superclasses based on the hasValue restriction
                            Set<OWLClass> superClassSet = reasoner.getSuperClasses(compoundQueryExpression, true)
                                    .getFlattened();

                            // If there is not superclass:

                            System.out.println("DL query: " + hasValueRestriction.toString());

                            System.out.println("Superclasses size: " + superClassSet.size());
                            for (OWLClass superclassFound : superClassSet) {
                                System.out.println(superclassFound.toString());
                            }

                            if (!superClassSet.isEmpty() && superClassSet.size() == 1 && !superClassSet.stream()
                                    .anyMatch(cls -> cls.equals(thingClass) || cls.equals(nothingClass))) {
                                for (OWLClass superClassObj : superClassSet) {
                                    System.out.println("Superclass found: " + superClassObj.getIRI().getFragment());
                                    dlQueryEquivalency.append(" and " + superClassObj.getIRI().getFragment());
                                }
                            } else {
                                // dlQueryEquivalency.append(" and " + assumption + " value \""
                                //         + variableAssumptions.get(assumption) + "\"");
                            }
                        }

                    } else {

                        dataProperty = dataFactory
                                .getOWLDataProperty(prefixManager.getDefaultPrefix() + "#" + assumption);
                        assumptionValue = variableAssumptions.get(assumption);
                        // get superclass (if any)

                        // Get range of assumption (data property)
                        rangeAxioms = ontology.getDataPropertyRangeAxioms(dataProperty);

                        if (rangeAxioms.isEmpty()) {
                            System.out.println(
                                    "The component catalog contains a variable that uses a scientific assumption (data property) undefined");
                            return ERROR;
                        }
                        OWLDataPropertyRangeAxiom axiom = rangeAxioms.iterator().next();
                        OWLDataRange range = axiom.getRange();

                        System.out.println("Range: " + range.toString());

                        OWLDatatype datatype = range.asOWLDatatype();

                        literal = dataFactory.getOWLLiteral(assumptionValue, datatype);

                        // Create a hasValue restriction
                        OWLDataHasValue hasValueRestriction = dataFactory.getOWLDataHasValue(dataProperty, literal);

                        // Get superclasses based on the hasValue restriction
                        Set<OWLClass> superClassSet = reasoner.getSuperClasses(hasValueRestriction, true)
                                .getFlattened();

                        System.out.println("DL query: " + hasValueRestriction.toString());

                        System.out.println("Superclasses size: " + superClassSet.size());
                        for (OWLClass superclassFound : superClassSet) {
                            System.out.println(superclassFound.toString());
                        }

                        if (!superClassSet.isEmpty() && superClassSet.size() == 1 && !superClassSet.stream()
                                .anyMatch(cls -> cls.equals(thingClass) || cls.equals(nothingClass))) {
                            for (OWLClass superClassObj : superClassSet) {
                                System.out.println("Superclass found: " + superClassObj.getIRI().getFragment());
                                dlQueryEquivalency.append(" and " + superClassObj.getIRI().getFragment());
                            }
                        } else {
                            dlQueryEquivalency.append(" and " + assumption + " value \""
                                    + variableAssumptions.get(assumption) + "\"");
                        }
                    }
                }
            } // end of traversing assumptions
        }

        // dlquery has been constructed, get individuals that satisfy that query
        System.out.println("\nQuerying individuals with: " + dlQueryEquivalency.toString());
        OWLClassExpression classExpression = parser.parse(dlQueryEquivalency.toString());
        Set<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression,
                false).getFlattened();
        if (!individuals.isEmpty()) {
            System.out.println("The query contains individuals: " + dlQueryEquivalency);
            for (OWLNamedIndividual indv : individuals) {
                System.out.println(indv.getIRI());
            }
            // serializing individuals as an equivalency rule
            equivalencyStringBuilder.append(addCommaEquivalencyRule ? "," : "");
            equivalencyStringBuilder.append(serializeIndividuals(individuals));
        } else {
            System.out.println("No individuals found");
        }
        System.out.println();
        return equivalencyStringBuilder.toString();
    }

    /**
     * Returns an IRI short form including an IRI with leading numbers
     * 
     * @param individual
     * @return
     */
    private String customShortForm(OWLNamedIndividual individual) {
        String iriString = individual.getIRI().toString();
        String shortForm = iriString.substring(iriString.lastIndexOf("#") + 1);

        return shortForm;
    }

    /**
     * Serializes assumptions ignored in the reconciliation process
     * @param assumptionsIgnoreArray
     * @return
     */
    private Object serializeAssumptionsIgnoreList(String[] assumptionsIgnoreArray) {
        StringBuilder ignoreList = new StringBuilder(",\"ignore\":[");
        boolean addComma = false;
        for (String assumptionToIgnore : assumptionsIgnoreArray) {
            ignoreList.append(addComma ? "," : "");
            ignoreList.append("\"" + assumptionToIgnore + "\"");
            addComma = true;
        }
        ignoreList.append("]}");
        return ignoreList.toString();
    }

    private OWLEntityChecker getEntityChecker(OWLOntologyManager owlManager, OWLOntology ontology) {

        ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
        Set<OWLOntology> importsClosure = ontology.getImportsClosure();

        BidirectionalShortFormProvider bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(owlManager,
                importsClosure, shortFormProvider);

        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        return entityChecker;
    }

    private void createIndividuals(OWLDataFactory dataFactory, PrefixManager prefixManager,
            OWLOntology ontology, OWLOntologyManager owlManager, Variable[] variables) {
        if (variables == null || variables.length == 0) {
            return;
        }

        // Process variables from inputs into individuals
        for (Variable variable : variables) {
            // Create individuals and add them to class
            OWLNamedIndividual owlIndividual = dataFactory.getOWLNamedIndividual("#" + variable.getId(),
                    prefixManager);
            OWLClass owlClass = dataFactory.getOWLClass("#" + variable.getType(), prefixManager);
            OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(owlClass, owlIndividual);
            owlManager.addAxiom(ontology, classAssertion);

            // Create data properties from assumptions
            HashMap<String, String> assumptions = variable.getAssumptions();
            String assumption;
            OWLDataProperty owlAssumption;
            // OWLAxiom dataPropertyAxiom;

            if (assumptions != null && !assumptions.isEmpty()) {
                for (String assumptionKey : assumptions.keySet()) {
                    assumption = assumptions.get(assumptionKey);

                    // Create data property and attach it to individual
                    owlAssumption = dataFactory.getOWLDataProperty("#" + assumptionKey, prefixManager);
                    // 4. Query the ontology to retrieve the range of the `hasAge` data property.
                    Set<OWLDataPropertyRangeAxiom> rangeAxioms = ontology.getDataPropertyRangeAxioms(owlAssumption);

                    for (OWLDataPropertyRangeAxiom axiom : rangeAxioms) {
                        OWLDataRange range = axiom.getRange();
                        OWLDatatype datatype = range.asOWLDatatype();
                        OWLLiteral ageLiteral = dataFactory.getOWLLiteral(assumption, datatype);
                        OWLDataPropertyAssertionAxiom dataPropertyAssertion = dataFactory
                                .getOWLDataPropertyAssertionAxiom(owlAssumption, owlIndividual, ageLiteral);
                        owlManager.addAxiom(ontology, dataPropertyAssertion);
                        System.out.println("The range of " + owlAssumption + " is: " + range);
                    }

                }
            }
        } // End processing variables from inputs
    }

    /**
     * Serializes individuals as JSON
     * Ex: { "equivalence" : ["evap_rat_p", "SWEvapV2_ft", "var3"]}
     * 
     * @param equivalentIndividuals
     * @return String, individuals serialized as JSON
     */
    private String serializeIndividuals(Set<OWLNamedIndividual> equivalentIndividuals) {
        if (equivalentIndividuals == null || equivalentIndividuals.size() == 0)
            return "";

        // Sorting individuals

        // Using a TreeSet with a custom comparator to maintain sorting order
        TreeSet<OWLNamedIndividual> individualsSorted = new TreeSet<>(
                Comparator.comparing(individual -> customShortForm(individual), String.CASE_INSENSITIVE_ORDER));

        // Adding all elements to the TreeSet
        individualsSorted.addAll(equivalentIndividuals);

        String serialization = "{\"equivalence\":[";
        int counter = 0;
        for (OWLNamedIndividual ind : individualsSorted) {
            if (counter > 0)
                serialization += ",";
            serialization += "\"" + customShortForm(ind) + "\"";
            counter++;
        }
        serialization += "]}";
        return serialization;
    }
}
