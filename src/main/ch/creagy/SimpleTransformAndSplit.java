
package ch.creagy;

import net.sf.saxon.s9api.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;

public class SimpleTransformAndSplit {

        public static void main(String[] args) {

                try {
                        String infile= "test\\source.xml";

                        // Saxon Processor initialisieren

                        Processor processor = new Processor();
                        System.out.println("Using Saxon: " + processor.getSaxonProductVersion() + processor.getSaxonEdition());

                        XsltCompiler compiler = processor.newXsltCompiler();

                        // XSLT-Dateipfade - XsltExecutable compilieren

                        XsltExecutable xslt1 = compiler
                                        .compile(new StreamSource(new File(
                                                        "src\\xslt\\xslt1.xslt")));

                        XsltExecutable xslt2 = compiler
                                        .compile(new StreamSource(
                                                        new File("src\\xslt\\xslt2.xslt")));

                        // Ausgabe-Verzeichnis
                        String outputDir = "test/out";
                        new File(outputDir).mkdirs();


                        ///////////// first trafo ///////////////U        
                        StreamSource inputXmlSource = new StreamSource(new FileInputStream(infile));
                        Path intermediateFile = new File(outputDir, "afterXslt1.xml").toPath();
                
                        // Load trafos for separate execution with saving to Filesystem inbetween
                        Xslt30Transformer trafo1 = xslt1.load30();
                        trafo1.setInitialMode(new QName("burststream"));
                        OutputStream afterXslt1 = Files.newOutputStream(intermediateFile);
        
                        long start = System.currentTimeMillis();
                        trafo1.applyTemplates(inputXmlSource, trafo1.newSerializer(afterXslt1));
                        afterXslt1.close();
                        System.out.println("duration xslt1:" + (System.currentTimeMillis()-start));

                        ///////////// second trafo ///////////////U        
                        Xslt30Transformer trafo2 = xslt2.load30();
                        trafo2.setInitialMode(new QName("burststream"));
                        OutputStream afterXslt2 = Files.newOutputStream(new File(outputDir, "afterXslt2.xml").toPath());
        
                        StreamSource intermediateSource =new StreamSource(Files.newInputStream(intermediateFile)); 
                        long start2 = System.currentTimeMillis();
                        trafo2.applyTemplates(intermediateSource, trafo2.newSerializer(afterXslt2));
                        System.out.println("duration xslt2:" + (System.currentTimeMillis()-start2));
                        System.out.println("duration xslt1 + xslt2:" + (System.currentTimeMillis()-start));
                        afterXslt2.close();
                        inputXmlSource.getInputStream().close();
                        intermediateSource.getInputStream().close();

                        ///////////// chained trafo ///////////////U        
        
                        // recerate inputsource from file
                        inputXmlSource = new StreamSource(new FileInputStream(infile));
                        trafo1 = xslt1.load30();
                        trafo1.setInitialMode(new QName("burststream"));
                        trafo2 = xslt2.load30();
                        trafo2.setInitialMode(new QName("burststream"));
                        OutputStream afterChained = Files.newOutputStream(new File(outputDir, "afterChained.xml").toPath());
                        start = System.currentTimeMillis();
                        trafo1.applyTemplates(inputXmlSource, trafo2.asDocumentDestination(trafo2.newSerializer(afterChained)));
                        System.out.println("duration chained:" + (System.currentTimeMillis()-start));

                        System.out.println("Transformation und Split abgeschlossen.");

                } catch (Exception e) {

                        e.printStackTrace();

                }

        }



        /**
         * 
         * Extrahiert alle <Dok>-Elemente aus dem transformierten XML und speichert sie
         * als separate Dateien.
         *
         * 
         * 
         * @param
         * processor             Saxon Processor
         * 
         * @param
         * transformedXml        Transformiertes XML
         * 
         * @param
         * outputDir             Zielverzeichnis
         * 
         * @throws
         * SaxonApiException         Wenn ein Fehler bei der Verarbeitung auftritt
         * 
         */

        private static void extractAndSaveDokElements(Processor processor,
                        XdmNode transformedXml,
                        String outputDir)
                        throws SaxonApiException {

                XPathCompiler xpath = processor.newXPathCompiler();
                XPathSelector selector = xpath.compile("//Dok").load();

                selector.setContextItem(transformedXml);

                int fileCounter = 1; // Zähler für Dateinamen

                // Jedes <Dok>-Element verarbeiten
                for (XdmItem dokItem : selector) {

                        XdmNode dokNode = (XdmNode) dokItem;

                        // <Export>-Wrapper erstellen
                        XdmNode exportNode = wrapDokWithCc(processor,
                                        dokNode);

                        // Datei speichern
                        String outputFilePath = outputDir +
                                        "/Dok" +
                                        fileCounter++
                                        + ".xml";

                        saveToFile(processor,
                                        exportNode,
                                        new File(outputFilePath));

                        System.out.println("Gespeichert: "
                                        +
                                        outputFilePath);

                }

        }

        /**
         * 
         * Umhüllt ein <Dok>-Element mit einem <Cc>-Element.
         *
         * 
         * 
         * @param
         * processor        Saxon Processor
         * 
         * @param
         * dokNode          Das zu umhüllende <Dok>-Element
         * 
         * @return Ein neues XdmNode mit der Struktur <Cc><Dok/></Cc>
         * 
         * @throws
         * SaxonApiException         Wenn ein Fehler bei der Verarbeitung auftritt
         * 
         */

        private static XdmNode wrapDokWithCc(Processor processor, XdmNode dokNode) throws SaxonApiException {

                XdmDestination destination = new XdmDestination();

                XsltCompiler compiler = processor.newXsltCompiler();

                XsltExecutable executable = compiler.compile(new StreamSource(new java.io.StringReader(
                                "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform version=\"3.0\">"
                                                + "<xsl:output method=\"xml version=\"1.0\" encoding=\"UTF-8 indent=\"yes\"/>"
                                                + "<xsl:template match=\"Dok\">"
                                                + "<Cc><xsl:copy-of select=\".\"/></Cc>"
                                                + "</xsl:template>"
                                                + "</xsl:stylesheet>")));

                XsltTransformer transformer = executable.load();

                transformer.setInitialContextNode(dokNode);

                transformer.setDestination(destination);

                transformer.transform();

                return destination.getXdmNode();

        }

        /**
         * 
         * Speichert einen XdmNode in eine Datei.
         *
         * 
         * 
         * @param
         * processor         Saxon Processor
         * 
         * @param
         * node              XdmNode (XML-Daten)
         * 
         * @param
         * outputFile        Zieldatei
         * 
         * @throws
         * SaxonApiException         Wenn ein Fehler beim Schreiben auftritt
         * 
         */

        private static void saveToFile(Processor processor, XdmNode node, File outputFile) throws SaxonApiException {

                Serializer serializer = processor.newSerializer(outputFile);

                serializer.setOutputProperty(Serializer.Property.METHOD,
                                "xml");

                serializer.setOutputProperty(Serializer.Property.INDENT,
                                "yes");

                serializer.serializeNode(node);

        }

}
