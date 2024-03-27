package com.jetbrains.codegen.docs;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.*;

import static io.swagger.codegen.DefaultCodegen.camelize;

public class TeamCityXMLExampleGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TeamCityXMLExampleGenerator.class);
    private static final Integer MAX_MODEL_DEPTH = 3;

    protected Map<String, Model> definitions;

    public TeamCityXMLExampleGenerator(Map<String, Model> definitions) {
        this.definitions = definitions;
        if (definitions == null) {
            this.definitions = new HashMap<>();
        }
    }

    protected String modelToXml(String modelName, ModelImpl model) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement(modelName);
        serializeModelToElement(doc, modelName, model, 0, rootElement);

        return convertXmlDocumentToString(doc, rootElement);
    }

    protected String propertyToXml(Property property) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        final String propName = property.getName();
        Element rootElement = doc.createElement(propName);
        serializePropertyToXml(doc, propName, property, 0, rootElement);

        return convertXmlDocumentToString(doc, rootElement);
    }

    private String convertXmlDocumentToString(Document doc, Element rootElement) throws TransformerException {
        doc.appendChild(rootElement);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(
                new DOMSource(doc), new StreamResult(writer)
        );
        return writer.toString();
    }

    protected void serializeModelToElement(Document doc, String modelName, ModelImpl model, int depth, Element targetElement) {
        if (model != null) {
            if (depth >= MAX_MODEL_DEPTH) {
                targetElement.appendChild(
                        doc.createTextNode(
                                String.format("[[[%s...|%s.md]]]", modelName, camelize(modelName, true))
                        )
                );
            }
            else {
                if (model.getProperties() != null) {
                    model.getProperties().forEach((propertyName, property) -> {
                        serializePropertyToXml(doc, propertyName, property, depth + 1, targetElement);
                    });
                }
            }
        }
    }

    protected void serializePropertyToXml(Document doc, String name, Property property, int depth, Element parentElement) {
        try {
            if (property != null) {
                if (property instanceof ArrayProperty) {
                    ArrayProperty p = (ArrayProperty) property;
                    Property inner = p.getItems();
                    serializePropertyToXml(doc, name, inner, depth + 1, parentElement);
                }
                else if (property instanceof RefProperty) {
                    RefProperty ref = (RefProperty) property;
                    String refModelName = ref.getSimpleRef();
                    ModelImpl actualModel = (ModelImpl) definitions.get(refModelName);
                    Element refElement = doc.createElement(name);
                    serializeModelToElement(doc, refModelName, actualModel, depth + 1, refElement);
                    parentElement.appendChild(refElement);
                }
                else {
                    parentElement.setAttribute(
                            name.replace("$", ""),
                            getExample(property)
                    );
                }
            }
        } catch (DOMException e) {
            throw new DOMException(
                    (short) 5,
                    "INVALID_CHARACTER_ERR, name: "
                            + name
                            + ", example string: "
                            + getExample(property)
            );
        }
    }


            /**
             * Get the example string value for the given Property.
             * <p>
             * If an example value was not provided in the specification, a default will be generated.
             *
             * @param property Property to get example string for
             * @return Example String
             */
    protected String getExample(Property property) {
        if (!(property.getExample() == null || property.getExample() == "")) {
            return property.getExample().toString();
        } else if (property instanceof DateTimeProperty) {
            return "2000-01-23T04:56:07.000Z";
        } else if (property instanceof DateProperty) {
            return "2000-01-23";
        } else if (property instanceof BooleanProperty) {
            return "true";
        } else if (property instanceof LongProperty) {
            return "123456789";
        } else if (property instanceof DoubleProperty) { // derived from DecimalProperty so make sure this is first
            return "3.149";
        } else if (property instanceof DecimalProperty) {
            return "1.3579";
        } else if (property instanceof PasswordProperty) {
            return "********";
        } else if (property instanceof UUIDProperty) {
            return "046b6c7f-0b8a-43b9-b35d-6489e6daee91";
            // do these last in case the specific types above are derived from these classes
        } else if (property instanceof StringProperty) {
            return "string";
        } else if (property instanceof BaseIntegerProperty) {
            return "123";
        } else if (property instanceof AbstractNumericProperty) {
            return "1.23";
        } else if (property instanceof MapProperty) {
            return "Map{}";
        }

        logger.warn("default example value not implemented for " + property);
        return "";
    }
}