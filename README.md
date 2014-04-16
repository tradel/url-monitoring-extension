URL Monitor for AppDynamics
===========================

An AppDynamics Machine Agent extension to visit a set of URL's and report whether they are up or down.

This extension requires the Java Machine Agent.

## Installation ##

1. Download KeynoteMonitor.zip from AppSphere.
1. Copy KeynoteMonitor.zip into the directory where you installed the machine agent, under `$AGENT_HOME/monitors`.
1. Unzip the file. This will create a new directory called KeynoteMonitor.
1. In $AGENT_HOME/monitors/KeynoteMonitor, edit the file monitor.xml and configure the plugin.
1. Restart the machine agent.

## Configuration ##

Configuration for this monitor is in the `monitor.xml` file in the monitor directory. All of the configurable options are in the `<task-arguments>` section.

api_key
: An API key generated from the [Keynote API console][]. Required.

exclude_slots
: A comma-separated list of measurement slot names to be excluded from import. You can use regular expressions for more specific matching. Optional.

## Metrics Provided ##

### Per-Queue Metrics

The following metrics are reported for each measurement slot defined in Keynote:

| Metric Name           | Description |
| :-------------------- | :---------- |
| PerformanceData       | Total time to execute the measurement script. |
| AvailabilityData      | External availability of the resource, expressed as a percentage. |

## Support

For any questions or feature requests, please contact the [AppDynamics Center of Excellence][].

**Version:** 1.0
**Controller Compatibility:** 3.6 or later
**Last Updated:** 12/11/2013
**Author:** Todd Radel

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppSphere][] community.

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

------------------------------------------------------------------------------

## Release Notes ##

### Version 1.0
- Initial release to AppSphere.


[Keynote API console]: http://api.keynote.com/apiconsole/apikeygen.aspx
[GitHub]: https://github.com/Appdynamics/keynote-monitoring-extension
[AppSphere]: http://appsphere.appdynamics.com/t5/eXchange/F5-Monitoring-Extension/idi-p/2063
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com