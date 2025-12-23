# NiFi ProbeFlow custom NAR

A Maven project for building a custom NiFi Archive (.nar). Designed for interoperability
with [Apache NiFi](https://github.com/apache/nifi) version 1.18+. The NAR contains a processor and a controller service
that allow for manual manipulation of FlowFile attributes / content, in the context of a running NiFi instance.

The controller service instantiates an SSL web server listening on the configured port, providing for user
interactivity. Instances of the processor may be added to the canvas, and configured to accept upstream FlowFiles, to
examine data associated with the FlowFiles, and to route them to configurable relationships.

The components in this NAR might be useful during flow development to manually debug FlowFile content as it traverses
the flow. It is not designed for use in a production flow. Manual intervention is needed to allow data to transit
configured ProbeFlow processors. It should be considered to be a debugging aid during flow development, rather than a
participating component in a production flow.

My goals in developing this NAR were to familiarize myself with the NiFi processor API and mechanics of custom NAR
development, to leverage past experience with similar technologies, and to develop novel components that might be
generally useful. I hope others might benefit from this work.

## Component Usage

See the NiFi documentation for information about how to incorporate this NAR into your running NiFi instance.

- [NiFi Archives (developer documentation)](https://nifi.apache.org/docs/nifi-docs/html/developer-guide.html#nars)

Additional guidance:

- [Auto Loading Nars in Apache NiFi](https://www.nifi.rocks/auto-loading-extensions/)
- [NiFi NAR Files Explained](https://medium.com/hashmapinc/nifi-nar-files-explained-14113f7796fd)
- [Apache NiFi - Component Versioning](https://bryanbende.com/development/2017/05/10/apache-nifi-1-2-0-component-versioning)

## SSL Context Service

In order to provide a secure TLS connection between the ProbeFlow controller service and the browser UI, a valid NiFi
SSLContextService must be defined and enabled. The default keystore and truststore specified in `conf/nifi.properties`
may be used to specify the parameters of this SSLContextService, or an additional keystore / truststore may be generated
using the [NiFi Toolkit](https://nifi.apache.org/docs/nifi-docs/html/administration-guide.html#tls_generation_toolkit).

## Additional Details

- Additional details for the ProbeFlow processor may be
  found [here](https://htmlpreview.github.io/?https://github.com/greyp9/probe-flow/blob/main/nifi-probe-flow-processors/src/main/resources/docs/io.github.greyp9.nifi.pf.processor.ProbeFlow/additionalDetails.html).
- Additional details for the ProbeFlow controller service may be
  found [here](https://htmlpreview.github.io/?https://github.com/greyp9/probe-flow/blob/main/nifi-probe-flow-processors/src/main/resources/docs/io.github.greyp9.nifi.pf.service.ProbeFlowService/additionalDetails.html).

## Usage Scenarios

### Save a Test Vector of Useful FlowFiles

While testing the output of an upstream component, it is possible to save a set of useful FlowFiles for future testing.

[This XML file](./nifi-probe-flow-processors/src/test/resources/export/probeflow.records.xml) is an example of such an
archived FlowFile set. It contains a single FlowFile, which specifies five test records. It may be uploaded from the
main page of the `ProbeFlow` web interface.

1) From the front page of the ProbeFlow web interface, click the "Download / Upload State" link for a running processor.
1) Click the "Choose File" button, and select the XML file referenced above.
1) Click the "Upload Content" button.
1) Navigate to the "viewer" resource for the relevant processor to see the uploaded content.
