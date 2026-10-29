package com.example.calculator.endpoint;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;

@Endpoint
public class CalculatorEndpoint {

    private static final String NAMESPACE =
            "http://example.com/calculator";

    @PayloadRoot(namespace = NAMESPACE, localPart = "AddRequest")
    @ResponsePayload
    public Source add(@RequestPayload Source request) throws Exception {
        return calculate(request, "AddResponse", "add");
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "SubtractRequest")
    @ResponsePayload
    public Source subtract(@RequestPayload Source request) throws Exception {
        return calculate(request, "SubtractResponse", "subtract");
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "MultiplyRequest")
    @ResponsePayload
    public Source multiply(@RequestPayload Source request) throws Exception {
        return calculate(request, "MultiplyResponse", "multiply");
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DivideRequest")
    @ResponsePayload
    public Source divide(@RequestPayload Source request) throws Exception {
        return calculate(request, "DivideResponse", "divide");
    }

    private Source calculate(
            Source request, String responseName, String operation)
            throws Exception {

        Transformer transformer =
                TransformerFactory.newInstance().newTransformer();

        DOMResult requestResult = new DOMResult();
        transformer.transform(request, requestResult);

        Element root = ((Document) requestResult.getNode())
                .getDocumentElement();

        BigDecimal a = getValue(root, "a");
        BigDecimal b = getValue(root, "b");

        BigDecimal result;

        switch (operation) {
            case "add":
                result = a.add(b);
                break;
            case "subtract":
                result = a.subtract(b);
                break;
            case "multiply":
                result = a.multiply(b);
                break;
            case "divide":
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException(
                            "Division by zero is not allowed.");
                }
                result = a.divide(b, 10, RoundingMode.HALF_UP);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation");
        }

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Document responseDocument =
                factory.newDocumentBuilder().newDocument();

        Element responseRoot = responseDocument.createElementNS(
                NAMESPACE, responseName);

        Element resultElement = responseDocument.createElementNS(
                NAMESPACE, "result");

        resultElement.setTextContent(result.stripTrailingZeros().toPlainString());

        responseRoot.appendChild(resultElement);
        responseDocument.appendChild(responseRoot);

        return new DOMSource(responseDocument);
    }

    private BigDecimal getValue(Element root, String name) {
        for (int i = 0; i < root.getChildNodes().getLength(); i++) {
            Node node = root.getChildNodes().item(i);

            if (node instanceof Element element
                    && name.equals(element.getLocalName())) {
                return new BigDecimal(element.getTextContent());
            }
        }

        throw new IllegalArgumentException(
                "Missing request parameter: " + name);
    }
}