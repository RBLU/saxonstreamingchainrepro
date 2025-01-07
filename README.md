Two transformations: (first unzip the source.zip in place)

1. exeucutes  the two streaming trafos (xslt1 and xslt2) serially with savin to disk inbetween.
  - both accumulators in xslt2 work correctly
  - no OOM with -Xmx200M  and an Input file of 131MB containing >100'000 Document nodes

2. chaines the executeion of the two streaming trafos directly with trafo1.applyTemplates(input, trafo2.asDocumentDestionation(finalDest))
  - accumulators do not give any value back
  - OOM shortly after starting.
   
