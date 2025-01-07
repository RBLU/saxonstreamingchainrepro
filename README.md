Two transformations: (first unzip the source.zip in place)

1. exeucutes  the two streaming trafos serially with savin to disk inbetween.
  -> both accumulators in xslt2 work correctly
  -> no OOM with Xml200MB  and an Input file of 131MB and >100'000 Document nodes

2. chaines the executeion of the two streaming trafos directly with trafo.asDocumentDestionation.
  -> accumulators do not give any value back
   -> OOM shortly after starting.
   
