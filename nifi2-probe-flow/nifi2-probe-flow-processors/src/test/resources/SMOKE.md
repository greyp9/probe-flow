# Smoke Test

1) Start NiFi.
1) (NiFi canvas) Add NAR to folder `$NIFI_HOME/extensions`.  
2) (NiFi app log)  Check log for successful `org.apache.nifi.nar.NarAutoLoader` messages.
1) (NiFi canvas) Add processor to NiFi canvas.  Action `Configure`.  Add new controller service in properties.  Action `Go to Service`.
1) Action `Configure` for controller service.  Set `Port`.  Set `Basic Authentication` (pattern "user:pass").  Add `SSL Context Service`.
1) Action `Enable` for controller services.
1) (ProbeFlow UI) Navigate web browser to `https://$NIFI_IP:port`.  Authenticate using controller service Basic Authentication credentials.
1) (NiFi canvas) Start `ProbeFlow` (PF) processor from NiFi canvas.  Observe processor appear in PF home page `Active Processors` list.
1) (ProbeFlow UI) Click `ProcessorID` link in `Active Processors` list to navigate to processor page.
1) (NiFi canvas) Add a `GenerateFlowFile` processor.  Attach to `ProbeFlow` via `GFF` output relationship `Success`.
1) Add a funnel.  Attach to `ProbeFlow` via `PF` output relationship `Outgoing`.
1) Action `Run Once` on `GFF`. Observe FlowFile in queue connecting `GFF` to `PF`.
1) (ProbeFlow UI) click button `Accept Incoming FlowFile`.  Observe FlowFile appear in `FlowFiles` list.
1) (NiFi canvas) Observe FlowFile disappear from `GFF -> PF` queue.
1) (ProbeFlow UI) Click `Metadata` link for FlowFile to check FlowFile attributes.
1) Click `Content` link for FlowFile to check FlowFile content.
1) Click `Outgoing` button to route FlowFile to `PF` output relationship `Outgoing`.
1) (NiFi canvas) Observe FlowFile appear in `PF -> $Outgoing` queue.
