# SystemZee's Workshop


### Build Instructions

SystemZee's Workshop uses the Fabric data generation system to prevent us from having to manually write over one hundred recipes just for the Sawmill.

When setting up your workspace, make sure to run the `Data Generation` IntelliJ IDEA run configuration, or the `runDatagenClient` Gradle task, otherwise datagen recipes will not be populated. This task must be re-run every time a change is made to `SZDatagen`.
