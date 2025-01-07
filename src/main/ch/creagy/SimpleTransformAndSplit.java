
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
}
